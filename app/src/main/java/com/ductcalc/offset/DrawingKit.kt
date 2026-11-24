package com.ductcalc.offset

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max

/**
 * Shared look for the drawings, modeled on the drawing in offset_calc.xlsx:
 * white duct on a dark panel, with the sheet's legend colors —
 * yellow = what you enter, green = what you cut / make up, red = elbow ℄ numbers, cyan = F.
 */
object DrawingColors {
    val Panel = Color(0xFF454B52)
    val Yellow = Color(0xFFFFE94D)   // A, B, H, I  (inputs)
    val Green = Color(0xFF86F55A)    // C, E        (cut piece / makeup length)
    val Red = Color(0xFFFF7A6B)      // D, G        (℄ rise, ℄ to ℄)
    val Cyan = Color(0xFF5CE1FF)     // F
    val Duct = Color.White
    val CutPiece = Color(0xFFDDF7D2)
    val Outline = Color(0xFF15181B)
    val Text = Color(0xFFF1F3F5)
}

enum class DimEnd { Arrow, Slash }

internal fun norm(o: Offset): Offset {
    val l = hypot(o.x, o.y)
    return if (l > 0f) o / l else Offset(1f, 0f)
}

internal fun perp(o: Offset) = Offset(-o.y, o.x)

internal fun dot(a: Offset, b: Offset) = a.x * b.x + a.y * b.y

/** Unit normal of a line with direction [dir] through [at], on the side away from [ref]. */
internal fun sideAway(dir: Offset, at: Offset, ref: Offset): Offset {
    val n = norm(perp(dir))
    return if (dot(ref - at, n) > 0f) -n else n
}

/** Inches to the nearest 1/16", e.g. 29.698 -> 29 11/16". */
internal fun frac(v: Double) = toSixteenths(v).removePrefix("≈ ")

/** Decimal like the sheet's bracketed numbers, e.g. [29.6985]. */
internal fun dec(v: Double) = "[" + String.format(Locale.US, "%.4f", v).trimEnd('0').trimEnd('.') + "]"

private fun mainStyle(c: Color) = TextStyle(color = c, fontSize = 13.sp, fontWeight = FontWeight.Bold)
private fun subStyle(c: Color) = TextStyle(color = c, fontSize = 10.sp)

internal fun DrawScope.labelSize(tm: TextMeasurer, main: String, sub: String?): Size {
    val l1 = tm.measure(main, mainStyle(Color.White))
    val l2 = sub?.let { tm.measure(it, subStyle(Color.White)) }
    return Size(
        max(l1.size.width, l2?.size?.width ?: 0).toFloat(),
        (l1.size.height + (l2?.size?.height ?: 0)).toFloat()
    )
}

/** Text turned to run along a line at [angleDeg] (never upside-down), optional smaller 2nd line. */
internal fun DrawScope.alignedLabel(
    tm: TextMeasurer, main: String, sub: String?, center: Offset, angleDeg: Float,
    color: Color, bg: Color?,
) {
    var a = angleDeg
    while (a >= 90f) a -= 180f
    while (a < -90f) a += 180f
    val l1 = tm.measure(main, mainStyle(color))
    val l2 = sub?.let { tm.measure(it, subStyle(color)) }
    val sz = labelSize(tm, main, sub)
    rotate(a, center) {
        val top = center.y - sz.height / 2
        if (bg != null) {
            val p = 3.dp.toPx()
            drawRoundRect(bg, Offset(center.x - sz.width / 2 - p, top - p / 2), Size(sz.width + 2 * p, sz.height + p), CornerRadius(p))
        }
        drawText(l1, topLeft = Offset(center.x - l1.size.width / 2f, top))
        if (l2 != null) drawText(l2, topLeft = Offset(center.x - l2.size.width / 2f, top + l1.size.height))
    }
}

internal fun DrawScope.arrowHead(tip: Offset, dir: Offset, color: Color) {
    val back = tip - dir * 8.dp.toPx()
    val n = perp(dir) * 3.2.dp.toPx()
    drawPath(Path().apply {
        moveTo(tip.x, tip.y); lineTo(back.x + n.x, back.y + n.y); lineTo(back.x - n.x, back.y - n.y); close()
    }, color)
}

/**
 * A drafting-style dimension: extension lines from the measured points [p], [q] out to the
 * dimension line [pp]–[qq], arrowheads or slash ticks at the ends, and the value written along
 * the line. If the value doesn't fit between the ends it is set just outside, on the [outward] side.
 */
internal fun DrawScope.dimension(
    tm: TextMeasurer, p: Offset, q: Offset, pp: Offset, qq: Offset,
    main: String, sub: String?, color: Color, bg: Color,
    ends: DimEnd = DimEnd.Arrow, outward: Offset? = null,
) {
    val sw = 1.25.dp.toPx()
    val gap = 3.dp.toPx()
    val over = 5.dp.toPx()
    fun ext(a: Offset, b: Offset) {
        val d = b - a
        val len = d.getDistance()
        if (len > gap + 1f) { val u = d / len; drawLine(color.copy(alpha = 0.85f), a + u * gap, b + u * over, sw * 0.8f) }
    }
    ext(p, pp); ext(q, qq)

    val dir = norm(qq - pp)
    val len = (qq - pp).getDistance()
    when (ends) {
        DimEnd.Arrow -> {
            drawLine(color, pp, qq, sw)
            arrowHead(qq, dir, color); arrowHead(pp, -dir, color)
        }
        DimEnd.Slash -> {
            val e = dir * 7.dp.toPx()
            drawLine(color, pp - e, qq + e, sw)
            val t = norm(dir + perp(dir)) * 7.dp.toPx()
            drawLine(color, pp - t, pp + t, sw * 1.8f); drawLine(color, qq - t, qq + t, sw * 1.8f)
        }
    }

    val ang = Math.toDegrees(atan2(dir.y, dir.x).toDouble()).toFloat()
    val sz = labelSize(tm, main, sub)
    val mid = (pp + qq) / 2f
    val fits = sz.width + 26.dp.toPx() < len
    val out = norm(outward ?: perp(dir))
    val center = if (fits) mid else mid + out * (sz.height / 2 + 5.dp.toPx())
    alignedLabel(tm, main, sub, center, ang, color, if (fits) bg else null)
}

/** Dimension whose line sits [off] px from p–q on the [side] direction. */
internal fun DrawScope.alignedDim(
    tm: TextMeasurer, p: Offset, q: Offset, side: Offset, off: Float,
    main: String, sub: String?, color: Color, bg: Color, ends: DimEnd = DimEnd.Arrow,
) = dimension(tm, p, q, p + side * off, q + side * off, main, sub, color, bg, ends, side)

/** One legend line like the sheet's: colored "(X) NAME" on the panel. */
@Composable
internal fun LegendLine(letter: String, name: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold)) { append("($letter) ") }
            withStyle(SpanStyle(color = DrawingColors.Text)) { append(name) }
        },
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier
    )
}

/** Two-column legend. */
@Composable
internal fun Legend(items: List<Triple<String, String, Color>>) {
    val half = (items.size + 1) / 2
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            items.take(half).forEach { (l, n, c) -> LegendLine(l, n, c) }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            items.drop(half).forEach { (l, n, c) -> LegendLine(l, n, c) }
        }
    }
}
