package app.komet.ui.screens

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.platform.LocalDensity
import app.komet.ui.scene.AdventureArtwork
import app.komet.domain.Adventures
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import app.komet.domain.Answer
import app.komet.domain.Topic
import app.komet.domain.Topics
import app.komet.domain.txt
import app.komet.ui.scene.MissionFlight
import app.komet.ui.theme.LocalMotion
import app.komet.ui.components.LocalFeedback
import app.komet.audio.Sfx
import app.komet.domain.Strokes
import app.komet.domain.Subject
import app.komet.domain.inCase
import app.komet.domain.Token
import app.komet.domain.Visual
import app.komet.ui.KometViewModel
import app.komet.ui.Phase
import app.komet.ui.RoundState
import app.komet.ui.S
import app.komet.ui.CompanionMessage
import app.komet.ui.companionMessage
import app.komet.ui.components.AnswerDisplay
import app.komet.ui.components.BigButton
import app.komet.ui.components.BuildSlots
import app.komet.ui.components.BuildTiles
import app.komet.ui.components.ChoiceGrid
import app.komet.ui.components.ConfettiBurst
import app.komet.ui.components.KometIcons
import app.komet.ui.components.Keypad
import app.komet.ui.components.OptionLook
import app.komet.ui.components.ProgressTrack
import app.komet.ui.components.RoundIconButton
import app.komet.ui.components.TaskVisual
import app.komet.ui.components.VisualState
import app.komet.ui.components.answerText
import app.komet.ui.components.str
import app.komet.ui.components.subjectColors
import app.komet.ui.theme.K
import app.komet.ui.theme.LocalReading
import app.komet.ui.theme.LocalVisualBox
import app.komet.ui.theme.ReadingFont
import app.komet.ui.components.TracingPad
import app.komet.ui.components.fixedSp
import app.komet.ui.components.subjectTone
import app.komet.domain.ShopSlot
import app.komet.ui.components.CloseButton
import app.komet.ui.scene.Astronaut
import app.komet.ui.scene.Gear
import app.komet.ui.scene.TaskCompanionButton
import app.komet.ui.scene.TaskCompanionDialog
import app.komet.ui.scene.HeroPose
import app.komet.ui.scene.JourneyTrack
import app.komet.ui.scene.SubjectBackdrop
import app.komet.ui.scene.rememberSceneTime
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import kotlinx.coroutines.delay

@Composable
fun PlayScreen(vm: KometViewModel, onQuit: () -> Unit) {
    val round = vm.round ?: return
    val question = round.question
    val phase = round.phase
    val mission = Topics.of(round.skill) == Topic.MISSIONS || round.adventureId != null
    val atlas = Topics.of(round.source) == Topic.GEOGRAPHY && !mission
    val motion = LocalMotion.current
    val feedback = LocalFeedback.current
    var companionOpen by remember(round, round.index) { mutableStateOf(false) }
    val companion = round.companionMessage()
    var launching by remember(round, round.index) { mutableStateOf(false) }
    LaunchedEffect(launching) {
        if (launching) {
            feedback.sfx(Sfx.WHOOSH)
            delay(if (motion) 1250L else 150L)
            feedback.sfx(Sfx.SPARKLE)
            vm.next()
        }
    }
    val view = LocalView.current
    val haptics = vm.settings.haptics
    val (accent, _) = subjectColors(round.skill.subject)

    // Read the task aloud when it appears.
    LaunchedEffect(round, round.index) {
        if (vm.settings.autoRead) {
            delay(300)
            if (!companionOpen && round.phase == Phase.ANSWERING) vm.readQuestion()
        }
    }
    // Move on by itself after a right answer; long rewards get time to be read aloud.
    LaunchedEffect(round, round.index, phase, companionOpen) {
        if (phase == Phase.CORRECT && !mission && !atlas && !companionOpen) {
            val reward = question.reward?.nn?.length ?: 0
            delay((1100L + reward * 45L).coerceAtMost(3800L))
            vm.next()
        }
    }
    LaunchedEffect(round.shakeCount) {
        if (round.shakeCount > 0 && haptics) {
            view.performHapticFeedback(if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.REJECT else HapticFeedbackConstants.LONG_PRESS)
        }
    }
    LaunchedEffect(phase) {
        if (phase == Phase.CORRECT && haptics) {
            view.performHapticFeedback(if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }

    var tryAgainVisible by remember(round, round.index) { mutableStateOf(false) }
    LaunchedEffect(round, round.index, round.shakeCount, phase) {
        tryAgainVisible = false
        if (round.misses > 0 && round.phase == Phase.ANSWERING) {
            tryAgainVisible = true
            delay(1300)
            tryAgainVisible = false
        }
    }

    val answered = phase != Phase.ANSWERING
    val correctText = answerText(question.answer)
    val visualState = VisualState(
        input = round.input,
        answered = answered,
        correctValue = if (answered) correctText else null,
        // English listening needs an English voice; without one the word is shown instead.
        speechAvailable = vm.speechAvailable && (round.skill.subject != Subject.ENGLISH || vm.speaker.englishAvailable),
    )

    // The banner sits in the column rather than over it, so the highlighted answer stays visible.
    val banner: @Composable ColumnScope.() -> Unit = {
        AnimatedVisibility(
            visible = answered,
            enter = slideInVertically(tween(240)) { it / 2 } + fadeIn(tween(200)),
            exit = slideOutVertically(tween(140)) { it / 2 } + fadeOut(tween(100)),
        ) {
            FeedbackBanner(
                correct = phase == Phase.CORRECT,
                praise = S.praise[round.praise % S.praise.size].str(),
                answer = correctText,
                explanation = question.explanation?.str(),
                answerFont = if (round.skill.subject == Subject.READING) ReadingFont else null,
                onNext = { if (mission) { if (!launching) launching = true } else vm.next() },
                nextText = if (mission) {
                    if (launching) txt("På veg!", "På vei!").str() else txt("Send hjelpa!", "Send hjelpen!").str()
                } else S.onward.str(),
                showExplanation = mission,
            )
        }
    }

    if (atlas) {
        AtlasRound(vm, round, onQuit) { companionOpen = true }
        if (companionOpen) TaskCompanionDialog(vm, round, companion, onClose = { companionOpen = false })
        return
    }
    Box(Modifier.fillMaxSize()) {
        SubjectBackdrop(round.skill.subject)
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            val wide = maxWidth >= (if (LocalDensity.current.fontScale >= 1.6f) 840.dp else 720.dp) && maxHeight >= 450.dp
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                TopBar(vm, round, accent, onQuit, companion, showCompanion = !wide, companionEnabled = !launching) { companionOpen = true }
                if (wide) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Row(
                            Modifier
                                .widthIn(max = 1180.dp)
                                .heightIn(max = 620.dp)
                                .fillMaxSize()
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                        ) {
                            Column(Modifier.weight(1.1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)) {
                                PromptLine(question.prompt.str(), tryAgainVisible)
                                if (round.source.id == "s_planet_clues" && question.visual is Visual.Story && !round.hintShown) {
                                    Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())
                                        .clip(RoundedCornerShape(24.dp)).background(K.Paper)) {
                                        AdventureArtwork(Subject.SPACE, vm.profile?.let { Adventures.completed(it, Subject.SPACE) } ?: 0,
                                            Modifier.fillMaxWidth().height(186.dp).clearAndSetSemantics {})
                                        TaskVisual(question.visual, visualState, onListen = { vm.say(it) }, modifier = Modifier.fillMaxWidth().padding(18.dp))
                                    }
                                } else if (question.visual == Visual.None && !round.hintShown && question.answer !is Answer.Build && question.answer !is Answer.Trace) {
                                    AdventureArtwork(round.skill.subject, vm.profile?.let { Adventures.completed(it, round.skill.subject) } ?: 0,
                                        Modifier.weight(1f, fill = false).fillMaxWidth().heightIn(max = 320.dp).fillMaxHeight().clip(RoundedCornerShape(22.dp)))
                                } else {
                                    val compactCard = !mission && !round.hintShown && (question.visual is Visual.Story || question.visual is Visual.Equation || question.visual is Visual.Glyph)
                                    val card = if (compactCard) Modifier.weight(1f, fill = false).heightIn(max = 300.dp) else Modifier.weight(1f)
                                    TaskCard(vm, round, visualState, card, launching)
                                }
                            }
                            Column(
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                            ) {
                                HeroCorner(vm, round, companion, companionEnabled = !launching) { companionOpen = true }
                                AnswerArea(vm, round, correctText)
                                banner()
                            }
                        }
                    }
                } else {
                    Column(
                        Modifier
                            .widthIn(max = 720.dp)
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        PromptLine(question.prompt.str(), tryAgainVisible)
                        TaskCard(vm, round, visualState, Modifier.weight(1f), launching)
                        AnswerArea(vm, round, correctText)
                        banner()
                    }
                }
            }
        }

        if (phase == Phase.CORRECT && round.isLast) ConfettiBurst(round.index, Modifier.fillMaxSize())
    }

    if (companionOpen) {
        TaskCompanionDialog(vm, round, companion, onClose = { companionOpen = false })
    }
}

@Composable
private fun TopBar(
    vm: KometViewModel,
    round: RoundState,
    accent: Color,
    onQuit: () -> Unit,
    companion: CompanionMessage,
    showCompanion: Boolean,
    companionEnabled: Boolean,
    onCompanion: () -> Unit,
) {
    Row(
        Modifier
            .widthIn(max = 960.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val done = round.index + if (round.phase == Phase.ANSWERING) 0 else 1
        val player = vm.profile
        if (showCompanion) {
            TaskCompanionButton(companion, player?.equipped?.get(ShopSlot.BOLT), onCompanion, enabled = companionEnabled)
        }
        if (player != null) {
            JourneyTrack(done, round.questions.size, accent, player.hero, Modifier.weight(1f), gear = Gear.of(player.equipped))
        } else {
            ProgressTrack(done / round.questions.size.toFloat(), Modifier.weight(1f), color = accent, track = K.SurfaceHigh, height = 16.dp)
        }
        if (round.question.hint != null && !round.hintShown && round.phase == Phase.ANSWERING) {
            RoundIconButton(KometIcons.Bulb, S.hint.str(), { vm.showHint() }, size = 64.dp, face = K.Gold, edge = K.GoldDeep, tint = K.Ink)
        }
        if (vm.speechAvailable && !showCompanion) {
            RoundIconButton(KometIcons.Speaker, S.readQuestion.str(), { vm.readQuestion() }, size = 64.dp)
        }
        CloseButton(onClick = onQuit, size = 64.dp)
    }
}

/**
 * On wide screens the astronaut and Bolt keep the child company beside the answers. Bolt uses the
 * same response and help action as on the phone, including when speech or motion is disabled.
 */
@Composable
private fun HeroCorner(vm: KometViewModel, round: RoundState, companion: CompanionMessage, companionEnabled: Boolean, onCompanion: () -> Unit) {
    val player = vm.profile ?: return
    val hero = player.hero
    val time = rememberSceneTime()
    val phase = round.phase
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom,
    ) {
        Astronaut(
            look = hero,
            time = time,
            pose = { if (phase == Phase.CORRECT) HeroPose.CHEER else HeroPose.IDLE },
            gear = Gear.of(player.equipped),
            modifier = Modifier.size(88.dp, 132.dp),
        )
        TaskCompanionButton(
            message = companion,
            paint = player.equipped[ShopSlot.BOLT],
            onClick = onCompanion,
            enabled = companionEnabled,
            modifier = Modifier.padding(bottom = 48.dp),
            size = 80.dp,
        )
    }
}

/** The task text; after a miss it briefly gives way to «Prøv igjen» instead of covering it. */
@Composable
private fun PromptLine(prompt: String, tryAgain: Boolean) {
    AnimatedContent(
        targetState = tryAgain,
        transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
        label = "prompt",
    ) { showTryAgain ->
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (showTryAgain) {
                Text(
                    S.tryAgain.str(),
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = ReadingFont),
                    color = K.Ink,
                    modifier = Modifier
                        .background(K.Reveal, RoundedCornerShape(50))
                        .padding(horizontal = 18.dp, vertical = 6.dp),
                )
            } else {
                // Children who are learning to read try the instruction too, so it uses the reading font.
                Text(
                    prompt,
                    style = MaterialTheme.typography.headlineSmall.copy(fontFamily = ReadingFont),
                    color = K.Text,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }
        }
    }
}

private fun Visual.hasBlank(): Boolean = when (this) {
    is Visual.Equation -> tokens.any { it == Token.Blank }
    is Visual.Balance -> (left + right).any { it == Token.Blank }
    is Visual.Stack -> items.any { it.hasBlank() }
    else -> false
}

@Composable
private fun TaskCard(vm: KometViewModel, round: RoundState, state: VisualState, modifier: Modifier, launching: Boolean = false) {
    val question = round.question
    val trace = question.answer as? Answer.Trace
    if (trace != null) {
        TracingCard(vm, round, trace, modifier)
        return
    }
    if (question.visual == Visual.None && !round.hintShown && question.answer !is Answer.Build) {
        Box(modifier)
        return
    }
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .lessonPaper(),
        contentAlignment = Alignment.Center,
    ) {
        val hint = question.hint
        val mission = Topics.of(round.skill) == Topic.MISSIONS || round.adventureId != null
        val delivered = round.phase != Phase.ANSWERING
        val sceneHeight = if (delivered) (maxHeight - 36.dp).coerceAtLeast(80.dp) else 160.dp
        val showMissionScene = mission && (delivered || maxHeight >= 340.dp)
        val showHint = round.hintShown && hint != null
        // Word-building tasks keep a row free for the slots under the picture.
        val reserved = if (question.answer is Answer.Build) 100.dp else 0.dp
        val inner = DpSize(maxWidth - 36.dp, ((maxHeight - 36.dp - reserved) * (if (showHint) 0.5f else 1f)).coerceAtLeast(110.dp))
        CompositionLocalProvider(LocalVisualBox provides inner) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (showMissionScene) {
                    MissionFlight(round.index, round.questions.size, launching,
                        Modifier.fillMaxWidth().height(sceneHeight).clip(RoundedCornerShape(22.dp)), missionId = round.skill.id)
                }
                if (!mission || !delivered) {
                    TaskVisual(question.visual, state, onListen = { vm.say(it) }, modifier = Modifier.fillMaxWidth())
                }
                val build = question.answer as? Answer.Build
                if (build != null) {
                    BuildSlots(build, round.placed, revealed = round.phase == Phase.REVEALED)
                }
                if (showHint && (!mission || !delivered)) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(K.Gold.copy(alpha = 0.16f), RoundedCornerShape(20.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        TaskVisual(hint, state.copy(input = "", answered = false, correctValue = null), onListen = { vm.say(it) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.AnswerArea(vm: KometViewModel, round: RoundState, correctText: String?) {
    val question = round.question
    val answering = round.phase == Phase.ANSWERING
    when (val answer = question.answer) {
        is Answer.Choice -> {
            val looks = answer.options.indices.map { index ->
                when {
                    !answering && index == answer.correct -> OptionLook.CORRECT
                    index in round.disabled -> OptionLook.WRONG
                    !answering -> OptionLook.FADED
                    else -> OptionLook.NORMAL
                }
            }
            val hunt = if (round.source.id == "s_planet_clues") answer.options.mapNotNull { option ->
                app.komet.domain.SolarDestination.entries.firstOrNull { it.planet.art == (option as? app.komet.domain.Option.Art)?.art }
            } else emptyList()
            if (hunt.size == answer.options.size) app.komet.ui.scene.PlanetHunt(hunt, looks, vm::choose)
            else ChoiceGrid(
                answer = answer,
                looks = looks,
                shakeTarget = round.shakeTarget,
                shakeCount = round.shakeCount,
                onChoose = vm::choose,
                enabled = answering,
                modifier = Modifier.padding(bottom = if (answering) 12.dp else 0.dp),
            )
        }
        is Answer.NumberInput -> {
            if (!question.visual.hasBlank()) {
                AnswerDisplay(
                    value = if (!answering) correctText.orEmpty() else round.input,
                    answered = !answering,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
            // Once the answer is known the keypad makes room for the banner.
            if (answering) {
                Keypad(
                    onDigit = vm::typeDigit,
                    onErase = vm::eraseDigit,
                    onSubmit = vm::submitNumber,
                    canSubmit = round.input.isNotEmpty(),
                    enabled = true,
                    shakeCount = round.shakeCount,
                    modifier = Modifier
                        .widthIn(max = 460.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 12.dp),
                )
            }
        }
        // Writing happens on the card; down here is the picture word or the things to count.
        is Answer.Trace -> TraceExample(question.visual, subjectTone(round.skill.subject).top)
        // The slots live on the task card; only the tiles are down here, and they leave once the word is done.
        is Answer.Build -> if (answering) {
            BuildTiles(
                answer = answer,
                placed = round.placed,
                misses = round.misses,
                shakeTarget = round.shakeTarget,
                shakeCount = round.shakeCount,
                enabled = true,
                onTile = vm::placeTile,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
    }
}

private fun Modifier.lessonPaper(): Modifier {
    val shape = RoundedCornerShape(24.dp)
    return clip(shape)
        .background(Brush.verticalGradient(listOf(K.Paper, K.Paper, K.PaperShade)))
        .border(1.dp, Brush.verticalGradient(listOf(Color.White, K.PaperShade)), shape)
}

/** The writing pad on its paper card. */
@Composable
private fun TracingCard(vm: KometViewModel, round: RoundState, answer: Answer.Trace, modifier: Modifier) {
    val glyph = remember(answer.symbol) { Strokes.glyph(answer.symbol) }
    val description = round.question.prompt.str() + " " + answer.symbol
    Box(
        modifier
            .fillMaxWidth()
            .lessonPaper(),
    ) {
        if (glyph != null) {
            TracingPad(
                glyph = glyph,
                resetKey = round to round.index,
                accent = subjectTone(round.skill.subject).face,
                finished = round.phase != Phase.ANSWERING,
                description = description,
                onStroke = vm::traceStroke,
                onDone = vm::traceFinished,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
            )
        }
    }
}

/** A word that starts with the letter, first letter lit, or as many things as the digit says. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TraceExample(visual: Visual, accent: Color) {
    val prefs = LocalReading.current
    when (visual) {
        is Visual.Picture -> Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(visual.emoji, fontSize = fixedSp(48.dp))
            visual.word?.inCase(prefs.letterCase)?.let { word ->
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = accent)) { append(word.take(1)) }
                        append(word.drop(1))
                    },
                    color = K.Text,
                    fontFamily = ReadingFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = fixedSp(42.dp),
                    maxLines = 1,
                )
            }
        }
        is Visual.Objects -> FlowRow(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(visual.count) { Text(visual.emoji, fontSize = fixedSp(32.dp)) }
        }
        else -> Unit
    }
}

@Composable
private fun FeedbackBanner(
    correct: Boolean,
    praise: String,
    answer: String?,
    explanation: String?,
    answerFont: FontFamily?,
    onNext: () -> Unit,
    nextText: String,
    showExplanation: Boolean = false,
) {
    val background = if (correct) Color(0xFF123B2B) else Color(0xFF462A10)
    val answerWas = S.answerWas.str()
    val title = if (correct) {
        AnnotatedString(praise)
    } else {
        buildAnnotatedString {
            append(answerWas)
            if (answer != null) {
                append(": ")
                withStyle(SpanStyle(fontFamily = answerFont)) { append(answer) }
            }
        }
    }
    Column(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(background)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(Modifier.fillMaxWidth()) {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                color = if (correct) K.Good else K.Reveal,
                fontWeight = FontWeight.Black,
            )
            if ((!correct || showExplanation) && explanation != null) {
                Text(explanation, style = MaterialTheme.typography.titleMedium.copy(fontFamily = answerFont), color = K.Text)
            }
        }
        BigButton(
            text = nextText,
            onClick = onNext,
            face = if (correct) K.Good else K.Reveal,
            edge = if (correct) K.GoodDeep else Color(0xFFB9761D),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
