package app.komet.domain

import org.junit.Assert.*
import org.junit.Test

class StarPostTest {
    @Test fun everyOrderCanBeDecodedFromTheOldLabelsAndHasOneSolution() {
        val numbers = mapOf("ka" to 1, "tiri" to 2, "moro" to 3)
        val words = mapOf("pip" to PostCargo.SEED, "vek" to PostCargo.CRYSTAL, "lum" to PostCargo.STAR)
        assertEquals(5, postOrders.count { it.job == PostJob.WRITE })
        assertEquals(5, postOrders.count { it.job == PostJob.PACK })
        postOrders.forEachIndexed { index, order ->
            val (quantity, noun) = order.target.code.split(" ")
            val expected = PostParcel(numbers.getValue(quantity), words.getValue(noun))
            assertTrue(order.examples.any { it.cargo == expected.cargo })
            assertTrue(order.examples.any { it.count == expected.count })
            if (index >= 2) assertFalse(expected in order.examples)
            for (n in 1..3) for (cargo in PostCargo.entries)
                assertEquals(PostParcel(n, cargo) == expected, order.matches(n, cargo))
            assertFalse(order.matches(null, expected.cargo))
        }
        assertEquals(Txt("3 krystallar", "3 krystaller"), PostParcel(3, PostCargo.CRYSTAL).label)
    }
}
