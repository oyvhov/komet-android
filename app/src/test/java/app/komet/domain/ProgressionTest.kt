package app.komet.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressionTest {

    private fun profile(grade: Int = 1) = Profile(id = "p", name = "Test", avatar = 0, grade = grade)

    private fun skill(id: String) = Curriculum.skill(id)!!

    @Test
    fun `stars follow first-try accuracy`() {
        assertEquals(3, Progression.stars(8, 8))
        assertEquals(3, Progression.stars(7, 8))
        assertEquals(2, Progression.stars(6, 8))
        assertEquals(2, Progression.stars(4, 8))
        assertEquals(1, Progression.stars(3, 8))
        assertEquals(1, Progression.stars(0, 8))
        assertEquals(0, Progression.stars(0, 0))
        assertEquals(3, Progression.stars(5, 6))
    }

    @Test
    fun `chapter starts and easier levels are open, harder ones open one by one`() {
        val p = profile(grade = 1)
        Curriculum.chapters.forEach { chapter -> assertTrue(Progression.isUnlocked(p, chapter, 0)) }
        val tal = Curriculum.chapters.first { it.id == "m_tal" }
        val firstGradeOne = tal.skills.indexOfFirst { it.grade == 1 }
        assertEquals("m_count20", tal.skills[firstGradeOne].id)
        assertTrue(Progression.isUnlocked(p, tal, firstGradeOne - 1)) // grade 0
        assertTrue(Progression.isUnlocked(p, tal, firstGradeOne)) // first grade-1 level
        assertFalse(Progression.isUnlocked(p, tal, firstGradeOne + 1)) // second grade-1 level waits
        val played = p.copy(skills = mapOf("m_count20" to SkillStats(bestStars = 1)))
        assertTrue(Progression.isUnlocked(played, tal, firstGradeOne + 1))
        assertTrue(Progression.isUnlocked(p.copy(unlockAll = true), tal, tal.skills.lastIndex))
    }

    @Test
    fun `preschool only opens the first level of each chapter plus its grade`() {
        val p = profile(grade = 0)
        val pluss = Curriculum.chapters.first { it.id == "m_pluss" }
        assertTrue(Progression.isUnlocked(p, pluss, 0))
        assertFalse(Progression.isUnlocked(p, pluss, 1))
    }

    @Test
    fun `recommendation alternates subjects and starts easy`() {
        val p = profile(grade = 1)
        val first = Progression.recommended(p)
        assertEquals(Subject.MATH, first.subject)
        assertEquals(0, first.grade)
        val afterMath = Progression.recommended(p.copy(lastSubject = Subject.MATH))
        assertEquals(Subject.READING, afterMath.subject)
    }

    @Test
    fun `a finished round updates stars, day and goal`() {
        val skill = skill("m_add5")
        var p = profile()
        val today = 20_000L
        val first = Progression.applyRound(p, skill, firstTry = 8, total = 8, seconds = 120, today = today, dailyGoal = 2, now = 1L)
        assertEquals(3, first.stars)
        assertEquals(3, first.profile.totalStars)
        assertEquals(3, first.profile.stars(skill.id))
        assertFalse(first.goalReachedNow)
        assertEquals(Subject.MATH, first.profile.lastSubject)
        p = first.profile
        val second = Progression.applyRound(p, skill, firstTry = 2, total = 8, seconds = 90, today = today, dailyGoal = 2, now = 2L)
        assertEquals(1, second.stars)
        assertEquals("best stars are kept", 3, second.profile.stars(skill.id))
        assertEquals(4, second.profile.totalStars)
        assertTrue(second.goalReachedNow)
        assertEquals(1, second.profile.streak)
        assertEquals(2, second.profile.today(today).rounds)
        assertEquals(210, second.profile.today(today).seconds)
        val third = Progression.applyRound(second.profile, skill, 8, 8, 60, today, 2, 3L)
        assertFalse("the goal is only reached once a day", third.goalReachedNow)
    }

    @Test
    fun `streak grows on consecutive days and breaks after a gap`() {
        val skill = skill("r_letters1")
        var p = profile()
        for (day in 100L..102L) {
            p = Progression.applyRound(p, skill, 8, 8, 60, day, 1, day).profile
        }
        assertEquals(3, p.streak)
        assertEquals(3, p.currentStreak(102))
        assertEquals(3, p.currentStreak(103))
        assertEquals(0, p.currentStreak(104))
        p = Progression.applyRound(p, skill, 8, 8, 60, 105, 1, 105).profile
        assertEquals(1, p.streak)
    }

    @Test
    fun `cards and ranks unlock when stars pass their thresholds`() {
        val skill = skill("m_count5")
        val p = profile().copy(totalStars = 13)
        val outcome = Progression.applyRound(p, skill, 8, 8, 60, 1, 3, 1)
        assertEquals(16, outcome.profile.totalStars)
        assertEquals(listOf("mars"), outcome.newCards.map { it.id })
        assertNotNull(outcome.newRank)
        assertEquals("Pilot", outcome.newRank!!.title.nn)
        val quiet = Progression.applyRound(outcome.profile, skill, 8, 8, 60, 1, 3, 2)
        assertTrue(quiet.newCards.isEmpty())
        assertNull(quiet.newRank)
    }

    @Test
    fun `card costs rise and ranks are ordered`() {
        SpaceCards.all.zipWithNext().forEach { (a, b) -> assertTrue(b.cost > a.cost) }
        Progression.ranks.zipWithNext().forEach { (a, b) -> assertTrue(b.minStars > a.minStars) }
        assertEquals(SpaceCards.all.size, SpaceCards.all.map { it.id }.toSet().size)
    }

    @Test
    fun `race keeps the best score`() {
        val p = profile()
        val first = Progression.applyRace(p, RaceMode.ADD10, score = 14, answered = 16, seconds = 60, today = 5, dailyGoal = 3)
        assertTrue(first.newRecord)
        assertEquals(2, first.stars)
        assertEquals(14, first.profile.raceBest[RaceMode.ADD10])
        val second = Progression.applyRace(first.profile, RaceMode.ADD10, score = 9, answered = 12, seconds = 60, today = 5, dailyGoal = 3)
        assertFalse(second.newRecord)
        assertEquals(14, second.profile.raceBest[RaceMode.ADD10])
        assertEquals(14, second.previousBest)
    }

    @Test
    fun `every answer is saved at once and a left round can be picked up`() {
        val skill = skill("m_add10")
        val today = 30_000L
        var p = profile()
        p = Progression.applyAnswer(p, skill, rightFirstTime = true, done = 1, roundFirstTry = 1, total = 8, seconds = 12, today = today, now = 10L)
        p = Progression.applyAnswer(p, skill, rightFirstTime = false, done = 2, roundFirstTry = 1, total = 8, seconds = 900, today = today, now = 20L)
        assertEquals(2, p.skills[skill.id]!!.answered)
        assertEquals(1, p.skills[skill.id]!!.firstTry)
        assertEquals(2, p.today(today).answered)
        assertEquals("a forgotten tablet does not count as practice", 12 + Progression.MAX_TASK_SECONDS, p.today(today).seconds)
        assertEquals(0, p.today(today).rounds)
        assertEquals(ActiveRound(skill.id, 2, 1, 8, 20L), p.activeRound)
        val (resumeSkill, active) = Progression.resumable(p)!!
        assertEquals(skill, resumeSkill)
        assertEquals(2, active.done)
        // The level itself is not finished, so it earns no stars yet.
        assertEquals(0, p.stars(skill.id))
    }

    @Test
    fun `finishing a round whose answers were saved does not count them twice`() {
        val skill = skill("m_add10")
        val today = 30_001L
        var p = profile()
        repeat(8) { index ->
            p = Progression.applyAnswer(p, skill, rightFirstTime = true, done = index + 1, roundFirstTry = index + 1, total = 8, seconds = 10, today = today, now = index.toLong())
        }
        assertNull("the last answer ends the saved round", p.activeRound)
        val outcome = Progression.applyRound(p, skill, firstTry = 8, total = 8, seconds = 80, today = today, dailyGoal = 3, now = 99L, answersCounted = true)
        val stats = outcome.profile.skills[skill.id]!!
        assertEquals(8, stats.answered)
        assertEquals(8, stats.firstTry)
        assertEquals(1, stats.plays)
        assertEquals(3, stats.bestStars)
        assertEquals(8, outcome.profile.today(today).answered)
        assertEquals(80, outcome.profile.today(today).seconds)
        assertEquals(1, outcome.profile.today(today).rounds)
        assertNull(outcome.profile.activeRound)
    }

    @Test
    fun `a saved round that cannot be finished is ignored`() {
        assertNull(Progression.resumable(profile().copy(activeRound = ActiveRound("gone_skill", 2, 2, 8))))
        assertNull(Progression.resumable(profile().copy(activeRound = ActiveRound("m_add10", 0, 0, 8))))
        assertNull(Progression.resumable(profile().copy(activeRound = ActiveRound("m_add10", 8, 8, 8))))
    }

    @Test
    fun `favourites toggle and keep their order`() {
        var p = profile()
        p = Progression.toggleFavorite(p, "m_add10")
        p = Progression.toggleFavorite(p, "r_rhyme")
        assertEquals(listOf("m_add10", "r_rhyme"), p.favorites)
        p = Progression.toggleFavorite(p, "m_add10")
        assertEquals(listOf("r_rhyme"), p.favorites)
    }

    @Test
    fun `next level in chapter respects locks`() {
        val p = profile(grade = 0)
        val count5 = skill("m_count5")
        assertNull(Progression.nextInChapter(p, count5))
        val played = p.copy(skills = mapOf("m_count5" to SkillStats(bestStars = 2)))
        assertEquals("counting to five leads to writing those digits", "m_write1", Progression.nextInChapter(played, count5)?.id)
    }
}
