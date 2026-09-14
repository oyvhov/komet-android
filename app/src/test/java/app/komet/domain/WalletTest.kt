package app.komet.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WalletTest {

    private fun profile(nuggets: Int = 0) = Profile(id = "p", name = "Test", avatar = 0, grade = 1, nuggets = nuggets)

    @Test
    fun `a round pays one nugget per star, and the daily mission pays extra`() {
        val p = profile()
        val round = Progression.applyRound(p, Curriculum.skill("m_add10")!!, firstTry = 8, total = 8, seconds = 60, today = 10, dailyGoal = 3, now = 1L)
        assertEquals(3, round.nuggets)
        assertEquals(3, round.profile.nuggets)

        // The third round of the day reaches the mission and brings the bonus.
        var q = p
        repeat(2) { q = Progression.applyRound(q, Curriculum.skill("m_add10")!!, firstTry = 1, total = 8, seconds = 60, today = 10, dailyGoal = 3, now = 1L).profile }
        val third = Progression.applyRound(q, Curriculum.skill("m_add10")!!, firstTry = 4, total = 8, seconds = 60, today = 10, dailyGoal = 3, now = 1L)
        assertTrue(third.goalReachedNow)
        assertEquals(2 + Wallet.DAILY_BONUS, third.nuggets)
        assertEquals(1 + 1 + 2 + Wallet.DAILY_BONUS, third.profile.nuggets)
    }

    @Test
    fun `a new race record pays a bonus, a review round pays its stars`() {
        val race = Progression.applyRace(profile(), RaceMode.ADD10, score = 14, answered = 16, seconds = 60, today = 5, dailyGoal = 3)
        assertEquals(2 + Wallet.RECORD_BONUS, race.nuggets)
        assertEquals(race.nuggets, race.profile.nuggets)
        val again = Progression.applyRace(race.profile, RaceMode.ADD10, score = 9, answered = 12, seconds = 60, today = 5, dailyGoal = 3)
        assertEquals(1, again.nuggets)

        val review = Progression.applyReview(profile(), firstTry = 5, total = 8, today = 5, dailyGoal = 3)
        assertEquals(2, review.nuggets)
    }

    @Test
    fun `buying needs enough nuggets, takes the price once and puts the thing on`() {
        val item = Shop.item("helmet_gold")!!
        val poor = Wallet.buy(profile(nuggets = item.price - 7), item.id)
        assertEquals(Purchase.NotEnough(7), poor)

        val bought = Wallet.buy(profile(nuggets = item.price + 4), item.id) as Purchase.Bought
        assertEquals(4, bought.profile.nuggets)
        assertTrue(item.id in bought.profile.owned)
        assertEquals(item.id, bought.profile.equipped[ShopSlot.HELMET])

        assertEquals(Purchase.AlreadyOwned, Wallet.buy(bought.profile.copy(nuggets = 999), item.id))
        assertEquals(Purchase.Unknown, Wallet.buy(profile(nuggets = 999), "no_such_thing"))
    }

    @Test
    fun `the balance never goes below zero`() {
        var p = profile(nuggets = 30)
        for (item in Shop.items.sortedBy { it.price }) {
            val result = Wallet.buy(p, item.id)
            if (result is Purchase.Bought) p = result.profile
            assertTrue("balance ${p.nuggets} after ${item.id}", p.nuggets >= 0)
        }
        assertTrue(p.owned.isNotEmpty())
    }

    @Test
    fun `only owned things can be put on, and taking off empties the slot`() {
        val p = profile(nuggets = 100)
        assertEquals(p, Wallet.equip(p, "visor_gold"))
        val bought = (Wallet.buy(p, "visor_gold") as Purchase.Bought).profile
        val off = Wallet.unequip(bought, ShopSlot.VISOR)
        assertNull(off.equipped[ShopSlot.VISOR])
        assertEquals("visor_gold", Wallet.equip(off, "visor_gold").equipped[ShopSlot.VISOR])
    }

    @Test
    fun `a nugget waits by every finished level and can be picked up once`() {
        val p = profile().copy(skills = mapOf("m_add10" to SkillStats(bestStars = 2), "m_sub10" to SkillStats(bestStars = 0)))
        assertEquals(listOf("m_add10"), Wallet.pickups(p, Subject.MATH).map { it.id })
        val picked = Wallet.collect(p, "m_add10")!!
        assertEquals(Wallet.PICKUP, picked.nuggets)
        assertTrue(Wallet.pickups(picked, Subject.MATH).isEmpty())
        assertNull(Wallet.collect(picked, "m_add10"))
        assertNull(Wallet.collect(p, "m_sub10"))
    }

    @Test
    fun `older profiles start with a gift that grows with their stars, up to a limit`() {
        assertEquals(Wallet.WELCOME, Wallet.startingSum(0))
        assertEquals(Wallet.WELCOME + 36, Wallet.startingSum(72))
        assertEquals(Wallet.WELCOME + 45, Wallet.startingSum(10_000))
    }

    @Test
    fun `every shop item has a unique id, a price and a name in both written standards`() {
        assertEquals(Shop.items.size, Shop.items.map { it.id }.toSet().size)
        Shop.items.forEach { item ->
            assertTrue(item.id, item.price > 0)
            assertTrue(item.id, item.name.nn.isNotBlank() && item.name.nb.isNotBlank())
            assertTrue(item.id, item.id.startsWith(item.slot.name.lowercase()))
        }
        ShopSlot.entries.forEach { slot -> assertTrue(slot.name, Shop.items(slot).isNotEmpty()) }
    }
}
