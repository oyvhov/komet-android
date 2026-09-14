package app.komet.ui.scene

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import app.komet.domain.HeroLook
import app.komet.domain.HeroPalette
import app.komet.ui.theme.K
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

enum class HeroPose { IDLE, WALK, CHEER, WAVE }

enum class BoltMood { IDLE, HAPPY, TALK, SURPRISED }

private val SuitWhite = Color(0xFFF4F7FF)
private val SuitShade = Color(0xFFC3CDE6)
private val SuitBack = Color(0xFFAAB5D6)
private val VisorTop = Color(0xFF24357E)
private val VisorBottom = Color(0xFF0A1036)
private val EyeInk = Color(0xFF1A1440)
private val BoltCyan = Color(0xFF5CF2FF)
private val BoltTeal = Color(0xFF27B8CC)
private val BubbleBottom = Color(0xFFE9EEFF)

/**
 * The child's astronaut, drawn in code. The pose, facing and clock are read while drawing only, so a
 * walking figure never recomposes. The canvas is two units wide for three tall.
 */
@Composable
fun Astronaut(
    look: HeroLook,
    time: State<Float>,
    modifier: Modifier = Modifier,
    pose: () -> HeroPose = { HeroPose.IDLE },
    facingLeft: () -> Boolean = { false },
) {
    val safe = look.safe()
    val suit = Color(HeroPalette.suits[safe.suit])
    val skin = Color(HeroPalette.skins[safe.skin])
    val hair = Color(HeroPalette.hairs[safe.hair])
    Canvas(modifier) {
        drawAstronaut(suit, skin, hair, safe.hairStyle, pose(), time.value, facingLeft())
    }
}

/** Draws the astronaut with its feet at the bottom centre of the canvas. */
fun DrawScope.drawAstronaut(suit: Color, skin: Color, hair: Color, hairStyle: Int, pose: HeroPose, t: Float, facingLeft: Boolean) {
    val s = min(size.width / 100f, size.height / 150f)
    val suitDeep = lerp(suit, Color.Black, 0.3f)
    val line = Stroke(width = 3.2f, join = StrokeJoin.Round, cap = StrokeCap.Round)

    var legFront = 0f
    var legBack = 0f
    var armFront = 10f
    var armBack = -8f
    var bob = 0f
    var mouthOpen = false
    when (pose) {
        HeroPose.IDLE -> {
            val breath = sin(t * 2.3f)
            bob = breath * 1.2f
            armFront = 10f + breath * 3f
            armBack = -8f - breath * 3f
        }
        HeroPose.WALK -> {
            val phase = t * 2f * PI.toFloat() * 1.9f
            val swing = sin(phase)
            legFront = 30f * swing
            legBack = -30f * swing
            armFront = -32f * swing
            armBack = 32f * swing
            bob = -abs(cos(phase)) * 3.5f
        }
        HeroPose.CHEER -> {
            // Arms out in a V beside the helmet, never across the face.
            bob = -abs(sin(t * 4.6f)) * 12f
            armFront = 120f + sin(t * 9f) * 10f
            armBack = -135f - sin(t * 9f) * 10f
            legFront = 10f
            legBack = -10f
            mouthOpen = true
        }
        HeroPose.WAVE -> {
            bob = sin(t * 2.3f) * 1.2f
            armFront = 112f + sin(t * 10f) * 16f
            armBack = -8f
            mouthOpen = true
        }
    }
    val blink = pose != HeroPose.CHEER && (t % 3.6f) > 3.45f

    withTransform({
        translate(size.width / 2, size.height)
        scale(if (facingLeft) -s else s, s, pivot = Offset.Zero)
    }) {
        // A soft shadow stays on the ground while the body bobs.
        drawOval(Color.Black.copy(alpha = 0.28f), Offset(-30f, -7f), Size(60f, 12f))
        withTransform({ translate(0f, bob) }) {
            limb(Offset(-7f, -72f), armBack, 27f, 12f, SuitBack, suitDeep, line, glove = true)
            // Backpack
            drawRoundRect(suitDeep, Offset(-33f, -86f), Size(22f, 40f), CornerRadius(8f))
            drawRoundRect(K.Outline, Offset(-33f, -86f), Size(22f, 40f), CornerRadius(8f), style = line)
            drawLine(Color.White.copy(alpha = 0.25f), Offset(-28f, -80f), Offset(-28f, -54f), strokeWidth = 3f, cap = StrokeCap.Round)
            limb(Offset(-7f, -40f), legBack, 31f, 15f, SuitBack, suitDeep, line, glove = false)
            limb(Offset(8f, -40f), legFront, 31f, 15f, SuitWhite, suitDeep, line, glove = false)

            // Body
            val torso = Brush.verticalGradient(listOf(SuitWhite, SuitShade), startY = -82f, endY = -32f)
            drawRoundRect(torso, Offset(-22f, -82f), Size(45f, 50f), CornerRadius(17f))
            drawRoundRect(K.Outline, Offset(-22f, -82f), Size(45f, 50f), CornerRadius(17f), style = line)
            drawRoundRect(suit, Offset(-1f, -68f), Size(17f, 15f), CornerRadius(4f))
            drawRoundRect(K.Outline.copy(alpha = 0.7f), Offset(-1f, -68f), Size(17f, 15f), CornerRadius(4f), style = Stroke(2f))
            drawCircle(Color.White, 2.2f, Offset(3.5f, -63f))
            drawCircle(K.Gold, 2.2f, Offset(10.5f, -63f))
            drawRoundRect(lerp(suit, Color.White, 0.35f), Offset(2f, -58f), Size(11f, 2.6f), CornerRadius(1.3f))
            // Belt
            drawRoundRect(suitDeep, Offset(-21f, -44f), Size(43f, 6f), CornerRadius(3f))

            // Collar
            drawRoundRect(suit, Offset(-24f, -88f), Size(50f, 11f), CornerRadius(5.5f))
            drawRoundRect(K.Outline, Offset(-24f, -88f), Size(50f, 11f), CornerRadius(5.5f), style = line)

            // Antenna with a light that pulses
            drawLine(K.Outline, Offset(-12f, -134f), Offset(-19f, -147f), strokeWidth = 4.2f, cap = StrokeCap.Round)
            drawLine(SuitShade, Offset(-12f, -134f), Offset(-19f, -147f), strokeWidth = 1.8f, cap = StrokeCap.Round)
            val pulse = 0.55f + 0.45f * sin(t * 3.1f)
            drawCircle(K.Gold.copy(alpha = 0.35f * pulse), 7.5f, Offset(-19.5f, -148f))
            drawCircle(K.Gold, 3.8f, Offset(-19.5f, -148f))
            drawCircle(K.Outline, 3.8f, Offset(-19.5f, -148f), style = Stroke(1.8f))

            // Helmet
            val helmetCenter = Offset(3f, -108f)
            drawCircle(
                Brush.radialGradient(listOf(Color.White, SuitWhite, SuitShade), center = Offset(-8f, -122f), radius = 46f),
                33f,
                helmetCenter,
            )
            drawCircle(K.Outline, 33f, helmetCenter, style = line)

            // Visor with the face behind it
            val visorRect = Rect(Offset(11.5f, -105f), 22f)
            val visor = Path().apply { addOval(Rect(visorRect.left - 1f, visorRect.top + 3f, visorRect.right + 1f, visorRect.bottom - 1f)) }
            drawPath(visor, Brush.verticalGradient(listOf(VisorTop, VisorBottom), startY = -126f, endY = -84f))
            clipPath(visor) {
                val face = Offset(13f, -100f)
                drawCircle(skin, 18.5f, face)
                drawHair(hair, hairStyle, face)
                if (blink) {
                    drawLine(EyeInk, Offset(3.5f, -99f), Offset(9f, -99f), strokeWidth = 2.2f, cap = StrokeCap.Round)
                    drawLine(EyeInk, Offset(18f, -99f), Offset(23.5f, -99f), strokeWidth = 2.2f, cap = StrokeCap.Round)
                } else {
                    drawOval(EyeInk, Offset(3.6f, -103f), Size(5.4f, 7.6f))
                    drawOval(EyeInk, Offset(18.1f, -103f), Size(5.4f, 7.6f))
                    drawCircle(Color.White, 1.3f, Offset(7.2f, -101.3f))
                    drawCircle(Color.White, 1.3f, Offset(21.7f, -101.3f))
                }
                drawCircle(Color(0xFFFF7A8A).copy(alpha = 0.45f), 3.2f, Offset(0.5f, -92.5f))
                drawCircle(Color(0xFFFF7A8A).copy(alpha = 0.45f), 3.2f, Offset(26f, -92.5f))
                if (mouthOpen) {
                    drawOval(Color(0xFF7A2432), Offset(9.5f, -93f), Size(8f, 6f))
                } else {
                    drawArc(EyeInk, 20f, 140f, false, Offset(9f, -96f), Size(9f, 6f), style = Stroke(2f, cap = StrokeCap.Round))
                }
                // The visor reflects the sky.
                drawRect(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.12f), Color.Transparent), startY = -127f, endY = -100f), Offset(-12f, -128f), Size(48f, 30f))
            }
            drawPath(visor, K.Outline, style = Stroke(2.6f))
            drawArc(Color.White.copy(alpha = 0.85f), 200f, 55f, false, Offset(-4f, -121f), Size(30f, 28f), style = Stroke(3f, cap = StrokeCap.Round))
            drawCircle(Color.White.copy(alpha = 0.85f), 1.8f, Offset(25f, -118f))

            limb(Offset(11f, -72f), armFront, 27f, 12f, SuitWhite, suitDeep, line, glove = true)
        }
    }
}

private fun DrawScope.drawHair(hair: Color, style: Int, face: Offset) {
    val top = face.y - 18.5f
    when (style) {
        0 -> {
            // Short, with a soft wave over the forehead.
            val path = Path().apply {
                moveTo(face.x - 22f, top - 4f)
                lineTo(face.x + 22f, top - 4f)
                lineTo(face.x + 22f, top + 9f)
                quadraticTo(face.x + 14f, top + 14f, face.x + 7f, top + 9f)
                quadraticTo(face.x, top + 15f, face.x - 7f, top + 9f)
                quadraticTo(face.x - 14f, top + 14f, face.x - 22f, top + 9f)
                close()
            }
            drawPath(path, hair)
        }
        1 -> {
            // A side fringe.
            val path = Path().apply {
                moveTo(face.x - 22f, top - 4f)
                lineTo(face.x + 22f, top - 4f)
                lineTo(face.x + 22f, top + 8f)
                quadraticTo(face.x + 2f, top + 8f, face.x - 14f, top + 19f)
                lineTo(face.x - 22f, top + 20f)
                close()
            }
            drawPath(path, hair)
        }
        2 -> {
            // Curls.
            drawRect(hair, Offset(face.x - 22f, top - 4f), Size(44f, 8f))
            for (i in -3..3) drawCircle(hair, 5.6f, Offset(face.x + i * 6.2f, top + 5f + (i % 2) * 1.5f))
        }
        else -> {
            // Long hair framing the face.
            val path = Path().apply {
                moveTo(face.x - 22f, top - 4f)
                lineTo(face.x + 22f, top - 4f)
                lineTo(face.x + 22f, face.y + 14f)
                lineTo(face.x + 15f, face.y + 14f)
                quadraticTo(face.x + 16f, top + 10f, face.x + 4f, top + 8f)
                quadraticTo(face.x - 8f, top + 12f, face.x - 15f, face.y + 14f)
                lineTo(face.x - 22f, face.y + 14f)
                close()
            }
            drawPath(path, hair)
        }
    }
}

/** An arm or a leg hanging from [pivot], swung [angle] degrees forward, with a glove or a boot. */
private fun DrawScope.limb(pivot: Offset, angle: Float, length: Float, width: Float, color: Color, end: Color, line: Stroke, glove: Boolean) {
    rotate(-angle, pivot) {
        val topLeft = Offset(pivot.x - width / 2, pivot.y - width / 2)
        val limbSize = Size(width, length + width / 2)
        drawRoundRect(color, topLeft, limbSize, CornerRadius(width / 2))
        drawRoundRect(K.Outline, topLeft, limbSize, CornerRadius(width / 2), style = line)
        val tip = Offset(pivot.x, pivot.y + length)
        if (glove) {
            drawCircle(end, 7.6f, tip)
            drawCircle(K.Outline, 7.6f, tip, style = line)
        } else {
            val boot = Offset(tip.x - 9f, tip.y - 5f)
            drawRoundRect(end, boot, Size(23f, 12f), CornerRadius(6f))
            drawRoundRect(K.Outline, boot, Size(23f, 12f), CornerRadius(6f), style = line)
        }
    }
}

/** Bolt, the robot friend: hovers, blinks and shows its mood on a screen face. */
@Composable
fun Bolt(time: State<Float>, modifier: Modifier = Modifier, mood: () -> BoltMood = { BoltMood.IDLE }) {
    Canvas(modifier) { drawBolt(mood(), time.value) }
}

fun DrawScope.drawBolt(mood: BoltMood, t: Float) {
    val s = min(size.width, size.height) / 100f
    val line = Stroke(width = 3.2f, join = StrokeJoin.Round, cap = StrokeCap.Round)
    val bob = sin(t * 2.4f) * 3f
    withTransform({
        translate((size.width - 100f * s) / 2, (size.height - 100f * s) / 2)
        scale(s, s, pivot = Offset.Zero)
        translate(0f, bob)
    }) {
        // Thruster flame
        val flicker = 0.8f + 0.2f * sin(t * 31f)
        val flame = Path().apply {
            moveTo(41f, 80f)
            quadraticTo(50f, 80f + 22f * flicker, 59f, 80f)
            close()
        }
        drawPath(flame, Brush.verticalGradient(listOf(Color.White, BoltCyan, Color.Transparent), startY = 80f, endY = 80f + 22f * flicker))

        val happy = mood == BoltMood.HAPPY
        val handLift = if (happy) -22f + sin(t * 10f) * 5f else 0f
        drawHand(Offset(8f, 60f + sin(t * 2.4f + 1f) * 3f + handLift), line)
        drawHand(Offset(92f, 60f + sin(t * 2.4f + 2.2f) * 3f + handLift), line)

        drawRoundRect(BoltTeal, Offset(9f, 38f), Size(10f, 22f), CornerRadius(4f))
        drawRoundRect(K.Outline, Offset(9f, 38f), Size(10f, 22f), CornerRadius(4f), style = line)
        drawRoundRect(BoltTeal, Offset(81f, 38f), Size(10f, 22f), CornerRadius(4f))
        drawRoundRect(K.Outline, Offset(81f, 38f), Size(10f, 22f), CornerRadius(4f), style = line)

        drawLine(K.Outline, Offset(50f, 14f), Offset(50f, 3f), strokeWidth = 4.4f, cap = StrokeCap.Round)
        drawLine(SuitShade, Offset(50f, 14f), Offset(50f, 3f), strokeWidth = 2f, cap = StrokeCap.Round)
        val glow = 0.5f + 0.5f * sin(t * 4f)
        drawCircle(K.Gold.copy(alpha = 0.4f * glow), 9f, Offset(50f, 2f))
        drawCircle(K.Gold, 4.8f, Offset(50f, 2f))
        drawCircle(K.Outline, 4.8f, Offset(50f, 2f), style = Stroke(2f))

        val body = Offset(50f, 48f)
        drawCircle(Brush.radialGradient(listOf(Color.White, Color(0xFFDDE6F5), Color(0xFF93A5C9)), center = Offset(36f, 30f), radius = 62f), 36f, body)
        clipPath(Path().apply { addOval(Rect(body, 36f)) }) {
            drawRect(BoltTeal, Offset(10f, 70f), Size(80f, 7f))
        }
        drawCircle(K.Outline, 36f, body, style = line)

        // Screen face
        drawRoundRect(Brush.verticalGradient(listOf(Color(0xFF1B2A6B), Color(0xFF080E30)), startY = 29f, endY = 61f), Offset(24f, 29f), Size(52f, 32f), CornerRadius(13f))
        drawRoundRect(K.Outline, Offset(24f, 29f), Size(52f, 32f), CornerRadius(13f), style = Stroke(2.6f))
        val blink = mood == BoltMood.IDLE && (t % 4.2f) > 4.05f
        when (mood) {
            BoltMood.HAPPY -> {
                for (x in listOf(40f, 60f)) {
                    drawArc(BoltCyan.copy(alpha = 0.35f), 195f, 150f, false, Offset(x - 8f, 38f), Size(16f, 14f), style = Stroke(7f, cap = StrokeCap.Round))
                    drawArc(BoltCyan, 195f, 150f, false, Offset(x - 8f, 38f), Size(16f, 14f), style = Stroke(3.6f, cap = StrokeCap.Round))
                }
            }
            BoltMood.SURPRISED -> {
                for (x in listOf(39f, 61f)) {
                    drawCircle(BoltCyan.copy(alpha = 0.35f), 8f, Offset(x, 43f))
                    drawCircle(BoltCyan, 5.4f, Offset(x, 43f))
                }
                drawCircle(BoltCyan, 3f, Offset(50f, 54f))
            }
            else -> {
                val eyeHeight = if (blink) 2.4f else 12f
                for (x in listOf(40f, 60f)) {
                    drawRoundRect(BoltCyan.copy(alpha = 0.3f), Offset(x - 6.5f, 44f - eyeHeight / 2 - 2.5f), Size(13f, eyeHeight + 5f), CornerRadius(6f))
                    drawRoundRect(BoltCyan, Offset(x - 4f, 44f - eyeHeight / 2), Size(8f, eyeHeight), CornerRadius(4f))
                }
                if (mood == BoltMood.TALK) {
                    val open = 2f + 4f * abs(sin(t * 13f))
                    drawRoundRect(BoltCyan, Offset(44f, 54f - open / 2), Size(12f, open), CornerRadius(open / 2))
                }
            }
        }
        drawOval(Color.White.copy(alpha = 0.85f), Offset(27f, 17f), Size(13f, 7f))
    }
}

private fun DrawScope.drawHand(center: Offset, line: Stroke) {
    drawCircle(Brush.radialGradient(listOf(Color.White, Color(0xFFB7C6E2)), center = center - Offset(3f, 3f), radius = 12f), 8.5f, center)
    drawCircle(K.Outline, 8.5f, center, style = line)
}

/**
 * A comic speech bubble with its tail pointing down-left, towards whoever is talking. White with a dark
 * rim, so it reads like a game and stays legible on any background.
 */
@Composable
fun SpeechBubble(modifier: Modifier = Modifier, tailAt: Float = 0.12f, content: @Composable BoxScope.() -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier
            .padding(bottom = 14.dp)
            .drawBehind {
                val tailX = size.width * tailAt
                val tail = Path().apply {
                    moveTo(tailX - 2.dp.toPx(), size.height - 2.dp.toPx())
                    lineTo(tailX - 10.dp.toPx(), size.height + 14.dp.toPx())
                    lineTo(tailX + 16.dp.toPx(), size.height - 2.dp.toPx())
                    close()
                }
                drawPath(tail, Color.White)
                drawPath(tail, K.Outline, style = Stroke(2.5.dp.toPx(), join = StrokeJoin.Round))
            }
            .drawWithContent {
                drawContent()
                // Opens the rim where the tail joins, so bubble and tail read as one shape.
                val tailX = size.width * tailAt
                drawLine(BubbleBottom, Offset(tailX - 0.5.dp.toPx(), size.height - 1.25.dp.toPx()), Offset(tailX + 13.5.dp.toPx(), size.height - 1.25.dp.toPx()), strokeWidth = 3.2.dp.toPx())
            }
            .border(2.5.dp, K.Outline, shape)
            .background(Brush.verticalGradient(listOf(Color.White, BubbleBottom)), shape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        content = content,
    )
}

/** The astronaut's helmet and face, filling a round portrait. */
@Composable
fun HeroPortrait(look: HeroLook, time: State<Float>, modifier: Modifier = Modifier) {
    val safe = look.safe()
    val suit = Color(HeroPalette.suits[safe.suit])
    val skin = Color(HeroPalette.skins[safe.skin])
    val hair = Color(HeroPalette.hairs[safe.hair])
    Canvas(modifier) {
        val side = min(size.width, size.height)
        // The helmet is 66 figure units across, centred 108 units above the feet.
        val scale = side / 88f
        val figure = Size(100f * scale, 150f * scale)
        // Put the figure's feet where the helmet ends up in the middle of the portrait.
        withTransform({ translate(size.width / 2 - 3f * scale - figure.width / 2, size.height / 2 + 108f * scale - figure.height) }) {
            drawFigureInto(figure, suit, skin, hair, safe.hairStyle, time.value)
        }
    }
}

private fun DrawScope.drawFigureInto(figure: Size, suit: Color, skin: Color, hair: Color, hairStyle: Int, t: Float) {
    val previous = drawContext.size
    drawContext.size = figure
    drawAstronaut(suit, skin, hair, hairStyle, HeroPose.IDLE, t, facingLeft = false)
    drawContext.size = previous
}

/**
 * A small inhabitant of a planet: a round, soft creature that blinks, breathes and jumps when it is
 * poked. [jump] runs from 0 to 1 over one hop.
 */
fun DrawScope.drawCritter(color: Color, kind: Int, t: Float, jump: Float) {
    val s = min(size.width, size.height) / 100f
    val hop = sin((jump.coerceIn(0f, 1f)) * PI.toFloat())
    val breath = sin(t * 2.6f + kind)
    val squashX = 1f + 0.04f * breath - 0.12f * hop
    val squashY = 1f - 0.04f * breath + 0.16f * hop
    val line = Stroke(width = 3.4f, join = StrokeJoin.Round, cap = StrokeCap.Round)
    val deep = lerp(color, Color.Black, 0.35f)
    val light = lerp(color, Color.White, 0.45f)
    withTransform({
        translate(size.width / 2, size.height)
        scale(s, s, pivot = Offset.Zero)
    }) {
        drawOval(Color.Black.copy(alpha = 0.25f * (1f - hop * 0.6f)), Offset(-30f, -6f), Size(60f, 11f))
        withTransform({
            translate(0f, -hop * 36f)
            scale(squashX, squashY, pivot = Offset(0f, 0f))
        }) {
            // Feet
            drawOval(deep, Offset(-24f, -9f), Size(18f, 10f))
            drawOval(deep, Offset(6f, -9f), Size(18f, 10f))
            val bodyTop = if (kind == 1) -86f else -66f
            val body = Path().apply {
                moveTo(-34f, -6f)
                cubicTo(-40f, bodyTop * 0.6f, -26f, bodyTop, 0f, bodyTop)
                cubicTo(26f, bodyTop, 40f, bodyTop * 0.6f, 34f, -6f)
                quadraticTo(0f, 2f, -34f, -6f)
                close()
            }
            if (kind == 2) {
                // Ears
                for (side in listOf(-1f, 1f)) {
                    val ear = Path().apply {
                        moveTo(side * 12f, bodyTop + 8f)
                        quadraticTo(side * 30f, bodyTop - 22f, side * 30f, bodyTop + 14f)
                        close()
                    }
                    drawPath(ear, color)
                    drawPath(ear, K.Outline, style = line)
                }
            } else {
                drawLine(K.Outline, Offset(0f, bodyTop + 2f), Offset(6f, bodyTop - 16f), strokeWidth = 5f, cap = StrokeCap.Round)
                drawLine(deep, Offset(0f, bodyTop + 2f), Offset(6f, bodyTop - 16f), strokeWidth = 2.4f, cap = StrokeCap.Round)
                val glow = 0.6f + 0.4f * sin(t * 3.3f + kind)
                drawCircle(K.Gold.copy(alpha = 0.4f * glow), 8f, Offset(6.5f, bodyTop - 18f))
                drawCircle(K.GoldTop, 4.2f, Offset(6.5f, bodyTop - 18f))
            }
            drawPath(body, Brush.radialGradient(listOf(light, color, deep), center = Offset(-12f, bodyTop * 0.7f), radius = 70f))
            drawPath(body, K.Outline, style = line)
            // Belly
            drawOval(light.copy(alpha = 0.55f), Offset(-16f, -30f), Size(32f, 22f))
            val blink = (t + kind * 1.3f) % 3.9f > 3.75f
            val happy = jump in 0.01f..0.99f
            val eyeY = bodyTop * 0.55f
            val eyes = if (kind == 1) listOf(0f) else listOf(-11f, 11f)
            for (x in eyes) {
                val r = if (kind == 1) 13f else 9f
                if (happy) {
                    drawArc(K.Outline, 200f, 140f, false, Offset(x - r * 0.8f, eyeY - r * 0.4f), Size(r * 1.6f, r * 1.2f), style = Stroke(4f, cap = StrokeCap.Round))
                } else if (blink) {
                    drawLine(K.Outline, Offset(x - r * 0.7f, eyeY), Offset(x + r * 0.7f, eyeY), strokeWidth = 3.4f, cap = StrokeCap.Round)
                } else {
                    drawCircle(Color.White, r, Offset(x, eyeY))
                    drawCircle(K.Outline, r, Offset(x, eyeY), style = Stroke(2.6f))
                    val look = sin(t * 0.7f + kind) * r * 0.25f
                    drawCircle(EyeInk, r * 0.55f, Offset(x + look, eyeY + 1f))
                    drawCircle(Color.White, r * 0.18f, Offset(x + look - r * 0.2f, eyeY - r * 0.2f))
                }
            }
            if (happy) {
                drawOval(Color(0xFF7A2432), Offset(-6f, eyeY + 10f), Size(12f, 9f))
            } else {
                drawArc(K.Outline, 20f, 140f, false, Offset(-6f, eyeY + 7f), Size(12f, 7f), style = Stroke(2.6f, cap = StrokeCap.Round))
            }
            drawOval(Color.White.copy(alpha = 0.5f), Offset(-22f, bodyTop * 0.82f), Size(12f, 7f))
        }
    }
}
