package app.komet.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.komet.domain.Question
import app.komet.domain.RaceMode
import app.komet.domain.RoundOutcome
import app.komet.domain.Skill
import app.komet.domain.Subject
import kotlin.random.Random

sealed interface Screen {
    data object Onboarding : Screen
    data object AddProfile : Screen
    data object Home : Screen
    data class World(val subject: Subject) : Screen
    data object Play : Screen
    data object Result : Screen
    data object Collection : Screen
    data object RaceMenu : Screen
    data object Race : Screen
    data object ParentGate : Screen
    data object Parent : Screen
}

enum class Phase { ANSWERING, CORRECT, REVEALED }

/**
 * Everything the task screen needs about the round in progress. Lives in the ViewModel. A round picked
 * up again starts at [startIndex] with the first-try count it had when it was left.
 */
class RoundState(val skill: Skill, val questions: List<Question>, startIndex: Int = 0, startFirstTry: Int = 0) {
    var index by mutableIntStateOf(startIndex)
    var phase by mutableStateOf(Phase.ANSWERING)
    /** Misses on the current task. */
    var misses by mutableIntStateOf(0)
    val disabled = mutableStateListOf<Int>()
    var chosen by mutableStateOf<Int?>(null)
    var input by mutableStateOf("")
    val placed = mutableStateListOf<Int>()
    var hintShown by mutableStateOf(false)
    /** Bumped on every miss so the screen can shake the element at [shakeTarget]. */
    var shakeCount by mutableIntStateOf(0)
    var shakeTarget by mutableIntStateOf(-1)
    var firstTry by mutableIntStateOf(startFirstTry)
    var praise by mutableIntStateOf(0)
    val startedAt: Long = System.currentTimeMillis()
    /** When the last task was answered, so practice time is counted task by task. */
    var lastAnswerAt: Long = startedAt

    val question: Question get() = questions[index]
    val isLast: Boolean get() = index == questions.lastIndex

    fun resetForNext() {
        phase = Phase.ANSWERING
        misses = 0
        disabled.clear()
        chosen = null
        input = ""
        placed.clear()
        hintShown = false
        shakeTarget = -1
    }
}

enum class RacePhase { COUNTDOWN, RUNNING, DONE }

class RaceState(val mode: RaceMode, private val random: Random = Random(System.nanoTime())) {
    var phase by mutableStateOf(RacePhase.COUNTDOWN)
    var countdown by mutableIntStateOf(3)
    var remainingMs by mutableLongStateOf(DURATION_MS)
    var score by mutableIntStateOf(0)
    var answered by mutableIntStateOf(0)
    var question by mutableStateOf(mode.question(random))
    val disabled = mutableStateListOf<Int>()
    var shakeCount by mutableIntStateOf(0)
    var shakeTarget by mutableIntStateOf(-1)
    var correctCount by mutableIntStateOf(0)
    val startedAt: Long = System.currentTimeMillis()

    fun nextQuestion() {
        var candidate = mode.question(random)
        var guard = 0
        while (candidate.key == question.key && guard < 10) {
            candidate = mode.question(random)
            guard++
        }
        question = candidate
        disabled.clear()
        shakeTarget = -1
    }

    companion object {
        const val DURATION_MS = 60_000L
    }
}

data class ResultInfo(
    val outcome: RoundOutcome,
    val skill: Skill?,
    val raceMode: RaceMode?,
    val next: Skill?,
)
