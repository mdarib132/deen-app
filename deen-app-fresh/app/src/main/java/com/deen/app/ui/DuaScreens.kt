package com.deen.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deen.app.data.AppState
import com.deen.app.data.Content
import com.deen.app.data.Dua
import com.deen.app.data.DuaLine

@Composable
fun DuasScreen(state: AppState, initialCat: String?, go: (String) -> Unit) {
    val c = LocalDeen.current
    var cat by rememberSaveable(initialCat) {
        mutableIntStateOf(Content.duaCategories.indexOfFirst { it.id == initialCat }.coerceAtLeast(0))
    }
    var query by rememberSaveable { mutableStateOf("") }
    val catId = Content.duaCategories[cat].id
    val q = query.trim()
    val list = Content.duas.filter { d ->
        (catId == "all" || d.category == catId) &&
            (q.isEmpty() || d.title.contains(q) || d.bn.contains(q) || d.tr.contains(q))
    }
    // "সব" হলে ক্যাটাগরি অনুযায়ী ভাগ করে দেখাই
    val sections: List<Pair<String?, List<Dua>>> =
        if (catId == "all" && q.isEmpty())
            Content.duaCategories.drop(1).map { cat -> cat.label to list.filter { it.category == cat.id } }.filter { it.second.isNotEmpty() }
        else listOf(null to list)

    Screen {
        item { Header("দোয়া", "${Bn.n(Content.duas.size)}টি দোয়া · ${Bn.n(Content.duaCategories.size - 1)}টি ভাগে") }
        item {
            Row(
                Modifier.fillMaxWidth().height(50.dp).clip(CircleShape).background(c.card).border(1.dp, c.border, CircleShape)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Ic.search, null, Modifier.size(18.dp), tint = c.muted)
                Spacer(Modifier.width(10.dp))
                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) Txt("দোয়া খুঁজুন — যেমন ‘কবর’, ‘ঘুম’", 15, color = c.muted)
                    BasicTextField(
                        query, { query = it }, singleLine = true,
                        textStyle = TextStyle(color = c.text, fontSize = 15.sp),
                        cursorBrush = SolidColor(c.accent),
                        modifier = Modifier.fillMaxWidth().semantics { contentDescription = "দোয়া খুঁজুন" }
                    )
                }
            }
        }
        item { ChipRow(Content.duaCategories.map { it.label }, cat) { cat = it } }
        Content.categoryNotes[catId]?.let { note ->
            item {
                DCard(color = c.sky, bordered = false) { Txt(note, 14, color = if (c.dark) c.body else c.skyInk) }
            }
        }
        Content.categoryLinks[catId]?.let { links ->
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    links.forEach { (label, route) ->
                        ListRow(label, null, lead = { IconTile(Ic.surah, c.lavender, 36.dp) }, onClick = { go(route) },
                            trailing = { Icon(Ic.arrow, null, Modifier.size(18.dp), tint = c.text) })
                    }
                }
            }
        }
        if (list.isEmpty()) item {
            DCard {
                Txt("এই নামে কোনো দোয়া পাওয়া যায়নি", 15, weight = FontWeight.Bold)
                Txt("অন্য শব্দে খুঁজুন, অথবা উপরের ‘সব’ চিপে ট্যাপ করুন।", 14, color = c.body)
            }
        }
        sections.forEach { (title, duas) ->
            if (title != null) item(key = "h-$title") {
                Txt(title, 18, Modifier.padding(top = 8.dp), weight = FontWeight.ExtraBold)
            }
            items(duas, key = { it.id }) { d ->
                val i = Content.duas.indexOf(d)
                ListRow(
                    d.title, d.src,
                    lead = {
                        Box(Modifier.size(44.dp).clip(CircleShape).background(c.tints[i % c.tints.size]), contentAlignment = Alignment.Center) {
                            Icon(Ic.dua, null, Modifier.size(20.dp), tint = c.text)
                        }
                    },
                    onClick = { go("dua/${d.id}") },
                    trailing = {
                        when {
                            d.id in state.learnedDuas -> StatusPill("মুখস্থ", PillKind.Learned)
                            d.id in state.duasToday -> StatusPill("আজ পড়েছি", PillKind.Learning)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DuaDetailScreen(state: AppState, id: String, back: () -> Unit, go: (String) -> Unit) {
    val c = LocalDeen.current
    val dua = remember(id) { Content.dua(id) }
    val sameCat = Content.duas.filter { it.category == dua.category }
    val next = sameCat.getOrNull(sameCat.indexOf(dua) + 1)
    LaunchedEffect(id) { state.markDuaRead(id) }
    val learned = id in state.learnedDuas

    Screen(bottomExtra = 32.dp) {
        item { TopBar(dua.title, back) }
        item {
            DCard(radius = 24.dp, padding = 20.dp, elevated = true) {
                ArabicText(dua.ar, if (dua.ar.length < 30) 40 else if (dua.ar.length < 90) 30 else 26, align = TextAlign.Center)
                Spacer(Modifier.size(4.dp))
                Label("উচ্চারণ")
                Txt(dua.tr, 18, weight = FontWeight.Bold)
                Spacer(Modifier.size(4.dp))
                Label("অর্থ")
                Txt(dua.bn, 16, color = c.body)
                Spacer(Modifier.size(4.dp))
                StatusPill("সূত্র: ${dua.src}", PillKind.Info)
            }
        }
        if (dua.note != null) item {
            DCard(color = c.greenSoft, bordered = false) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Ic.star, null, Modifier.size(16.dp), tint = c.greenInk)
                    Spacer(Modifier.width(6.dp))
                    Txt("ফজিলত ও আমল", 13, weight = FontWeight.Bold, color = c.greenInk)
                }
                Txt(dua.note, 14, color = if (c.dark) c.body else c.greenInk)
            }
        }
        items(dua.extras) { line -> ExtraLine(line) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (learned) {
                    Row(
                        Modifier.weight(1f).height(56.dp).clip(CircleShape).background(c.greenSoft).border(1.dp, c.greenLine, CircleShape)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Ic.check, null, Modifier.size(18.dp), tint = c.greenInk)
                        Spacer(Modifier.width(8.dp))
                        Txt("মুখস্থ হয়েছে", 16, weight = FontWeight.ExtraBold, color = c.greenInk)
                    }
                    CircleBtn(Ic.repeat, "আবার শিখছি হিসেবে রাখুন", { state.toggleDuaLearned(id) }, size = 56.dp)
                } else {
                    PrimaryButton("মুখস্থ করেছি", { state.toggleDuaLearned(id) }, Modifier.weight(1f), height = 56.dp)
                }
            }
        }
        if (next != null) item {
            SecondaryButton("পরের দোয়া: ${next.title}", { go("dua/${next.id}") }, Modifier.fillMaxWidth(), icon = Ic.arrow)
        }
        item {
            Txt(
                "অডিও শিগগিরই আসছে। আরবি উচ্চারণ নিশ্চিত হতে একজন আলেম বা কারীর কাছে একবার শুনে নিন।",
                12, color = c.muted, lineHeight = 1.5f
            )
        }
    }
}

@Composable
private fun ExtraLine(line: DuaLine) {
    val c = LocalDeen.current
    DCard(color = c.butter, bordered = false, padding = 18.dp) {
        Txt(line.label, 13, weight = FontWeight.Bold, color = c.butterInk)
        ArabicText(line.ar, 26)
        Txt(line.tr, 15, weight = FontWeight.Bold)
        Txt(line.bn, 14, color = c.body)
        Txt("সূত্র: ${line.src}", 12, weight = FontWeight.Bold, color = c.butterInk)
    }
}
