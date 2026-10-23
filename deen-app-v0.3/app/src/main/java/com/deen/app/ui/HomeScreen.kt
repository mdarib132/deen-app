package com.deen.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deen.app.data.AppState
import com.deen.app.data.Content
import com.deen.app.data.QuranRepo
import com.deen.app.prayer.PrayerCalc
import com.deen.app.prayer.SALAH
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.Locale

private data class Quick(val label: String, val duaId: String, val icon: ImageVector, val tint: (DeenColors) -> Color)

private val QUICK = listOf(
    Quick("খাওয়ার আগে", "eat_before", Ic.food) { it.peach },
    Quick("ঘর থেকে বের", "leave_home", Ic.door) { it.sky },
    Quick("ঘুমানোর আগে", "sleep", Ic.moon) { it.lavender },
    Quick("ঘুম থেকে উঠে", "wake", Ic.sun) { it.butter },
    Quick("টয়লেটে ঢোকা", "toilet_enter", Ic.toilet) { it.mint },
    Quick("পোশাক পরা", "dressing", Ic.shirt) { it.soft },
    Quick("যানবাহনে উঠে", "vehicle", Ic.car) { it.peach },
    Quick("পড়া শুরু", "study", Ic.surah) { it.sky },
    Quick("ওজুর পরে", "wudu_after", Ic.drop) { it.lavender },
)

private val HIJRI_MONTHS = listOf(
    "মুহাররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি", "জমাদিউল আউয়াল", "জমাদিউস সানি",
    "রজব", "শাবান", "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"
)

/** বাংলায় হিজরি তারিখ, যেমন “১ জমাদিউল আউয়াল ১৪৪৮ হি.” */
fun hijriBangla(date: LocalDate, adjust: Int): String = runCatching {
    val h = HijrahDate.from(date.plusDays(adjust.toLong()))
    val d = h.get(ChronoField.DAY_OF_MONTH)
    val m = h.get(ChronoField.MONTH_OF_YEAR)
    val y = h.get(ChronoField.YEAR)
    "${Bn.n(d)} ${HIJRI_MONTHS[m - 1]} ${Bn.n(y)} হি."
}.getOrDefault("")

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
    val times = remember(today, state.timeVersion) { PrayerCalc.times(state.prefs, today) }
    val (nextPrayer, nextAt) = remember(now, state.timeVersion) { PrayerCalc.next(state.prefs, now) }
    val left = Duration.between(now, nextAt).toMinutes().coerceAtLeast(0)
    val nextSurah = remember { QuranRepo.all(ctx).filter { it.group == "salah" } }
        .firstOrNull { state.surahStatus(it.id) != "learned" }
    val quote = Content.quotes[(today.dayOfYear - 1) % Content.quotes.size]
    val showQuote = state.quoteHiddenOn != today.toString()

    Screen {
        // ── তারিখ ──
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Txt(Bn.weekdays[today.dayOfWeek.value - 1], 40, weight = FontWeight.ExtraBold, lineHeight = 1.25f)
                        Spacer(Modifier.width(10.dp))
                        Box(Modifier.size(12.dp).clip(CircleShape).background(c.accent))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Txt(today.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)), 13, weight = FontWeight.SemiBold, color = c.muted)
                        Txt("  ·  ", 13, color = c.muted)
                        Txt(hijriBangla(today, state.hijriAdjust), 13, weight = FontWeight.SemiBold, color = c.text)
                    }
                }
                CircleBtn(Ic.settings, "সেটিংস", { go("settings") })
            }
        }

        // ── আজকের লাইন ──
        item {
            AnimatedVisibility(showQuote, exit = shrinkVertically(tween(300, easing = Smooth)) + fadeOut()) {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(c.soft)
                        .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "“${quote.text}”", color = c.text, fontSize = 15.sp, lineHeight = 23.sp,
                            fontStyle = FontStyle.Italic, fontWeight = FontWeight.Medium
                        )
                        Txt("— ${quote.src}", 12, weight = FontWeight.SemiBold, color = c.muted)
                    }
                    CircleBtn(Ic.close, "আজকের জন্য লুকান", { state.hideQuoteToday() }, size = 36.dp, bg = Color.Transparent, fg = c.muted, bordered = false)
                }
            }
        }

        // ── পরের নামাজ (ইলাস্ট্রেশন) ──
        item {
            val shape = RoundedCornerShape(24.dp)
            Box(
                Modifier.fillMaxWidth().height(196.dp).clip(shape).background(sceneColor(nextPrayer))
                    .clickable(role = Role.Button) { go("salah") }
            ) {
                MosqueScene(nextPrayer, Modifier.fillMaxSize())
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Txt("পরের নামাজ", 13, weight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.75f))
                    Txt(nextPrayer.label(nextAt.toLocalDate()), 30, weight = FontWeight.ExtraBold, color = Color.White, lineHeight = 1.25f)
                    Txt("${Bn.partOfDay(nextAt)} ${Bn.clock(nextAt)}", 14, weight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.85f))
                    Spacer(Modifier.size(6.dp))
                    Box(
                        Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.16f))
                            .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) { Txt("${Bn.duration(left)} বাকি", 13, weight = FontWeight.Bold, color = Color.White, lineHeight = 1.3f) }
                }
            }
        }

        // ── আজকের নামাজ: চিহ্ন দেওয়া ──
        item {
            val doneCount = SALAH.count { it.name in state.prayersToday }
            DCard {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Txt("আজকের নামাজ", 16, weight = FontWeight.ExtraBold)
                        Txt("পড়া হলে বৃত্তে ট্যাপ করুন — সবুজ ✓ হয়ে যাবে", 12, color = c.muted)
                    }
                    StatusPill("${Bn.n(doneCount)}/৫", if (doneCount == 5) PillKind.Learned else PillKind.Info)
                }
                Spacer(Modifier.size(4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SALAH.forEach { p ->
                        val key = p.name
                        val done = key in state.prayersToday
                        val isNext = p == nextPrayer && nextAt.toLocalDate() == today
                        val label = p.label(today)
                        val fill by animateColorAsState(if (done) c.green else Color.Transparent, tween(250, easing = Smooth), label = "p")
                        Column(
                            Modifier.clip(RoundedCornerShape(14.dp))
                                .clickable(role = Role.Checkbox) { state.togglePrayer(key) }
                                .semantics { contentDescription = "$label — ${if (done) "পড়া হয়েছে" else "এখনো বাকি, চিহ্ন দিতে ট্যাপ করুন"}" }
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                Modifier.size(44.dp).clip(CircleShape).background(fill)
                                    .border(
                                        if (done) 0.dp else 2.dp,
                                        if (isNext) c.accent else c.track,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (done) Icon(Ic.check, null, Modifier.size(18.dp), tint = Color.White)
                                else Icon(Ic.plus, null, Modifier.size(16.dp), tint = if (isNext) c.accent else c.muted)
                            }
                            Spacer(Modifier.size(6.dp))
                            Txt(label, 12, weight = FontWeight.Bold, color = if (isNext) c.text else c.body, lineHeight = 1.3f)
                            Txt(times[p]?.let { Bn.clock(it) } ?: "", 11, color = c.muted, lineHeight = 1.3f)
                        }
                    }
                }
            }
        }

        // ── আজকের তওবা ──
        item {
            val done = "istighfar100" in state.zikrDoneToday
            DCard(color = if (done) c.greenSoft else c.peach, bordered = false) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Txt("আজকের তওবা", 16, Modifier.weight(1f), weight = FontWeight.ExtraBold, color = if (done) c.greenInk else c.text)
                    if (done) StatusPill("আজ হয়েছে ✓", PillKind.Learned)
                    else PrimaryButton("শুরু করুন", { go("zikr?set=istighfar100") }, height = 40.dp)
                }
                ArabicText("أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ", 24)
                Txt("আসতাগফিরুল্লাহা ওয়া আতূবু ইলাইহ — আমি আল্লাহর কাছে ক্ষমা চাই ও তাঁর কাছে ফিরে আসি।", 14, color = c.body)
                Txt("নবীজি (সা.) দিনে ৭০ বারেরও বেশি তওবা করতেন — বুখারি ৬৩০৭", 12, weight = FontWeight.SemiBold, color = if (c.dark) c.muted else c.peachInk)
            }
        }

        // ── কাজ শুরুর দোয়া ──
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    Txt("শুরুর আগে পড়ুন", 18, weight = FontWeight.ExtraBold)
                    Txt("প্রতিদিনের কাজের ছোট দোয়া — এক ট্যাপে", 12, color = c.muted)
                }
                Txt("সব দোয়া", 14, Modifier.clip(CircleShape).clickable(role = Role.Button) { go("duas") }.padding(8.dp), weight = FontWeight.Bold)
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                QUICK.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { q ->
                            Column(
                                Modifier.weight(1f).heightIn(min = 96.dp).clip(RoundedCornerShape(20.dp)).background(q.tint(c))
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

        // ── গুরুত্বপূর্ণ ──
        item { Txt("গুরুত্বপূর্ণ", 18, weight = FontWeight.ExtraBold) }
        item {
            val important = listOf(
                Triple("৪ কালেমা", "duas?cat=kalima", c.mint),
                Triple("ওজুর নিয়ম", "salah?guide=wudu", c.sky),
                Triple("জানাযার নামাজ", "salah?guide=janaza", c.lavender),
                Triple("ঈদের নামাজ", "salah?guide=eid", c.butter),
                Triple("কবরের সুরক্ষা", "duas?cat=qabr", c.peach),
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
                "জিকির", "নামাজের পরের তাসবিহ থেকে শুরু — একটা শেষ হলে পরেরটা",
                lead = { IconTile(Ic.zikr, c.mint, 44.dp) }, onClick = { go("zikr") },
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
