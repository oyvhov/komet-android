package app.komet.domain

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class MissionContentTest {
    @Test fun `mission answers follow the information the child sees in both languages`() {
        for (form in Maalform.entries) {
            for (skill in MissionCurriculum.chapters.flatMap { it.skills }) {
                val ctx = QuestionContext(Random(431 + skill.id.hashCode()), form)
                repeat(500) {
                    val q = skill.generate(ctx)
                    val story = QuestionChecks.visuals(q.visual).filterIsInstance<Visual.Story>().single().text
                    val text = if (form == Maalform.NYNORSK) story.nn else story.nb
                    val n = Regex("\\d+").findAll(text).map { it.value.toInt() }.toList()
                    val expected = when (skill.id) {
                        "m_mission_collect" -> n[0] + n[1]
                        "m_mission_lights", "m_mission_fuel10", "m_mission_fuel", "m_mission_measure" -> n[0] - n[1]
                        "m_mission_route" -> {
                            val steps = n.zipWithNext { a, b -> b - a }
                            assertEquals(1, steps.distinct().size)
                            n.last() + steps.first()
                        }
                        "m_mission_cargo" -> n[0] + n[1] - n[2]
                        "m_mission_robots" -> n[0] * n[1]
                        "m_mission_picnic" -> {
                            assertEquals(0, n[1] % n[0])
                            n[1] / n[0]
                        }
                        "m_mission_packs" -> n[0] * n[1] + n[2]
                        "m_mission_code" -> n[0] * 100 + n[1] * 10 + n[2]
                        "m_mission_code10" -> n[0] * 10 + n[1]
                        else -> error("Missing independent check for ${skill.id}")
                    }
                    assertEquals("${skill.id}: $text", expected, QuestionChecks.correctNumber(q.answer))
                    assertTrue(expected in 0..999)
                    if (skill.grade == 0) {
                        val groups = QuestionChecks.visuals(q.visual).filterIsInstance<Visual.AddGroups>().single()
                        assertEquals(expected, groups.a + groups.b)
                    } else assertNotNull(q.hint)
                    assertTrue(q.explanation!!.nn.isNotBlank())
                    assertTrue(q.explanation.nb.isNotBlank())
                    assertTrue(q.speech.nn.contains(story.nn))
                    assertTrue(q.speech.nb.contains(story.nb))
                    QuestionChecks.structure(skill, q)
                }
            }
        }
    }

    @Test fun `missions remain varied over many replays and preserve progression`() {
        MissionCurriculum.chapters.single().skills.forEach { skill ->
            val ctx = QuestionContext(Random(93))
            val keys = mutableSetOf<String>()
            repeat(20) {
                val round = Curriculum.round(skill, ctx)
                assertEquals(skill.length, round.size)
                assertEquals(skill.length, round.map { it.key }.distinct().size)
                keys.addAll(round.map { it.key })
            }
            val minimum = when (skill.id) {
                "m_mission_collect" -> 10
                "m_mission_fuel10" -> 9
                "m_mission_route" -> 13
                else -> 19
            }
            assertTrue("${skill.id}: too little variety", keys.size >= minimum)
            assertEquals(Topic.MISSIONS, Topics.of(skill))
        }
    }

    @Test fun `early missions have bounded answers and manageable rounds`() {
        val limits = mapOf("m_mission_collect" to 5, "m_mission_lights" to 10,
            "m_mission_fuel10" to 10, "m_mission_route" to 20, "m_mission_fuel" to 20,
            "m_mission_cargo" to 20, "m_mission_robots" to 30, "m_mission_code10" to 99,
            "m_mission_code" to 999)
        limits.forEach { (id, max) ->
            val skill = Curriculum.skill(id)!!
            assertTrue(skill.length in 5..6)
            val ctx = QuestionContext(Random(927))
            repeat(500) {
                val answer = skill.generate(ctx).answer as Answer.Choice
                assertEquals(if (skill.grade == 0) 3 else 4, answer.options.size)
                answer.options.forEach { assertTrue((it as Option.Label).text.nn.toInt() in 0..max) }
            }
        }
    }
}
