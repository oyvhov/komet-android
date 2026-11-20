package app.komet.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.komet.audio.Sfx
import app.komet.domain.*
import app.komet.ui.KometViewModel
import app.komet.ui.components.*
import app.komet.ui.theme.K
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal fun DrawScope.drawPostEnvelope(at: Offset, width: Float) {
    val corner = at - Offset(width * .5f, width * .34f)
    drawRoundRect(K.PaperShade, corner, Size(width, width * .68f), CornerRadius(width * .055f))
    val seal = at + Offset(0f, width * .04f)
    for (x in listOf(.035f, .965f)) drawLine(K.InkMuted, corner + Offset(width * x, width * .04f), seal, width * .027f)
    drawCircle(Color(0xFFC45241), width * .095f, seal)
    drawCircle(Color(0xFFF8AD77), width * .039f, seal)
}

@Composable
private fun ParcelMarks(parcel: PostParcel) {
    Canvas(Modifier.size(62.dp, 24.dp)) {
        val r = minOf(size.width / 7f, size.height * .38f)
        repeat(parcel.count) { i ->
            val p = Offset(r + i * r * 2.5f, center.y)
            when (parcel.cargo) {
                PostCargo.STAR -> {
                    val star = Path().apply {
                        repeat(10) { j ->
                            val angle = PI * (j / 5.0 - .5)
                            val radius = if (j % 2 == 0) r else r * .45f
                            val x = p.x + radius * cos(angle).toFloat()
                            val y = p.y + radius * sin(angle).toFloat()
                            if (j == 0) moveTo(x, y) else lineTo(x, y)
                        }
                        close()
                    }
                    drawPath(star, Color(0xFF956326))
                }
                PostCargo.SEED -> {
                    drawOval(Color(0xFF487152), p - Offset(r * .7f, r), Size(r * 1.4f, r * 2))
                    drawLine(K.Paper, p + Offset(-r * .28f, r * .57f), p + Offset(r * .3f, -r * .54f), r * .14f)
                }
                PostCargo.CRYSTAL -> {
                    drawPath(Path().apply {
                        moveTo(p.x, p.y - r); lineTo(p.x + r * .74f, p.y - r * .15f)
                        lineTo(p.x, p.y + r); lineTo(p.x - r * .74f, p.y - r * .15f); close()
                    }, Color(0xFF306C88))
                    drawLine(K.Paper, p - Offset(0f, r * .72f), p + Offset(0f, r * .67f), r * .14f)
                }
            }
        }
    }
}

@Composable
internal fun PostStudio(vm: KometViewModel, order: PostOrder, nextLabel: Txt, onSolved: () -> Unit, next: () -> Unit) {
    val target = order.target
    val pack = order.job == PostJob.PACK
    var count by rememberSaveable { mutableStateOf(order.amounts.singleOrNull()) }
    var cargo by rememberSaveable { mutableStateOf(order.contents.singleOrNull()) }
    var misses by rememberSaveable { mutableIntStateOf(0) }
    var solved by rememberSaveable { mutableStateOf(false) }
    val code = readingString(Txt(target.code), GlyphKind.WORD)
    val prompt = if (pack) Txt("Kva bestiller Bolt med «$code»?", "Hva bestiller Bolt med «$code»?")
        else Txt("Korleis bestiller Bolt ${target.label.nn}?", "Hvordan bestiller Bolt ${target.label.nb}?")
    val answer = Txt("${postNumber(target.count)} tyder ${target.count}, og ${target.cargo.word} tyder ${target.cargo.singular.nn}.",
        "${postNumber(target.count)} betyr ${target.count}, og ${target.cargo.word} betyr ${target.cargo.singular.nb}.")
    val clue = if (count != target.count) Txt("Sjå på første ordet. Kor mange er det?", "Se på det første ordet. Hvor mange er det?")
        else Txt("Sjå på siste ordet. Kva er i pakken?", "Se på det siste ordet. Hva er i pakken?")
    StudioLayout(vm, prompt, board = {
        Column(Modifier.fillMaxWidth().background(K.Paper, RoundedCornerShape(16.dp)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Canvas(Modifier.size(46.dp)) { drawPostEnvelope(center, size.minDimension * .94f) }
                Text(Txt("Gamle pakkelappar", "Gamle pakkelapper").str(), color = K.Ink, style = MaterialTheme.typography.titleMedium)
            }
            order.examples.forEach { sample ->
                val speakLabel = if (vm.speechAvailable) Modifier.clickable { vm.say(Txt("${sample.code}. ") + sample.label) } else Modifier
                Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).background(K.PaperShade, RoundedCornerShape(8.dp)).then(speakLabel).padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(readingString(Txt(sample.code), GlyphKind.WORD), Modifier.weight(1f), color = K.Ink, style = MaterialTheme.typography.titleMedium)
                    Column(Modifier.weight(1f)) {
                        ParcelMarks(sample)
                        Text(sample.label.str(), color = K.Ink, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }) {
        val chosenCargo = cargo
        val chosenCount = count
        val parcel = if (chosenCount != null && chosenCargo != null) PostParcel(chosenCount, chosenCargo) else null
        if (solved) GameText(Txt("Sendt!").str(), style = MaterialTheme.typography.headlineMedium)
        else if (pack) StudioNote(parcel?.label ?: Txt("Vel innhaldet i pakken.", "Velg innholdet i pakken."))
        else GameText(readingString(Txt("${chosenCount?.let(::postNumber) ?: "…"}  ${chosenCargo?.word ?: "…"}"), GlyphKind.WORD))
        if (!solved) {
            if (order.amounts.size > 1) PostChoices(order.amounts, count, { if (pack) it.toString() else readingString(Txt(postNumber(it)), GlyphKind.WORD) }) { count = it }
            if (order.contents.size > 1) PostChoices(order.contents, cargo, { if (pack) it.singular.str() else readingString(Txt(it.word), GlyphKind.WORD) }) { cargo = it }
        }
        if (solved || misses >= 2) StudioNote(answer) else if (misses > 0) StudioNote(clue)
        BigButton((if (solved) nextLabel else if (pack) Txt("Send pakken") else Txt("Send meldinga", "Send meldingen")).str(), {
            if (solved) next() else if (order.matches(count, cargo)) {
                solved = true
                onSolved()
                vm.feedback.sfx(Sfx.CORRECT)
                vm.say(answer)
            } else {
                misses++
                vm.say(if (misses >= 2) answer else clue)
            }
        }, Modifier.fillMaxWidth(), face = K.Reading, edge = K.ReadingDeep, enabled = parcel != null)
    }
}

@Composable
private fun <T> PostChoices(items: List<T>, current: T?, label: @Composable (T) -> String, choose: (T) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { item ->
            val active = item == current
            PressSurface({ choose(item) }, Modifier.weight(1f).heightIn(min = 64.dp).semantics { selected = active },
                face = if (active) K.Reading else K.SurfaceHigh, edge = K.SurfaceLow, contentPadding = PaddingValues(4.dp)) {
                GameText(label(item), style = MaterialTheme.typography.titleMedium, maxLines = 1, autoFit = true)
            }
        }
    }
}
