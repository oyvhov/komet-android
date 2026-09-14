package app.komet.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import app.komet.domain.Pt
import app.komet.domain.TraceGlyph
import app.komet.domain.TraceStroke
import app.komet.domain.TraceTracker
import app.komet.domain.TraceZone
import app.komet.ui.theme.K
import app.komet.ui.theme.LocalMotion
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** How far from the line a finger may wander, in glyph units (a capital is 100 units tall). */
private const val TOLERANCE = 14f

private val TrackColor = Color(0xFFE4DDF2)
private val LineColor = Color(0xFFCFC5E3)

/** Where glyph units land on the canvas: one scale for the whole writing room, with the glyph centred. */
private class TraceFrame(val scale: Float, val left: Float, val top: Float) {
    fun px(p: Pt) = Offset(left + p.x * scale, top + p.y * scale)
    fun y(units: Float) = top + units * scale
    fun units(o: Offset) = Pt((o.x - left) / scale, (o.y - top) / scale)
}

private fun frameFor(glyph: TraceGlyph, width: Float, height: Float): TraceFrame {
    // Capitals get room above for the ring of Å; small letters get the descender room below.
    val roomTop = if (glyph.zone == TraceZone.CAPITAL) -30f else -12f
    val roomBottom = if (glyph.zone == TraceZone.CAPITAL) 112f else 162f
    // Wide enough for Æ and æ, the widest glyphs, so every letter in a round is drawn at the same size.
    val roomWidth = if (glyph.zone == TraceZone.CAPITAL) 112f else 104f
    val scale = min(width / roomWidth, height / (roomBottom - roomTop))
    val centre = (glyph.minX + glyph.maxX) / 2
    return TraceFrame(
        scale = scale,
        left = width / 2 - centre * scale,
        top = (height - (roomBottom - roomTop) * scale) / 2 - roomTop * scale,
    )
}

private fun distance(a: Pt, b: Pt): Float = kotlin.math.hypot(a.x - b.x, a.y - b.y)

private fun pathOf(stroke: TraceStroke, frame: TraceFrame, from: Float = 0f, to: Float = stroke.length): Path {
    val path = Path()
    val start = frame.px(stroke.pointAt(from))
    path.moveTo(start.x, start.y)
    stroke.points.forEachIndexed { index, point ->
        val along = stroke.lengths[index]
        if (along > from && along < to) {
            val p = frame.px(point)
            path.lineTo(p.x, p.y)
        }
    }
    val end = frame.px(stroke.pointAt(to))
    path.lineTo(end.x, end.y)
    return path
}

/**
 * A writing pad: the letter lies on school writing lines as a pale track, a green dot shows where
 * the next stroke starts, and the child drags a comet along it. Written strokes light up in
 * [accent]; the whole letter turns gold when it is done.
 */
@Composable
fun TracingPad(
    glyph: TraceGlyph,
    resetKey: Any,
    accent: Color,
    finished: Boolean,
    description: String,
    onStroke: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tracker = remember(resetKey, glyph) { TraceTracker(glyph, TOLERANCE) }
    var tick by remember(resetKey, glyph) { mutableIntStateOf(0) }
    var misses by remember(resetKey, glyph) { mutableIntStateOf(0) }
    val strokeDone by rememberUpdatedState(onStroke)
    val glyphDone by rememberUpdatedState(onDone)
    val isFinished by rememberUpdatedState(finished)
    val motion = LocalMotion.current
    val measurer = rememberTextMeasurer()

    val transition = rememberInfiniteTransition(label = "trace")
    val pulse by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(850), RepeatMode.Reverse), label = "pulse")
    // A miss makes the start dot or the comet swell for a moment: «here!».
    val nudge = remember(resetKey, glyph) { Animatable(0f) }
    LaunchedEffect(misses) {
        if (misses > 0) {
            nudge.snapTo(1f)
            nudge.animateTo(0f, tween(550))
        }
    }
    val celebrate = remember(resetKey, glyph) { Animatable(0f) }
    LaunchedEffect(finished) {
        if (finished) celebrate.animateTo(1f, tween(1000)) else celebrate.snapTo(0f)
    }

    Canvas(
        modifier
            .semantics { contentDescription = description }
            .pointerInput(resetKey, glyph) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    if (isFinished || tracker.done) return@awaitEachGesture
                    val frame = frameFor(glyph, size.width.toFloat(), size.height.toFloat())
                    fun handle(event: TraceTracker.Event) {
                        when (event) {
                            TraceTracker.Event.STROKE_DONE -> strokeDone()
                            TraceTracker.Event.GLYPH_DONE -> glyphDone()
                            TraceTracker.Event.MISSED_START, TraceTracker.Event.LEFT_PATH -> misses++
                            TraceTracker.Event.MOVED, TraceTracker.Event.NONE -> Unit
                        }
                        tick++
                    }
                    val start = frame.units(down.position)
                    handle(tracker.down(start.x, start.y))
                    down.consume()
                    while (!tracker.done) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) {
                            tracker.up()
                            tick++
                            break
                        }
                        change.historical.forEach { moved ->
                            val p = frame.units(moved.position)
                            handle(tracker.move(p.x, p.y))
                        }
                        val p = frame.units(change.position)
                        handle(tracker.move(p.x, p.y))
                        change.consume()
                    }
                }
            },
    ) {
        @Suppress("UNUSED_EXPRESSION") tick // Redraw whenever the tracker moves.
        val frame = frameFor(glyph, size.width, size.height)
        val s = frame.scale
        val round = { width: Float -> Stroke(width, cap = StrokeCap.Round, join = StrokeJoin.Round) }

        drawWritingLines(glyph, frame)

        // The pale track of the whole letter.
        glyph.strokes.forEach { stroke ->
            if (stroke.isDot) {
                drawCircle(TrackColor, 11f * s, frame.px(stroke.start))
            } else {
                drawPath(pathOf(stroke, frame), TrackColor, style = round(20f * s))
            }
        }

        // What has been written. All glows go down before any core, so crossing strokes stay clean.
        val lit = lerp(accent, K.Gold, celebrate.value)
        val glowColor = lerp(lit, Color.White, 0.55f)
        val written = glyph.strokes.mapIndexedNotNull { index, stroke ->
            val length = when {
                tracker.done || index < tracker.stroke -> stroke.length
                index == tracker.stroke && !stroke.isDot -> tracker.progress
                else -> return@mapIndexedNotNull null
            }
            if (!stroke.isDot && length <= 0.5f) null else stroke to length
        }
        written.forEach { (stroke, length) ->
            if (stroke.isDot) drawCircle(glowColor, 15f * s, frame.px(stroke.start))
            else drawPath(pathOf(stroke, frame, to = length), glowColor, style = round(24f * s))
        }
        written.forEach { (stroke, length) ->
            if (stroke.isDot) {
                drawCircle(lit, 9f * s, frame.px(stroke.start))
            } else {
                val path = pathOf(stroke, frame, to = length)
                drawPath(path, lit, style = round(13f * s))
                drawPath(path, Color.White.copy(alpha = 0.4f), style = round(3.5f * s))
            }
        }

        if (!tracker.done && !finished) {
            val current = glyph.strokes[tracker.stroke]
            if (!current.isDot) {
                // The way ahead: a dashed centre line and small arrows in the writing direction.
                val guide = K.InkMuted.copy(alpha = 0.5f)
                drawPath(
                    pathOf(current, frame, from = tracker.progress),
                    guide,
                    style = Stroke(2.6f * s, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f * s, 6f * s))),
                )
                var along = tracker.progress + 18f
                while (along < current.length - 7f) {
                    val tip = frame.px(current.pointAt(along))
                    val dir = current.directionAt(along)
                    val back = Offset(-dir.x * 5.5f * s, -dir.y * 5.5f * s)
                    val side = Offset(-dir.y * 4.5f * s, dir.x * 4.5f * s)
                    drawLine(guide, tip + back + side, tip, 2.8f * s, cap = StrokeCap.Round)
                    drawLine(guide, tip + back - side, tip, 2.8f * s, cap = StrokeCap.Round)
                    along += 22f
                }
            }

            // Numbered starts for the strokes still to write. Later ones are small and quiet and drawn
            // first; the next one pulses in green on top, and hides any later start in the same place.
            val next = glyph.strokes[tracker.stroke]
            val order = (tracker.stroke + 1 until glyph.strokes.size) + tracker.stroke
            order.forEach { index ->
                val stroke = glyph.strokes[index]
                val isNext = index == tracker.stroke
                if (isNext && tracker.progress > 0.5f) return@forEach
                if (!isNext && distance(stroke.start, next.start) < 14f) return@forEach
                val centre = frame.px(stroke.start)
                val radius = (if (isNext) 11f else 6.5f) * s
                if (isNext) {
                    val ring = radius * (1.45f + 0.35f * (if (motion) pulse else 0.5f)) + nudge.value * 9f * s
                    drawCircle(K.Good.copy(alpha = 0.28f), ring, centre)
                }
                val alpha = if (isNext) 1f else 0.7f
                drawCircle(K.Outline.copy(alpha = if (isNext) 0.75f else 0.3f), radius + 1.6.dp.toPx(), centre)
                drawCircle((if (isNext) K.Good else Color(0xFFA9A2C6)).copy(alpha = alpha), radius, centre)
                val label = measurer.measure(
                    "${index + 1}",
                    TextStyle(color = Color.White.copy(alpha = alpha), fontSize = (radius * 1.15f).toSp(), fontWeight = FontWeight.Black),
                )
                drawText(label, topLeft = centre - Offset(label.size.width / 2f, label.size.height / 2f))
            }

            // The comet itself, once the stroke has begun.
            if (!current.isDot && tracker.progress > 0.5f) {
                val head = frame.px(current.pointAt(tracker.progress))
                val glow = 18f * s * (1f + (if (motion && !tracker.tracking) 0.2f * pulse else 0f)) + nudge.value * 9f * s
                drawCircle(Brush.radialGradient(listOf(K.Gold.copy(alpha = 0.8f), Color.Transparent), head, glow), glow, head)
                drawCircle(K.Outline.copy(alpha = 0.6f), 7.6f * s, head)
                drawCircle(Color.White, 6.2f * s, head)
                drawCircle(K.Gold, 6.2f * s, head, style = Stroke(2.2f * s))
            }
        }

        if (finished && motion) drawSparkles(glyph, frame, celebrate.value)
    }
}

private fun DrawScope.drawWritingLines(glyph: TraceGlyph, frame: TraceFrame) {
    val inset = 14.dp.toPx()
    val lines = if (glyph.zone == TraceZone.CAPITAL) listOf(0f, 50f, 100f) else listOf(0f, 50f, 100f, 150f)
    lines.forEach { units ->
        val y = frame.y(units)
        val baseline = units == 100f
        drawLine(
            color = if (baseline) LineColor else LineColor.copy(alpha = 0.7f),
            start = Offset(inset, y),
            end = Offset(size.width - inset, y),
            strokeWidth = (if (baseline) 2.6.dp else 1.6.dp).toPx(),
            pathEffect = if (units == 50f) PathEffect.dashPathEffect(floatArrayOf(9.dp.toPx(), 7.dp.toPx())) else null,
        )
    }
}

/** Gold sparks fly out from the finished letter. */
private fun DrawScope.drawSparkles(glyph: TraceGlyph, frame: TraceFrame, progress: Float) {
    if (progress <= 0f || progress >= 1f) return
    val centre = frame.px(Pt((glyph.minX + glyph.maxX) / 2, (glyph.minY + glyph.maxY) / 2))
    val reach = (glyph.maxY - glyph.minY).coerceAtLeast(60f) * frame.scale * 0.75f
    val alpha = (1f - progress).coerceIn(0f, 1f)
    for (i in 0 until 12) {
        val angle = i * 2 * PI / 12 + 0.3
        val distance = reach * (0.55f + 0.45f * progress) * (if (i % 2 == 0) 1f else 0.8f)
        val p = centre + Offset((cos(angle) * distance).toFloat(), (sin(angle) * distance).toFloat())
        val r = frame.scale * (if (i % 2 == 0) 5f else 3.5f) * (1f - progress * 0.5f)
        drawLine(K.Gold.copy(alpha = alpha), p - Offset(r, 0f), p + Offset(r, 0f), r * 0.55f, cap = StrokeCap.Round)
        drawLine(K.Gold.copy(alpha = alpha), p - Offset(0f, r), p + Offset(0f, r), r * 0.55f, cap = StrokeCap.Round)
    }
}
