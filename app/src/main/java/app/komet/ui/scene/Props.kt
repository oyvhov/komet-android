package app.komet.ui.scene

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import app.komet.ui.theme.K
import kotlin.math.sin

/** Things that stand in a landscape. Each is drawn one unit tall with its foot at the origin. */
enum class PropKind { CRYSTALS, CACTUS, ROCK, DOME, LAMP, MUSHROOM, TREE, FLOWERS, DISH, ANTENNA, GRASS, PINE }

/**
 * The colours a prop is painted with. A silhouette ([flat]) paints every part in one hazy colour, without
 * outlines or lights, for hills far away.
 */
class PropInk(
    val leaf: Color,
    val bark: Color,
    val stone: Color,
    val accent: Color,
    val flat: Color? = null,
) {
    val detailed: Boolean get() = flat == null
    fun of(color: Color): Color = flat ?: color
}

private const val LINE = 0.028f

/**
 * Draws [kind] with its foot at ([x], [base]) in pixels and [height] pixels tall. [variant] varies
 * the shape a little so a forest is not a row of copies; [t] animates lights.
 */
fun DrawScope.drawProp(kind: PropKind, x: Float, base: Float, height: Float, ink: PropInk, variant: Int, t: Float, mirror: Boolean = false) {
    withTransform({
        translate(x, base)
        scale(if (mirror) -height else height, height, pivot = Offset.Zero)
    }) {
        when (kind) {
            PropKind.CRYSTALS -> crystals(ink, variant, t)
            PropKind.CACTUS -> cactus(ink, variant, t)
            PropKind.ROCK -> rock(ink, variant)
            PropKind.DOME -> dome(ink, variant, t)
            PropKind.LAMP -> lamp(ink, t + variant)
            PropKind.MUSHROOM -> mushroom(ink, variant, t)
            PropKind.TREE -> tree(ink, variant, t)
            PropKind.FLOWERS -> flowers(ink, variant, t)
            PropKind.DISH -> dish(ink, t + variant)
            PropKind.ANTENNA -> antenna(ink, t + variant)
            PropKind.GRASS -> grass(ink, variant, t)
            PropKind.PINE -> pine(ink, variant)
        }
    }
}

private fun DrawScope.outline(path: Path, ink: PropInk) {
    if (ink.detailed) drawPath(path, K.Outline, style = Stroke(LINE, join = StrokeJoin.Round, cap = StrokeCap.Round))
}

private fun DrawScope.glow(center: Offset, radius: Float, color: Color, strength: Float) {
    drawCircle(Brush.radialGradient(listOf(color.copy(alpha = strength), Color.Transparent), center, radius), radius, center)
}

private fun DrawScope.crystals(ink: PropInk, variant: Int, t: Float) {
    val shards = when (variant % 3) {
        0 -> listOf(Triple(-0.2f, 0.6f, -16f), Triple(0.03f, 1f, 4f), Triple(0.25f, 0.7f, 20f))
        1 -> listOf(Triple(-0.12f, 0.85f, -8f), Triple(0.15f, 0.55f, 16f))
        else -> listOf(Triple(-0.25f, 0.5f, -22f), Triple(-0.02f, 0.78f, -4f), Triple(0.2f, 1f, 10f), Triple(0.38f, 0.45f, 28f))
    }
    val color = ink.accent
    if (ink.detailed) glow(Offset(0f, -0.25f), 0.75f, color, 0.28f + 0.1f * sin(t * 1.7f + variant))
    for ((offset, tall, angle) in shards) {
        rotate(angle, Offset(offset, 0f)) {
            val w = 0.11f
            val shape = Path().apply {
                moveTo(offset - w, 0.05f)
                lineTo(offset - w, -tall * 0.72f)
                lineTo(offset, -tall)
                lineTo(offset + w, -tall * 0.72f)
                lineTo(offset + w, 0.05f)
                close()
            }
            if (ink.detailed) {
                drawPath(shape, lerp(color, Color.Black, 0.28f))
                val light = Path().apply {
                    moveTo(offset - w, 0.05f)
                    lineTo(offset - w, -tall * 0.72f)
                    lineTo(offset, -tall)
                    lineTo(offset, 0.05f)
                    close()
                }
                drawPath(light, Brush.verticalGradient(listOf(lerp(color, Color.White, 0.55f), color), startY = -tall, endY = 0f))
                drawLine(Color.White.copy(alpha = 0.7f), Offset(offset - w * 0.5f, -tall * 0.65f), Offset(offset - w * 0.5f, -tall * 0.25f), strokeWidth = 0.022f, cap = StrokeCap.Round)
                outline(shape, ink)
            } else {
                drawPath(shape, ink.of(color))
            }
        }
    }
}

private fun DrawScope.cactus(ink: PropInk, variant: Int, t: Float) {
    val body = ink.of(ink.leaf)
    val shade = ink.of(lerp(ink.leaf, Color.Black, 0.25f))
    val lean = if (variant % 2 == 0) 1f else -1f
    val trunk = Path().apply {
        addRoundRect(androidx.compose.ui.geometry.RoundRect(-0.1f, -0.9f, 0.1f, 0.03f, CornerRadius(0.1f)))
    }
    val armA = Path().apply {
        moveTo(-0.06f * lean, -0.42f)
        lineTo(-0.3f * lean, -0.42f)
        quadraticTo(-0.36f * lean, -0.42f, -0.36f * lean, -0.5f)
        lineTo(-0.36f * lean, -0.72f)
    }
    val armB = Path().apply {
        moveTo(0.06f * lean, -0.58f)
        lineTo(0.26f * lean, -0.58f)
        quadraticTo(0.32f * lean, -0.58f, 0.32f * lean, -0.66f)
        lineTo(0.32f * lean, -0.82f)
    }
    if (ink.detailed) {
        drawPath(armA, K.Outline, style = Stroke(0.16f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(armB, K.Outline, style = Stroke(0.14f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
    drawPath(armA, shade, style = Stroke(0.11f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(armB, shade, style = Stroke(0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    if (ink.detailed) {
        drawPath(trunk, Brush.horizontalGradient(listOf(lerp(ink.leaf, Color.White, 0.25f), ink.leaf, shade), startX = -0.1f, endX = 0.1f))
        drawLine(Color.White.copy(alpha = 0.3f), Offset(-0.03f, -0.8f), Offset(-0.03f, -0.1f), strokeWidth = 0.02f, cap = StrokeCap.Round)
        outline(trunk, ink)
        val pulse = 0.6f + 0.4f * sin(t * 2.2f + variant)
        for (tip in listOf(Offset(0f, -0.92f), Offset(-0.36f * lean, -0.76f), Offset(0.32f * lean, -0.86f))) {
            glow(tip, 0.14f, ink.accent, 0.5f * pulse)
            drawCircle(ink.accent, 0.045f, tip)
        }
    } else {
        drawPath(trunk, body)
    }
}

private fun DrawScope.rock(ink: PropInk, variant: Int) {
    val squash = 0.8f + (variant % 3) * 0.15f
    val shape = Path().apply {
        moveTo(-0.55f * squash, 0.02f)
        cubicTo(-0.62f * squash, -0.45f, -0.25f * squash, -0.82f, 0.05f, -0.76f)
        cubicTo(0.38f * squash, -0.72f, 0.66f * squash, -0.4f, 0.58f * squash, 0.02f)
        close()
    }
    if (ink.detailed) {
        drawPath(shape, Brush.linearGradient(listOf(lerp(ink.stone, Color.White, 0.25f), ink.stone, lerp(ink.stone, Color.Black, 0.35f)), start = Offset(-0.4f, -0.7f), end = Offset(0.5f, 0f)))
        drawArc(Color.White.copy(alpha = 0.35f), 200f, 60f, false, Offset(-0.42f * squash, -0.66f), Size(0.5f * squash, 0.5f), style = Stroke(0.05f, cap = StrokeCap.Round))
        drawCircle(lerp(ink.stone, Color.Black, 0.3f), 0.06f, Offset(0.2f * squash, -0.3f))
        outline(shape, ink)
    } else {
        drawPath(shape, ink.of(ink.stone))
    }
}

private fun DrawScope.dome(ink: PropInk, variant: Int, t: Float) {
    val shell = lerp(ink.stone, Color.White, 0.45f)
    val shape = Path().apply {
        moveTo(-0.62f, 0.02f)
        lineTo(-0.62f, -0.06f)
        cubicTo(-0.62f, -0.44f, -0.34f, -0.7f, 0f, -0.7f)
        cubicTo(0.34f, -0.7f, 0.62f, -0.44f, 0.62f, -0.06f)
        lineTo(0.62f, 0.02f)
        close()
    }
    if (!ink.detailed) {
        drawPath(shape, ink.of(shell))
        drawLine(ink.of(shell), Offset(0.3f, -0.58f), Offset(0.42f, -0.9f), strokeWidth = 0.04f)
        return
    }
    drawLine(K.Outline, Offset(0.3f, -0.58f), Offset(0.42f, -0.9f), strokeWidth = 0.06f, cap = StrokeCap.Round)
    drawLine(shell, Offset(0.3f, -0.58f), Offset(0.42f, -0.9f), strokeWidth = 0.025f, cap = StrokeCap.Round)
    val blink = if (sin(t * 3f + variant) > 0.2f) 1f else 0.35f
    glow(Offset(0.42f, -0.92f), 0.14f, Color(0xFFFF5A64), 0.6f * blink)
    drawCircle(Color(0xFFFF5A64), 0.04f, Offset(0.42f, -0.92f))
    drawPath(shape, Brush.linearGradient(listOf(Color.White, shell, lerp(shell, Color.Black, 0.3f)), start = Offset(-0.4f, -0.7f), end = Offset(0.6f, 0f)))
    drawRect(lerp(ink.stone, Color.Black, 0.2f), Offset(-0.62f, -0.1f), Size(1.24f, 0.12f))
    // Stripes in the planet's accent
    drawArc(ink.accent.copy(alpha = 0.9f), 180f, 180f, false, Offset(-0.5f, -0.6f), Size(1f, 1.1f), style = Stroke(0.045f))
    // Door and a warm window
    drawRoundRect(Color(0xFF1A1440), Offset(-0.36f, -0.36f), Size(0.22f, 0.36f), CornerRadius(0.11f, 0.11f))
    val light = 0.85f + 0.15f * sin(t * 1.3f + variant)
    glow(Offset(0.22f, -0.33f), 0.3f, K.Gold, 0.45f * light)
    drawCircle(Color(0xFFFFE27A), 0.11f, Offset(0.22f, -0.33f))
    drawCircle(K.Outline, 0.11f, Offset(0.22f, -0.33f), style = Stroke(LINE))
    drawLine(Color(0xFFE39400), Offset(0.22f, -0.44f), Offset(0.22f, -0.22f), strokeWidth = 0.02f)
    drawArc(Color.White.copy(alpha = 0.6f), 200f, 50f, false, Offset(-0.48f, -0.62f), Size(0.7f, 0.9f), style = Stroke(0.04f, cap = StrokeCap.Round))
    outline(shape, ink)
}

private fun DrawScope.lamp(ink: PropInk, t: Float) {
    val pole = ink.of(lerp(ink.bark, Color.Black, 0.2f))
    if (ink.detailed) drawLine(K.Outline, Offset(0f, 0f), Offset(0f, -0.82f), strokeWidth = 0.08f, cap = StrokeCap.Round)
    drawLine(pole, Offset(0f, 0f), Offset(0f, -0.82f), strokeWidth = 0.045f, cap = StrokeCap.Round)
    if (!ink.detailed) {
        drawCircle(pole, 0.1f, Offset(0f, -0.88f))
        return
    }
    val flicker = 0.8f + 0.2f * sin(t * 5f)
    glow(Offset(0f, -0.88f), 0.45f, ink.accent, 0.5f * flicker)
    drawCircle(lerp(ink.accent, Color.White, 0.6f), 0.1f, Offset(0f, -0.88f))
    drawCircle(K.Outline, 0.1f, Offset(0f, -0.88f), style = Stroke(LINE))
    drawRoundRect(pole, Offset(-0.07f, -0.8f), Size(0.14f, 0.05f), CornerRadius(0.02f))
}

private fun DrawScope.mushroom(ink: PropInk, variant: Int, t: Float) {
    val stemColor = lerp(ink.leaf, Color.White, 0.65f)
    val lean = ((variant % 3) - 1) * 0.06f
    val stem = Path().apply {
        moveTo(-0.08f, 0.02f)
        quadraticTo(-0.02f + lean, -0.4f, -0.06f + lean * 2, -0.68f)
        lineTo(0.06f + lean * 2, -0.68f)
        quadraticTo(0.03f + lean, -0.4f, 0.09f, 0.02f)
        close()
    }
    val capCenter = Offset(lean * 2, -0.68f)
    val cap = Path().apply {
        moveTo(capCenter.x - 0.44f, capCenter.y + 0.02f)
        cubicTo(capCenter.x - 0.44f, capCenter.y - 0.36f, capCenter.x + 0.44f, capCenter.y - 0.36f, capCenter.x + 0.44f, capCenter.y + 0.02f)
        quadraticTo(capCenter.x, capCenter.y + 0.1f, capCenter.x - 0.44f, capCenter.y + 0.02f)
        close()
    }
    if (!ink.detailed) {
        drawPath(stem, ink.of(stemColor))
        drawPath(cap, ink.of(ink.leaf))
        return
    }
    val pulse = 0.7f + 0.3f * sin(t * 1.6f + variant * 1.3f)
    glow(capCenter + Offset(0f, 0.05f), 0.7f, ink.accent, 0.28f * pulse)
    drawPath(stem, Brush.horizontalGradient(listOf(Color.White, stemColor, lerp(stemColor, Color.Black, 0.25f)), startX = -0.1f, endX = 0.1f))
    outline(stem, ink)
    drawPath(cap, Brush.verticalGradient(listOf(lerp(ink.leaf, Color.White, 0.3f), ink.leaf, lerp(ink.leaf, Color.Black, 0.3f)), startY = capCenter.y - 0.3f, endY = capCenter.y + 0.06f))
    for ((dx, dy, r) in listOf(Triple(-0.2f, -0.12f, 0.055f), Triple(0.08f, -0.2f, 0.045f), Triple(0.25f, -0.06f, 0.04f))) {
        drawCircle(lerp(ink.accent, Color.White, 0.5f).copy(alpha = 0.6f + 0.4f * pulse), r, capCenter + Offset(dx, dy))
    }
    drawArc(Color.White.copy(alpha = 0.45f), 205f, 45f, false, Offset(capCenter.x - 0.36f, capCenter.y - 0.26f), Size(0.5f, 0.5f), style = Stroke(0.035f, cap = StrokeCap.Round))
    outline(cap, ink)
}

private fun DrawScope.tree(ink: PropInk, variant: Int, t: Float) {
    val sway = sin(t * 0.9f + variant) * 0.015f
    val trunk = Path().apply {
        moveTo(-0.07f, 0.02f)
        quadraticTo(-0.03f, -0.3f, -0.045f + sway, -0.5f)
        lineTo(0.045f + sway, -0.5f)
        quadraticTo(0.04f, -0.3f, 0.08f, 0.02f)
        close()
    }
    val blobs = when (variant % 3) {
        0 -> listOf(Triple(-0.2f, -0.6f, 0.22f), Triple(0.18f, -0.64f, 0.24f), Triple(0f, -0.83f, 0.26f))
        1 -> listOf(Triple(-0.14f, -0.64f, 0.24f), Triple(0.14f, -0.7f, 0.22f), Triple(0.02f, -0.88f, 0.2f))
        else -> listOf(Triple(-0.22f, -0.58f, 0.2f), Triple(0.22f, -0.6f, 0.2f), Triple(-0.06f, -0.78f, 0.24f), Triple(0.12f, -0.92f, 0.16f))
    }
    val bark = ink.of(ink.bark)
    if (ink.detailed) outline(trunk, ink)
    drawPath(trunk, bark)
    if (ink.detailed) {
        for ((dx, dy, r) in blobs) drawCircle(K.Outline, r + LINE / 2, Offset(dx + sway * 3, dy))
        for ((dx, dy, r) in blobs) {
            val c = Offset(dx + sway * 3, dy)
            drawCircle(Brush.radialGradient(listOf(lerp(ink.leaf, Color.White, 0.3f), ink.leaf, lerp(ink.leaf, Color.Black, 0.3f)), center = c + Offset(-r * 0.4f, -r * 0.5f), radius = r * 1.7f), r, c)
        }
        drawCircle(Color.White.copy(alpha = 0.25f), 0.06f, Offset(blobs.last().first - 0.08f + sway * 3, blobs.last().second - 0.08f))
        for ((i, b) in blobs.withIndex()) {
            if (i % 2 == 0) drawCircle(ink.accent, 0.035f, Offset(b.first + b.third * 0.3f + sway * 3, b.second + b.third * 0.2f))
        }
    } else {
        for ((dx, dy, r) in blobs) drawCircle(ink.of(ink.leaf), r, Offset(dx, dy))
    }
}

private fun DrawScope.pine(ink: PropInk, variant: Int) {
    val tiers = 3 + variant % 2
    val color = ink.of(ink.leaf)
    drawRect(ink.of(ink.bark), Offset(-0.04f, -0.2f), Size(0.08f, 0.22f))
    for (i in 0 until tiers) {
        val bottom = -0.14f - i * (0.7f / tiers)
        val width = 0.36f - i * 0.07f
        val tier = Path().apply {
            moveTo(-width, bottom)
            lineTo(0f, bottom - 0.42f)
            lineTo(width, bottom)
            close()
        }
        drawPath(tier, if (ink.detailed) Brush.horizontalGradient(listOf(lerp(ink.leaf, Color.White, 0.2f), ink.leaf, lerp(ink.leaf, Color.Black, 0.35f)), startX = -width, endX = width) else Brush.linearGradient(listOf(color, color)))
        outline(tier, ink)
    }
}

private fun DrawScope.flowers(ink: PropInk, variant: Int, t: Float) {
    val stems = listOf(Triple(-0.22f, 0.6f, -10f), Triple(0.02f, 0.9f, 3f), Triple(0.24f, 0.7f, 14f))
    val petals = listOf(ink.accent, Color(0xFFFF7D8C), Color.White)
    for ((i, stem) in stems.withIndex()) {
        val (dx, tall, angle) = stem
        val sway = sin(t * 1.4f + i + variant) * 3f
        rotate(angle + sway, Offset(dx, 0f)) {
            drawLine(ink.of(lerp(ink.leaf, Color.Black, 0.2f)), Offset(dx, 0f), Offset(dx, -tall), strokeWidth = 0.035f, cap = StrokeCap.Round)
            val head = Offset(dx, -tall)
            if (ink.detailed) {
                val color = petals[(i + variant) % petals.size]
                for (p in 0 until 5) {
                    val a = p * 72f
                    rotate(a, head) { drawCircle(color, 0.06f, head + Offset(0f, -0.07f)) }
                }
                drawCircle(K.Gold, 0.045f, head)
                drawCircle(K.Outline, 0.045f, head, style = Stroke(0.015f))
            } else {
                drawCircle(ink.of(ink.leaf), 0.1f, head)
            }
        }
    }
}

private fun DrawScope.dish(ink: PropInk, t: Float) {
    val metal = ink.of(lerp(ink.stone, Color.White, 0.5f))
    val legs = ink.of(lerp(ink.stone, Color.Black, 0.2f))
    if (ink.detailed) {
        drawLine(K.Outline, Offset(-0.25f, 0f), Offset(0f, -0.42f), strokeWidth = 0.08f, cap = StrokeCap.Round)
        drawLine(K.Outline, Offset(0.25f, 0f), Offset(0f, -0.42f), strokeWidth = 0.08f, cap = StrokeCap.Round)
    }
    drawLine(legs, Offset(-0.25f, 0f), Offset(0f, -0.42f), strokeWidth = 0.045f, cap = StrokeCap.Round)
    drawLine(legs, Offset(0.25f, 0f), Offset(0f, -0.42f), strokeWidth = 0.045f, cap = StrokeCap.Round)
    val turn = sin(t * 0.4f) * 12f
    rotate(-30f + turn, Offset(0f, -0.5f)) {
        val bowl = Path().apply {
            moveTo(-0.42f, -0.5f)
            quadraticTo(0f, -0.1f, 0.42f, -0.5f)
            close()
        }
        drawPath(bowl, if (ink.detailed) Brush.verticalGradient(listOf(Color.White, metal), startY = -0.5f, endY = -0.2f) else Brush.linearGradient(listOf(metal, metal)))
        outline(bowl, ink)
        drawLine(ink.of(legs), Offset(0f, -0.36f), Offset(0f, -0.72f), strokeWidth = 0.03f)
        if (ink.detailed) {
            val blink = if (sin(t * 2.5f) > 0f) 0.9f else 0.3f
            glow(Offset(0f, -0.74f), 0.14f, ink.accent, 0.7f * blink)
            drawCircle(ink.accent, 0.045f, Offset(0f, -0.74f))
        }
    }
}

private fun DrawScope.antenna(ink: PropInk, t: Float) {
    val metal = ink.of(lerp(ink.stone, Color.Black, 0.1f))
    val frame = Path().apply {
        moveTo(-0.2f, 0f)
        lineTo(0f, -0.92f)
        lineTo(0.2f, 0f)
        moveTo(-0.15f, -0.22f)
        lineTo(0.13f, -0.3f)
        moveTo(-0.1f, -0.46f)
        lineTo(0.09f, -0.54f)
        moveTo(-0.06f, -0.68f)
        lineTo(0.05f, -0.74f)
    }
    if (ink.detailed) drawPath(frame, K.Outline, style = Stroke(0.065f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(frame, metal, style = Stroke(0.03f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    if (ink.detailed) {
        val blink = if (sin(t * 3.4f) > 0.3f) 1f else 0.25f
        glow(Offset(0f, -0.95f), 0.2f, Color(0xFFFF5A64), 0.7f * blink)
        drawCircle(Color(0xFFFF5A64), 0.05f, Offset(0f, -0.95f))
    }
}

private fun DrawScope.grass(ink: PropInk, variant: Int, t: Float) {
    val color = ink.of(lerp(ink.leaf, Color.White, 0.12f))
    val blades = 5 + variant % 3
    for (i in 0 until blades) {
        val dx = -0.3f + i * (0.6f / (blades - 1))
        val tall = 0.55f + ((i * 37 + variant * 11) % 10) * 0.045f
        val sway = sin(t * 1.8f + i * 0.7f + variant) * 0.05f
        val blade = Path().apply {
            moveTo(dx - 0.05f, 0.02f)
            quadraticTo(dx + sway, -tall * 0.6f, dx + sway * 2 + (dx * 0.4f), -tall)
            quadraticTo(dx + 0.02f, -tall * 0.5f, dx + 0.05f, 0.02f)
            close()
        }
        drawPath(blade, color)
    }
}
