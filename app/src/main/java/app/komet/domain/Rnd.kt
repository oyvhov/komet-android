package app.komet.domain

import kotlin.math.abs
import kotlin.random.Random

fun <T> Random.pick(items: List<T>): T = items[nextInt(items.size)]

fun <T> Random.pickDistinct(items: List<T>, count: Int): List<T> = items.shuffled(this).take(count)

fun Random.chance(probability: Double): Boolean = nextDouble() < probability

/** Inclusive range helper that reads like the curriculum: `rnd.between(1, 5)`. */
fun Random.between(from: Int, to: Int): Int = nextInt(from, to + 1)

/**
 * Builds [count] distinct number options around [correct]. Distractors prefer the mistakes children
 * actually make — off by one or two, and off by ten — before random values in range.
 */
fun numberChoices(
    correct: Int,
    random: Random,
    count: Int = 4,
    min: Int = 0,
    max: Int = correct + 10,
    likely: List<Int> = emptyList(),
): Pair<List<Int>, Int> {
    val pool = LinkedHashSet<Int>()
    val candidates = likely + listOf(correct + 1, correct - 1, correct + 2, correct - 2, correct + 10, correct - 10)
    for (candidate in candidates.shuffled(random)) {
        if (candidate != correct && candidate in min..max) pool += candidate
        if (pool.size >= count - 1) break
    }
    // A range narrower than the number of options is widened upwards rather than returning too few.
    val high = maxOf(max, min + count - 1)
    var guard = 0
    while (pool.size < count - 1 && guard < 400) {
        guard++
        val candidate = random.between(min, high)
        if (candidate != correct && (guard > 200 || abs(candidate - correct) <= maxOf(10, correct))) pool += candidate
    }
    val options = (pool.take(count - 1) + correct).shuffled(random)
    return options to options.indexOf(correct)
}

fun numberLabel(value: Int): Option = Option.Label(Txt(value.toString()), GlyphKind.NUMBER)

fun choiceOfNumbers(correct: Int, random: Random, count: Int = 4, min: Int = 0, max: Int = correct + 10, likely: List<Int> = emptyList()): Answer.Choice {
    val (values, index) = numberChoices(correct, random, count, min, max, likely)
    return Answer.Choice(values.map(::numberLabel), index)
}

/** Shuffles [correct] in among [others] and returns a choice. */
fun <T> choiceOf(correct: T, others: List<T>, random: Random, toOption: (T) -> Option): Answer.Choice {
    val all = (others + correct).shuffled(random)
    return Answer.Choice(all.map(toOption), all.indexOf(correct))
}
