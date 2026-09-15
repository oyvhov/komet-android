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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.komet.domain.txt
import app.komet.ui.components.str
import app.komet.ui.theme.LocalMotion
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** A delivery is the child's deliberate action after solving, with no time pressure or penalties. */
@Composable
fun MissionFlight(completed: Int, total: Int, launching: Boolean, modifier: Modifier = Modifier, missionId: String = "") {
    val motion = LocalMotion.current
    val clock = rememberSceneTime()
    val flight = remember(completed) { Animatable(0f) }
    LaunchedEffect(launching, motion) {
        if (launching) {
            if (motion) flight.animateTo(1f, tween(1050, easing = FastOutSlowInEasing))
            else flight.snapTo(1f)
        }
    }
    val description = txt("Rombasen har fått $completed av $total leveransar.", "Rombasen har fått $completed av $total leveranser.").str()
    val landscape = when {
        "collect" in missionId -> 0
        "fuel" in missionId || "lights" in missionId -> 1
        "robots" in missionId -> 2
        "code" in missionId -> 3
        "picnic" in missionId || "packs" in missionId -> 4
        "measure" in missionId -> 5
        else -> 6
    }
    val glow = Color(listOf(0xFF7AE9FF, 0xFFFFBC67, 0xFF6AFFCA, 0xFFDB9BFF, 0xFFA5EF90, 0xFFFFD58A, 0xFFACBEFF)[landscape])
    val dusk = Color(listOf(0xFF244877, 0xFF64344B, 0xFF214F58, 0xFF503477, 0xFF264B52, 0xFF634653, 0xFF394779)[landscape])
    Canvas(modifier.clipToBounds().semantics { contentDescription = description }) {
        val w = size.width; val h = size.height
        val unit = minOf(w / 360f, h / 150f)
        val t = clock.value
        val p = flight.value
        drawRect(Brush.verticalGradient(listOf(Color(0xFF10152E), dusk)))
        drawCircle(Brush.radialGradient(listOf(glow.copy(alpha = .17f), Color.Transparent),
            center = Offset(w * .72f, h * .56f), radius = w * .48f), w * .48f, Offset(w * .72f, h * .56f))
        repeat(24) { i ->
            val x = ((i * 79 + 17) % 359) / 360f * w
            val y = ((i * 43 + 11) % 101) / 150f * h
            val alpha = 0.45f + 0.25f * sin(t * 1.4f + i)
            drawCircle(Color.White.copy(alpha = alpha), (if (i % 4 == 0) 1.8f else 1f) * unit, Offset(x, y))
        }
        val moon = Offset(w * .36f, h * .27f)
        drawCircle(Brush.radialGradient(listOf(Color.White, glow, dusk), center = moon - Offset(8f, 9f) * unit, radius = 39f * unit), 23f * unit, moon)
        if (landscape % 2 == 1) rotate(-24f, moon) {
            drawOval(glow.copy(alpha = .45f), moon - Offset(36f, 9f) * unit, Size(72f, 18f) * unit, style = Stroke(3f * unit))
        }
        val hills = Path().apply {
            moveTo(0f, h * .82f)
            cubicTo(w * .15f, h * .5f, w * .22f, h * .8f, w * .45f, h * .72f)
            cubicTo(w * .7f, h * .54f, w * .8f, h * .7f, w, h * .61f)
            lineTo(w, h); lineTo(0f, h); close()
        }
        drawPath(hills, dusk.copy(alpha = .75f))
        drawOval(Brush.verticalGradient(listOf(dusk, Color(0xFF151B37)), startY = h * .81f, endY = h), Offset(-w * .1f, h * .81f), Size(w * 1.2f, h * .45f))
        drawOval(glow.copy(alpha = .16f), Offset(-w * .1f, h * .81f), Size(w * 1.2f, h * .13f))
        // Each mission setting has its own landmark, away from the learning pictures and answers.
        val ground = h * .87f
        val landmark = w * .43f
        when (landscape) {
            0 -> repeat(3) { i ->
                val cx = landmark + (i - 1) * 15f * unit
                val height = (if (i == 1) 40f else 26f) * unit
                drawCircle(Brush.radialGradient(listOf(glow.copy(alpha = .3f), Color.Transparent), center = Offset(cx, ground - height / 2), radius = height), height, Offset(cx, ground - height / 2))
                val crystal = Path().apply {
                    moveTo(cx, ground - height); lineTo(cx + 9f * unit, ground - height * .67f)
                    lineTo(cx + 6f * unit, ground); lineTo(cx - 6f * unit, ground)
                    lineTo(cx - 9f * unit, ground - height * .67f); close()
                }
                drawPath(crystal, Brush.linearGradient(listOf(Color.White, glow, dusk), Offset(cx - 8f * unit, ground - height), Offset(cx + 8f * unit, ground)))
                drawLine(Color.White.copy(alpha = .6f), Offset(cx, ground - height), Offset(cx, ground - 3f * unit), unit)
            }
            1 -> {
                drawRoundRect(dusk, Offset(landmark - 14f * unit, ground - 47f * unit), Size(28f, 48f) * unit, CornerRadius(9f * unit))
                drawRoundRect(Brush.verticalGradient(listOf(Color.White, glow, Color(0xFFDB674F))), Offset(landmark - 8f * unit, ground - 40f * unit), Size(16f, 33f) * unit, CornerRadius(6f * unit))
                drawOval(glow.copy(alpha = .25f), Offset(landmark - 25f * unit, ground - 4f * unit), Size(50f, 8f) * unit)
            }
            2 -> rotate(if (motion) t * 13f else 0f, Offset(landmark, ground - 24f * unit)) {
                repeat(8) { i -> rotate(i * 45f, Offset(landmark, ground - 24f * unit)) {
                    drawRoundRect(glow, Offset(landmark - 4f * unit, ground - 47f * unit), Size(8f, 14f) * unit, CornerRadius(2f * unit))
                } }
                drawCircle(glow, 16f * unit, Offset(landmark, ground - 24f * unit), style = Stroke(7f * unit))
                drawCircle(dusk, 8f * unit, Offset(landmark, ground - 24f * unit))
            }
            3 -> {
                val portal = Offset(landmark, ground - 24f * unit)
                drawOval(Brush.radialGradient(listOf(glow.copy(alpha = .7f), dusk, Color(0xFF111B3C)), center = portal, radius = 27f * unit), portal - Offset(18f, 30f) * unit, Size(36f, 60f) * unit)
                drawOval(glow, portal - Offset(18f, 30f) * unit, Size(36f, 60f) * unit, style = Stroke(3f * unit))
                repeat(5) { i ->
                    val angle = t * .7f + i * 1.256f
                    drawCircle(Color.White, 1.6f * unit, portal + Offset(cos(angle) * 17f, sin(angle) * 29f) * unit)
                }
            }
            4 -> repeat(3) { i ->
                val cx = landmark + (i - 1) * 17f * unit
                drawLine(glow.copy(alpha = .7f), Offset(cx, ground), Offset(cx, ground - 22f * unit), 3f * unit)
                drawOval(Brush.linearGradient(listOf(glow, dusk)), Offset(cx - 11f * unit, ground - (30f + i * 4f) * unit), Size(22f, 18f) * unit)
                drawCircle(Color(0xFFFFCF81), 3f * unit, Offset(cx, ground - 26f * unit))
            }
            5 -> {
                drawArc(glow.copy(alpha = .7f), 180f, 180f, false, Offset(landmark - 33f * unit, ground - 15f * unit), Size(66f, 32f) * unit, style = Stroke(6f * unit))
                drawLine(glow, Offset(landmark - 36f * unit, ground - 15f * unit), Offset(landmark + 36f * unit, ground - 15f * unit), 5f * unit, StrokeCap.Round)
            }
            else -> repeat(3) { i ->
                val cx = landmark + (i - 1) * 24f * unit
                drawLine(dusk, Offset(cx, ground), Offset(cx, ground - 20f * unit), 4f * unit)
                drawCircle(glow, 4f * unit, Offset(cx, ground - 20f * unit))
            }
        }
        // Rounded glass habitat gradually lights up as the player makes deliveries.
        val base = Offset(w * .8f, h * .79f)
        drawOval(Color(0xFF251A4A), Offset(base.x - 47f * unit, base.y), Size(94f * unit, 13f * unit))
        drawRoundRect(Brush.verticalGradient(listOf(glow, dusk), startY = base.y - 48f * unit, endY = base.y),
            Offset(base.x - 39f * unit, base.y - 48f * unit), Size(78f * unit, 51f * unit), CornerRadius(30f * unit))
        drawRoundRect(Color.White.copy(alpha = .3f), Offset(base.x - 27f * unit, base.y - 41f * unit), Size(40f * unit, 8f * unit), CornerRadius(5f * unit))
        val delivered = completed + if (p >= .95f) 1 else 0
        repeat(total) { i ->
            val x = base.x + (i - (total - 1) / 2f) * 8f * unit
            drawCircle(if (i < delivered) Color(0xFFFFDC68) else Color(0xFF25446E), 2.6f * unit, Offset(x, base.y - 10f * unit))
        }
        // A small courier follows an arcing route to the habitat.
        val x = w * (.12f + .62f * p)
        val y = h * (.67f - .36f * sin(p * PI).toFloat()) + if (launching) 0f else sin(t * 2f) * 2f * unit
        if (launching && p < .95f) {
            repeat(12) { i ->
                val tail = (p - i * .012f).coerceAtLeast(0f)
                val pos = Offset(w * (.12f + .62f * tail), h * (.67f - .36f * sin(tail * PI).toFloat()))
                drawCircle(Color(0xFFFFC65C).copy(alpha = (1f - i / 12f) * .65f), (4f - i * .22f) * unit, pos)
            }
        }
        rotate(if (launching) -20f + p * 40f else -12f, Offset(x, y)) {
            val fin = Path().apply {
                moveTo(x - 17f * unit, y); lineTo(x - 23f * unit, y + 14f * unit)
                lineTo(x + 3f * unit, y + 7f * unit); close()
            }
            drawPath(fin, Color(0xFFED6385))
            drawOval(Brush.verticalGradient(listOf(Color.White, Color(0xFF8CA5CF)), startY = y - 10f * unit, endY = y + 10f * unit),
                Offset(x - 23f * unit, y - 10f * unit), Size(46f * unit, 20f * unit))
            drawCircle(Color(0xFF213459), 7f * unit, Offset(x + 6f * unit, y))
            drawCircle(Color(0xFF75E8FF), 4.5f * unit, Offset(x + 6f * unit, y - unit))
        }
        if (p > .8f && motion) {
            val burst = ((p - .8f) / .2f).coerceIn(0f, 1f)
            repeat(10) { i ->
                val angle = i * PI.toFloat() / 5f
                val center = Offset(base.x + cos(angle) * burst * 60f * unit, base.y - 25f * unit + sin(angle) * burst * 45f * unit)
                drawLine(Color(0xFFFFDF70).copy(alpha = 1f - burst * .7f), center, center + Offset(cos(angle), sin(angle)) * 5f * unit, 2f * unit, StrokeCap.Round)
            }
            drawCircle(Color(0xFFFFDF70).copy(alpha = 1f - burst), burst * 48f * unit, Offset(base.x, base.y - 20f * unit), style = Stroke(2f * unit))
        }
    }
}
