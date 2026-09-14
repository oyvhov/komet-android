package app.komet.domain

import app.komet.audio.Speaker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class EnglishContentTest {

    @Test
    fun `every word has a meaning in both written standards and a way to be shown`() {
        val words = EnglishContent.allWords
        assertEquals("English words must be unique", words.size, words.map { it.en }.toSet().size)
        words.forEach { word ->
            assertTrue(word.en.isNotBlank())
            assertTrue("${word.en} lacks nynorsk", word.no.nn.isNotBlank())
            assertTrue("${word.en} lacks bokmål", word.no.nb.isNotBlank())
            val shows = listOfNotNull(word.emoji, word.color, word.number).size
            assertEquals("${word.en} must have exactly one picture, colour or number", 1, shows)
        }
        assertEquals((1..10).toList(), EnglishContent.numbers.map { it.number })
        assertEquals("colours must look different", EnglishContent.colours.size, EnglishContent.colours.map { it.color }.toSet().size)
    }

    @Test
    fun `phrases cover every grade and spelling words are plain letters`() {
        val phrases = EnglishContent.phrases
        assertEquals(phrases.size, phrases.map { it.en }.toSet().size)
        assertTrue(phrases.count { it.grade == 0 } >= 3)
        assertEquals(setOf(0, 1, 2, 3), phrases.map { it.grade }.toSet())
        assertTrue(EnglishContent.spellShort.size >= 6)
        assertTrue(EnglishContent.spellLong.size >= 10)
        (EnglishContent.spellShort + EnglishContent.spellLong).forEach { word ->
            assertTrue(word.en, word.en.all { it in 'a'..'z' })
        }
    }

    @Test
    fun `english tasks keep marks out of what is shown and speak the right word`() {
        val context = QuestionContext(Random(7), Maalform.NYNORSK, LetterCase.LOWER)
        Curriculum.chapters(Subject.ENGLISH).flatMap { it.skills }.forEach { skill ->
            repeat(60) {
                val question = skill.generate(context)
                val where = "${skill.id} «${question.key}»"
                assertFalse("$where shows a speech mark in the prompt", "{en:" in question.prompt.nn || "{en:" in question.prompt.nb)
                listOf(question.speech, question.reward).filterNotNull().forEach { text ->
                    listOf(text.nn, text.nb).forEach { spoken ->
                        assertEquals("$where has unbalanced marks: $spoken", spoken.count { it == '{' }, spoken.count { it == '}' })
                        assertTrue("$where says nothing: $spoken", Speaker.segments(spoken).isNotEmpty())
                    }
                }
                val answer = question.answer
                if (answer is Answer.Choice) {
                    val word = EnglishContent.allWords.firstOrNull { it.en == question.key }
                    if (question.visual is Visual.Listen && word != null) {
                        val right = answer.options[answer.correct]
                        when {
                            word.color != null -> assertEquals(where, Option.Color(word.color!!), right)
                            word.number != null -> assertEquals(where, word.number.toString(), (right as Option.Label).text.nn)
                            else -> assertEquals(where, word.emoji, (right as Option.Picture).emoji)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `english levels are all in a topic`() {
        Curriculum.chapters(Subject.ENGLISH).flatMap { it.skills }.forEach { assertNotNull(it.id, Topics.of(it)) }
    }

    @Test
    fun `speech is split into norwegian and english pieces`() {
        assertEquals(
            listOf(Speaker.Companion.Segment("Finn", false), Speaker.Companion.Segment("cat", true)),
            Speaker.segments("Finn {en:cat}."),
        )
        assertEquals(
            listOf(Speaker.Companion.Segment("Thank you!", true), Speaker.Companion.Segment("Takk!", false)),
            Speaker.segments("{en:Thank you!} Takk!"),
        )
        assertEquals(listOf(Speaker.Companion.Segment("Hei på deg.", false)), Speaker.segments("Hei på deg."))
        assertEquals("Finn cat.", Speaker.plain("Finn {en:cat}."))
    }
}
