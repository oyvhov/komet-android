package app.komet.domain

enum class EnglishTheme { COLOURS, NUMBERS, ANIMALS, FOOD, BODY, CLOTHES, FAMILY, THINGS }

/**
 * An English word with its Norwegian meaning and one way to show it: a picture, a colour or a number.
 * [en] is written as English children's books write it, in small letters.
 */
data class EnglishWord(
    val en: String,
    val no: Txt,
    val theme: EnglishTheme,
    val emoji: String? = null,
    val color: Long? = null,
    val number: Int? = null,
)

/** Something to say in a situation, e.g. «Thank you!» when you get a present. */
data class EnglishPhrase(val en: String, val no: Txt, val emoji: String, val situation: Txt, val grade: Int)

object EnglishContent {

    private fun word(en: String, nn: String, nb: String = nn, theme: EnglishTheme, emoji: String) =
        EnglishWord(en, txt(nn, nb), theme, emoji = emoji.asEmoji())

    val colours: List<EnglishWord> = listOf(
        EnglishWord("red", txt("raud", "rød"), EnglishTheme.COLOURS, color = 0xFFE53935),
        EnglishWord("blue", txt("blå"), EnglishTheme.COLOURS, color = 0xFF1E88E5),
        EnglishWord("green", txt("grøn", "grønn"), EnglishTheme.COLOURS, color = 0xFF43A047),
        EnglishWord("yellow", txt("gul"), EnglishTheme.COLOURS, color = 0xFFFDD835),
        EnglishWord("orange", txt("oransje"), EnglishTheme.COLOURS, color = 0xFFFB8C00),
        EnglishWord("purple", txt("lilla"), EnglishTheme.COLOURS, color = 0xFF8E24AA),
        EnglishWord("pink", txt("rosa"), EnglishTheme.COLOURS, color = 0xFFF06292),
        EnglishWord("black", txt("svart"), EnglishTheme.COLOURS, color = 0xFF212121),
        EnglishWord("white", txt("kvit", "hvit"), EnglishTheme.COLOURS, color = 0xFFFFFFFF),
        EnglishWord("brown", txt("brun"), EnglishTheme.COLOURS, color = 0xFF795548),
        EnglishWord("grey", txt("grå"), EnglishTheme.COLOURS, color = 0xFF9E9E9E),
    )

    val numbers: List<EnglishWord> = listOf(
        "one" to txt("ein", "en"), "two" to txt("to"), "three" to txt("tre"), "four" to txt("fire"), "five" to txt("fem"),
        "six" to txt("seks"), "seven" to txt("sju"), "eight" to txt("åtte"), "nine" to txt("ni"), "ten" to txt("ti"),
    ).mapIndexed { index, (en, no) -> EnglishWord(en, no, EnglishTheme.NUMBERS, number = index + 1) }

    val animals: List<EnglishWord> = listOf(
        word("cat", "katt", theme = EnglishTheme.ANIMALS, emoji = "🐱"),
        word("dog", "hund", theme = EnglishTheme.ANIMALS, emoji = "🐶"),
        word("cow", "ku", theme = EnglishTheme.ANIMALS, emoji = "🐮"),
        word("horse", "hest", theme = EnglishTheme.ANIMALS, emoji = "🐴"),
        word("pig", "gris", theme = EnglishTheme.ANIMALS, emoji = "🐷"),
        word("sheep", "sau", theme = EnglishTheme.ANIMALS, emoji = "🐑"),
        word("bird", "fugl", theme = EnglishTheme.ANIMALS, emoji = "🐦"),
        word("fish", "fisk", theme = EnglishTheme.ANIMALS, emoji = "🐟"),
        word("mouse", "mus", theme = EnglishTheme.ANIMALS, emoji = "🐭"),
        word("rabbit", "kanin", theme = EnglishTheme.ANIMALS, emoji = "🐰"),
        word("lion", "løve", theme = EnglishTheme.ANIMALS, emoji = "🦁"),
        word("elephant", "elefant", theme = EnglishTheme.ANIMALS, emoji = "🐘"),
        word("monkey", "ape", theme = EnglishTheme.ANIMALS, emoji = "🐒"),
        word("bear", "bjørn", theme = EnglishTheme.ANIMALS, emoji = "🐻"),
        word("frog", "frosk", theme = EnglishTheme.ANIMALS, emoji = "🐸"),
        word("duck", "and", theme = EnglishTheme.ANIMALS, emoji = "🦆"),
        word("snake", "slange", theme = EnglishTheme.ANIMALS, emoji = "🐍"),
    )

    val food: List<EnglishWord> = listOf(
        word("apple", "eple", theme = EnglishTheme.FOOD, emoji = "🍎"),
        word("banana", "banan", theme = EnglishTheme.FOOD, emoji = "🍌"),
        word("bread", "brød", theme = EnglishTheme.FOOD, emoji = "🍞"),
        word("milk", "mjølk", "melk", EnglishTheme.FOOD, "🥛"),
        word("cheese", "ost", theme = EnglishTheme.FOOD, emoji = "🧀"),
        word("egg", "egg", theme = EnglishTheme.FOOD, emoji = "🥚"),
        word("pizza", "pizza", theme = EnglishTheme.FOOD, emoji = "🍕"),
        word("ice cream", "is", theme = EnglishTheme.FOOD, emoji = "🍦"),
        word("cake", "kake", theme = EnglishTheme.FOOD, emoji = "🍰"),
        word("carrot", "gulrot", theme = EnglishTheme.FOOD, emoji = "🥕"),
        word("strawberry", "jordbær", theme = EnglishTheme.FOOD, emoji = "🍓"),
        word("water", "vatn", "vann", EnglishTheme.FOOD, "💧"),
    )

    val body: List<EnglishWord> = listOf(
        word("eye", "auge", "øye", EnglishTheme.BODY, "👁"),
        word("ear", "øyre", "øre", EnglishTheme.BODY, "👂"),
        word("nose", "nase", "nese", EnglishTheme.BODY, "👃"),
        word("mouth", "munn", theme = EnglishTheme.BODY, emoji = "👄"),
        word("hand", "hand", "hånd", EnglishTheme.BODY, "✋"),
        word("foot", "fot", theme = EnglishTheme.BODY, emoji = "🦶"),
        word("leg", "bein", "ben", EnglishTheme.BODY, "🦵"),
        word("arm", "arm", theme = EnglishTheme.BODY, emoji = "💪"),
        word("tooth", "tann", theme = EnglishTheme.BODY, emoji = "🦷"),
    )

    val clothes: List<EnglishWord> = listOf(
        word("hat", "hatt", theme = EnglishTheme.CLOTHES, emoji = "🎩"),
        word("cap", "caps", theme = EnglishTheme.CLOTHES, emoji = "🧢"),
        word("shoe", "sko", theme = EnglishTheme.CLOTHES, emoji = "👟"),
        word("sock", "sokk", theme = EnglishTheme.CLOTHES, emoji = "🧦"),
        word("T-shirt", "t-skjorte", theme = EnglishTheme.CLOTHES, emoji = "👕"),
        word("dress", "kjole", theme = EnglishTheme.CLOTHES, emoji = "👗"),
        word("jacket", "jakke", theme = EnglishTheme.CLOTHES, emoji = "🧥"),
        word("trousers", "bukse", theme = EnglishTheme.CLOTHES, emoji = "👖"),
        word("scarf", "skjerf", theme = EnglishTheme.CLOTHES, emoji = "🧣"),
        word("gloves", "hanskar", "hansker", EnglishTheme.CLOTHES, "🧤"),
    )

    val family: List<EnglishWord> = listOf(
        word("mum", "mamma", theme = EnglishTheme.FAMILY, emoji = "👩"),
        word("dad", "pappa", theme = EnglishTheme.FAMILY, emoji = "👨"),
        word("baby", "baby", theme = EnglishTheme.FAMILY, emoji = "👶"),
        word("sister", "syster", "søster", EnglishTheme.FAMILY, "👧"),
        word("brother", "bror", theme = EnglishTheme.FAMILY, emoji = "👦"),
        word("grandma", "bestemor", theme = EnglishTheme.FAMILY, emoji = "👵"),
        word("grandpa", "bestefar", theme = EnglishTheme.FAMILY, emoji = "👴"),
    )

    val things: List<EnglishWord> = listOf(
        word("ball", "ball", theme = EnglishTheme.THINGS, emoji = "⚽"),
        word("car", "bil", theme = EnglishTheme.THINGS, emoji = "🚗"),
        word("book", "bok", theme = EnglishTheme.THINGS, emoji = "📖"),
        word("bike", "sykkel", theme = EnglishTheme.THINGS, emoji = "🚲"),
        word("boat", "båt", theme = EnglishTheme.THINGS, emoji = "⛵"),
        word("train", "tog", theme = EnglishTheme.THINGS, emoji = "🚂"),
        word("plane", "fly", theme = EnglishTheme.THINGS, emoji = "✈"),
        word("house", "hus", theme = EnglishTheme.THINGS, emoji = "🏠"),
        word("tree", "tre", theme = EnglishTheme.THINGS, emoji = "🌳"),
        word("sun", "sol", theme = EnglishTheme.THINGS, emoji = "☀"),
        word("moon", "måne", theme = EnglishTheme.THINGS, emoji = "🌙"),
        word("star", "stjerne", theme = EnglishTheme.THINGS, emoji = "⭐"),
        word("rocket", "rakett", theme = EnglishTheme.THINGS, emoji = "🚀"),
        word("flower", "blome", "blomst", EnglishTheme.THINGS, "🌸"),
        word("bus", "buss", theme = EnglishTheme.THINGS, emoji = "🚌"),
    )

    val allWords: List<EnglishWord> get() = colours + numbers + animals + food + body + clothes + family + things

    val phrases: List<EnglishPhrase> = listOf(
        EnglishPhrase("Hello!", txt("Hei!"), "👋", txt("Du møter ein venn.", "Du møter en venn."), 0),
        EnglishPhrase("Goodbye!", txt("Ha det!"), "🚪", txt("Du går heim frå ein venn.", "Du går hjem fra en venn."), 0),
        EnglishPhrase("Thank you!", txt("Takk!"), "🎁", txt("Du får ei gåve.", "Du får en gave."), 0),
        EnglishPhrase("Good morning!", txt("God morgon!", "God morgen!"), "🌅", txt("Du står opp om morgonen.", "Du står opp om morgenen."), 1),
        EnglishPhrase("Good night!", txt("God natt!"), "🌙", txt("Du skal leggje deg.", "Du skal legge deg."), 1),
        EnglishPhrase("Happy birthday!", txt("Gratulerer med dagen!"), "🎂", txt("Venen din har bursdag.", "Vennen din har bursdag."), 1),
        EnglishPhrase("Sorry!", txt("Orsak!", "Unnskyld!"), "🙇", txt("Du dyttar borti nokon.", "Du dytter borti noen."), 2),
        EnglishPhrase("Please!", txt("Ver så snill!", "Vær så snill!"), "🙏", txt("Du vil gjerne låne ein blyant.", "Du vil gjerne låne en blyant."), 2),
        EnglishPhrase("How are you?", txt("Korleis har du det?", "Hvordan har du det?"), "🙂", txt("Du vil vite korleis venen din har det.", "Du vil vite hvordan vennen din har det."), 2),
        EnglishPhrase("I'm fine, thank you!", txt("Eg har det bra, takk!", "Jeg har det bra, takk!"), "😊", txt("Nokon spør korleis du har det.", "Noen spør hvordan du har det."), 3),
        EnglishPhrase("What's your name?", txt("Kva heiter du?", "Hva heter du?"), "🧒", txt("Du vil vite namnet til ein ny venn.", "Du vil vite navnet til en ny venn."), 3),
    ).map { it.copy(emoji = it.emoji.asEmoji()) }

    /** Words short enough to spell with letter tiles, by length. */
    val spellShort: List<EnglishWord> get() = (animals + food + things + colours).filter { it.en.length == 3 && it.en.all(Char::isLetter) }

    val spellLong: List<EnglishWord> get() = (animals + food + things + colours + clothes).filter { it.en.length in 4..5 && it.en.all(Char::isLetter) }
}
