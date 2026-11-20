package app.komet.ui.scene

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.komet.domain.Adventures
import app.komet.domain.Subject
import app.komet.ui.S
import app.komet.ui.components.str
import app.komet.ui.theme.K
import app.komet.ui.theme.LocalMotion
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

internal fun adventureColor(subject: Subject): Color = when (subject) {
    Subject.MATH -> Color(0xFF63E0FF)
    Subject.READING -> Color(0xFFFFCB72)
    Subject.ENGLISH -> Color(0xFFB9F27C)
    Subject.SPACE -> Color(0xFFB9A1FF)
}

@Composable
fun AdventureResident(subject: Subject, modifier: Modifier = Modifier) {
    val time = rememberSceneTime()
    Canvas(modifier) {
        // The tall resident's antenna extends above its body; leave room inside scrolling parents.
        inset(left = 0f, top = size.height * .08f, right = 0f, bottom = 0f) {
            drawCritter(adventureColor(subject), subject.ordinal % 3, time.value, 0f)
        }
    }
}

/** Four landmarks with three visible repairs. The scene is saved progress, not a decorative reward. */
@Composable
fun AdventureArtwork(subject: Subject, completed: Int, modifier: Modifier = Modifier, reveal: Boolean = false) {
    val motion = LocalMotion.current
    val time = rememberSceneTime()
    val progress = remember(subject, completed, reveal) {
        Animatable(if (reveal && motion) (completed - 1).coerceAtLeast(0).toFloat() else completed.toFloat())
    }
    LaunchedEffect(subject, completed, reveal, motion) {
        if (reveal && motion) {
            delay(350)
            progress.animateTo(completed.toFloat(), tween(1600, easing = FastOutSlowInEasing))
        } else progress.snapTo(completed.toFloat())
    }
    val description = Adventures.of(subject).title.str() + ". " + S.adventureProgress(completed, 3).str()
    Canvas(modifier.clipToBounds().semantics { contentDescription = description }) {
        val accent = adventureColor(subject)
        val t = time.value
        val glow = (progress.value / 3f).coerceIn(0f, 1f)
        drawRect(Brush.verticalGradient(listOf(Color(0xFF090E2C), lerp(Color(0xFF27264C), accent, 0.19f))))
        val horizon = Offset(size.width * .6f, size.height * .6f)
        drawCircle(Brush.radialGradient(listOf(accent.copy(alpha = .1f + glow * .18f), Color.Transparent), horizon, size.width * .65f), size.width * .65f, horizon)
        repeat(52) { i ->
            val at = Offset(((i * 137 + 19) % 761) / 761f * size.width, ((i * 83 + 9) % 257) / 430f * size.height)
            drawCircle(Color.White.copy(alpha = .4f + .23f * sin(t + i)), if (i % 7 == 0) 2.4f else 1.3f, at)
        }
        val unit = minOf(size.width / 760f, size.height / 430f)
        withTransform({
            translate((size.width - 760f * unit) / 2f, (size.height - 430f * unit) / 2f)
            scale(unit, unit, pivot = Offset.Zero)
        }) {
            val moon = Offset(120f, 80f)
            drawCircle(Brush.radialGradient(listOf(Color(0xFFFFF2CD), Color(0xFFBA85B3), Color(0xFF4A3E79)), moon - Offset(12f, 12f), 48f), 35f, moon)
            rotate(-24f, moon) { drawOval(accent.copy(alpha = .5f), Offset(66f, 67f), Size(108f, 24f), style = Stroke(3f)) }
            val far = Path().apply {
                moveTo(-160f, 350f); lineTo(-40f, 210f); lineTo(110f, 308f); lineTo(225f, 187f)
                lineTo(360f, 314f); lineTo(540f, 218f); lineTo(680f, 294f); lineTo(900f, 160f)
                lineTo(920f, 480f); lineTo(-160f, 480f); close()
            }
            drawPath(far, Color(0xFF363759))
            drawOval(Brush.verticalGradient(listOf(Color(0xFF4B4774), Color(0xFF171A36)), startY = 315f, endY = 465f), Offset(-180f, 316f), Size(1120f, 300f))
            drawOval(accent.copy(alpha = .12f), Offset(290f, 328f), Size(365f, 38f))
            when (subject) {
                Subject.MATH -> crystalBeacon(progress.value, accent, t)
                Subject.READING -> signalBridge(progress.value, accent, t)
                Subject.ENGLISH -> starGarden(progress.value, accent, t)
                Subject.SPACE -> telescope(progress.value, accent, t)
            }
            // A lit path carries the eye from the resident towards the restored place.
            repeat(7) { i ->
                val at = Offset(220f + i * 48f, 373f + sin(i * .7f) * 11f)
                drawOval(Color(0xFF12172E), at, Size(26f, 8f))
                drawOval(accent.copy(alpha = .18f + glow * .55f), at - Offset(0f, 2f), Size(26f, 8f))
            }
            if (progress.value > 2f) {
                val alpha = (progress.value - 2f).coerceIn(0f, 1f)
                repeat(18) { i ->
                    val phase = (t * .16f + i / 18f) % 1f
                    val at = Offset(290f + ((i * 73) % 340), 330f - phase * 245f)
                    drawCircle(accent.copy(alpha = alpha * (1f - phase) * .7f), 2f + i % 3, at)
                }
            }
        }
    }
}

private fun DrawScope.halo(at: Offset, radius: Float, color: Color, amount: Float) {
    if (amount <= 0f) return
    drawCircle(Brush.radialGradient(listOf(color.copy(alpha = .5f * amount.coerceIn(0f, 1f)), Color.Transparent), at, radius), radius, at)
}

private fun DrawScope.crystalBeacon(progress: Float, accent: Color, t: Float) {
    val top = (progress - 2f).coerceIn(0f, 1f)
    val beam = Path().apply { moveTo(462f, 147f); lineTo(317f, -30f); lineTo(603f, -30f); close() }
    drawPath(beam, Brush.verticalGradient(listOf(Color.Transparent, accent.copy(alpha = top * .4f)), startY = 0f, endY = 160f))
    drawRoundRect(Color(0xFF171D39), Offset(336f, 301f), Size(255f, 47f), CornerRadius(17f))
    drawRoundRect(Brush.verticalGradient(listOf(Color(0xFF8F94B5), Color(0xFF363D62))), Offset(340f, 299f), Size(247f, 30f), CornerRadius(12f))
    for (i in 0..2) {
        val charge = (progress - i).coerceIn(0f, 1f)
        val x = 377f + i * 85f
        val height = if (i == 1) 205f else 142f
        val y = 315f - height
        halo(Offset(x, y + height * .45f), 125f, accent, charge * (.85f + .1f * sin(t * 2f)))
        val crystal = Path().apply {
            moveTo(x, y); lineTo(x + 37f, y + 42f); lineTo(x + 25f, 312f)
            lineTo(x - 25f, 312f); lineTo(x - 37f, y + 42f); close()
        }
        drawPath(crystal, Brush.linearGradient(listOf(lerp(Color(0xFF67718B), Color.White, charge), lerp(Color(0xFF343E63), accent, charge), Color(0xFF203555)), Offset(x - 32f, y), Offset(x + 40f, 310f)))
        drawPath(crystal, K.Outline, style = Stroke(4f))
        drawLine(Color.White.copy(alpha = .2f + charge * .65f), Offset(x, y + 7f), Offset(x - 2f, 302f), 3f)
    }
}

private fun DrawScope.signalBridge(progress: Float, accent: Color, t: Float) {
    drawOval(Color(0xFF080C24), Offset(240f, 310f), Size(430f, 92f))
    for (x in listOf(268f, 658f)) {
        drawRoundRect(Brush.verticalGradient(listOf(Color(0xFF8A81AD), Color(0xFF343C68))), Offset(x - 23f, 158f), Size(46f, 192f), CornerRadius(15f))
        halo(Offset(x, 170f), 72f, accent, (progress / 3f).coerceIn(0f, 1f))
        drawCircle(lerp(Color(0xFF49466A), accent, (progress / 3f).coerceIn(0f, 1f)), 18f, Offset(x, 168f))
    }
    repeat(3) { i ->
        val charge = (progress - i).coerceIn(0f, 1f)
        val x = 287f + i * 119f
        val arch = Path().apply { moveTo(x, 277f); quadraticTo(x + 57f, 205f, x + 116f, 277f) }
        drawPath(arch, Color(0xFF202947), style = Stroke(18f, cap = StrokeCap.Round))
        drawPath(arch, accent.copy(alpha = .1f + charge * .9f), style = Stroke(10f, cap = StrokeCap.Round))
        drawRoundRect(lerp(Color(0xFF323952), accent, charge * .7f), Offset(x, 293f), Size(114f, 22f), CornerRadius(8f))
        if (charge > 0f) {
            val phase = (t * .4f + i * .3f) % 1f
            val at = Offset(x + 10f + phase * 94f, 285f - sin(phase * PI).toFloat() * 32f)
            halo(at, 24f, accent, charge)
            drawCircle(Color.White.copy(alpha = charge), 5f, at)
        }
    }
}

private fun DrawScope.starGarden(progress: Float, accent: Color, t: Float) {
    val colours = listOf(Color(0xFFFFB36B), Color(0xFFFF83BA), Color(0xFF92DAFF))
    repeat(3) { i ->
        val bloom = (progress - i).coerceIn(0f, 1f)
        val x = 331f + i * 134f
        val y = 273f - bloom * (if (i == 1) 106f else 79f)
        drawOval(Color(0xFF101D31), Offset(x - 43f, 319f), Size(86f, 22f))
        drawRoundRect(Brush.verticalGradient(listOf(Color(0xFF829E90), Color(0xFF3E5663))), Offset(x - 34f, 298f), Size(68f, 32f), CornerRadius(12f))
        val stem = Path().apply { moveTo(x, 305f); quadraticTo(x - 18f, 246f, x, y) }
        drawPath(stem, Color(0xFF65AFA0), style = Stroke(12f, cap = StrokeCap.Round))
        drawOval(accent.copy(alpha = .65f), Offset(x - 36f, 260f), Size(36f, 17f))
        drawOval(accent.copy(alpha = .8f), Offset(x + 2f, 240f), Size(32f, 16f))
        val center = Offset(x, y)
        halo(center, 90f, colours[i], bloom)
        repeat(6) { petal ->
            val angle = petal * 60f + sin(t * .9f + i) * 5f
            rotate(angle, center) {
                drawOval(Brush.verticalGradient(listOf(Color.White, colours[i], Color(0xFF595780)), startY = y - 45f, endY = y),
                    Offset(x - 12f - bloom * 7f, y - 12f - bloom * 39f), Size(24f + bloom * 14f, 18f + bloom * 32f))
            }
        }
        drawCircle(lerp(Color(0xFF557577), K.GoldTop, bloom), 11f + bloom * 5f, center)
        drawCircle(Color.White.copy(alpha = bloom), 4f, center - Offset(4f, 4f))
    }
}

private fun DrawScope.telescope(progress: Float, accent: Color, t: Float) {
    val power = progress.coerceIn(0f, 1f)
    val lens = (progress - 1f).coerceIn(0f, 1f)
    val sky = (progress - 2f).coerceIn(0f, 1f)
    drawOval(Color(0xFF1A213C), Offset(333f, 317f), Size(225f, 44f))
    for (x in listOf(355f, 528f)) drawLine(Color(0xFF73839F), Offset(446f, 267f), Offset(x, 336f), 22f, StrokeCap.Round)
    drawRoundRect(Brush.verticalGradient(listOf(Color(0xFFC5CADA), Color(0xFF556783))), Offset(421f, 233f), Size(50f, 89f), CornerRadius(14f))
    halo(Offset(446f, 276f), 45f, accent, power)
    drawCircle(lerp(Color(0xFF283651), accent, power), 13f, Offset(446f, 276f))
    rotate(-28f - sky * 8f, Offset(446f, 221f)) {
        drawRoundRect(Brush.verticalGradient(listOf(Color.White, Color(0xFFBBCADC), Color(0xFF3F5578)), startY = 179f, endY = 263f), Offset(370f, 179f), Size(209f, 84f), CornerRadius(26f))
        drawRoundRect(Color(0xFF334564), Offset(555f, 173f), Size(29f, 96f), CornerRadius(10f))
        drawOval(lerp(Color(0xFF233550), accent, lens), Offset(563f, 184f), Size(17f, 74f))
        halo(Offset(574f, 221f), 76f, accent, sky)
        drawRoundRect(Color(0xFF263856), Offset(349f, 202f), Size(34f, 33f), CornerRadius(8f))
    }
    val constellation = listOf(Offset(432f, 81f), Offset(507f, 61f), Offset(564f, 110f), Offset(642f, 62f), Offset(667f, 139f))
    constellation.zipWithNext().forEach { (a, b) -> drawLine(accent.copy(alpha = sky * .6f), a, b, 2.5f) }
    constellation.forEachIndexed { i, at ->
        halo(at, 25f, accent, sky)
        drawCircle(Color.White.copy(alpha = sky), 4f + sin(t + i), at)
    }
}
