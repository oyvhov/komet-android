package app.komet.domain

data class SkillStats(
    val bestStars: Int = 0,
    val plays: Int = 0,
    val answered: Int = 0,
    val firstTry: Int = 0,
    val lastPlayed: Long = 0L,
)

data class DayStats(
    val rounds: Int = 0,
    val seconds: Int = 0,
    val answered: Int = 0,
    val firstTry: Int = 0,
)

/** A round that was left before the end. Its answers are already counted in the statistics. */
data class ActiveRound(
    val skillId: String,
    /** Tasks answered so far. */
    val done: Int,
    /** How many of those were right on the first try. */
    val firstTry: Int,
    val total: Int,
    val updatedAt: Long = 0L,
)

data class Profile(
    val id: String,
    val name: String,
    val avatar: Int,
    /** 0 = førskule, 1–3 = klassesteg. Decides where the map starts open. */
    val grade: Int,
    val maalform: Maalform = Maalform.NYNORSK,
    val letterCase: LetterCase = LetterCase.UPPER,
    val unlockAll: Boolean = false,
    val skills: Map<String, SkillStats> = emptyMap(),
    /** Every star ever earned, replays included. Drives ranks and space cards. */
    val totalStars: Int = 0,
    val streak: Int = 0,
    /** Epoch day the daily goal was last reached, or -1. */
    val lastGoalDay: Long = -1L,
    /** Per epoch day, trimmed to the last [Progression.KEEP_DAYS] days. */
    val days: Map<Long, DayStats> = emptyMap(),
    val raceBest: Map<RaceMode, Int> = emptyMap(),
    val lastSubject: Subject? = null,
    /** How many unlocked cards the child has already opened the collection for. */
    val seenCards: Int = 0,
    val createdAt: Long = 0L,
    /** The round to pick up again, saved after every answer. */
    val activeRound: ActiveRound? = null,
    /** Levels the child has marked with a heart, in the order they were added. */
    val favorites: List<String> = emptyList(),
    /** The child's astronaut. */
    val hero: HeroLook = HeroLook.fromAvatar(avatar),
) {
    fun stars(skillId: String): Int = skills[skillId]?.bestStars ?: 0

    fun today(day: Long): DayStats = days[day] ?: DayStats()

    /** A streak is only alive if the goal was reached today or yesterday. */
    fun currentStreak(today: Long): Int = if (lastGoalDay >= today - 1) streak else 0
}

data class Settings(
    val sound: Boolean = true,
    /** Background music for the places in the adventure. */
    val music: Boolean = true,
    val speech: Boolean = true,
    val slowSpeech: Boolean = true,
    val haptics: Boolean = true,
    val autoRead: Boolean = true,
    /** Rounds per day before «Dagens oppdrag» is complete. */
    val dailyGoal: Int = 3,
)

data class AppState(
    val profiles: List<Profile> = emptyList(),
    val activeProfileId: String? = null,
    val settings: Settings = Settings(),
) {
    val activeProfile: Profile?
        get() = profiles.firstOrNull { it.id == activeProfileId } ?: profiles.firstOrNull()

    fun withProfile(profile: Profile): AppState =
        copy(profiles = profiles.map { if (it.id == profile.id) profile else it })
}
