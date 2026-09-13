package app.komet.domain

import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/** A point in glyph units: y = 0 is the top line, 50 the dashed middle line, 100 the baseline. */
data class Pt(val x: Float, val y: Float)

/** Capital letters and digits sit between the top line and the baseline; small letters also use the descender room below. */
enum class TraceZone { CAPITAL, SMALL }

/** One pen movement, sampled densely enough to follow with a finger. A single point is a dot to tap. */
class TraceStroke(val points: List<Pt>) {
    init {
        require(points.isNotEmpty()) { "a stroke needs at least one point" }
    }

    val isDot: Boolean get() = points.size == 1

    /** Distance along the stroke at each point. */
    val lengths: FloatArray = FloatArray(points.size).also { acc ->
        for (i in 1 until points.size) acc[i] = acc[i - 1] + distance(points[i - 1], points[i])
    }

    val length: Float get() = lengths.last()

    val start: Pt get() = points.first()

    val end: Pt get() = points.last()

    fun pointAt(distance: Float): Pt {
        if (isDot || distance <= 0f) return points.first()
        if (distance >= length) return points.last()
        val i = segmentAt(distance)
        val span = lengths[i + 1] - lengths[i]
        val t = if (span <= 0f) 0f else (distance - lengths[i]) / span
        val a = points[i]
        val b = points[i + 1]
        return Pt(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)
    }

    /** Unit vector of the writing direction at [distance]. */
    fun directionAt(distance: Float): Pt {
        if (isDot) return Pt(0f, 1f)
        val i = segmentAt(distance.coerceIn(0f, length))
        val a = points[i]
        val b = points[i + 1]
        val d = distance(a, b).takeIf { it > 0f } ?: return Pt(0f, 1f)
        return Pt((b.x - a.x) / d, (b.y - a.y) / d)
    }

    /**
     * The closest point to ([x], [y]) whose distance along the stroke lies in [from]..[to].
     * Returns the distance along the stroke and the gap to the finger.
     */
    fun nearest(x: Float, y: Float, from: Float, to: Float): Pair<Float, Float> {
        if (isDot) return 0f to hypot(x - points[0].x, y - points[0].y)
        var bestAlong = from.coerceIn(0f, length)
        var bestGap = Float.MAX_VALUE
        for (i in 0 until points.size - 1) {
            val segStart = lengths[i]
            val segEnd = lengths[i + 1]
            if (segEnd < from || segStart > to) continue
            val a = points[i]
            val b = points[i + 1]
            val dx = b.x - a.x
            val dy = b.y - a.y
            val span = dx * dx + dy * dy
            var t = if (span <= 0f) 0f else ((x - a.x) * dx + (y - a.y) * dy) / span
            // Stay inside the allowed window, also within this segment.
            val tMin = if (segEnd > segStart) ((from - segStart) / (segEnd - segStart)).coerceIn(0f, 1f) else 0f
            val tMax = if (segEnd > segStart) ((to - segStart) / (segEnd - segStart)).coerceIn(0f, 1f) else 1f
            t = t.coerceIn(tMin, tMax)
            val px = a.x + dx * t
            val py = a.y + dy * t
            val gap = hypot(x - px, y - py)
            if (gap < bestGap) {
                bestGap = gap
                bestAlong = segStart + (segEnd - segStart) * t
            }
        }
        return bestAlong to bestGap
    }

    private fun segmentAt(distance: Float): Int {
        var lo = 0
        var hi = points.size - 2
        while (lo < hi) {
            val mid = (lo + hi + 1) / 2
            if (lengths[mid] <= distance) lo = mid else hi = mid - 1
        }
        return lo
    }
}

class TraceGlyph(val symbol: String, val zone: TraceZone, val strokes: List<TraceStroke>) {
    val minX: Float = strokes.minOf { s -> s.points.minOf { it.x } }
    val maxX: Float = strokes.maxOf { s -> s.points.maxOf { it.x } }
    val minY: Float = strokes.minOf { s -> s.points.minOf { it.y } }
    val maxY: Float = strokes.maxOf { s -> s.points.maxOf { it.y } }
}

private fun distance(a: Pt, b: Pt): Float = hypot(b.x - a.x, b.y - a.y)

/** Builds one stroke from pen commands. Every command samples points at most [STEP] units apart. */
private class StrokeBuilder {
    val points = ArrayList<Pt>()

    private val pen: Pt? get() = points.lastOrNull()

    fun move(x: Number, y: Number) {
        check(points.isEmpty()) { "move only starts a stroke" }
        points += Pt(x.toFloat(), y.toFloat())
    }

    fun dot(x: Number, y: Number) = move(x, y)

    fun line(x: Number, y: Number) {
        val from = checkNotNull(pen) { "line needs a start point" }
        val to = Pt(x.toFloat(), y.toFloat())
        val steps = max(1, ceil(distance(from, to) / STEP).toInt())
        for (i in 1..steps) {
            val t = i.toFloat() / steps
            points += Pt(from.x + (to.x - from.x) * t, from.y + (to.y - from.y) * t)
        }
    }

    fun cubic(c1x: Number, c1y: Number, c2x: Number, c2y: Number, x: Number, y: Number) {
        val p0 = checkNotNull(pen) { "cubic needs a start point" }
        val p1 = Pt(c1x.toFloat(), c1y.toFloat())
        val p2 = Pt(c2x.toFloat(), c2y.toFloat())
        val p3 = Pt(x.toFloat(), y.toFloat())
        // A cubic never moves faster than three times its longest control leg, so this bounds the spacing.
        val fastest = 3 * maxOf(distance(p0, p1), distance(p1, p2), distance(p2, p3))
        val steps = max(2, ceil(fastest / STEP).toInt())
        for (i in 1..steps) {
            val t = i.toFloat() / steps
            val u = 1 - t
            val a = u * u * u
            val b = 3 * u * u * t
            val c = 3 * u * t * t
            val d = t * t * t
            points += Pt(a * p0.x + b * p1.x + c * p2.x + d * p3.x, a * p0.y + b * p1.y + c * p2.y + d * p3.y)
        }
    }

    /**
     * An elliptical arc. Angles are in degrees on screen, so a negative [sweep] runs anticlockwise as
     * seen by the child: from the top towards the left.
     */
    fun arc(cx: Number, cy: Number, rx: Number, ry: Number, start: Number, sweep: Number) {
        val centreX = cx.toFloat()
        val centreY = cy.toFloat()
        val radiusX = rx.toFloat()
        val radiusY = ry.toFloat()
        val startRad = start.toDouble() * PI / 180
        val sweepRad = sweep.toDouble() * PI / 180
        fun at(angle: Double) = Pt(centreX + radiusX * cos(angle).toFloat(), centreY + radiusY * sin(angle).toFloat())
        val first = at(startRad)
        if (points.isEmpty()) points += first else if (distance(pen!!, first) > 0.5f) line(first.x, first.y)
        val rough = abs(sweepRad) * max(radiusX, radiusY)
        val steps = max(8, ceil(rough / STEP).toInt())
        for (i in 1..steps) points += at(startRad + sweepRad * i / steps)
    }

    private fun abs(value: Double) = if (value < 0) -value else value

    companion object {
        const val STEP = 1.5f
    }
}

private fun glyph(symbol: String, zone: TraceZone, vararg strokes: StrokeBuilder.() -> Unit): Pair<String, TraceGlyph> =
    symbol to TraceGlyph(symbol, zone, strokes.map { build -> TraceStroke(StrokeBuilder().apply(build).points.toList()) })

private fun caps(symbol: String, vararg strokes: StrokeBuilder.() -> Unit) = glyph(symbol, TraceZone.CAPITAL, *strokes)

private fun small(symbol: String, vararg strokes: StrokeBuilder.() -> Unit) = glyph(symbol, TraceZone.SMALL, *strokes)

/**
 * Letters and digits for writing practice, following the stroke order and direction of Norwegian
 * stavskrift: start at the top, write downwards and from left to right, round letters anticlockwise
 * from the top, a and g with one bowl.
 */
object Strokes {

    // Round small letters share one bowl in the x-height, started at «two o'clock».
    private val bowl: StrokeBuilder.() -> Unit = { arc(22, 75, 22, 25, -30, -360) }

    private val upper: Map<String, TraceGlyph> = mapOf(
        caps("A", { move(32, 0); line(2, 100) }, { move(32, 0); line(62, 100) }, { move(13, 64); line(51, 64) }),
        caps(
            "B",
            { move(0, 0); line(0, 100) },
            { move(0, 0); line(22, 0); cubic(50, 0, 50, 48, 22, 48); line(0, 48) },
            { move(0, 48); line(26, 48); cubic(56, 48, 56, 100, 26, 100); line(0, 100) },
        ),
        caps("C", { arc(34, 50, 32, 50, -42, -276) }),
        caps("D", { move(0, 0); line(0, 100) }, { move(0, 0); line(16, 0); cubic(66, 0, 66, 100, 16, 100); line(0, 100) }),
        caps("E", { move(0, 0); line(0, 100) }, { move(0, 0); line(48, 0) }, { move(0, 50); line(40, 50) }, { move(0, 100); line(48, 100) }),
        caps("F", { move(0, 0); line(0, 100) }, { move(0, 0); line(46, 0) }, { move(0, 50); line(38, 50) }),
        caps("G", { arc(34, 50, 32, 50, -42, -318); line(40, 50) }),
        caps("H", { move(0, 0); line(0, 100) }, { move(56, 0); line(56, 100) }, { move(0, 50); line(56, 50) }),
        caps("I", { move(0, 0); line(0, 100) }),
        caps("J", { move(40, 0); line(40, 70); cubic(40, 106, 2, 108, 0, 76) }),
        caps("K", { move(0, 0); line(0, 100) }, { move(52, 0); line(2, 56); line(54, 100) }),
        caps("L", { move(0, 0); line(0, 100); line(46, 100) }),
        caps("M", { move(0, 0); line(0, 100) }, { move(0, 0); line(36, 66); line(72, 0); line(72, 100) }),
        caps("N", { move(0, 0); line(0, 100) }, { move(0, 0); line(58, 100); line(58, 0) }),
        caps("O", { arc(33, 50, 33, 50, -90, -360) }),
        caps("P", { move(0, 0); line(0, 100) }, { move(0, 0); line(20, 0); cubic(52, 0, 52, 54, 20, 54); line(0, 54) }),
        caps("Q", { arc(33, 50, 33, 50, -90, -360) }, { move(40, 72); line(68, 104) }),
        caps(
            "R",
            { move(0, 0); line(0, 100) },
            { move(0, 0); line(20, 0); cubic(52, 0, 52, 52, 20, 52); line(0, 52) },
            { move(18, 52); line(54, 100) },
        ),
        caps(
            "S",
            {
                move(50, 16); cubic(46, 5, 37, 0, 26, 0); cubic(12, 0, 3, 10, 3, 24); cubic(3, 38, 15, 44, 27, 49)
                cubic(40, 54, 52, 61, 52, 76); cubic(52, 91, 41, 100, 26, 100); cubic(12, 100, 3, 94, 0, 84)
            },
        ),
        caps("T", { move(29, 0); line(29, 100) }, { move(0, 0); line(58, 0) }),
        caps("U", { move(0, 0); line(0, 64); cubic(0, 110, 56, 110, 56, 64); line(56, 0) }),
        caps("V", { move(0, 0); line(30, 100); line(60, 0) }),
        caps("W", { move(0, 0); line(20, 100); line(42, 0); line(64, 100); line(84, 0) }),
        caps("X", { move(0, 0); line(58, 100) }, { move(58, 0); line(0, 100) }),
        caps("Y", { move(0, 0); line(30, 50) }, { move(60, 0); line(30, 50); line(30, 100) }),
        caps("Z", { move(0, 0); line(56, 0); line(0, 100); line(56, 100) }),
        caps(
            "Æ",
            { move(44, 0); line(2, 100) },
            { move(44, 0); line(44, 100) },
            { move(18, 62); line(44, 62) },
            { move(44, 0); line(88, 0) },
            { move(44, 50); line(82, 50) },
            { move(44, 100); line(88, 100) },
        ),
        caps("Ø", { arc(33, 50, 33, 50, -90, -360) }, { move(62, 4); line(4, 96) }),
        caps(
            "Å",
            { move(32, 0); line(2, 100) },
            { move(32, 0); line(62, 100) },
            { move(13, 64); line(51, 64) },
            { arc(32, -16, 9, 9, -90, -360) },
        ),
    )

    private val lower: Map<String, TraceGlyph> = mapOf(
        small("a", bowl, { move(44, 50); line(44, 100) }),
        small("b", { move(0, 0); line(0, 100) }, { arc(23, 75, 23, 25, 180, 360) }),
        small("c", { arc(22, 75, 22, 25, -40, -280) }),
        small("d", bowl, { move(44, 0); line(44, 100) }),
        small("e", { move(0, 75); line(44, 75); arc(22, 75, 22, 25, 0, -300) }),
        small("f", { move(36, 6); cubic(30, -1, 14, -2, 14, 18); line(14, 100) }, { move(0, 50); line(30, 50) }),
        small("g", bowl, { move(44, 50); line(44, 128); cubic(44, 154, 8, 156, 2, 138) }),
        small("h", { move(0, 0); line(0, 100) }, { move(0, 74); cubic(4, 54, 14, 50, 22, 50); cubic(34, 50, 44, 58, 44, 72); line(44, 100) }),
        small("i", { move(2, 50); line(2, 100) }, { dot(2, 28) }),
        small("j", { move(26, 50); line(26, 128); cubic(26, 152, 6, 156, 0, 138) }, { dot(26, 28) }),
        small("k", { move(0, 0); line(0, 100) }, { move(40, 50); line(2, 78); line(42, 100) }),
        small("l", { move(2, 0); line(2, 100) }),
        small(
            "m",
            { move(0, 50); line(0, 100) },
            { move(0, 72); cubic(4, 54, 12, 50, 18, 50); cubic(28, 50, 34, 58, 34, 70); line(34, 100) },
            { move(34, 72); cubic(38, 54, 46, 50, 52, 50); cubic(62, 50, 70, 58, 70, 70); line(70, 100) },
        ),
        small("n", { move(0, 50); line(0, 100) }, { move(0, 74); cubic(4, 54, 14, 50, 22, 50); cubic(34, 50, 44, 58, 44, 72); line(44, 100) }),
        small("o", { arc(23, 75, 23, 25, -90, -360) }),
        small("p", { move(0, 50); line(0, 150) }, { arc(23, 75, 23, 25, 180, 360) }),
        small("q", bowl, { move(44, 50); line(44, 150) }),
        small("r", { move(0, 50); line(0, 100) }, { move(0, 76); cubic(4, 56, 16, 50, 24, 50); cubic(28, 50, 32, 51, 34, 53) }),
        small(
            "s",
            {
                move(36, 58); cubic(32, 52, 26, 50, 19, 50); cubic(9, 50, 3, 56, 3, 63); cubic(3, 72, 12, 74, 19, 75)
                cubic(28, 77, 37, 80, 37, 88); cubic(37, 96, 29, 100, 19, 100); cubic(10, 100, 3, 97, 0, 92)
            },
        ),
        small("t", { move(12, 14); line(12, 100) }, { move(0, 50); line(28, 50) }),
        small("u", { move(0, 50); line(0, 76); cubic(0, 92, 10, 100, 22, 100); cubic(34, 100, 44, 92, 44, 76); line(44, 50) }, { move(44, 50); line(44, 100) }),
        small("v", { move(0, 50); line(22, 100); line(44, 50) }),
        small("w", { move(0, 50); line(16, 100); line(32, 54); line(48, 100); line(64, 50) }),
        small("x", { move(0, 50); line(42, 100) }, { move(42, 50); line(0, 100) }),
        small("y", { move(0, 50); line(24, 100) }, { move(44, 50); line(4, 150) }),
        small("z", { move(0, 50); line(40, 50); line(0, 100); line(40, 100) }),
        small(
            "æ",
            { arc(21, 75, 21, 25, -30, -360) },
            { move(42, 50); line(42, 100) },
            { move(42, 75); line(84, 75); arc(63, 75, 21, 25, 0, -300) },
        ),
        small("ø", { arc(23, 75, 23, 25, -90, -360) }, { move(44, 46); line(2, 104) }),
        small("å", bowl, { move(44, 50); line(44, 100) }, { arc(22, 26, 9, 9, -90, -360) }),
    )

    private val digits: Map<String, TraceGlyph> = mapOf(
        caps("0", { arc(24, 50, 24, 50, -90, -360) }),
        caps("1", { move(2, 24); line(28, 0); line(28, 100) }),
        caps("2", { move(2, 26); cubic(4, 4, 20, 0, 28, 0); cubic(44, 0, 52, 12, 52, 26); cubic(52, 46, 30, 64, 0, 100); line(54, 100) }),
        caps(
            "3",
            {
                move(2, 14); cubic(10, 4, 18, 0, 26, 0); cubic(40, 0, 48, 10, 48, 24); cubic(48, 38, 38, 47, 22, 47)
                cubic(40, 47, 52, 58, 52, 74); cubic(52, 90, 40, 100, 26, 100); cubic(14, 100, 4, 94, 0, 86)
            },
        ),
        caps("4", { move(30, 0); line(0, 66); line(54, 66) }, { move(40, 30); line(40, 100) }),
        caps(
            "5",
            { move(8, 0); line(4, 44); cubic(14, 36, 24, 34, 30, 34); cubic(44, 34, 52, 46, 52, 64); cubic(52, 86, 40, 100, 24, 100); cubic(12, 100, 4, 94, 0, 86) },
            { move(8, 0); line(46, 0) },
        ),
        caps("6", { move(44, 6); cubic(20, -6, 2, 24, 2, 74); arc(27, 74, 25, 26, 180, -360) }),
        caps("7", { move(0, 0); line(52, 0); line(14, 100) }),
        caps(
            "8",
            {
                move(44, 16); cubic(40, 4, 34, 0, 25, 0); cubic(14, 0, 6, 8, 6, 22); cubic(6, 36, 20, 44, 26, 49)
                cubic(38, 56, 50, 62, 50, 76); cubic(50, 92, 40, 100, 25, 100); cubic(10, 100, 0, 92, 0, 76)
                cubic(0, 62, 12, 56, 24, 49); cubic(32, 44, 44, 36, 44, 16)
            },
        ),
        caps("9", { arc(25, 26, 24, 26, 0, -360); line(49, 100) }),
    )

    /** Every symbol that can be written: capitals, small letters and digits. */
    val symbols: Set<String> get() = upper.keys + lower.keys + digits.keys

    fun glyph(symbol: String): TraceGlyph? = upper[symbol] ?: lower[symbol] ?: digits[symbol]
}

/**
 * Follows a finger along a glyph: one stroke at a time, only in the writing direction, and never
 * backwards. Lifting the finger keeps what is written. Coordinates are glyph units.
 */
class TraceTracker(val glyph: TraceGlyph, private val tolerance: Float) {

    enum class Event { NONE, MOVED, STROKE_DONE, GLYPH_DONE, MISSED_START, LEFT_PATH }

    var stroke: Int = 0
        private set

    /** Distance written along the current stroke. */
    var progress: Float = 0f
        private set

    var tracking: Boolean = false
        private set

    val done: Boolean get() = stroke >= glyph.strokes.size

    /** Where the comet waits: the pen position on the current stroke. */
    val head: Pt? get() = glyph.strokes.getOrNull(stroke)?.pointAt(progress)

    private var lastX = 0f
    private var lastY = 0f

    fun down(x: Float, y: Float): Event {
        if (done) return Event.NONE
        val current = glyph.strokes[stroke]
        val head = current.pointAt(progress)
        if (hypot(x - head.x, y - head.y) > tolerance * START_REACH) {
            tracking = false
            return Event.MISSED_START
        }
        if (current.isDot) return completeStroke(x, y)
        tracking = true
        lastX = x
        lastY = y
        return Event.MOVED
    }

    fun move(x: Float, y: Float): Event {
        if (!tracking || done) return Event.NONE
        val current = glyph.strokes[stroke]
        val (along, gap) = current.nearest(x, y, from = progress - BACK_SLACK, to = progress + LOOKAHEAD + tolerance)
        if (gap > tolerance * LEAVE_FACTOR) {
            tracking = false
            return Event.LEFT_PATH
        }
        // Only a finger moving the way the pen goes writes; where strokes overlap, going back does not count.
        val dx = x - lastX
        val dy = y - lastY
        val moved = hypot(dx, dy)
        val direction = current.directionAt(along)
        val forwards = moved < 0.8f || dx * direction.x + dy * direction.y > -0.2f * moved
        lastX = x
        lastY = y
        if (gap <= tolerance && along > progress && forwards) progress = along
        return if (current.length - progress <= endSlack(current)) completeStroke(x, y) else Event.MOVED
    }

    fun up() {
        tracking = false
    }

    private fun endSlack(stroke: TraceStroke) = min(tolerance * 0.6f, stroke.length * 0.2f)

    private fun completeStroke(x: Float, y: Float): Event {
        stroke++
        progress = 0f
        if (done) {
            tracking = false
            return Event.GLYPH_DONE
        }
        // A stroke that starts where the finger already is (the second leg of u) carries straight on.
        val next = glyph.strokes[stroke]
        tracking = !next.isDot && hypot(x - next.start.x, y - next.start.y) <= tolerance
        lastX = x
        lastY = y
        return Event.STROKE_DONE
    }

    private companion object {
        const val START_REACH = 1.5f
        const val LEAVE_FACTOR = 1.8f
        const val LOOKAHEAD = 24f
        const val BACK_SLACK = 2f
    }
}
