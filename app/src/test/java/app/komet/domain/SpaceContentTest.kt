package app.komet.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SpaceContentTest {

    @Test
    fun `atlas covers all topics exactly once and every real planet has its own mission`() {
        val destinations = SolarDestination.entries
        val expectedOrder = listOf("merkur", "venus", "jorda", "mars", "jupiter", "saturn", "uranus", "neptun")
        assertEquals(expectedOrder, destinations.map { it.planet.id })
        assertEquals(Topics.all.sorted(), destinations.flatMap { it.topics }.sorted())
        assertEquals(Subject.entries, destinations.mapNotNull { it.trail })
        assertTrue(destinations.all { it.topics.isNotEmpty() })
        assertEquals(8, destinations.map { it.missionId }.toSet().size)
        Curriculum.skills.forEach { skill -> assertTrue(skill.id, SolarDestination.forSkill(skill) != null) }
        destinations.forEach { destination ->
            val skill = requireNotNull(Curriculum.skill(destination.missionId))
            assertEquals(destination, SolarDestination.forSkill(skill))
            assertEquals(Subject.SPACE, skill.subject)
        }
    }

    @Test
    fun `planet missions teach the picture orbit order and clue for their own planet`() {
        val orderFromSun = listOf("merkur", "venus", "jorda", "mars", "jupiter", "saturn", "uranus", "neptun")
        for (destination in SolarDestination.entries) {
            val mission = requireNotNull(Curriculum.skill(destination.missionId))
            val planet = SpaceContent.planets.single { it.id == destination.planetId }
            for (maalform in Maalform.entries) {
                repeat(25) { seed ->
                    val round = Curriculum.round(mission, QuestionContext(Random(seed), maalform))
                    assertEquals(setOf("picture", "order", "clue"), round.map { it.key }.toSet())
                    for (question in round) {
                        val answer = question.answer as Answer.Choice
                        val expected = when (question.key) {
                            "picture" -> Option.Label(planet.name, GlyphKind.PLAIN)
                            "order" -> Option.Label(txt((orderFromSun.indexOf(planet.id) + 1).toString()), GlyphKind.NUMBER)
                            else -> Option.Art(planet.art, planet.name)
                        }
                        assertEquals(1, answer.options.count { it == expected })
                        assertEquals(expected, answer.options[answer.correct])
                        assertTrue(requireNotNull(question.explanation).get(maalform).isNotBlank())
                    }
                }
            }
        }
    }

    @Test
    fun `planets are the eight of the solar system in the right order and size`() {
        val planets = SpaceContent.planets
        assertEquals(8, planets.size)
        assertEquals((1..8).toList(), planets.map { it.fromSun })
        assertEquals("Jupiter is the biggest", "jupiter", planets.minBy { it.sizeRank }.id)
        assertEquals("Mercury is the smallest", "merkur", planets.maxBy { it.sizeRank }.id)
        assertEquals((1..8).toSet(), planets.map { it.sizeRank }.toSet())
        // Every planet has a drawing; this throws if a card is missing.
        planets.forEach { it.art }
    }

    @Test
    fun `facts are balanced and explained in both written standards`() {
        val facts = SpaceContent.allFacts
        assertEquals(facts.size, facts.map { it.statement.nn }.toSet().size)
        assertTrue("too few false statements", facts.count { !it.truth } >= facts.size / 4)
        assertTrue("too few true statements", facts.count { it.truth } >= facts.size / 4)
        facts.forEach {
            listOf(it.statement.nn, it.statement.nb, it.why.nn, it.why.nb).forEach { text -> assertTrue(text.isNotBlank()) }
        }
    }

    @Test
    fun `the planet order task names the planets from the sun outwards`() {
        val skill = Curriculum.skill("s_planet_order")!!
        repeat(40) { seed ->
            val question = skill.generate(QuestionContext(Random(seed), Maalform.BOKMAAL, LetterCase.UPPER))
            val build = question.answer as Answer.Build
            val order = build.target.map { name -> SpaceContent.planets.first { it.name.nb == name }.fromSun }
            assertEquals(order.sorted(), order)
            assertEquals(order.first() + 3, order.last())
        }
    }

    @Test
    fun `size questions pick the biggest or smallest shown`() {
        val skill = Curriculum.skill("s_planet_size")!!
        repeat(80) { seed ->
            val question = skill.generate(QuestionContext(Random(seed)))
            val answer = question.answer as Answer.Choice
            val shown = answer.options.map { option -> SpaceContent.planets.first { (option as Option.Art).caption == it.name } }
            val right = shown[answer.correct]
            val biggest = "størst" in question.prompt.nn
            assertEquals(question.key, if (biggest) shown.minBy { it.sizeRank } else shown.maxBy { it.sizeRank }, right)
        }
    }
}
