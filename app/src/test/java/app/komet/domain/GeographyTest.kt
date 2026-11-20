package app.komet.domain

import kotlin.math.abs
import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

class GeographyTest {
    private val capitals = mapOf("NO" to "Oslo", "SE" to "Stockholm", "DK" to "København", "FI" to "Helsinki",
        "IS" to "Reykjavík", "FR" to "Paris", "IT" to "Roma", "JP" to "Tokyo", "NG" to "Abuja",
        "PE" to "Lima", "PW" to "Ngerulmud", "BS" to "Nassau")
    private val names = mapOf("NO" to "Norge", "SE" to "Sverige", "DK" to "Danmark", "FI" to "Finland",
        "IS" to "Island", "FR" to "Frankrike", "IT" to "Italia", "JP" to "Japan", "NG" to "Nigeria",
        "PE" to "Peru", "PW" to "Palau", "BS" to "Bahamas")

    private fun questions(id: String): List<Question> {
        val context = QuestionContext(Random(46752))
        val skill = requireNotNull(Curriculum.skill("g_$id"))
        return List(300) { skill.generate(context) }
    }

    @Test fun atlasIsPlayableAtEverySupportedGradeAndFromEarth() {
        val skills = Topics.skills(Topic.GEOGRAPHY)
        assertEquals(11, skills.size)
        assertEquals((0..3).toSet(), skills.map { it.grade }.toSet())
        skills.forEach { assertEquals(SolarDestination.EARTH, SolarDestination.forSkill(it)) }
        assertTrue(Topic.GEOGRAPHY in SolarDestination.EARTH.topics)
    }

    @Test fun flagsCountriesAndTheirCapitalsAgreeInBothStandards() {
        for (id in listOf("flag_pair", "nordic", "flags", "capitals", "world_capitals")) {
            val generated = questions(id)
            val pool = if (id == "nordic" || id == "capitals") setOf("NO", "SE", "FI", "DK", "IS") else capitals.keys
            assertEquals(pool, generated.map { (it.visual as Visual.Flag).country.name }.toSet())
            generated.forEach { q ->
                val flag = (q.visual as Visual.Flag).country
                val answer = q.answer as Answer.Choice
                val correct = answer.options[answer.correct]
                if (id == "flag_pair") assertEquals(Option.Flag(flag), correct)
                else {
                    val label = (correct as Option.Label).text
                    val expected = if (id.contains("capitals")) capitals.getValue(flag.name) else names.getValue(flag.name)
                    assertEquals(expected, label.nb)
                    assertEquals(if (expected == "Norge") "Noreg" else expected, label.nn)
                }
                assertEquals(1, answer.options.count { it == correct })
            }
        }
    }

    @Test fun plottedPointsActuallyIdentifyTheRightLandmassOrOcean() {
        // Independent interior regions, in degrees east and north. Each contains one atlas pin.
        val interior = mapOf(
            "Europa" to (10..17 to 48..55), "Afrika" to (18..24 to 0..8), "Asia" to (89..100 to 45..54),
            "Nord-Amerika" to (-107..-98 to 45..53), "Sør-Amerika" to (-62..-51 to -18..-8),
            "Oseania" to (128..138 to -29..-20), "Antarktis" to (27..40 to -84..-74),
            "Stillehavet" to (-148..-130 to -5..10), "Atlanterhavet" to (-42..-29 to 17..26),
            "Indiahavet" to (70..81 to -27..-16), "Nordishavet" to (65..81 to 81..87),
            "Sørishavet" to (63..74 to -66..-62),
        )
        val oceans = setOf("Atlanterhavet", "Stillehavet", "Indiahavet", "Nordishavet", "Sørishavet")
        for (skill in listOf("land_sea", "continents", "oceans")) {
            val generated = questions(skill)
            assertEquals(when (skill) { "land_sea" -> 12; "oceans" -> 5; else -> 7 }, generated.map { it.visual }.distinct().size)
            generated.forEach { q ->
                val pin = (q.visual as Visual.WorldPin).place
                val expected = interior.entries.single { (_, region) -> pin.longitude in region.first && pin.latitude in region.second }.key
                val label = if (skill == "land_sea") { if (expected in oceans) "I havet" else "På land" } else expected
                assertEquals(label, QuestionChecks.correctLabel(q.answer))
            }
        }
    }

    @Test fun compassCoordinatesAndScaleAreSolvedFromTheDrawnCells() {
        for (skill in listOf("grid", "compass", "scale")) {
            questions(skill).forEach { q ->
                val map = q.visual as Visual.SeaChart
                val dx = map.tower.column - map.boat.column
                val dy = map.tower.row - map.boat.row
                if (map.harbours.isEmpty()) assertTrue((dx == 0) xor (dy == 0))
                else {
                    assertEquals(3, map.harbours.toSet().size)
                    assertFalse(map.boat in map.harbours)
                    assertEquals(map.tower, map.harbours[(q.answer as Answer.Choice).correct])
                    assertTrue(q.prompt.nn.contains(map.tower.address))
                }
                val expected = when (skill) {
                    "grid" -> "${"ABCD"[map.tower.column]}${map.tower.row + 1}"
                    "compass" -> when { dx < 0 -> "Vest"; dx > 0 -> "Aust"; dy < 0 -> "Nord"; else -> "Sør" }
                    else -> "${maxOf(abs(dx), abs(dy)) * requireNotNull(map.kmPerStep)} km"
                }
                assertEquals(expected, QuestionChecks.correctLabel(q.answer))
            }
        }
    }
}
