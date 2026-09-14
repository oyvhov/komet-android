package app.komet.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/**
 * Read-aloud through the device's own text-to-speech engine: Norwegian for everything, and English
 * for the parts of a text marked `{en:…}`. Nothing leaves the device unless the engine itself is set
 * up for online voices.
 */
class Speaker(context: Context) {

    enum class Status { LOADING, READY, MISSING_NORWEGIAN, UNAVAILABLE }

    var status by mutableStateOf(Status.LOADING)
        private set

    /** Name of the chosen voice, for the parent screen. */
    var voiceName by mutableStateOf<String?>(null)
        private set

    /** Whether an English voice is installed, so English words sound English. */
    var englishAvailable by mutableStateOf(false)
        private set

    var rate: Float = 0.9f

    private var pending: String? = null
    private var engine: TextToSpeech? = null
    private var norwegian: Voice? = null
    private var norwegianLocale: Locale? = null
    private var english: Voice? = null
    private var englishLocale: Locale? = null

    init {
        engine = TextToSpeech(context.applicationContext) { code -> onReady(code) }
    }

    private fun onReady(code: Int) {
        val tts = engine ?: return
        if (code != TextToSpeech.SUCCESS) {
            status = Status.UNAVAILABLE
            return
        }
        englishLocale = ENGLISH_LOCALES.firstOrNull { tts.supports(it) }
        english = englishLocale?.let { tts.bestVoice(ENGLISH) }
        englishAvailable = englishLocale != null
        val locale = NORWEGIAN_LOCALES.firstOrNull { tts.supports(it) }
        if (locale == null) {
            status = Status.MISSING_NORWEGIAN
            return
        }
        norwegianLocale = locale
        tts.language = locale
        // Prefer the best installed voice that works offline.
        norwegian = tts.bestVoice(NORWEGIAN)?.also { runCatching { tts.voice = it } }
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
                val parts = segments(text)
                parts.forEachIndexed { index, part ->
                    useLanguage(tts, part.english)
                    val mode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                    tts.speak(part.text, mode, null, "komet-$index")
                }
                if (parts.any { it.english }) useLanguage(tts, english = false)
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

    private fun useLanguage(tts: TextToSpeech, english: Boolean) {
        runCatching {
            if (english && englishLocale != null) {
                this.english?.let { tts.voice = it } ?: run { tts.language = englishLocale }
            } else {
                norwegian?.let { tts.voice = it } ?: norwegianLocale?.let { tts.language = it }
            }
        }
    }

    private fun TextToSpeech.supports(locale: Locale): Boolean =
        runCatching { isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE }.getOrDefault(false)

    private fun TextToSpeech.bestVoice(languages: Set<String>): Voice? = runCatching {
        voices
            ?.filter { voice: Voice ->
                voice.locale.language in languages &&
                    !voice.isNetworkConnectionRequired &&
                    TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in voice.features
            }
            // A British or American voice over other English variants, then the best quality.
            ?.sortedWith(compareByDescending<Voice> { it.locale.country in setOf("GB", "US", "NO") }.thenByDescending { it.quality })
            ?.firstOrNull()
    }.getOrNull()

    companion object {
        private val NORWEGIAN = setOf("nb", "no", "nn")
        private val ENGLISH = setOf("en")
        private val NORWEGIAN_LOCALES: List<Locale> = listOf("nb-NO", "no-NO", "nn-NO", "nb", "no").map(Locale::forLanguageTag)
        private val ENGLISH_LOCALES: List<Locale> = listOf("en-GB", "en-US", "en").map(Locale::forLanguageTag)

        private val MARK = Regex("""\{en:([^}]*)\}""")

        /** A piece of text to read in one language. */
        data class Segment(val text: String, val english: Boolean)

        /** Splits «Finn {en:cat}.» into Norwegian and English pieces, dropping empty ones. */
        fun segments(text: String): List<Segment> {
            val parts = ArrayList<Segment>()
            var last = 0
            for (match in MARK.findAll(text)) {
                val before = text.substring(last, match.range.first)
                if (before.any(Char::isLetterOrDigit)) parts += Segment(before.trim(), english = false)
                val word = match.groupValues[1]
                if (word.any(Char::isLetterOrDigit)) parts += Segment(word.trim(), english = true)
                last = match.range.last + 1
            }
            val rest = text.substring(last)
            // A lone full stop would otherwise be read out as «punktum».
            if (rest.any(Char::isLetterOrDigit)) parts += Segment(rest.trim(), english = false)
            return parts
        }

        /** The text as it should be shown, without language marks. */
        fun plain(text: String): String = MARK.replace(text) { it.groupValues[1] }
    }
}
