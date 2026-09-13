package app.komet.domain

enum class Gender { MASCULINE, FEMININE, NEUTER }

/**
 * One picture word. Spelling is upper case with syllables split by «-»: «BA-NAN».
 * [nb] is only given where bokmål spells the word differently.
 */
data class Word(
    private val nnSpelling: String,
    val emoji: String,
    private val nbSpelling: String = nnSpelling,
    /** Grammatical gender in nynorsk. Only set where the article in a simple sentence is certain. */
    val gender: Gender? = null,
    val tags: Set<String> = emptySet(),
) {
    fun syllables(maalform: Maalform): List<String> =
        (if (maalform == Maalform.NYNORSK) nnSpelling else nbSpelling).split('-')

    fun text(maalform: Maalform): String = syllables(maalform).joinToString("")

    val sameInBoth: Boolean get() = nnSpelling == nbSpelling

    /** First letter is not the first sound (STJERNE, KJOLE, GIRAFF, silent H …). */
    val irregularStart: Boolean get() = IRREGULAR_START in tags

    /** Last letter is not heard (BRØD). */
    val silentEnd: Boolean get() = SILENT_END in tags

    fun article(maalform: Maalform): String? = when (gender) {
        null -> null
        Gender.MASCULINE -> if (maalform == Maalform.NYNORSK) "ein" else "en"
        Gender.FEMININE -> if (maalform == Maalform.NYNORSK) "ei" else "en"
        Gender.NEUTER -> if (maalform == Maalform.NYNORSK) "eit" else "et"
    }

    companion object {
        const val IRREGULAR_START = "irregular-start"
        const val SILENT_END = "silent-end"
        /** Words that must never be offered side by side because one picture could be both (BALL/FOTBALL). */
        const val BALL = "ball"
    }
}

/** Adds the emoji presentation selector to single symbols that would otherwise render as plain text. */
fun String.asEmoji(): String =
    if (codePointCount(0, length) == 1 && !endsWith("️")) this + "️" else this

private val M = Gender.MASCULINE
private val F = Gender.FEMININE
private val N = Gender.NEUTER

private fun w(
    spelling: String,
    emoji: String,
    nb: String = spelling,
    gender: Gender? = null,
    tags: Set<String> = emptySet(),
) = Word(spelling, emoji.asEmoji(), nb, gender, tags)

object WordBank {

    val words: List<Word> = listOf(
        // Korte ord
        w("SOL", "☀", gender = F),
        w("BIL", "🚗", gender = M),
        w("MUS", "🐭", gender = F),
        w("HUS", "🏠", gender = N),
        w("KU", "🐄", gender = F),
        w("IS", "🍦", gender = M),
        w("OST", "🧀", gender = M),
        w("EGG", "🥚", gender = N),
        w("BÅT", "⛵", gender = M),
        w("TOG", "🚆", gender = N),
        w("FLY", "✈", gender = N),
        w("SKO", "👟", gender = M),
        w("BUSS", "🚌", gender = M),
        w("ULV", "🐺", gender = M),
        w("SAU", "🐑", gender = M),
        w("GRIS", "🐷", gender = M),
        w("KATT", "🐱", gender = M),
        w("HUND", "🐶", gender = M),
        w("FISK", "🐟", gender = M),
        w("BALL", "🏀", gender = M, tags = setOf(Word.BALL)),
        w("BOK", "📖", gender = F),
        w("BJØRN", "🐻", gender = M),
        w("REV", "🦊", gender = M),
        w("UG-LE", "🦉", gender = F),
        w("ØRN", "🦅", gender = M),
        w("HEST", "🐴", gender = M),
        w("GEIT", "🐐", gender = F),
        w("TRE", "🌳", gender = N),
        w("RO-SE", "🌹", gender = F),
        w("SKY", "☁", gender = M),
        w("FEST", "🎉", gender = M),
        w("KOKK", "👨‍🍳", gender = M),
        w("MANN", "👨", gender = M),
        w("RIS", "🍚"),
        w("BREV", "✉", gender = N),
        w("SNØ", "❄"),
        w("LYN", "⚡"),
        w("HATT", "🎩", gender = M),
        w("SOKK", "🧦", gender = M),
        w("TANN", "🦷", gender = F),
        w("FOT", "🦶", gender = M),
        w("MUNN", "👄", gender = M),
        w("SOPP", "🍄", gender = M),
        w("BRØD", "🍞", gender = N, tags = setOf(Word.SILENT_END)),
        w("MAIS", "🌽", gender = M),
        w("TELT", "⛺", gender = N),
        w("SLOTT", "🏰", gender = N),
        w("DØR", "🚪", gender = F),
        w("SENG", "🛏", gender = F),
        w("STOL", "🪑", gender = M),
        w("SAKS", "✂", gender = F),
        w("DUSJ", "🚿", gender = M),
        w("FLAGG", "🚩", gender = N),
        w("KART", "🗺", gender = N),
        w("LYS", "🕯", gender = N),
        w("ØY", "🏝", gender = F),
        w("FJELL", "🏔", gender = N),
        w("MAUR", "🐜", gender = M),
        w("HAI", "🦈", gender = M),
        w("FROSK", "🐸", gender = M),
        w("KVAL", "🐳", nb = "HVAL", gender = M, tags = setOf(Word.IRREGULAR_START)),
        w("MJØLK", "🥛", nb = "MELK"),
        w("HAND", "✋", nb = "HÅND", gender = F),

        // Frukt og mat
        w("EP-LE", "🍎", gender = N),
        w("PÆ-RE", "🍐", gender = F),
        w("BA-NAN", "🍌", gender = M),
        w("SI-TRON", "🍋", gender = M),
        w("DRU-E", "🍇", gender = F),
        w("ME-LON", "🍉", gender = M),
        w("KA-KE", "🎂", gender = F),
        w("PIZ-ZA", "🍕", gender = M),
        w("PO-TET", "🥔", gender = M),
        w("GUL-ROT", "🥕", gender = F),
        w("SJO-KO-LA-DE", "🍫", gender = M, tags = setOf(Word.IRREGULAR_START)),
        w("POP-KORN", "🍿"),
        w("JORD-BÆR", "🍓", gender = N),

        // Dyr
        w("LØ-VE", "🦁"),
        w("TI-GER", "🐯", gender = M),
        w("A-PE", "🐒"),
        w("SLAN-GE", "🐍", gender = M),
        w("KRAB-BE", "🦀", gender = M),
        w("PING-VIN", "🐧", gender = M),
        w("HA-NE", "🐓", gender = M),
        w("BI-E", "🐝", gender = F),
        w("ED-DER-KOPP", "🕷", gender = M),
        w("DEL-FIN", "🐬", gender = M),
        w("KA-MEL", "🐫", gender = M),
        w("E-LE-FANT", "🐘", gender = M),
        w("GI-RAFF", "🦒", gender = M, tags = setOf(Word.IRREGULAR_START)),
        w("SE-BRA", "🦓", gender = M),
        w("PAN-DA", "🐼", gender = M),
        w("KO-A-LA", "🐨", gender = M),
        w("KA-NIN", "🐰", gender = M),
        w("ROT-TE", "🐀", gender = F),
        w("MA-RI-HØ-NE", "🐞", gender = F),
        w("BLEKK-SPRUT", "🐙", gender = M),
        w("DRA-GE", "🐉", gender = M),
        w("DI-NO-SAUR", "🦖", gender = M),
        w("SOM-MAR-FUGL", "🦋", nb = "SOM-MER-FUGL", gender = M),

        // Ting
        w("SYK-KEL", "🚲", gender = M),
        w("RA-KETT", "🚀", gender = M),
        w("RO-BOT", "🤖", gender = M),
        w("STJER-NE", "⭐", gender = F, tags = setOf(Word.IRREGULAR_START)),
        w("PLA-NET", "🪐", gender = M),
        w("KO-MET", "☄", gender = M),
        w("MÅ-NE", "🌙", gender = M),
        w("BUK-SE", "👖", gender = F),
        w("KJO-LE", "👗", gender = M, tags = setOf(Word.IRREGULAR_START)),
        w("KRO-NE", "👑", gender = F),
        w("NØK-KEL", "🔑", gender = M),
        w("KLOK-KE", "⏰", gender = F),
        w("TROM-ME", "🥁", gender = F),
        w("GI-TAR", "🎸", gender = M),
        w("KAK-TUS", "🌵", gender = M),
        w("BLY-ANT", "✏", gender = M),
        w("PEN-SEL", "🖌", gender = M),
        w("BAL-LONG", "🎈", gender = M),
        w("U-FO", "🛸"),
        w("TE-LE-SKOP", "🔭", gender = N),
        w("MAG-NET", "🧲", gender = M),
        w("BRANN-BIL", "🚒", gender = M),
        w("TRAK-TOR", "🚜", gender = M),
        w("HE-LI-KOP-TER", "🚁", gender = N),
        w("VUL-KAN", "🌋", gender = M),
        w("SNØ-MANN", "⛄", gender = M),
        w("FOT-BALL", "⚽", gender = M, tags = setOf(Word.BALL)),
        w("MO-BIL", "📱", gender = M),
        w("KA-ME-RA", "📷", gender = N),
        w("SÅ-PE", "🧼", gender = F),
        w("BA-DE-KAR", "🛁", gender = N),
        w("TER-NING", "🎲", gender = M),
        w("ME-DAL-JE", "🏅", gender = M),
        w("PO-KAL", "🏆", gender = M),
        w("BAT-TE-RI", "🔋", gender = N),
        w("HAM-MAR", "🔨", nb = "HAM-MER", gender = M),
        w("NA-SE", "👃", nb = "NE-SE", gender = F),
        w("HJAR-TE", "❤", nb = "HJER-TE", gender = N, tags = setOf(Word.IRREGULAR_START)),
    )

    private val byText: Map<String, Word> = words.associateBy { it.text(Maalform.NYNORSK) }

    fun get(nynorskText: String): Word = byText.getValue(nynorskText)

    /** Words spelled with exactly [letters] letters in the given standard. */
    fun withLength(maalform: Maalform, letters: IntRange): List<Word> =
        words.filter { it.text(maalform).length in letters }

    /**
     * Rhyme families. Only words spelled the same in both standards, so a rhyme never depends on
     * the profile's målform.
     */
    val rhymes: List<List<String>> = listOf(
        listOf("HUS", "MUS"),
        listOf("SOL", "STOL"),
        listOf("KATT", "HATT"),
        listOf("TANN", "MANN"),
        listOf("BJØRN", "ØRN"),
        listOf("HEST", "FEST"),
        listOf("FLY", "SKY"),
        listOf("SOKK", "KOKK"),
        listOf("IS", "GRIS", "RIS"),
        listOf("REV", "BREV"),
    )

    /** Two words that make a third. The picture belongs to the whole word. */
    data class Compound(val left: Txt, val right: Txt, val whole: Txt, val emoji: String)

    val compounds: List<Compound> = listOf(
        Compound(txt("SNØ"), txt("MANN"), txt("SNØMANN"), "⛄".asEmoji()),
        Compound(txt("FOT"), txt("BALL"), txt("FOTBALL"), "⚽".asEmoji()),
        Compound(txt("BRANN"), txt("BIL"), txt("BRANNBIL"), "🚒".asEmoji()),
        Compound(txt("SOL"), txt("BRILLER"), txt("SOLBRILLER"), "🕶".asEmoji()),
        Compound(txt("IS"), txt("BJØRN"), txt("ISBJØRN"), "🐻‍❄️"),
        Compound(txt("REGN"), txt("JAKKE"), txt("REGNJAKKE"), "🧥".asEmoji()),
        Compound(txt("BADE"), txt("KAR"), txt("BADEKAR"), "🛁".asEmoji()),
        Compound(txt("POP"), txt("KORN"), txt("POPKORN"), "🍿".asEmoji()),
        Compound(txt("SOL"), txt("SIKKE"), txt("SOLSIKKE"), "🌻".asEmoji()),
        Compound(txt("MARI"), txt("HØNE"), txt("MARIHØNE"), "🐞".asEmoji()),
        Compound(txt("BLEKK"), txt("SPRUT"), txt("BLEKKSPRUT"), "🐙".asEmoji()),
        Compound(txt("JORD"), txt("BÆR"), txt("JORDBÆR"), "🍓".asEmoji()),
        Compound(txt("REGN"), txt("BOGE", "BUE"), txt("REGNBOGE", "REGNBUE"), "🌈".asEmoji()),
        Compound(txt("SKULE", "SKOLE"), txt("SEKK"), txt("SKULESEKK", "SKOLESEKK"), "🎒".asEmoji()),
        Compound(txt("TANN"), txt("BØRSTE"), txt("TANNBØRSTE"), "🪥".asEmoji()),
    )

    /** Letters in the order Norwegian first grade usually meets them, grouped into learning sets. */
    val letterSets: List<List<String>> = listOf(
        listOf("A", "I", "O", "S", "L", "M"),
        listOf("E", "R", "N", "U", "T", "K"),
        listOf("V", "F", "H", "G", "D", "B", "P"),
        listOf("Å", "Ø", "Æ", "J", "Y"),
        listOf("C", "W", "Z", "X", "Q"),
    )

    val alphabet: List<String> = "ABCDEFGHIJKLMNOPQRSTUVWXYZÆØÅ".map { it.toString() }

    val vowels: Set<String> = setOf("A", "E", "I", "O", "U", "Y", "Æ", "Ø", "Å")

    /** Lower-case letters that are easy to mix up, used as distractors. */
    val lowerLookalikes: Map<Char, List<Char>> = mapOf(
        'b' to listOf('d', 'p', 'h'),
        'd' to listOf('b', 'q', 'p'),
        'p' to listOf('q', 'b', 'd'),
        'q' to listOf('p', 'g', 'd'),
        'm' to listOf('n', 'w', 'u'),
        'n' to listOf('m', 'u', 'h'),
        'u' to listOf('n', 'v', 'y'),
        'i' to listOf('l', 'j', 't'),
        'l' to listOf('i', 't', 'j'),
        'j' to listOf('i', 'g', 'y'),
        'g' to listOf('q', 'j', 'y'),
        'h' to listOf('n', 'b', 'k'),
        'e' to listOf('a', 'o', 'c'),
        'a' to listOf('o', 'e', 'd'),
        'o' to listOf('a', 'c', 'e'),
        'y' to listOf('v', 'j', 'g'),
        'v' to listOf('y', 'w', 'u'),
        'w' to listOf('v', 'm', 'u'),
        'æ' to listOf('ø', 'a', 'e'),
        'ø' to listOf('o', 'æ', 'å'),
        'å' to listOf('a', 'ø', 'æ'),
    )
}
