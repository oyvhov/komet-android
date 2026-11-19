package app.komet.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import app.komet.domain.ClockTime
import app.komet.domain.clockSweep
import app.komet.ui.components.fixedSp
import app.komet.ui.components.str
import app.komet.ui.theme.K
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

internal val ClockGold = K.GoldTop
internal val ClockMint = K.ExploreTop
private val HourHand = Color(0xFFAE641F)
private val MinuteHand = Color(0xFF176B68)
private const val FACE_RADIUS = 0.405f
private const val HOUR_LENGTH = 0.49f
private const val MINUTE_LENGTH = 0.63f

@Composable
internal fun WorkshopDial(
    time: ClockTime,
    modifier: Modifier = Modifier,
    minuteStep: Int = 1,
    onChange: ((ClockTime) -> Unit)? = null,
) {
    val currentTime by rememberUpdatedState(time)
    val changeTime by rememberUpdatedState(onChange)
    val clue = time.dialClue().str()
    val input = if (onChange == null) Modifier else Modifier.pointerInput(minuteStep) {
        awaitEachGesture {
            val down = awaitFirstDown()
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = minOf(size.width, size.height) * FACE_RADIUS
            val vector = (down.position - center) / radius
            if (vector.getDistance() !in 0.18f..1.08f) return@awaitEachGesture

            val start = currentTime
            val hourDistance = (point(start.hourAngle, HOUR_LENGTH) - vector).getDistance()
            val minuteDistance = (point(start.minuteAngle, MINUTE_LENGTH) - vector).getDistance()
            val movesHour = hourDistance <= 0.22f && hourDistance < minuteDistance
            var previous = angle(vector)
            val base = if (!movesHour && minuteDistance > 0.22f) start.moveMinuteTo(previous, minuteStep) else start
            var sweep = 0f
            var lastTime = start
            if (base != lastTime) {
                changeTime?.invoke(base)
                lastTime = base
            }
            down.consume()

            while (true) {
                val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                if (!change.pressed || change.isConsumed) break
                val nextVector = (change.position - center) / radius
                if (nextVector.getDistance() < 0.18f) {
                    change.consume()
                    continue
                }
                val current = angle(nextVector)
                sweep += clockSweep(previous, current)
                previous = current
                val time = if (movesHour) base + (sweep / 30f).roundToInt() * 60
                    else base + (sweep / (6f * minuteStep)).roundToInt() * minuteStep
                if (lastTime != time) {
                    changeTime?.invoke(time)
                    lastTime = time
                }
                change.consume()
            }
        }
    }
    val measurer = rememberTextMeasurer()
    val unit = fixedSp(1.dp)
    Canvas(modifier.then(input).semantics { contentDescription = clue }) {
        val r = size.minDimension * FACE_RADIUS
        val c = center
        drawCircle(
            Brush.radialGradient(listOf(K.ReadingTop.copy(alpha = 0.20f), Color.Transparent), c, r * 1.23f),
            radius = r * 1.23f,
        )
        drawCircle(K.Outline, r * 1.1f, c + Offset(0f, r * 0.055f))
        drawCircle(
            Brush.linearGradient(
                0f to K.GoldTop,
                0.24f to Color(0xFFAA8462),
                0.52f to Color(0xFF4C4649),
                0.84f to Color(0xFF867165),
                1f to K.SurfaceLow,
                start = c - Offset(r, r),
                end = c + Offset(r, r),
            ),
            r * 1.105f,
        )
        drawCircle(K.Outline, r * 1.055f)
        val highlight = c - Offset(r * 0.25f, r * 0.37f)
        drawCircle(
            Brush.radialGradient(listOf(Color(0xFFFFFDF7), K.Paper, K.PaperShade), highlight, r * 1.5f),
            r * 1.01f,
        )
        drawCircle(K.Ink.copy(alpha = 0.18f), r * 0.985f, style = Stroke(r * 0.009f))
        for (i in 0 until 60) {
            val degrees = i * 6f
            val quarter = i % 15 == 0
            val major = i % 5 == 0
            drawLine(
                when {
                    quarter -> MinuteHand
                    major -> K.Ink
                    else -> K.InkMuted
                },
                c + point(degrees, r * if (major) 0.87f else 0.92f),
                c + point(degrees, r * 0.965f),
                strokeWidth = r * if (major) 0.023f else 0.009f,
                cap = StrokeCap.Round,
            )
        }
        for (hour in 1..12) {
            val cardinal = hour % 3 == 0
            val label = measurer.measure(
                hour.toString(),
                TextStyle(
                    color = if (cardinal) K.Ink else K.InkMuted,
                    fontSize = unit * (r / density * if (cardinal) 0.20f else 0.157f),
                    fontWeight = if (cardinal) FontWeight.Black else FontWeight.Bold,
                ),
            )
            val at = c + point(hour * 30f, r * 0.745f)
            drawText(label, topLeft = at - Offset(label.size.width / 2f, label.size.height / 2f))
        }
        val hourTip = c + point(time.hourAngle, r * HOUR_LENGTH)
        val minuteTip = c + point(time.minuteAngle, r * MINUTE_LENGTH)
        val shadow = Offset(r * 0.012f, r * 0.023f)
        drawLine(K.Ink.copy(alpha = 0.16f), c + shadow, hourTip + shadow, r * 0.11f, StrokeCap.Round)
        drawLine(K.Ink.copy(alpha = 0.16f), c + shadow, minuteTip + shadow, r * 0.063f, StrokeCap.Round)
        drawLine(HourHand, c, hourTip, r * 0.096f, StrokeCap.Round)
        drawLine(MinuteHand, c, minuteTip, r * 0.056f, StrokeCap.Round)
        if (onChange != null) {
            drawCircle(HourHand, r * 0.058f, hourTip)
            drawCircle(K.GoldTop, r * 0.024f, hourTip)
            drawCircle(MinuteHand, r * 0.052f, minuteTip)
            drawCircle(K.ExploreTop, r * 0.022f, minuteTip)
        }
        drawCircle(K.Ink, r * 0.078f, c)
        drawCircle(K.GoldTop, r * 0.055f, c)
        drawCircle(HourHand, r * 0.024f, c)
    }
}

private fun point(angle: Float, distance: Float): Offset {
    val radians = angle * PI / 180
    return Offset(sin(radians).toFloat() * distance, -cos(radians).toFloat() * distance)
}

private fun angle(vector: Offset): Float =
    ((atan2(vector.x, -vector.y) * 180 / PI).toFloat() + 360f) % 360f
