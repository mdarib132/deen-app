package com.deen.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deen.app.R

// ───────────────────────── Colors (Zimi) ─────────────────────────

data class DeenColors(
    val dark: Boolean,
    val bg: Color, val card: Color, val soft: Color, val track: Color,
    val text: Color, val muted: Color, val body: Color,
    val ink: Color, val onInk: Color, val inkMuted: Color, val inkSoft: Color,
    val border: Color, val navBg: Color,
    val accent: Color, val green: Color,
    val greenSoft: Color, val greenLine: Color, val greenInk: Color,
    val amberSoft: Color, val amberLine: Color, val amberInk: Color,
    val peach: Color, val sky: Color, val lavender: Color, val butter: Color, val mint: Color,
    val peachInk: Color, val skyInk: Color, val lavenderInk: Color, val butterInk: Color,
) {
    val tints get() = listOf(peach, sky, lavender, butter, mint)
}

val LightDeen = DeenColors(
    dark = false,
    bg = Color(0xFFF7F7FA), card = Color.White, soft = Color(0xFFF0EFF4), track = Color(0xFFE6E5EC),
    text = Color(0xFF121216), muted = Color(0xFF6E6D7A), body = Color(0xFF45444F),
    ink = Color(0xFF111114), onInk = Color.White, inkMuted = Color(0xFFB9B8C4), inkSoft = Color(0xFF242428),
    border = Color(0x14111114), navBg = Color(0xFF111114),
    accent = Color(0xFFFF6B2C), green = Color(0xFF16B773),
    greenSoft = Color(0xFFE3F7EE), greenLine = Color(0xFFA6E3C7), greenInk = Color(0xFF0B6B43),
    amberSoft = Color(0xFFFFF4DC), amberLine = Color(0xFFF3D27E), amberInk = Color(0xFF7A5300),
    peach = Color(0xFFFFE7DA), sky = Color(0xFFDCECFF), lavender = Color(0xFFE9E4FF), butter = Color(0xFFFFF3C9), mint = Color(0xFFD8F5E8),
    peachInk = Color(0xFF8A3510), skyInk = Color(0xFF1D4F8F), lavenderInk = Color(0xFF4A3AA8), butterInk = Color(0xFF6B4A00),
)

val DarkDeen = DeenColors(
    dark = true,
    bg = Color(0xFF111113), card = Color(0xFF1A1A1D), soft = Color(0xFF242428), track = Color(0xFF2C2C31),
    text = Color(0xFFF2F2F5), muted = Color(0xFFA5A4B0), body = Color(0xFFC9C8D2),
    ink = Color(0xFFF2F2F5), onInk = Color(0xFF111114), inkMuted = Color(0xFF55545E), inkSoft = Color(0xFFDDDCE3),
    border = Color(0x1AFFFFFF), navBg = Color(0xFF242428),
    accent = Color(0xFFFF6B2C), green = Color(0xFF16B773),
    greenSoft = Color(0xFF0F2E22), greenLine = Color(0xFF1F5A41), greenInk = Color(0xFF6EE0AE),
    amberSoft = Color(0xFF33290F), amberLine = Color(0xFF6B5216), amberInk = Color(0xFFF3D27E),
    peach = Color(0xFF3A2419), sky = Color(0xFF1B2A3D), lavender = Color(0xFF27233D), butter = Color(0xFF3A3318), mint = Color(0xFF16302A),
    peachInk = Color(0xFFFFB08C), skyInk = Color(0xFF9CC4FF), lavenderInk = Color(0xFFC3B8FF), butterInk = Color(0xFFF3D27E),
)

val LocalDeen = staticCompositionLocalOf { LightDeen }

val QuranFont = FontFamily(Font(R.font.amiri_quran))
val ArabicFont = FontFamily(Font(R.font.amiri_regular, FontWeight.Normal), Font(R.font.amiri_bold, FontWeight.Bold))

val Smooth = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)

@Composable
fun isDark(mode: Int): Boolean = when (mode) { 1 -> false; 2 -> true; else -> isSystemInDarkTheme() }

@Composable
fun DeenTheme(dark: Boolean, content: @Composable () -> Unit) {
    val c = if (dark) DarkDeen else LightDeen
    val scheme = if (dark) darkColorScheme(primary = c.accent, background = c.bg, surface = c.card, onSurface = c.text)
    else lightColorScheme(primary = c.accent, background = c.bg, surface = c.card, onSurface = c.text)
    CompositionLocalProvider(LocalDeen provides c) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}

// ───────────────────────── Icons (stroke, 24) ─────────────────────────

private fun icon(name: String, stroke: List<String>, fill: List<String> = emptyList(), width: Float = 1.8f): ImageVector =
    ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
        stroke.forEach {
            addPath(
                pathData = addPathNodes(it), fill = null, stroke = SolidColor(Color.Black),
                strokeLineWidth = width, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round
            )
        }
        fill.forEach { addPath(pathData = addPathNodes(it), fill = SolidColor(Color.Black)) }
    }.build()

object Ic {
    val home = icon("home", listOf("M4 10.5L12 4L20 10.5V19a1 1 0 0 1 -1 1h-4v-6H9v6H5a1 1 0 0 1 -1 -1z"))
    /** দুই হাত তুলে দোয়া */
    val dua = icon(
        "dua",
        listOf(
            "M3.5 20.5L3.5 9.5A1.25 1.25 0 0 1 6 9.5L6 7.5A1.25 1.25 0 0 1 8.5 7.5L8.5 13.2L9.4 12.3A1.05 1.05 0 0 1 10.9 13.8L9.2 16.8C8.6 17.9 8.5 19.3 8.5 20.5",
            "M6 9.5L6 13",
            "M20.5 20.5L20.5 9.5A1.25 1.25 0 0 0 18 9.5L18 7.5A1.25 1.25 0 0 0 15.5 7.5L15.5 13.2L14.6 12.3A1.05 1.05 0 0 0 13.1 13.8L14.8 16.8C15.4 17.9 15.5 19.3 15.5 20.5",
            "M18 9.5L18 13",
        )
    )
    /** মসজিদ */
    val salah = icon(
        "salah",
        listOf("M2.5 21h19", "M5 21V12", "M19 21V12", "M8 21v-6.5a4 4 0 0 1 8 0V21", "M12 10.5V8.5", "M10.5 21v-2.5a1.5 1.5 0 0 1 3 0V21"),
        listOf("M4 12L5 9.2L6 12Z", "M18 12L19 9.2L20 12Z")
    )
    val surah = icon("surah", listOf("M3 5h6a3 3 0 0 1 3 3v12a2.5 2.5 0 0 0 -2.5 -2.5H3z", "M21 5h-6a3 3 0 0 0 -3 3v12a2.5 2.5 0 0 1 2.5 -2.5H21z"))
    /** তাসবিহ */
    val zikr = icon(
        "zikr",
        listOf("M6.50 9.50a5.5 5.5 0 1 0 11.0 0a5.5 5.5 0 1 0 -11.0 0", "M12 16.6V20", "M10.8 21.3L12 20L13.2 21.3"),
        listOf("M10.40 15.00a1.6 1.6 0 1 0 3.2 0a1.6 1.6 0 1 0 -3.2 0", "M6.76 13.39a1.35 1.35 0 1 0 2.7 0a1.35 1.35 0 1 0 -2.7 0", "M5.15 9.50a1.35 1.35 0 1 0 2.7 0a1.35 1.35 0 1 0 -2.7 0", "M6.76 5.61a1.35 1.35 0 1 0 2.7 0a1.35 1.35 0 1 0 -2.7 0", "M10.65 4.00a1.35 1.35 0 1 0 2.7 0a1.35 1.35 0 1 0 -2.7 0", "M14.54 5.61a1.35 1.35 0 1 0 2.7 0a1.35 1.35 0 1 0 -2.7 0", "M16.15 9.50a1.35 1.35 0 1 0 2.7 0a1.35 1.35 0 1 0 -2.7 0", "M14.54 13.39a1.35 1.35 0 1 0 2.7 0a1.35 1.35 0 1 0 -2.7 0")
    )
    val shirt = icon("shirt", listOf("M9 4L4.5 6.5l1.8 3.5L8 9.2V20h8V9.2l1.7 0.8l1.8 -3.5L15 4a3 3 0 0 1 -6 0z"))
    val sun = icon("sun", listOf("M8.50 12.00a3.5 3.5 0 1 0 7.0 0a3.5 3.5 0 1 0 -7.0 0", "M12 3v2M12 19v2M3 12h2M19 12h2M5.6 5.6l1.4 1.4M17 17l1.4 1.4M5.6 18.4L7 17M17 7l1.4 -1.4"))
    val toilet = icon("toilet", listOf("M7 3.5h5v6H7z", "M5 9.5h12a6 6 0 0 1 -6 6H11a6 6 0 0 1 -6 -6z", "M9 15.5L8.5 20.5h6L14 15.5"))
    val close = icon("close", listOf("M6 6l12 12M18 6L6 18"), width = 2f)
    val plus = icon("plus", listOf("M12 7v10M7 12h10"), width = 2f)
    val minus = icon("minus", listOf("M7 12h10"), width = 2f)
    val food = icon("food", listOf("M7 3v8M5 3v5a2 2 0 0 0 4 0V3M7 11v10", "M17 3c-2 2 -2 6 0 8v10"))
    val door = icon("door", listOf("M5 21V4h10v17M3 21h18", "M17 9h4M19 7l2 2l-2 2"), listOf("M11.3 12.5a0.9 0.9 0 1 0 1.8 0a0.9 0.9 0 1 0 -1.8 0"))
    val moon = icon("moon", listOf("M20 14.5A8 8 0 1 1 9.5 4a6.5 6.5 0 0 0 10.5 10.5z"))
    val drop = icon("drop", listOf("M12 3s6 6.5 6 11a6 6 0 0 1 -12 0c0 -4.5 6 -11 6 -11z"))
    val car = icon("car", listOf("M4 17v-5l2.2 -5h11.6L20 12v5z", "M6.4 17a1.6 1.6 0 1 0 3.2 0a1.6 1.6 0 1 0 -3.2 0", "M14.4 17a1.6 1.6 0 1 0 3.2 0a1.6 1.6 0 1 0 -3.2 0"))
    val bell = icon("bell", listOf("M6 16v-5a6 6 0 0 1 12 0v5l1.5 2h-15z", "M10 21h4"))
    val back = icon("back", listOf("M19 12H5M11 6l-6 6l6 6"))
    val arrow = icon("arrow", listOf("M5 12h14M13 6l6 6l-6 6"))
    val check = icon("check", listOf("M5 12.5L10 17L19 7"), width = 2.4f)
    val play = icon("play", emptyList(), listOf("M8 5v14l11 -7z"))
    val pause = icon("pause", emptyList(), listOf("M6 5h4v14h-4z", "M14 5h4v14h-4z"))
    val repeat = icon("repeat", listOf("M4 12a8 8 0 1 0 2.4 -5.7L4 8.5", "M4 4v4.5h4.5"))
    val star = icon("star", listOf("M12 3l2.6 5.6l6 0.7l-4.5 4.1l1.2 6L12 16.4L6.7 19.4l1.2 -6L3.4 9.3l6 -0.7z"))
    val shield = icon("shield", listOf("M12 3L5 6v5c0 4.5 3 8 7 10c4 -2 7 -5.5 7 -10V6z", "M9 12l2 2l4 -4"), width = 2f)
    val search = icon("search", listOf("M4 11a7 7 0 1 0 14 0a7 7 0 1 0 -14 0", "M20 20l-4 -4"))
    val settings = icon("settings", listOf("M4 7h9M17 7h3M4 17h3M11 17h9", "M13 7a2 2 0 1 0 4 0a2 2 0 1 0 -4 0", "M7 17a2 2 0 1 0 4 0a2 2 0 1 0 -4 0"))
    val chevronDown = icon("chev", listOf("M6 9l6 6l6 -6"))
}

// ───────────────────────── Text ─────────────────────────

@Composable
fun Txt(
    text: String,
    size: Int,
    modifier: Modifier = Modifier,
    weight: FontWeight = FontWeight.Medium,
    color: Color = LocalDeen.current.text,
    lineHeight: Float = 1.5f,
    align: TextAlign? = null,
    decoration: TextDecoration? = null,
) {
    Text(
        text, modifier, color = color, fontSize = size.sp, fontWeight = weight,
        lineHeight = (size * lineHeight).sp, textAlign = align, textDecoration = decoration
    )
}

@Composable
fun ArabicText(
    text: String,
    size: Int,
    modifier: Modifier = Modifier,
    quran: Boolean = false,
    color: Color = LocalDeen.current.text,
    align: TextAlign = TextAlign.Start,
) {
    Text(
        text, modifier.fillMaxWidth(),
        style = TextStyle(
            fontFamily = if (quran) QuranFont else ArabicFont,
            fontWeight = if (quran) FontWeight.Normal else FontWeight.Bold,
            fontSize = size.sp, lineHeight = (size * 1.95f).sp, color = color,
            textDirection = TextDirection.Rtl, textAlign = align,
        )
    )
}

// ───────────────────────── Layout ─────────────────────────

/** Scrolling screen body with status-bar and bottom-nav padding. */
@Composable
fun Screen(
    state: LazyListState = rememberLazyListState(),
    bottomExtra: Dp = 120.dp,
    content: LazyListScope.() -> Unit,
) {
    val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    LazyColumn(
        state = state,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = top + 20.dp, bottom = bottom + bottomExtra),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}

@Composable
fun Header(title: String, sub: String? = null, trailing: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Column(Modifier.weight(1f)) {
            if (sub != null) Txt(sub, 13, weight = FontWeight.SemiBold, color = LocalDeen.current.muted)
            Txt(title, 36, Modifier.semantics { heading() }, weight = FontWeight.ExtraBold, lineHeight = 1.25f)
        }
        trailing?.invoke()
    }
}

@Composable
fun TopBar(title: String?, onBack: () -> Unit, trailing: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        CircleBtn(Ic.back, "ফিরে যান", onBack)
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            if (title != null) Txt(title, 15, weight = FontWeight.Bold)
        }
        if (trailing != null) trailing() else Spacer(Modifier.size(44.dp))
    }
}

@Composable
fun DCard(
    modifier: Modifier = Modifier,
    color: Color = LocalDeen.current.card,
    radius: Dp = 22.dp,
    padding: Dp = 16.dp,
    bordered: Boolean = true,
    elevated: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = LocalDeen.current
    val shape = RoundedCornerShape(radius)
    var m = modifier.fillMaxWidth()
    if (elevated && !c.dark) m = m.shadow(10.dp, shape, ambientColor = Color(0x22111114), spotColor = Color(0x22111114))
    m = m.clip(shape).background(color)
    if (bordered) m = m.border(1.dp, c.border, shape)
    if (onClick != null) m = m.clickable(role = Role.Button, onClick = onClick)
    Column(m.padding(padding), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
}

@Composable
fun IconTile(icon: ImageVector, bg: Color, size: Dp = 40.dp, tint: Color = LocalDeen.current.text) {
    Box(Modifier.size(size).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) {
        Icon(icon, null, Modifier.size(size * 0.48f), tint = tint)
    }
}

@Composable
fun CircleBtn(
    icon: ImageVector,
    desc: String,
    onClick: () -> Unit,
    size: Dp = 44.dp,
    bg: Color = LocalDeen.current.card,
    fg: Color = LocalDeen.current.text,
    bordered: Boolean = true,
) {
    val c = LocalDeen.current
    var m = Modifier.size(size).clip(CircleShape).background(bg)
    if (bordered) m = m.border(1.dp, c.border, CircleShape)
    Box(m.clickable(role = Role.Button, onClickLabel = desc, onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, desc, Modifier.size(size * 0.44f), tint = fg)
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, height: Dp = 52.dp) {
    val c = LocalDeen.current
    Row(
        modifier.height(height).clip(CircleShape).background(c.ink).clickable(role = Role.Button, onClick = onClick).padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
    ) {
        Txt(text, 15, weight = FontWeight.Bold, color = c.onInk)
        if (icon != null) { Spacer(Modifier.width(8.dp)); Icon(icon, null, Modifier.size(18.dp), tint = c.onInk) }
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, height: Dp = 52.dp) {
    val c = LocalDeen.current
    Row(
        modifier.height(height).clip(CircleShape).background(c.card).border(1.dp, c.border, CircleShape)
            .clickable(role = Role.Button, onClick = onClick).padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
    ) {
        Txt(text, 15, weight = FontWeight.Bold)
        if (icon != null) { Spacer(Modifier.width(8.dp)); Icon(icon, null, Modifier.size(18.dp), tint = c.text) }
    }
}

@Composable
fun ChipRow(labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val c = LocalDeen.current
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        labels.forEachIndexed { i, label ->
            val sel = i == selected
            val bg by animateColorAsState(if (sel) c.ink else c.card, tween(250, easing = Smooth), label = "chip")
            Box(
                Modifier.height(40.dp).clip(CircleShape).background(bg).border(1.dp, if (sel) c.ink else c.border, CircleShape)
                    .clickable(role = Role.Tab) { onSelect(i) }.padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) { Txt(label, 14, weight = FontWeight.Bold, color = if (sel) c.onInk else c.text) }
        }
    }
}

@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val c = LocalDeen.current
    Row(
        Modifier.fillMaxWidth().clip(CircleShape).background(c.soft).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { i, label ->
            val sel = i == selected
            val bg by animateColorAsState(if (sel) c.card else Color.Transparent, tween(250, easing = Smooth), label = "seg")
            Box(
                Modifier.weight(1f).height(44.dp).clip(CircleShape).background(bg).clickable(role = Role.Tab) { onSelect(i) },
                contentAlignment = Alignment.Center
            ) { Txt(label, 14, weight = FontWeight.Bold, color = if (sel) c.text else c.body) }
        }
    }
}

enum class PillKind { Learned, Learning, Info }

@Composable
fun StatusPill(text: String, kind: PillKind) {
    val c = LocalDeen.current
    val (bg, line, fg) = when (kind) {
        PillKind.Learned -> Triple(c.greenSoft, c.greenLine, c.greenInk)
        PillKind.Learning -> Triple(c.amberSoft, c.amberLine, c.amberInk)
        PillKind.Info -> Triple(c.soft, c.border, c.body)
    }
    Box(
        Modifier.clip(CircleShape).background(bg).border(1.dp, line, CircleShape).padding(horizontal = 10.dp, vertical = 4.dp)
    ) { Txt(text, 12, weight = FontWeight.Bold, color = fg, lineHeight = 1.3f) }
}

@Composable
fun Label(text: String) = Txt(text, 12, weight = FontWeight.Bold, color = LocalDeen.current.muted)

@Composable
fun ListRow(
    title: String,
    sub: String?,
    lead: @Composable () -> Unit,
    onClick: () -> Unit,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val c = LocalDeen.current
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(c.card).border(1.dp, c.border, RoundedCornerShape(20.dp))
            .clickable(role = Role.Button, onClick = onClick).padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        lead()
        Column(Modifier.weight(1f)) {
            Txt(title, 15, weight = FontWeight.Bold, lineHeight = 1.35f)
            if (sub != null) Txt(sub, 12, color = c.muted, lineHeight = 1.35f)
        }
        trailing?.invoke(this)
    }
}

// ───────────────────────── Bottom pill nav ─────────────────────────

data class NavItem(val route: String, val label: String, val icon: ImageVector)

val NAV_ITEMS = listOf(
    NavItem("home", "আজ", Ic.home),
    NavItem("duas", "দোয়া", Ic.dua),
    NavItem("salah", "নামাজ", Ic.salah),
    NavItem("surahs", "সূরা", Ic.surah),
    NavItem("zikr", "জিকির", Ic.zikr),
)

@Composable
fun PillNav(current: String?, onGo: (String) -> Unit, modifier: Modifier = Modifier) {
    val c = LocalDeen.current
    Row(
        modifier.fillMaxWidth().height(64.dp)
            .shadow(16.dp, CircleShape, ambientColor = Color(0x40111114), spotColor = Color(0x40111114))
            .clip(CircleShape).background(c.navBg).padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NAV_ITEMS.forEach { item ->
            val active = current == item.route
            val bg by animateColorAsState(if (active) Color.White else Color.Transparent, tween(300, easing = Smooth), label = "nav")
            Row(
                Modifier.height(48.dp).clip(CircleShape).background(bg)
                    .clickable(role = Role.Tab, onClickLabel = item.label) { onGo(item.route) }
                    .animateContentSize(tween(300, easing = Smooth))
                    .padding(horizontal = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(item.icon, item.label, Modifier.size(22.dp), tint = if (active) Color(0xFF111114) else Color.White)
                if (active) {
                    Spacer(Modifier.width(8.dp))
                    Txt(item.label, 15, weight = FontWeight.Bold, color = Color(0xFF111114), lineHeight = 1.2f)
                }
            }
        }
    }
}

// ───────────────────────── Bangla helpers ─────────────────────────

object Bn {
    private const val DIGITS = "০১২৩৪৫৬৭৮৯"
    fun n(x: Int): String = num(x.toString())
    fun num(s: String): String = buildString { s.forEach { ch -> append(if (ch in '0'..'9') DIGITS[ch - '0'] else ch) } }
    fun two(x: Int): String = num(x.toString().padStart(2, '0'))
    fun duration(totalMin: Long): String {
        val h = totalMin / 60
        val m = totalMin % 60
        return if (h > 0) "${num(h.toString())} ঘণ্টা ${num(m.toString())} মিনিট" else "${num(m.toString())} মিনিট"
    }
    val months = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
    /** java.time DayOfWeek.value: 1 = Monday … 7 = Sunday */
    val weekdays = listOf("সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার", "শনিবার", "রবিবার")
    fun clock(t: java.time.LocalDateTime): String {
        val h12 = if (t.hour % 12 == 0) 12 else t.hour % 12
        return "${n(h12)}:${two(t.minute)}"
    }
    fun partOfDay(t: java.time.LocalDateTime): String = when (t.hour) {
        in 4..5 -> "ভোর"; in 6..11 -> "সকাল"; in 12..14 -> "দুপুর"; in 15..17 -> "বিকেল"; in 18..19 -> "সন্ধ্যা"; else -> "রাত"
    }
}
