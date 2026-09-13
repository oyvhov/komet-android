package app.komet.domain

import kotlin.math.max

data class Rank(val minStars: Int, val title: Txt)

data class RoundOutcome(
    val profile: Profile,
    val stars: Int,
    val firstTry: Int,
    val total: Int,
    val newCards: List<SpaceCard>,
    val newRank: Rank?,
    val goalReachedNow: Boolean,
    val newRecord: Boolean = false,
    val previousBest: Int = 0,
)

object Progression {
    const val KEEP_DAYS = 60

    val ranks: List<Rank> = listOf(
        Rank(0, txt("Romkadett")),
        Rank(15, txt("Pilot")),
        Rank(40, txt("Navigatør")),
        Rank(80, txt("Kaptein")),
        Rank(130, txt("Kommandør")),
        Rank(200, txt("Astronaut")),
        Rank(300, txt("Galaksehelt")),
        Rank(450, txt("Stjernemeister", "Stjernemester")),
    )

    fun rank(totalStars: Int): Rank = ranks.last { totalStars >= it.minStars }

    fun nextRank(totalStars: Int): Rank? = ranks.firstOrNull { it.minStars > totalStars }

    /** 80 % right on the first try gives three stars, half gives two, finishing gives one. */
    fun stars(firstTry: Int, total: Int): Int {
        if (total <= 0) return 0
        val ratio = firstTry.toDouble() / total
        return when {
            ratio >= 0.8 -> 3
            ratio >= 0.5 -> 2
            else -> 1
        }
    }

    fun raceStars(score: Int): Int = when {
        score >= 20 -> 3
        score >= 12 -> 2
        score >= 5 -> 1
        else -> 0
    }

    /**
     * The first level of every chapter is always open, so a child can choose freely between planets.
     * Levels below the chosen grade are open from the start; above it they open one at a time.
     */
    fun isUnlocked(profile: Profile, chapter: Chapter, index: Int): Boolean {
        if (profile.unlockAll || index == 0) return true
        val skill = chapter.skills[index]
        val previous = chapter.skills[index - 1]
        return skill.grade < profile.grade ||
            profile.stars(previous.id) > 0 ||
            profile.stars(skill.id) > 0 ||
            (skill.grade == profile.grade && previous.grade < profile.grade)
    }

    fun isUnlocked(profile: Profile, skill: Skill): Boolean {
        val chapter = Curriculum.chapterOf(skill)
        return isUnlocked(profile, chapter, chapter.skills.indexOf(skill))
    }

    private fun unlockedSkills(profile: Profile, subject: Subject): List<Pair<Skill, Int>> =
        Curriculum.chapters(subject).flatMap { chapter ->
            chapter.skills.mapIndexedNotNull { index, skill ->
                if (isUnlocked(profile, chapter, index)) skill to index else null
            }
        }

    /** The next thing to play: alternate subjects, easiest new level first, then improve, then review. */
    fun recommended(profile: Profile): Skill {
        val first = if (profile.lastSubject == Subject.MATH) Subject.READING else Subject.MATH
        val second = if (first == Subject.MATH) Subject.READING else Subject.MATH
        return recommended(profile, first) ?: recommended(profile, second) ?: Curriculum.skills.first()
    }

    fun recommended(profile: Profile, subject: Subject): Skill? {
        val unlocked = unlockedSkills(profile, subject)
        val chapterIndex = Curriculum.chapters(subject).withIndex().associate { it.value.id to it.index }
        val fresh = unlocked.filter { profile.stars(it.first.id) == 0 }
            .minWithOrNull(compareBy({ it.first.grade }, { it.second }, { chapterIndex[Curriculum.chapterOf(it.first).id] }))
        if (fresh != null) return fresh.first
        val improve = unlocked.filter { profile.stars(it.first.id) < 3 }
            .minWithOrNull(compareBy({ profile.stars(it.first.id) }, { it.first.grade }, { it.second }))
        if (improve != null) return improve.first
        return unlocked.minByOrNull { profile.skills[it.first.id]?.lastPlayed ?: 0L }?.first
    }

    /** The level after [skill] in its chapter, if it is open for [profile]. */
    fun nextInChapter(profile: Profile, skill: Skill): Skill? {
        val chapter = Curriculum.chapterOf(skill)
        val index = chapter.skills.indexOf(skill) + 1
        if (index >= chapter.skills.size) return null
        return chapter.skills[index].takeIf { isUnlocked(profile, chapter, index) }
    }

    fun earnedStars(profile: Profile, skills: List<Skill>): Int = skills.sumOf { profile.stars(it.id) }

    fun completed(profile: Profile, skills: List<Skill>): Int = skills.count { profile.stars(it.id) > 0 }

    fun applyRound(
        profile: Profile,
        skill: Skill,
        firstTry: Int,
        total: Int,
        seconds: Int,
        today: Long,
        dailyGoal: Int,
        now: Long,
    ): RoundOutcome {
        val stars = stars(firstTry, total)
        val old = profile.skills[skill.id] ?: SkillStats()
        val updatedSkill = old.copy(
            bestStars = max(old.bestStars, stars),
            plays = old.plays + 1,
            answered = old.answered + total,
            firstTry = old.firstTry + firstTry,
            lastPlayed = now,
        )
        val base = profile.copy(
            skills = profile.skills + (skill.id to updatedSkill),
            lastSubject = skill.subject,
        )
        return finish(profile, base, stars, firstTry, total, seconds, today, dailyGoal)
    }

    fun applyRace(
        profile: Profile,
        mode: RaceMode,
        score: Int,
        answered: Int,
        seconds: Int,
        today: Long,
        dailyGoal: Int,
    ): RoundOutcome {
        val previous = profile.raceBest[mode] ?: 0
        val record = score > previous
        val stars = raceStars(score)
        val base = profile.copy(raceBest = if (record) profile.raceBest + (mode to score) else profile.raceBest)
        return finish(profile, base, stars, score, answered, seconds, today, dailyGoal)
            .copy(newRecord = record && score > 0, previousBest = previous)
    }

    private fun finish(
        before: Profile,
        base: Profile,
        stars: Int,
        firstTry: Int,
        total: Int,
        seconds: Int,
        today: Long,
        dailyGoal: Int,
    ): RoundOutcome {
        val day = base.today(today)
        val updatedDay = day.copy(
            rounds = day.rounds + 1,
            seconds = day.seconds + seconds.coerceIn(0, 60 * 30),
            answered = day.answered + total,
            firstTry = day.firstTry + firstTry,
        )
        val reachedNow = day.rounds < dailyGoal && updatedDay.rounds >= dailyGoal
        val streak = when {
            !reachedNow -> base.streak
            base.lastGoalDay == today - 1 -> base.streak + 1
            base.lastGoalDay == today -> base.streak
            else -> 1
        }
        val totalStars = base.totalStars + stars
        val days = (base.days + (today to updatedDay)).filterKeys { it > today - KEEP_DAYS }
        val after = base.copy(
            totalStars = totalStars,
            days = days,
            streak = streak,
            lastGoalDay = if (reachedNow) today else base.lastGoalDay,
        )
        val cardsBefore = SpaceCards.unlockedCount(before.totalStars)
        val cardsAfter = SpaceCards.unlockedCount(totalStars)
        val rankBefore = rank(before.totalStars)
        val rankAfter = rank(totalStars)
        return RoundOutcome(
            profile = after,
            stars = stars,
            firstTry = firstTry,
            total = total,
            newCards = SpaceCards.all.subList(cardsBefore, cardsAfter),
            newRank = rankAfter.takeIf { it != rankBefore },
            goalReachedNow = reachedNow,
        )
    }
}
