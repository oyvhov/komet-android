package app.komet.audio

import java.io.ByteArrayOutputStream
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

enum class Sfx { TAP, CORRECT, WRONG, STAR, COMPLETE, UNLOCK, TICK, GO, WHOOSH, STEP, BOING, BEEP, SPARKLE, COIN }

/**
 * Every sound in Komet is synthesised here: short bell tones with a soft attack, so feedback is
 * pleasant at full volume and there are no audio assets to license.
 */
object Synth {
    const val SAMPLE_RATE = 44_100

    private val bell = listOf(1.0 to 1.0, 2.0 to 0.32, 3.0 to 0.1, 4.16 to 0.05)
    private val soft = listOf(1.0 to 1.0, 2.0 to 0.12)

    private class Tone(
        val frequency: Double,
        val start: Double,
        val duration: Double,
        val gain: Double = 0.5,
        val partials: List<Pair<Double, Double>> = bell,
        val decay: Double = 5.0,
        val slideTo: Double? = null,
    )

    private fun tones(sfx: Sfx): List<Tone> = when (sfx) {
        Sfx.TAP -> listOf(Tone(1046.5, 0.0, 0.07, 0.3, soft, decay = 45.0))
        Sfx.CORRECT -> listOf(
            Tone(1046.5, 0.0, 0.32, 0.45),
            Tone(1318.5, 0.07, 0.32, 0.4),
            Tone(1568.0, 0.14, 0.5, 0.42),
        )
        Sfx.WRONG -> listOf(
            Tone(261.6, 0.0, 0.2, 0.45, soft, decay = 9.0, slideTo = 220.0),
            Tone(196.0, 0.1, 0.26, 0.4, soft, decay = 9.0),
        )
        Sfx.STAR -> listOf(
            Tone(1568.0, 0.0, 0.25, 0.35),
            Tone(2093.0, 0.05, 0.3, 0.3),
            Tone(2637.0, 0.1, 0.45, 0.28),
        )
        Sfx.COMPLETE -> listOf(
            Tone(523.25, 0.0, 0.3, 0.4),
            Tone(659.25, 0.1, 0.3, 0.4),
            Tone(783.99, 0.2, 0.3, 0.4),
            Tone(1046.5, 0.3, 0.9, 0.45, decay = 3.0),
            Tone(523.25, 0.42, 0.9, 0.22, decay = 3.0),
            Tone(659.25, 0.42, 0.9, 0.2, decay = 3.0),
            Tone(783.99, 0.42, 0.9, 0.2, decay = 3.0),
        )
        Sfx.UNLOCK -> listOf(523.25, 587.33, 659.25, 783.99, 880.0, 1046.5, 1174.66, 1318.5).mapIndexed { index, frequency ->
            Tone(frequency, index * 0.045, 0.35, 0.3, decay = 6.0)
        }
        Sfx.TICK -> listOf(Tone(880.0, 0.0, 0.06, 0.3, soft, decay = 50.0))
        Sfx.GO -> listOf(Tone(1318.5, 0.0, 0.35, 0.4), Tone(1760.0, 0.0, 0.35, 0.28))
        Sfx.WHOOSH -> emptyList()
        Sfx.STEP -> listOf(Tone(150.0, 0.0, 0.07, 0.5, soft, decay = 38.0, slideTo = 95.0))
        Sfx.BOING -> listOf(
            Tone(260.0, 0.0, 0.16, 0.45, soft, decay = 5.0, slideTo = 540.0),
            Tone(540.0, 0.14, 0.26, 0.4, soft, decay = 7.0, slideTo = 380.0),
        )
        Sfx.BEEP -> listOf(
            Tone(1174.66, 0.0, 0.08, 0.35, soft, decay = 18.0),
            Tone(1567.98, 0.09, 0.12, 0.35, soft, decay = 14.0, slideTo = 1975.5),
        )
        Sfx.SPARKLE -> listOf(2093.0, 2637.0, 3136.0, 3520.0, 4186.0).mapIndexed { index, frequency ->
            Tone(frequency, index * 0.05, 0.4, 0.22, decay = 7.0)
        }
        // A bright two-note «pling», like a coin in a game.
        Sfx.COIN -> listOf(
            Tone(1975.5, 0.0, 0.09, 0.4, decay = 18.0),
            Tone(2637.0, 0.07, 0.42, 0.42, decay = 6.0),
        )
    }

    fun render(sfx: Sfx): FloatArray {
        if (sfx == Sfx.WHOOSH) return whoosh()
        val tones = tones(sfx)
        val seconds = tones.maxOf { it.start + it.duration } + 0.02
        val out = FloatArray((seconds * SAMPLE_RATE).toInt())
        for (tone in tones) {
            val offset = (tone.start * SAMPLE_RATE).toInt()
            val count = (tone.duration * SAMPLE_RATE).toInt()
            val phases = DoubleArray(tone.partials.size)
            for (i in 0 until count) {
                val t = i.toDouble() / SAMPLE_RATE
                val progress = i.toDouble() / count
                val frequency = tone.slideTo?.let { tone.frequency + (it - tone.frequency) * progress } ?: tone.frequency
                val attack = min(1.0, t / 0.004)
                val release = min(1.0, (tone.duration - t) / 0.025).coerceAtLeast(0.0)
                val envelope = attack * release * exp(-tone.decay * t)
                var sample = 0.0
                tone.partials.forEachIndexed { k, (ratio, level) ->
                    phases[k] += 2.0 * PI * frequency * ratio / SAMPLE_RATE
                    sample += sin(phases[k]) * level
                }
                val index = offset + i
                if (index < out.size) out[index] += (sample * envelope * tone.gain).toFloat()
            }
        }
        val peak = out.maxOfOrNull { abs(it) } ?: 0f
        if (peak > 0f) {
            val scale = 0.8f / peak
            for (i in out.indices) out[i] *= scale
        }
        return out
    }

    /** Rushing air for the rocket: noise through a filter that opens and closes as it passes. */
    private fun whoosh(): FloatArray {
        val seconds = 0.95
        val out = FloatArray((seconds * SAMPLE_RATE).toInt())
        val random = java.util.Random(7)
        var low = 0.0
        var band = 0.0
        for (i in out.indices) {
            val progress = i.toDouble() / out.size
            val swell = sin(PI * progress)
            val envelope = swell * swell * min(1.0, (out.size - i) / (0.05 * SAMPLE_RATE))
            val cutoff = 250.0 + 2400.0 * swell
            val f = 2.0 * sin(PI * cutoff / SAMPLE_RATE)
            val noise = random.nextDouble() * 2.0 - 1.0
            low += f * band
            val high = noise - low - 0.6 * band
            band += f * high
            out[i] = (band * envelope).toFloat()
        }
        val peak = out.maxOfOrNull { abs(it) } ?: 0f
        if (peak > 0f) for (i in out.indices) out[i] *= 0.7f / peak
        return out
    }

    /** 16-bit mono PCM in a RIFF/WAVE container. */
    fun wav(samples: FloatArray): ByteArray {
        val dataSize = samples.size * 2
        val stream = ByteArrayOutputStream(44 + dataSize)
        fun int(value: Int) {
            stream.write(value and 0xFF)
            stream.write((value shr 8) and 0xFF)
            stream.write((value shr 16) and 0xFF)
            stream.write((value shr 24) and 0xFF)
        }
        fun short(value: Int) {
            stream.write(value and 0xFF)
            stream.write((value shr 8) and 0xFF)
        }
        stream.write("RIFF".toByteArray(Charsets.US_ASCII))
        int(36 + dataSize)
        stream.write("WAVE".toByteArray(Charsets.US_ASCII))
        stream.write("fmt ".toByteArray(Charsets.US_ASCII))
        int(16)
        short(1)
        short(1)
        int(SAMPLE_RATE)
        int(SAMPLE_RATE * 2)
        short(2)
        short(16)
        stream.write("data".toByteArray(Charsets.US_ASCII))
        int(dataSize)
        for (sample in samples) {
            short((sample.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt())
        }
        return stream.toByteArray()
    }
}
