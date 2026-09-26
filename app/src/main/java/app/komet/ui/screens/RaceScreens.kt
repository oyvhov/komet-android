package app.komet.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.draw.shadow
import app.komet.domain.PlanetLook
import app.komet.domain.RaceMode
import app.komet.ui.KometViewModel
import app.komet.ui.RacePhase
import app.komet.ui.S
import app.komet.ui.components.ChoiceGrid
import app.komet.ui.components.OptionLook
import app.komet.ui.components.PageColumn
import app.komet.ui.components.Pill
import app.komet.ui.components.PlanetArt
import app.komet.ui.components.PressSurface
import app.komet.ui.components.ProgressTrack
import app.komet.domain.ShopSlot
import app.komet.ui.components.CloseButton
import app.komet.ui.components.RocketArt
import app.komet.ui.scene.rocketPaint
import app.komet.ui.components.ScreenTopBar
import app.komet.ui.components.TaskVisual
import app.komet.ui.components.VisualState
import app.komet.ui.components.fixedSp
import app.komet.ui.components.str
import app.komet.ui.theme.K
import app.komet.ui.theme.LocalMotion
import app.komet.ui.components.gloss
import androidx.compose.foundation.border
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RaceMenuScreen(vm: KometViewModel) {
    val profile = vm.profile ?: return
    PageColumn(maxWidth = 1200.dp, contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)) {
        ScreenTopBar(S.race.str(), onBack = { vm.back() })
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            if (maxWidth >= 700.dp) {
                Row(horizontalArrangement = Arrangement.spacedBy(40.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(0.85f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        Box(Modifier.fillMaxWidth().heightIn(min = 300.dp), contentAlignment = Alignment.Center) {
                            PlanetArt(PlanetLook(0xFFB52F83, 0xFFFF9BC4, 0xFF582E70, craters = true), Modifier.size(210.dp))
                            RocketArt(Modifier.size(135.dp, 240.dp).offset(x = 35.dp, y = (-20).dp))
                        }
                        Text(S.raceRules.str(), style = MaterialTheme.typography.headlineSmall, color = K.Text, textAlign = TextAlign.Center)
                    }
                    Column(Modifier.weight(1.6f), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        app.komet.ui.components.GameText(S.chooseRace.str(), style = MaterialTheme.typography.headlineMedium)
                        RaceMode.entries.chunked(2).forEach { modes ->
                            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                                modes.forEach { mode ->
                                    RaceChoice(mode, profile.raceBest[mode] ?: 0, Modifier.weight(1f), tile = true) { vm.startRace(mode) }
                                }
                            }
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        RocketArt(Modifier.size(64.dp, 100.dp))
                        Text(S.raceRules.str(), style = MaterialTheme.typography.titleLarge, color = K.Muted, modifier = Modifier.weight(1f))
                    }
                    app.komet.ui.components.GameText(S.chooseRace.str(), style = MaterialTheme.typography.headlineSmall)
                    RaceMode.entries.forEach { mode ->
                        RaceChoice(mode, profile.raceBest[mode] ?: 0, Modifier.fillMaxWidth(), tile = false) { vm.startRace(mode) }
                    }
                }
            }
        }
    }
}

@Composable
private fun RaceChoice(mode: RaceMode, best: Int, modifier: Modifier, tile: Boolean, onChoose: () -> Unit) {
    PressSurface(
        onClick = onChoose,
        modifier = modifier.heightIn(min = if (tile) 205.dp else 88.dp),
        face = K.Surface,
        edge = K.RaceDeep,
        contentAlignment = Alignment.CenterStart,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        val emblem: @Composable () -> Unit = {
            Box(
                Modifier.size(58.dp).border(2.dp, K.Outline, RoundedCornerShape(18.dp))
                    .gloss(K.Race, RoundedCornerShape(18.dp), top = K.RaceTop),
                contentAlignment = Alignment.Center,
            ) {
                app.komet.ui.components.GameText(mode.symbol, style = MaterialTheme.typography.headlineMedium, fontSize = fixedSp(28.dp), maxLines = 1)
            }
        }
        if (tile) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                emblem()
                app.komet.ui.components.GameText(mode.title.str(), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                if (best > 0) Pill(S.record(best).str())
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                emblem()
                app.komet.ui.components.GameText(mode.title.str(), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                if (best > 0) Pill(S.record(best).str())
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
                .widthIn(max = 1200.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProgressTrack(race.remainingMs / 60_000f, Modifier.weight(1f), color = if (race.remainingMs < 10_000) K.Bad else K.Race, height = 16.dp)
            Text("${(race.remainingMs + 999) / 1000}", style = MaterialTheme.typography.headlineSmall, color = K.Text, modifier = Modifier.widthIn(min = 40.dp), textAlign = TextAlign.End)
            Pill(race.score.toString(), star = true)
            CloseButton(onClick = { vm.back() })
        }
        BoxWithConstraints(
            Modifier
                .widthIn(max = 1200.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            val wide = maxWidth >= 900.dp && maxWidth > maxHeight
            val question: @Composable (Modifier) -> Unit = { area ->
                Box(
                    area
                        .shadow(9.dp, RoundedCornerShape(30.dp))
                        .clip(RoundedCornerShape(30.dp))
                        .background(Brush.verticalGradient(listOf(K.Paper, K.PaperShade)))
                        .border(2.dp, K.RaceTop.copy(alpha = 0.7f), RoundedCornerShape(30.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    RaceChallengeBackdrop()
                    if (race.phase == RacePhase.COUNTDOWN) {
                        app.komet.ui.components.GameText(
                            if (race.countdown > 0) race.countdown.toString() else S.go.str(),
                            style = MaterialTheme.typography.displayLarge,
                            color = K.RaceTop,
                            fontSize = fixedSp(120.dp),
                        )
                    } else {
                        TaskVisual(race.question.visual, VisualState(speechAvailable = vm.speechAvailable), onListen = {}, modifier = Modifier.padding(16.dp))
                    }
                }
            }
            val answers: @Composable () -> Unit = {
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
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(if (wide) 28.dp else 14.dp)) {
                RocketTrack(score = race.score, target = target, best = best, paint = rocketPaint(profile.equipped[ShopSlot.ROCKET]), modifier = Modifier.width(if (wide) 120.dp else 76.dp).fillMaxHeight())
                if (wide) {
                    question(Modifier.weight(1.55f).fillMaxHeight().padding(bottom = 12.dp))
                    Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterVertically), horizontalAlignment = Alignment.CenterHorizontally) {
                        app.komet.ui.components.GameText(race.mode.title.str(), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                        answers()
                    }
                } else {
                    Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        question(Modifier.fillMaxWidth().weight(1f))
                        answers()
                    }
                }
            }
        }
    }
}

/** Gentle illustrated orbit marks give the large question surface depth without hiding the task. */
@Composable
private fun RaceChallengeBackdrop() {
    val motion = LocalMotion.current
    val time = if (motion) {
        val transition = rememberInfiniteTransition(label = "challenge-light")
        transition.animateFloat(0f, 1f, infiniteRepeatable(tween(8500, easing = LinearEasing)), label = "orbit-light")
    } else null
    Canvas(Modifier.fillMaxSize()) {
        val phase = time?.value ?: 0.15f
        val c = Offset(size.width * 0.52f, size.height * 0.5f)
        val r = size.width * 0.37f
        drawCircle(
            Brush.radialGradient(listOf(K.GoldTop.copy(alpha = 0.18f), Color.Transparent), center = c, radius = r * 1.3f),
            radius = r * 1.3f, center = c,
        )
        drawArc(K.Race.copy(alpha = 0.09f), 15f, 290f, false, Offset(c.x - r, c.y - r), Size(r * 2, r * 2), style = Stroke(2.dp.toPx()))
        drawArc(K.Math.copy(alpha = 0.1f), 180f, 245f, false, Offset(c.x - r * 0.72f, c.y - r * 0.72f), Size(r * 1.44f, r * 1.44f), style = Stroke(2.dp.toPx()))
        val marks = listOf(0.15f to 0.14f, 0.79f to 0.17f, 0.19f to 0.8f, 0.84f to 0.78f)
        marks.forEachIndexed { index, (x, y) ->
            val alpha = 0.19f + if (motion) 0.08f * sin(phase * 6.28f + index) else 0f
            val p = Offset(size.width * x, size.height * y)
            val arm = (3 + index % 2).dp.toPx()
            drawLine(K.GoldDeep.copy(alpha = alpha), p - Offset(arm, 0f), p + Offset(arm, 0f), 1.6.dp.toPx(), cap = StrokeCap.Round)
            drawLine(K.GoldDeep.copy(alpha = alpha), p - Offset(0f, arm), p + Offset(0f, arm), 1.6.dp.toPx(), cap = StrokeCap.Round)
        }
        val angle = phase * 6.28f
        val glint = Offset(c.x + cos(angle) * r, c.y + sin(angle) * r)
        drawCircle(K.Gold.copy(alpha = 0.34f), 4.dp.toPx(), glint)
    }
}

/** Earth at the bottom, the moon at the top, and the rocket climbing one step per right answer. */
@Composable
private fun RocketTrack(score: Int, target: Int, best: Int, paint: app.komet.ui.scene.RocketPaint, modifier: Modifier = Modifier) {
    val motion = LocalMotion.current
    val progress by animateFloatAsState((score / target.toFloat()).coerceIn(0f, 1f), if (motion) tween(550) else snap(), label = "rocket")
    val engineTime = if (motion) {
        val transition = rememberInfiniteTransition(label = "engine")
        transition.animateFloat(0f, 1f, infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse), label = "flame")
    } else null
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
            val rocketCenterY = trackBottom.toPx() - (trackBottom - trackTop).toPx() * progress
            drawLine(K.RaceTop.copy(alpha = 0.13f), Offset(x, trackBottom.toPx()), Offset(x, rocketCenterY), strokeWidth = 24.dp.toPx(), cap = StrokeCap.Round)
            drawLine(
                Brush.verticalGradient(listOf(K.GoldTop.copy(alpha = 0.85f), K.Race.copy(alpha = 0.42f)), startY = rocketCenterY, endY = trackBottom.toPx()),
                Offset(x, rocketCenterY), Offset(x, trackBottom.toPx()),
                strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round,
            )
            repeat(8) { index ->
                val f = (index + 0.5f) / 8f
                val py = trackBottom.toPx() - (trackBottom - trackTop).toPx() * f
                val sway = if (motion) kotlin.math.sin((engineTime?.value ?: 0f) * 6.28f + index * 1.9f) * 3.dp.toPx() else 0f
                drawCircle(K.GoldTop.copy(alpha = if (f <= progress) 0.8f else 0.22f), radius = (if (f <= progress) 2.8f else 1.6f).dp.toPx(), center = Offset(x + sway, py))
            }
            if (best in 1 until target) {
                val y = trackBottom.toPx() - (trackBottom - trackTop).toPx() * (best / target.toFloat())
                drawLine(K.Gold, Offset(x - 22.dp.toPx(), y), Offset(x + 22.dp.toPx(), y), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
                drawCircle(K.GoldTop, radius = 5.dp.toPx(), center = Offset(x, y))
            }
        }
        PlanetArt(PlanetLook(0xFFD2D2D6, 0xFFF2F2F4, 0xFF7C7C86, craters = true), Modifier.size(56.dp).align(Alignment.TopCenter))
        PlanetArt(PlanetLook(0xFF3D8BFF, 0xFF9FE7FF, 0xFF1B4FA8, bands = true), Modifier.size(56.dp).align(Alignment.BottomCenter))
        val rocketY = trackBottom - (trackBottom - trackTop) * progress - 40.dp
        Canvas(
            Modifier.size(36.dp, 40.dp).align(Alignment.TopCenter).offset(y = rocketY + 52.dp),
        ) {
            val flicker = engineTime?.value ?: 0.5f
            val tip = Offset(size.width / 2f, size.height * (0.92f + flicker * 0.08f))
            val flame = Path().apply {
                moveTo(size.width * 0.28f, 0f)
                quadraticTo(size.width * 0.15f, size.height * 0.5f, tip.x, tip.y)
                quadraticTo(size.width * 0.85f, size.height * 0.5f, size.width * 0.72f, 0f)
                close()
            }
            drawPath(flame, Brush.verticalGradient(listOf(K.GoldTop, K.Math, Color.Transparent)))
        }
        RocketArt(
            Modifier
                .size(40.dp, 64.dp)
                .align(Alignment.TopCenter)
                .offset(y = rocketY),
            body = paint.body,
            accent = paint.accent,
            stripes = paint.stripes,
        )
    }
}
