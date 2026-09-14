package app.komet.ui.scene

import androidx.compose.runtime.Immutable
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
import androidx.compose.ui.graphics.lerp
import app.komet.domain.ShopSlot
import app.komet.ui.theme.K
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** What the astronaut wears from the shop, one id per slot, or null for the plain suit. */
@Immutable
data class Gear(
    val helmet: String? = null,
    val visor: String? = null,
    val pattern: String? = null,
    val pack: String? = null,
    val antenna: String? = null,
    val badge: String? = null,
) {
    companion object {
        val None = Gear()

        fun of(equipped: Map<ShopSlot, String>): Gear = Gear(
            helmet = equipped[ShopSlot.HELMET],
            visor = equipped[ShopSlot.VISOR],
            pattern = equipped[ShopSlot.PATTERN],
            pack = equipped[ShopSlot.PACK],
            antenna = equipped[ShopSlot.ANTENNA],
            badge = equipped[ShopSlot.BADGE],
        )
    }
}

/** The three shades of the suit: the side facing us, its shadow and the limbs further away. */
internal class SuitTones(val front: Color, val shade: Color, val back: Color)

internal fun suitTones(gear: Gear): SuitTones =
    if (gear.pattern == "pattern_galaxy") {
        SuitTones(Color(0xFF5646B8), Color(0xFF261C66), Color(0xFF3A2F8A))
    } else {
        SuitTones(SuitWhite, SuitShade, SuitBack)
    }

private val Flame = listOf(Color.White, Color(0xFFFFE27A), Color(0xFFFF8A2E), Color.Transparent)
private val ScarfRed = Color(0xFFE02A50)
private val Metal = listOf(Color(0xFFF1F4FB), Color(0xFFB6C0D9), Color(0xFF7E8AAD))

internal fun starPath(center: Offset, outer: Float, inner: Float = outer * 0.45f): Path = Path().apply {
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) outer else inner
        val a = -PI / 2 + i * PI / 5
        val x = center.x + (r * cos(a)).toFloat()
        val y = center.y + (r * sin(a)).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

private fun heartPath(center: Offset, size: Float): Path = Path().apply {
    moveTo(center.x, center.y + size * 0.9f)
    cubicTo(center.x - size * 1.4f, center.y - size * 0.1f, center.x - size * 0.7f, center.y - size * 1.2f, center.x, center.y - size * 0.45f)
    cubicTo(center.x + size * 0.7f, center.y - size * 1.2f, center.x + size * 1.4f, center.y - size * 0.1f, center.x, center.y + size * 0.9f)
    close()
}

/** Things behind the whole figure: a cape or wings. */
internal fun DrawScope.drawPackBehind(gear: Gear, suit: Color, t: Float, line: Stroke) {
    when (gear.pack) {
        "pack_cape" -> {
            val w1 = sin(t * 3f) * 3f
            val w2 = sin(t * 3f + 1.2f) * 4f
            val cape = Path().apply {
                moveTo(-18f, -86f)
                lineTo(8f, -86f)
                cubicTo(6f, -60f, -2f, -40f, -6f, -22f + w1)
                quadraticTo(-24f, -16f + w2, -46f, -24f + w1)
                cubicTo(-42f, -50f, -32f, -72f, -18f, -86f)
                close()
            }
            drawPath(cape, Brush.verticalGradient(listOf(lerp(suit, Color.White, 0.15f), suit, lerp(suit, Color.Black, 0.35f)), startY = -86f, endY = -20f))
            drawPath(cape, K.Outline, style = line)
        }
        "pack_wings" -> {
            rotate(sin(t * 3f) * 6f, Offset(-14f, -74f)) {
                val upper = Path().apply {
                    moveTo(-12f, -80f)
                    quadraticTo(-40f, -122f, -64f, -120f)
                    quadraticTo(-52f, -102f, -60f, -93f)
                    quadraticTo(-40f, -84f, -14f, -66f)
                    close()
                }
                val lower = Path().apply {
                    moveTo(-12f, -70f)
                    quadraticTo(-36f, -98f, -58f, -96f)
                    quadraticTo(-48f, -83f, -54f, -74f)
                    quadraticTo(-36f, -66f, -14f, -58f)
                    close()
                }
                for (blade in listOf(lower, upper)) {
                    drawPath(blade, Brush.linearGradient(Metal, start = Offset(-12f, -70f), end = Offset(-60f, -110f)))
                    drawPath(blade, K.Outline, style = line)
                }
                drawLine(suit, Offset(-18f, -76f), Offset(-54f, -112f), strokeWidth = 2.6f, cap = StrokeCap.Round)
                drawLine(suit, Offset(-18f, -65f), Offset(-50f, -90f), strokeWidth = 2.4f, cap = StrokeCap.Round)
            }
        }
    }
}

/** The thing on the back itself: the plain backpack or a jetpack with flames. */
internal fun DrawScope.drawPack(gear: Gear, suit: Color, suitDeep: Color, pose: HeroPose, t: Float, line: Stroke) {
    if (gear.pack != "pack_jet") {
        if (gear.pack == "pack_wings" || gear.pack == "pack_cape") {
            // A small harness holds the wings or the cape.
            drawRoundRect(suitDeep, Offset(-27f, -84f), Size(14f, 26f), CornerRadius(6f))
            drawRoundRect(K.Outline, Offset(-27f, -84f), Size(14f, 26f), CornerRadius(6f), style = line)
            return
        }
        drawRoundRect(suitDeep, Offset(-33f, -86f), Size(22f, 40f), CornerRadius(8f))
        drawRoundRect(K.Outline, Offset(-33f, -86f), Size(22f, 40f), CornerRadius(8f), style = line)
        drawLine(Color.White.copy(alpha = 0.25f), Offset(-28f, -80f), Offset(-28f, -54f), strokeWidth = 3f, cap = StrokeCap.Round)
        return
    }
    val flying = pose == HeroPose.WALK || pose == HeroPose.CHEER
    val flicker = 0.8f + 0.2f * sin(t * 29f)
    // The tanks sit well back, so they show beside the body and not only behind it.
    for (cx in listOf(-40f, -27f)) {
        val length = (if (flying) 18f else 9f) * flicker
        val flame = Path().apply {
            moveTo(cx - 4.5f, -41f)
            quadraticTo(cx, -41f + length * 1.3f, cx + 4.5f, -41f)
            close()
        }
        drawPath(flame, Brush.verticalGradient(Flame, startY = -41f, endY = -41f + length * 1.3f))
        drawRoundRect(Color(0xFF6D7699), Offset(cx - 4.5f, -47f), Size(9f, 6f), CornerRadius(2f))
        drawRoundRect(K.Outline, Offset(cx - 4.5f, -47f), Size(9f, 6f), CornerRadius(2f), style = Stroke(2f))
    }
    for (x in listOf(-46.5f, -33.5f)) {
        drawRoundRect(Brush.horizontalGradient(Metal, startX = x, endX = x + 13f), Offset(x, -92f), Size(13f, 46f), CornerRadius(6.5f))
        drawRoundRect(K.Race, Offset(x, -94f), Size(13f, 10f), CornerRadius(5f))
        drawRect(suit, Offset(x + 0.5f, -64f), Size(12f, 4f))
        drawRoundRect(K.Outline, Offset(x, -94f), Size(13f, 48f), CornerRadius(6.5f), style = line)
    }
    // Straps to the shoulders
    drawLine(K.Outline, Offset(-24f, -84f), Offset(-14f, -80f), strokeWidth = 5f, cap = StrokeCap.Round)
}

/** A pattern on an arm or a leg, drawn inside the limb's own turned frame. */
internal fun DrawScope.drawLimbPattern(pattern: String?, accent: Color, pivot: Offset, length: Float, width: Float, leg: Boolean) {
    when (pattern) {
        "pattern_stripes" -> for (f in listOf(0.34f, 0.58f)) {
            drawRect(accent, Offset(pivot.x - width / 2, pivot.y + length * f), Size(width, 3.4f))
        }
        "pattern_stars" -> drawPath(starPath(Offset(pivot.x, pivot.y + length * 0.48f), 3.6f), K.Gold)
        "pattern_flames" -> if (leg) {
            val bottom = pivot.y + length - 3f
            val left = pivot.x - width / 2
            val right = pivot.x + width / 2
            val flame = Path().apply {
                moveTo(left, bottom)
                quadraticTo(left + 1f, bottom - 12f, left + 4f, bottom - 9f)
                quadraticTo(left + 6f, bottom - 19f, pivot.x + 1f, bottom - 8f)
                quadraticTo(right - 2f, bottom - 15f, right, bottom - 3f)
                lineTo(right, bottom)
                close()
            }
            drawPath(flame, Brush.verticalGradient(listOf(K.GoldTop, Color(0xFFFF8A2E), Color(0xFFE02A50)), startY = bottom - 19f, endY = bottom))
        }
        "pattern_galaxy" -> {
            drawCircle(Color.White.copy(alpha = 0.8f), 0.9f, Offset(pivot.x - 2f, pivot.y + length * 0.3f))
            drawCircle(Color.White.copy(alpha = 0.6f), 0.7f, Offset(pivot.x + 2.5f, pivot.y + length * 0.62f))
        }
    }
}

/** A pattern on the body, over the suit and under the rim. */
internal fun DrawScope.drawTorsoPattern(gear: Gear, suit: Color) {
    when (gear.pattern) {
        "pattern_stripes" -> {
            drawRect(suit, Offset(-21f, -52f), Size(43f, 4.5f))
            drawRect(suit, Offset(-21f, -76f), Size(18f, 3f))
        }
        "pattern_stars" -> {
            drawPath(starPath(Offset(-12f, -70f), 4.4f), K.Gold)
            drawPath(starPath(Offset(-9f, -53f), 3.2f), Color.White)
            drawPath(starPath(Offset(17f, -45f), 2.8f), K.Gold)
        }
        "pattern_lightning" -> {
            val bolt = Path().apply {
                moveTo(-14f, -78f)
                lineTo(-5f, -66f)
                lineTo(-10f, -64f)
                lineTo(-2f, -50f)
                lineTo(-16f, -64f)
                lineTo(-11f, -66f)
                close()
            }
            drawPath(bolt, Brush.verticalGradient(listOf(K.GoldTop, K.Gold), startY = -78f, endY = -50f))
            drawPath(bolt, K.Outline, style = Stroke(1.8f, join = StrokeJoin.Round))
        }
        "pattern_galaxy" -> {
            for ((x, y, r) in listOf(Triple(-15f, -74f, 1.2f), Triple(-8f, -58f, 0.9f), Triple(18f, -72f, 1f), Triple(14f, -40f, 1.1f), Triple(-16f, -44f, 0.8f))) {
                drawCircle(Color.White.copy(alpha = 0.85f), r, Offset(x, y))
            }
            drawPath(starPath(Offset(-11f, -66f), 2.6f), Color(0xFFFFE27A))
            drawCircle(Color(0xFFB38FFF).copy(alpha = 0.35f), 7f, Offset(12f, -44f))
        }
    }
}

/** A scarf's tail blows out behind the figure. */
internal fun DrawScope.drawBadgeBehind(gear: Gear, t: Float, line: Stroke) {
    if (gear.badge != "badge_scarf") return
    val wave = sin(t * 4f) * 3f
    val tail = Path().apply {
        moveTo(-18f, -80f)
        quadraticTo(-32f, -78f + wave, -46f, -68f + wave)
        lineTo(-42f, -60f + wave)
        quadraticTo(-30f, -70f + wave, -16f, -72f)
        close()
    }
    drawPath(tail, ScarfRed)
    drawPath(tail, K.Outline, style = line)
}

/** A medal on the chest. */
internal fun DrawScope.drawBadgeChest(gear: Gear) {
    if (gear.badge != "badge_medal") return
    val ribbon = Path().apply {
        moveTo(-15f, -79f)
        lineTo(-9f, -64f)
        lineTo(-3f, -79f)
        lineTo(-6f, -79f)
        lineTo(-9f, -70f)
        lineTo(-12f, -79f)
        close()
    }
    drawPath(ribbon, Color(0xFF1684E6))
    drawCircle(Brush.radialGradient(listOf(K.GoldTop, K.Gold, K.GoldDeep), center = Offset(-10.5f, -62f), radius = 8f), 5.6f, Offset(-9f, -60f))
    drawCircle(K.Outline, 5.6f, Offset(-9f, -60f), style = Stroke(1.6f))
    drawPath(starPath(Offset(-9f, -60f), 2.8f), Color.White)
}

/** Things worn at the collar: the scarf itself or a bow tie. */
internal fun DrawScope.drawBadgeCollar(gear: Gear, line: Stroke) {
    when (gear.badge) {
        // Both sit low enough to show under the helmet, which covers the collar itself.
        "badge_scarf" -> {
            drawRoundRect(ScarfRed, Offset(-25f, -83f), Size(52f, 12f), CornerRadius(6f))
            for (x in listOf(-14f, 0f, 14f)) drawRect(Color.White.copy(alpha = 0.85f), Offset(x, -83f), Size(3.5f, 12f))
            drawRoundRect(K.Outline, Offset(-25f, -83f), Size(52f, 12f), CornerRadius(6f), style = line)
        }
        "badge_bowtie" -> {
            // Left of the front arm, which would otherwise hide it.
            val knot = Offset(-5f, -70f)
            val left = Path().apply { moveTo(knot.x, knot.y); lineTo(knot.x - 11f, knot.y - 7f); lineTo(knot.x - 11f, knot.y + 7f); close() }
            val right = Path().apply { moveTo(knot.x, knot.y); lineTo(knot.x + 11f, knot.y - 7f); lineTo(knot.x + 11f, knot.y + 7f); close() }
            for (wing in listOf(left, right)) {
                drawPath(wing, ScarfRed)
                drawPath(wing, K.Outline, style = Stroke(2f, join = StrokeJoin.Round))
            }
            drawCircle(Color(0xFFB8183C), 3f, knot)
            drawCircle(K.Outline, 3f, knot, style = Stroke(1.6f))
        }
    }
}

/** The antenna on the helmet, or whatever the child chose instead. */
internal fun DrawScope.drawAntenna(gear: Gear, suit: Color, t: Float) {
    val pulse = 0.55f + 0.45f * sin(t * 3.1f)
    fun stalk(from: Offset, to: Offset) {
        drawLine(K.Outline, from, to, strokeWidth = 4.2f, cap = StrokeCap.Round)
        drawLine(SuitShade, from, to, strokeWidth = 1.8f, cap = StrokeCap.Round)
    }
    fun ball(center: Offset, color: Color) {
        drawCircle(color.copy(alpha = 0.35f * pulse), 7.5f, center)
        drawCircle(color, 3.8f, center)
        drawCircle(K.Outline, 3.8f, center, style = Stroke(1.8f))
    }
    val tip = Offset(-19.5f, -148f)
    when (gear.antenna) {
        "antenna_star" -> {
            stalk(Offset(-12f, -134f), Offset(-19f, -145f))
            drawCircle(K.Gold.copy(alpha = 0.35f * pulse), 9f, tip)
            val star = starPath(Offset(tip.x, tip.y - 1f), 6.5f)
            drawPath(star, Brush.verticalGradient(listOf(K.GoldTop, K.Gold), startY = tip.y - 8f, endY = tip.y + 6f))
            drawPath(star, K.Outline, style = Stroke(1.6f, join = StrokeJoin.Round))
        }
        "antenna_heart" -> {
            stalk(Offset(-12f, -134f), Offset(-19f, -145f))
            drawCircle(Color(0xFFFF5A8A).copy(alpha = 0.35f * pulse), 9f, tip)
            val heart = heartPath(Offset(tip.x, tip.y - 1f), 5f)
            drawPath(heart, Color(0xFFFF5A8A))
            drawPath(heart, K.Outline, style = Stroke(1.6f, join = StrokeJoin.Round))
        }
        "antenna_double" -> {
            stalk(Offset(-12f, -134f), Offset(-19f, -147f))
            ball(tip, K.Gold)
            stalk(Offset(14f, -135f), Offset(22f, -147f))
            ball(Offset(22.5f, -148f), suit)
        }
        "antenna_propeller" -> {
            stalk(Offset(3f, -138f), Offset(3f, -150f))
            val spin = cos(t * 14f)
            val hub = Offset(3f, -152f)
            drawOval(suit, Offset(hub.x - 14f * abs(spin), hub.y - 2.6f), Size(28f * abs(spin) + 0.1f, 5.2f))
            drawOval(K.Outline, Offset(hub.x - 14f * abs(spin), hub.y - 2.6f), Size(28f * abs(spin) + 0.1f, 5.2f), style = Stroke(1.6f))
            drawCircle(K.Gold, 2.6f, hub)
            drawCircle(K.Outline, 2.6f, hub, style = Stroke(1.4f))
        }
        else -> {
            stalk(Offset(-12f, -134f), Offset(-19f, -147f))
            ball(tip, K.Gold)
        }
    }
}

/** Fins on the back of a dragon-crest helmet, drawn before the shell so they stand out behind it. */
internal fun DrawScope.drawHelmetBehind(gear: Gear, suit: Color, line: Stroke) {
    if (gear.helmet != "helmet_fins") return
    val center = Offset(3f, -108f)
    for ((degrees, height) in listOf(250f to 16f, 222f to 13f, 195f to 10f)) {
        val a = degrees * PI.toFloat() / 180f
        val spread = 10f * PI.toFloat() / 180f
        val tipAngle = a - spread * 0.6f
        val fin = Path().apply {
            moveTo(center.x + cos(a - spread) * 30f, center.y + sin(a - spread) * 30f)
            lineTo(center.x + cos(tipAngle) * (33f + height), center.y + sin(tipAngle) * (33f + height))
            lineTo(center.x + cos(a + spread) * 30f, center.y + sin(a + spread) * 30f)
            close()
        }
        drawPath(fin, Brush.linearGradient(listOf(lerp(suit, Color.White, 0.3f), suit, lerp(suit, Color.Black, 0.3f)), start = center, end = center + Offset(cos(a), sin(a)) * 50f))
        drawPath(fin, K.Outline, style = line)
    }
}

internal fun helmetBrush(gear: Gear): Brush = when (gear.helmet) {
    "helmet_gold" -> Brush.radialGradient(listOf(K.GoldTop, K.Gold, K.GoldDeep), center = Offset(-8f, -122f), radius = 48f)
    "helmet_crystal" -> Brush.radialGradient(listOf(Color(0xFFF2FCFF), Color(0xFF9FDDF7), Color(0xFF3E8FD1)), center = Offset(-8f, -122f), radius = 48f)
    else -> Brush.radialGradient(listOf(Color.White, SuitWhite, SuitShade), center = Offset(-8f, -122f), radius = 46f)
}

/** Details on top of the shell: a racing stripe or crystal facets. */
internal fun DrawScope.drawHelmetDetails(gear: Gear, suit: Color) {
    val center = Offset(3f, -108f)
    when (gear.helmet) {
        "helmet_stripe" -> {
            val arc = Rect(center, 27f)
            drawArc(K.Outline, 196f, 148f, false, arc.topLeft, arc.size, style = Stroke(11f))
            drawArc(suit, 197f, 146f, false, arc.topLeft, arc.size, style = Stroke(7.5f))
            drawArc(Color.White.copy(alpha = 0.35f), 200f, 60f, false, arc.topLeft, arc.size, style = Stroke(2f))
        }
        "helmet_crystal" -> clipPath(Path().apply { addOval(Rect(center, 33f)) }) {
            val facet = Color.White.copy(alpha = 0.45f)
            drawLine(facet, Offset(-24f, -126f), Offset(0f, -142f), strokeWidth = 1.6f)
            drawLine(facet, Offset(-30f, -104f), Offset(-6f, -140f), strokeWidth = 1.2f)
            drawLine(facet, Offset(22f, -138f), Offset(38f, -112f), strokeWidth = 1.4f)
            drawLine(facet, Offset(-28f, -90f), Offset(-12f, -78f), strokeWidth = 1.2f)
        }
    }
}

internal fun visorBrush(gear: Gear): Brush = when (gear.visor) {
    "visor_mint" -> Brush.verticalGradient(listOf(Color(0xFF2FA385), Color(0xFF0B3D33)), startY = -126f, endY = -84f)
    "visor_red" -> Brush.verticalGradient(listOf(Color(0xFFB8283F), Color(0xFF3D0710)), startY = -126f, endY = -84f)
    "visor_gold" -> Brush.verticalGradient(listOf(Color(0xFFFFD86B), Color(0xFFB36A00)), startY = -126f, endY = -84f)
    "visor_rainbow" -> Brush.horizontalGradient(
        listOf(Color(0xFF5634B0), Color(0xFF2A62B8), Color(0xFF1E8E48), Color(0xFFB88A10), Color(0xFFB8462A)),
        startX = -12f,
        endX = 36f,
    )
    else -> Brush.verticalGradient(listOf(VisorTop, VisorBottom), startY = -126f, endY = -84f)
}

/** A coloured rim just inside the glass, so a new visor is easy to spot even though the face fills the middle. */
internal fun DrawScope.drawVisorRim(gear: Gear, glass: Rect) {
    val brush = when (gear.visor) {
        "visor_mint" -> Brush.verticalGradient(listOf(Color(0xFF8CF7D8), Color(0xFF1E9E7E)), startY = glass.top, endY = glass.bottom)
        "visor_red" -> Brush.verticalGradient(listOf(Color(0xFFFF8F99), Color(0xFFC8243C)), startY = glass.top, endY = glass.bottom)
        "visor_gold" -> Brush.verticalGradient(listOf(K.GoldTop, K.GoldDeep), startY = glass.top, endY = glass.bottom)
        "visor_rainbow" -> Brush.sweepGradient(
            listOf(Color(0xFFFF5A64), Color(0xFFFFB02E), Color(0xFFFFE14D), Color(0xFF4CD97B), Color(0xFF3FA9F5), Color(0xFF9B6BFF), Color(0xFFFF5A64)),
            center = glass.center,
        )
        else -> return
    }
    val inset = 2.5f
    drawOval(brush, Offset(glass.left + inset, glass.top + inset), Size(glass.width - inset * 2, glass.height - inset * 2), style = Stroke(5f))
}

/** A light tint over the face, so the visor's colour reads while the face stays visible. */
internal fun DrawScope.drawVisorTint(gear: Gear) {
    val tint = when (gear.visor) {
        "visor_mint" -> Color(0xFF3EF0C0).copy(alpha = 0.12f)
        "visor_red" -> Color(0xFFFF5A64).copy(alpha = 0.12f)
        "visor_gold" -> K.Gold.copy(alpha = 0.2f)
        "visor_rainbow" -> Color(0xFFB38FFF).copy(alpha = 0.14f)
        else -> return
    }
    drawRect(tint, Offset(-14f, -128f), Size(52f, 48f))
}

/** Sunglasses sit on the outside of the visor. */
internal fun DrawScope.drawShades(gear: Gear) {
    if (gear.badge != "badge_shades") return
    for (x in listOf(1.5f, 16f)) {
        drawRoundRect(Color(0xFF15102E), Offset(x, -107f), Size(10.5f, 8.5f), CornerRadius(3.5f))
        drawRoundRect(K.Outline, Offset(x, -107f), Size(10.5f, 8.5f), CornerRadius(3.5f), style = Stroke(1.8f))
        drawLine(Color.White.copy(alpha = 0.7f), Offset(x + 2.5f, -105f), Offset(x + 5.5f, -105f), strokeWidth = 1.4f, cap = StrokeCap.Round)
    }
    drawLine(K.Outline, Offset(12f, -103.5f), Offset(16f, -103.5f), strokeWidth = 1.8f)
}

/** Bolt's paint job. */
internal class BoltPaint(val light: Color, val mid: Color, val dark: Color, val trim: Color)

internal fun boltPaint(id: String?): BoltPaint = when (id) {
    "bolt_pink" -> BoltPaint(Color.White, Color(0xFFFFD1E3), Color(0xFFD9779F), Color(0xFFE0457B))
    "bolt_green" -> BoltPaint(Color(0xFFF2FFF7), Color(0xFFBDF2D2), Color(0xFF4FA878), Color(0xFF1F9D55))
    "bolt_gold" -> BoltPaint(Color(0xFFFFF6D6), Color(0xFFFFD34E), Color(0xFFC27400), Color(0xFFE02A50))
    "bolt_night" -> BoltPaint(Color(0xFF7A82D6), Color(0xFF3B418F), Color(0xFF1A1D4E), Color(0xFF9B6BFF))
    else -> BoltPaint(Color.White, Color(0xFFDDE6F5), Color(0xFF93A5C9), BoltTeal)
}

/** The rocket's paint job: hull, nose and fins, and whether it has rainbow bands. */
data class RocketPaint(val body: Color, val accent: Color, val stripes: Boolean = false)

fun rocketPaint(id: String?): RocketPaint = when (id) {
    "rocket_blue" -> RocketPaint(Color(0xFFE3F1FF), Color(0xFF1684E6))
    "rocket_green" -> RocketPaint(Color(0xFFE6FBEA), Color(0xFF23A049))
    "rocket_gold" -> RocketPaint(Color(0xFFFFE9A8), Color(0xFF6C3FF0))
    "rocket_stripes" -> RocketPaint(Color.White, Color(0xFF6C3FF0), stripes = true)
    else -> RocketPaint(Color.White, K.Race)
}
