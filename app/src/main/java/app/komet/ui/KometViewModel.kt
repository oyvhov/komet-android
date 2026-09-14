package app.komet.ui

import android.app.Application
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.komet.audio.MusicPlayer
import app.komet.audio.MusicTheme
import app.komet.audio.Sfx
import app.komet.audio.SoundFx
import app.komet.audio.Speaker
import app.komet.data.StateStore
import app.komet.domain.Answer
import app.komet.domain.AppState
import app.komet.domain.Curriculum
import app.komet.domain.HeroLook
import app.komet.domain.Purchase
import app.komet.domain.ShopSlot
import app.komet.domain.Wallet
import app.komet.domain.LetterCase
import app.komet.domain.Maalform
import app.komet.domain.Profile
import app.komet.domain.Progression
import app.komet.domain.QuestionContext
import app.komet.domain.RaceMode
import app.komet.domain.Settings
import app.komet.domain.Skill
import app.komet.domain.SpaceCards
import app.komet.domain.Subject
import app.komet.domain.Txt
import app.komet.ui.components.Feedback
import app.komet.ui.scene.MapPlace
import app.komet.ui.scene.mapPlaceOf
import app.komet.update.AppUpdater
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.Executors
import kotlin.random.Random

class KometViewModel(application: Application) : AndroidViewModel(application) {

    private val store = StateStore(File(application.filesDir, "komet.json"))
    private val saver = Executors.newSingleThreadExecutor()

    val speaker = Speaker(application)
    private val sounds = SoundFx(application)
    private val music = MusicPlayer(application.cacheDir)
    val updater = AppUpdater(application, viewModelScope)

    var state by mutableStateOf(store.load())
        private set

    val stack = mutableStateListOf<Screen>()

    var round by mutableStateOf<RoundState?>(null)
        private set

    var race by mutableStateOf<RaceState?>(null)
        private set

    var result by mutableStateOf<ResultInfo?>(null)
        private set

    /** Where the rocket is parked on the star map. Starts at the planet played last. */
    var mapPlace by mutableStateOf(state.activeProfile?.lastSubject?.let(::mapPlaceOf) ?: MapPlace.MATH)

    private var raceJob: Job? = null

    val feedback = object : Feedback {
        override fun sfx(effect: Sfx, volume: Float) = sounds.play(effect, volume)
    }

    init {
        stack += if (state.profiles.isEmpty()) Screen.Onboarding else Screen.Home
        speaker.onSpeaking = { speaking -> music.duck(speaking) }
        applySettings()
    }

    val profile: Profile? get() = state.activeProfile
    val settings: Settings get() = state.settings
    val screen: Screen get() = stack.last()

    val today: Long get() = LocalDate.now().toEpochDay()

    // ── State ──────────────────────────────────────────────────────────────────────────────────

    private fun commit(next: AppState) {
        state = next
        saver.execute { runCatching { store.save(next) } }
    }

    fun updateSettings(transform: (Settings) -> Settings) {
        commit(state.copy(settings = transform(state.settings)))
        applySettings()
    }

    fun updateProfile(id: String, transform: (Profile) -> Profile) {
        val current = state.profiles.firstOrNull { it.id == id } ?: return
        commit(state.withProfile(transform(current)))
    }

    private fun applySettings() {
        sounds.enabled = state.settings.sound
        music.enabled = state.settings.music
        speaker.rate = if (state.settings.slowSpeech) 0.82f else 1.0f
        if (!state.settings.speech) speaker.stop()
    }

    // ── Music ──────────────────────────────────────────────────────────────────────────────────

    /** Every place has its own music; a result or the parents' pages keep what was playing. */
    fun onScreenShown(target: Screen) {
        music.setQuiet(target == Screen.Play)
        val theme = when (target) {
            Screen.Onboarding, Screen.AddProfile, Screen.Home, Screen.Explore, is Screen.Topic -> MusicTheme.MAP
            is Screen.World -> musicFor(target.subject)
            Screen.Play -> round?.let { musicFor(it.skill.subject) }
            Screen.Collection -> MusicTheme.SPACE
            Screen.RaceMenu, Screen.Race -> MusicTheme.RACE
            Screen.Result, Screen.ParentGate, Screen.Parent, Screen.HeroEditor, Screen.Shop -> null
        }
        theme?.let(music::play)
    }

    private fun musicFor(subject: Subject): MusicTheme = when (subject) {
        Subject.MATH -> MusicTheme.MATH
        Subject.READING -> MusicTheme.READING
        Subject.ENGLISH -> MusicTheme.ENGLISH
        Subject.SPACE -> MusicTheme.SPACE
    }

    fun onForeground() = music.resume()

    /** Nothing should keep playing or talking from a pocket or a closed tablet cover. */
    fun onBackground() {
        speaker.stop()
        music.pause()
    }

    // ── Navigation ─────────────────────────────────────────────────────────────────────────────

    fun open(target: Screen) {
        speaker.stop()
        stack += target
    }

    fun back(): Boolean {
        if (stack.size <= 1) return false
        speaker.stop()
        val leaving = stack.removeAt(stack.lastIndex)
        if (leaving == Screen.Race) stopRace()
        return true
    }

    fun goHome() {
        speaker.stop()
        stopRace()
        round = null
        stack.clear()
        stack += if (state.profiles.isEmpty()) Screen.Onboarding else Screen.Home
    }

    /** A result goes back to the planet its level came from; anything else goes home. */
    val resultReturnsToPlanet: Boolean
        get() = stack.size >= 2 && stack[stack.lastIndex - 1] is Screen.World

    fun leaveResult() {
        if (resultReturnsToPlanet) back() else goHome()
    }

    private fun replaceTop(target: Screen) {
        speaker.stop()
        if (stack.isEmpty()) stack += target else stack[stack.lastIndex] = target
    }

    // ── Profiles ───────────────────────────────────────────────────────────────────────────────

    fun createProfile(name: String, hero: HeroLook, grade: Int, maalform: Maalform, letterCase: LetterCase) {
        val profile = Profile(
            id = UUID.randomUUID().toString(),
            name = name.trim().ifBlank { "Romfarar" }.take(20),
            avatar = hero.suit,
            hero = hero.safe(),
            nuggets = Wallet.WELCOME,
            grade = grade,
            maalform = maalform,
            letterCase = letterCase,
            createdAt = System.currentTimeMillis(),
        )
        commit(state.copy(profiles = state.profiles + profile, activeProfileId = profile.id))
        goHome()
    }

    // ── Gold nuggets and the shop ──────────────────────────────────────────────────────────────

    fun buy(itemId: String): Purchase {
        val current = profile ?: return Purchase.Unknown
        val result = Wallet.buy(current, itemId)
        if (result is Purchase.Bought) {
            commit(state.withProfile(result.profile))
            sounds.play(Sfx.UNLOCK)
        }
        return result
    }

    fun equip(itemId: String) {
        val current = profile ?: return
        commit(state.withProfile(Wallet.equip(current, itemId)))
    }

    fun unequip(slot: ShopSlot) {
        val current = profile ?: return
        commit(state.withProfile(Wallet.unequip(current, slot)))
    }

    /** Picks up the nugget by a finished level on a planet. */
    fun collectNugget(skillId: String): Boolean {
        val current = profile ?: return false
        val updated = Wallet.collect(current, skillId) ?: return false
        commit(state.withProfile(updated))
        sounds.play(Sfx.COIN)
        return true
    }

    /** Remembers that Bolt has welcomed the child to [subject]'s planet. */
    fun markPlanetVisited(subject: Subject) {
        val current = profile ?: return
        if (subject !in current.visitedPlanets) updateProfile(current.id) { it.copy(visitedPlanets = it.visitedPlanets + subject) }
    }

    fun updateHero(look: HeroLook) {
        val current = profile ?: return
        updateProfile(current.id) { it.copy(hero = look.safe(), avatar = look.suit) }
    }

    fun switchProfile(id: String) {
        if (state.profiles.none { it.id == id }) return
        commit(state.copy(activeProfileId = id))
        mapPlace = profile?.lastSubject?.let(::mapPlaceOf) ?: MapPlace.MATH
    }

    fun deleteProfile(id: String) {
        val remaining = state.profiles.filterNot { it.id == id }
        val active = if (state.activeProfileId == id) remaining.firstOrNull()?.id else state.activeProfileId
        commit(state.copy(profiles = remaining, activeProfileId = active))
        if (remaining.isEmpty()) goHome()
    }

    fun resetProgress(id: String) = updateProfile(id) {
        // The wardrobe stays; the nuggets and the planets' pickups start again with the stars.
        it.copy(
            skills = emptyMap(), totalStars = 0, streak = 0, lastGoalDay = -1L, days = emptyMap(),
            raceBest = emptyMap(), lastSubject = null, seenCards = 0,
            nuggets = Wallet.WELCOME, collectedNuggets = emptySet(),
        )
    }

    fun markCardsSeen() {
        val current = profile ?: return
        val unlocked = SpaceCards.unlockedCount(current.totalStars)
        if (current.seenCards != unlocked) updateProfile(current.id) { it.copy(seenCards = unlocked) }
    }

    // ── Speech ─────────────────────────────────────────────────────────────────────────────────

    fun say(text: Txt) {
        if (!state.settings.speech) return
        speaker.speak(text.get(profile?.maalform ?: Maalform.NYNORSK))
    }

    fun sayPlain(text: String) {
        if (state.settings.speech) speaker.speak(text)
    }

    val speechAvailable: Boolean
        get() = state.settings.speech && speaker.status != Speaker.Status.UNAVAILABLE && speaker.status != Speaker.Status.MISSING_NORWEGIAN

    // ── Rounds ─────────────────────────────────────────────────────────────────────────────────

    fun startSkill(skill: Skill) {
        if (Curriculum.isReview(skill)) {
            startReview(skill.subject)
            return
        }
        val current = profile ?: return
        val context = QuestionContext(Random(System.nanoTime()), current.maalform, current.letterCase)
        // A new round replaces one that was left earlier; its answers are already counted.
        if (current.activeRound != null) updateProfile(current.id) { it.copy(activeRound = null) }
        round = RoundState(skill, Curriculum.round(skill, context))
        if (screen == Screen.Result || screen == Screen.Play) replaceTop(Screen.Play) else open(Screen.Play)
    }

    /** A round of tasks from the levels in [subject] that the child finds hardest right now. */
    fun startReview(subject: Subject) {
        val current = profile ?: return
        val weak = Progression.weakSkills(current, subject)
        if (weak.isEmpty()) return
        val context = QuestionContext(Random(System.nanoTime()), current.maalform, current.letterCase)
        val tasks = Curriculum.reviewRound(weak, context)
        round = RoundState(Curriculum.reviewSkill(subject), tasks.map { it.second }, sources = tasks.map { it.first })
        if (screen == Screen.Result || screen == Screen.Play) replaceTop(Screen.Play) else open(Screen.Play)
    }

    /** Picks up the round that was left, at the task after the last one answered. */
    fun resumeRound() {
        val current = profile ?: return
        val (skill, active) = Progression.resumable(current) ?: return
        val context = QuestionContext(Random(System.nanoTime()), current.maalform, current.letterCase)
        round = RoundState(skill, Curriculum.round(skill, context, count = active.total), startIndex = active.done, startFirstTry = active.firstTry)
        if (screen == Screen.Result || screen == Screen.Play) replaceTop(Screen.Play) else open(Screen.Play)
    }

    fun toggleFavorite(skillId: String) {
        val current = profile ?: return
        commit(state.withProfile(Progression.toggleFavorite(current, skillId)))
    }

    fun readQuestion() {
        val current = round ?: return
        say(current.question.speech)
    }

    fun choose(index: Int) {
        val current = round ?: return
        val answer = current.question.answer as? Answer.Choice ?: return
        if (current.phase != Phase.ANSWERING || index in current.disabled) return
        if (index == answer.correct) {
            current.chosen = index
            markCorrect(current)
        } else {
            current.disabled += index
            markMiss(current, index)
            val allowed = if (answer.options.size <= 2) 1 else 2
            if (current.misses >= allowed) reveal(current)
        }
    }

    fun typeDigit(digit: Int) {
        val current = round ?: return
        if (current.phase != Phase.ANSWERING || current.input.length >= 3) return
        current.input = if (current.input == "0") digit.toString() else current.input + digit
    }

    fun eraseDigit() {
        val current = round ?: return
        if (current.phase == Phase.ANSWERING) current.input = current.input.dropLast(1)
    }

    fun submitNumber() {
        val current = round ?: return
        val answer = current.question.answer as? Answer.NumberInput ?: return
        if (current.phase != Phase.ANSWERING) return
        val value = current.input.toIntOrNull() ?: return
        if (value == answer.correct) {
            markCorrect(current)
        } else {
            markMiss(current, -1)
            current.input = ""
            if (current.misses >= 2) reveal(current)
        }
    }

    fun placeTile(tile: Int) {
        val current = round ?: return
        val answer = current.question.answer as? Answer.Build ?: return
        if (current.phase != Phase.ANSWERING || tile in current.placed) return
        val expected = answer.target[current.placed.size]
        if (answer.tiles[tile] == expected) {
            current.placed += tile
            if (current.placed.size == answer.target.size) markCorrect(current) else sounds.play(Sfx.TAP)
        } else {
            markMiss(current, tile)
        }
    }

    /** One stroke of a letter or digit is written. */
    fun traceStroke() {
        sounds.play(Sfx.TAP)
    }

    /** The whole letter or digit is written; writing has no wrong answers, only practice. */
    fun traceFinished() {
        val current = round ?: return
        if (current.question.answer !is Answer.Trace || current.phase != Phase.ANSWERING) return
        markCorrect(current)
    }

    fun showHint() {
        round?.let { it.hintShown = true }
    }

    private fun markCorrect(current: RoundState) {
        current.phase = Phase.CORRECT
        val firstTime = current.misses == 0
        if (firstTime) current.firstTry++
        current.praise = Random.nextInt(S.praise.size)
        sounds.play(Sfx.CORRECT)
        current.question.reward?.let(::say)
        recordAnswer(current, firstTime)
    }

    /** Saves the answer straight away, so a round that is left early still counts and can be picked up. */
    private fun recordAnswer(current: RoundState, rightFirstTime: Boolean) {
        val player = profile ?: return
        val now = System.currentTimeMillis()
        val seconds = ((now - current.lastAnswerAt) / 1000).toInt()
        current.lastAnswerAt = now
        val updated = Progression.applyAnswer(
            profile = player,
            skill = current.source,
            rightFirstTime = rightFirstTime,
            done = current.index + 1,
            roundFirstTry = current.firstTry,
            total = current.questions.size,
            seconds = seconds,
            today = today,
            now = now,
            trackRound = !current.isReview,
        )
        commit(state.withProfile(updated))
    }

    private fun markMiss(current: RoundState, target: Int) {
        current.misses++
        current.shakeTarget = target
        current.shakeCount++
        sounds.play(Sfx.WRONG)
    }

    private fun reveal(current: RoundState) {
        current.phase = Phase.REVEALED
        val answer = current.question.answer
        if (answer is Answer.Build) current.placed.clear()
        current.question.reward?.let(::say)
        recordAnswer(current, rightFirstTime = false)
    }

    fun next() {
        val current = round ?: return
        if (current.phase == Phase.ANSWERING) return
        if (current.isLast) {
            finishRound(current)
        } else {
            current.index++
            current.resetForNext()
        }
    }

    fun quitRound() {
        round = null
        back()
    }

    private fun finishRound(current: RoundState) {
        val player = profile ?: return
        if (current.isReview) {
            val outcome = Progression.applyReview(player, current.firstTry, current.questions.size, today, state.settings.dailyGoal)
            commit(state.withProfile(outcome.profile))
            result = ResultInfo(outcome = outcome, skill = current.skill, raceMode = null, next = null)
            round = null
            replaceTop(Screen.Result)
            sounds.play(Sfx.COMPLETE)
            return
        }
        val seconds = ((System.currentTimeMillis() - current.startedAt) / 1000).toInt()
        val outcome = Progression.applyRound(
            profile = player,
            skill = current.skill,
            firstTry = current.firstTry,
            total = current.questions.size,
            seconds = seconds,
            today = today,
            dailyGoal = state.settings.dailyGoal,
            now = System.currentTimeMillis(),
            answersCounted = true,
        )
        commit(state.withProfile(outcome.profile))
        result = ResultInfo(
            outcome = outcome,
            skill = current.skill,
            raceMode = null,
            next = Progression.nextInChapter(outcome.profile, current.skill),
        )
        round = null
        replaceTop(Screen.Result)
        sounds.play(Sfx.COMPLETE)
    }

    // ── Rakettløp ──────────────────────────────────────────────────────────────────────────────

    fun startRace(mode: RaceMode) {
        stopRace()
        val current = RaceState(mode)
        race = current
        if (screen == Screen.Result) replaceTop(Screen.Race) else open(Screen.Race)
        raceJob = viewModelScope.launch {
            for (count in 3 downTo 1) {
                current.countdown = count
                sounds.play(Sfx.TICK)
                delay(800)
            }
            current.countdown = 0
            current.phase = RacePhase.RUNNING
            sounds.play(Sfx.GO)
            say(current.question.speech)
            val end = SystemClock.elapsedRealtime() + RaceState.DURATION_MS
            while (true) {
                val remaining = end - SystemClock.elapsedRealtime()
                current.remainingMs = remaining.coerceAtLeast(0)
                if (remaining <= 0) break
                delay(50)
            }
            finishRace(current)
        }
    }

    fun raceAnswer(index: Int) {
        val current = race ?: return
        if (current.phase != RacePhase.RUNNING || index in current.disabled) return
        val answer = current.question.answer as? Answer.Choice ?: return
        if (index == answer.correct) {
            if (current.disabled.isEmpty()) current.score++
            current.answered++
            current.correctCount++
            sounds.play(Sfx.STAR, 0.6f)
            current.nextQuestion()
            if (state.settings.autoRead) say(current.question.speech)
        } else {
            current.disabled += index
            current.shakeTarget = index
            current.shakeCount++
            sounds.play(Sfx.WRONG, 0.7f)
        }
    }

    private fun stopRace() {
        raceJob?.cancel()
        raceJob = null
        race = null
    }

    private fun finishRace(current: RaceState) {
        current.phase = RacePhase.DONE
        val player = profile ?: return
        val outcome = Progression.applyRace(
            profile = player,
            mode = current.mode,
            score = current.score,
            answered = current.answered,
            seconds = ((System.currentTimeMillis() - current.startedAt) / 1000).toInt(),
            today = today,
            dailyGoal = state.settings.dailyGoal,
        )
        commit(state.withProfile(outcome.profile))
        result = ResultInfo(outcome = outcome, skill = null, raceMode = current.mode, next = null)
        raceJob = null
        race = null
        replaceTop(Screen.Result)
        sounds.play(Sfx.COMPLETE)
    }

    // ── Debug ──────────────────────────────────────────────────────────────────────────────────

    /** Only reachable from debug builds (see MainActivity). Never used by the child-facing UI. */
    fun debug(
        skill: String?,
        screen: String?,
        stars: Int,
        unlockAll: Boolean,
        solve: Int,
        miss: Int,
        speech: String?,
        maalform: String?,
        letterCase: String?,
        backgroundMusic: String? = null,
    ) {
        when (backgroundMusic) {
            "off" -> updateSettings { it.copy(music = false) }
            "on" -> updateSettings { it.copy(music = true) }
        }
        val current = profile ?: return
        if (stars >= 0 || unlockAll) {
            updateProfile(current.id) { it.copy(totalStars = if (stars >= 0) stars else it.totalStars, unlockAll = it.unlockAll || unlockAll) }
        }
        when (maalform) {
            "nn" -> updateProfile(current.id) { it.copy(maalform = Maalform.NYNORSK) }
            "nb" -> updateProfile(current.id) { it.copy(maalform = Maalform.BOKMAAL) }
        }
        when (letterCase) {
            "upper" -> updateProfile(current.id) { it.copy(letterCase = LetterCase.UPPER) }
            "lower" -> updateProfile(current.id) { it.copy(letterCase = LetterCase.LOWER) }
        }
        when (speech) {
            "off" -> updateSettings { it.copy(speech = false) }
            "on" -> updateSettings { it.copy(speech = true) }
        }
        when (screen) {
            "home" -> goHome()
            "math" -> { goHome(); open(Screen.World(Subject.MATH)) }
            "reading" -> { goHome(); open(Screen.World(Subject.READING)) }
            "cards" -> { goHome(); open(Screen.Collection) }
            "race" -> { goHome(); open(Screen.RaceMenu) }
            "parent" -> { goHome(); open(Screen.Parent) }
            "explore" -> { goHome(); open(Screen.Explore) }
            "english" -> { goHome(); open(Screen.World(Subject.ENGLISH)) }
            "space" -> { goHome(); open(Screen.World(Subject.SPACE)) }
            "addprofile" -> { goHome(); open(Screen.AddProfile) }
            "hero" -> { goHome(); open(Screen.HeroEditor) }
            "shop" -> { goHome(); open(Screen.Shop) }
        }
        skill?.let(Curriculum::skill)?.let {
            goHome()
            startSkill(it)
        }
        repeat(miss) { debugMiss() }
        repeat(solve) { debugSolve() }
    }

    private fun debugMiss() {
        val current = round ?: return
        if (current.phase != Phase.ANSWERING) return
        when (val answer = current.question.answer) {
            is Answer.Choice -> answer.options.indices.firstOrNull { it != answer.correct && it !in current.disabled }?.let(::choose)
            is Answer.NumberInput -> {
                current.input = (answer.correct + 1).toString()
                submitNumber()
            }
            is Answer.Build -> answer.tiles.indices.firstOrNull { answer.tiles[it] != answer.target[current.placed.size] }?.let(::placeTile)
            is Answer.Trace -> Unit
        }
    }

    private fun debugSolve() {
        val current = round ?: return
        if (current.phase == Phase.ANSWERING) {
            when (val answer = current.question.answer) {
                is Answer.Choice -> choose(answer.correct)
                is Answer.NumberInput -> {
                    current.input = answer.correct.toString()
                    submitNumber()
                }
                is Answer.Build -> answer.target.forEach { value ->
                    answer.tiles.indices.firstOrNull { it !in current.placed && answer.tiles[it] == value }?.let(::placeTile)
                }
                is Answer.Trace -> traceFinished()
            }
        }
        next()
    }

    override fun onCleared() {
        raceJob?.cancel()
        speaker.onSpeaking = null
        speaker.shutdown()
        sounds.release()
        music.release()
        saver.shutdown()
    }
}
