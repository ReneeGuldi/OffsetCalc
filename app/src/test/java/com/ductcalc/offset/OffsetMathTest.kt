package com.ductcalc.offset

import org.junit.Assert.assertEquals
import org.junit.Test

/** Expected values are the cached results stored in offset_calc.xlsx. */
class OffsetMathTest {
    private val eps = 1e-9

    @Test fun offsetCutPiece_matchesSpreadsheet() {
        val r = OffsetMath.offsetCutPiece(offset = 42.0, angleDeg = 45.0, riseMult = 1.5, diameter = 26.0)
        assertEquals(59.396969619669996, r.centerToCenter, eps)
        assertEquals(16.154328932550705, r.elbowRise, eps)
        assertEquals(27.088311754568586, r.cutPiece, eps)
        assertEquals(42.00000000000001, r.offsetLengthCL, 1e-6)
        assertEquals(74.30865786510142, r.offsetLength, 1e-6)
    }

    @Test fun sheetExampleNote_14in_21in_45deg() {
        // Example in the sheet: 14"ø, 21" offset, 2×45° elbows, multiplier 1.5 → cut piece 12.30151519
        val r = OffsetMath.offsetCutPiece(21.0, 45.0, 1.5, 14.0)
        assertEquals(12.30151519, r.cutPiece, 1e-6)
    }

    @Test fun mixedElbows_60to30() {
        val r = OffsetMath.offsetCutPiece(21.0, 60.0, 1.5, 14.0, elbow2Deg = 30.0)
        assertEquals(5.626933041053577, r.elbowRise2, eps)
        assertEquals(6.497422611928568, r.cutPiece, eps)
    }

    @Test fun ninetyElbowRise_is1_5xDiameter() {
        // Sheet note: the ℄ rise of a 90 is 1.5 × diameter
        val r = OffsetMath.offsetCutPiece(42.0, 45.0, 1.5, 26.0, elbow2Deg = 90.0)
        assertEquals(39.0, r.elbowRise2, 1e-9)
        assertEquals(4.242640687119298, r.cutPiece, 1e-9)
    }

    @Test fun compound() = assertEquals(62.050382754661555, OffsetMath.compoundOffset(62.0, 2.5), eps)
    @Test fun owl() = assertEquals(5.688888888888889, OffsetMath.owlMiter(16.0, 16.0, 45.0), eps)
    @Test fun resize() = assertEquals(45.06666666666667, OffsetMath.sameAreaHeight(26.0, 26.0, 15.0), eps)
}
