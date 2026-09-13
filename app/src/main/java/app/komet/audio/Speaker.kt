package app.komet.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/**
 * Norwegian read-aloud through the device's own text-to-speech engine. Nothing leaves the device
 * unless the engine itself is set up for online voices.
 */
class Speaker(context: Context) {

    enum class Status { LOADING, READY, MISSING_NORWEGIAN, UNAVAILABLE }

    var status by mutableStateOf(Status.LOADING)
        private set

    /** Name of the chosen voice, for the parent screen. */
    var voiceName by mutableStateOf<String?>(null)
        private set

    var rate: Float = 0.9f

    private var pending: String? = null
    private var engine: TextToSpeech? = null

    init {
        engine = TextToSpeech(context.applicationContext) { code -> onReady(code) }
    }

    private fun onReady(code: Int) {
        val tts = engine ?: return
        if (code != TextToSpeech.SUCCESS) {
            status = Status.UNAVAILABLE
            return
        }
        val locale = LOCALES.firstOrNull { runCatching { tts.isLanguageAvailable(it) >= TextToSpeech.LANG_AVAILABLE }.getOrDefault(false) }
        if (locale == null) {
            status = Status.MISSING_NORWEGIAN
            return
        }
        tts.language = locale
        // Prefer the best installed voice that works offline.
        runCatching {
            tts.voices
                ?.filter { voice: Voice ->
                    voice.locale.language in NORWEGIAN &&
                        !voice.isNetworkConnectionRequired &&
                        TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in voice.features
                }
                ?.maxByOrNull { it.quality }
                ?.let { tts.voice = it }
        }
        voiceName = runCatching { tts.voice?.name }.getOrNull()
        status = Status.READY
        pending?.let { speak(it) }
        pending = null
    }

    fun speak(text: String) {
        if (text.isBlank()) return
        val tts = engine
        when {
            status == Status.READY && tts != null -> {
                tts.setSpeechRate(rate)
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "komet")
            }
            status == Status.LOADING -> pending = text
        }
    }

    fun stop() {
        pending = null
        engine?.stop()
    }

    fun shutdown() {
        engine?.shutdown()
        engine = null
    }

    private companion object {
        val NORWEGIAN = setOf("nb", "no", "nn")
        val LOCALES: List<Locale> = listOf("nb-NO", "no-NO", "nn-NO", "nb", "no").map(Locale::forLanguageTag)
    }
}
