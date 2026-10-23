package com.deen.app.prayer

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.deen.app.MainActivity
import com.deen.app.R
import com.deen.app.data.Prefs
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.tan

data class City(val name: String, val lat: Double, val lng: Double)

val CITIES = listOf(
    City("ঢাকা", 23.8103, 90.4125),
    City("চট্টগ্রাম", 22.3569, 91.7832),
    City("সিলেট", 24.8949, 91.8687),
    City("রাজশাহী", 24.3745, 88.6042),
    City("খুলনা", 22.8456, 89.5403),
    City("বরিশাল", 22.7010, 90.3535),
    City("রংপুর", 25.7439, 89.2752),
    City("ময়মনসিংহ", 24.7471, 90.4203),
    City("কুমিল্লা", 23.4607, 91.1809),
)

enum class Prayer(val bn: String) {
    FAJR("ফজর"), SUNRISE("সূর্যোদয়"), DHUHR("যোহর"), ASR("আসর"), MAGHRIB("মাগরিব"), ISHA("ইশা");

    fun label(date: LocalDate) = if (this == DHUHR && date.dayOfWeek == DayOfWeek.FRIDAY) "জুমা" else bn
}

val SALAH = listOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA)

/**
 * নামাজের সময় হিসাব (praytimes.org-এর অ্যালগরিদম)।
 * পদ্ধতি: University of Islamic Sciences, Karachi — ফজর ও ইশা ১৮°; আসর হানাফি (ছায়া ২ গুণ) বা শাফেয়ি (১ গুণ)।
 */
object PrayerCalc {
    private fun dtr(d: Double) = d * PI / 180.0
    private fun rtd(r: Double) = r * 180.0 / PI
    private fun fixAngle(a: Double) = a - 360.0 * floor(a / 360.0)
    private fun fixHour(a: Double) = a - 24.0 * floor(a / 24.0)

    private fun julian(date: LocalDate): Double {
        var y = date.year
        var m = date.monthValue
        val d = date.dayOfMonth
        if (m <= 2) { y -= 1; m += 12 }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + d + b - 1524.5
    }

    /** returns (declination°, equation of time in hours) */
    private fun sun(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(dtr(g)) + 0.020 * sin(dtr(2 * g)))
        val e = 23.439 - 0.00000036 * d
        val ra = rtd(atan2(cos(dtr(e)) * sin(dtr(l)), cos(dtr(l)))) / 15.0
        val eqt = q / 15.0 - fixHour(ra)
        val decl = rtd(asin(sin(dtr(e)) * sin(dtr(l))))
        return decl to eqt
    }

    /** local clock hours for each prayer */
    fun compute(date: LocalDate, city: City, hanafi: Boolean, zone: ZoneId = ZoneId.systemDefault()): Map<Prayer, Double> {
        val tz = zone.rules.getOffset(date.atTime(12, 0)).totalSeconds / 3600.0
        val lat = city.lat
        val jd0 = julian(date) - city.lng / (15.0 * 24.0)

        fun midDay(t: Double) = fixHour(12.0 - sun(jd0 + t).second)
        fun angleTime(angle: Double, t: Double, before: Boolean): Double {
            val decl = sun(jd0 + t).first
            val noon = midDay(t)
            val x = (-sin(dtr(angle)) - sin(dtr(decl)) * sin(dtr(lat))) / (cos(dtr(decl)) * cos(dtr(lat)))
            val span = rtd(acos(x.coerceIn(-1.0, 1.0))) / 15.0
            return noon + if (before) -span else span
        }
        fun asrTime(factor: Double, t: Double): Double {
            val decl = sun(jd0 + t).first
            val angle = -rtd(atan(1.0 / (factor + tan(dtr(abs(lat - decl))))))
            return angleTime(angle, t, false)
        }

        var raw = doubleArrayOf(5.0, 6.0, 12.0, 13.0, 18.0, 18.0)
        repeat(2) {
            val p = raw.map { it / 24.0 }
            raw = doubleArrayOf(
                angleTime(18.0, p[0], true),
                angleTime(0.833, p[1], true),
                midDay(p[2]),
                asrTime(if (hanafi) 2.0 else 1.0, p[3]),
                angleTime(0.833, p[4], false),
                angleTime(18.0, p[5], false),
            )
        }
        val adj = raw.map { it + tz - city.lng / 15.0 }
        return Prayer.entries.mapIndexed { i, pr -> pr to adj[i] }.toMap()
    }

    fun times(prefs: Prefs, date: LocalDate): Map<Prayer, LocalDateTime> {
        val city = CITIES.getOrElse(prefs.cityIndex) { CITIES.first() }
        return compute(date, city, prefs.hanafiAsr).mapValues { (_, h) ->
            date.atStartOfDay().plusSeconds((h * 3600).roundToLong())
        }
    }

    /** পরের নামাজ (সূর্যোদয় বাদে)। */
    fun next(prefs: Prefs, now: LocalDateTime = LocalDateTime.now()): Pair<Prayer, LocalDateTime> {
        val today = times(prefs, now.toLocalDate())
        SALAH.forEach { p -> today[p]?.let { if (it.isAfter(now)) return p to it } }
        val tomorrow = now.toLocalDate().plusDays(1)
        return Prayer.FAJR to times(prefs, tomorrow).getValue(Prayer.FAJR)
    }
}

object Reminders {
    private const val CHANNEL = "prayer_times"
    private const val REQ = 7

    fun scheduleNext(ctx: Context) {
        val prefs = Prefs(ctx)
        val am = ctx.getSystemService(AlarmManager::class.java) ?: return
        val base = Intent(ctx, PrayerAlarmReceiver::class.java)
        am.cancel(PendingIntent.getBroadcast(ctx, REQ, base, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        if (!prefs.reminders) return

        val (prayer, at) = PrayerCalc.next(prefs, LocalDateTime.now().plusMinutes(1))
        val intent = Intent(ctx, PrayerAlarmReceiver::class.java).putExtra("prayer", prayer.name)
        val pi = PendingIntent.getBroadcast(ctx, REQ, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val millis = at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        try {
            val exact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
            if (exact) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
            else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
        } catch (_: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
        }
    }

    fun notify(ctx: Context, prayer: Prayer) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "নামাজের সময়", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "প্রতি ওয়াক্তের শুরুতে একটি নোটিফিকেশন"
            }
        )
        val open = PendingIntent.getActivity(
            ctx, 0, Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        val name = prayer.label(LocalDate.now())
        val n = NotificationCompat.Builder(ctx, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_deen)
            .setContentTitle("$name-এর সময় হয়েছে")
            .setContentText("চলুন, ওজু করে নামাজের প্রস্তুতি নিই।")
            .setContentIntent(open)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        try {
            NotificationManagerCompat.from(ctx).notify(prayer.ordinal + 100, n)
        } catch (_: SecurityException) { }
    }
}

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        intent.getStringExtra("prayer")?.let { name ->
            runCatching { Prayer.valueOf(name) }.getOrNull()?.let { Reminders.notify(context, it) }
        }
        Reminders.scheduleNext(context)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Reminders.scheduleNext(context)
    }
}
