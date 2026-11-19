package app.komet.domain

import kotlin.math.roundToInt
import kotlin.random.Random

private const val MINUTES_PER_DAY = 24 * 60
private const val MINUTES_PER_DIAL = 12 * 60
const val CLOCK_ROUND_LENGTH = 6

@JvmInline
value class ClockTime private constructor(val totalMinutes: Int) {
    val hour: Int get() = totalMinutes / 60
    val minute: Int get() = totalMinutes % 60
    val dialHour: Int get() = (hour + 11) % 12 + 1
    val hourAngle: Float get() = (totalMinutes % MINUTES_PER_DIAL) / 2f
    val minuteAngle: Float get() = minute * 6f
    val digital: String get() = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

    operator fun plus(minutes: Int): ClockTime = fromMinutes(totalMinutes + minutes)

    fun moveMinuteTo(angle: Float, step: Int): ClockTime {
        val target = (angle / (6 * step)).roundToInt() * step
        // A tap exactly half a turn away moves forward.
        val distance = Math.floorMod(target - minute + 29, 60) - 29
        return this + distance
    }

    fun dialTime(): ClockTime = at(dialHour, minute)

    fun spokenDigital(): Txt = when (minute) {
        0 -> Txt("Klokka $hour.")
        1 -> Txt("Klokka $hour og eitt minutt.", "Klokka $hour og ett minutt.")
        else -> Txt("Klokka $hour og $minute minutt.", "Klokka $hour og $minute minutter.")
    }

    fun dialClue(): Txt {
        val next = dialHour % 12 + 1
        val position = if (minute == 0) {
            Txt("Den korte visaren peikar på $dialHour.", "Den korte viseren peker på $dialHour.")
        } else {
            Txt("Den korte visaren er mellom $dialHour og $next.", "Den korte viseren er mellom $dialHour og $next.")
        }
        val tick = if (minute == 0) 12 else minute / 5
        val minutes = when {
            minute == 0 -> Txt("Den lange peikar på 12.", "Den lange peker på 12.")
            minute % 5 == 0 -> Txt("Den lange peikar på $tick.", "Den lange peker på $tick.")
            else -> {
                val from = if (tick == 0) 12 else tick
                Txt("Den lange er ${minute % 5} strekar etter $from.", "Den lange er ${minute % 5} streker etter $from.")
            }
        }
        return Txt("${position.nn} ${minutes.nn}", "${position.nb} ${minutes.nb}")
    }

    fun spokenDial(): Txt {
        val next = dialHour % 12 + 1
        return when (minute) {
            0 -> Txt("Klokka $dialHour")
            15 -> Txt("Kvart over $dialHour")
            30 -> Txt("Halv $next")
            45 -> Txt("Kvart på $next")
            1 -> Txt("Eitt minutt over $dialHour", "Ett minutt over $dialHour")
            59 -> Txt("Eitt minutt på $next", "Ett minutt på $next")
            in 31..58 -> Txt("${60 - minute} minutt på $next", "${60 - minute} minutter på $next")
            else -> Txt("$minute minutt over $dialHour", "$minute minutter over $dialHour")
        }
    }

    companion object {
        fun at(hour: Int, minute: Int) = fromMinutes(hour * 60 + minute)
        fun fromMinutes(minutes: Int) = ClockTime(Math.floorMod(minutes, MINUTES_PER_DAY))
    }
}

enum class ClockLevel(val label: Txt, val minuteStep: Int, private val minutes: List<Int>) {
    HOURS(Txt("Heile timar", "Hele timer"), 60, listOf(0)),
    HALVES(Txt("Halve timar", "Halve timer"), 30, listOf(30)),
    QUARTERS(Txt("Kvarter"), 15, listOf(15, 30, 45)),
    FIVE_MINUTES(Txt("Fem minutt", "Fem minutter"), 5, (0..55 step 5).toList());

    fun round(seed: Long): List<ClockChallenge> {
        val random = Random(seed)
        val targets = minutes.flatMap { minute -> (1..12).map { ClockTime.at(it, minute) } }
        return targets.shuffled(random).take(CLOCK_ROUND_LENGTH).mapIndexed { index, target ->
            val build = index % 2 == 1
            val otherHour = (target + if (random.nextBoolean()) 60 else -60).dialTime()
            val otherMinute = if (minuteStep == 60) (target + 180).dialTime() else {
                ClockTime.at(target.hour, (target.minute + minuteStep) % 60)
            }
            ClockChallenge(
                target = target,
                choices = if (build) emptyList() else listOf(target, otherHour, otherMinute).shuffled(random),
                start = otherHour.let { ClockTime.at(it.hour, 0) },
            )
        }
    }
}

data class ClockChallenge(val target: ClockTime, val choices: List<ClockTime>, val start: ClockTime) {
    val setHands: Boolean get() = choices.isEmpty()

    fun matches(time: ClockTime): Boolean = time.dialTime() == target
}

/** Signed motion that stays continuous when a finger crosses 12. */
fun clockSweep(previous: Float, current: Float): Float =
    ((current - previous + 540f) % 360f) - 180f
