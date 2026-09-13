package app.komet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.komet.domain.Curriculum
import app.komet.domain.Progression
import app.komet.domain.SpaceCards
import app.komet.domain.Subject
import app.komet.ui.KometViewModel
import app.komet.ui.S
import app.komet.ui.Screen
import app.komet.ui.components.Avatar
import app.komet.ui.components.BigButton
import app.komet.ui.components.KometIcons
import app.komet.ui.components.PageColumn
import app.komet.ui.components.Panel
import app.komet.ui.components.Pill
import app.komet.ui.components.PlanetArt
import app.komet.ui.components.PressSurface
import app.komet.ui.components.ProgressTrack
import app.komet.ui.components.RocketArt
import app.komet.ui.components.RoundIconButton
import app.komet.ui.components.SpaceCardArt
import app.komet.ui.components.StarGlyph
import app.komet.ui.components.str
import app.komet.ui.components.subjectColors
import app.komet.ui.theme.K

@Composable
fun HomeScreen(vm: KometViewModel) {
    val profile = vm.profile ?: return
    val today = vm.today
    val recommended = remember(profile) { Progression.recommended(profile) }
    val chapter = Curriculum.chapterOf(recommended)
    val (accent, accentDeep) = subjectColors(recommended.subject)
    val rank = Progression.rank(profile.totalStars)
    val nextRank = Progression.nextRank(profile.totalStars)
    val streak = profile.currentStreak(today)
    val roundsToday = profile.today(today).rounds
    val goal = vm.settings.dailyGoal
    val unlockedCards = SpaceCards.unlockedCount(profile.totalStars)
    var picker by remember { mutableStateOf(false) }

    val switchLabel = S.switchProfile.str()

    PageColumn {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier
                    .semantics { contentDescription = switchLabel }
                    .clickable(role = Role.Button, enabled = vm.state.profiles.size > 1) { picker = true },
            ) {
                Avatar(profile.avatar, size = 60.dp)
            }
            Column(Modifier.weight(1f)) {
                Text(S.hello(profile.name).str(), style = MaterialTheme.typography.headlineMedium, color = K.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(rank.title.str(), style = MaterialTheme.typography.titleMedium, color = K.Gold)
            }
            // A waiting update is for the parents, so it shows as a quiet dot on their door only.
            Box {
                RoundIconButton(KometIcons.Lock, S.parents.str(), onClick = { vm.open(Screen.ParentGate) }, size = 48.dp, tint = K.Muted)
                if (vm.updater.state.release != null) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .size(14.dp)
                            .background(K.Gold, CircleShape),
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Pill(profile.totalStars.toString(), star = true)
            if (streak > 0) Pill(S.streak(streak).str(), icon = KometIcons.Flame, iconTint = Color(0xFFFF8A3D))
            Spacer(Modifier.weight(1f))
        }
        if (nextRank != null) {
            val span = (nextRank.minStars - rank.minStars).coerceAtLeast(1)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(S.nextRank(nextRank.title.str()).str(), style = MaterialTheme.typography.labelLarge, color = K.Muted, modifier = Modifier.weight(1f))
                    Text("${profile.totalStars} / ${nextRank.minStars}", style = MaterialTheme.typography.labelLarge, color = K.Muted)
                    StarGlyph(filled = true, modifier = Modifier.padding(start = 4.dp).size(16.dp))
                }
                ProgressTrack((profile.totalStars - rank.minStars) / span.toFloat(), Modifier.fillMaxWidth(), color = K.Gold, track = K.SurfaceHigh, height = 10.dp)
            }
        }

        // Neste oppdrag
        Panel(Modifier.fillMaxWidth(), color = K.Surface) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                PlanetArt(chapter.look, Modifier.size(96.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(S.nextMission.str().uppercase(), style = MaterialTheme.typography.labelMedium, color = accent)
                    Text(recommended.title.str(), style = MaterialTheme.typography.headlineMedium, color = K.Text)
                    Text(
                        (if (recommended.subject == Subject.MATH) S.math else S.reading).str() + " · " + chapter.title.str(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = K.Muted,
                    )
                }
            }
            BigButton(
                text = S.start.str(),
                onClick = { vm.startSkill(recommended) },
                icon = KometIcons.Play,
                face = accent,
                edge = accentDeep,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Dagens oppdrag
        Panel(Modifier.fillMaxWidth(), color = K.SurfaceLow.copy(alpha = 0.8f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(S.dailyMission.str(), style = MaterialTheme.typography.titleLarge, color = K.Text)
                    Text(
                        if (roundsToday >= goal) S.goalDone.str() else S.roundsOf(roundsToday.coerceAtMost(goal), goal).str(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (roundsToday >= goal) K.Good else K.Muted,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(goal) { index -> StarGlyph(filled = index < roundsToday, modifier = Modifier.size(30.dp)) }
                }
            }
            ProgressTrack(roundsToday / goal.toFloat(), Modifier.fillMaxWidth(), color = if (roundsToday >= goal) K.Good else K.Gold, track = K.SurfaceHigh)
        }

        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val wide = maxWidth >= 600.dp
            val tiles: List<@Composable (Modifier) -> Unit> = listOf(
                { m ->
                    SubjectTile(
                        title = S.math.str(),
                        detail = starsText(vm, Subject.MATH),
                        look = Curriculum.chapters(Subject.MATH).first().look,
                        face = K.Math,
                        edge = K.MathDeep,
                        onClick = { vm.open(Screen.World(Subject.MATH)) },
                        modifier = m,
                    )
                },
                { m ->
                    SubjectTile(
                        title = S.reading.str(),
                        detail = starsText(vm, Subject.READING),
                        look = Curriculum.chapters(Subject.READING).first().look,
                        face = K.Reading,
                        edge = K.ReadingDeep,
                        onClick = { vm.open(Screen.World(Subject.READING)) },
                        modifier = m,
                    )
                },
                { m ->
                    FeatureTile(
                        title = S.race.str(),
                        detail = S.raceDetail.str(),
                        face = K.Race,
                        edge = K.RaceDeep,
                        onClick = { vm.open(Screen.RaceMenu) },
                        modifier = m,
                    ) { RocketArt(Modifier.size(46.dp, 72.dp), body = Color.White, accent = K.Gold) }
                },
                { m ->
                    FeatureTile(
                        title = S.cards.str(),
                        detail = S.cardsOf(unlockedCards, SpaceCards.all.size).str(),
                        face = K.Cards,
                        edge = K.CardsDeep,
                        badge = if (unlockedCards > profile.seenCards) S.newBadge.str() else null,
                        onClick = { vm.open(Screen.Collection) },
                        modifier = m,
                    ) {
                        val card = SpaceCards.all.getOrNull(unlockedCards - 1)
                        if (card != null) {
                            SpaceCardArt(card.art, Modifier.size(72.dp))
                        } else {
                            // Nothing collected yet: a sealed card says «there is something to win».
                            Box(
                                Modifier
                                    .size(52.dp, 68.dp)
                                    .background(K.CardsDeep, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("?", color = K.Text, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                },
            )
            val perRow = if (wide) 4 else 2
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                tiles.chunked(perRow).forEach { row ->
                    Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { tile -> tile(Modifier.weight(1f).fillMaxHeight()) }
                    }
                }
            }
        }
        Spacer(Modifier.heightIn(min = 8.dp))
    }

    if (picker) {
        AlertDialog(
            onDismissRequest = { picker = false },
            containerColor = K.SurfaceHigh,
            title = { Text(S.whoPlays.str(), color = K.Text, style = MaterialTheme.typography.headlineSmall) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    vm.state.profiles.forEach { other ->
                        PressSurface(
                            onClick = {
                                vm.switchProfile(other.id)
                                picker = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            face = if (other.id == profile.id) K.Surface else K.SurfaceLow,
                            edge = K.SpaceTop,
                            contentAlignment = Alignment.CenterStart,
                            contentPadding = PaddingValues(12.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Avatar(other.avatar, size = 48.dp)
                                Text(other.name, style = MaterialTheme.typography.titleLarge, color = K.Text, modifier = Modifier.weight(1f))
                                Pill(other.totalStars.toString(), star = true)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { picker = false }) { Text(S.close.str(), color = K.Gold) }
            },
        )
    }
}

@Composable
private fun starsText(vm: KometViewModel, subject: Subject): String {
    val profile = vm.profile ?: return ""
    val skills = Curriculum.skills(subject)
    return "${Progression.earnedStars(profile, skills)} / ${skills.size * 3} ⭐"
}

@Composable
private fun SubjectTile(
    title: String,
    detail: String,
    look: app.komet.domain.PlanetLook,
    face: Color,
    edge: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PressSurface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 150.dp),
        face = face,
        edge = edge,
        shape = RoundedCornerShape(26.dp),
        depth = 6.dp,
        contentAlignment = Alignment.TopStart,
        contentPadding = PaddingValues(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                PlanetArt(look, Modifier.size(64.dp), glow = false)
            }
            Text(title, style = MaterialTheme.typography.headlineMedium, color = K.Ink, fontWeight = FontWeight.Black)
            Text(detail, style = MaterialTheme.typography.titleSmall, color = K.Ink.copy(alpha = 0.75f))
        }
    }
}

@Composable
private fun FeatureTile(
    title: String,
    detail: String,
    face: Color,
    edge: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
    art: @Composable () -> Unit,
) {
    PressSurface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 150.dp),
        face = face,
        edge = edge,
        shape = RoundedCornerShape(26.dp),
        depth = 6.dp,
        contentAlignment = Alignment.TopStart,
        contentPadding = PaddingValues(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                if (badge != null) {
                    Text(
                        badge,
                        style = MaterialTheme.typography.labelMedium,
                        color = K.Ink,
                        modifier = Modifier
                            .background(K.Gold, RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                Box(Modifier.heightIn(min = 64.dp), contentAlignment = Alignment.Center) { art() }
            }
            Text(title, style = MaterialTheme.typography.headlineMedium, color = K.Ink, fontWeight = FontWeight.Black)
            Text(detail, style = MaterialTheme.typography.titleSmall, color = K.Ink.copy(alpha = 0.75f))
        }
    }
}
