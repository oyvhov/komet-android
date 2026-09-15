package app.komet.domain

object Curriculum {
    val chapters: List<Chapter> = MathCurriculum.chapters + MissionCurriculum.chapters + ReadingCurriculum.chapters + EnglishCurriculum.chapters + SpaceCurriculum.chapters

    val skills: List<Skill> = chapters.flatMap { it.skills }

    private val byId: Map<String, Skill> = skills.associateBy { it.id }
    private val chapterBySkill: Map<String, Chapter> = chapters.flatMap { chapter -> chapter.skills.map { it.id to chapter } }.toMap()

    fun skill(id: String): Skill? = byId[id]

    fun chapterOf(skill: Skill): Chapter = chapterBySkill.getValue(skill.id)

    fun chapters(subject: Subject): List<Chapter> = chapters.filter { it.subject == subject }

    fun skills(subject: Subject): List<Skill> = chapters(subject).flatMap { it.skills }

    /** Builds one round of distinct questions. Tiny skills may repeat once they run out of variety. */
    fun round(skill: Skill, context: QuestionContext, count: Int = skill.length): List<Question> {
        val questions = ArrayList<Question>(count)
        val keys = HashSet<String>()
        var attempts = 0
        while (questions.size < count) {
            attempts++
            val question = skill.generate(context)
            val fresh = keys.add(question.key)
            // Avoid the same answer twice in a row where possible; it reads like a bug to a child.
            val repeatsAnswer = questions.lastOrNull()?.let { sameAnswer(it, question) } == true
            if ((fresh && !repeatsAnswer) || attempts > count * 30) questions += question
        }
        return questions
    }

    fun isReview(skill: Skill): Boolean = skill.id.startsWith(REVIEW_PREFIX)

    /** The level a review round shows on its result screen. Its tasks come from [reviewRound]. */
    fun reviewSkill(subject: Subject): Skill = Skill(
        id = REVIEW_PREFIX + subject.name.lowercase(),
        subject = subject,
        title = txt("Repetisjon"),
        detail = txt("Øv på det som er vanskeleg", "Øv på det som er vanskelig"),
        grade = 0,
        symbol = "↻",
    ) { error("Review rounds are built from other levels with reviewRound") }

    /** A mixed round from [skills], taking turns so every weak level gets practice. */
    fun reviewRound(skills: List<Skill>, context: QuestionContext, count: Int = 8): List<Pair<Skill, Question>> {
        require(skills.isNotEmpty()) { "nothing to review" }
        val result = ArrayList<Pair<Skill, Question>>(count)
        val keys = HashSet<String>()
        var attempts = 0
        while (result.size < count) {
            val skill = skills[result.size % skills.size]
            val question = skill.generate(context)
            attempts++
            if (keys.add("${skill.id}:${question.key}") || attempts > count * 30) result += skill to question
        }
        return result
    }

    private const val REVIEW_PREFIX = "review_"

    private fun sameAnswer(a: Question, b: Question): Boolean {
        val left = a.answer
        val right = b.answer
        return when {
            left is Answer.NumberInput && right is Answer.NumberInput -> left.correct == right.correct
            // «Sant» twice in a row is normal; only a repeated number or word looks like a bug.
            left is Answer.Choice && right is Answer.Choice ->
                left.options[left.correct] !is Option.Verdict && left.options[left.correct] == right.options[right.correct]
            left is Answer.Trace && right is Answer.Trace -> left.symbol == right.symbol
            else -> false
        }
    }
}
