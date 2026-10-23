package com.deen.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.deen.app.data.Content

@Composable
fun ZikrScreen(initialSet: String?, go: (String) -> Unit) {
    val c = LocalDeen.current
    val haptics = LocalHapticFeedback.current
    var setIdx by rememberSaveable { mutableIntStateOf(Content.zikrSets.indexOf(Content.zikrSet(initialSet)).coerceAtLeast(0)) }
    val set = Content.zikrSets[setIdx]
    var step by rememberSaveable(setIdx) { mutableIntStateOf(0) }
    var count by rememberSaveable(setIdx) { mutableIntStateOf(0) }
    var finished by rememberSaveable(setIdx) { mutableStateOf(false) }
    val cur = set.steps[step]
    val free = cur.target == 0
    val pct by animateFloatAsState(
        if (free) ((count % 33) / 33f) else (count / cur.target.toFloat()).coerceAtMost(1f),
        tween(250, easing = Smooth), label = "ring"
    )
    val press = remember { MutableInteractionSource() }
    val pressed by press.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, tween(150, easing = Smooth), label = "press")

    fun tap() {
        if (finished) return
        val n = count + 1
        if (!free && n >= cur.target) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            if (step < set.steps.lastIndex) { step += 1; count = 0 } else { count = cur.target; finished = true }
        } else {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            count = n
        }
    }

    Screen {
        item {
            Header("জিকির", set.sub) {
                CircleBtn(Ic.repeat, "আবার শুরু", { step = 0; count = 0; finished = false })
            }
        }
        item { ChipRow(Content.zikrSets.map { it.title }, setIdx) { setIdx = it } }
        if (set.steps.size > 1) item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                set.steps.forEachIndexed { i, s ->
                    val done = finished || i < step
                    val active = !finished && i == step
                    val bg = if (done) c.greenSoft else if (active) c.ink else c.card
                    val fg = if (done) c.greenInk else if (active) c.onInk else c.body
                    Column(
                        Modifier.weight(1f).heightIn(min = 56.dp).clip(RoundedCornerShape(18.dp)).background(bg)
                            .border(1.dp, if (done) c.greenLine else if (active) c.ink else c.border, RoundedCornerShape(18.dp))
                            .padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                    ) {
                        Txt(s.label, 12, weight = FontWeight.Bold, color = fg, align = TextAlign.Center, lineHeight = 1.3f)
                        Txt(
                            if (done) "হয়েছে" else if (active) "${Bn.n(count)}/${Bn.n(s.target)}" else "${Bn.n(s.target)} বার",
                            11, color = fg, lineHeight = 1.3f
                        )
                    }
                }
            }
        }
        item {
            DCard(radius = 24.dp, padding = 20.dp, elevated = true) {
                ArabicText(cur.ar, if (cur.ar.length > 60) 24 else 36, align = TextAlign.Center)
                Txt(cur.tr, 17, Modifier.fillMaxWidth(), weight = FontWeight.Bold, align = TextAlign.Center)
                Txt(cur.bn, 14, Modifier.fillMaxWidth(), color = c.body, align = TextAlign.Center)
                Txt("সূত্র: ${set.src}", 12, Modifier.fillMaxWidth(), weight = FontWeight.Bold, color = c.muted, align = TextAlign.Center)
            }
        }
        item {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(232.dp).scale(scale).clip(CircleShape)
                        .clickable(interactionSource = press, indication = null, role = Role.Button) { tap() }
                        .semantics { contentDescription = "গুনুন। এখন ${Bn.n(count)}" },
                    contentAlignment = Alignment.Center
                ) {
                    val track = c.track
                    val accent = c.accent
                    Canvas(Modifier.fillMaxSize()) {
                        val w = 16.dp.toPx()
                        val inset = w / 2
                        val arcSize = Size(size.width - w, size.height - w)
                        drawArc(track, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(w))
                        drawArc(accent, -90f, 360f * pct, false, Offset(inset, inset), arcSize, style = Stroke(w, cap = StrokeCap.Round))
                    }
                    Box(Modifier.size(192.dp).clip(CircleShape).background(c.ink), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Txt(Bn.n(count), 56, weight = FontWeight.ExtraBold, color = c.onInk, lineHeight = 1.15f)
                            Txt(if (free) "যতবার খুশি" else "${Bn.n(cur.target)} এর মধ্যে", 14, weight = FontWeight.SemiBold, color = c.inkMuted)
                        }
                    }
                }
                Spacer(Modifier.size(10.dp))
                Txt(
                    if (finished) "মাশাআল্লাহ, এই তাসবিহ শেষ হয়েছে" else "বৃত্তে ট্যাপ করুন — প্রতি ট্যাপে হালকা কম্পন",
                    13, weight = FontWeight.SemiBold, color = c.muted, align = TextAlign.Center
                )
            }
        }
        item {
            ListRow(
                "এই জিকিরের ফজিলত", "কতবার পড়লে কী পুরস্কার — সহিহ হাদিসে",
                lead = { IconTile(Ic.star, c.peach, 40.dp) }, onClick = { go("fazilat") },
                trailing = { androidx.compose.material3.Icon(Ic.arrow, null, Modifier.size(20.dp), tint = c.text) }
            )
        }
    }
}

@Composable
fun FazilatScreen(back: () -> Unit, go: (String) -> Unit) {
    val c = LocalDeen.current
    var cat by rememberSaveable { mutableIntStateOf(0) }
    var open by rememberSaveable { mutableStateOf<String?>(Content.fazilat.first().id) }
    val catId = Content.fazilatCategories[cat].id
    val list = Content.fazilat.filter { catId == "all" || it.cat == catId }
    val tint = mapOf("hundred" to (c.peach to c.peachInk), "anytime" to (c.sky to c.skyInk), "salah" to (c.lavender to c.lavenderInk), "morning" to (c.butter to c.butterInk), "protect" to (c.mint to c.greenInk))

    Screen(bottomExtra = 32.dp) {
        item { TopBar(null, back) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Txt("ফজিলত", 34, weight = FontWeight.ExtraBold, lineHeight = 1.25f)
                Txt("কোন জিকির বা দোয়া কতবার পড়লে কী পুরস্কার — শুধু সহিহ হাদিস থেকে।", 15, color = c.muted)
                Row(
                    Modifier.clip(CircleShape).background(c.greenSoft).border(1.dp, c.greenLine, CircleShape).padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Icon(Ic.shield, null, Modifier.size(14.dp), tint = c.greenInk)
                    Spacer(Modifier.size(6.dp))
                    Txt("প্রতিটির সাথে হাদিস নম্বর", 12, weight = FontWeight.Bold, color = c.greenInk, lineHeight = 1.3f)
                }
            }
        }
        item { ChipRow(Content.fazilatCategories.map { it.label }, cat) { cat = it } }
        items(list, key = { it.id }) { f ->
            val isOpen = open == f.id
            val (bg, ink) = tint[f.cat] ?: (c.soft to c.body)
            DCard(padding = 0.dp, elevated = isOpen) {
                Column(
                    Modifier.fillMaxWidth().clickable(role = Role.Button) { open = if (isOpen) null else f.id }.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(Modifier.clip(CircleShape).background(bg).padding(horizontal = 11.dp, vertical = 5.dp)) {
                        Txt(f.times, 12, weight = FontWeight.ExtraBold, color = ink, lineHeight = 1.3f)
                    }
                    Txt(f.title, 17, weight = FontWeight.ExtraBold, lineHeight = 1.35f)
                    Txt(f.reward, 14, color = c.body)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Txt(f.src, 12, Modifier.weight(1f, fill = false), weight = FontWeight.Bold, color = c.body)
                        Spacer(Modifier.size(8.dp))
                        StatusPill("সহিহ", PillKind.Learned)
                    }
                }
                AnimatedVisibility(isOpen, enter = expandVertically(tween(300, easing = Smooth)) + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                    Column(
                        Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp).clip(RoundedCornerShape(16.dp)).background(c.bg).padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ArabicText(f.ar, 24)
                        Txt(f.bn, 14, color = c.body)
                        when {
                            f.route != null -> PrimaryButton(if (f.route.startsWith("surah")) "সূরাটি অর্থসহ খুলুন" else "খুলুন", { go(f.route) }, Modifier.fillMaxWidth(), Ic.arrow, 44.dp)
                            f.zikrSet != null -> PrimaryButton("কাউন্টারে শুরু করুন", { go("zikr?set=${f.zikrSet}") }, Modifier.fillMaxWidth(), Ic.arrow, 44.dp)
                            f.duaId != null -> PrimaryButton("পুরো দোয়া দেখুন", { go("dua/${f.duaId}") }, Modifier.fillMaxWidth(), Ic.arrow, 44.dp)
                        }
                    }
                }
            }
        }
    }
}
