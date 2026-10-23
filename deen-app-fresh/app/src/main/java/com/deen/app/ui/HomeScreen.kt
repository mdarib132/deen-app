package com.deen.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deen.app.data.AppState
import com.deen.app.data.QuranRepo
import com.deen.app.prayer.PrayerCalc
import com.deen.app.prayer.SALAH
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime

private data class Quick(val label: String, val duaId: String, val icon: ImageVector, val tint: (DeenColors) -> Color)

private val QUICK = listOf(
    Quick("খাওয়ার আগে", "eat_before", Ic.food) { it.peach },
    Quick("ঘর থেকে বের", "leave_home", Ic.door) { it.sky },
    Quick("ঘুমানোর আগে", "sleep", Ic.moon) { it.lavender },
    Quick("টয়লেটে ঢোকা", "toilet_enter", Ic.drop) { it.butter },
    Quick("যানবাহনে উঠে", "vehicle", Ic.car) { it.mint },
    Quick("পড়া শুরু", "study", Ic.surah) { it.soft },
)

@Composable
fun HomeScreen(state: AppState, go: (String) -> Unit) {
    val c = LocalDeen.current
    val ctx = LocalContext.current
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            state.refreshDay()
            delay(30_000)
        }
    }
    val today = now.toLocalDate()
    val times = remember(today, state.cityIndex, state.hanafiAsr) { PrayerCalc.times(state.prefs, today) }
    val (nextPrayer, nextAt) = remember(now, state.cityIndex, state.hanafiAsr) { PrayerCalc.next(state.prefs, now) }
    val left = Duration.between(now, nextAt).toMinutes().coerceAtLeast(0)
    val nextSurah = remember { QuranRepo.all(ctx).filter { it.group == "salah" } }
        .firstOrNull { state.surahStatus(it.id) != "learned" }

    Screen {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Txt("${Bn.n(today.dayOfMonth)} ${Bn.months[today.monthValue - 1]}", 13, weight = FontWeight.SemiBold, color = c.muted)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Txt(Bn.weekdays[today.dayOfWeek.value - 1], 42, weight = FontWeight.ExtraBold, lineHeight = 1.25f)
                        Spacer(Modifier.width(10.dp))
                        Box(Modifier.size(12.dp).clip(CircleShape).background(c.accent))
                    }
                }
                CircleBtn(Ic.settings, "সেটিংস", { go("settings") })
            }
        }
        item {
            val doneDuas = state.duasToday.size
            Text(
                buildAnnotatedString {
                    val strong = SpanStyle(color = c.text, fontWeight = FontWeight.Bold)
                    if (today.dayOfWeek.value == 5) append("জুমার দিন। ")
                    append("${nextPrayer.label(nextAt.toLocalDate())}-এর ")
                    withStyle(strong) { append(Bn.duration(left)) }
                    append(" বাকি")
                    if (doneDuas > 0) {
                        append(", আর আজ ")
                        withStyle(strong) { append("${Bn.n(doneDuas)}টা দোয়া") }
                        append(" পড়া হয়েছে।")
                    } else append("। আজ একটা ছোট দোয়া দিয়ে শুরু করবেন?")
                },
                color = c.muted, fontSize = 17.sp, lineHeight = 26.sp, fontWeight = FontWeight.Medium
            )
        }
        item {
            // Next prayer card
            DCard(color = c.ink, radius = 24.dp, padding = 20.dp, bordered = false, elevated = true, onClick = { go("salah") }) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Txt("পরের নামাজ", 13, weight = FontWeight.SemiBold, color = c.inkMuted)
                        Txt(nextPrayer.label(nextAt.toLocalDate()), 30, weight = FontWeight.ExtraBold, color = c.onInk, lineHeight = 1.25f)
                        Txt("${Bn.partOfDay(nextAt)} ${Bn.clock(nextAt)}", 14, weight = FontWeight.SemiBold, color = c.inkMuted)
                    }
                    Box(
                        Modifier.clip(CircleShape).background(c.accent.copy(alpha = 0.16f))
                            .border(1.dp, c.accent.copy(alpha = 0.5f), CircleShape).padding(horizontal = 14.dp, vertical = 8.dp)
                    ) { Txt("${Bn.duration(left)} বাকি", 13, weight = FontWeight.Bold, color = if (c.dark) Color(0xFFB34A1C) else Color(0xFFFFB08C), lineHeight = 1.3f) }
                }
                Spacer(Modifier.size(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SALAH.forEach { p ->
                        val key = p.name
                        val done = key in state.prayersToday
                        val isNext = p == nextPrayer && nextAt.toLocalDate() == today
                        val bg by animateColorAsState(if (done) c.green else if (c.dark) Color(0xFFE6E5EC) else c.inkSoft, tween(250, easing = Smooth), label = "p")
                        val label = p.label(today)
                        Column(
                            Modifier.clip(RoundedCornerShape(14.dp))
                                .clickable(role = Role.Checkbox) { state.togglePrayer(key) }
                                .semantics { contentDescription = "$label ${if (done) "পড়া হয়েছে" else "এখনো বাকি"}" }
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                Modifier.size(36.dp).clip(CircleShape).background(if (isNext && !done) Color.Transparent else bg)
                                    .then(if (isNext && !done) Modifier.border(2.dp, c.accent, CircleShape) else Modifier),
                                contentAlignment = Alignment.Center
                            ) {
                                if (done) Icon(Ic.check, null, Modifier.size(16.dp), tint = Color.White)
                                else if (isNext) Box(Modifier.size(8.dp).clip(CircleShape).background(c.accent))
                            }
                            Spacer(Modifier.size(6.dp))
                            Txt(label, 12, weight = if (isNext) FontWeight.Bold else FontWeight.SemiBold, color = if (isNext) c.onInk else c.inkMuted, lineHeight = 1.3f)
                            Txt(times[p]?.let { Bn.clock(it) } ?: "", 11, color = c.inkMuted, lineHeight = 1.3f)
                        }
                    }
                }
                Txt("নামাজ পড়া হলে বৃত্তে ট্যাপ করে চিহ্ন দিন", 12, color = c.inkMuted, lineHeight = 1.4f)
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Txt("এখন কী করছেন?", 18, Modifier.weight(1f), weight = FontWeight.ExtraBold)
                Txt("সব দোয়া", 14, Modifier.clip(CircleShape).clickable(role = Role.Button) { go("duas") }.padding(8.dp), weight = FontWeight.Bold)
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                QUICK.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { q ->
                            Column(
                                Modifier.weight(1f).heightIn(min = 100.dp).clip(RoundedCornerShape(20.dp)).background(q.tint(c))
                                    .clickable(role = Role.Button) { go("dua/${q.duaId}") }.padding(horizontal = 12.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconTile(q.icon, c.card, 36.dp)
                                Spacer(Modifier.size(10.dp))
                                Txt(q.label, 14, weight = FontWeight.Bold, lineHeight = 1.3f)
                            }
                        }
                    }
                }
            }
        }
        item { Txt("গুরুত্বপূর্ণ", 18, weight = FontWeight.ExtraBold) }
        item {
            val important = listOf(
                Triple("৪ কালেমা", "duas?cat=kalima", c.mint),
                Triple("তওবা", "salah?guide=tawbah", c.peach),
                Triple("জানাযার নামাজ", "salah?guide=janaza", c.lavender),
                Triple("ঈদের নামাজ", "salah?guide=eid", c.butter),
                Triple("কবরের সুরক্ষা", "duas?cat=qabr", c.sky),
                Triple("শেষ জামানার ফিতনা", "duas?cat=fitnah", c.soft),
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                important.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { (label, route, bg) ->
                            Row(
                                Modifier.weight(1f).heightIn(min = 56.dp).clip(RoundedCornerShape(18.dp)).background(bg)
                                    .clickable(role = Role.Button) { go(route) }.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Txt(label, 14, Modifier.weight(1f), weight = FontWeight.Bold, lineHeight = 1.3f)
                                Icon(Ic.arrow, null, Modifier.size(16.dp), tint = c.text)
                            }
                        }
                    }
                }
            }
        }
        item {
            ListRow(
                "নামাজের পরের তাসবিহ", "৩৩ + ৩৩ + ৩৩ + ১ · প্রায় ৩ মিনিট",
                lead = { IconTile(Ic.zikr, c.mint, 44.dp) }, onClick = { go("zikr") },
                trailing = { Icon(Ic.arrow, null, Modifier.size(20.dp), tint = c.text) }
            )
        }
        item {
            val evening = now.hour >= 12
            ListRow(
                "সাইয়েদুল ইস্তিগফার", if (evening) "সন্ধ্যায় একবার · ক্ষমা চাওয়ার শ্রেষ্ঠ দোয়া" else "সকালে একবার · ক্ষমা চাওয়ার শ্রেষ্ঠ দোয়া",
                lead = { IconTile(Ic.star, c.peach, 44.dp) }, onClick = { go("dua/sayyidul_istighfar") },
                trailing = { Icon(Ic.arrow, null, Modifier.size(20.dp), tint = c.text) }
            )
        }
        if (nextSurah != null) item {
            ListRow(
                "আজকের সূরা: ${nextSurah.name}", "${nextSurah.meaning} · ${Bn.n(nextSurah.ayahs.size)} আয়াত · মুখস্থ করা শুরু করুন",
                lead = { IconTile(Ic.surah, c.lavender, 44.dp) }, onClick = { go("surah/${nextSurah.id}") },
                trailing = { Icon(Ic.arrow, null, Modifier.size(20.dp), tint = c.text) }
            )
        }
        item {
            ListRow(
                "ফজিলত", "কোন আমলে কী পুরস্কার — সহিহ হাদিসে",
                lead = { IconTile(Ic.shield, c.sky, 44.dp) }, onClick = { go("fazilat") },
                trailing = { Icon(Ic.arrow, null, Modifier.size(20.dp), tint = c.text) }
            )
        }
    }
}
