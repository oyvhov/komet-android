package app.komet.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

/** Plays the synthesised effects. Rendering happens once per install, on a background thread. */
class SoundFx(context: Context) {
    private val pool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()
    private val ids = ConcurrentHashMap<Sfx, Int>()

    @Volatile
    var enabled: Boolean = true

    init {
        val directory = File(context.cacheDir, "sfx").apply { mkdirs() }
        thread(name = "komet-sfx", isDaemon = true) {
            for (sfx in Sfx.entries) {
                runCatching {
                    val file = File(directory, "${sfx.name.lowercase()}-v$VERSION.wav")
                    if (!file.exists()) file.writeBytes(Synth.wav(Synth.render(sfx)))
                    ids[sfx] = pool.load(file.path, 1)
                }
            }
        }
    }

    fun play(sfx: Sfx, volume: Float = 0.9f) {
        if (!enabled) return
        val id = ids[sfx] ?: return
        pool.play(id, volume, volume, 1, 0, 1f)
    }

    fun release() {
        pool.release()
    }

    private companion object {
        /** Bump when [Synth] changes so cached files are rendered again. */
        const val VERSION = 2
    }
}
