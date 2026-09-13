package app.komet.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class CurriculumTest {

    private val contexts = listOf(
        QuestionContext(Random(1), Maalform.NYNORSK, LetterCase.UPPER),
        QuestionContext(Random(2), Maalform.BOKMAAL, LetterCase.LOWER),
        QuestionContext(Random(3), Maalform.NYNORSK, LetterCase.LOWER),
        QuestionContext(Random(4), Maalform.BOKMAAL, LetterCase.UPPER),
    )

    @Test
    fun `skill ids are unique and every chapter has content`() {
        val ids = Curriculum.skills.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        Curriculum.chapters.forEach { assertTrue("${it.id} is empty", it.skills.isNotEmpty()) }
        assertTrue("expected a large curriculum, got ${ids.size}", ids.size >= 70)
    }

    @Test
    fun `titles exist in both written standards`() {
        (Curriculum.skills.map { it.title } + Curriculum.skills.map { it.detail } + Curriculum.chapters.map { it.title }).forEach {
            assertTrue(it.nn.isNotBlank())
            assertTrue(it.nb.isNotBlank())
        }
    }

    @Test
    fun `every skill generates valid questions in every profile setting`() {
        for (skill in Curriculum.skills) {
            for (context in contexts) {
                repeat(150) {
                    QuestionChecks.structure(skill, skill.generate(context))
                }
            }
        }
    }

    @Test
    fun `rounds have the planned length and mostly distinct tasks`() {
        for (skill in Curriculum.skills) {
            val round = Curriculum.round(skill, contexts[0])
            assertEquals(skill.id, skill.length, round.size)
            val distinct = round.map { it.key }.distinct().size
            assertTrue("${skill.id} repeats too much: $distinct of ${round.size}", distinct >= round.size - 2)
        }
    }

    @Test
    fun `grades only go up within a chapter`() {
        Curriculum.chapters.forEach { chapter ->
            chapter.skills.zipWithNext().forEach { (a, b) ->
                assertTrue("${chapter.id}: ${a.id} (${a.grade}) before ${b.id} (${b.grade})", b.grade >= a.grade)
            }
        }
    }

    @Test
    fun `race questions are valid`() {
        val random = Random(9)
        RaceMode.entries.forEach { mode ->
            repeat(300) {
                val question = mode.question(random)
                val answer = question.answer as Answer.Choice
                assertEquals(3, answer.options.size)
                QuestionChecks.structure(Curriculum.skills.first(), question)
            }
        }
    }
}
