package app.komet.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import app.komet.R
import app.komet.domain.*
import app.komet.ui.theme.K
import app.komet.ui.theme.LocalMotion
import app.komet.ui.theme.LocalVisualBox

@Composable
internal fun AtlasFlag(country: AtlasCountry, modifier: Modifier) {
    val description = country.description.str()
    Canvas(modifier.semantics { contentDescription = description }) {
        val ratio = when (country) {
            AtlasCountry.NO -> 22f / 16; AtlasCountry.DK -> 37f / 28; AtlasCountry.SE, AtlasCountry.PW -> 1.6f
            AtlasCountry.FI -> 18f / 11; AtlasCountry.IS -> 25f / 18; AtlasCountry.NG, AtlasCountry.BS -> 2f
            else -> 1.5f
        }
        val w = minOf(size.width, size.height * ratio) * .94f
        val h = w / ratio
        translate(center.x - w / 2, center.y - h / 2) {
            fun fill(c: Color) = drawRect(c, Offset.Zero, Size(w, h))
            fun cross(c: Color, x: Float, thickness: Float) {
                val band = thickness * h
                drawRect(c, Offset(x * w - band / 2, 0f), Size(band, h))
                drawRect(c, Offset(0f, (h - band) / 2), Size(w, band))
            }
            fun thirds(left: Color, right: Color) {
                fill(Color.White)
                drawRect(left, Offset.Zero, Size(w / 3, h))
                drawRect(right, Offset(w * 2 / 3, 0f), Size(w / 3, h))
            }
            when (country) {
                AtlasCountry.NO -> { fill(Color(0xFFBA0C2F)); cross(Color.White, 8f / 22, 4f / 16); cross(Color(0xFF00205B), 8f / 22, 2f / 16) }
                AtlasCountry.SE -> { fill(Color(0xFF006AA7)); cross(Color(0xFFFECC02), 6f / 16, .2f) }
                AtlasCountry.DK -> { fill(Color(0xFFC8102E)); cross(Color.White, 14f / 37, 4f / 28) }
                AtlasCountry.FI -> { fill(Color.White); cross(Color(0xFF002F6C), 6.5f / 18, 3f / 11) }
                AtlasCountry.IS -> { fill(Color(0xFF02529C)); cross(Color.White, 9f / 25, 4f / 18); cross(Color(0xFFDC1E35), 9f / 25, 2f / 18) }
                AtlasCountry.JP -> { fill(Color.White); drawCircle(Color(0xFFBC002D), .3f * h, Offset(w / 2, h / 2)) }
                AtlasCountry.PW -> { fill(Color(0xFF4AADD6)); drawCircle(Color(0xFFFFDE00), .3f * h, Offset(w * .4375f, h / 2)) }
                AtlasCountry.IT -> thirds(Color(0xFF009246), Color(0xFFCE2B37))
                AtlasCountry.FR -> thirds(Color(0xFF000091), Color(0xFFE1000F))
                AtlasCountry.PE -> thirds(Color(0xFFD91023), Color(0xFFD91023))
                AtlasCountry.NG -> thirds(Color(0xFF008751), Color(0xFF008751))
                AtlasCountry.BS -> {
                    fill(Color(0xFF00778B))
                    drawRect(Color(0xFFFFC72C), Offset(0f, h / 3), Size(w, h / 3))
                    drawPath(Path().apply { moveTo(0f, 0f); lineTo(h * .866f, h / 2); lineTo(0f, h); close() }, Color.Black)
                }
            }
            drawRect(K.Ink.copy(alpha = .45f), Offset.Zero, Size(w, h), style = Stroke(1.dp.toPx()))
        }
    }
}

private val water = Color(0xFF26647A)
private val gridInk = Color(0xFFEDFAEF)
private val beacon = Color(0xFFFF7556)

@Composable
internal fun AtlasMap(place: AtlasPlace) {
    val bounds = LocalVisualBox.current
    val width = minOf(bounds.width, bounds.height * 2, 700.dp)
    val description = Txt("Verdskart med ein prikk ved ${kotlin.math.abs(place.latitude)}° ${if (place.latitude >= 0) "nord" else "sør"}, ${kotlin.math.abs(place.longitude)}° ${if (place.longitude >= 0) "aust" else "vest"}.",
        "Verdenskart med en prikk ved ${kotlin.math.abs(place.latitude)}° ${if (place.latitude >= 0) "nord" else "sør"}, ${kotlin.math.abs(place.longitude)}° ${if (place.longitude >= 0) "øst" else "vest"}.").str()
    Box(Modifier.size(width, width / 2).clip(RoundedCornerShape(8.dp)).background(water).semantics { contentDescription = description }) {
        Image(painterResource(R.drawable.atlas_land), null, Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
        Canvas(Modifier.fillMaxSize()) {
            for (i in 1..11) {
                val x = i * size.width / 12
                drawLine(K.Ink.copy(alpha = .14f), Offset(x, 0f), Offset(x, size.height))
            }
            for (i in 1..5) {
                val y = i * size.height / 6
                drawLine(if (i == 3) gridInk.copy(alpha = .65f) else K.Ink.copy(alpha = .14f), Offset(0f, y), Offset(size.width, y))
            }
            val point = Offset((place.longitude + 180) / 360f * size.width, (90 - place.latitude) / 180f * size.height)
            val r = (size.width * .012f).coerceIn(4.dp.toPx(), 8.dp.toPx())
            drawCircle(K.Ink, r * 1.4f, point)
            drawCircle(Color.White, r * 1.18f, point)
            drawCircle(beacon, r * .83f, point)
        }
    }
}

@Composable
internal fun SeaChart(map: Visual.SeaChart, docked: Boolean = false, disabled: List<Int> = emptyList(), onDock: ((Int) -> Unit)? = null) {
    val bounds = LocalVisualBox.current
    val mapSize = minOf(bounds.width / 1.16f, bounds.height, 400.dp)
    val text = rememberTextMeasurer()
    val description = (if (map.harbours.isEmpty()) Txt("Sjøkart med nord opp. Båten er i ${map.boat.address}, fyret i ${map.tower.address}.")
        else Txt("Sjøkart med nord opp. Båten er i ${if (docked) map.tower.address else map.boat.address}.")) +
        (map.kmPerStep?.let { Txt(" Eitt rutesteg er $it km.", " Ett rutesteg er $it km.") } ?: Txt(""))
    val semanticsText = description.str()
    val scaleLabel = map.kmPerStep?.let { Txt("1 rutesteg = $it km").str() }
    val voyage = animateFloatAsState(if (docked) 1f else 0f, tween(if (LocalMotion.current) 680 else 0), label = "dock")
    Box(Modifier.size(mapSize * 1.16f, mapSize).clip(RoundedCornerShape(8.dp)).background(water)) {
        Canvas(Modifier.fillMaxSize().semantics { contentDescription = semanticsText }) {
            val unit = size.height / 5.5f
            val origin = Offset((size.width - unit * 4) / 2, unit * .65f)
            fun label(word: String, x: Float, y: Float, factor: Float = .38f) {
                val layout = text.measure(word, TextStyle(color = gridInk, fontWeight = FontWeight.Bold, fontSize = (factor * unit).toSp()))
                drawText(layout, topLeft = Offset(x - layout.size.width / 2f, y - layout.size.height / 2f))
            }
            fun point(cell: ChartCell) = origin + Offset((cell.column + .5f) * unit, (cell.row + .5f) * unit)
            for (i in 0..4) {
                val span = unit * i
                drawLine(gridInk.copy(alpha = .4f), origin + Offset(span, 0f), origin + Offset(span, unit * 4), 1.dp.toPx())
                drawLine(gridInk.copy(alpha = .4f), origin + Offset(0f, span), origin + Offset(unit * 4, span), 1.dp.toPx())
                if (i < 4) {
                    label(('A' + i).toString(), origin.x + span + unit / 2, unit * .32f)
                    label((i + 1).toString(), origin.x - unit * .33f, origin.y + span + unit / 2)
                }
            }
            label("↑", size.width - unit * .48f, origin.y + .28f * unit, .55f)
            label("N", size.width - unit * .48f, origin.y + .73f * unit)
            scaleLabel?.let { label(it, size.width / 2, unit * 5.06f) }
            fun polygon(at: Offset, color: Color, vararg vertices: Pair<Float, Float>) {
                drawPath(Path().apply {
                    vertices.forEachIndexed { i, (x, y) -> if (i == 0) moveTo(at.x + x * unit, at.y + y * unit) else lineTo(at.x + x * unit, at.y + y * unit) }
                    close()
                }, color)
            }
            for ((index, berth) in map.harbours.withIndex()) {
                val p = point(berth)
                val isTarget = docked && berth == map.tower
                val timber = if (index in disabled && !isTarget) Color(0xFF839B9A) else Color(0xFFE8C491)
                drawOval(Color(0xFFABC39D), p - Offset(unit * .37f, unit * .39f), Size(unit * .56f, unit * .44f))
                drawLine(timber, p + Offset(-unit * .1f, -unit * .18f), p + Offset(-unit * .1f, unit * .24f), unit * .11f)
                drawLine(timber, p + Offset(-unit * .16f, unit * .23f), p + Offset(unit * .23f, unit * .23f), unit * .1f)
            }
            val boat = if (docked) {
                val start = point(map.boat)
                val end = point(map.tower) + Offset(unit * .19f, -unit * .1f)
                val position = start + (end - start) * voyage.value
                drawLine(gridInk, start, position, unit * .035f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(unit * .11f, unit * .09f)))
                position
            } else point(map.boat)
            polygon(boat, Color(0xFFF9EDBE), -.02f to -.38f, -.02f to .07f, -.33f to .07f)
            polygon(boat, beacon, .02f to -.27f, .28f to .07f, .02f to .07f)
            polygon(boat, K.Ink, -.4f to .12f, .39f to .12f, .22f to .31f, -.21f to .31f)
            if (map.harbours.isEmpty()) {
                val tower = point(map.tower)
                drawOval(Color(0xFFA4BE8F), tower + Offset(-.43f * unit, .17f * unit), Size(.86f * unit, .24f * unit))
                polygon(tower, Color.White, -.1f to -.19f, .1f to -.19f, .17f to .28f, -.17f to .28f)
                polygon(tower, beacon, -.12f to -.04f, .12f to -.04f, .14f to .09f, -.14f to .09f)
                drawRect(Color(0xFFFFDB71), tower + Offset(-.11f * unit, -.29f * unit), Size(.22f * unit, .1f * unit))
                polygon(tower, K.Ink, -.19f to -.29f, 0f to -.42f, .19f to -.29f)
            }
        }
        if (onDock != null && !docked) {
            val step = mapSize / 5.5f
            val hit = maxOf(step, 64.dp)
            map.harbours.forEachIndexed { index, berth ->
                val x = (mapSize * 1.16f - step * 4) / 2 + step * (berth.column + .5f)
                val y = step * (berth.row + 1.15f)
                val name = Txt("Kai ${berth.address}").str()
                Box(Modifier.offset(x - hit / 2, y - hit / 2).size(hit).clickable(enabled = index !in disabled, role = Role.Button) { onDock(index) }
                    .semantics { contentDescription = name })
            }
        }
    }
}
