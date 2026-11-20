package app.komet.domain

import app.komet.domain.Drive.*

enum class DiscoveryWorkshop(val title: Txt, val subject: Subject, val planet: SolarDestination) {
    PIZZA(Txt("Pizzaverkstaden", "Pizzaverkstedet"), Subject.MATH, SolarDestination.VENUS),
    ROVER(Txt("Marsruta"), Subject.SPACE, SolarDestination.MARS),
    POST(Txt("Stjerneposten"), Subject.READING, SolarDestination.URANUS),
}

/** The number of slices on the plate and the denominator in the order can differ. */
data class PizzaOrder(val slices: Int, val numerator: Int, val denominator: Int, val portion: Txt) {
    fun isFilled(count: Int) = count in 0..slices && count * denominator == slices * numerator
    val requestedSlices get() = slices * numerator / denominator
}

val pizzaOrders = listOf(
    PizzaOrder(2, 1, 2, Txt("ein halv pizza", "en halv pizza")),
    PizzaOrder(4, 1, 4, Txt("ein firedel av pizzaen", "en firedel av pizzaen")),
    PizzaOrder(4, 1, 2, Txt("ein halv pizza", "en halv pizza")),
    PizzaOrder(4, 3, 4, Txt("tre firedelar av pizzaen", "tre firedeler av pizzaen")),
    PizzaOrder(6, 1, 3, Txt("ein tredel av pizzaen", "en tredel av pizzaen")),
    PizzaOrder(8, 3, 4, Txt("tre firedelar av pizzaen", "tre firedeler av pizzaen")),
)

enum class Drive(val arrow: String, val label: Txt, val dx: Int, val dy: Int) {
    LEFT("←", Txt("Venstre"), -1, 0), UP("↑", Txt("Opp"), 0, -1),
    DOWN("↓", Txt("Ned"), 0, 1), RIGHT("→", Txt("Høgre", "Høyre"), 1, 0),
}

/** S is the rover, F the flag and # a rock. Steps use screen directions. */
class MarsRoute(val rows: List<String>, val example: List<Drive>) {
    val width = rows.first().length
    val start = rows.joinToString("").indexOf('S')
    val flag = rows.joinToString("").indexOf('F')

    fun step(position: Int, move: Drive): Int? {
        val x = position % width + move.dx
        val y = position / width + move.dy
        if (y !in rows.indices || x !in 0 until width || rows[y][x] == '#') return null
        return y * width + x
    }
}

val marsRoutes = listOf(
    MarsRoute(listOf("...#", "S.F.", ".#.."), listOf(RIGHT, RIGHT)),
    MarsRoute(listOf("..F.", ".#..", "S..."), listOf(RIGHT, RIGHT, UP, UP)),
    MarsRoute(listOf("F..#", ".#..", "..S."), listOf(LEFT, LEFT, UP, UP)),
    MarsRoute(listOf("S.#.", "#.#F", "#..."), listOf(RIGHT, DOWN, DOWN, RIGHT, RIGHT, UP)),
    MarsRoute(listOf("...S", ".###", "...F"), listOf(LEFT, LEFT, LEFT, DOWN, DOWN, RIGHT, RIGHT, RIGHT)),
)
