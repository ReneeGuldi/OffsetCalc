package com.ductcalc.offset

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.tan

/**
 * Side view of a 2-elbow offset, drawn to scale and laid out like the drawing in offset_calc.xlsx:
 * the duct comes in at the top left, drops by the offset (A) through two curved elbows, and leaves
 * at the bottom right. Every value has its own dimension line in the sheet's legend colors.
 *
 * Elbow 1 turns the duct by [angleDeg]; elbow 2 turns it back by [elbow2Deg]
 * (equal angles = the outgoing run is parallel to the incoming run).
 */
@Composable
fun DuctDiagram(
    offset: Double,
    angleDeg: Double,
    diameter: Double,
    r: OffsetMath.OffsetResult,
    modifier: Modifier = Modifier,
    elbow2Deg: Double = angleDeg,
) {
    val tm = rememberTextMeasurer()
    val cutOk = r.cutPiece > 0
    val mixed = abs(angleDeg - elbow2Deg) > 1e-9
    val bendRadius = r.elbowRise / tan(Math.toRadians(angleDeg / 2))   // = B × H
    val multiplier = if (diameter != 0.0) bendRadius / diameter else 0.0

    val th1 = Math.toRadians(angleDeg)
    val phi = Math.toRadians(angleDeg - elbow2Deg)          // heading of the outgoing run
    val ux = cos(th1).toFloat(); val uy = sin(th1).toFloat() // along the cut piece
    val vx = cos(phi).toFloat(); val vy = sin(phi).toFloat() // along the outgoing run
    val a = offset.toFloat()
    val h = diameter.toFloat()
    val d1 = r.elbowRise.toFloat()
    val d2 = r.elbowRise2.toFloat()
    val f = r.offsetLengthCL.toFloat()
    val rad = bendRadius.toFloat()
    val lead = h                                             // straight collar drawn at each end

    // ---- Geometry in inches. P1/P2 = where the run centerlines intersect. ----
    val wP1 = Offset(0f, 0f)
    val wP2 = Offset(f, a)
    val wT1a = Offset(-d1, 0f)                               // elbow 1 starts
    val wT1b = Offset(d1 * ux, d1 * uy)                      // elbow 1 ends → cut piece starts
    val wT2a = Offset(f - d2 * ux, a - d2 * uy)              // cut piece ends → elbow 2 starts
    val wT2b = Offset(f + d2 * vx, a + d2 * vy)              // elbow 2 ends
    val wStart = Offset(-d1 - lead, 0f)
    val wEnd = Offset(f + (d2 + lead) * vx, a + (d2 + lead) * vy)

    // Centerline: collar → arc (elbow 1) → cut piece → arc (elbow 2) → collar
    val n = 24
    val c1 = Offset(-d1, rad)
    val c2 = wT2a + Offset(uy, -ux) * rad
    val arc1 = { hd: Double -> c1 + Offset(sin(hd).toFloat(), -cos(hd).toFloat()) * rad }
    val arc2 = { hd: Double -> c2 + Offset(-sin(hd).toFloat(), cos(hd).toFloat()) * rad }
    val cl = ArrayList<Offset>()
    cl += wStart
    if (cutOk) {
        for (k in 0..n) cl += arc1(th1 * k / n)
        for (k in 0..n) cl += arc2(th1 + (phi - th1) * k / n)
    } else {
        cl += wP1; cl += wP2                                 // elbows overlap: corners only
    }
    cl += wEnd

    val minX = cl.minOf { it.x } - h / 2; val maxX = cl.maxOf { it.x } + h / 2
    val minY = cl.minOf { it.y } - h / 2; val maxY = cl.maxOf { it.y } + h / 2

    Card(
        modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DrawingColors.Panel, contentColor = DrawingColors.Text)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Side view (to scale)", style = MaterialTheme.typography.titleSmall)

            // Height follows the duct's shape so the drawing fills the width without big empty bands
            BoxWithConstraints(Modifier.fillMaxWidth()) {
            val drawW = maxWidth - PAD_L - PAD_R
            val shape = ((maxY - minY) / (maxX - minX)).takeIf { it.isFinite() && it > 0f } ?: 0.6f
            val canvasH = (drawW * shape + PAD_T + PAD_B).coerceIn(260.dp, 480.dp)
            Canvas(Modifier.fillMaxWidth().height(canvasH)) {
                // ---- Fit to screen. Screen y points down, so the offset drops like the sheet's drawing. ----
                val padL = PAD_L.toPx(); val padR = PAD_R.toPx()
                val padT = PAD_T.toPx(); val padB = PAD_B.toPx()
                val availW = size.width - padL - padR
                val availH = size.height - padT - padB
                val s = min(availW / (maxX - minX), availH / (maxY - minY))
                if (!s.isFinite() || s <= 0f) return@Canvas
                val x0 = padL + (availW - (maxX - minX) * s) / 2
                val yt = padT + (availH - (maxY - minY) * s) / 2
                val yBottom = yt + (maxY - minY) * s
                fun pt(wp: Offset) = Offset(x0 + (wp.x - minX) * s, yt + (wp.y - minY) * s)

                val w = h * s
                val sw = 1.3.dp.toPx()
                val start = pt(wStart); val end = pt(wEnd)
                val p1 = pt(wP1); val p2 = pt(wP2)
                val t1a = pt(wT1a); val t1b = pt(wT1b); val t2a = pt(wT2a); val t2b = pt(wT2b)
                val sIn = Offset(1f, 0f)
                val sU = Offset(ux, uy)
                val sV = Offset(vx, vy)
                val panel = DrawingColors.Panel
                val ink = DrawingColors.Outline

                // ---- Duct: white body with a dark outline ----
                val body = Path().apply {
                    val q0 = pt(cl[0]); moveTo(q0.x, q0.y)
                    for (i in 1 until cl.size) { val q = pt(cl[i]); lineTo(q.x, q.y) }
                }
                val join = if (cutOk) StrokeJoin.Round else StrokeJoin.Miter
                drawPath(body, ink, style = Stroke(width = w + 2 * sw, join = join, cap = StrokeCap.Butt))
                drawPath(body, DrawingColors.Duct, style = Stroke(width = w, join = join, cap = StrokeCap.Butt))
                if (cutOk) drawLine(DrawingColors.CutPiece, t1b, t2a, w, StrokeCap.Butt)

                fun across(at: Offset, dir: Offset, alpha: Float = 1f, ext: Float = 0f) {
                    val nn = perp(dir) * (w / 2 + ext)
                    drawLine(ink.copy(alpha = alpha), at - nn, at + nn, sw)
                }
                // Square ends, collar seams, and the joints between pieces
                across(start, sIn, ext = sw); across(end, sV, ext = sw)
                val collar = min(lead * s * 0.3f, 16.dp.toPx())
                across(start + sIn * collar, sIn); across(end - sV * collar, sV)
                across(t1a, sIn); across(t2b, sV)
                if (cutOk) {
                    across(t1b, sU); across(t2a, sU)
                    for (k in 1..2) {                                   // gore seams inside each elbow
                        val hA = th1 * k / 3
                        across(pt(arc1(hA)), Offset(cos(hA).toFloat(), sin(hA).toFloat()), 0.55f)
                        val hB = th1 + (phi - th1) * k / 3
                        across(pt(arc2(hB)), Offset(cos(hB).toFloat(), sin(hB).toFloat()), 0.55f)
                    }
                }
                // Dashed duct centerline
                drawPath(body, ink.copy(alpha = 0.45f), style = Stroke(1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f))))

                // Red construction lines: the run centerlines meeting at each elbow's intersection point
                if (cutOk) {
                    val tl = Path().apply {
                        moveTo(t1a.x, t1a.y); lineTo(p1.x, p1.y); lineTo(t1b.x, t1b.y)
                        moveTo(t2a.x, t2a.y); lineTo(p2.x, p2.y); lineTo(t2b.x, t2b.y)
                    }
                    drawPath(tl, DrawingColors.Red, style = Stroke(1.2.dp.toPx()))
                    drawCircle(DrawingColors.Red, 2.5.dp.toPx(), p1)
                    drawCircle(DrawingColors.Red, 2.5.dp.toPx(), p2)
                }

                // ---- Dimensions ----
                val near = w / 2 + 18.dp.toPx()

                if (cutOk) {
                    // (D) on the incoming run, above it
                    alignedDim(tm, t1a, p1, sideAway(sIn, p1, p2), near, "(D) ${frac(r.elbowRise)}", dec(r.elbowRise), DrawingColors.Red, panel)

                    // Along the slope, on the side away from the incoming run: D | C | D, then G further out
                    val slopeSide = sideAway(sU, p1, start)
                    alignedDim(tm, p1, t1b, slopeSide, near, "(D)", null, DrawingColors.Red, panel)
                    alignedDim(tm, t1b, t2a, slopeSide, near, "(C) ${frac(r.cutPiece)}", dec(r.cutPiece), DrawingColors.Green, panel)
                    alignedDim(tm, t2a, p2, slopeSide, near, "(D)", null, DrawingColors.Red, panel)
                    alignedDim(tm, p1, p2, slopeSide, near + 46.dp.toPx(), "(G) ${frac(r.centerToCenter)}", dec(r.centerToCenter), DrawingColors.Red, panel)

                    // (D) on the outgoing run, below it
                    alignedDim(tm, p2, t2b, sideAway(sV, p2, p1), near, "(D) ${frac(r.elbowRise2)}", dec(r.elbowRise2), DrawingColors.Red, panel)

                    // (I) angle at elbow 2: from the outgoing run to the slope's centerline carried on past P2
                    val ar = w / 2 + 44.dp.toPx()
                    drawLine(DrawingColors.Yellow, p2, p2 + sU * (ar + 12.dp.toPx()), 1.dp.toPx())
                    drawLine(DrawingColors.Yellow, p2, p2 + sV * (ar + 12.dp.toPx()), 1.dp.toPx())
                    drawArc(
                        DrawingColors.Yellow, startAngle = (angleDeg - elbow2Deg).toFloat(), sweepAngle = elbow2Deg.toFloat(),
                        useCenter = false, topLeft = p2 - Offset(ar, ar), size = Size(2 * ar, 2 * ar), style = Stroke(1.2.dp.toPx())
                    )
                    fun onArc(dg: Double) = p2 + Offset(cos(Math.toRadians(dg)).toFloat(), sin(Math.toRadians(dg)).toFloat()) * ar
                    fun tangentCw(dg: Double) = Offset(-sin(Math.toRadians(dg)).toFloat(), cos(Math.toRadians(dg)).toFloat())
                    arrowHead(onArc(angleDeg), tangentCw(angleDeg), DrawingColors.Yellow)
                    arrowHead(onArc(angleDeg - elbow2Deg), -tangentCw(angleDeg - elbow2Deg), DrawingColors.Yellow)
                    val iSize = labelSize(tm, "00°", "(elbow 2)")
                    val iAt = p2 + sU * (ar + 16.dp.toPx() + iSize.height / 2)
                    alignedLabel(tm, "${deg(elbow2Deg)}°", if (mixed) "(elbow 2)" else "(I)", iAt, 0f, DrawingColors.Yellow, panel)
                }

                // (A) offset: between the two run centerlines, at the far left
                val ax = x0 - 18.dp.toPx()
                val aEnd = Offset(start.x, p2.y)
                drawLine(DrawingColors.Yellow.copy(alpha = 0.7f), p2, aEnd, 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f)))
                dimension(tm, start, aEnd, Offset(ax, start.y), Offset(ax, p2.y),
                    "(A) ${frac(offset)}", dec(offset), DrawingColors.Yellow, panel, outward = Offset(-1f, 0f))

                // (H) diameter across the outgoing collar
                val nOut = perp(sV) * (w / 2)
                alignedDim(tm, end + nOut, end - nOut, sV, 20.dp.toPx(), "(H) ${frac(diameter)}", dec(diameter), DrawingColors.Yellow, panel)

                // (F) and (E) along the bottom
                val fy = yBottom + 24.dp.toPx()
                if (f > 0.01f) dimension(tm, p1, p2, Offset(p1.x, fy), Offset(p2.x, fy),
                    "(F) ${frac(r.offsetLengthCL)}", dec(r.offsetLengthCL), DrawingColors.Cyan, panel, outward = Offset(0f, 1f))
                if (!mixed && cutOk) {
                    val ey = yBottom + 62.dp.toPx()
                    dimension(tm, t1a, t2b, Offset(t1a.x, ey), Offset(t2b.x, ey),
                        "(E) ${frac(r.offsetLength)}", dec(r.offsetLength), DrawingColors.Green, panel, outward = Offset(0f, 1f))
                }

                // (B) bend radius: a radius line on the inside of elbow 1 (open space under the incoming run)
                if (cutOk) {
                    val midHead = th1 / 2
                    val onBend = pt(arc1(midHead))
                    val inward = norm(pt(c1) - onBend)
                    val tail = onBend + inward * min(rad * s, w / 2 + 30.dp.toPx())
                    drawLine(DrawingColors.Yellow, onBend, tail, 1.2.dp.toPx())
                    arrowHead(onBend, -inward, DrawingColors.Yellow)
                    val bMain = "(B) R${frac(bendRadius)}"
                    val bSub = "${deg(multiplier)} × ø"
                    val bSize = labelSize(tm, bMain, bSub)
                    val bAt = tail + Offset(-bSize.width / 2 + 8.dp.toPx(), bSize.height / 2 + 4.dp.toPx())
                    alignedLabel(tm, bMain, bSub, bAt, 0f, DrawingColors.Yellow, panel)
                }
            }
            }

            Legend(
                listOf(
                    Triple("A", "Offset", DrawingColors.Yellow),
                    Triple("B", "Multiplier (bend radius = B × H)", DrawingColors.Yellow),
                    Triple("C", "Cut piece", DrawingColors.Green),
                    Triple("D", "Centerline rise of elbow", DrawingColors.Red),
                    Triple("E", "Offset length makeups", DrawingColors.Green),
                    Triple("F", "Offset length ℄s", DrawingColors.Cyan),
                    Triple("G", "℄ to ℄ at angle", DrawingColors.Red),
                    Triple("H", "ø or cheek", DrawingColors.Yellow),
                    Triple("I", "Angle", DrawingColors.Yellow),
                )
            )
            if (!cutOk) Text(
                "Elbows overlap: no room for a cut piece at this offset/angle. Try a bigger angle or a smaller multiplier.",
                color = DrawingColors.Red, style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private val PAD_L = 60.dp
private val PAD_R = 64.dp
private val PAD_T = 60.dp
private val PAD_B = 92.dp

/** 45.0 -> "45", 22.5 -> "22.5", 1.5 -> "1.5" */
private fun deg(v: Double) = String.format(Locale.US, "%.2f", v).trimEnd('0').trimEnd('.')
