package app.komet.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.komet.audio.Sfx
import app.komet.ui.KometViewModel
import app.komet.ui.S
import app.komet.ui.Screen
import app.komet.ui.components.BigButton
import app.komet.ui.components.ConfettiBurst
import app.komet.ui.components.GameText
import app.komet.ui.components.KometIcons
import app.komet.ui.components.LocalFeedback
import app.komet.ui.components.PageColumn
import app.komet.ui.components.Panel
import app.komet.ui.components.PressSurface
import app.komet.ui.components.SpaceCardArt
import app.komet.ui.components.StarGlyph
import app.komet.ui.components.str
import app.komet.ui.theme.K
import app.komet.ui.theme.LocalMotion
import kotlinx.coroutines.delay

@Composable
fun ResultScreen(vm: KometViewModel) {
    val info = vm.result ?: return
    val outcome = info.outcome
    val feedback = LocalFeedback.current
    val motion = LocalMotion.current
    val scales = remember(info) { List(3) { Animatable(if (motion) 0f else 1f) } }

    LaunchedEffect(info) {
        if (!motion) return@LaunchedEffect
        delay(450)
        for (index in 0 until 3) {
            if (index < outcome.stars) {
                feedback.sfx(Sfx.STAR)
                scales[index].animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow))
            } else {
                scales[index].snapTo(1f)
            }
            delay(120)
        }
        val newThings = outcome.newCards.isNotEmpty() || outcome.newRank != null
        if (newThings) {
            delay(250)
            feedback.sfx(Sfx.UNLOCK)
        }
    }

    Box(Modifier.fillMaxSize()) {
        PageColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Spacer(Modifier.height(12.dp))
            val title = when {
                info.raceMode != null && outcome.newRecord -> S.newRecord
                outcome.stars >= 3 -> S.resultGreat
                outcome.stars == 2 -> S.resultGood
                outcome.stars == 1 -> S.resultDone
                else -> S.resultTry
            }
            GameText(title.str(), style = MaterialTheme.typography.displayMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Text(
                (info.skill?.title ?: info.raceMode?.title)?.str().orEmpty(),
                style = MaterialTheme.typography.titleLarge,
                color = K.Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.Bottom) {
                repeat(3) { index ->
                    val big = index == 1
                    StarGlyph(
                        filled = index < outcome.stars,
                        modifier = Modifier
                            .size(if (big) 110.dp else 86.dp)
                            .offset(y = if (big) (-10).dp else 0.dp)
                            .scale(scales[index].value),
                    )
                }
            }

            GameText(
                if (info.raceMode != null) S.raceScore(outcome.firstTry).str() else S.score(outcome.firstTry, outcome.total).str(),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            if (info.raceMode != null) {
                val best = maxOf(outcome.previousBest, outcome.firstTry)
                Text(S.record(best).str(), style = MaterialTheme.typography.titleMedium, color = K.Gold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }

            outcome.newCards.lastOrNull()?.let { card ->
                PressSurface(
                    onClick = { vm.open(Screen.Collection) },
                    modifier = Modifier.fillMaxWidth(),
                    face = K.Cards,
                    edge = K.CardsDeep,
                    top = K.CardsTop,
                    depth = 6.dp,
                    contentAlignment = Alignment.CenterStart,
                    contentPadding = PaddingValues(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        SpaceCardArt(card.art, Modifier.size(76.dp))
                        Column(Modifier.weight(1f)) {
                            GameText(S.newCard.str(), style = MaterialTheme.typography.titleMedium)
                            GameText(card.title.str(), style = MaterialTheme.typography.headlineSmall)
                        }
                        GameText(S.seeCard.str(), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            outcome.newRank?.let { rank ->
                Badge(S.newRank(rank.title.str()).str(), K.Gold)
            }
            if (outcome.goalReachedNow) {
                val streak = outcome.profile.currentStreak(vm.today)
                Badge(S.goalReached.str() + if (streak > 1) "  🔥 " + S.streak(streak).str() else "", K.Good)
            }

            Spacer(Modifier.height(4.dp))
            val next = info.next
            if (info.raceMode != null) {
                BigButton(S.playAgain.str(), onClick = { vm.startRace(info.raceMode) }, icon = KometIcons.Refresh, modifier = Modifier.fillMaxWidth())
            } else if (next != null && next != info.skill && outcome.stars > 0) {
                BigButton(S.nextLevel.str(), onClick = { vm.startSkill(next) }, icon = KometIcons.Play, modifier = Modifier.fillMaxWidth())
                BigButton(S.playAgain.str(), onClick = { info.skill?.let(vm::startSkill) }, face = K.SurfaceHigh, edge = K.SurfaceLow, textColor = K.Text, icon = KometIcons.Refresh, modifier = Modifier.fillMaxWidth())
            } else {
                BigButton(S.playAgain.str(), onClick = { info.skill?.let(vm::startSkill) }, icon = KometIcons.Refresh, modifier = Modifier.fillMaxWidth())
            }
            BigButton(S.home.str(), onClick = { vm.goHome() }, face = K.SurfaceHigh, edge = K.SurfaceLow, textColor = K.Text, icon = KometIcons.Home, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
        }
        if (outcome.stars >= 2 || outcome.newRecord) ConfettiBurst(info, Modifier.fillMaxSize())
    }
}

@Composable
private fun Badge(text: String, color: Color) {
    Panel(Modifier.fillMaxWidth(), color = color.copy(alpha = 0.16f), padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                Modifier
                    .size(34.dp)
                    .background(color, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(KometIcons.Star, contentDescription = null, tint = K.Ink, modifier = Modifier.size(20.dp))
            }
            GameText(text, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(end = 4.dp))
        }
    }
}
