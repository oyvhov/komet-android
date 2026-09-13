package app.komet.domain

import java.util.Locale

/** Written standard the child meets at school. Chosen per profile, independent of the device language. */
enum class Maalform { NYNORSK, BOKMAAL }

/** How letters and words in reading tasks are drawn. Early readers often start with capitals only. */
enum class LetterCase { UPPER, LOWER }

/**
 * A piece of text in both written standards. Most early-reading words are identical, so [nb]
 * defaults to [nn]; only the places where the standards differ spell out both.
 */
data class Txt(val nn: String, val nb: String = nn) {
    fun get(maalform: Maalform): String = if (maalform == Maalform.NYNORSK) nn else nb

    operator fun plus(other: Txt): Txt = Txt(nn + other.nn, nb + other.nb)

    fun map(transform: (String) -> String): Txt = Txt(transform(nn), transform(nb))
}

fun txt(nn: String, nb: String = nn) = Txt(nn, nb)

private val norwegian: Locale = Locale.forLanguageTag("nb-NO")

fun String.upperNo(): String = uppercase(norwegian)
fun String.lowerNo(): String = lowercase(norwegian)

/**
 * Applies the profile's letter case to reading material. In lower-case mode sentences keep the way
 * they are written — capitals after full stops and on names («Noah») are part of reading.
 */
fun String.inCase(case: LetterCase, sentence: Boolean = false): String = when (case) {
    LetterCase.UPPER -> upperNo()
    LetterCase.LOWER -> if (sentence) this else lowerNo()
}

/** Joins alternatives the way a question is read aloud: «a, b eller c». Same word in both standards. */
fun spokenList(items: List<String>): String {
    if (items.size <= 1) return items.joinToString()
    return items.dropLast(1).joinToString(", ") + " eller " + items.last()
}
