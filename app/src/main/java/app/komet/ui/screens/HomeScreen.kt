package app.komet.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.komet.domain.Curriculum
import app.komet.domain.Progression
import app.komet.domain.SpaceCards
import app.komet.domain.Subject
import app.komet.ui.KometViewModel
import app.komet.ui.S
import app.komet.ui.Screen
import app.komet.ui.components.Avatar
import app.komet.ui.components.BigButton
import app.komet.ui.components.GameText
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
import app.komet.ui.components.Tone
import app.komet.ui.components.Tones
import app.komet.ui.components.str
import app.komet.ui.components.subjectTone
import app.komet.ui.theme.K
import app.komet.ui.theme.LocalMotion

@Composable
fun HomeScreen(vm: KometViewModel) {
    val profile = vm.profile ?: return
    val today = vm.today
    val recommended = remember(profile) { Progression.recommended(profile) }
    val resume = remember(profile) { Progression.resumable(profile) }
    val chapter = Curriculum.chapterOf(recommended)
    val tone = subjectTone(recommended.subject)
    val rank = Progression.rank(profile.totalStars)
    val rankNumber = Progression.ranks.indexOf(rank) + 1
    val nextRank = Progression.nextRank(profile.totalStars)
    val streak = profile.currentStreak(today)
    val roundsToday = profile.today(today).rounds
    val goal = vm.settings.dailyGoal
    val unlockedCards = SpaceCards.unlockedCount(profile.totalStars)
    var picker by remember { mutableStateOf(false) }

    val switchLabel = S.switchProfile.str()

    PageColumn(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                Modifier
                    .semantics { contentDescription = switchLabel }
                    .clickable(role = Role.Button, enabled = vm.state.profiles.size > 1) { picker = true },
            ) {
                Avatar(profile.avatar, size = 68.dp)
                RankBadge(rankNumber, Modifier.align(Alignment.BottomEnd).offset(x = 6.dp, y = 6.dp))
            }
            Column(Modifier.weight(1f)) {
                GameText(profile.name, style = MaterialTheme.typography.headlineMedium, maxLines = 1)
                GameText(rank.title.str(), style = MaterialTheme.typography.titleMedium, color = K.Gold, maxLines = 1)
            }
            // A waiting update is for the parents, so it shows as a quiet dot on their door only.
            Box {
                RoundIconButton(KometIcons.Lock, S.parents.str(), onClick = { vm.open(Screen.ParentGate) }, size = 48.dp, tint = K.Muted)
                if (vm.updater.state.release != null) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .size(14.dp)
                            .border(2.dp, K.Outline, CircleShape)
                            .background(K.Gold, CircleShape),
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Pill(profile.totalStars.toString(), star = true)
                if (streak > 0) Pill(S.streak(streak).str(), icon = KometIcons.Flame, iconTint = Color(0xFFFF8A3D))
                Spacer(Modifier.weight(1f))
                if (nextRank != null) {
                    Text(S.nextRank(nextRank.title.str()).str(), style = MaterialTheme.typography.labelLarge, color = K.Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            if (nextRank != null) {
                val span = (nextRank.minStars - rank.minStars).coerceAtLeast(1)
                ProgressTrack((profile.totalStars - rank.minStars) / span.toFloat(), Modifier.fillMaxWidth(), color = K.Gold, track = K.SurfaceLow, height = 14.dp)
            }
        }

        if (resume != null) {
            // A round that was left comes first: finishing it is what earns the stars.
            val (skill, active) = resume
            MissionCard(
                label = S.resumeLabel.str(),
                title = skill.title.str(),
                detail = S.resumeDetail(active.done, active.total).str(),
                tone = subjectTone(skill.subject),
                button = S.resume.str(),
                progress = active.done / active.total.toFloat(),
                onStart = { vm.resumeRound() },
            ) {
                PlanetArt(Curriculum.chapterOf(skill).look, Modifier.size(150.dp))
            }
        } else {
            MissionCard(
                label = S.nextMission.str(),
                title = recommended.title.str(),
                detail = (if (recommended.subject == Subject.MATH) S.math else S.reading).str() + " · " + chapter.title.str(),
                tone = tone,
                button = S.start.str(),
                onStart = { vm.startSkill(recommended) },
            ) {
                PlanetArt(chapter.look, Modifier.size(150.dp))
            }
        }

        Panel(Modifier.fillMaxWidth(), color = K.Surface) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    GameText(S.dailyMission.str(), style = MaterialTheme.typography.titleLarge)
                    Text(
                        if (roundsToday >= goal) S.goalDone.str() else S.roundsOf(roundsToday.coerceAtMost(goal), goal).str(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (roundsToday >= goal) K.GoodTop else K.Muted,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(goal) { index -> StarGlyph(filled = index < roundsToday, modifier = Modifier.size(38.dp)) }
                }
            }
            ProgressTrack(roundsToday / goal.toFloat(), Modifier.fillMaxWidth(), color = if (roundsToday >= goal) K.Good else K.Gold, track = K.SurfaceLow, height = 14.dp)
        }

        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val wide = maxWidth >= 600.dp
            val tiles: List<@Composable (Modifier) -> Unit> = listOf(
                { m ->
                    GameTile(
                        title = S.math.str(),
                        stars = starsText(vm, Subject.MATH),
                        tone = Tones.Math,
                        onClick = { vm.open(Screen.World(Subject.MATH)) },
                        modifier = m,
                    ) { PlanetArt(Curriculum.chapters(Subject.MATH).first().look, Modifier.size(80.dp), glow = false) }
                },
                { m ->
                    GameTile(
                        title = S.reading.str(),
                        stars = starsText(vm, Subject.READING),
                        tone = Tones.Reading,
                        onClick = { vm.open(Screen.World(Subject.READING)) },
                        modifier = m,
                    ) { PlanetArt(Curriculum.chapters(Subject.READING).first().look, Modifier.size(80.dp), glow = false) }
                },
                { m ->
                    GameTile(
                        title = S.race.str(),
                        detail = S.raceDetail.str(),
                        tone = Tones.Race,
                        onClick = { vm.open(Screen.RaceMenu) },
                        modifier = m,
                    ) { FloatingRocket() }
                },
                { m ->
                    GameTile(
                        title = S.cards.str(),
                        detail = S.cardsOf(unlockedCards, SpaceCards.all.size).str(),
                        tone = Tones.Cards,
                        badge = if (unlockedCards > profile.seenCards) S.newBadge.str() else null,
                        onClick = { vm.open(Screen.Collection) },
                        modifier = m,
                    ) {
                        val card = SpaceCards.all.getOrNull(unlockedCards - 1)
                        if (card != null) {
                            SpaceCardArt(card.art, Modifier.size(80.dp), emojiSize = 52.dp)
                        } else {
                            // Nothing collected yet: a sealed card says «there is something to win».
                            Box(
                                Modifier
                                    .size(56.dp, 74.dp)
                                    .border(2.dp, K.Outline, RoundedCornerShape(10.dp))
                                    .background(Brush.verticalGradient(listOf(K.CardsTop, K.CardsDeep)), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                GameText("?", style = MaterialTheme.typography.headlineLarge)
                            }
                        }
                    }
                },
            )
            val perRow = if (wide) 4 else 2
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                tiles.chunked(perRow).forEach { row ->
                    Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
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
    return "${Progression.earnedStars(profile, skills)} / ${skills.size * 3}"
}

/** A gold medal with the rank number, pinned to the avatar. */
@Composable
private fun RankBadge(number: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(30.dp)
            .border(2.dp, K.Outline, CircleShape)
            .padding(2.dp)
            .background(Brush.verticalGradient(listOf(K.GoldTop, K.Gold, K.GoldDeep)), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        GameText(number.toString(), style = MaterialTheme.typography.labelLarge, fontSize = 15.sp)
    }
}

/**
 * The next mission as a hero card: the planet breaks out of the frame, the world colour lights the
 * card from behind, and one big button starts it.
 */
@Composable
private fun MissionCard(
    label: String,
    title: String,
    detail: String,
    tone: Tone,
    button: String,
    onStart: () -> Unit,
    progress: Float? = null,
    art: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(30.dp)
    Box(Modifier.fillMaxWidth().padding(top = 26.dp)) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(Brush.verticalGradient(listOf(lerp(tone.edge, K.SpaceTop, 0.15f), lerp(tone.edge, K.SpaceTop, 0.62f))))
                .drawBehind {
                    drawCircle(
                        Brush.radialGradient(listOf(tone.face.copy(alpha = 0.55f), Color.Transparent), center = Offset(size.width * 0.86f, 0f), radius = size.width * 0.6f),
                        radius = size.width * 0.6f,
                        center = Offset(size.width * 0.86f, 0f),
                    )
                }
                .border(2.dp, Brush.verticalGradient(listOf(tone.top.copy(alpha = 0.55f), tone.edge.copy(alpha = 0.3f))), shape)
                .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                label.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = tone.top,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 120.dp),
            )
            GameText(title, style = MaterialTheme.typography.displaySmall, modifier = Modifier.padding(end = 110.dp), maxLines = 2)
            Text(detail, style = MaterialTheme.typography.titleSmall, color = K.Muted)
            if (progress != null) {
                ProgressTrack(progress, Modifier.fillMaxWidth().padding(top = 8.dp, end = 110.dp), color = tone.top, track = K.SurfaceLow, height = 12.dp)
            }
            Spacer(Modifier.height(12.dp))
            BigButton(
                text = button,
                onClick = onStart,
                icon = KometIcons.Play,
                face = K.Gold,
                edge = K.GoldDeep,
                top = K.GoldTop,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Box(Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-30).dp)) { art() }
    }
}

/** A world or feature on Heim: a glossy slab in its own colour with the art breaking out of the corner. */
@Composable
private fun GameTile(
    title: String,
    tone: Tone,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    detail: String? = null,
    stars: String? = null,
    badge: String? = null,
    art: @Composable () -> Unit,
) {
    PressSurface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 164.dp),
        face = tone.face,
        edge = tone.edge,
        // Deep colour with only a hint of light on top: rich, not candy.
        top = lerp(tone.face, Color.White, 0.1f),
        shape = RoundedCornerShape(28.dp),
        depth = 7.dp,
        contentAlignment = Alignment.TopStart,
        contentPadding = PaddingValues(16.dp),
    ) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                if (badge != null) {
                    GameText(
                        badge,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .border(2.dp, K.Outline, RoundedCornerShape(50))
                            .background(Brush.verticalGradient(listOf(K.RaceTop, K.Race)), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                Box(Modifier.heightIn(min = 72.dp), contentAlignment = Alignment.Center) { art() }
            }
            GameText(title, style = MaterialTheme.typography.headlineMedium, maxLines = 1)
            if (stars != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    StarGlyph(filled = true, modifier = Modifier.size(20.dp))
                    GameText(stars, style = MaterialTheme.typography.titleSmall)
                }
            }
            if (detail != null) {
                Text(detail, style = MaterialTheme.typography.titleSmall, color = Color.White.copy(alpha = 0.92f), fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** The race tile's rocket bobs gently, as if it is waiting on the launch pad. */
@Composable
private fun FloatingRocket() {
    val motion = LocalMotion.current
    val transition = rememberInfiniteTransition(label = "bob")
    val bob = transition.animateFloat(0f, 1f, infiniteRepeatable(tween(1300), RepeatMode.Reverse), label = "bobbing")
    RocketArt(
        Modifier
            .offset(y = if (motion) (bob.value * -6f).dp else 0.dp)
            .size(50.dp, 78.dp),
        body = Color.White,
        accent = K.Gold,
    )
}
