package com.deen.app.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONObject
import java.time.LocalDate

data class Ayah(val n: Int, val ar: String, val tr: String, val bn: String)

data class Surah(
    val id: Int,
    val name: String,
    val meaning: String,
    val arabicName: String,
    val meccan: Boolean,
    val ayahs: List<Ayah>,
    /** "salah" = নামাজের ২০ সূরা, "special" = আয়াতুল কুরসি, কাহফ ১–১০, মুলক */
    val group: String = "salah",
    /** সূরার মাঝখান থেকে শুরু (যেমন আয়াতুল কুরসি) — বিসমিল্লাহ দেখানো হয় না */
    val partial: Boolean = false,
    val note: String = "",
)

/** assets/surahs.json — আরবি (quranenc Uthmani) ও বাংলা অর্থ (মুহিউদ্দীন খান, Tanzil)। */
object QuranRepo {
    @Volatile private var cache: List<Surah>? = null

    fun all(ctx: Context): List<Surah> = cache ?: synchronized(this) {
        cache ?: load(ctx).also { cache = it }
    }

    fun get(ctx: Context, id: Int): Surah = all(ctx).first { it.id == id }

    private fun load(ctx: Context): List<Surah> {
        val text = ctx.assets.open("surahs.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val arr = JSONObject(text).getJSONArray("surahs")
        return (0 until arr.length()).map { i ->
            val s = arr.getJSONObject(i)
            val ay = s.getJSONArray("ayahs")
            Surah(
                id = s.getInt("id"),
                name = s.getString("name"),
                meaning = s.getString("meaning"),
                arabicName = s.getString("arabicName"),
                meccan = s.getString("type") == "meccan",
                group = s.optString("group", "salah"),
                partial = s.optBoolean("partial", false),
                note = s.optString("note", ""),
                ayahs = (0 until ay.length()).map { j ->
                    val a = ay.getJSONObject(j)
                    Ayah(a.getInt("n"), a.getString("ar"), a.getString("tr"), a.getString("bn"))
                }
            )
        }
    }
}

/** সাধারণ SharedPreferences — UI আর রিমাইন্ডার রিসিভার দুজনেই ব্যবহার করে। */
class Prefs(ctx: Context) {
    private val sp = ctx.applicationContext.getSharedPreferences("deen", Context.MODE_PRIVATE)

    var cityIndex: Int
        get() = sp.getInt("city", 0)
        set(v) = sp.edit().putInt("city", v).apply()

    var hanafiAsr: Boolean
        get() = sp.getBoolean("hanafi", true)
        set(v) = sp.edit().putBoolean("hanafi", v).apply()

    var reminders: Boolean
        get() = sp.getBoolean("reminders", false)
        set(v) = sp.edit().putBoolean("reminders", v).apply()

    /** 0 = ফোনের মতো, 1 = লাইট, 2 = ডার্ক */
    var themeMode: Int
        get() = sp.getInt("theme", 0)
        set(v) = sp.edit().putInt("theme", v).apply()

    /** প্রতি ওয়াক্তে মিনিট যোগ/বিয়োগ (মসজিদের জামাতের সময় মেলাতে) */
    fun offset(prayer: String): Int = sp.getInt("off_$prayer", 0)
    fun setOffset(prayer: String, v: Int) = sp.edit().putInt("off_$prayer", v).apply()

    /** জুমার জামাতের সময় — মধ্যরাত থেকে মিনিট; ডিফল্ট দুপুর ১:১৫ */
    var jumuahMinutes: Int
        get() = sp.getInt("jumuah", 13 * 60 + 15)
        set(v) = sp.edit().putInt("jumuah", v).apply()

    /** হিজরি তারিখ সমন্বয় (দিন) — বাংলাদেশে চাঁদ দেখা অনুযায়ী −১/০/+১ */
    var hijriAdjust: Int
        get() = sp.getInt("hijriAdj", 0)
        set(v) = sp.edit().putInt("hijriAdj", v).apply()

    var quoteHiddenOn: String
        get() = sp.getString("quoteHidden", "") ?: ""
        set(v) = sp.edit().putString("quoteHidden", v).apply()

    fun guideSteps(id: String): Int = sp.getInt("guide_$id", 0)
    fun setGuideSteps(id: String, v: Int) = sp.edit().putInt("guide_$id", v).apply()

    fun getSet(key: String): Set<String> = sp.getStringSet(key, emptySet())?.toSet() ?: emptySet()
    fun putSet(key: String, v: Set<String>) = sp.edit().putStringSet(key, v).apply()

    /** আজকের দিনের জন্য সেট — তারিখ বদলালে খালি হয়ে যায়। */
    fun getToday(key: String): Set<String> =
        if (sp.getString("${key}_date", null) == LocalDate.now().toString()) getSet(key) else emptySet()

    fun putToday(key: String, v: Set<String>) {
        sp.edit().putString("${key}_date", LocalDate.now().toString()).putStringSet(key, v).apply()
    }
}

class AppState(ctx: Context) {
    val prefs = Prefs(ctx)

    var cityIndex by mutableStateOf(prefs.cityIndex)
        private set
    var hanafiAsr by mutableStateOf(prefs.hanafiAsr)
        private set
    private var remindersState by mutableStateOf(prefs.reminders)
    val reminders: Boolean get() = remindersState
    var themeMode by mutableStateOf(prefs.themeMode)
        private set
    private val guideSteps = mutableStateMapOf<String, Int>().apply {
        Content.guides.forEach { put(it.id, prefs.guideSteps(it.id)) }
    }

    var learnedDuas by mutableStateOf(prefs.getSet("learnedDuas"))
        private set
    /** "112:learned" / "113:learning" */
    private var surahMarks by mutableStateOf(prefs.getSet("surahMarks"))
    var duasToday by mutableStateOf(prefs.getToday("duasToday"))
        private set
    var prayersToday by mutableStateOf(prefs.getToday("prayersToday"))
        private set

    var jumuahMinutes by mutableStateOf(prefs.jumuahMinutes)
        private set
    var hijriAdjust by mutableStateOf(prefs.hijriAdjust)
        private set
    var quoteHiddenOn by mutableStateOf(prefs.quoteHiddenOn)
        private set
    /** সময়-সংক্রান্ত কিছু বদলালে বাড়ে — স্ক্রিনগুলো নতুন করে হিসাব করে */
    var timeVersion by mutableStateOf(0)
        private set
    private val offsets = mutableStateMapOf<String, Int>().apply {
        listOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA").forEach { put(it, prefs.offset(it)) }
    }
    var zikrDoneToday by mutableStateOf(prefs.getToday("zikrToday"))
        private set

    fun offsetOf(prayer: String): Int = offsets[prayer] ?: 0
    fun changeOffset(prayer: String, delta: Int) {
        val v = (offsetOf(prayer) + delta).coerceIn(-30, 90)
        offsets[prayer] = v; prefs.setOffset(prayer, v); timeVersion++
    }
    fun changeJumuah(minutes: Int) {
        jumuahMinutes = minutes.coerceIn(12 * 60, 15 * 60); prefs.jumuahMinutes = jumuahMinutes; timeVersion++
    }
    fun changeHijri(v: Int) { hijriAdjust = v.coerceIn(-2, 2); prefs.hijriAdjust = hijriAdjust }
    fun hideQuoteToday() { quoteHiddenOn = LocalDate.now().toString(); prefs.quoteHiddenOn = quoteHiddenOn }
    fun markZikrDone(id: String) {
        refreshDay()
        if (id in zikrDoneToday) return
        zikrDoneToday = zikrDoneToday + id
        prefs.putToday("zikrToday", zikrDoneToday)
    }

    fun refreshDay() {
        zikrDoneToday = prefs.getToday("zikrToday")
        duasToday = prefs.getToday("duasToday")
        prayersToday = prefs.getToday("prayersToday")
    }

    fun setCity(i: Int) { cityIndex = i; prefs.cityIndex = i; timeVersion++ }
    fun setHanafi(v: Boolean) { hanafiAsr = v; prefs.hanafiAsr = v; timeVersion++ }
    fun setReminders(v: Boolean) { remindersState = v; prefs.reminders = v }
    fun setTheme(v: Int) { themeMode = v; prefs.themeMode = v }
    fun guideDone(id: String): Int = guideSteps[id] ?: 0
    fun setGuideDone(id: String, v: Int) { guideSteps[id] = v; prefs.setGuideSteps(id, v) }

    fun toggleDuaLearned(id: String) {
        learnedDuas = if (id in learnedDuas) learnedDuas - id else learnedDuas + id
        prefs.putSet("learnedDuas", learnedDuas)
    }

    fun markDuaRead(id: String) {
        refreshDay()
        if (id in duasToday) return
        duasToday = duasToday + id
        prefs.putToday("duasToday", duasToday)
    }

    fun togglePrayer(key: String) {
        refreshDay()
        prayersToday = if (key in prayersToday) prayersToday - key else prayersToday + key
        prefs.putToday("prayersToday", prayersToday)
    }

    fun surahStatus(id: Int): String? =
        surahMarks.firstOrNull { it.substringBefore(':') == id.toString() }?.substringAfter(':')

    fun setSurahStatus(id: Int, status: String?) {
        val rest = surahMarks.filterNot { it.substringBefore(':') == id.toString() }.toSet()
        surahMarks = if (status == null) rest else rest + "$id:$status"
        prefs.putSet("surahMarks", surahMarks)
    }
}
