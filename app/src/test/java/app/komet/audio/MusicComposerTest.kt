package app.komet.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class MusicComposerTest {

    @Test
    fun `every theme renders a seamless eight-bar loop without clipping`() {
        MusicTheme.entries.forEach { theme ->
            val pcm = MusicComposer.render(theme)
            val seconds = pcm.size.toDouble() / MusicComposer.SAMPLE_RATE
            assertTrue("$theme is $seconds s", seconds in 14.0..32.0)
            val peak = pcm.maxOf { abs(it.toInt()) }
            assertTrue("$theme clips or is silent: $peak", peak in 4000..20000)
            // At the seam the wave continues: the jump is no bigger than an ordinary step inside the loop.
            val seam = abs(pcm.first().toInt() - pcm.last().toInt())
            val typicalStep = (1 until 2000).maxOf { abs(pcm[it].toInt() - pcm[it - 1].toInt()) }
            assertTrue("$theme clicks at the seam: $seam vs $typicalStep", seam <= typicalStep * 2 + 50)
        }
    }

    /** Writes every theme to build/reports/music so a person can listen to them without the app. */
    @Test
    fun `export music for listening`() {
        val dir = java.io.File("build/reports/music").apply { mkdirs() }
        MusicTheme.entries.forEach { theme ->
            val pcm = MusicComposer.render(theme)
            val bytes = java.io.ByteArrayOutputStream()
            fun int(v: Int) = repeat(4) { bytes.write((v shr (8 * it)) and 0xFF) }
            fun short(v: Int) = repeat(2) { bytes.write((v shr (8 * it)) and 0xFF) }
            val data = pcm.size * 2 * 2 // two passes, to hear the seam
            bytes.write("RIFF".toByteArray()); int(36 + data); bytes.write("WAVEfmt ".toByteArray())
            int(16); short(1); short(1); int(MusicComposer.SAMPLE_RATE); int(MusicComposer.SAMPLE_RATE * 2); short(2); short(16)
            bytes.write("data".toByteArray()); int(data)
            repeat(2) { pcm.forEach { short(it.toInt()) } }
            java.io.File(dir, "${theme.name.lowercase()}.wav").writeBytes(bytes.toByteArray())
        }
    }

    @Test
    fun `rendering is deterministic`() {
        assertEquals(MusicComposer.render(MusicTheme.MATH).toList().hashCode(), MusicComposer.render(MusicTheme.MATH).toList().hashCode())
    }
}
