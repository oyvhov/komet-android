package app.komet.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class PlanetPassportTest {
    @Test fun huntGlobePositionsMatchTheClueInBothLanguages() {
        val hunt = requireNotNull(Curriculum.skill("s_planet_clues"))
        for (language in Maalform.entries) {
            repeat(50) { seed ->
                val question = hunt.generate(QuestionContext(kotlin.random.Random(seed), language))
                val clue = (question.visual as Visual.Story).text.get(language)
                val target = SolarDestination.entries.single { it.planet.clue.get(language) == clue }
                val answer = question.answer as Answer.Choice
                val shown = answer.options.map { option ->
                    SolarDestination.entries.single { it.planet.art == (option as Option.Art).art }
                }
                assertEquals(3, shown.toSet().size)
                assertEquals(target, shown[answer.correct])
            }
        }
    }

    @Test fun onlyFinishedPlanetMissionsEarnStampsIncludingOnOlderProfiles() {
        val pilot = Profile("a", "Ada", 0, 1, skills = mapOf(
            "m_add5" to SkillStats(bestStars = 3),
            "s_visit_mars" to SkillStats(answered = 2),
            "s_visit_jorda" to SkillStats(bestStars = 1),
            "s_visit_saturn" to SkillStats(bestStars = 3, plays = 5),
        ))
        assertEquals(setOf(SolarDestination.EARTH, SolarDestination.SATURN), SolarDestination.stamps(pilot))
        assertEquals(emptySet<SolarDestination>(), SolarDestination.stamps(Profile("b", "Leo", 1, 1)))
        for (planet in SolarDestination.entries) {
            val mission = requireNotNull(Curriculum.skill(planet.missionId))
            val played = Progression.applyRound(pilot, mission, 0, 3, 20, 1, 3, 100L).profile
            assertEquals(SolarDestination.stamps(pilot) + planet, SolarDestination.stamps(played))
        }
    }
}
