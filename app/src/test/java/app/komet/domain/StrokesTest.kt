package app.komet.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.hypot

class StrokesTest {

    private val capitals = WordBank.alphabet
    private val smalls = WordBank.alphabet.map { it.lowerNo() }
    private val digits = (0..9).map { it.toString() }

    @Test
    fun `every letter in both cases and every digit can be written`() {
        (capitals + smalls + digits).forEach { symbol ->
            assertNotNull("missing glyph for «$symbol»", Strokes.glyph(symbol))
        }
        assertEquals(capitals.size * 2 + digits.size, Strokes.symbols.size)
    }

    @Test
    fun `glyphs stay inside their writing lines and are densely sampled`() {
        Strokes.symbols.forEach { symbol ->
            val glyph = Strokes.glyph(symbol)!!
            val bottom = if (glyph.zone == TraceZone.CAPITAL) 108f else 158f
            assertTrue("«$symbol» reaches above the top room: ${glyph.minY}", glyph.minY >= -28f)
            assertTrue("«$symbol» reaches below its room: ${glyph.maxY}", glyph.maxY <= bottom)
            assertTrue("«$symbol» is too wide: ${glyph.minX}..${glyph.maxX}", glyph.minX >= -2f && glyph.maxX <= 92f)
            glyph.strokes.forEachIndexed { index, stroke ->
                if (stroke.isDot) return@forEachIndexed
                assertTrue("«$symbol» stroke ${index + 1} is too short", stroke.length >= 10f)
                stroke.points.zipWithNext().forEach { (a, b) ->
                    assertTrue("«$symbol» stroke ${index + 1} has a gap", hypot(b.x - a.x, b.y - a.y) <= 2.2f)
                }
            }
        }
    }

    @Test
    fun `straight strokes go down and to the right`() {
        Strokes.symbols.forEach { symbol ->
            Strokes.glyph(symbol)!!.strokes.forEachIndexed { index, stroke ->
                if (stroke.isDot) return@forEachIndexed
                val xs = stroke.points.map { it.x }
                val ys = stroke.points.map { it.y }
                if (xs.max() - xs.min() <= 1f) {
                    assertTrue("«$symbol» stroke ${index + 1} is a vertical line written upwards", stroke.end.y > stroke.start.y)
                }
                if (ys.max() - ys.min() <= 1f) {
                    assertTrue("«$symbol» stroke ${index + 1} is a horizontal line written leftwards", stroke.end.x > stroke.start.x)
                }
            }
        }
    }

    @Test
    fun `round letters are written anticlockwise from the top`() {
        listOf("O", "Q", "Ø", "o", "ø", "a", "d", "g", "q", "å", "0", "C", "c").forEach { symbol ->
            val stroke = Strokes.glyph(symbol)!!.strokes.first()
            // In screen coordinates (y down) an anticlockwise loop has a negative shoelace sum.
            assertTrue("«$symbol» runs clockwise", signedArea(stroke) < 0)
            assertTrue("«$symbol» does not start in the upper half", stroke.start.y <= (if (symbol[0].isLowerCase()) 75f else 50f))
        }
        // b and p have their bowl on the other side of the stem, so it runs clockwise.
        listOf("b", "p").forEach { symbol ->
            assertTrue("«$symbol» bowl should run clockwise", signedArea(Strokes.glyph(symbol)!!.strokes[1]) > 0)
        }
    }

    /** Shoelace sum of the stroke closed by a straight line back to its start. */
    private fun signedArea(stroke: TraceStroke): Double =
        (stroke.points + stroke.start).zipWithNext().sumOf { (a, b) -> (a.x * b.y - b.x * a.y).toDouble() }

    @Test
    fun `a finger that follows the strokes writes every glyph`() {
        Strokes.symbols.forEach { symbol ->
            val glyph = Strokes.glyph(symbol)!!
            val tracker = TraceTracker(glyph, tolerance = 12f)
            var last = TraceTracker.Event.NONE
            glyph.strokes.forEach { stroke ->
                val start = stroke.start
                last = tracker.down(start.x + 3f, start.y - 2f)
                if (!stroke.isDot) {
                    stroke.points.filterIndexed { i, _ -> i % 2 == 0 }.plus(stroke.end).forEach { p ->
                        // A slightly shaky hand: a few units off the line.
                        val event = tracker.move(p.x + 2.5f, p.y + 1.5f)
                        if (event != TraceTracker.Event.MOVED && event != TraceTracker.Event.NONE) last = event
                    }
                }
                tracker.up()
            }
            assertEquals("«$symbol» was not completed", TraceTracker.Event.GLYPH_DONE, last)
            assertTrue(tracker.done)
        }
    }

    @Test
    fun `strokes cannot be written from the wrong end or out of order`() {
        val glyph = Strokes.glyph("L")!!
        val tracker = TraceTracker(glyph, tolerance = 12f)
        assertEquals(TraceTracker.Event.MISSED_START, tracker.down(46f, 100f))
        assertEquals(TraceTracker.Event.MOVED, tracker.down(0f, 0f))
        // Jumping straight to the end is not writing.
        assertEquals(TraceTracker.Event.LEFT_PATH, tracker.move(46f, 100f))
        assertEquals(0f, tracker.progress, 0.001f)

        val h = TraceTracker(Strokes.glyph("H")!!, tolerance = 12f)
        assertEquals("the cross bar is the third stroke", TraceTracker.Event.MISSED_START, h.down(0f, 50f))
    }

    @Test
    fun `lifting the finger keeps what is written, and backwards does not count`() {
        val tracker = TraceTracker(Strokes.glyph("I")!!, tolerance = 12f)
        tracker.down(0f, 0f)
        for (y in 0..50 step 3) tracker.move(1f, y.toFloat())
        val halfway = tracker.progress
        assertTrue(halfway in 45f..52f)
        tracker.up()
        tracker.move(0f, 90f)
        assertEquals("moves without a finger do nothing", halfway, tracker.progress, 0.001f)
        tracker.down(0f, halfway)
        for (y in halfway.toInt() - 1 downTo 20 step 3) tracker.move(0f, y.toFloat())
        assertEquals("going back never undoes writing", halfway, tracker.progress, 0.001f)
        assertTrue("far behind the comet the finger lets go", !tracker.tracking)
        tracker.up()
        var event = tracker.down(0f, halfway)
        for (y in halfway.toInt()..100 step 3) event = notable(tracker.move(0f, y.toFloat()), event)
        event = notable(tracker.move(0f, 100f), event)
        assertEquals(TraceTracker.Event.GLYPH_DONE, event)
    }

    /** Keeps the last event that means something; plain moves and ignored input do not count. */
    private fun notable(event: TraceTracker.Event, previous: TraceTracker.Event) =
        if (event == TraceTracker.Event.MOVED || event == TraceTracker.Event.NONE) previous else event

    @Test
    fun `the dot on i is a tap and comes after the stem`() {
        val glyph = Strokes.glyph("i")!!
        assertTrue(glyph.strokes[1].isDot)
        val tracker = TraceTracker(glyph, tolerance = 12f)
        assertEquals(TraceTracker.Event.MISSED_START, tracker.down(2f, 28f))
        tracker.down(2f, 50f)
        var event = TraceTracker.Event.NONE
        for (y in 50..100 step 2) event = notable(tracker.move(2f, y.toFloat()), event)
        assertEquals(TraceTracker.Event.STROKE_DONE, event)
        tracker.up()
        assertEquals(TraceTracker.Event.GLYPH_DONE, tracker.down(4f, 30f))
    }

    @Test
    fun `the second leg of u carries on without lifting`() {
        val glyph = Strokes.glyph("u")!!
        val tracker = TraceTracker(glyph, tolerance = 12f)
        tracker.down(0f, 50f)
        glyph.strokes[0].points.forEach { tracker.move(it.x, it.y) }
        assertEquals(1, tracker.stroke)
        assertTrue("still tracking at the start of the second stroke", tracker.tracking)
        var event = TraceTracker.Event.NONE
        glyph.strokes[1].points.forEach { p -> event = notable(tracker.move(p.x, p.y), event) }
        assertEquals(TraceTracker.Event.GLYPH_DONE, event)
    }

    @Test
    fun `nearest point respects the window`() {
        val o = Strokes.glyph("O")!!.strokes.first()
        // The start and the end of O are the same place; at the start only the beginning counts.
        val (along, gap) = o.nearest(o.end.x, o.end.y, from = 0f, to = 30f)
        assertTrue(along < 3f)
        assertTrue(gap < 1f)
        assertTrue(abs(o.length - 260f) < 30f)
    }
}
