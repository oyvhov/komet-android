package app.komet.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MontessoriMaterialsTest {
    @Test fun rodsRequireEveryLengthInIncreasingOrder() {
        assertTrue(MontessoriMaterials.rodsInOrder(listOf(1, 2, 3, 4, 5)))
        assertFalse(MontessoriMaterials.rodsInOrder(listOf(1, 2, 4, 3, 5)))
        assertFalse(MontessoriMaterials.rodsInOrder(listOf(1, 2, 3, 4)))
    }

    @Test fun allSpindlesMustMatchTheirBoxesIncludingZero() {
        val correct = listOf(0, 1, 2, 3, 4, 5)
        assertEquals(0, MontessoriMaterials.remainingSpindles(correct))
        assertTrue(MontessoriMaterials.spindlesMatch(correct))
        assertFalse(MontessoriMaterials.spindlesMatch(listOf(1, 0, 2, 3, 4, 5)))
        assertFalse(MontessoriMaterials.spindlesMatch(listOf(0, 1, 2, 3, 4, 4)))
        assertFalse(MontessoriMaterials.canAddSpindle(correct, 5))
        assertTrue(MontessoriMaterials.canAddSpindle(listOf(0, 0, 0, 0, 0, 0), 5))
    }

    @Test fun rodCheckUsesTheLengthsInListsWithIdentityEquality() {
        // Compose's mutable state lists need element checks, not the list object's equality.
        val correct = object : List<Int> by listOf(1, 2, 3, 4, 5) {}
        val wrong = object : List<Int> by listOf(1, 2, 4, 3, 5) {}
        assertTrue(MontessoriMaterials.rodsInOrder(correct))
        assertFalse(MontessoriMaterials.rodsInOrder(wrong))
    }
}
