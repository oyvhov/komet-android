package app.komet.domain

object Curriculum {
    val chapters: List<Chapter> = MathCurriculum.chapters + ReadingCurriculum.chapters

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

    private fun sameAnswer(a: Question, b: Question): Boolean {
        val left = a.answer
        val right = b.answer
        return when {
            left is Answer.NumberInput && right is Answer.NumberInput -> left.correct == right.correct
            left is Answer.Choice && right is Answer.Choice -> left.options[left.correct] == right.options[right.correct]
            left is Answer.Trace && right is Answer.Trace -> left.symbol == right.symbol
            else -> false
        }
    }
}
