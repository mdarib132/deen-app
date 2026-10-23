package com.deen.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.deen.app.prayer.Prayer

/** পরের নামাজ অনুযায়ী আকাশের রং — ফ্ল্যাট, দুই রঙের মিনিমাল দৃশ্য। */
fun sceneColor(p: Prayer): Color = when (p) {
    Prayer.FAJR, Prayer.SUNRISE -> Color(0xFF1E2A78)
    Prayer.DHUHR -> Color(0xFF1F3BD1)
    Prayer.ASR -> Color(0xFF2350D8)
    Prayer.MAGHRIB -> Color(0xFF5B2A86)
    Prayer.ISHA -> Color(0xFF141C52)
}

private fun isNight(p: Prayer) = p == Prayer.FAJR || p == Prayer.MAGHRIB || p == Prayer.ISHA || p == Prayer.SUNRISE

/** সাদা সিলুয়েটে মসজিদ, দুই মিনার, চাঁদ বা সূর্য। */
@Composable
fun MosqueScene(prayer: Prayer, modifier: Modifier = Modifier) {
    val bg = sceneColor(prayer)
    val night = isNight(prayer)
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val white = Color.White
        val base = h * 0.93f

        // মাটি
        drawRect(white.copy(alpha = 0.16f), Offset(0f, base), Size(w, h - base))

        // চাঁদ / সূর্য
        val cx = w * 0.47f
        val cy = h * 0.26f
        val r = h * 0.075f
        if (night) {
            drawCircle(white, r, Offset(cx, cy))
            drawCircle(bg, r * 0.85f, Offset(cx + r * 0.45f, cy - r * 0.25f))
            listOf(0.38f to 0.14f, 0.60f to 0.11f, 0.68f to 0.30f, 0.86f to 0.09f, 0.78f to 0.18f).forEach { (x, y) ->
                drawCircle(white.copy(alpha = 0.8f), h * 0.009f, Offset(w * x, h * y))
            }
        } else {
            val sun = if (prayer == Prayer.ASR) Color(0xFFFFB547) else Color(0xFFFFD84D)
            drawCircle(sun, r, Offset(cx, cy))
        }

        // মিনার
        minaret(w * 0.56f, w, h, base, white)
        minaret(w * 0.93f, w, h, base, white)

        // মূল ঘর
        val x0 = w * 0.62f
        val x1 = w * 0.91f
        val top = h * 0.66f
        drawRect(white, Offset(x0, top), Size(x1 - x0, base - top))

        // গম্বুজ
        val dcx = (x0 + x1) / 2f
        val dr = (x1 - x0) * 0.36f
        drawArc(white, 180f, 180f, true, Offset(dcx - dr, top - dr), Size(dr * 2, dr * 2))
        drawRect(white, Offset(dcx - dr * 1.08f, top - h * 0.012f), Size(dr * 2.16f, h * 0.03f))
        drawRect(white, Offset(dcx - h * 0.006f, top - dr - h * 0.07f), Size(h * 0.012f, h * 0.07f))

        // দরজা ও জানালা — আকাশের রঙে কেটে নেওয়া খিলান
        val dw = (x1 - x0) * 0.16f
        arch(dcx, dw, base - h * 0.17f, base, bg)
        val ww = (x1 - x0) * 0.08f
        val wt = top + h * 0.07f
        arch(x0 + (x1 - x0) * 0.18f, ww, wt, wt + h * 0.09f, bg)
        arch(x1 - (x1 - x0) * 0.18f, ww, wt, wt + h * 0.09f, bg)
    }
}

private fun DrawScope.minaret(x: Float, w: Float, h: Float, base: Float, color: Color) {
    val mw = w * 0.035f
    drawRect(color, Offset(x, h * 0.36f), Size(mw, base - h * 0.36f))
    drawRect(color, Offset(x - mw * 0.35f, h * 0.46f), Size(mw * 1.7f, h * 0.03f))
    val cap = Path().apply {
        moveTo(x - mw * 0.15f, h * 0.36f)
        lineTo(x + mw / 2f, h * 0.22f)
        lineTo(x + mw * 1.15f, h * 0.36f)
        close()
    }
    drawPath(cap, color)
}

/** খিলান আকৃতি: নিচে সোজা, ওপরে অর্ধবৃত্ত। */
private fun DrawScope.arch(cx: Float, width: Float, archTop: Float, bottom: Float, color: Color) {
    val half = width / 2f
    val path = Path().apply {
        moveTo(cx - half, bottom)
        lineTo(cx - half, archTop)
        arcTo(Rect(cx - half, archTop - half, cx + half, archTop + half), 180f, 180f, false)
        lineTo(cx + half, bottom)
        close()
    }
    drawPath(path, color)
}
