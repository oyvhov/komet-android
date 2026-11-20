package app.komet.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import app.komet.audio.Sfx
import app.komet.domain.*
import app.komet.ui.KometViewModel
import app.komet.ui.components.*
import app.komet.ui.theme.K
import app.komet.ui.theme.LocalMotion
import kotlinx.coroutines.delay

internal fun DrawScope.drawRover(c: Offset, unit: Float) {
    fun point(x: Float, y: Float) = c + Offset(x * unit, y * unit)
    fun plate(x: Float, y: Float, w: Float, h: Float, color: Color) =
        drawRoundRect(color, point(x, y), Size(unit * w, unit * h), CornerRadius(unit * .05f))
    drawOval(Color(0x500F111C), point(-.48f, .23f), Size(unit * .96f, unit * .17f))
    drawLine(Color(0xFFFEDA96), point(.16f, -.04f), point(.16f, -.34f), unit * .064f)
    plate(.035f, -.44f, .32f, .17f, Color(0xFFFFEDD2))
    drawCircle(Color(0xFF294756), unit * .056f, point(.26f, -.36f))
    plate(-.42f, -.13f, .83f, .3f, Color(0xFFBA712C))
    plate(-.43f, -.18f, .87f, .19f, Color(0xFFFFCA72))
    plate(-.35f, -.21f, .36f, .095f, Color(0xFF54798A))
    for (x in listOf(-.32f, 0f, .32f)) {
        drawCircle(Color(0xFF24303A), unit * .155f, point(x, .19f))
        drawCircle(Color(0xFF95AAB0), unit * .082f, point(x, .19f))
        drawCircle(Color(0xFF475966), unit * .034f, point(x, .19f))
    }
}

@Composable
private fun Terrain(route: MarsRoute, trail: List<Int>) {
    val position = trail.last()
    val cell = { p: Int -> "${p % route.width + 1}, ${p / route.width + 1}" }
    val rocks = route.rows.joinToString("").mapIndexedNotNull { i, tile -> if (tile == '#') cell(i) else null }.joinToString(" · ")
    val description = Txt("Kolonne og rad. Rover ${cell(position)}. Flagg ${cell(route.flag)}. Stein $rocks.",
        "Kolonne og rad. Rover ${cell(position)}. Flagg ${cell(route.flag)}. Stein $rocks.").str()
    Canvas(Modifier.widthIn(max = 400.dp).fillMaxWidth().aspectRatio(route.width.toFloat() / route.rows.size)
        .clip(RoundedCornerShape(22.dp)).semantics { contentDescription = description }) {
        val tile = size.width / route.width
        fun centerOf(i: Int) = Offset((i % route.width + .5f) * tile, (i / route.width + .5f) * tile)
        drawRect(Brush.linearGradient(listOf(Color(0xFFCC9266), Color(0xFFAA694C), Color(0xFF884E40))))
        route.rows.joinToString("").forEachIndexed { i, type ->
            val c = centerOf(i)
            val origin = c - Offset(tile / 2, tile / 2)
            drawRect(Color(0x50734136), origin, Size(tile, tile), style = Stroke(tile * .024f))
            repeat(7) { k ->
                val x = ((i * 23 + k * 31) % 89 + 5) / 100f
                val y = ((i * 41 + k * 17) % 86 + 5) / 100f
                drawOval(Color(0x48905240), origin + Offset(x * tile, y * tile), Size(tile * .055f, tile * .021f))
            }
            if (type == '#') {
                fun rock(vararg coords: Pair<Float, Float>) = Path().apply {
                    coords.forEachIndexed { n, (x, y) ->
                        if (n == 0) moveTo(c.x + x * tile, c.y + y * tile) else lineTo(c.x + x * tile, c.y + y * tile)
                    }
                    close()
                }
                drawOval(Color(0x60704238), c - Offset(.38f, -.09f) * tile, Size(tile * .81f, tile * .22f))
                drawPath(rock(-.36f to .13f, -.25f to -.17f, .02f to -.31f, .28f to -.17f, .38f to .14f, .09f to .25f), Color(0xFF794F48))
                drawPath(rock(-.36f to .13f, -.25f to -.17f, .02f to -.31f, .17f to -.12f, -.08f to .05f), Color(0xFFE4AF88))
                drawPath(rock(-.36f to .13f, -.08f to .05f, .17f to -.12f, .09f to .25f), Color(0xFFB48269))
            }
            if (type == 'F') {
                val top = c + Offset(-.12f, -.34f) * tile
                drawLine(Color(0xFFFFEAC1), top, c + Offset(-.12f, .29f) * tile, tile * .046f)
                val flag = Path().apply {
                    moveTo(top.x, top.y); relativeLineTo(.46f * tile, .08f * tile)
                    relativeLineTo(-.46f * tile, .17f * tile); close()
                }
                drawPath(flag, Color(0xFF194C54))
            }
        }
        trail.zipWithNext().forEach { (from, to) ->
            drawLine(Color(0xFF693F35), centerOf(from), centerOf(to), tile * .065f, StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(tile * .07f, tile * .09f)))
        }
        drawRover(centerOf(position), tile * .83f)
    }
}

@Composable
internal fun RoverStudio(vm: KometViewModel, route: MarsRoute, nextLabel: Txt, onSolved: () -> Unit, next: () -> Unit) {
    var program by rememberSaveable { mutableStateOf("") }
    var misses by rememberSaveable { mutableIntStateOf(0) }
    var solved by rememberSaveable { mutableStateOf(false) }
    var failure by rememberSaveable { mutableStateOf("") }
    var moving by remember { mutableStateOf(false) }
    var active by remember { mutableIntStateOf(-1) }
    val trail = remember { mutableStateListOf(if (solved) route.flag else route.start) }
    val editable = !moving && !solved
    val motion = LocalMotion.current
    val prompt = Txt("Køyr roveren til flagget.", "Kjør roveren til flagget.")
    val example = route.example.joinToString(" ") { it.arrow }
    val spokenExample = Txt(route.example.joinToString(", ") { it.label.nn }, route.example.joinToString(", ") { it.label.nb })
    val note = when {
        solved -> Txt("Framme! Roveren følgde pilene dine.", "Fremme! Roveren fulgte pilene dine.")
        failure == "wall" -> Txt("Her er vegen stengd. Endre pilene og prøv igjen.", "Her er veien stengt. Endre pilene og prøv igjen.")
        failure == "short" -> Txt("Ruta slutta før flagget. Prøv å endre pilene.", "Ruta sluttet før flagget. Prøv å endre pilene.")
        program.length == 16 -> Txt("Ruta er full. Køyr eller ta bort ei pil.", "Ruta er full. Kjør eller ta bort en pil.")
        else -> Txt("Legg inn éi pil for kvart felt. Køyr når ruta er klar.", "Legg inn én pil for hvert felt. Kjør når ruta er klar.")
    }
    fun edit(value: String) {
        program = value
        active = -1
        failure = ""
        trail.clear()
        trail.add(route.start)
    }
    LaunchedEffect(moving) {
        if (!moving) return@LaunchedEffect
        trail.clear()
        trail.add(route.start)
        failure = ""
        for ((i, arrow) in program.withIndex()) {
            active = i
            val move = Drive.entries.first { it.arrow.single() == arrow }
            delay(if (motion) 420 else 160)
            val dest = route.step(trail.last(), move)
            if (dest == null) { failure = "wall"; break }
            trail.add(dest)
        }
        solved = failure.isEmpty() && trail.last() == route.flag
        if (solved) {
            onSolved()
            vm.feedback.sfx(Sfx.CORRECT)
            vm.say(Txt("Framme ved flagget!", "Fremme ved flagget!"))
        } else {
            if (failure.isEmpty()) failure = "short"
            misses++
            vm.say(if (misses >= 2) spokenExample else if (failure == "wall") Txt("Vegen er stengd her.", "Veien er stengt her.") else Txt("Roveren treng ei anna rute.", "Roveren trenger en annen rute."))
        }
        moving = false
    }
    StudioLayout(vm, prompt, board = { Terrain(route, trail) }) {
        if (program.isNotEmpty()) Text(buildAnnotatedString {
            program.forEachIndexed { i, ch ->
                withStyle(SpanStyle(color = if (i == active) K.GoldTop else K.Text)) { append(ch) }
                if (i < program.lastIndex) append(' ')
            }
        }, style = MaterialTheme.typography.headlineSmall)
        if (!solved) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Drive.entries.forEach { direction ->
                    StudioKey(direction.arrow, direction.label, editable && program.length < 16, Modifier.weight(1f)) { edit(program + direction.arrow) }
                }
            }
        }
        StudioNote(note)
        if (misses >= 2 && !solved) StudioNote(Txt("Prøv denne ruta: $example", "Prøv denne ruta: $example"))
        if (editable && program.isNotEmpty()) {
            PressSurface({ edit(program.dropLast(1)) }, Modifier.fillMaxWidth().heightIn(min = 64.dp), face = K.Surface, edge = K.SurfaceLow) {
                GameText(Txt("Ta bort siste pil").str(), style = MaterialTheme.typography.titleMedium)
            }
        }
        BigButton((if (solved) nextLabel else Txt("Køyr ruta", "Kjør ruta")).str(), {
            if (solved) next() else moving = true
        }, Modifier.fillMaxWidth(), face = K.Explore, edge = K.ExploreDeep, enabled = !moving && program.isNotEmpty())
    }
}
