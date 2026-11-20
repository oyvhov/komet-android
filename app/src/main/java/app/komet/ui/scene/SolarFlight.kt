package app.komet.ui.scene

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.komet.ui.components.RocketArt
import app.komet.ui.theme.K
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2

/** The rocket and its exhaust share a curved path between the actual globe positions. */
@Composable
internal fun SolarFlight(
    from: () -> Offset?,
    to: () -> Offset?,
    progress: () -> Float,
    flying: Boolean,
    paint: RocketPaint,
    rocketHeight: Dp,
    modifier: Modifier,
) {
    Box(modifier) {
        if (flying) {
            Canvas(Modifier.matchParentSize()) {
                val start = from() ?: return@Canvas
                val end = to() ?: return@Canvas
                val p = progress()
                val lift = 42.dp.toPx()
                for (index in 1..14) {
                    val t = p - index * 0.016f
                    if (t < 0f) break
                    val fade = 1f - index / 15f
                    drawCircle(
                        if (index < 5) K.GoldTop.copy(alpha = 0.76f * fade)
                        else Color(0xFFFF863D).copy(alpha = 0.55f * fade),
                        radius = (1f + 2f * fade).dp.toPx(),
                        center = flightPoint(start, end, lift, t),
                    )
                }
            }
        }
        RocketArt(
            Modifier.size(rocketHeight * 0.68f, rocketHeight).graphicsLayer {
                val start = from()
                alpha = if (start == null) 0f else 1f
                if (start != null) {
                    val end = to()
                    val p = progress()
                    val lift = 42.dp.toPx()
                    val point = if (end == null) start else flightPoint(start, end, lift, p)
                    translationX = point.x - size.width / 2f
                    translationY = point.y - size.height / 2f
                    rotationZ = if (end == null) 24f else {
                        val ahead = flightPoint(start, end, lift, (p + 0.01f).coerceAtMost(1f))
                        val behind = flightPoint(start, end, lift, (p - 0.01f).coerceAtLeast(0f))
                        (atan2(ahead.x - behind.x, behind.y - ahead.y) * 180 / PI).toFloat()
                    }
                }
            },
            body = paint.body,
            accent = paint.accent,
            flame = flying,
            stripes = paint.stripes,
        )
    }
}

private fun flightPoint(start: Offset, end: Offset, lift: Float, t: Float): Offset {
    val delta = end - start
    val bend = if (abs(delta.x) < lift * 0.5f) Offset(-lift, 0f) else Offset(0f, -lift)
    return start + delta * t + bend * (2f * t * (1f - t))
}
