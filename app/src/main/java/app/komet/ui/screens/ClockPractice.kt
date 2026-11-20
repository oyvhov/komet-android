package app.komet.ui.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.komet.audio.Sfx
import app.komet.domain.CLOCK_ROUND_LENGTH
import app.komet.domain.ClockLevel
import app.komet.domain.ClockTime
import app.komet.domain.Txt
import app.komet.ui.KometViewModel
import app.komet.ui.S
import app.komet.ui.components.GameText
import app.komet.ui.components.KometIcons
import app.komet.ui.components.PressSurface
import app.komet.ui.components.RoundIconButton
import app.komet.ui.components.fixedSp
import app.komet.ui.components.str
import app.komet.ui.theme.K
import kotlin.random.Random

private val readClock = Txt("Kva er klokka?", "Hva er klokka?")
private val setClock = Txt("Still klokka")
private val matchTime = Txt("Vel tida som passar.", "Velg tiden som passer.")
private val clockHint = Txt("Den korte visaren viser timen. Den lange viser minutta.", "Den korte viseren viser timen. Den lange viser minuttene.")
private val hintLabel = Txt("Få eit tips", "Få et tips")
private val chooseLevel = Txt("Vel kva du vil øve på", "Velg hva du vil øve på")
private val checkTime = Txt("Sjekk klokka")
private val nextTime = Txt("Neste")
private val seeRound = Txt("Sjå resultatet", "Se resultatet")
private val correctTime = Txt("Rett!", "Riktig!")
private val revealedTime = Txt("Rett svar", "Riktig svar")
private val tryTimeAgain = Txt("Prøv igjen.")
private val changeLevel = Txt("Vel øving", "Velg øving")

private enum class ClockAnswer { WAITING, CORRECT, REVEALED }

@Composable
internal fun ClockPractice(vm: KometViewModel, scroll: ScrollState) {
    var level by rememberSaveable { mutableStateOf<ClockLevel?>(null) }
    var seed by rememberSaveable { mutableStateOf(0L) }
    val playing = level
    LaunchedEffect(playing) { scroll.scrollTo(0) }
    if (playing == null) {
        ChooseClockLevel { option ->
            seed = Random.nextLong()
            level = option
        }
    } else {
        key(playing, seed) {
            ClockRound(
                vm, playing, seed, scroll,
                replay = { seed = Random.nextLong() },
                leave = { level = null },
            )
        }
    }
}

@Composable
private fun ChooseClockLevel(onSelect: (ClockLevel) -> Unit) {
    BoxWithConstraints(Modifier.widthIn(max = 510.dp).fillMaxWidth()) {
        val columns = if (maxWidth < 294.dp || LocalDensity.current.fontScale > 1.6f) 1 else 2
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GameText(chooseLevel.str(), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            ClockLevel.entries.chunked(columns).forEach { options ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    options.forEach { option ->
                        ClockLevelTile(option, Modifier.weight(1f), onClick = { onSelect(option) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ClockLevelTile(level: ClockLevel, modifier: Modifier, onClick: () -> Unit) {
    val example = when (level) {
        ClockLevel.HOURS -> ClockTime.at(8, 0)
        ClockLevel.HALVES -> ClockTime.at(9, 30)
        ClockLevel.QUARTERS -> ClockTime.at(4, 15)
        ClockLevel.FIVE_MINUTES -> ClockTime.at(10, 25)
    }
    val first = level == ClockLevel.HOURS
    PressSurface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 138.dp),
        face = if (first) lerp(K.Surface, K.GoldDeep, 0.25f) else K.Surface,
        top = if (first) lerp(K.SurfaceHigh, K.Gold, 0.24f) else K.SurfaceHigh,
        edge = K.SpaceTop,
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 9.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Box(Modifier.clearAndSetSemantics {}) { WorkshopDial(example, Modifier.size(80.dp)) }
            Text(
                level.label.str(),
                style = MaterialTheme.typography.titleMedium,
                color = if (first) K.GoldTop else K.Text,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ClockRound(vm: KometViewModel, level: ClockLevel, seed: Long, scroll: ScrollState, replay: () -> Unit, leave: () -> Unit) {
    val tasks = remember(level, seed) { level.round(seed) }
    var index by rememberSaveable { mutableIntStateOf(0) }
    var firstTry by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(index) { scroll.scrollTo(0) }
    if (index == tasks.size) {
        Column(
            Modifier.widthIn(max = 470.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ClockPanel {
                GameText(Txt("Runden er ferdig!").str(), textAlign = TextAlign.Center)
                ClockInstruction(Txt("$firstTry av $CLOCK_ROUND_LENGTH rette på første forsøk.", "$firstTry av $CLOCK_ROUND_LENGTH riktige på første forsøk."))
            }
            WorkshopAction(S.playAgain, replay, primary = true)
            WorkshopAction(changeLevel, leave)
        }
        return
    }

    val task = tasks[index]
    key(index) {
        var minutes by rememberSaveable { mutableIntStateOf(task.start.totalMinutes) }
        var misses by rememberSaveable { mutableIntStateOf(0) }
        var showHint by rememberSaveable { mutableStateOf(false) }
        var answer by rememberSaveable { mutableStateOf(ClockAnswer.WAITING) }
        val editing = task.setHands && answer == ClockAnswer.WAITING
        val dial = if (task.setHands) ClockTime.fromMinutes(minutes) else task.target
        val title = if (task.setHands) setClock else readClock
        val speakingPrompt = if (task.setHands) {
            val digital = task.target.spokenDigital()
            Txt("${setClock.nn}. ${digital.nn}", "${setClock.nb}. ${digital.nb}")
        } else title

        LaunchedEffect(task) { if (answer == ClockAnswer.WAITING) vm.say(speakingPrompt) }

        fun submit(time: ClockTime) {
            if (answer != ClockAnswer.WAITING) return
            if (task.matches(time)) {
                if (misses == 0) firstTry++
                answer = ClockAnswer.CORRECT
                vm.feedback.sfx(Sfx.CORRECT, 0.8f)
                vm.say(correctTime)
            } else {
                misses++
                vm.feedback.sfx(Sfx.WRONG, 0.55f)
                if (misses == 2) {
                    answer = ClockAnswer.REVEALED
                    minutes = task.target.totalMinutes
                    vm.say(task.target.dialClue())
                } else vm.say(tryTimeAgain)
            }
        }

        ClockStage(
            dial = { size ->
                WorkshopDial(
                    dial, Modifier.size(size), level.minuteStep,
                    onChange = if (editing) { time -> minutes = time.dialTime().totalMinutes } else null,
                )
            },
            controls = {
                ClockPanel {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "${level.label.str()} · ${index + 1} / $CLOCK_ROUND_LENGTH",
                                color = ClockMint, style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            GameText(title.str(), style = MaterialTheme.typography.headlineSmall)
                            if (task.setHands) DigitalTime(task.target)
                        }
                        if (vm.speechAvailable) {
                            RoundIconButton(KometIcons.Speaker, S.readAloud.str(), size = 64.dp, onClick = {
                                vm.say(if (task.setHands) speakingPrompt else task.target.dialClue())
                            })
                        }
                    }
                }

                if (answer == ClockAnswer.WAITING) {
                    if (editing) {
                        ClockAdjustments(dial, level.minuteStep) { minutes = it.dialTime().totalMinutes }
                        WorkshopAction(checkTime, { submit(dial) }, primary = true)
                    } else {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            task.choices.forEach { time ->
                                DigitalChoice(time, { submit(time) }, Modifier.weight(1f))
                            }
                        }
                    }
                    if (misses > 0) {
                        Box(Modifier.semantics { liveRegion = LiveRegionMode.Polite }) { ClockInstruction(tryTimeAgain) }
                    }
                    if (showHint) ClockInstruction(clockHint)
                    else {
                        WorkshopAction(hintLabel, {
                            showHint = true
                            vm.say(clockHint)
                        })
                        ClockInstruction(if (editing) moveHands else matchTime)
                    }
                } else {
                    ClockPanel {
                        Column(Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
                            GameText(
                                (if (answer == ClockAnswer.CORRECT) correctTime else revealedTime).str(),
                                style = MaterialTheme.typography.titleLarge,
                                color = if (answer == ClockAnswer.CORRECT) ClockMint else ClockGold,
                            )
                            if (!task.setHands) DigitalTime(task.target)
                            Text(task.target.dialClue().str(), color = K.Text, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    WorkshopAction(if (index == tasks.lastIndex) seeRound else nextTime, { index++ }, primary = true)
                }
                WorkshopAction(changeLevel, leave)
            },
        )
    }
}

@Composable
private fun DigitalChoice(time: ClockTime, onClick: () -> Unit, modifier: Modifier) {
    PressSurface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 64.dp),
        face = K.SurfaceHigh,
        edge = K.SurfaceLow,
        description = time.spokenDigital().str(),
        contentPadding = PaddingValues(horizontal = 3.dp, vertical = 8.dp),
    ) {
        GameText(
            time.digital,
            color = ClockMint,
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace),
            fontSize = fixedSp(20.dp),
            textAlign = TextAlign.Center,
            maxLines = 1,
            autoFit = true,
        )
    }
}
