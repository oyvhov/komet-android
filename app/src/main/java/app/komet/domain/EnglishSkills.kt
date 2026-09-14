package app.komet.domain

import kotlin.random.Random

private fun english(
    id: String,
    title: Txt,
    detail: Txt,
    grade: Int,
    symbol: String,
    length: Int = 8,
    generate: (QuestionContext) -> Question,
) = Skill(id, Subject.ENGLISH, title, detail, grade, symbol, length, generate)

/** English to read aloud, marked for the English voice. */
private fun spoken(en: String) = "{en:$en}"

/** How a word is offered as an answer when it should be recognised: its colour, its digit or its picture. */
private fun EnglishWord.asPicture(): Option = when {
    color != null -> Option.Color(color)
    number != null -> numberLabel(number)
    else -> Option.Picture(emoji.orEmpty())
}

/** How a word is shown when its English name should be found. */
private fun EnglishWord.asVisual(): Visual = when {
    color != null -> Visual.ColorBlob(color)
    number != null -> Visual.Glyph(txt(number.toString()), GlyphKind.NUMBER)
    else -> Visual.Picture(emoji.orEmpty())
}

private fun EnglishWord.label() = Option.Label(txt(en), GlyphKind.WORD)

/** Distinct neighbours for [word]: no two options with the same picture, colour or meaning. */
private fun Random.others(word: EnglishWord, pool: List<EnglishWord>, count: Int): List<EnglishWord> =
    pool.filter { it.en != word.en && it.no.nn != word.no.nn && (it.emoji == null || it.emoji != word.emoji) }
        .shuffled(this)
        .take(count)

private fun meaning(word: EnglishWord) = txt("${word.en} = ${word.no.nn}", "${word.en} = ${word.no.nb}")

private fun reward(word: EnglishWord) = txt("${spoken(word.en)}. ${word.no.nn}.", "${spoken(word.en)}. ${word.no.nb}.")

/** Hear an English word and find its picture, colour or number. */
private fun listenSkill(id: String, title: Txt, detail: Txt, grade: Int, symbol: String, pool: () -> List<EnglishWord>, options: Int = 3, length: Int = 8) =
    english(id, title, detail, grade, symbol, length) { ctx ->
        val r = ctx.random
        val words = pool()
        val word = r.pick(words)
        val choices = (r.others(word, words, options - 1) + word).shuffled(r)
        Question(
            key = word.en,
            prompt = txt("Lytt og finn", "Lytt og finn"),
            visual = Visual.Listen(spoken = txt(spoken(word.en)), fallback = txt(word.en), kind = GlyphKind.WORD, sameAsFallback = false),
            answer = Answer.Choice(choices.map { it.asPicture() }, choices.indexOf(word)),
            speech = txt("Finn ${spoken(word.en)}."),
            reward = reward(word),
            explanation = meaning(word),
        )
    }

/** See a picture and choose its English word. */
private fun readSkill(id: String, title: Txt, detail: Txt, grade: Int, symbol: String, pool: () -> List<EnglishWord>) =
    english(id, title, detail, grade, symbol) { ctx ->
        val r = ctx.random
        val words = pool()
        val word = r.pick(words)
        Question(
            key = word.en,
            prompt = txt("Kva heiter det på engelsk?", "Hva heter det på engelsk?"),
            visual = word.asVisual(),
            answer = choiceOf(word, r.others(word, words, 2), r) { it.label() },
            reward = reward(word),
            explanation = meaning(word),
        )
    }

/** A Norwegian word to find in English, or the other way round. */
private fun translateSkill(id: String, title: Txt, grade: Int, symbol: String, toEnglish: Boolean, pool: () -> List<EnglishWord>) =
    english(id, title, if (toEnglish) txt("Frå norsk til engelsk", "Fra norsk til engelsk") else txt("Frå engelsk til norsk", "Fra engelsk til norsk"), grade, symbol) { ctx ->
        val r = ctx.random
        // Words that are the same in both languages (egg, pizza) would make the task pointless.
        val words = pool().filter { it.en != it.no.nn && it.en != it.no.nb }
        val word = r.pick(words)
        val others = r.others(word, words, 2)
        if (toEnglish) {
            Question(
                key = word.en,
                prompt = txt("Kva heiter «${word.no.nn}» på engelsk?", "Hva heter «${word.no.nb}» på engelsk?"),
                visual = Visual.Glyph(word.no, GlyphKind.WORD),
                answer = choiceOf(word, others, r) { it.label() },
                speech = txt("Kva heiter ${word.no.nn} på engelsk?", "Hva heter ${word.no.nb} på engelsk?"),
                reward = reward(word),
                explanation = meaning(word),
            )
        } else {
            Question(
                key = word.en,
                prompt = txt("Kva betyr ordet?", "Hva betyr ordet?"),
                visual = Visual.Glyph(txt(word.en), GlyphKind.WORD),
                answer = choiceOf(word, others, r) { Option.Label(it.no, GlyphKind.WORD) },
                speech = txt("Kva betyr ${spoken(word.en)}?", "Hva betyr ${spoken(word.en)}?"),
                reward = reward(word),
                explanation = meaning(word),
            )
        }
    }

/**
 * Phrases two ways: a situation with three things to say, or a phrase to hear with three situations
 * to match it to.
 */
private fun phraseSkill(id: String, title: Txt, detail: Txt, grade: Int, symbol: String, maxGrade: Int) =
    english(id, title, detail, grade, symbol, length = 6) { ctx ->
        val r = ctx.random
        val pool = EnglishContent.phrases.filter { it.grade <= maxGrade }
        val phrase = r.pick(pool)
        val others = pool.filter { it !== phrase }.shuffled(r).take(2)
        val reward = txt("${spoken(phrase.en)} ${phrase.no.nn}", "${spoken(phrase.en)} ${phrase.no.nb}")
        val explanation = txt("${phrase.en} = ${phrase.no.nn}", "${phrase.en} = ${phrase.no.nb}")
        if (r.nextBoolean()) {
            Question(
                key = "say:${phrase.en}",
                prompt = txt("Kva seier du?", "Hva sier du?"),
                visual = Visual.Stack(listOf(Visual.Picture(phrase.emoji), Visual.Story(phrase.situation, reading = false))),
                answer = choiceOf(phrase, others, r) { Option.Label(txt(it.en), GlyphKind.PLAIN) },
                speech = txt("${phrase.situation.nn} Kva seier du?", "${phrase.situation.nb} Hva sier du?"),
                reward = reward,
                explanation = explanation,
            )
        } else {
            Question(
                key = "hear:${phrase.en}",
                prompt = txt("Når seier du det?", "Når sier du det?"),
                visual = Visual.Listen(spoken = txt(spoken(phrase.en)), fallback = txt(phrase.en), kind = GlyphKind.PLAIN, sameAsFallback = false),
                answer = choiceOf(phrase, others, r) { Option.Picture(it.emoji) },
                speech = txt("Når seier du ${spoken(phrase.en)}", "Når sier du ${spoken(phrase.en)}"),
                reward = reward,
                explanation = explanation,
            )
        }
    }

/** Hear an English word and build it from letter tiles. */
private fun spellSkill(id: String, title: Txt, grade: Int, symbol: String, pool: () -> List<EnglishWord>) =
    english(id, title, txt("Høyr og stav", "Hør og stav"), grade, symbol, length = 6) { ctx ->
        val r = ctx.random
        val word = r.pick(pool())
        val letters = word.en.uppercase().map { it.toString() }
        val extra = ('A'..'Z').map { it.toString() }.filter { it !in letters }.shuffled(r).take(if (letters.size <= 3) 2 else 3)
        Question(
            key = word.en,
            prompt = txt("Stav ordet på engelsk"),
            visual = word.asVisual(),
            answer = Answer.Build(target = letters, tiles = (letters + extra).shuffled(r), kind = GlyphKind.LETTER),
            speech = txt("Stav ${spoken(word.en)}."),
            reward = reward(word),
            explanation = meaning(word),
        )
    }

object EnglishCurriculum {

    val chapters: List<Chapter> = listOf(
        Chapter(
            id = "e_hello",
            subject = Subject.ENGLISH,
            title = txt("Hello!"),
            look = PlanetLook(0xFF6FD58A, 0xFFD5F7DD, 0xFF2E8A4B, craters = true),
            skills = listOf(
                phraseSkill("e_hello1", txt("Hei og takk"), txt("Hello, goodbye, thank you"), 0, "👋", maxGrade = 0),
                phraseSkill("e_hello2", txt("God natt"), txt("Morgon, kveld og bursdag", "Morgen, kveld og bursdag"), 1, "🌙", maxGrade = 1),
                phraseSkill("e_hello3", txt("Snakk engelsk"), txt("Please, sorry, how are you?"), 2, "💬", maxGrade = 3),
            ),
        ),
        Chapter(
            id = "e_colours",
            subject = Subject.ENGLISH,
            title = txt("Colours"),
            look = PlanetLook(0xFFFF8FB1, 0xFFFFDCE7, 0xFFB83E6B, bands = true),
            skills = listOf(
                listenSkill("e_colours_listen", txt("Høyr fargane", "Hør fargene"), txt("Red, blue, green …"), 0, "🎨", { EnglishContent.colours }),
                readSkill("e_colours_read", txt("Les fargane", "Les fargene"), txt("Kva farge er det?", "Hvilken farge er det?"), 1, "Red", { EnglishContent.colours }),
            ),
        ),
        Chapter(
            id = "e_numbers",
            subject = Subject.ENGLISH,
            title = txt("Numbers"),
            look = PlanetLook(0xFF8EC5FF, 0xFFDCEBFF, 0xFF3A6FC2, ring = true),
            skills = listOf(
                listenSkill("e_numbers5", txt("One, two, three"), txt("Tal til 5", "Tall til 5"), 0, "1-5", { EnglishContent.numbers.take(5) }, length = 5),
                listenSkill("e_numbers10", txt("Tal til ten", "Tall til ten"), txt("Tal til 10", "Tall til 10"), 1, "10", { EnglishContent.numbers }, options = 4),
                readSkill("e_numbers_read", txt("Les tala", "Les tallene"), txt("Frå 1 til ten", "Fra 1 til ten"), 2, "Ten", { EnglishContent.numbers }),
            ),
        ),
        Chapter(
            id = "e_animals",
            subject = Subject.ENGLISH,
            title = txt("Animals"),
            look = PlanetLook(0xFFFFC46B, 0xFFFFEBC9, 0xFFB5751F, bands = true, craters = true),
            skills = listOf(
                listenSkill("e_animals_listen", txt("Høyr dyra", "Hør dyrene"), txt("Cat, dog, cow …"), 0, "🐶", { EnglishContent.animals }),
                readSkill("e_animals_read", txt("Les dyra", "Les dyrene"), txt("Kva dyr er det?", "Hvilket dyr er det?"), 1, "Cat", { EnglishContent.animals }),
                translateSkill("e_animals_translate", txt("Dyr på engelsk"), 2, "?", toEnglish = true, pool = { EnglishContent.animals }),
            ),
        ),
        Chapter(
            id = "e_food",
            subject = Subject.ENGLISH,
            title = txt("Food"),
            look = PlanetLook(0xFFFF7A6B, 0xFFFFD9D2, 0xFFB23A2C, craters = true),
            skills = listOf(
                listenSkill("e_food_listen", txt("Høyr maten", "Hør maten"), txt("Apple, milk, pizza …"), 1, "🍎", { EnglishContent.food }),
                readSkill("e_food_read", txt("Les maten", "Les maten"), txt("Kva mat er det?", "Hvilken mat er det?"), 2, "Egg", { EnglishContent.food }),
            ),
        ),
        Chapter(
            id = "e_me",
            subject = Subject.ENGLISH,
            title = txt("Me and my family"),
            look = PlanetLook(0xFFB39BFF, 0xFFE7DFFF, 0xFF5F48B8, ring = true, bands = true),
            skills = listOf(
                listenSkill("e_body_listen", txt("Kroppen"), txt("Eye, nose, hand …"), 1, "👃", { EnglishContent.body }),
                listenSkill("e_family_listen", txt("Familien"), txt("Mum, dad, baby …"), 1, "👪", { EnglishContent.family }),
                listenSkill("e_clothes_listen", txt("Kleda", "Klærne"), txt("Hat, shoe, sock …"), 2, "👕", { EnglishContent.clothes }),
                readSkill("e_me_read", txt("Les om meg", "Les om meg"), txt("Kropp, familie og klede", "Kropp, familie og klær"), 3, "Me", { EnglishContent.body + EnglishContent.family + EnglishContent.clothes }),
            ),
        ),
        Chapter(
            id = "e_things",
            subject = Subject.ENGLISH,
            title = txt("Things"),
            look = PlanetLook(0xFF67D9D0, 0xFFD2F6F2, 0xFF238A82, craters = true),
            skills = listOf(
                listenSkill("e_things_listen", txt("Ting rundt meg"), txt("Ball, car, book …"), 1, "🚲", { EnglishContent.things }),
                translateSkill("e_things_translate", txt("Kva betyr det?", "Hva betyr det?"), 2, "Car", toEnglish = false, pool = { EnglishContent.things + EnglishContent.food }),
            ),
        ),
        Chapter(
            id = "e_spell",
            subject = Subject.ENGLISH,
            title = txt("Spell it!"),
            look = PlanetLook(0xFFFFE066, 0xFFFFF5C2, 0xFFB89414, ring = true),
            skills = listOf(
                spellSkill("e_spell3", txt("Stav korte ord"), 2, "ABC", { EnglishContent.spellShort }),
                spellSkill("e_spell5", txt("Stav lengre ord"), 3, "ABCD", { EnglishContent.spellLong }),
            ),
        ),
    )
}
