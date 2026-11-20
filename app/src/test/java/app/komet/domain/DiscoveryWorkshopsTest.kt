package app.komet.domain

import org.junit.Assert.*
import org.junit.Test

class DiscoveryWorkshopsTest {
    @Test fun eachOrderAcceptsOnlyTheRequestedShareOfTheWholePizza() {
        val expected = listOf(1 to 2, 1 to 4, 2 to 4, 3 to 4, 2 to 6, 6 to 8)
        assertEquals(expected.size, pizzaOrders.size)
        pizzaOrders.zip(expected).forEach { (order, serving) ->
            val (pieces, total) = serving
            assertEquals(total, order.slices)
            assertEquals(pieces, order.requestedSlices)
            for (count in -1..total + 1) assertEquals(count == pieces, order.isFilled(count))
        }
    }

    @Test fun everyRoverHintReachesItsFlagAndEveryStepRespectsTheMap() {
        for (route in marsRoutes) {
            assertTrue(route.rows.all { it.length == route.width })
            val tiles = route.rows.joinToString("")
            assertEquals(1, tiles.count { it == 'S' })
            assertEquals(1, tiles.count { it == 'F' })
            for (at in tiles.indices.filter { tiles[it] != '#' }) {
                val x = at % route.width
                val y = at / route.width
                val neighbors = mapOf(Drive.LEFT to (x - 1 to y), Drive.RIGHT to (x + 1 to y),
                    Drive.UP to (x to y - 1), Drive.DOWN to (x to y + 1))
                for ((direction, dest) in neighbors) {
                    val (nx, ny) = dest
                    val tile = route.rows.getOrNull(ny)?.getOrNull(nx)
                    val expected = if (tile == null || tile == '#') null else ny * route.width + nx
                    assertEquals(expected, route.step(at, direction))
                }
            }
            val end = route.example.fold(route.start) { at, direction -> requireNotNull(route.step(at, direction)) }
            assertTrue(route.example.size in 1..16)
            assertEquals(tiles.indexOf('F'), end)
        }
    }
}
