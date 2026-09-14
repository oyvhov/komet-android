package app.komet.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TopicsTest {

    @Test
    fun `every level has a topic in its own subject`() {
        Curriculum.skills.forEach { skill ->
            val topic = Topics.of(skill)
            assertNotNull("${skill.id} has no topic, so it cannot be found in Utforsk", topic)
            assertEquals("${skill.id} is filed under the wrong subject", skill.subject, topic!!.subject)
        }
    }

    @Test
    fun `topics list their levels from easy to hard and are not empty`() {
        Topics.all.forEach { topic ->
            val skills = Topics.skills(topic)
            assertTrue("${topic.name} is empty", skills.isNotEmpty())
            skills.zipWithNext().forEach { (a, b) -> assertTrue("${topic.name}: ${a.id} before ${b.id}", a.grade <= b.grade) }
        }
        assertEquals(Curriculum.skills.size, Topics.all.sumOf { Topics.skills(it).size })
    }

    @Test
    fun `topic titles exist in both written standards`() {
        Topic.entries.forEach {
            assertTrue(it.title.nn.isNotBlank())
            assertTrue(it.title.nb.isNotBlank())
        }
    }
}
