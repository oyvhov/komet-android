package app.komet.data

import app.komet.domain.ActiveRound
import app.komet.domain.AppState
import app.komet.domain.DayStats
import app.komet.domain.HeroLook
import app.komet.domain.HeroPalette
import app.komet.domain.LetterCase
import app.komet.domain.Maalform
import app.komet.domain.Profile
import app.komet.domain.RaceMode
import app.komet.domain.Settings
import app.komet.domain.ShopSlot
import app.komet.domain.SkillStats
import app.komet.domain.Subject
import app.komet.domain.Wallet
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
                activeRound = ActiveRound("m_add10", done = 3, firstTry = 2, total = 8, updatedAt = 77L),
                favorites = listOf("r_rhyme", "m_add10"),
                hero = HeroLook(suit = 4, skin = 5, hair = 3, hairStyle = 2),
                visitedPlanets = setOf(Subject.MATH, Subject.SPACE),
                nuggets = 23,
                owned = setOf("helmet_gold", "bolt_pink"),
                equipped = mapOf(ShopSlot.HELMET to "helmet_gold"),
                collectedNuggets = setOf("m_add5"),
            ),
            Profile(id = "b", name = "Bror", avatar = 0, grade = 0),
        ),
        activeProfileId = "a",
        settings = Settings(sound = false, music = false, speech = true, slowSpeech = false, haptics = false, autoRead = false, dailyGoal = 5),
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
    fun `a profile from before astronauts gets a look from its figure, and odd looks are kept in range`() {
        val old = StateStore.decode(JSONObject("""{"profiles":[{"id":"x","avatar":9}]}""")).profiles.single()
        assertEquals(HeroLook.fromAvatar(9), old.hero)
        val odd = StateStore.decode(JSONObject("""{"profiles":[{"id":"y","hero":{"suit":99,"skin":-1,"hair":6,"hairStyle":4}}]}""")).profiles.single()
        assertTrue(odd.hero.suit in HeroPalette.suits.indices)
        assertTrue(odd.hero.skin in HeroPalette.skins.indices)
        assertTrue(odd.hero.hair in HeroPalette.hairs.indices)
        assertTrue(odd.hero.hairStyle in 0 until HeroPalette.HAIR_STYLES)
    }

    @Test
    fun `nuggets and the wardrobe are checked when read`() {
        val gift = StateStore.decode(JSONObject("""{"profiles":[{"id":"x","totalStars":72}]}""")).profiles.single()
        assertEquals(Wallet.startingSum(72), gift.nuggets)

        val odd = StateStore.decode(
            JSONObject(
                """{"profiles":[{"id":"y","nuggets":-4,"owned":["helmet_gold","gone_item"],
                "equipped":{"HELMET":"helmet_gold","VISOR":"visor_gold","BOLT":"helmet_gold","NOPE":"x"},
                "collectedNuggets":["m_add5","no_level"]}]}""",
            ),
        ).profiles.single()
        assertEquals(0, odd.nuggets)
        assertEquals(setOf("helmet_gold"), odd.owned)
        // Not owned, or in the wrong slot: taken off.
        assertEquals(mapOf(ShopSlot.HELMET to "helmet_gold"), odd.equipped)
        assertEquals(setOf("m_add5"), odd.collectedNuggets)
    }

    @Test
    fun `a damaged saved round is dropped instead of breaking the profile`() {
        val json = JSONObject("""{"profiles":[{"id":"x","activeRound":{"skillId":"m_add10","done":9,"total":8},"favorites":["m_add10","","m_add10"]}]}""")
        val profile = StateStore.decode(json).profiles.single()
        assertEquals(null, profile.activeRound)
        assertEquals(listOf("m_add10"), profile.favorites)
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
