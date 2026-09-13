package app.komet.data

import app.komet.domain.AppState
import app.komet.domain.DayStats
import app.komet.domain.LetterCase
import app.komet.domain.Maalform
import app.komet.domain.Profile
import app.komet.domain.RaceMode
import app.komet.domain.Settings
import app.komet.domain.SkillStats
import app.komet.domain.Subject
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class StateStoreTest {

    private val state = AppState(
        profiles = listOf(
            Profile(
                id = "a",
                name = "Æsa Ørn",
                avatar = 3,
                grade = 1,
                maalform = Maalform.BOKMAAL,
                letterCase = LetterCase.LOWER,
                unlockAll = true,
                skills = mapOf("m_add5" to SkillStats(3, 2, 16, 14, 99L)),
                totalStars = 42,
                streak = 4,
                lastGoalDay = 20_100L,
                days = mapOf(20_100L to DayStats(3, 400, 24, 20)),
                raceBest = mapOf(RaceMode.MULTIPLY to 17),
                lastSubject = Subject.READING,
                seenCards = 5,
                createdAt = 1234L,
            ),
            Profile(id = "b", name = "Bror", avatar = 0, grade = 0),
        ),
        activeProfileId = "a",
        settings = Settings(sound = false, speech = true, slowSpeech = false, haptics = false, autoRead = false, dailyGoal = 5),
    )

    @Test
    fun `state survives a round trip`() {
        val decoded = StateStore.decode(JSONObject(StateStore.encode(state).toString()))
        assertEquals(state, decoded)
    }

    @Test
    fun `unknown or damaged values fall back to defaults`() {
        val json = JSONObject("""{"profiles":[{"id":"x","maalform":"KLINGON","grade":9,"skills":{"m_add5":{"bestStars":7}},"raceBest":{"NOPE":3}}, {"name":"no id"}]}""")
        val decoded = StateStore.decode(json)
        assertEquals(1, decoded.profiles.size)
        val profile = decoded.profiles.single()
        assertEquals(Maalform.NYNORSK, profile.maalform)
        assertEquals(3, profile.grade)
        assertEquals(3, profile.stars("m_add5"))
        assertTrue(profile.raceBest.isEmpty())
    }

    @Test
    fun `save replaces the file and a broken file is kept aside`() {
        val directory = Files.createTempDirectory("komet").toFile()
        val file = directory.resolve("komet.json")
        val store = StateStore(file)
        store.save(state)
        store.save(state.copy(activeProfileId = "b"))
        assertEquals("b", store.load().activeProfileId)
        file.writeText("{ not json")
        assertEquals(AppState(), store.load())
        assertTrue(directory.listFiles()!!.any { it.name.startsWith("komet.json.broken-") })
        directory.deleteRecursively()
    }
}
