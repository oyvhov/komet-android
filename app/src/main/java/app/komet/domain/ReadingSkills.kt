package app.komet.domain

import kotlin.random.Random

private fun reading(
    id: String,
    title: Txt,
    detail: Txt,
    grade: Int,
    symbol: String,
    length: Int = 8,
    generate: (QuestionContext) -> Question,
) = Skill(id, Subject.READING, title, detail, grade, symbol, length, generate)

private fun letter(value: String) = Option.Label(txt(value), GlyphKind.LETTER)

/** Letters that are rare in Norwegian words and only confuse early distractor sets. */
private val rareLetters = setOf("C", "Q", "W", "X", "Z")

/** Letters whose sounds are easy to mix up, used to make the harder first-sound level harder. */
private val soundPairs: Map<String, List<String>> = mapOf(
    "B" to listOf("P", "D"), "P" to listOf("B", "T"), "D" to listOf("T", "B"), "T" to listOf("D", "K"),
    "G" to listOf("K", "J"), "K" to listOf("G", "T"), "F" to listOf("V", "H"), "V" to listOf("F", "B"),
    "M" to listOf("N", "B"), "N" to listOf("M", "L"), "S" to listOf("F", "Z"), "L" to listOf("R", "N"),
    "R" to listOf("L", "N"), "E" to listOf("I", "Æ"), "I" to listOf("E", "Y"), "O" to listOf("Å", "U"),
    "U" to listOf("Y", "O"), "Y" to listOf("U", "I"), "Å" to listOf("O", "A"), "Ø" to listOf("Y", "E"),
    "Æ" to listOf("E", "A"), "A" to listOf("Æ", "Å"), "H" to listOf("K", "F"), "J" to listOf("G", "I"),
)

private fun lower(text: String) = text.lowerNo()

private fun Random.otherLetters(exclude: Collection<String>, count: Int, pool: List<String> = WordBank.alphabet): List<String> =
    pool.filter { it !in exclude && it !in rareLetters }.shuffled(this).take(count)

/** Picture words that can be offered together with [target] without two pictures fitting one word. */
private fun distractorWords(target: Word, maalform: Maalform, random: Random, count: Int, preferSameStart: Boolean): List<Word> {
    val text = target.text(maalform)
    val usable = WordBank.words.filter {
        it !== target && it.emoji != target.emoji && it.text(maalform) != text &&
            !(Word.BALL in target.tags && Word.BALL in it.tags)
    }
    val sameStart = if (preferSameStart) usable.filter { it.text(maalform).first() == text.first() }.shuffled(random) else emptyList()
    val rest = usable.filter { it !in sameStart }.shuffled(random)
    return (sameStart + rest).take(count)
}

private fun lettersSkill(index: Int, id: String, grade: Int): Skill {
    val set = WordBank.letterSets[index]
    return reading(
        id = id,
        title = txt("Bokstavar ${index + 1}", "Bokstaver ${index + 1}"),
        detail = txt(set.joinToString(", ")),
        grade = grade,
        symbol = set.first(),
    ) { ctx ->
        val r = ctx.random
        val known = WordBank.letterSets.take(index + 1).flatten()
        val target = r.pick(set)
        val count = if (index == 0) 3 else 4
        val others = known.filter { it != target }.shuffled(r).take(count - 1)
        Question(
            // Small letter sets repeat a letter within a round; a new set of neighbours keeps it a new task.
            key = target + others.sorted().joinToString(""),
            prompt = txt("Finn bokstaven du høyrer", "Finn bokstaven du hører"),
            visual = Visual.Listen(spoken = txt("$target."), fallback = txt(target), kind = GlyphKind.LETTER),
            answer = choiceOf(target, others, r, ::letter),
            speech = txt("Finn bokstaven $target."),
            reward = txt("$target."),
        )
    }
}

private fun firstSoundSkill(id: String, hard: Boolean) = reading(
    id = id,
    title = if (hard) txt("Første lyd 2") else txt("Første lyd"),
    detail = txt("Kva lyd byrjar ordet med?", "Hvilken lyd begynner ordet med?"),
    grade = if (hard) 1 else 0,
    symbol = "🔊",
) { ctx ->
    val r = ctx.random
    val m = ctx.maalform
    val word = r.pick(WordBank.words.filter { !it.irregularStart && it.text(m).length in 2..7 })
    val text = word.text(m)
    val first = text.first().toString()
    val others = if (hard) {
        val confusable = soundPairs[first].orEmpty().filter { it != first }
        (confusable.shuffled(r).take(2) + r.otherLetters(confusable + first, 3)).distinct().filter { it != first }.take(3)
    } else {
        r.otherLetters(listOf(first), 2)
    }
    Question(
        key = text,
        prompt = txt("Kva lyd byrjar ordet med?", "Hvilken lyd begynner ordet med?"),
        visual = Visual.Picture(word.emoji, word = text, revealWord = true),
        answer = choiceOf(first, others, r, ::letter),
        speech = txt("Kva lyd byrjar ${lower(text)} med?", "Hvilken lyd begynner ${lower(text)} med?"),
        reward = txt(lower(text)),
    )
}

private val lastSoundExcluded = listOf("ND", "LD", "RD", "NG", "RT", "RN", "RS", "RL")

object ReadingCurriculum {

    val chapters: List<Chapter> = listOf(
        Chapter(
            id = "r_bokstavar",
            subject = Subject.READING,
            title = txt("Bokstavar", "Bokstaver"),
            look = PlanetLook(0xFF5CE1E6, 0xFFC9F7F8, 0xFF1F8F9C, craters = true),
            skills = listOf(
                lettersSkill(0, "r_letters1", 0),
                lettersSkill(1, "r_letters2", 0),
                lettersSkill(2, "r_letters3", 1),
                reading("r_case", txt("Store og små"), txt("A og a"), 1, "Aa") { ctx ->
                    val r = ctx.random
                    val pool = "ABDEFGHIJKLMNPQRTUYÆØÅ".map { it.toString() }
                    val upper = r.pick(pool)
                    val lowerLetter = upper.lowerNo()
                    val toLower = r.nextBoolean()
                    val lookalikes = WordBank.lowerLookalikes[lowerLetter.first()].orEmpty().map { it.toString() }
                    val extra = pool.map { it.lowerNo() }.filter { it != lowerLetter && it !in lookalikes }.shuffled(r)
                    val lowerOthers = (lookalikes + extra).distinct().take(3)
                    val (shown, target, others) = if (toLower) {
                        Triple(upper, lowerLetter, lowerOthers)
                    } else {
                        Triple(lowerLetter, upper, lowerOthers.map { it.upperNo() })
                    }
                    Question(
                        key = "$shown$toLower",
                        prompt = if (toLower) txt("Finn den vesle bokstaven", "Finn den lille bokstaven") else txt("Finn den store bokstaven"),
                        visual = Visual.Glyph(txt(shown), GlyphKind.EXACT),
                        answer = choiceOf(target, others, r) { Option.Label(txt(it), GlyphKind.EXACT) },
                        reward = txt("$upper."),
                        explanation = txt("${upper}${lowerLetter}"),
                    )
                },
                reading("r_vowels", txt("Vokalar", "Vokaler"), txt("A, E, I, O, U, Y, Æ, Ø, Å"), 1, "E") { ctx ->
                    val r = ctx.random
                    val vowel = r.pick(WordBank.vowels.toList())
                    val consonants = r.otherLetters(WordBank.vowels, 3)
                    Question(
                        key = vowel + consonants.joinToString(""),
                        prompt = txt("Trykk på vokalen"),
                        visual = Visual.None,
                        answer = choiceOf(vowel, consonants, r, ::letter),
                        reward = txt("$vowel."),
                        explanation = txt("Vokalane er A, E, I, O, U, Y, Æ, Ø og Å.", "Vokalene er A, E, I, O, U, Y, Æ, Ø og Å."),
                    )
                },
                lettersSkill(3, "r_letters4", 1),
                reading("r_abc", txt("Alfabetet"), txt("Kva kjem før og etter?", "Hva kommer før og etter?"), 1, "ABC") { ctx ->
                    val r = ctx.random
                    val alphabet = WordBank.alphabet
                    val i = r.between(1, alphabet.size - 2)
                    val askNext = r.nextBoolean()
                    val target = if (askNext) alphabet[i + 1] else alphabet[i - 1]
                    val shown = if (askNext) "${alphabet[i - 1]}  ${alphabet[i]}  ?" else "?  ${alphabet[i]}  ${alphabet[i + 1]}"
                    val neighbours = listOfNotNull(alphabet.getOrNull(i + 2), alphabet.getOrNull(i - 2), alphabet[i]).filter { it != target }
                    val others = (neighbours.shuffled(r).take(2) + r.otherLetters(neighbours + target, 1)).distinct().take(3)
                    Question(
                        key = "$i$askNext",
                        prompt = txt("Kva bokstav manglar?", "Hvilken bokstav mangler?"),
                        visual = Visual.Glyph(txt(shown), GlyphKind.LETTER),
                        answer = choiceOf(target, others, r, ::letter),
                        speech = if (askNext) {
                            txt("Kva bokstav kjem etter ${alphabet[i]}?", "Hvilken bokstav kommer etter ${alphabet[i]}?")
                        } else {
                            txt("Kva bokstav kjem før ${alphabet[i]}?", "Hvilken bokstav kommer før ${alphabet[i]}?")
                        },
                        reward = txt("$target."),
                    )
                },
                lettersSkill(4, "r_letters5", 2),
            ),
        ),
        Chapter(
            id = "r_lydar",
            subject = Subject.READING,
            title = txt("Lydar og rim", "Lyder og rim"),
            look = PlanetLook(0xFF8BE38B, 0xFFD8F8D2, 0xFF3A9A4A, bands = true),
            skills = listOf(
                firstSoundSkill("r_first1", hard = false),
                firstSoundSkill("r_first2", hard = true),
                reading("r_last", txt("Siste lyd"), txt("Kva lyd sluttar ordet med?", "Hvilken lyd slutter ordet med?"), 1, "🔚") { ctx ->
                    val r = ctx.random
                    val m = ctx.maalform
                    val word = r.pick(
                        WordBank.words.filter { w ->
                            val t = w.text(m)
                            !w.silentEnd && t.length in 3..7 && lastSoundExcluded.none { t.endsWith(it) }
                        },
                    )
                    val text = word.text(m)
                    val last = text.last().toString()
                    Question(
                        key = text,
                        prompt = txt("Kva lyd sluttar ordet med?", "Hvilken lyd slutter ordet med?"),
                        visual = Visual.Picture(word.emoji, word = text, revealWord = true),
                        answer = choiceOf(last, r.otherLetters(listOf(last, text.first().toString()), 2), r, ::letter),
                        speech = txt("Kva lyd sluttar ${lower(text)} med?", "Hvilken lyd slutter ${lower(text)} med?"),
                        reward = txt(lower(text)),
                    )
                },
                reading("r_missing", txt("Hol i ordet", "Hull i ordet"), txt("Finn bokstaven som manglar", "Finn bokstaven som mangler"), 1, "_") { ctx ->
                    val r = ctx.random
                    val m = ctx.maalform
                    val word = r.pick(WordBank.withLength(m, 3..5))
                    val text = word.text(m)
                    val vowelPositions = text.indices.filter { text[it].toString() in WordBank.vowels }
                    val index = if (vowelPositions.isNotEmpty() && r.chance(0.6)) r.pick(vowelPositions) else r.nextInt(text.length)
                    val missing = text[index].toString()
                    val pool = if (missing in WordBank.vowels) WordBank.vowels.toList() else WordBank.alphabet.filter { it !in WordBank.vowels }
                    Question(
                        key = "$text$index",
                        prompt = txt("Kva bokstav manglar?", "Hvilken bokstav mangler?"),
                        visual = Visual.Picture(word.emoji, word = text, hideIndex = index),
                        answer = choiceOf(missing, r.otherLetters(listOf(missing), 2, pool), r, ::letter),
                        speech = txt("Kva bokstav manglar i ${lower(text)}?", "Hvilken bokstav mangler i ${lower(text)}?"),
                        reward = txt(lower(text)),
                    )
                },
                reading("r_syllables", txt("Stavingar", "Stavelser"), txt("Klapp og tel", "Klapp og tell"), 1, "👏") { ctx ->
                    val r = ctx.random
                    val m = ctx.maalform
                    val wanted = r.between(1, 4)
                    val candidates = WordBank.words.filter { it.syllables(m).size == wanted }
                    val word = r.pick(candidates.ifEmpty { WordBank.words })
                    val parts = word.syllables(m)
                    val text = word.text(m)
                    Question(
                        key = text,
                        prompt = txt("Kor mange stavingar?", "Hvor mange stavelser?"),
                        visual = Visual.Picture(word.emoji, word = text),
                        answer = Answer.Choice((1..4).map { Option.Claps(it) }, parts.size - 1),
                        speech = txt("Klapp og tel. Kor mange stavingar har ${lower(text)}?", "Klapp og tell. Hvor mange stavelser har ${lower(text)}?"),
                        reward = txt(parts.joinToString(" – ") { lower(it) }),
                        explanation = txt(parts.joinToString(" – ") { it.inCase(ctx.letterCase) }),
                    )
                },
                reading("r_rhyme", txt("Rim"), txt("Ord som rimar", "Ord som rimer"), 1, "♪") { ctx ->
                    val r = ctx.random
                    val m = ctx.maalform
                    val family = r.pick(WordBank.rhymes)
                    val (targetText, matchText) = family.shuffled(r).take(2)
                    val target = WordBank.get(targetText)
                    val match = WordBank.get(matchText)
                    val ending = targetText.takeLast(2)
                    val others = WordBank.words.filter {
                        val t = it.text(m)
                        it.sameInBoth && t !in family && t.length <= 6 && !t.endsWith(ending) && it.emoji != target.emoji
                    }.shuffled(r).take(2)
                    val options = (others + match).shuffled(r)
                    val spokenOptions = spokenList(options.map { lower(it.text(m)) })
                    Question(
                        key = "$targetText$matchText",
                        prompt = txt("Finn ordet som rimar", "Finn ordet som rimer"),
                        visual = Visual.Picture(target.emoji, word = targetText),
                        answer = Answer.Choice(options.map { Option.Picture(it.emoji, it.text(m)) }, options.indexOf(match)),
                        speech = txt("Kva rimar på ${lower(targetText)}? $spokenOptions?", "Hva rimer på ${lower(targetText)}? $spokenOptions?"),
                        reward = txt("${lower(targetText)}, ${lower(matchText)}"),
                    )
                },
            ),
        ),
        Chapter(
            id = "r_ord",
            subject = Subject.READING,
            title = txt("Ord"),
            look = PlanetLook(0xFF67B7FF, 0xFFD3E9FF, 0xFF2769C2, ring = true),
            skills = listOf(
                buildWordSkill("r_build3", txt("Skriv korte ord"), 0, 2..3),
                reading("r_word_pic", txt("Les og finn"), txt("Les ordet, finn biletet", "Les ordet, finn bildet"), 1, "📖") { ctx ->
                    val r = ctx.random
                    val m = ctx.maalform
                    val word = r.pick(WordBank.words.filter { it.text(m).length in 2..6 })
                    val text = word.text(m)
                    val options = (distractorWords(word, m, r, 2, preferSameStart = true) + word).shuffled(r)
                    Question(
                        key = text,
                        prompt = txt("Les ordet. Finn biletet.", "Les ordet. Finn bildet."),
                        visual = Visual.Glyph(txt(text), GlyphKind.WORD),
                        answer = Answer.Choice(options.map { Option.Picture(it.emoji) }, options.indexOf(word)),
                        reward = txt(lower(text)),
                    )
                },
                reading("r_pic_word", txt("Kva står det?", "Hva står det?"), txt("Vel rett ord", "Velg riktig ord"), 1, "Ord") { ctx ->
                    val r = ctx.random
                    val m = ctx.maalform
                    val word = r.pick(WordBank.words.filter { it.text(m).length in 3..7 })
                    val text = word.text(m)
                    val options = (distractorWords(word, m, r, 2, preferSameStart = true) + word).shuffled(r)
                    Question(
                        key = text,
                        prompt = txt("Kva ord passar til biletet?", "Hvilket ord passer til bildet?"),
                        visual = Visual.Picture(word.emoji),
                        answer = Answer.Choice(options.map { Option.Label(txt(it.text(m)), GlyphKind.WORD) }, options.indexOf(word)),
                        reward = txt(lower(text)),
                    )
                },
                buildWordSkill("r_build4", txt("Skriv ord med 4"), 1, 4..4),
                reading("r_sight", txt("Småord"), txt("Eg, og, på, er …", "Jeg, og, på, er …"), 1, "og") { ctx ->
                    val r = ctx.random
                    val m = ctx.maalform
                    val words = ReadingContent.sightWords.map { it.get(m) }.distinct()
                    val target = r.pick(words)
                    val similar = words.filter { it != target && (it.first() == target.first() || it.length == target.length) }.shuffled(r)
                    val others = (similar + words.filter { it != target }.shuffled(r)).distinct().filter { it != target }.take(3)
                    Question(
                        key = target,
                        prompt = txt("Finn ordet du høyrer", "Finn ordet du hører"),
                        visual = Visual.Listen(spoken = txt(target), fallback = txt(target), kind = GlyphKind.WORD),
                        answer = choiceOf(target, others, r) { Option.Label(txt(it), GlyphKind.WORD) },
                        speech = txt("Finn ordet: $target."),
                        reward = txt("$target."),
                    )
                },
                buildWordSkill("r_build5", txt("Skriv lange ord"), 2, 5..8),
                reading("r_compound", txt("To ord blir eitt", "To ord blir ett"), txt("Samansette ord", "Sammensatte ord"), 2, "+") { ctx ->
                    val r = ctx.random
                    val compound = r.pick(WordBank.compounds)
                    val others = WordBank.compounds.filter { it !== compound }.shuffled(r).take(2)
                    val options = (others + compound).shuffled(r)
                    Question(
                        key = compound.whole.nn,
                        prompt = txt("Set saman orda. Finn biletet.", "Sett sammen ordene. Finn bildet."),
                        visual = Visual.Glyph(txt("${compound.left.nn} + ${compound.right.nn}", "${compound.left.nb} + ${compound.right.nb}"), GlyphKind.WORD),
                        answer = Answer.Choice(options.map { Option.Picture(it.emoji) }, options.indexOf(compound)),
                        reward = compound.whole.map(::lower),
                        explanation = compound.whole.map { it.inCase(ctx.letterCase) },
                    )
                },
            ),
        ),
        Chapter(
            id = "r_setningar",
            subject = Subject.READING,
            title = txt("Setningar", "Setninger"),
            look = PlanetLook(0xFFFF9F7A, 0xFFFFDCCB, 0xFFC4543A, bands = true, ring = true),
            skills = listOf(
                reading("r_sentence_pic", txt("Les setninga", "Les setningen"), txt("Finn biletet", "Finn bildet"), 1, "Aa") { ctx ->
                    val r = ctx.random
                    val m = ctx.maalform
                    val word = r.pick(WordBank.words.filter { it.gender != null && it.text(m).length <= 7 })
                    val template = r.pick(ReadingContent.pictureSentences).get(m)
                    val sentence = template.replace("#", "${word.article(m)} ${lower(word.text(m))}")
                    val options = (distractorWords(word, m, r, 2, preferSameStart = true) + word).shuffled(r)
                    Question(
                        key = sentence,
                        prompt = txt("Les setninga. Finn biletet.", "Les setningen. Finn bildet."),
                        visual = Visual.Glyph(txt(sentence), GlyphKind.SENTENCE),
                        answer = Answer.Choice(options.map { Option.Picture(it.emoji) }, options.indexOf(word)),
                        reward = txt(sentence),
                    )
                },
                reading("r_truefalse", txt("Sant eller usant"), txt("Les og tenk", "Les og tenk"), 1, "✓") { ctx ->
                    val r = ctx.random
                    val (statement, truth) = r.pick(ReadingContent.trueFalse)
                    Question(
                        key = statement.nn,
                        prompt = txt("Er dette sant?"),
                        visual = Visual.Glyph(statement, GlyphKind.SENTENCE),
                        answer = Answer.Choice(listOf(Option.Verdict(true), Option.Verdict(false)), if (truth) 0 else 1),
                        reward = statement,
                    )
                },
                reading("r_missing_word", txt("Finn ordet"), txt("Kva ord passar?", "Hvilket ord passer?"), 2, "___") { ctx ->
                    val r = ctx.random
                    val gap = r.pick(ReadingContent.gaps)
                    val options = (gap.wrong + gap.answer).shuffled(r)
                    Question(
                        key = gap.sentence.nn,
                        prompt = txt("Kva ord passar?", "Hvilket ord passer?"),
                        visual = Visual.Glyph(gap.sentence, GlyphKind.SENTENCE),
                        answer = Answer.Choice(options.map { Option.Label(it, GlyphKind.SENTENCE_WORD) }, options.indexOf(gap.answer)),
                        reward = txt(gap.sentence.nn.replace("___", gap.answer.nn), gap.sentence.nb.replace("___", gap.answer.nb)),
                    )
                },
                reading("r_word_order", txt("Ordne orda", "Ordne ordene"), txt("Bygg ei setning", "Bygg en setning"), 2, "1-2-3", length = 6) { ctx ->
                    val r = ctx.random
                    val sentence = r.pick(ReadingContent.orderedSentences)
                    val words = sentence.get(ctx.maalform).split(" ")
                    var tiles = words.shuffled(r)
                    var guard = 0
                    while (tiles == words && guard < 10) {
                        tiles = words.shuffled(r)
                        guard++
                    }
                    Question(
                        key = sentence.nn,
                        prompt = txt("Set orda i rett rekkjefølgje", "Sett ordene i riktig rekkefølge"),
                        visual = Visual.None,
                        answer = Answer.Build(target = words, tiles = tiles, kind = GlyphKind.SENTENCE_WORD),
                        reward = sentence,
                    )
                },
                reading("r_story", txt("Les ein tekst", "Les en tekst"), txt("Svar på spørsmålet", "Svar på spørsmålet"), 2, "📜", length = 5) { ctx ->
                    val r = ctx.random
                    val story = r.pick(ReadingContent.stories)
                    val options = (story.wrong + story.answer).shuffled(r)
                    Question(
                        key = story.text.nn,
                        prompt = story.question,
                        visual = Visual.Story(story.text, reading = true),
                        answer = Answer.Choice(options.map { Option.Label(it, GlyphKind.SENTENCE_WORD) }, options.indexOf(story.answer)),
                        reward = story.answer,
                    )
                },
            ),
        ),
    )
}

private fun buildWordSkill(id: String, title: Txt, grade: Int, lengths: IntRange) = reading(
    id = id,
    title = title,
    detail = txt("Lytt og skriv", "Lytt og skriv"),
    grade = grade,
    symbol = "✎",
    length = 6,
) { ctx ->
    val r = ctx.random
    val m = ctx.maalform
    val word = r.pick(WordBank.withLength(m, lengths))
    val text = word.text(m)
    val letters = text.map { it.toString() }
    val extra = r.otherLetters(letters, if (letters.size <= 3) 1 else 2)
    Question(
        key = text,
        prompt = txt("Skriv ordet"),
        visual = Visual.Picture(word.emoji),
        answer = Answer.Build(target = letters, tiles = (letters + extra).shuffled(r), kind = GlyphKind.LETTER),
        speech = txt("Skriv ${lower(text)}."),
        reward = txt(lower(text)),
        explanation = txt(text.inCase(ctx.letterCase)),
    )
}
