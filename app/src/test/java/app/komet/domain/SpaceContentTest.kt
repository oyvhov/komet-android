package app.komet.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SpaceContentTest {

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
