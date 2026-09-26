package app.komet.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import app.komet.ui.components.KometIcons
import app.komet.domain.MontessoriMaterials
import app.komet.domain.Strokes
import app.komet.domain.Txt
import app.komet.audio.Sfx
import app.komet.ui.KometViewModel
import app.komet.ui.S
import app.komet.ui.components.BigButton
import app.komet.ui.components.GameText
import app.komet.ui.components.LocalFeedback
import app.komet.ui.components.PageColumn
import app.komet.ui.components.Panel
import app.komet.ui.components.PressSurface
import app.komet.ui.components.ScreenTopBar
import app.komet.ui.components.TracingPad
import app.komet.ui.components.fixedSp
import app.komet.ui.components.str
import app.komet.ui.theme.K
import app.komet.ui.theme.LocalMotion
import kotlin.math.ceil

private object W {
    val intro = Txt("Vel eit arbeid frå hylla. Du kan gjere det så mange gonger du vil.", "Velg et arbeid fra hyllen. Du kan gjøre det så mange ganger du vil.")
    val rods = Txt("Raude og blå stenger", "Røde og blå stenger")
    val rodsDetail = Txt("Bygg ei trapp frå kortast til lengst.", "Bygg en trapp fra kortest til lengst.")
    val spindles = Txt("Teljepinnar", "Tellepinner")
    val spindlesDetail = Txt("Legg rett tal pinnar i kvar boks. Null skal vere tom.", "Legg riktig antall pinner i hver boks. Null skal være tom.")
    val trace = Txt("Spor tala", "Spor tallene")
    val traceDetail = Txt("Følg sporet med fingeren, og sei talet høgt.", "Følg sporet med fingeren, og si tallet høyt.")
    val choose = Txt("Vel eit anna arbeid", "Velg et annet arbeid")
    val reset = Txt("Legg tilbake", "Legg tilbake")
    val remove = Txt("Ta bort siste", "Ta bort siste")
    val check = Txt("Sjå om det stemmer", "Se om det stemmer")
    val matches = Txt("Det stemmer. Vil du gjere det ein gong til?", "Det stemmer. Vil du gjøre det en gang til?")
    val adjust = Txt("Sjå nøye på lengdene. Du kan flytte dei igjen.", "Se nøye på lengdene. Du kan flytte dem igjen.")
    val adjustBoxes = Txt("Nokre boksar treng fleire eller færre pinnar. Du kan endre dei.", "Noen bokser trenger flere eller færre pinner. Du kan endre dem.")
    val left = Txt("Pinnar att", "Pinner igjen")
    val workAgain = Txt("Arbeid ein gong til", "Arbeid en gang til")
    val traced = Txt("Du følgde sporet! Prøv eit anna tal eller gjer det igjen.", "Du fulgte sporet! Prøv et annet tall eller gjør det igjen.")
    val chosen = Txt("På matta", "På matten")
    val shelf = Txt("På hylla", "På hyllen")
    val practice = Txt("Arbeidsro · utan klokke og poeng", "Arbeidsro · uten klokke og poeng")
}

/** A quiet, optional workshop using concrete representations and self-checks. */
@Composable
fun MontessoriScreen(vm: KometViewModel) {
    var work by rememberSaveable { mutableIntStateOf(0) }
    val feedback = LocalFeedback.current
    PageColumn(maxWidth = 1200.dp, contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)) {
        ScreenTopBar(S.montessoriShort.str(), onBack = { vm.back() })
        Text(W.practice.str(), color = K.GoldTop, style = MaterialTheme.typography.labelLarge)
        if (work == 0) {
            Text(W.intro.str(), color = K.Text, style = MaterialTheme.typography.bodyLarge)
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                if (maxWidth >= 900.dp) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        WorkCard("01", W.rods, W.rodsDetail, K.MathTop, Modifier.weight(1f), wide = true) { work = 1; feedback.sfx(Sfx.OPEN, 0.45f); vm.say(W.rodsDetail) }
                        WorkCard("02", W.spindles, W.spindlesDetail, K.EnglishTop, Modifier.weight(1f), wide = true) { work = 2; feedback.sfx(Sfx.OPEN, 0.45f); vm.say(W.spindlesDetail) }
                        WorkCard("03", W.trace, W.traceDetail, K.ReadingTop, Modifier.weight(1f), wide = true) { work = 3; feedback.sfx(Sfx.OPEN, 0.45f); vm.say(W.traceDetail) }
                    }
                } else {
                    val portraitTablet = maxWidth >= 700.dp
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        WorkCard("01", W.rods, W.rodsDetail, K.MathTop, portrait = portraitTablet) { work = 1; feedback.sfx(Sfx.OPEN, 0.45f); vm.say(W.rodsDetail) }
                        WorkCard("02", W.spindles, W.spindlesDetail, K.EnglishTop, portrait = portraitTablet) { work = 2; feedback.sfx(Sfx.OPEN, 0.45f); vm.say(W.spindlesDetail) }
                        WorkCard("03", W.trace, W.traceDetail, K.ReadingTop, portrait = portraitTablet) { work = 3; feedback.sfx(Sfx.OPEN, 0.45f); vm.say(W.traceDetail) }
                    }
                }
            }
        } else {
            val title = when (work) { 1 -> W.rods; 2 -> W.spindles; else -> W.trace }
            val detail = when (work) { 1 -> W.rodsDetail; 2 -> W.spindlesDetail; else -> W.traceDetail }
            val guide = WorkGuide(title, detail, { work = 0 }, if (vm.speechAvailable) ({ vm.say(detail) }) else null)
            when (work) {
                1 -> RodWork(guide)
                2 -> SpindleWork(guide)
                else -> TraceWork(guide)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

private class WorkGuide(val title: Txt, val detail: Txt, val choose: () -> Unit, val listen: (() -> Unit)?)

/** On tablets the material and guidance have separate places, as on a classroom work table. */
@Composable
private fun WorkLayout(guide: WorkGuide, material: @Composable ColumnScope.() -> Unit, controls: @Composable ColumnScope.() -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val heading: @Composable ColumnScope.() -> Unit = {
            GameText(guide.title.str(), style = MaterialTheme.typography.headlineMedium)
            Text(guide.detail.str(), color = K.Text, style = MaterialTheme.typography.bodyLarge)
            guide.listen?.let { listen ->
                BigButton(S.readAloud.str(), onClick = listen, icon = KometIcons.Speaker, face = K.Reading, edge = K.ReadingDeep, modifier = Modifier.fillMaxWidth())
            }
        }
        val choose: @Composable ColumnScope.() -> Unit = {
            BigButton(W.choose.str(), onClick = guide.choose, face = K.SurfaceHigh, edge = K.SurfaceLow, modifier = Modifier.fillMaxWidth())
        }
        if (maxWidth >= 700.dp) {
            val materialWeight = if (maxWidth >= 900.dp) 1.8f else 1.5f
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(28.dp), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(materialWeight), verticalArrangement = Arrangement.spacedBy(14.dp), content = material)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    heading()
                    controls()
                    Spacer(Modifier.height(8.dp))
                    choose()
                }
            }
        } else {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                heading()
                material()
                controls()
                choose()
            }
        }
    }
}

@Composable
private fun WorkCard(number: String, title: Txt, detail: Txt, accent: Color, modifier: Modifier = Modifier, wide: Boolean = false, portrait: Boolean = false, onClick: () -> Unit) {
    PressSurface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = if (wide) 300.dp else if (portrait) 240.dp else 108.dp),
        face = K.SurfaceHigh,
        edge = K.SurfaceLow,
        contentPadding = PaddingValues(18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MaterialPreview(number, Modifier.size(if (portrait) 200.dp else 72.dp), compact = !portrait, accent = accent)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    GameText(title.str(), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Start)
                    Text(detail.str(), color = K.Muted, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (wide) MaterialPreview(number)
        }
    }
}

@Composable
private fun MaterialPreview(number: String, modifier: Modifier = Modifier.fillMaxWidth().height(150.dp), compact: Boolean = false, accent: Color = Color(0xFFA76B3B)) {
    Box(
        modifier.background(Brush.verticalGradient(listOf(K.Paper, K.PaperShade)), RoundedCornerShape(16.dp))
            .border(2.dp, accent, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (number == "03") {
            Text("3", color = K.ReadingDeep, style = MaterialTheme.typography.displayLarge, fontSize = fixedSp(if (compact) 46.dp else 112.dp), fontWeight = FontWeight.Bold)
        } else {
            Canvas(Modifier.fillMaxWidth().height(if (compact) 64.dp else 130.dp).padding(if (compact) 7.dp else 16.dp)) {
                if (number == "01") {
                    val unit = size.width / 6.2f
                    repeat(5) { row ->
                        repeat(row + 1) { segment ->
                            drawRect(
                                if (segment % 2 == 0) Color(0xFFCC3B37) else Color(0xFF2D73BD),
                                Offset(unit * 0.25f + segment * unit, row * size.height / 5f),
                                Size(unit, size.height / 7f),
                            )
                        }
                    }
                } else {
                    val gap = size.width / 6f
                    repeat(6) { box ->
                        val x = box * gap + gap * 0.1f
                        drawRoundRect(Color(0xFFDBCEB7), Offset(x, size.height * 0.18f), Size(gap * 0.8f, size.height * 0.65f), CornerRadius((if (compact) 2.dp else 8.dp).toPx()))
                        repeat(box) { stick ->
                            val sx = x + gap * (stick + 1) / (box + 1).toFloat() * 0.7f
                            drawLine(Color(0xFF9C653B), Offset(sx, size.height * 0.36f), Offset(sx, size.height * 0.68f), (if (compact) 1.dp else 3.dp).toPx(), cap = StrokeCap.Round)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RodWork(guide: WorkGuide) {
    val shelf = remember { MontessoriMaterials.rods.shuffled() }
    val placed = remember { mutableStateListOf<Int>() }
    var checked by remember { mutableStateOf(false) }
    val correct = MontessoriMaterials.rodsInOrder(placed)
    val feedback = LocalFeedback.current
    WorkLayout(guide, material = {
        MaterialTray {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                if (maxWidth >= 580.dp) {
                    Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                        Column(Modifier.weight(1f)) { RodMat(placed, 48.dp) }
                        Column(Modifier.weight(1f)) { RodShelf(shelf, placed, 48.dp) { placed += it; checked = false; feedback.sfx(Sfx.PLACE, 0.7f) } }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        RodMat(placed, 34.dp)
                        RodShelf(shelf, placed, 34.dp) { placed += it; checked = false; feedback.sfx(Sfx.PLACE, 0.7f) }
                    }
                }
            }
        }
    }, controls = {
        if (checked) FeedbackLine(if (correct) W.matches else W.adjust, correct)
        BigButton(W.remove.str(), onClick = { if (placed.isNotEmpty()) placed.removeAt(placed.lastIndex); checked = false }, enabled = placed.isNotEmpty(), face = K.SurfaceHigh, edge = K.SurfaceLow, modifier = Modifier.fillMaxWidth())
        BigButton(W.check.str(), onClick = { checked = true; if (correct) feedback.sfx(Sfx.CORRECT, 0.45f) }, enabled = placed.size == 5, modifier = Modifier.fillMaxWidth())
        if (correct && checked) BigButton(W.workAgain.str(), onClick = { placed.clear(); checked = false }, modifier = Modifier.fillMaxWidth())
    })
}

@Composable
private fun RodMat(placed: List<Int>, unit: Dp) {
    val motion = LocalMotion.current
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(W.chosen.str(), color = K.Ink, fontWeight = FontWeight.Bold)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MontessoriMaterials.rods.forEachIndexed { index, _ ->
                val rod = placed.getOrNull(index)
                Box(Modifier.fillMaxWidth().height(if (unit > 34.dp) 58.dp else 38.dp), contentAlignment = Alignment.CenterStart) {
                    AnimatedContent(
                        targetState = rod,
                        transitionSpec = {
                            if (motion) (fadeIn(tween(180)) + scaleIn(tween(220), initialScale = 0.88f)) togetherWith fadeOut(tween(80))
                            else EnterTransition.None togetherWith ExitTransition.None
                        },
                        label = "rod-slot",
                    ) { current ->
                        if (current != null) Rod(current, unit)
                        else Box(Modifier.width(unit * (index + 1)).height(unit * 0.8f).border(1.dp, K.InkMuted.copy(alpha = 0.3f), RoundedCornerShape(6.dp)))
                    }
                }
            }
        }
    }
}

@Composable
private fun RodShelf(shelf: List<Int>, placed: List<Int>, unit: Dp, onPlace: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(W.shelf.str(), color = K.Ink, fontWeight = FontWeight.Bold)
        shelf.filterNot { it in placed }.forEach { value ->
            PressSurface(
                onClick = { onPlace(value) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp),
                face = K.SurfaceHigh, edge = K.SurfaceLow,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart,
                description = "${W.rods.str()} $value",
                tapSound = false,
            ) { Rod(value, unit) }
        }
    }
}

@Composable
private fun Rod(length: Int, unit: Dp = 34.dp) {
    Row(Modifier.height(unit * 0.8f).shadow(3.dp, RoundedCornerShape(2.dp)), horizontalArrangement = Arrangement.spacedBy(0.dp)) {
        repeat(length) { segment ->
            val paint = if (segment % 2 == 0) Color(0xFFCC3B37) else Color(0xFF2D73BD)
            Box(
                Modifier.width(unit).height(unit * 0.8f)
                    .background(Brush.verticalGradient(listOf(lerp(paint, Color.White, 0.22f), paint, lerp(paint, Color.Black, 0.18f))))
                    .border(1.dp, Color(0xFF593A33).copy(alpha = 0.55f)),
            )
        }
    }
}

@Composable
private fun SpindleWork(guide: WorkGuide) {
    val boxes = remember { mutableStateListOf(0, 0, 0, 0, 0, 0) }
    var checked by remember { mutableStateOf(false) }
    val correct = MontessoriMaterials.spindlesMatch(boxes)
    val feedback = LocalFeedback.current
    WorkLayout(guide, material = {
        MaterialTray {
            Text("${W.left.str()}: ${MontessoriMaterials.remainingSpindles(boxes)}", color = K.Ink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val columns = if (maxWidth >= 580.dp) 3 else 2
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(6 / columns) { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            repeat(columns) { column ->
                                val index = row * columns + column
                                SpindleBox(index, boxes[index], MontessoriMaterials.canAddSpindle(boxes, index), Modifier.weight(1f),
                                    onAdd = { boxes[index]++; checked = false; feedback.sfx(Sfx.PLACE, 0.65f) },
                                    onRemove = { boxes[index]--; checked = false; feedback.sfx(Sfx.PLACE, 0.4f) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }, controls = {
        if (checked) FeedbackLine(if (correct) W.matches else W.adjustBoxes, correct)
        BigButton(W.check.str(), onClick = { checked = true; if (correct) feedback.sfx(Sfx.CORRECT, 0.45f) }, enabled = MontessoriMaterials.remainingSpindles(boxes) == 0, modifier = Modifier.fillMaxWidth())
        BigButton(W.reset.str(), onClick = { boxes.indices.forEach { boxes[it] = 0 }; checked = false }, face = K.SurfaceHigh, edge = K.SurfaceLow, modifier = Modifier.fillMaxWidth())
    })
}

@Composable
private fun SpindleBox(index: Int, count: Int, canAdd: Boolean, modifier: Modifier, onAdd: () -> Unit, onRemove: () -> Unit) {
    val motion = LocalMotion.current
    val visibleCount by animateFloatAsState(count.toFloat(), tween(if (motion) 190 else 0), label = "spindles")
    Column(
        modifier.background(K.PaperShade, RoundedCornerShape(18.dp))
            .border(1.dp, K.InkMuted.copy(alpha = 0.25f), RoundedCornerShape(18.dp)).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(index.toString(), color = K.Ink, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Box(Modifier.fillMaxWidth().height(62.dp), contentAlignment = Alignment.CenterStart) {
            Canvas(Modifier.fillMaxWidth().height(50.dp)) {
                repeat(ceil(visibleCount.coerceAtLeast(0f)).toInt().coerceAtMost(9)) { stick ->
                    val x = 8.dp.toPx() + stick * (size.width - 16.dp.toPx()) / 9f
                    drawLine(Color(0xFF9C653B).copy(alpha = (visibleCount - stick).coerceIn(0f, 1f)), Offset(x, 5.dp.toPx()), Offset(x, size.height - 5.dp.toPx()), 6.dp.toPx(), cap = StrokeCap.Round)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SmallAction("+", enabled = canAdd, modifier = Modifier.weight(1f), onClick = onAdd)
            SmallAction("−", enabled = count > 0, modifier = Modifier.weight(1f), onClick = onRemove)
        }
    }
}

@Composable
private fun SmallAction(label: String, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    PressSurface(onClick = onClick, modifier = modifier.heightIn(min = 64.dp), enabled = enabled, face = K.SurfaceHigh, edge = K.SurfaceLow, tapSound = false, contentPadding = PaddingValues(0.dp)) {
        GameText(label, style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun TraceWork(guide: WorkGuide) {
    var digit by remember { mutableIntStateOf(1) }
    var generation by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }
    val feedback = LocalFeedback.current
    val glyph = remember(digit) { Strokes.glyph(digit.toString()) }
    WorkLayout(guide, material = {
        if (glyph != null) {
            MaterialTray {
                BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TracingPad(
                        glyph = glyph,
                        resetKey = generation,
                        accent = K.Reading,
                        finished = finished,
                        description = "${W.trace.str()} $digit",
                        onStroke = {},
                        onDone = { finished = true; feedback.sfx(Sfx.SPARKLE, 0.45f) },
                        modifier = Modifier.size(minOf(maxWidth, 420.dp)),
                    )
                }
            }
        }
    }, controls = {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        (1..5).forEach { value ->
            PressSurface(
                onClick = { digit = value; generation++; finished = false },
                modifier = Modifier.weight(1f).heightIn(min = 64.dp),
                face = if (digit == value) K.Reading else K.SurfaceHigh,
                edge = if (digit == value) K.ReadingDeep else K.SurfaceLow,
                contentPadding = PaddingValues(0.dp),
            ) { GameText(value.toString(), style = MaterialTheme.typography.headlineMedium) }
        }
    }
    if (finished) FeedbackLine(W.traced, true)
    BigButton(W.workAgain.str(), onClick = { generation++; finished = false }, face = K.SurfaceHigh, edge = K.SurfaceLow, modifier = Modifier.fillMaxWidth())
    })
}

@Composable
private fun MaterialTray(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().shadow(10.dp, RoundedCornerShape(26.dp))
            .background(Brush.verticalGradient(listOf(K.Paper, K.PaperShade)), RoundedCornerShape(26.dp))
            .border(3.dp, Brush.verticalGradient(listOf(Color(0xFFDDA36B), Color(0xFF8F552C), Color(0xFFBD8551))), RoundedCornerShape(26.dp)).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

@Composable
private fun FeedbackLine(message: Txt, correct: Boolean) {
    Panel(Modifier.fillMaxWidth(), color = if (correct) K.GoodDeep else K.SurfaceHigh) {
        Text(message.str(), color = K.Text, style = MaterialTheme.typography.titleMedium)
    }
}
