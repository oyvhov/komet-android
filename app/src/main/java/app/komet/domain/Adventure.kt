package app.komet.domain

/** A short story uses existing, tested learning tasks. Finishing, including with help, changes the world. */
data class AdventureQuest(
    val id: String,
    val subject: Subject,
    val title: Txt,
    val request: Txt,
    val thanks: Txt,
    val skillsByGrade: List<String>,
) {
    fun skill(grade: Int): Skill = requireNotNull(Curriculum.skill(skillsByGrade[grade.coerceIn(0, 3)]))
    fun accepts(skillId: String): Boolean = skillId in skillsByGrade
}

data class PlanetAdventure(
    val subject: Subject,
    val resident: String,
    val title: Txt,
    val ending: Txt,
    val quests: List<AdventureQuest>,
)

object Adventures {
    const val ROUND_LENGTH = 5

    val all = listOf(
        PlanetAdventure(Subject.MATH, "Pip", txt("Krystallfyret"),
            txt("Fyret lyser! No finn romskipa vegen heim. Pip vinkar til deg frå krystallane.",
                "Fyret lyser! Nå finner romskipene veien hjem. Pip vinker til deg fra krystallene."),
            listOf(
                AdventureQuest("math_energy", Subject.MATH, txt("Finn energien"),
                    txt("Eg er Pip! Krystallfyret har slokna. Hjelp meg å rekne, så får vi energi til det første lyset.",
                        "Jeg er Pip! Krystallfyret har sluknet. Hjelp meg å regne, så får vi energi til det første lyset."),
                    txt("Den første krystallen lyser! Pip har sett energien på plass."),
                    listOf("m_mission_collect", "m_mission_fuel10", "m_mission_fuel", "m_mission_packs")),
                AdventureQuest("math_repair", Subject.MATH, txt("Vekk krystallane", "Vekk krystallene"),
                    txt("Eitt lys er tent! No treng vi talhjelpa di for å få den neste krystallen til å vakne.",
                        "Ett lys er tent! Nå trenger vi tallhjelpen din for å få den neste krystallen til å våkne."),
                    txt("To krystallar skin! Lyset klatrar oppover fyret.", "To krystaller skinner! Lyset klatrer oppover fyret."),
                    listOf("m_count5", "m_mission_lights", "m_mission_robots", "m_times_all")),
                AdventureQuest("math_beacon", Subject.MATH, txt("Tenn fyret"),
                    txt("Berre toppljuset står att. Finn mønster og kodar, så kan vi sende lyset heilt ut i rommet!",
                        "Bare topplyset er igjen. Finn mønstre og koder, så kan vi sende lyset helt ut i rommet!"),
                    txt("Du tende fyret! Ein lysstråle viser romskipa vegen heim.", "Du tente fyret! En lysstråle viser romskipene veien hjem."),
                    listOf("m_patterns", "m_mission_route", "m_mission_code10", "m_mission_code")),
            )),
        PlanetAdventure(Subject.READING, "Mira", txt("Signalbrua", "Signalbroen"),
            txt("Brua strålar! Mira kan sende forteljingar til vennene på den andre sida.",
                "Broen stråler! Mira kan sende fortellinger til vennene på den andre siden."),
            listOf(
                AdventureQuest("reading_signals", Subject.READING, txt("Finn signala", "Finn signalene"),
                    txt("Eg er Mira! Signalbrua har mista lyset sitt. Hjelp meg med bokstavane, så vaknar den første delen.",
                        "Jeg er Mira! Signalbroen har mistet lyset sitt. Hjelp meg med bokstavene, så våkner den første delen."),
                    txt("Dei første signala er framme! Ein del av brua lyser.", "De første signalene er fremme! En del av broen lyser."),
                    listOf("r_letters1", "r_letters3", "r_letters5", "r_abc")),
                AdventureQuest("reading_words", Subject.READING, txt("Ord over juvet"),
                    txt("Vi er på veg! Hjelp meg med orda, så kan signalet hoppe vidare over juvet.",
                        "Vi er på vei! Hjelp meg med ordene, så kan signalet hoppe videre over juvet."),
                    txt("Orda kom fram! No skin to delar av brua.", "Ordene kom frem! Nå skinner to deler av broen."),
                    listOf("r_build3", "r_word_pic", "r_build4", "r_compound")),
                AdventureQuest("reading_bridge", Subject.READING, txt("Lys på andre sida", "Lys på den andre siden"),
                    txt("Vennene ventar på den andre sida. Eitt leseoppdrag til, så når lyset heilt fram!",
                        "Vennene venter på den andre siden. Ett leseoppdrag til, så når lyset helt frem!"),
                    txt("Heile brua lyser! Mira sender den første helsinga til vennene sine.",
                        "Hele broen lyser! Mira sender den første hilsenen til vennene sine."),
                    listOf("r_first1", "r_sentence_pic", "r_missing_word", "r_story")),
            )),
        PlanetAdventure(Subject.ENGLISH, "Rio", txt("Stjernehagen"),
            txt("Hagen blømer! Rio og smådyra har fått ein lysande stad å møtast.",
                "Hagen blomstrer! Rio og smådyrene har fått et lysende sted å møtes."),
            listOf(
                AdventureQuest("english_colours", Subject.ENGLISH, txt("Fargar til hagen", "Farger til hagen"),
                    txt("Eg er Rio! Frøa mine lyser når dei høyrer engelske ord. Finn fargane, så kan den første blomsten vakne.",
                        "Jeg er Rio! Frøene mine lyser når de hører engelske ord. Finn fargene, så kan den første blomsten våkne."),
                    txt("Den første stjerneblomsten opnar seg! Rio jublar.", "Den første stjerneblomsten åpner seg! Rio jubler."),
                    listOf("e_colours_listen", "e_colours_read", "e_colours_read", "e_colours_read")),
                AdventureQuest("english_animals", Subject.ENGLISH, txt("Vennene kjem", "Vennene kommer"),
                    txt("Smådyra vil også vere med. Hjelp meg med dyrenamna på engelsk, så finn dei hagen!",
                        "Smådyrene vil også være med. Hjelp meg med dyrenavnene på engelsk, så finner de hagen!"),
                    txt("Vennene fann fram! Endå ein blomst spring ut.", "Vennene fant frem! Enda en blomst springer ut."),
                    listOf("e_animals_listen", "e_animals_read", "e_animals_translate", "e_animals_translate")),
                AdventureQuest("english_garden", Subject.ENGLISH, txt("Hagefest i rommet"),
                    txt("No gjer vi klart til hagefest. Hjelp meg med nokre engelske ord til, så opnar den siste blomsten seg!",
                        "Nå gjør vi klart til hagefest. Hjelp meg med noen engelske ord til, så åpner den siste blomsten seg!"),
                    txt("Alle blomstrane lyser! Rio inviterer deg til hagefest.", "Alle blomstene lyser! Rio inviterer deg til hagefest."),
                    listOf("e_hello1", "e_food_listen", "e_food_read", "e_spell5")),
            )),
        PlanetAdventure(Subject.SPACE, "Vega", txt("Stjernekikkaren", "Stjernekikkeren"),
            txt("Teleskopet er klart! Vega har funne eit nytt stjernemønster saman med deg.",
                "Teleskopet er klart! Vega har funnet et nytt stjernemønster sammen med deg."),
            listOf(
                AdventureQuest("space_moon", Subject.SPACE, txt("Finn lyset på himmelen"),
                    txt("Eg er Vega! Stjernekikkaren treng hjelp. Vis kva du veit om sola, jorda og månen, så får vi straum.",
                        "Jeg er Vega! Stjernekikkeren trenger hjelp. Vis hva du vet om sola, jorda og månen, så får vi strøm."),
                    txt("Straumen er tilbake! Teleskopet byrjar å vakne.", "Strømmen er tilbake! Teleskopet begynner å våkne."),
                    listOf("s_basics", "s_sun_facts", "s_moon_facts", "s_moon_facts")),
                AdventureQuest("space_planets", Subject.SPACE, txt("Sikt mot planetane", "Sikt mot planetene"),
                    txt("No må vi rette kikkaren inn. Hjelp meg å kjenne att det som finst ute i rommet!",
                        "Nå må vi rette kikkeren inn. Hjelp meg å kjenne igjen det som finnes ute i rommet!"),
                    txt("Linsa er på plass! Vega siktar mot den mørke himmelen.", "Linsen er på plass! Vega sikter mot den mørke himmelen."),
                    listOf("s_travel_things", "s_planet_names", "s_planet_clues", "s_planet_order")),
                AdventureQuest("space_stars", Subject.SPACE, txt("Opne stjernekikkaren", "Åpne stjernekikkeren"),
                    txt("Vi er nesten klare! Fullfør det siste romoppdraget, så opnar vi luka og ser kva som lyser der oppe.",
                        "Vi er nesten klare! Fullfør det siste romoppdraget, så åpner vi luken og ser hva som lyser der oppe."),
                    txt("Sjå, eit stjernemønster! Du og Vega fekk teleskopet til å verke.",
                        "Se, et stjernemønster! Du og Vega fikk teleskopet til å virke."),
                    listOf("s_basics", "s_sky_things", "s_sky_facts", "s_quiz")),
            )),
    )

    fun of(subject: Subject): PlanetAdventure = all.first { it.subject == subject }
    fun quest(id: String?): AdventureQuest? = all.asSequence().flatMap { it.quests.asSequence() }.firstOrNull { it.id == id }
    fun completed(profile: Profile, subject: Subject): Int = of(subject).quests.takeWhile { it.id in profile.adventures }.size
    fun next(profile: Profile, subject: Subject): AdventureQuest? = of(subject).quests.getOrNull(completed(profile, subject))

    /** A round only belongs to the explicitly chosen, current chapter, never ordinary practice. */
    fun complete(profile: Profile, id: String?, skillId: String): Profile {
        val quest = quest(id) ?: return profile
        if (next(profile, quest.subject) != quest || !quest.accepts(skillId)) return profile
        return profile.copy(adventures = profile.adventures + quest.id)
    }

    fun validRound(id: String?, skillId: String): Boolean = quest(id)?.accepts(skillId) == true
}
