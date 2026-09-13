package app.komet.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

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
}
