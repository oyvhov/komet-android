package app.komet.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.komet.domain.PlanetLook
import app.komet.domain.RaceMode
import app.komet.ui.KometViewModel
import app.komet.ui.RacePhase
import app.komet.ui.S
import app.komet.ui.components.ChoiceGrid
import app.komet.ui.components.KometIcons
import app.komet.ui.components.OptionLook
import app.komet.ui.components.PageColumn
import app.komet.ui.components.Pill
import app.komet.ui.components.PlanetArt
import app.komet.ui.components.PressSurface
import app.komet.ui.components.ProgressTrack
import app.komet.ui.components.RocketArt
import app.komet.ui.components.RoundIconButton
import app.komet.ui.components.ScreenTopBar
import app.komet.ui.components.TaskVisual
import app.komet.ui.components.VisualState
import app.komet.ui.components.fixedSp
import app.komet.ui.components.str
import app.komet.ui.theme.K

@Composable
fun RaceMenuScreen(vm: KometViewModel) {
    val profile = vm.profile ?: return
    PageColumn {
        ScreenTopBar(S.race.str(), onBack = { vm.back() })
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            RocketArt(Modifier.size(64.dp, 100.dp))
            Text(S.raceRules.str(), style = MaterialTheme.typography.titleLarge, color = K.Muted, modifier = Modifier.weight(1f))
        }
        Text(S.chooseRace.str(), style = MaterialTheme.typography.headlineSmall, color = K.Text)
        RaceMode.entries.forEach { mode ->
            val best = profile.raceBest[mode] ?: 0
            PressSurface(
                onClick = { vm.startRace(mode) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 88.dp),
                face = K.Surface,
                edge = K.RaceDeep,
                contentAlignment = Alignment.CenterStart,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        Modifier
                            .size(58.dp)
                            .background(K.Race, RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(mode.symbol, color = K.Ink, fontSize = fixedSp(30.dp), fontWeight = FontWeight.Black)
                    }
                    Text(mode.title.str(), style = MaterialTheme.typography.titleLarge, color = K.Text, modifier = Modifier.weight(1f))
                    if (best > 0) Pill(S.record(best).str())
                }
            }
        }
    }
}

@Composable
fun RaceScreen(vm: KometViewModel) {
    val race = vm.race ?: return
    val profile = vm.profile ?: return
    val best = profile.raceBest[race.mode] ?: 0
    val target = maxOf(20, best + 5)

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            Modifier
                .widthIn(max = 960.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RoundIconButton(KometIcons.Close, S.close.str(), { vm.back() }, size = 50.dp)
            ProgressTrack(race.remainingMs / 60_000f, Modifier.weight(1f), color = if (race.remainingMs < 10_000) K.Bad else K.Race, height = 16.dp)
            Text("${(race.remainingMs + 999) / 1000}", style = MaterialTheme.typography.headlineSmall, color = K.Text, modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
            Pill(race.score.toString(), star = true)
        }
        Row(
            Modifier
                .widthIn(max = 960.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            RocketTrack(score = race.score, target = target, best = best, modifier = Modifier.width(64.dp).fillMaxHeight())
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(30.dp))
                        .background(K.Paper),
                    contentAlignment = Alignment.Center,
                ) {
                    if (race.phase == RacePhase.COUNTDOWN) {
                        Text(
                            if (race.countdown > 0) race.countdown.toString() else S.go.str(),
                            color = K.Race,
                            fontSize = fixedSp(120.dp),
                            fontWeight = FontWeight.Black,
                        )
                    } else {
                        TaskVisual(race.question.visual, VisualState(speechAvailable = vm.speechAvailable), onListen = {}, modifier = Modifier.padding(16.dp))
                    }
                }
                val answer = race.question.answer as? app.komet.domain.Answer.Choice
                if (answer != null) {
                    ChoiceGrid(
                        answer = answer,
                        looks = answer.options.indices.map { if (it in race.disabled) OptionLook.WRONG else OptionLook.NORMAL },
                        shakeTarget = race.shakeTarget,
                        shakeCount = race.shakeCount,
                        onChoose = vm::raceAnswer,
                        enabled = race.phase == RacePhase.RUNNING,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }
            }
        }
    }
}

/** Earth at the bottom, the moon at the top, and the rocket climbing one step per right answer. */
@Composable
private fun RocketTrack(score: Int, target: Int, best: Int, modifier: Modifier = Modifier) {
    val progress by animateFloatAsState((score / target.toFloat()).coerceIn(0f, 1f), tween(350), label = "rocket")
    BoxWithConstraints(modifier) {
        val trackTop = 56.dp
        val trackBottom = maxHeight - 56.dp
        Canvas(Modifier.fillMaxSize()) {
            val x = size.width / 2
            drawLine(
                Color.White.copy(alpha = 0.25f),
                Offset(x, trackTop.toPx()),
                Offset(x, trackBottom.toPx()),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 16f)),
            )
            if (best in 1 until target) {
                val y = trackBottom.toPx() - (trackBottom - trackTop).toPx() * (best / target.toFloat())
                drawLine(K.Gold, Offset(x - 22.dp.toPx(), y), Offset(x + 22.dp.toPx(), y), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
            }
        }
        PlanetArt(PlanetLook(0xFFD2D2D6, 0xFFF2F2F4, 0xFF7C7C86, craters = true), Modifier.size(56.dp).align(Alignment.TopCenter))
        PlanetArt(PlanetLook(0xFF3D8BFF, 0xFF9FE7FF, 0xFF1B4FA8, bands = true), Modifier.size(56.dp).align(Alignment.BottomCenter))
        val rocketY = trackBottom - (trackBottom - trackTop) * progress - 40.dp
        RocketArt(
            Modifier
                .size(40.dp, 64.dp)
                .align(Alignment.TopCenter)
                .offset(y = rocketY),
        )
    }
}
