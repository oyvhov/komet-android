package app.komet.domain

import org.junit.Assert.*
import org.junit.Test

class AdventureTest {
    private val player = Profile("child", "Test", 0, 0)

    @Test fun `each planet has three distinct bilingual chapters and age appropriate existing tasks`() {
        assertEquals(Subject.entries.toSet(), Adventures.all.map { it.subject }.toSet())
        val ids = Adventures.all.flatMap { it.quests }.map { it.id }
        assertEquals(12, ids.size)
        assertEquals(ids.size, ids.toSet().size)
        for (story in Adventures.all) {
            assertEquals(3, story.quests.size)
            val copy = listOf(story.title, story.ending) + story.quests.flatMap { listOf(it.title, it.request, it.thanks) }
            for (form in Maalform.entries) assertTrue(copy.all { it.get(form).isNotBlank() })
            for (quest in story.quests) {
                assertEquals(4, quest.skillsByGrade.size)
                for (grade in 0..3) {
                    val skill = quest.skill(grade)
                    assertEquals(story.subject, skill.subject)
                    assertTrue("${quest.id}, grade $grade: ${skill.id}", skill.grade <= grade)
                }
            }
        }
    }

    @Test fun `chapters must be explicitly chosen and completed in order`() {
        val story = Adventures.of(Subject.MATH)
        val first = story.quests.first()
        val last = story.quests.last()
        assertEquals(player, Adventures.complete(player, null, first.skill(0).id))
        assertEquals(player, Adventures.complete(player, last.id, last.skill(0).id))
        assertEquals(player, Adventures.complete(player, first.id, "s_basics"))
        val updated = Adventures.complete(player, first.id, first.skill(0).id)
        assertEquals(setOf(first.id), updated.adventures)
        assertEquals(1, Adventures.completed(updated, Subject.MATH))
        assertEquals(story.quests[1], Adventures.next(updated, Subject.MATH))
        assertEquals(updated, Adventures.complete(updated, first.id, first.skill(0).id))
    }

    @Test fun `finishing with help restores the whole place without changing other planets or awarding extra currency`() {
        var current = player.copy(nuggets = 19)
        for (quest in Adventures.of(Subject.READING).quests) {
            val skill = quest.skill(current.grade)
            val outcome = Progression.applyRound(current, skill, 0, 5, 0, 123L, 10, 50L)
            current = Adventures.complete(outcome.profile, quest.id, skill.id)
            assertEquals(outcome.profile.nuggets, current.nuggets)
            assertEquals(outcome.profile.totalStars, current.totalStars)
        }
        assertEquals(3, Adventures.completed(current, Subject.READING))
        assertNull(Adventures.next(current, Subject.READING))
        for (subject in Subject.entries.filter { it != Subject.READING }) {
            assertEquals(0, Adventures.completed(current, subject))
            assertNotNull(Adventures.next(current, subject))
        }
        assertTrue(player.adventures.isEmpty())
    }

    @Test fun `saved adventure works before first answer after last answer and after changing grade`() {
        val quest = Adventures.of(Subject.MATH).quests.first()
        val skill = quest.skill(0)
        val beginning = player.copy(activeRound = ActiveRound(skill.id, 0, 0, 5, 0L, quest.id))
        assertEquals(0, Progression.resumable(beginning)!!.second.done)
        var current = beginning
        for (done in 1..5) {
            current = Progression.applyAnswer(current, skill, rightFirstTime = false, done = done,
                roundFirstTry = 0, total = 5, seconds = 1, today = 123L, now = 50L, adventureId = quest.id)
        }
        val restored = Progression.resumable(current.copy(grade = 3))!!
        assertEquals(skill, restored.first)
        assertEquals(5, restored.second.done)
        assertEquals(quest.id, restored.second.adventureId)
        assertTrue(current.adventures.isEmpty())
        val outcome = Progression.applyRound(current, skill, restored.second.firstTry, restored.second.total,
            0, 123L, 3, 55L, answersCounted = true)
        val finished = Adventures.complete(outcome.profile, quest.id, skill.id)
        assertNull(finished.activeRound)
        assertEquals(5, finished.skills.getValue(skill.id).answered)
        assertEquals(1, finished.today(123L).rounds)
        assertEquals(1, Adventures.completed(finished, Subject.MATH))
    }

    @Test fun `ordinary practice and review keep their existing save behaviour`() {
        val skill = Curriculum.skill("m_add10")!!
        val normal = Progression.applyAnswer(player, skill, true, 5, 5, 5, 0, 123L, 1L)
        assertNull(normal.activeRound)
        assertTrue(normal.adventures.isEmpty())
        val active = ActiveRound("m_mission_collect", 2, 1, 5, 4L, "math_energy")
        val review = Progression.applyAnswer(player.copy(activeRound = active), skill, true, 1, 1, 5, 0, 123L, 5L, trackRound = false)
        assertEquals(active, review.activeRound)
    }
}
