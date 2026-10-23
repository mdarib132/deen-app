package com.deen.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.deen.app.data.AppState
import com.deen.app.data.Content

@Composable
fun SalahScreen(state: AppState, initialGuide: String?, go: (String) -> Unit) {
    val c = LocalDeen.current
    var gi by rememberSaveable(initialGuide) { mutableIntStateOf(Content.guides.indexOf(Content.guide(initialGuide)).coerceAtLeast(0)) }
    val guide = Content.guides[gi]
    val steps = guide.steps
    val done = state.guideDone(guide.id).coerceIn(0, steps.size)
    var open by rememberSaveable(gi) { mutableIntStateOf(done.coerceAtMost(steps.size - 1)) }
    val progress by animateFloatAsState(done / steps.size.toFloat(), tween(350, easing = Smooth), label = "prog")

    Screen {
        item {
            Header(guide.title, guide.sub) {
                StatusPill("${Bn.n(done)}/${Bn.n(steps.size)}", PillKind.Info)
            }
        }
        item { ChipRow(Content.guides.map { it.chip }, gi) { gi = it } }
        item {
            Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(c.track)) {
                Box(Modifier.fillMaxWidth(progress).fillMaxHeight().clip(CircleShape).background(c.accent))
            }
        }
        if (guide.notes.isNotEmpty()) item {
            DCard(color = c.sky, bordered = false) {
                guide.notes.forEach { Txt(it, 14, color = if (c.dark) c.body else c.skyInk) }
            }
        }
        itemsIndexed(steps, key = { i, _ -> "${guide.id}-$i" }) { i, s ->
            val isDone = i < done
            val isOpen = i == open
            val isCurrent = i == done
            val shape = RoundedCornerShape(20.dp)
            Column(
                Modifier.fillMaxWidth().clip(shape)
                    .background(if (isOpen) c.card else Color.Transparent)
                    .border(1.dp, if (isOpen) c.border else Color.Transparent, shape)
            ) {
                Row(
                    Modifier.fillMaxWidth().clickable(role = Role.Button) { open = if (isOpen) -1 else i }
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(36.dp).clip(CircleShape)
                            .background(if (isDone) c.green else if (isCurrent) c.card else c.soft)
                            .then(if (isCurrent) Modifier.border(2.dp, c.accent, CircleShape) else Modifier),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDone) Icon(Ic.check, "শেষ", Modifier.size(16.dp), tint = Color.White)
                        else Txt(Bn.n(i + 1), 14, weight = FontWeight.ExtraBold, color = c.body, lineHeight = 1.2f)
                    }
                    Spacer(Modifier.size(12.dp))
                    Txt(
                        s.title, 15, Modifier.weight(1f),
                        weight = if (isOpen) FontWeight.ExtraBold else FontWeight.Bold,
                        color = if (isDone && !isOpen) c.muted else c.text,
                        decoration = if (isDone && !isOpen) TextDecoration.LineThrough else null
                    )
                }
                AnimatedVisibility(isOpen, enter = expandVertically(tween(300, easing = Smooth)) + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                    Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (s.ar != null) ArabicText(s.ar, if (s.ar.length > 120) 22 else 28)
                        if (s.tr != null) Txt(s.tr, 15, weight = FontWeight.Bold)
                        Txt(s.bn, 14, color = c.body)
                        if (s.src != null) StatusPill("সূত্র: ${s.src}", PillKind.Info)
                        s.extras.forEach { line ->
                            Column(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.butter).padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Txt(line.label, 13, weight = FontWeight.Bold, color = c.butterInk)
                                ArabicText(line.ar, 22)
                                Txt(line.tr, 14, weight = FontWeight.Bold)
                                Txt(line.bn, 13, color = c.body)
                                Txt("সূত্র: ${line.src}", 12, weight = FontWeight.Bold, color = c.butterInk)
                            }
                        }
                        if (s.surahId != null) SecondaryButton("সূরাটি অর্থসহ খুলুন", { go("surah/${s.surahId}") }, Modifier.fillMaxWidth(), Ic.arrow, 44.dp)
                        if (s.surahList) SecondaryButton("২০টি ছোট সূরা দেখুন", { go("surahs") }, Modifier.fillMaxWidth(), Ic.arrow, 44.dp)
                        if (s.linkLabel != null && s.linkRoute != null) SecondaryButton(s.linkLabel, { go(s.linkRoute) }, Modifier.fillMaxWidth(), Ic.arrow, 44.dp)
                        PrimaryButton(
                            if (i == steps.lastIndex) "শেষ — আলহামদুলিল্লাহ" else "এই ধাপ বুঝেছি — পরেরটা",
                            {
                                val nd = maxOf(done, i + 1)
                                state.setGuideDone(guide.id, nd)
                                open = if (nd < steps.size) nd else -1
                            },
                            Modifier.fillMaxWidth(), height = 44.dp
                        )
                    }
                }
            }
        }
        if (done >= steps.size) item {
            SecondaryButton("আবার শুরু থেকে দেখুন", { state.setGuideDone(guide.id, 0); open = 0 }, Modifier.fillMaxWidth(), Ic.repeat)
        }
    }
}
