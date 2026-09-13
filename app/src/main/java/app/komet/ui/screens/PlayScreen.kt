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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import app.komet.domain.Subject
import app.komet.domain.Token
import app.komet.domain.Visual
import app.komet.ui.KometViewModel
import app.komet.ui.Phase
import app.komet.ui.RoundState
import app.komet.ui.S
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
import app.komet.ui.theme.LocalVisualBox
import app.komet.ui.theme.ReadingFont
import kotlinx.coroutines.delay

@Composable
fun PlayScreen(vm: KometViewModel, onQuit: () -> Unit) {
    val round = vm.round ?: return
    val question = round.question
    val phase = round.phase
    val view = LocalView.current
    val haptics = vm.settings.haptics
    val (accent, _) = subjectColors(round.skill.subject)

    // Read the task aloud when it appears.
    LaunchedEffect(round, round.index) {
        if (vm.settings.autoRead) {
            delay(300)
            vm.readQuestion()
        }
    }
    // Move on by itself after a right answer; long rewards get time to be read aloud.
    LaunchedEffect(round, round.index, phase) {
        if (phase == Phase.CORRECT) {
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

    var tryAgainVisible by remember { mutableStateOf(false) }
    LaunchedEffect(round.shakeCount) {
        if (round.shakeCount > 0 && round.phase == Phase.ANSWERING) {
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
        speechAvailable = vm.speechAvailable,
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
                onNext = { vm.next() },
            )
        }
    }

    Box(Modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val wide = maxWidth >= 840.dp && maxWidth > maxHeight
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                TopBar(vm, round, accent, onQuit)
                if (wide) {
                    Row(
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        Column(Modifier.weight(1.1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            PromptLine(question.prompt.str(), tryAgainVisible)
                            TaskCard(vm, round, visualState, Modifier.weight(1f))
                        }
                        Column(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                        ) {
                            AnswerArea(vm, round, correctText)
                            banner()
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
                        TaskCard(vm, round, visualState, Modifier.weight(1f))
                        AnswerArea(vm, round, correctText)
                        banner()
                    }
                }
            }
        }

        if (phase == Phase.CORRECT && round.isLast) ConfettiBurst(round.index, Modifier.fillMaxSize())
    }
}

@Composable
private fun TopBar(vm: KometViewModel, round: RoundState, accent: Color, onQuit: () -> Unit) {
    Row(
        Modifier
            .widthIn(max = 960.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RoundIconButton(KometIcons.Close, S.closeRound.str(), onQuit, size = 50.dp)
        val done = round.index + if (round.phase == Phase.ANSWERING) 0 else 1
        ProgressTrack(done / round.questions.size.toFloat(), Modifier.weight(1f), color = accent, track = K.SurfaceHigh, height = 16.dp)
        if (round.question.hint != null && !round.hintShown && round.phase == Phase.ANSWERING) {
            RoundIconButton(KometIcons.Bulb, S.hint.str(), { vm.showHint() }, size = 50.dp, face = K.Gold, edge = K.GoldDeep, tint = K.Ink)
        }
        if (vm.speechAvailable) {
            RoundIconButton(KometIcons.Speaker, S.readQuestion.str(), { vm.readQuestion() }, size = 50.dp)
        }
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
private fun TaskCard(vm: KometViewModel, round: RoundState, state: VisualState, modifier: Modifier) {
    val question = round.question
    if (question.visual == Visual.None && !round.hintShown && question.answer !is Answer.Build) {
        Box(modifier)
        return
    }
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(K.Paper),
        contentAlignment = Alignment.Center,
    ) {
        val hint = question.hint
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
                TaskVisual(question.visual, state, onListen = { vm.say(it) }, modifier = Modifier.fillMaxWidth())
                val build = question.answer as? Answer.Build
                if (build != null) {
                    BuildSlots(build, round.placed, revealed = round.phase == Phase.REVEALED)
                }
                if (showHint && hint != null) {
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
            ChoiceGrid(
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

@Composable
private fun FeedbackBanner(
    correct: Boolean,
    praise: String,
    answer: String?,
    explanation: String?,
    answerFont: FontFamily?,
    onNext: () -> Unit,
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
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier
                    .size(44.dp)
                    .background(if (correct) K.Good else K.Reveal, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(if (correct) KometIcons.Check else KometIcons.Info, contentDescription = null, tint = K.Ink, modifier = Modifier.size(28.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (correct) K.Good else K.Reveal,
                    fontWeight = FontWeight.Black,
                )
                if (!correct && explanation != null) {
                    Text(explanation, style = MaterialTheme.typography.titleMedium.copy(fontFamily = answerFont), color = K.Text)
                }
            }
        }
        BigButton(
            text = S.onward.str(),
            onClick = onNext,
            face = if (correct) K.Good else K.Reveal,
            edge = if (correct) K.GoodDeep else Color(0xFFB9761D),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
