package com.ductcalc.offset

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.min

/**
 * Compound offset drawn like the triangle in offset_calc.xlsx:
 * A (rise) is the upright side, B (run) the bottom, C (the true offset) the slope.
 * Each value sits on its own dimension line running parallel to the side it measures.
 * Side lengths are to scale.
 */
@Composable
fun CompoundDiagram(rise: Double, run: Double, modifier: Modifier = Modifier) {
    val tm = rememberTextMeasurer()
    val c = OffsetMath.compoundOffset(rise, run)
    // A very lopsided offset (e.g. 62" × 2.5") would draw as a sliver, so the short side is
    // stretched to at least 1/3 of the long side. The numbers on the dimensions stay exact.
    val longSide = maxOf(abs(rise), abs(run))
    val minSide = longSide / 3
    val drawRise = if (abs(rise) < minSide) minSide else abs(rise)
    val drawRun = if (abs(run) < minSide) minSide else abs(run)
    val toScale = drawRise == abs(rise) && drawRun == abs(run)

    Card(
        modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DrawingColors.Panel, contentColor = DrawingColors.Text)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (toScale) "Compound offset (to scale)" else "Compound offset (not to scale: short side stretched so it's readable)", style = MaterialTheme.typography.titleSmall)

            Canvas(Modifier.fillMaxWidth().height(300.dp)) {
                val a = drawRise.toFloat()
                val b = drawRun.toFloat()
                if (a == 0f && b == 0f) return@Canvas

                val padL = 64.dp.toPx(); val padR = 72.dp.toPx()
                val padT = 56.dp.toPx(); val padB = 60.dp.toPx()
                val availW = size.width - padL - padR
                val availH = size.height - padT - padB
                val sx = if (b > 0f) availW / b else Float.MAX_VALUE
                val sy = if (a > 0f) availH / a else Float.MAX_VALUE
                val s = min(sx, sy)

                // Right angle at the bottom-left, like the sheet.
                val corner = Offset(padL + (availW - b * s) / 2, padT + (availH + a * s) / 2)
                val top = corner - Offset(0f, a * s)          // end of the A side
                val right = corner + Offset(b * s, 0f)        // end of the B side
                val panel = DrawingColors.Panel

                // Sides of the triangle
                drawLine(DrawingColors.Text.copy(alpha = 0.8f), corner, top, 1.5.dp.toPx())
                drawLine(DrawingColors.Text.copy(alpha = 0.8f), corner, right, 1.5.dp.toPx())
                drawLine(DrawingColors.Green, top, right, 3.dp.toPx())
                if (a > 0f && b > 0f) {
                    val m = 10.dp.toPx()
                    drawPath(Path().apply {
                        moveTo(corner.x + m, corner.y); lineTo(corner.x + m, corner.y - m); lineTo(corner.x, corner.y - m)
                    }, DrawingColors.Text.copy(alpha = 0.8f), style = Stroke(1.dp.toPx()))
                }

                // Names inside the triangle, each turned to run along its side
                val aLen = a * s; val bLen = b * s
                if (aLen > 70.dp.toPx())
                    alignedLabel(tm, "A (rise)", null, Offset(corner.x + 14.dp.toPx(), (corner.y + top.y) / 2), -90f, DrawingColors.Text, null)
                if (bLen > 70.dp.toPx())
                    alignedLabel(tm, "B (run)", null, Offset((corner.x + right.x) / 2, corner.y - 13.dp.toPx()), 0f, DrawingColors.Text, null)
                val slope = Math.toDegrees(atan2((right.y - top.y).toDouble(), (right.x - top.x).toDouble())).toFloat()
                val inward = norm(corner - (top + right) / 2f)
                if (aLen > 120.dp.toPx() && bLen > 120.dp.toPx())
                    alignedLabel(tm, "C (true offset)", null, (top + right) / 2f + inward * 14.dp.toPx(), slope, DrawingColors.Text, null)

                // Dimension lines parallel to each side, with the sheet's slash ticks
                alignedDim(tm, corner, top, Offset(-1f, 0f), 30.dp.toPx(), "A ${inch(rise)}", null, DrawingColors.Yellow, panel, DimEnd.Slash)
                alignedDim(tm, corner, right, Offset(0f, 1f), 30.dp.toPx(), "B ${inch(run)}", null, DrawingColors.Yellow, panel, DimEnd.Slash)
                alignedDim(tm, top, right, -inward, 30.dp.toPx(), "C ${inch(c)}", frac(c), DrawingColors.Green, panel, DimEnd.Slash)
            }

            Legend(
                listOf(
                    Triple("A", "Rise (one direction)", DrawingColors.Yellow),
                    Triple("B", "Run (other direction)", DrawingColors.Yellow),
                    Triple("C", "True offset = √(A² + B²)", DrawingColors.Green),
                )
            )
            Text(
                "Use C as the (A) Offset on the Offset tab. Round up if you like, e.g. 20.95\" → 21\".",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/** e.g. 20.9538" */
private fun inch(v: Double) = String.format(Locale.US, "%.4f", v).trimEnd('0').trimEnd('.') + "\""
