package app.komet.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ReadingContentTest {

    private fun questions(id: String, maalform: Maalform = Maalform.NYNORSK, count: Int = 400): List<Question> {
        val skill = Curriculum.skill(id) ?: error("missing $id")
        val context = QuestionContext(Random(id.hashCode() + maalform.ordinal), maalform, LetterCase.UPPER)
        return List(count) { skill.generate(context) }
    }

    private fun wordFor(emoji: String): Word = WordBank.words.first { it.emoji == emoji }

    @Test
    fun `word bank is tidy`() {
        val texts = WordBank.words.map { it.text(Maalform.NYNORSK) }
        assertEquals("duplicate words", texts.size, texts.toSet().size)
        val emoji = WordBank.words.map { it.emoji }
        assertEquals("duplicate pictures", emoji.size, emoji.toSet().size)
        WordBank.words.forEach { word ->
            Maalform.entries.forEach { m ->
                assertTrue("${word.text(m)} syllables", word.syllables(m).size in 1..4)
                assertTrue(word.text(m).all { it.isUpperCase() })
            }
        }
        assertTrue(WordBank.words.size >= 120)
    }

    @Test
    fun `rhyme families are real words that rhyme only with each other`() {
        WordBank.rhymes.forEach { family ->
            assertTrue(family.size >= 2)
            family.forEach { assertTrue("$it must be spelled the same in both", WordBank.get(it).sameInBoth) }
        }
        Maalform.entries.forEach { m ->
            questions("r_rhyme", m).forEach { q ->
                val target = (q.visual as Visual.Picture).word!!
                val family = WordBank.rhymes.first { target in it }
                val answer = q.answer as Answer.Choice
                answer.options.forEachIndexed { index, option ->
                    val caption = (option as Option.Picture).caption!!
                    assertEquals("$target / $caption", index == answer.correct, caption in family)
                }
            }
        }
    }

    @Test
    fun `first and last sounds match the picture`() {
        Maalform.entries.forEach { m ->
            listOf("r_first1", "r_first2").forEach { id ->
                questions(id, m).forEach { q ->
                    val picture = q.visual as Visual.Picture
                    val word = wordFor(picture.emoji)
                    assertFalse(word.irregularStart)
                    assertEquals(word.text(m).first().toString(), QuestionChecks.correctLabel(q.answer))
                    assertFalse("the spoken prompt must name the word", q.speech.get(m).isBlank())
                }
            }
            questions("r_last", m).forEach { q ->
                val word = wordFor((q.visual as Visual.Picture).emoji)
                assertEquals(word.text(m).last().toString(), QuestionChecks.correctLabel(q.answer))
            }
        }
    }

    @Test
    fun `missing letter and syllables agree with the word`() {
        Maalform.entries.forEach { m ->
            questions("r_missing", m).forEach { q ->
                val picture = q.visual as Visual.Picture
                assertEquals(picture.word!![picture.hideIndex!!].toString(), QuestionChecks.correctLabel(q.answer))
            }
            questions("r_syllables", m).forEach { q ->
                val picture = q.visual as Visual.Picture
                val word = wordFor(picture.emoji)
                val answer = q.answer as Answer.Choice
                assertEquals(word.syllables(m).size, (answer.options[answer.correct] as Option.Claps).count)
            }
        }
    }

    @Test
    fun `picture and word tasks point at the same word`() {
        Maalform.entries.forEach { m ->
            questions("r_word_pic", m).forEach { q ->
                val text = (q.visual as Visual.Glyph).text.nn
                val answer = q.answer as Answer.Choice
                assertEquals(text, wordFor((answer.options[answer.correct] as Option.Picture).emoji).text(m))
            }
            questions("r_pic_word", m).forEach { q ->
                val emoji = (q.visual as Visual.Picture).emoji
                assertEquals(wordFor(emoji).text(m), QuestionChecks.correctLabel(q.answer))
            }
            questions("r_sentence_pic", m).forEach { q ->
                val sentence = (q.visual as Visual.Glyph).text.nn
                val answer = q.answer as Answer.Choice
                val word = wordFor((answer.options[answer.correct] as Option.Picture).emoji)
                assertTrue("$sentence / ${word.text(m)}", sentence.endsWith(" ${word.text(m).lowerNo()}.") || sentence.endsWith(" ${word.text(m).lowerNo()}!"))
            }
        }
    }

    @Test
    fun `building words uses the right letters`() {
        Maalform.entries.forEach { m ->
            listOf("r_build3", "r_build4", "r_build5").forEach { id ->
                questions(id, m).forEach { q ->
                    val build = q.answer as Answer.Build
                    val word = wordFor((q.visual as Visual.Picture).emoji)
                    assertEquals(word.text(m), build.target.joinToString(""))
                }
            }
            questions("r_word_order", m).forEach { q ->
                val build = q.answer as Answer.Build
                val sentence = build.target.joinToString(" ")
                assertTrue(sentence, ReadingContent.orderedSentences.any { it.get(m) == sentence })
                assertEquals("repeated words make the order ambiguous: $sentence", build.target.size, build.target.toSet().size)
            }
        }
    }

    @Test
    fun `letters, case and alphabet`() {
        questions("r_case").forEach { q ->
            val shown = (q.visual as Visual.Glyph).text.nn
            val correct = QuestionChecks.correctLabel(q.answer)!!
            assertTrue(shown != correct && shown.lowercase() == correct.lowercase())
        }
        questions("r_abc").forEach { q ->
            val shown = (q.visual as Visual.Glyph).text.nn.split(Regex("\\s+"))
            val correct = QuestionChecks.correctLabel(q.answer)!!
            val alphabet = WordBank.alphabet
            if (shown.last() == "?") {
                assertEquals(alphabet[alphabet.indexOf(shown[1]) + 1], correct)
            } else {
                assertEquals(alphabet[alphabet.indexOf(shown[1]) - 1], correct)
            }
        }
        questions("r_vowels").forEach { q ->
            val answer = q.answer as Answer.Choice
            answer.options.forEachIndexed { index, option ->
                val letter = (option as Option.Label).text.nn
                assertEquals(index == answer.correct, letter in WordBank.vowels)
            }
        }
        listOf("r_letters1", "r_letters2", "r_letters3", "r_letters4", "r_letters5", "r_sight").forEach { id ->
            Maalform.entries.forEach { m ->
                questions(id, m).forEach { q ->
                    val listen = q.visual as Visual.Listen
                    val answer = q.answer as Answer.Choice
                    val correct = answer.options[answer.correct] as Option.Label
                    assertEquals(listen.fallback.get(m), correct.text.get(m))
                }
            }
        }
    }

    @Test
    fun `sentences, stories and compounds`() {
        questions("r_truefalse").forEach { q ->
            val statement = (q.visual as Visual.Glyph).text
            val truth = ReadingContent.trueFalse.first { it.first == statement }.second
            val answer = q.answer as Answer.Choice
            assertEquals(truth, (answer.options[answer.correct] as Option.Verdict).truth)
        }
        questions("r_compound").forEach { q ->
            val glyph = (q.visual as Visual.Glyph).text.nn
            val compound = WordBank.compounds.first { "${it.left.nn} + ${it.right.nn}" == glyph }
            val answer = q.answer as Answer.Choice
            assertEquals(compound.emoji, (answer.options[answer.correct] as Option.Picture).emoji)
        }
        ReadingContent.gaps.forEach { gap ->
            assertTrue(gap.sentence.nn.contains("___") && gap.sentence.nb.contains("___"))
            assertEquals(2, gap.wrong.size)
        }
        ReadingContent.stories.forEach { story ->
            assertEquals(2, story.wrong.size)
            assertFalse(story.wrong.contains(story.answer))
        }
    }
}
