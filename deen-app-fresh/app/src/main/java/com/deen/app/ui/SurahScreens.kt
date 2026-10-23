package com.deen.app.ui

import android.media.AudioAttributes
import android.media.MediaPlayer
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.deen.app.data.AppState
import com.deen.app.data.QuranRepo

@Composable
fun SurahsScreen(state: AppState, go: (String) -> Unit) {
    val c = LocalDeen.current
    val ctx = LocalContext.current
    val everything = remember { QuranRepo.all(ctx) }
    val all = remember { everything.filter { it.group == "salah" } }
    val special = remember { everything.filter { it.group == "special" } }
    var filter by rememberSaveable { mutableIntStateOf(0) }
    val learnedCount = all.count { state.surahStatus(it.id) == "learned" }
    val nextGoal = all.firstOrNull { state.surahStatus(it.id) != "learned" }
    val list = all.filter {
        when (filter) {
            1 -> state.surahStatus(it.id) == "learning"
            2 -> state.surahStatus(it.id) == "learned"
            else -> true
        }
    }

    Screen {
        item { Header("সূরা", "নামাজে পড়ার ছোট সূরা") }
        item {
            DCard(color = c.ink, radius = 24.dp, padding = 20.dp, bordered = false, elevated = true) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                    Txt("মুখস্থ হয়েছে", 15, Modifier.weight(1f), weight = FontWeight.Bold, color = c.onInk)
                    Txt("${Bn.n(learnedCount)} / ${Bn.n(all.size)}", 26, weight = FontWeight.ExtraBold, color = c.onInk, lineHeight = 1.2f)
                }
                Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(if (c.dark) Color(0xFFDDDCE3) else c.inkSoft)) {
                    Box(Modifier.fillMaxWidth(learnedCount / all.size.toFloat()).fillMaxHeight().clip(CircleShape).background(c.accent))
                }
                if (nextGoal != null) Txt("পরের লক্ষ্য: ${nextGoal.name} · ${Bn.n(nextGoal.ayahs.size)} আয়াত", 13, color = c.inkMuted)
                else Txt("মাশাআল্লাহ, ২০টি সূরাই মুখস্থ!", 13, color = c.inkMuted)
            }
        }
        item { ChipRow(listOf("সব ${Bn.n(all.size)}টি", "শিখছি", "মুখস্থ"), filter) { filter = it } }
        if (list.isEmpty()) item {
            DCard {
                Txt(if (filter == 1) "এখনো কোনো সূরা ‘শিখছি’ তালিকায় নেই" else "এখনো কোনো সূরা মুখস্থ চিহ্ন দেওয়া হয়নি", 15, weight = FontWeight.Bold)
                Txt("সূরা খুলে নিচের বাটন থেকে চিহ্ন দিন।", 14, color = c.body)
            }
        }
        itemsIndexed(list, key = { _, s -> s.id }) { i, s ->
            ListRow(
                s.name, "${s.meaning} · ${Bn.n(s.ayahs.size)} আয়াত",
                lead = {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(c.tints[i % c.tints.size]), contentAlignment = Alignment.Center) {
                        Txt(Bn.n(s.id), 13, weight = FontWeight.ExtraBold, lineHeight = 1.2f)
                    }
                },
                onClick = { go("surah/${s.id}") },
                trailing = {
                    when (state.surahStatus(s.id)) {
                        "learned" -> StatusPill("মুখস্থ", PillKind.Learned)
                        "learning" -> StatusPill("শিখছি", PillKind.Learning)
                    }
                }
            )
        }
        if (filter == 0) {
            item(key = "special-h") {
                Column(Modifier.padding(top = 8.dp)) {
                    Txt("সুরক্ষার আয়াত ও সূরা", 18, weight = FontWeight.ExtraBold)
                    Txt("ঘুম, কবর ও দাজ্জালের ফিতনা থেকে সুরক্ষার জন্য", 13, color = c.muted)
                }
            }
            itemsIndexed(special, key = { _, s -> "sp-${s.id}" }) { i, s ->
                ListRow(
                    s.name, "${s.meaning} · ${Bn.n(s.ayahs.size)} আয়াত",
                    lead = { IconTile(Ic.shield, listOf(c.mint, c.lavender, c.sky)[i % 3], 40.dp) },
                    onClick = { go("surah/${s.id}") },
                    trailing = {
                        when (state.surahStatus(s.id)) {
                            "learned" -> StatusPill("মুখস্থ", PillKind.Learned)
                            "learning" -> StatusPill("শিখছি", PillKind.Learning)
                        }
                    }
                )
            }
        }
        item {
            Txt(
                "আরবি: The Noble Qur’an Encyclopedia (উসমানি লিপি) · বাংলা অর্থ: মুহিউদ্দীন খান (Tanzil) · তিলাওয়াত: মিশারী রাশিদ আল-আফাসি (everyayah.com)",
                12, color = c.muted
            )
        }
    }
}

private class AyahPlayer(private val onAyah: (Int?) -> Unit, private val onError: () -> Unit) {
    private var mp: MediaPlayer? = null
    private var queue: List<Int> = emptyList()
    private var surah = 0

    fun play(surahId: Int, ayahs: List<Int>) {
        stop()
        surah = surahId
        queue = ayahs
        next()
    }

    private fun next() {
        val a = queue.firstOrNull() ?: run { stop(); return }
        queue = queue.drop(1)
        onAyah(a)
        val url = "https://everyayah.com/data/Alafasy_128kbps/%03d%03d.mp3".format(surah, a)
        mp = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).setUsage(AudioAttributes.USAGE_MEDIA).build()
            )
            try {
                setDataSource(url)
            } catch (e: Exception) {
                fail(); return
            }
            setOnPreparedListener { it.start() }
            setOnCompletionListener { it.release(); mp = null; next() }
            setOnErrorListener { _, _, _ -> fail(); true }
            prepareAsync()
        }
    }

    private fun fail() {
        stop()
        onError()
    }

    fun stop() {
        queue = emptyList()
        mp?.let { runCatching { it.release() } }
        mp = null
        onAyah(null)
    }
}

@Composable
fun SurahDetailScreen(state: AppState, id: Int, back: () -> Unit) {
    val c = LocalDeen.current
    val ctx = LocalContext.current
    val surah = remember(id) { QuranRepo.get(ctx, id) }
    val bismillah = remember { QuranRepo.get(ctx, 1).ayahs.first().ar }
    var memo by rememberSaveable { mutableStateOf(false) }
    var revealed by remember(memo) { mutableStateOf(setOf<Int>()) }
    var playing by remember { mutableStateOf<Int?>(null) }
    var audioError by remember { mutableStateOf(false) }
    val player = remember { AyahPlayer(onAyah = { playing = it }, onError = { audioError = true }) }
    DisposableEffect(Unit) { onDispose { player.stop() } }
    val listState = rememberLazyListState()
    val headerItems = 5 // top bar, title card, note, mode, (error/space)
    LaunchedEffect(playing) {
        playing?.let { n ->
            val idx = surah.ayahs.indexOfFirst { it.n == n }
            if (idx >= 0) listState.animateScrollToItem(headerItems + idx)
        }
    }
    val status = state.surahStatus(id)

    Screen(state = listState, bottomExtra = 32.dp) {
        item { TopBar(null, back) { StatusPill("সূরা ${Bn.n(surah.id)} · ${Bn.n(surah.ayahs.size)} আয়াত", PillKind.Info) } }
        item {
            DCard(color = c.ink, radius = 24.dp, padding = 20.dp, bordered = false, elevated = true) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Txt(surah.name, 28, weight = FontWeight.ExtraBold, color = c.onInk, lineHeight = 1.3f)
                    Txt("${surah.meaning} · ${if (surah.meccan) "মক্কায় অবতীর্ণ" else "মদিনায় অবতীর্ণ"}", 14, color = c.inkMuted)
                    if (surah.id != 1 && surah.id != 9 && !surah.partial) {
                        Spacer(Modifier.size(8.dp))
                        ArabicText(bismillah, 26, quran = true, color = c.onInk, align = TextAlign.Center)
                    }
                }
            }
        }
        item {
            if (surah.note.isNotEmpty()) DCard(color = c.greenSoft, bordered = false) {
                Txt(surah.note, 14, color = if (c.dark) c.body else c.greenInk)
            } else Spacer(Modifier.size(0.dp))
        }
        item { Segmented(listOf("অর্থসহ পড়ি", "মুখস্থ মোড"), if (memo) 1 else 0) { memo = it == 1 } }
        item {
            if (audioError) DCard(color = c.amberSoft, bordered = false) {
                Txt("তিলাওয়াত চালানো যায়নি। ইন্টারনেট সংযোগ দেখে আবার চেষ্টা করুন।", 14, weight = FontWeight.Bold, color = c.amberInk)
            } else if (memo) Txt("প্রতিটি আয়াত মনে মনে বলুন, তারপর ট্যাপ করে মিলিয়ে দেখুন।", 13, color = c.muted)
            else Spacer(Modifier.size(0.dp))
        }
        itemsIndexed(surah.ayahs, key = { _, a -> a.n }) { _, a ->
            val isPlaying = playing == a.n
            val border by animateColorAsState(if (isPlaying) c.accent else Color.Transparent, tween(250, easing = Smooth), label = "ay")
            val show = !memo || a.n in revealed
            val shape = RoundedCornerShape(22.dp)
            Column(
                Modifier.fillMaxWidth().clip(shape).background(c.card).border(2.dp, border, shape).padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(30.dp).clip(CircleShape).background(c.soft), contentAlignment = Alignment.Center) {
                        Txt(Bn.n(a.n), 13, weight = FontWeight.ExtraBold, lineHeight = 1.2f)
                    }
                    Spacer(Modifier.weight(1f))
                    CircleBtn(
                        if (isPlaying) Ic.pause else Ic.play,
                        if (isPlaying) "থামান" else "আয়াত ${Bn.n(a.n)} শুনুন",
                        { audioError = false; if (isPlaying) player.stop() else player.play(surah.id, listOf(a.n)) },
                        bg = if (isPlaying) c.accent else c.ink, fg = if (isPlaying) Color(0xFF111114) else c.onInk, bordered = false
                    )
                }
                if (show) ArabicText(a.ar, if (a.ar.length > 90) 26 else 30, quran = true)
                else Box(
                    Modifier.fillMaxWidth().heightIn(min = 64.dp).clip(RoundedCornerShape(16.dp)).background(c.bg)
                        .border(1.dp, c.border, RoundedCornerShape(16.dp))
                        .clickable(role = Role.Button) { revealed = revealed + a.n }.padding(12.dp),
                    contentAlignment = Alignment.Center
                ) { Txt("মনে মনে বলুন, তারপর দেখতে ট্যাপ করুন", 14, weight = FontWeight.Bold, color = c.body, align = TextAlign.Center) }
                Txt(a.tr, 16, weight = FontWeight.Bold)
                if (!memo) Txt(a.bn, 14, color = c.body)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                val anyPlaying = playing != null
                Row(
                    Modifier.weight(1f).height(56.dp).clip(CircleShape).background(c.ink)
                        .clickable(role = Role.Button) {
                            audioError = false
                            if (anyPlaying) player.stop() else player.play(surah.id, surah.ayahs.map { it.n })
                        }.padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(32.dp).clip(CircleShape).background(c.accent), contentAlignment = Alignment.Center) {
                        Icon(if (anyPlaying) Ic.pause else Ic.play, null, Modifier.size(14.dp), tint = Color(0xFF111114))
                    }
                    Spacer(Modifier.width(10.dp))
                    Txt(if (anyPlaying) "থামান" else "পুরো সূরা শুনুন", 16, weight = FontWeight.ExtraBold, color = c.onInk)
                }
                CircleBtn(
                    Ic.repeat, "প্রতি আয়াত ৩ বার করে শুনুন",
                    { audioError = false; player.play(surah.id, surah.ayahs.flatMap { a -> List(3) { a.n } }) },
                    size = 56.dp
                )
            }
        }
        item {
            Segmented(
                listOf("শিখছি", "মুখস্থ হয়েছে"),
                when (status) { "learning" -> 0; "learned" -> 1; else -> -1 }
            ) { i ->
                val target = if (i == 0) "learning" else "learned"
                state.setSurahStatus(id, if (status == target) null else target)
            }
        }
        item {
            Txt("বাংলা উচ্চারণ শুধু সহায়তার জন্য — সঠিক তিলাওয়াত শিখতে অডিও শুনুন ও একজন কারীর কাছে মিলিয়ে নিন।", 12, color = c.muted)
        }
    }
}
