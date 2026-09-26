package app.komet.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class SynthTest {

    @Test
    fun `every effect renders a short, clean wav`() {
        Sfx.entries.forEach { sfx ->
            val samples = Synth.render(sfx)
            val seconds = samples.size / Synth.SAMPLE_RATE.toFloat()
            assertTrue("$sfx is $seconds s", seconds in 0.05f..1.6f)
            assertTrue("$sfx clips", samples.all { it in -1f..1f })
            val wav = Synth.wav(samples)
            assertEquals("RIFF", String(wav, 0, 4, Charsets.US_ASCII))
            assertEquals("WAVE", String(wav, 8, 4, Charsets.US_ASCII))
            assertEquals("data", String(wav, 36, 4, Charsets.US_ASCII))
            assertEquals(44 + samples.size * 2, wav.size)
        }
    }

    @Test
    fun `new interaction sounds have audible attacks and quiet tails`() {
        listOf(Sfx.TAP, Sfx.OPEN, Sfx.PLACE).forEach { sfx ->
            val samples = Synth.render(sfx)
            val first = samples.take(Synth.SAMPLE_RATE / 25).map { abs(it) }.average()
            val tail = samples.takeLast(Synth.SAMPLE_RATE / 100).map { abs(it) }.average()
            assertTrue("$sfx has no audible attack", first > 0.01)
            assertTrue("$sfx ends abruptly", tail < first * 0.15)
        }
    }
}
