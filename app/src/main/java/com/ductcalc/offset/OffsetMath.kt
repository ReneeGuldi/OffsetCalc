package com.ductcalc.offset

import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * All formulas are ported 1:1 from offset_calc.xlsx (Sheet1).
 * Angles are entered in degrees, just like the spreadsheet.
 */
object OffsetMath {

    private fun rad(deg: Double) = deg * Math.PI / 180.0

    /** Result of the "Calculate Offset Cut Piece" section. Letters match the sheet's legend. */
    data class OffsetResult(
        val centerToCenter: Double,   // (G) ℄ to ℄ at angle        = A ÷ sin(I)
        val elbowRise: Double,        // (D) ℄ rise of elbow 1      = H × B × tan(I/2)
        val elbowRise2: Double,       // (D) ℄ rise of elbow 2      = H × B × tan(I2/2)
        val cutPiece: Double,         // (C) cut piece              = G − D1 − D2
        val offsetLengthCL: Double,   // (F) length of offset ℄ to ℄ = √(G² − A²)
        val offsetLength: Double,     // (E) offset length makeups  = F + D1 + D2
    )

    /**
     * @param offset       (A) offset
     * @param angleDeg     (I) angle of elbow 1 in degrees — this sets the travel angle of the cut piece
     * @param riseMult     (B) ℄ rise multiplier
     * @param diameter     (H) ø or cheek
     * @param elbow2Deg    angle of elbow 2 (defaults to the same as elbow 1, like the spreadsheet).
     *                     Per the sheet's note, a different 2nd elbow (60 to a 30, 45 to a 90…) just
     *                     swaps in that elbow's own ℄ rise, e.g. a 90 at 1.5 multiplier = 1.5 × diameter.
     */
    fun offsetCutPiece(
        offset: Double,
        angleDeg: Double,
        riseMult: Double,
        diameter: Double,
        elbow2Deg: Double = angleDeg,
    ): OffsetResult {
        val g = offset / sin(rad(angleDeg))                     // D21 = D16/F18
        val d1 = diameter * riseMult * tan(rad(angleDeg / 2))   // I21 = PRODUCT(J16,H16,TAN(F19))
        val d2 = diameter * riseMult * tan(rad(elbow2Deg / 2))
        val c = g - d1 - d2                                     // F26 = D21-(I21*2)
        val f = sqrt(g * g - offset * offset)                   // B26 = SQRT(D20-D18)
        val e = f + d1 + d2                                     // J26 = B26+I21+I21
        return OffsetResult(g, d1, d2, c, f, e)
    }

    /** Compound offset: A² + B² = C² (sheet cells I4:K6). */
    fun compoundOffset(a: Double, b: Double): Double = sqrt(a * a + b * b)

    /** O.W.L. method for square mitered offsets: Offset × Width ÷ Length = Miter (C37:F37). */
    fun owlMiter(offset: Double, width: Double, length: Double): Double = offset * width / length

    /** Change size with same square inches: (W1 × H1) ÷ W2 = H2 (H37:K37). */
    fun sameAreaHeight(w1: Double, h1: Double, w2: Double): Double = w1 * h1 / w2
}
