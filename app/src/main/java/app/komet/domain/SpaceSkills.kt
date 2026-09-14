package app.komet.domain

private fun space(
    id: String,
    title: Txt,
    detail: Txt,
    grade: Int,
    symbol: String,
    length: Int = 8,
    generate: (QuestionContext) -> Question,
) = Skill(id, Subject.SPACE, title, detail, grade, symbol, length, generate)

private fun SpaceThing.option() = Option.Art(art, name)

private fun PlanetInfo.option() = Option.Art(art, name)

/** Which picture fits the clue? */
private fun thingQuestion(ctx: QuestionContext, things: List<SpaceThing>): Question {
    val r = ctx.random
    val thing = r.pick(things)
    val others = things.filter { it !== thing }.shuffled(r).take(2)
    return Question(
        key = thing.id,
        prompt = txt("Finn rett bilete", "Finn riktig bilde"),
        visual = Visual.Story(thing.clue, reading = false),
        answer = choiceOf(thing, others, r) { it.option() },
        speech = thing.clue,
        reward = thing.name.map { "$it." },
    )
}

/** Full moon, half moon or new moon? */
private fun moonPhaseQuestion(ctx: QuestionContext): Question {
    val r = ctx.random
    val (emoji, name) = r.pick(SpaceContent.moonPhases)
    return Question(
        key = emoji,
        prompt = txt("Kva heiter denne månen?", "Hva heter denne månen?"),
        visual = Visual.Picture(emoji.asEmoji()),
        answer = choiceOf(name, SpaceContent.moonPhases.map { it.second }.filter { it != name }, r) { Option.Label(it, GlyphKind.PLAIN) },
        reward = name.map { "$it." },
    )
}

/** A statement to check, read aloud, with the reason afterwards. */
private fun factSkill(id: String, title: Txt, detail: Txt, grade: Int, symbol: String, length: Int, facts: () -> List<SpaceFact>) =
    space(id, title, detail, grade, symbol, length) { ctx ->
        val fact = ctx.random.pick(facts())
        Question(
            key = fact.statement.nn,
            prompt = txt("Sant eller usant?"),
            visual = Visual.Story(fact.statement, reading = false),
            answer = Answer.Choice(listOf(Option.Verdict(true), Option.Verdict(false)), if (fact.truth) 0 else 1),
            speech = txt("${fact.statement.nn} Er det sant?", "${fact.statement.nb} Er det sant?"),
            reward = fact.why,
            explanation = fact.why,
        )
    }

object SpaceCurriculum {

    val chapters: List<Chapter> = listOf(
        Chapter(
            id = "s_sol",
            subject = Subject.SPACE,
            title = txt("Sola, jorda og månen", "Solen, jorden og månen"),
            look = PlanetLook(0xFFFFC34D, 0xFFFFF0B8, 0xFFE07B12, craters = false),
            skills = listOf(
                space("s_basics", txt("Sol, jord og måne"), txt("Kva er kva?", "Hva er hva?"), 0, "☀", length = 6) { ctx ->
                    if (ctx.random.nextBoolean()) thingQuestion(ctx, SpaceContent.sunAndEarth) else moonPhaseQuestion(ctx)
                },
                factSkill("s_sun_facts", txt("Dag og natt"), txt("Sola og jorda", "Solen og jorden"), 1, "🌗", 8) { SpaceContent.sunFacts },
                factSkill("s_moon_facts", txt("Månen"), txt("Kan du hoppe høgt der?", "Kan du hoppe høyt der?"), 2, "🌙", 6) { SpaceContent.moonFacts },
            ),
        ),
        Chapter(
            id = "s_planetar",
            subject = Subject.SPACE,
            title = txt("Planetane", "Planetene"),
            look = PlanetLook(0xFFE9C98B, 0xFFFFF0CF, 0xFFA8834A, ring = true, bands = true),
            skills = listOf(
                space("s_planet_names", txt("Kva planet?", "Hvilken planet?"), txt("Jorda, Mars, Jupiter, Saturn", "Jorden, Mars, Jupiter, Saturn"), 1, "🪐", length = 6) { ctx ->
                    val r = ctx.random
                    val pool = SpaceContent.recognisable
                    val planet = r.pick(pool)
                    val others = pool.filter { it !== planet }.shuffled(r).take(2)
                    Question(
                        key = planet.id + others.map { it.id }.sorted().joinToString(""),
                        prompt = txt("Kva planet er dette?", "Hvilken planet er dette?"),
                        visual = Visual.Art(planet.art),
                        answer = choiceOf(planet, others, r) { Option.Label(it.name, GlyphKind.PLAIN) },
                        reward = planet.name.map { "$it." },
                        explanation = planet.clue,
                    )
                },
                space("s_planet_clues", txt("Kven er eg?", "Hvem er jeg?"), txt("Finn planeten", "Finn planeten"), 2, "?") { ctx ->
                    val r = ctx.random
                    val planet = r.pick(SpaceContent.planets)
                    val others = SpaceContent.planets.filter { it !== planet }.shuffled(r).take(2)
                    Question(
                        key = planet.id,
                        prompt = txt("Kven er eg?", "Hvem er jeg?"),
                        visual = Visual.Story(planet.clue, reading = false),
                        speech = planet.clue.map { "$it " } + txt("Kven er eg?", "Hvem er jeg?"),
                        answer = choiceOf(planet, others, r) { it.option() },
                        reward = planet.name.map { "$it." },
                    )
                },
                space("s_planet_size", txt("Størst og minst"), txt("Kva planet er størst?", "Hvilken planet er størst?"), 2, "⬆") { ctx ->
                    val r = ctx.random
                    val three = SpaceContent.planets.shuffled(r).take(3)
                    val biggest = r.nextBoolean()
                    val answer = if (biggest) three.minBy { it.sizeRank } else three.maxBy { it.sizeRank }
                    Question(
                        key = three.map { it.id }.sorted().joinToString("") + biggest,
                        prompt = if (biggest) txt("Kva planet er størst?", "Hvilken planet er størst?") else txt("Kva planet er minst?", "Hvilken planet er minst?"),
                        // The drawings are all the same size, so the child needs to know, not look.
                        visual = Visual.Story(txt("Bileta er like store. Tenk på kor store planetane er i verkelegheita.", "Bildene er like store. Tenk på hvor store planetene er i virkeligheten."), reading = false),
                        answer = Answer.Choice(three.map { it.option() }, three.indexOf(answer)),
                        reward = answer.name.map { "$it." },
                        explanation = answer.clue,
                    )
                },
                space("s_planet_order", txt("Rekkjefølgja", "Rekkefølgen"), txt("Frå sola og utover", "Fra solen og utover"), 3, "1-2-3", length = 5) { ctx ->
                    val r = ctx.random
                    val start = r.nextInt(0, 5)
                    val row = SpaceContent.planets.subList(start, start + 4)
                    val names = row.map { it.name.get(ctx.maalform) }
                    var tiles = names.shuffled(r)
                    var guard = 0
                    while (tiles == names && guard < 10) {
                        tiles = names.shuffled(r)
                        guard++
                    }
                    Question(
                        key = "order$start",
                        prompt = txt("Ordne planetane frå sola og utover", "Ordne planetene fra solen og utover"),
                        visual = Visual.None,
                        answer = Answer.Build(target = names, tiles = tiles, kind = GlyphKind.PLAIN),
                        speech = txt("Ordne planetane. Den som er nærast sola, kjem først.", "Ordne planetene. Den som er nærmest solen, kommer først."),
                        reward = txt(names.joinToString(", ") + "."),
                        explanation = txt(names.joinToString(" → ")),
                    )
                },
            ),
        ),
        Chapter(
            id = "s_romfart",
            subject = Subject.SPACE,
            title = txt("Romfart"),
            look = PlanetLook(0xFFB0BEC5, 0xFFE8EEF2, 0xFF5E7380, craters = true),
            skills = listOf(
                space("s_travel_things", txt("Ut i rommet"), txt("Rakett, astronaut og satellitt"), 0, "🚀", length = 4) { ctx ->
                    thingQuestion(ctx, SpaceContent.spaceTravel)
                },
                factSkill("s_travel_facts", txt("Livet i romstasjonen"), txt("Korleis er det å vere astronaut?", "Hvordan er det å være astronaut?"), 2, "🧑‍🚀", 5) { SpaceContent.travelFacts },
            ),
        ),
        Chapter(
            id = "s_himmelen",
            subject = Subject.SPACE,
            title = txt("Stjernehimmelen"),
            look = PlanetLook(0xFF5C6BC0, 0xFFC5CAE9, 0xFF283593, bands = true),
            skills = listOf(
                space("s_sky_things", txt("På himmelen"), txt("Nordlys, stjerneskot og komet", "Nordlys, stjerneskudd og komet"), 1, "✨", length = 4) { ctx ->
                    thingQuestion(ctx, SpaceContent.skyThings)
                },
                factSkill("s_sky_facts", txt("Stjernene"), txt("Sant eller usant om stjernene"), 2, "⭐", 6) { SpaceContent.skyFacts },
                factSkill("s_quiz", txt("Stor romquiz"), txt("Alt om verdsrommet", "Alt om verdensrommet"), 3, "🏆", 8) { SpaceContent.allFacts },
            ),
        ),
    )
}
