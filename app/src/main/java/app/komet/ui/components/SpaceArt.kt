package app.komet.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import app.komet.domain.asEmoji
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.komet.domain.CardArt
import app.komet.domain.PlanetLook
import app.komet.domain.SpecialArt
import app.komet.ui.theme.K
import app.komet.ui.theme.LocalMotion
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private class StarDot(val x: Float, val y: Float, val radius: Float, val alpha: Float, val phase: Float)

/** Deep-space backdrop: gradient, two soft nebulae and a slowly twinkling star field. */
@Composable
fun SpaceBackground(
    modifier: Modifier = Modifier,
    twinkle: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val motion = LocalMotion.current && twinkle
    val stars = remember {
        val random = Random(42)
        List(110) {
            StarDot(random.nextFloat(), random.nextFloat(), 0.5f + random.nextFloat() * 1.3f, 0.25f + random.nextFloat() * 0.65f, random.nextFloat())
        }
    }
    val transition = rememberInfiniteTransition(label = "stars")
    val time = transition.animateFloat(0f, 1f, infiniteRepeatable(tween(9000, easing = LinearEasing)), label = "twinkle")
    Box(
        modifier = modifier
            .background(Brush.verticalGradient(listOf(K.SpaceTop, K.SpaceBottom)))
            .drawBehind {
                val w = size.width
                val h = size.height
                drawCircle(
                    Brush.radialGradient(listOf(K.Nebula.copy(alpha = 0.45f), Color.Transparent), center = Offset(w * 0.9f, h * 0.08f), radius = w * 0.75f),
                    radius = w * 0.75f,
                    center = Offset(w * 0.9f, h * 0.08f),
                )
                drawCircle(
                    Brush.radialGradient(listOf(Color(0xFF155E8C).copy(alpha = 0.28f), Color.Transparent), center = Offset(w * 0.05f, h * 0.78f), radius = w * 0.85f),
                    radius = w * 0.85f,
                    center = Offset(w * 0.05f, h * 0.78f),
                )
                val phase = if (motion) time.value else 0.25f
                for (star in stars) {
                    val shimmer = 0.55f + 0.45f * abs(sin(((phase + star.phase) * 2 * PI).toFloat()))
                    drawCircle(
                        color = Color.White.copy(alpha = (star.alpha * shimmer).coerceIn(0f, 1f)),
                        radius = star.radius.dp.toPx(),
                        center = Offset(star.x * w, star.y * h),
                    )
                }
            },
        content = content,
    )
}

@Composable
fun PlanetArt(look: PlanetLook, modifier: Modifier = Modifier, glow: Boolean = true, dim: Boolean = false) {
    Canvas(modifier) { drawPlanet(look, glow, dim) }
}

fun DrawScope.drawPlanet(look: PlanetLook, glow: Boolean = true, dim: Boolean = false) {
    val base = Color(look.base)
    val light = Color(look.light)
    val dark = Color(look.dark)
    val c = center
    val r = size.minDimension * (if (look.ring) 0.31f else 0.4f)
    if (glow) {
        drawCircle(Brush.radialGradient(listOf(base.copy(alpha = 0.35f), Color.Transparent), center = c, radius = r * 1.7f), radius = r * 1.7f, center = c)
    }
    val ringTopLeft = Offset(c.x - r * 1.8f, c.y - r * 0.5f)
    val ringSize = Size(r * 3.6f, r * 1.0f)
    if (look.ring) {
        rotate(-16f, c) {
            drawArc(light.copy(alpha = 0.55f), 180f, 180f, false, ringTopLeft, ringSize, style = Stroke(width = r * 0.17f))
        }
    }
    drawCircle(
        brush = Brush.radialGradient(listOf(light, base, dark), center = Offset(c.x - r * 0.35f, c.y - r * 0.4f), radius = r * 1.6f),
        radius = r,
        center = c,
    )
    val body = Path().apply { addOval(Rect(c, r)) }
    clipPath(body) {
        if (look.bands) {
            for (i in 0 until 6) {
                val y = c.y - r + (i + 0.5f) * (2 * r / 6)
                drawRect(dark.copy(alpha = if (i % 2 == 0) 0.2f else 0.08f), Offset(c.x - r, y - r * 0.08f), Size(2 * r, r * 0.16f))
            }
        }
        if (look.craters) {
            val craters = listOf(Triple(-0.35f, 0.25f, 0.18f), Triple(0.32f, -0.22f, 0.13f), Triple(0.12f, 0.52f, 0.1f), Triple(-0.12f, -0.48f, 0.08f), Triple(0.55f, 0.2f, 0.07f))
            for ((dx, dy, cr) in craters) {
                drawCircle(dark.copy(alpha = 0.3f), radius = r * cr, center = Offset(c.x + dx * r, c.y + dy * r))
                drawCircle(light.copy(alpha = 0.18f), radius = r * cr * 0.55f, center = Offset(c.x + dx * r - r * cr * 0.25f, c.y + dy * r - r * cr * 0.25f))
            }
        }
        drawCircle(
            Brush.radialGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.38f)), center = Offset(c.x - r * 0.45f, c.y - r * 0.5f), radius = r * 2.1f),
            radius = r,
            center = c,
        )
    }
    if (look.ring) {
        rotate(-16f, c) {
            drawArc(light.copy(alpha = 0.85f), 0f, 180f, false, ringTopLeft, ringSize, style = Stroke(width = r * 0.17f))
        }
    }
    if (dim) drawCircle(Color(0xFF0A0F2E).copy(alpha = 0.55f), radius = r * 1.02f, center = c)
}

@Composable
fun SpecialArtView(kind: SpecialArt, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val c = center
        val r = size.minDimension * 0.4f
        when (kind) {
            SpecialArt.SUN -> {
                drawCircle(Brush.radialGradient(listOf(Color(0x88FFB02E), Color.Transparent), c, r * 1.8f), r * 1.8f, c)
                for (i in 0 until 12) {
                    val angle = i * PI / 6
                    drawLine(
                        Color(0xFFFFC94A).copy(alpha = 0.7f),
                        Offset(c.x + (cos(angle) * r * 1.15f).toFloat(), c.y + (sin(angle) * r * 1.15f).toFloat()),
                        Offset(c.x + (cos(angle) * r * 1.45f).toFloat(), c.y + (sin(angle) * r * 1.45f).toFloat()),
                        strokeWidth = r * 0.1f,
                        cap = StrokeCap.Round,
                    )
                }
                drawCircle(Brush.radialGradient(listOf(Color(0xFFFFF4C2), Color(0xFFFFC93C), Color(0xFFFF8A1F)), Offset(c.x - r * 0.3f, c.y - r * 0.3f), r * 1.5f), r, c)
            }
            SpecialArt.EARTH -> {
                drawCircle(Brush.radialGradient(listOf(Color(0x553D8BFF), Color.Transparent), c, r * 1.7f), r * 1.7f, c)
                drawCircle(Brush.radialGradient(listOf(Color(0xFF7CC4FF), Color(0xFF2C7BE5), Color(0xFF123E8C)), Offset(c.x - r * 0.35f, c.y - r * 0.4f), r * 1.6f), r, c)
                clipPath(Path().apply { addOval(Rect(c, r)) }) {
                    val land = Color(0xFF3FBF6A)
                    drawOval(land, Offset(c.x - r * 0.7f, c.y - r * 0.55f), Size(r * 0.8f, r * 0.55f))
                    drawOval(land, Offset(c.x - r * 0.35f, c.y - r * 0.1f), Size(r * 0.45f, r * 0.8f))
                    drawOval(land, Offset(c.x + r * 0.2f, c.y - r * 0.35f), Size(r * 0.7f, r * 0.45f))
                    drawOval(land, Offset(c.x + r * 0.35f, c.y + r * 0.25f), Size(r * 0.4f, r * 0.35f))
                    drawOval(Color.White.copy(alpha = 0.85f), Offset(c.x - r * 0.5f, c.y - r * 1.05f), Size(r, r * 0.3f))
                    drawCircle(Brush.radialGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f)), Offset(c.x - r * 0.45f, c.y - r * 0.5f), r * 2.1f), r, c)
                }
            }
            SpecialArt.JUPITER -> {
                drawPlanet(PlanetLook(0xFFE3B98A, 0xFFFBE3C6, 0xFF9C6A43, bands = true))
                val pr = size.minDimension * 0.4f
                drawOval(Color(0xFFC8553D), Offset(c.x + pr * 0.05f, c.y + pr * 0.18f), Size(pr * 0.42f, pr * 0.24f))
            }
            SpecialArt.BLACK_HOLE -> {
                drawCircle(Brush.radialGradient(listOf(Color(0xAAFF8A3D), Color(0x33FF6E9C), Color.Transparent), c, r * 1.6f), r * 1.6f, c)
                rotate(-12f, c) {
                    drawOval(Color(0xFFFFB547), Offset(c.x - r * 1.35f, c.y - r * 0.3f), Size(r * 2.7f, r * 0.6f), style = Stroke(r * 0.16f))
                }
                drawCircle(Color.Black, r * 0.62f, c)
                drawCircle(Color(0xFFFFD27A).copy(alpha = 0.8f), r * 0.66f, c, style = Stroke(r * 0.05f))
            }
            SpecialArt.AURORA -> {
                drawRect(Brush.verticalGradient(listOf(Color(0xFF071233), Color(0xFF123056))))
                val curtains = listOf(Color(0xFF3EF0A0), Color(0xFF3ED6C8), Color(0xFF8A7CFF))
                curtains.forEachIndexed { index, color ->
                    val path = Path()
                    val top = size.height * (0.18f + index * 0.08f)
                    path.moveTo(0f, top)
                    for (x in 0..20) {
                        val px = size.width * x / 20f
                        path.lineTo(px, top + sin(x * 0.7f + index) * size.height * 0.06f)
                    }
                    path.lineTo(size.width, size.height * 0.75f)
                    path.lineTo(0f, size.height * 0.75f)
                    path.close()
                    drawPath(path, Brush.verticalGradient(listOf(color.copy(alpha = 0.55f), Color.Transparent), startY = top, endY = size.height * 0.72f))
                }
                val mountains = Path().apply {
                    moveTo(0f, size.height)
                    lineTo(0f, size.height * 0.78f)
                    lineTo(size.width * 0.25f, size.height * 0.62f)
                    lineTo(size.width * 0.45f, size.height * 0.76f)
                    lineTo(size.width * 0.7f, size.height * 0.58f)
                    lineTo(size.width, size.height * 0.74f)
                    lineTo(size.width, size.height)
                    close()
                }
                drawPath(mountains, Color(0xFF050A1F))
            }
            SpecialArt.BIG_DIPPER -> {
                val points = listOf(0.12f to 0.62f, 0.3f to 0.56f, 0.45f to 0.6f, 0.58f to 0.52f, 0.62f to 0.32f, 0.86f to 0.3f, 0.88f to 0.52f)
                val offsets = points.map { (x, y) -> Offset(size.width * x, size.height * y) }
                val lines = listOf(0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5, 5 to 6, 6 to 3)
                for ((a, b) in lines) drawLine(Color.White.copy(alpha = 0.35f), offsets[a], offsets[b], strokeWidth = 2.dp.toPx())
                for (point in offsets) {
                    drawCircle(Color(0x66FFE9A8), 9.dp.toPx(), point)
                    drawCircle(Color.White, 3.5.dp.toPx(), point)
                }
            }
            SpecialArt.MOON_LANDING -> {
                drawCircle(Color(0x33FFFFFF), r * 0.25f, Offset(size.width * 0.8f, size.height * 0.2f))
                drawCircle(Brush.radialGradient(listOf(Color(0xFF7CC4FF), Color(0xFF2C7BE5)), Offset(size.width * 0.8f, size.height * 0.2f), r * 0.25f), r * 0.2f, Offset(size.width * 0.8f, size.height * 0.2f))
                drawOval(Color(0xFFCFCFD6), Offset(-size.width * 0.2f, size.height * 0.62f), Size(size.width * 1.4f, size.height * 0.8f))
                drawOval(Color(0xFFAFAFB8), Offset(size.width * 0.12f, size.height * 0.74f), Size(size.width * 0.22f, size.height * 0.06f))
                drawOval(Color(0xFFAFAFB8), Offset(size.width * 0.6f, size.height * 0.84f), Size(size.width * 0.18f, size.height * 0.05f))
                val poleX = size.width * 0.52f
                drawLine(Color(0xFFE6E6EE), Offset(poleX, size.height * 0.68f), Offset(poleX, size.height * 0.28f), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                drawRect(Color(0xFFFF6B6B), Offset(poleX, size.height * 0.29f), Size(size.width * 0.22f, size.height * 0.14f))
                drawRect(Color.White, Offset(poleX, size.height * 0.34f), Size(size.width * 0.22f, size.height * 0.035f))
            }
            SpecialArt.ASTEROID -> {
                val rock = Path().apply {
                    val random = Random(5)
                    for (i in 0 until 11) {
                        val angle = i * 2 * PI / 11
                        val radius = r * (0.75f + random.nextFloat() * 0.3f)
                        val x = c.x + (cos(angle) * radius).toFloat()
                        val y = c.y + (sin(angle) * radius * 0.8f).toFloat()
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }
                drawPath(rock, Brush.radialGradient(listOf(Color(0xFFB9ADA0), Color(0xFF7B6F64), Color(0xFF4A423B)), Offset(c.x - r * 0.3f, c.y - r * 0.3f), r * 1.4f))
                clipPath(rock) {
                    drawCircle(Color(0x55352E28), r * 0.2f, Offset(c.x - r * 0.3f, c.y + r * 0.1f))
                    drawCircle(Color(0x55352E28), r * 0.13f, Offset(c.x + r * 0.35f, c.y - r * 0.2f))
                    drawCircle(Color(0x55352E28), r * 0.09f, Offset(c.x + r * 0.1f, c.y + r * 0.4f))
                }
            }
        }
    }
}

/**
 * Art for a space card. [emojiSize] is explicit because the tile rows on Heim measure intrinsic
 * heights, and a constraints-reading layout would crash there.
 */
@Composable
fun SpaceCardArt(art: CardArt, modifier: Modifier = Modifier, emojiSize: Dp = 44.dp) {
    when (art) {
        is CardArt.Planet -> PlanetArt(art.look, modifier)
        is CardArt.Special -> SpecialArtView(art.kind, modifier)
        is CardArt.Emoji -> Box(modifier, contentAlignment = Alignment.Center) {
            Text(art.emoji, fontSize = fixedSp(emojiSize), lineHeight = fixedSp(emojiSize * 1.15f))
        }
    }
}

/** A small rocket, drawn pointing up. [flame] animates the exhaust. */
@Composable
fun RocketArt(modifier: Modifier = Modifier, body: Color = Color(0xFFF4F6FF), accent: Color = K.Race, flame: Boolean = true) {
    val motion = LocalMotion.current && flame
    val transition = rememberInfiniteTransition(label = "flame")
    val flicker = transition.animateFloat(0.8f, 1.15f, infiniteRepeatable(tween(140), RepeatMode.Reverse), label = "flicker")
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        if (flame) {
            val scale = if (motion) flicker.value else 1f
            val flamePath = Path().apply {
                moveTo(w * 0.36f, h * 0.76f)
                quadraticTo(w * 0.5f, h * (0.76f + 0.26f * scale), w * 0.64f, h * 0.76f)
                close()
            }
            drawPath(flamePath, Brush.verticalGradient(listOf(Color(0xFFFFE27A), Color(0xFFFF7A2E), Color.Transparent), startY = h * 0.74f, endY = h * (0.76f + 0.26f * scale)))
        }
        val finLeft = Path().apply {
            moveTo(w * 0.32f, h * 0.48f); lineTo(w * 0.1f, h * 0.72f); lineTo(w * 0.12f, h * 0.8f); lineTo(w * 0.34f, h * 0.7f); close()
        }
        val finRight = Path().apply {
            moveTo(w * 0.68f, h * 0.48f); lineTo(w * 0.9f, h * 0.72f); lineTo(w * 0.88f, h * 0.8f); lineTo(w * 0.66f, h * 0.7f); close()
        }
        drawPath(finLeft, accent)
        drawPath(finRight, accent)
        val hull = Path().apply {
            moveTo(w * 0.5f, 0f)
            cubicTo(w * 0.8f, h * 0.16f, w * 0.74f, h * 0.55f, w * 0.66f, h * 0.78f)
            lineTo(w * 0.34f, h * 0.78f)
            cubicTo(w * 0.26f, h * 0.55f, w * 0.2f, h * 0.16f, w * 0.5f, 0f)
            close()
        }
        drawPath(hull, Brush.horizontalGradient(listOf(body, body.copy(red = body.red * 0.8f, green = body.green * 0.82f, blue = body.blue * 0.92f))))
        val nose = Path().apply {
            moveTo(w * 0.5f, 0f)
            cubicTo(w * 0.64f, h * 0.07f, w * 0.7f, h * 0.14f, w * 0.72f, h * 0.2f)
            lineTo(w * 0.28f, h * 0.2f)
            cubicTo(w * 0.3f, h * 0.14f, w * 0.36f, h * 0.07f, w * 0.5f, 0f)
            close()
        }
        drawPath(nose, accent)
        drawCircle(Color(0xFF2A3A8F), w * 0.13f, Offset(w * 0.5f, h * 0.38f))
        drawCircle(Brush.radialGradient(listOf(Color(0xFF9FE7FF), Color(0xFF3D8BFF)), Offset(w * 0.46f, h * 0.35f), w * 0.12f), w * 0.095f, Offset(w * 0.5f, h * 0.38f))
    }
}

/** One star, filled gold or as a faint outline. */
@Composable
fun StarGlyph(filled: Boolean, modifier: Modifier = Modifier, color: Color = K.Gold) {
    Canvas(modifier) {
        val path = Path()
        val cx = size.width / 2
        val cy = size.height * 0.53f
        val outer = size.minDimension * 0.48f
        val inner = outer * 0.45f
        for (i in 0 until 10) {
            val radius = if (i % 2 == 0) outer else inner
            val angle = -PI / 2 + i * PI / 5
            val x = cx + (radius * cos(angle)).toFloat()
            val y = cy + (radius * sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        if (filled) {
            drawPath(path, color)
            drawPath(path, Color.White.copy(alpha = 0.25f), style = Stroke(width = size.minDimension * 0.04f))
        } else {
            drawPath(path, Color.White.copy(alpha = 0.12f))
            drawPath(path, Color.White.copy(alpha = 0.3f), style = Stroke(width = size.minDimension * 0.05f))
        }
    }
}

private val avatarEmoji = listOf("🚀", "🤖", "👾", "🦖", "🐉", "🦊", "🐼", "🦁", "🐙", "🦈", "🛸", "🐱")
private val avatarColors = listOf(
    Color(0xFFFF6E9C) to Color(0xFF7053D8),
    Color(0xFF4DD9E8) to Color(0xFF2C5BD8),
    Color(0xFFE6F57A) to Color(0xFF3FA35B),
    Color(0xFF3ED67F) to Color(0xFF1E7A8C),
    Color(0xFFFF8A3D) to Color(0xFFC53F6C),
    Color(0xFFFFB547) to Color(0xFFCC5A1B),
    Color(0xFF9FE7FF) to Color(0xFF3D6BD8),
    Color(0xFFFFD34E) to Color(0xFFD9731B),
    Color(0xFFFF8FB1) to Color(0xFF8C3FD8),
    Color(0xFF6FA8FF) to Color(0xFF1E3E9C),
    Color(0xFF7FD6C2) to Color(0xFF2E6B8C),
    Color(0xFFFFC2A8) to Color(0xFFD8583D),
)

val avatarCount: Int get() = avatarEmoji.size

@Composable
fun Avatar(index: Int, modifier: Modifier = Modifier, size: Dp = 56.dp) {
    val safe = index.mod(avatarEmoji.size)
    val (top, bottom) = avatarColors[safe]
    Box(
        modifier = modifier
            .size(size)
            .background(Brush.linearGradient(listOf(top, bottom)), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(avatarEmoji[safe].asEmoji(), fontSize = (size.value * 0.5f).sp)
    }
}

private class ConfettiBit(val dx: Float, val dy: Float, val spin: Float, val color: Color, val size: Float, val delay: Float)

/** A single burst of confetti whenever [trigger] changes. Skipped entirely when animations are off. */
@Composable
fun ConfettiBurst(trigger: Any, modifier: Modifier = Modifier) {
    if (!LocalMotion.current) return
    val progress = remember { Animatable(1f) }
    val bits = remember(trigger) {
        val random = Random(trigger.hashCode())
        val colors = listOf(K.Gold, K.Race, K.Reading, K.Math, K.Good, K.Cards, Color.White)
        List(70) {
            val angle = random.nextFloat() * PI.toFloat() - PI.toFloat()
            val speed = 0.35f + random.nextFloat() * 0.75f
            ConfettiBit(cos(angle) * speed, sin(angle) * speed - 0.2f, random.nextFloat() * 720f, colors[random.nextInt(colors.size)], 5f + random.nextFloat() * 6f, random.nextFloat() * 0.15f)
        }
    }
    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(1700, easing = LinearEasing))
    }
    Canvas(modifier) {
        val p = progress.value
        if (p >= 1f) return@Canvas
        val origin = Offset(size.width / 2, size.height * 0.35f)
        for (bit in bits) {
            val t = ((p - bit.delay) / (1f - bit.delay)).coerceIn(0f, 1f)
            if (t <= 0f) continue
            val x = origin.x + bit.dx * size.width * 0.6f * t
            val y = origin.y + bit.dy * size.height * 0.5f * t + size.height * 0.9f * t * t
            val alpha = (1f - t).coerceIn(0f, 1f)
            rotate(bit.spin * t, Offset(x, y)) {
                drawRect(bit.color.copy(alpha = alpha), Offset(x - bit.size.dp.toPx() / 2, y - bit.size.dp.toPx() / 4), Size(bit.size.dp.toPx(), bit.size.dp.toPx() / 2))
            }
        }
    }
}
