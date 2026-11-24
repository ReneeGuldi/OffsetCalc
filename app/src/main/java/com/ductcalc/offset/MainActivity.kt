package com.ductcalc.offset

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ductcalc.offset.ui.OffsetCalcTheme
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { OffsetCalcTheme { App() } }
    }
}

private val tabs = listOf("Offset", "Compound", "O.W.L.", "Resize")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text("Duct Offset Calculator") })
                TabRow(selectedTabIndex = tab) {
                    tabs.forEachIndexed { i, t ->
                        Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t) })
                    }
                }
            }
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (tab) {
                0 -> OffsetScreen()
                1 -> CompoundScreen()
                2 -> OwlScreen()
                else -> ResizeScreen()
            }
        }
    }
}

/* ---------- Screens ---------- */

@Composable
fun OffsetScreen() {
    var a by rememberSaveable { mutableStateOf("42") }
    var i by rememberSaveable { mutableStateOf("45") }
    var b by rememberSaveable { mutableStateOf("1.5") }
    var h by rememberSaveable { mutableStateOf("26") }
    var mixed by rememberSaveable { mutableStateOf(false) }
    var i2 by rememberSaveable { mutableStateOf("90") }

    SectionTitle("Calculate Offset Cut Piece")
    Text("Fill out the highlighted numbers to match your requirements.", style = MaterialTheme.typography.bodyMedium)

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NumberField("(A) Offset", a, { a = it }, Modifier.weight(1f))
        NumberField(if (mixed) "(I) Elbow 1 angle °" else "(I) Angle °", i, { i = it }, Modifier.weight(1f))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NumberField("(B) ℄ rise multiplier", b, { b = it }, Modifier.weight(1f))
        NumberField("(H) ø or cheek", h, { h = it }, Modifier.weight(1f))
    }

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("2nd elbow is a different angle", style = MaterialTheme.typography.bodyLarge)
            Text("e.g. a 60 to a 30, or a 45 to a 90", style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = mixed, onCheckedChange = { mixed = it })
    }
    if (mixed) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField("Elbow 2 angle °", i2, { i2 = it }, Modifier.weight(1f))
            Text(
                "Elbow 1 sets the travel angle of the cut piece.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f).align(Alignment.CenterVertically)
            )
        }
    }

    val av = a.num(); val iv = i.num(); val bv = b.num(); val hv = h.num()
    val i2v = if (mixed) i2.num() else iv
    val r = if (av != null && iv != null && bv != null && hv != null && i2v != null &&
        iv > 0 && iv < 180 && i2v > 0 && i2v < 180
    ) OffsetMath.offsetCutPiece(av, iv, bv, hv, i2v) else null

    ResultCard {
        ResultRow("(C) CUT PIECE", r?.cutPiece, highlight = true, tag = DrawingColors.Green)
        if (mixed) {
            ResultRow("(D) ℄ rise of elbow 1", r?.elbowRise, tag = DrawingColors.Red)
            ResultRow("(D) ℄ rise of elbow 2", r?.elbowRise2, tag = DrawingColors.Red)
        } else {
            ResultRow("(D) ℄ rise of elbow", r?.elbowRise, tag = DrawingColors.Red)
        }
        ResultRow("(E) Offset length (makeups)", r?.offsetLength, tag = DrawingColors.Green)
        ResultRow("(F) Length of offset ℄ to ℄", r?.offsetLengthCL, tag = DrawingColors.Cyan)
        ResultRow("(G) ℄ to ℄ at angle", r?.centerToCenter, tag = DrawingColors.Red)
    }

    if (r != null && av != null && iv != null && hv != null && i2v != null) {
        DuctDiagram(offset = av, angleDeg = iv, diameter = hv, r = r, elbow2Deg = i2v)
    }

    NotesCard(
        "Offset ÷ angle(sin) = ℄ to ℄",
        "Diameter × ℄ rise multiplier × ½ angle(tan) = centerline rise of 1 elbow",
        "℄ to ℄ − ℄ rise of elbow 1 − ℄ rise of elbow 2 = cut piece",
        "NOTE: 1 elbow may be a different degree, such as a 60 to a 30, or a 45 to a 90 depending on needs. " +
            "Turn on \"2nd elbow is a different angle\" above and the app swaps in that elbow's own ℄ rise.",
        "A 90° elbow's ℄ rise is 1.5 × diameter (with a 1.5 multiplier)."
    )
}

@Composable
fun CompoundScreen() {
    var a by rememberSaveable { mutableStateOf("62") }
    var b by rememberSaveable { mutableStateOf("2.5") }

    SectionTitle("Compound Offset  (A² + B² = C²)")
    Text(
        "A compound offset moves 2 directions, such as up 17\" and to the left 12.25\". " +
            "Use the result as your (A) Offset on the Offset tab.",
        style = MaterialTheme.typography.bodyMedium
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NumberField("A (rise)", a, { a = it }, Modifier.weight(1f))
        NumberField("B (run)", b, { b = it }, Modifier.weight(1f))
    }
    val av = a.num(); val bv = b.num()
    ResultCard {
        ResultRow("A²", av?.let { it * it }, fraction = false)
        ResultRow("B²", bv?.let { it * it }, fraction = false)
        ResultRow("C²", if (av != null && bv != null) av * av + bv * bv else null, fraction = false)
        ResultRow("Offset (C)", if (av != null && bv != null) OffsetMath.compoundOffset(av, bv) else null, highlight = true, tag = DrawingColors.Green)
    }
    if (av != null && bv != null) CompoundDiagram(rise = av, run = bv)
    NotesCard("Example: 17² + 12.25² = 289 + 150.0625 = 439.0625 → √ = 20.95. Round up to 21\".")
}

@Composable
fun OwlScreen() {
    var o by rememberSaveable { mutableStateOf("16") }
    var w by rememberSaveable { mutableStateOf("16") }
    var l by rememberSaveable { mutableStateOf("45") }

    SectionTitle("O.W.L. Method — Square Mitered Offsets")
    Text("Offset × Width ÷ Length = Miter", style = MaterialTheme.typography.bodyMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NumberField("Offset", o, { o = it }, Modifier.weight(1f))
        NumberField("Width", w, { w = it }, Modifier.weight(1f))
        NumberField("Length", l, { l = it }, Modifier.weight(1f))
    }
    val ov = o.num(); val wv = w.num(); val lv = l.num()
    ResultCard {
        ResultRow("Miter", if (ov != null && wv != null && lv != null && lv != 0.0) OffsetMath.owlMiter(ov, wv, lv) else null, highlight = true)
    }
}

@Composable
fun ResizeScreen() {
    var w1 by rememberSaveable { mutableStateOf("26") }
    var h1 by rememberSaveable { mutableStateOf("26") }
    var w2 by rememberSaveable { mutableStateOf("15") }

    SectionTitle("Change Size — Same Square Inches")
    Text("(W1 × H1) ÷ W2 = H2", style = MaterialTheme.typography.bodyMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NumberField("W1 width", w1, { w1 = it }, Modifier.weight(1f))
        NumberField("H1 height", h1, { h1 = it }, Modifier.weight(1f))
        NumberField("W2 width", w2, { w2 = it }, Modifier.weight(1f))
    }
    val a = w1.num(); val b = h1.num(); val c = w2.num()
    ResultCard {
        ResultRow("Area (sq in)", if (a != null && b != null) a * b else null, fraction = false)
        ResultRow("H2 height", if (a != null && b != null && c != null && c != 0.0) OffsetMath.sameAreaHeight(a, b, c) else null, highlight = true)
    }
    NotesCard("H2 is the new calculated size for either W or H.")
}

/* ---------- Building blocks ---------- */

private fun String.num(): Double? = trim().replace(',', '.').toDoubleOrNull()

@Composable
fun SectionTitle(text: String) =
    Text(text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

@Composable
fun NumberField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = { s -> if (s.all { it.isDigit() || it == '.' || it == ',' || it == '-' }) onChange(s) },
        label = { Text(label, maxLines = 1) },
        singleLine = true,
        isError = value.isNotBlank() && value.num() == null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
            focusedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
        modifier = modifier
    )
}

@Composable
fun ResultCard(content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
fun ResultRow(label: String, value: Double?, highlight: Boolean = false, fraction: Boolean = true, tag: Color? = null) {
    val ok = value != null && value.isFinite()
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (tag != null) {
            // Same color as this value's dimension line in the drawing
            Box(Modifier.size(12.dp).background(tag, RoundedCornerShape(3.dp)).border(1.dp, Color(0x55000000), RoundedCornerShape(3.dp)))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            label,
            Modifier.weight(1f),
            style = if (highlight) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal
        )
        Column(horizontalAlignment = Alignment.End) {
            Text(
                if (ok) String.format(Locale.US, "%.4f", value) else "—",
                style = if (highlight) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
                fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
                color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            if (ok && fraction) Text(toSixteenths(value!!), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun NotesCard(vararg lines: String) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Notes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            lines.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
        }
    }
}

/** Formats inches to the nearest 1/16", e.g. 27.0883 -> 27 1/16". */
fun toSixteenths(v: Double): String {
    val total = (abs(v) * 16).roundToLong()
    val whole = total / 16
    var num = total % 16
    var den = 16L
    while (num != 0L && num % 2 == 0L) { num /= 2; den /= 2 }
    val sign = if (v < 0 && total != 0L) "-" else ""
    return when {
        num == 0L -> "≈ $sign$whole\""
        whole == 0L -> "≈ $sign$num/$den\""
        else -> "≈ $sign$whole $num/$den\""
    }
}
