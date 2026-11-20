package app.komet.domain

/** Each kind of practice has a home, in the order planets appear from the Sun. */
enum class SolarDestination(
    val planetId: String,
    val topics: List<Topic>,
    val trail: Subject? = null,
) {
    MERCURY("merkur", listOf(Topic.COUNTING, Topic.PLUS, Topic.WRITE_DIGITS), Subject.MATH),
    VENUS("venus", listOf(Topic.MINUS, Topic.MIXED, Topic.WORD_PROBLEMS)),
    EARTH(
        "jorda",
        listOf(Topic.GEOGRAPHY, Topic.LETTERS, Topic.SOUNDS, Topic.RHYME, Topic.WORDS, Topic.SPELLING, Topic.SENTENCES, Topic.WRITE_LETTERS, Topic.SP_SUN_MOON),
        Subject.READING,
    ),
    MARS("mars", listOf(Topic.SHAPES, Topic.TIMES, Topic.MISSIONS)),
    JUPITER("jupiter", listOf(Topic.EN_PHRASES, Topic.EN_COLOURS_NUMBERS, Topic.EN_WORDS, Topic.EN_SPELL), Subject.ENGLISH),
    SATURN("saturn", listOf(Topic.CLOCK, Topic.MONEY)),
    URANUS("uranus", listOf(Topic.SP_PLANETS, Topic.SP_TRAVEL)),
    NEPTUNE("neptun", listOf(Topic.SP_SKY), Subject.SPACE);

    val planet: PlanetInfo get() = SpaceContent.planets.first { it.id == planetId }
    val missionId: String get() = "s_visit_$planetId"

    companion object {
        fun forSkill(skill: Skill): SolarDestination? {
            val topic = Topics.of(skill)
            return entries.firstOrNull { it.missionId == skill.id }
                ?: entries.firstOrNull { topic in it.topics }
        }

        fun stamps(profile: Profile): Set<SolarDestination> =
            entries.filterTo(linkedSetOf()) { profile.stars(it.missionId) > 0 }
    }
}

internal object PlanetMissions {
    val chapter = Chapter(
        id = "s_planetoppdrag",
        subject = Subject.SPACE,
        title = txt("Planetoppdrag"),
        look = PlanetLook(0xFF6E8DAA, 0xFFC0DEEB, 0xFF294966, ring = true),
        skills = SolarDestination.entries.map { destination ->
            Skill(
                id = destination.missionId,
                subject = Subject.SPACE,
                title = destination.planet.name,
                detail = txt("Bilete, spor og plassen i solsystemet", "Bilder, spor og plassen i solsystemet"),
                grade = 0,
                symbol = "🪐",
                length = 3,
            ) { ctx -> question(destination.planet, ctx) }
        },
    )

    private fun question(planet: PlanetInfo, context: QuestionContext): Question {
        val random = context.random
        val others = SpaceContent.planets.filter { it != planet }.shuffled(random).take(2)
        val name = planet.name
        return when (random.nextInt(3)) {
            0 -> Question(
                key = "picture",
                prompt = txt("Kva planet er dette?", "Hvilken planet er dette?"),
                visual = Visual.Art(planet.art),
                answer = choiceOf(planet, others, random) { Option.Label(it.name, GlyphKind.PLAIN) },
                explanation = planet.clue,
            )
            1 -> Question(
                key = "order",
                prompt = txt(
                    "Kva nummer har ${name.nn} frå sola?",
                    "Hvilket nummer har ${name.nb} fra solen?",
                ),
                visual = Visual.Art(planet.art),
                answer = choiceOf(planet.fromSun, others.map { it.fromSun }, random) {
                    Option.Label(txt(it.toString()), GlyphKind.NUMBER)
                },
                explanation = txt(
                    "${name.nn} er planet nummer ${planet.fromSun} frå sola.",
                    "${name.nb} er planet nummer ${planet.fromSun} fra solen.",
                ),
            )
            else -> Question(
                key = "clue",
                prompt = txt("Kven er eg?", "Hvem er jeg?"),
                visual = Visual.Story(planet.clue, reading = false),
                speech = planet.clue.map { "$it " } + txt("Kven er eg?", "Hvem er jeg?"),
                answer = choiceOf(planet, others, random) { Option.Art(it.art, it.name) },
                explanation = name.map { "$it!" },
            )
        }
    }
}
