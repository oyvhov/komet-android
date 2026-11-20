package app.komet.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.komet.audio.Sfx
import app.komet.domain.PizzaOrder
import app.komet.domain.Txt
import app.komet.ui.KometViewModel
import app.komet.ui.components.BigButton
import app.komet.ui.components.GameText
import app.komet.ui.components.str
import app.komet.ui.theme.K
import kotlin.math.*

private fun slicePoint(center: Offset, degrees: Float, distance: Float): Offset {
    val radians = degrees * PI / 180
    return center + Offset(cos(radians).toFloat(), sin(radians).toFloat()) * distance
}

internal fun DrawScope.drawPizza(slices: Int, mask: Int) {
    val r = size.minDimension * .465f
    val steel = Color(0xFF6F8D96)
    drawCircle(Color(0x66000000), r * 1.065f, center + Offset(0f, r * .026f))
    drawCircle(Brush.linearGradient(listOf(Color(0xFFDAE9DE), steel, Color(0xFFCEDDD6))), r * 1.06f)
    drawCircle(Color(0xFF34535A), r)
    drawCircle(Color(0xFF28464E), r * .94f)
    val sweep = 360f / slices
    repeat(slices) { i ->
        val start = -90f + i * sweep
        val angle = start + sweep / 2
        val mid = slicePoint(center, angle, r * .016f)
        fun wedge(radius: Float, color: Color) {
            val v = Offset(radius, radius)
            drawArc(color, start + 1.2f, sweep - 2.4f, true, mid - v, Size(radius * 2, radius * 2))
        }
        if (mask and (1 shl i) == 0) {
            drawLine(steel, center, slicePoint(center, start, r), strokeWidth = r * .011f)
            drawCircle(steel.copy(alpha = .7f), r * .032f, slicePoint(center, angle, r * .61f), style = Stroke(r * .011f))
        } else {
            wedge(r * .966f, Color(0xFFAA542B))
            wedge(r * .944f, Color(0xFFF4B75F))
            wedge(r * .85f, Color(0xFFC65131))
            wedge(r * .817f, Color(0xFFFFD272))
            for ((fraction, reach) in listOf(.36f to .62f, .65f to .48f)) {
                val p = slicePoint(mid, start + fraction * sweep, r * reach)
                drawCircle(Color(0xFFAE442E), r * .098f, p)
                drawCircle(Color(0xFFE76A44), r * .076f, p)
                drawCircle(Color(0xFFFFAB69), r * .024f, p - Offset(r * .021f, r * .021f))
            }
            val leaf = slicePoint(mid, start + .61f * sweep, r * .72f)
            drawLine(Color(0xFF467845), leaf, slicePoint(mid, start + .67f * sweep, r * .65f), r * .043f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            for (n in 1..4) {
                val dot = slicePoint(mid, start + sweep * n / 5, r * .9f)
                drawCircle(Color(0xFFDA9150), r * .016f, dot)
            }
        }
    }
}

@Composable
internal fun PizzaStudio(vm: KometViewModel, order: PizzaOrder, nextLabel: Txt, onSolved: () -> Unit, next: () -> Unit) {
    var mask by rememberSaveable { mutableIntStateOf((1 shl order.slices) - 1) }
    var misses by rememberSaveable { mutableIntStateOf(0) }
    var checked by rememberSaveable { mutableStateOf(false) }
    var solved by rememberSaveable { mutableStateOf(false) }
    val pieces = mask.countOneBits()
    val prompt = Txt("Server ${order.portion.nn}.", "Server ${order.portion.nb}.")
    val total = order.slices
    val change: (Int) -> Unit = { i -> mask = mask xor (1 shl i); checked = false }
    val answer = Txt("${order.requestedSlices} av $total stykke er ${order.portion.nn}.", "${order.requestedSlices} av $total stykker er ${order.portion.nb}.")
    val more = pieces < order.requestedSlices
    val note = when {
        solved -> answer
        checked && misses >= 2 -> answer
        checked && more -> Txt("Litt for lite. Legg på meir.", "Litt for lite. Legg på mer.")
        checked -> Txt("Litt for mykje. Ta av eit stykke.", "Litt for mye. Ta av et stykke.")
        else -> Txt("Trykk på eit stykke for å ta det av. Trykk på plassen for å leggje det tilbake.",
            "Trykk på et stykke for å ta det av. Trykk på plassen for å legge det tilbake.")
    }
    StudioLayout(vm, prompt, board = {
        val description = Txt("Fat med $pieces av $total stykke", "Fat med $pieces av $total stykker").str()
        Canvas(
            Modifier.widthIn(max = 308.dp).fillMaxWidth(.86f).aspectRatio(1f)
                .semantics { contentDescription = description }
                .pointerInput(order, solved) {
                    if (!solved) detectTapGestures { p ->
                        val offset = p - Offset(size.width / 2f, size.height / 2f)
                        if (offset.getDistance() <= minOf(size.width, size.height) * .465f) {
                            val degrees = (atan2(offset.y.toDouble(), offset.x.toDouble()) * 180 / PI + 450) % 360
                            change((degrees / 360 * total).toInt())
                        }
                    }
                },
        ) { drawPizza(total, mask) }
    }) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            StudioKey("−", Txt("Ta av eit stykke", "Ta av et stykke"), !solved && pieces > 0, Modifier.width(66.dp)) {
                change((0 until total).last { mask and (1 shl it) != 0 })
            }
            GameText("$pieces / $total", style = MaterialTheme.typography.headlineMedium)
            StudioKey("+", Txt("Legg på eit stykke", "Legg på et stykke"), !solved && pieces < total, Modifier.width(66.dp)) {
                change((0 until total).first { mask and (1 shl it) == 0 })
            }
        }
        StudioNote(note)
        BigButton((if (solved) nextLabel else Txt("Server pizzaen")).str(), {
            if (solved) next() else {
                checked = true
                if (order.isFilled(pieces)) {
                    solved = true
                    onSolved()
                    vm.feedback.sfx(Sfx.CORRECT)
                    vm.say(answer)
                } else {
                    misses++
                    vm.say(if (misses >= 2) answer else if (more) Txt("Legg på meir.", "Legg på mer.") else Txt("Ta av litt."))
                }
            }
        }, Modifier.fillMaxWidth(), face = K.Math, edge = K.MathDeep)
    }
}
