package app.komet.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.komet.domain.Curriculum
import app.komet.domain.Progression
import app.komet.domain.Skill
import app.komet.domain.Subject
import app.komet.domain.Topic
import app.komet.domain.Topics
import app.komet.ui.KometViewModel
import app.komet.ui.S
import app.komet.ui.components.GameText
import app.komet.ui.components.KometIcons
import app.komet.ui.components.MaxContentWidth
import app.komet.ui.components.Pill
import app.komet.ui.components.PressSurface
import app.komet.ui.components.RoundIconButton
import app.komet.ui.components.ScreenTopBar
import app.komet.ui.components.StarRow
import app.komet.ui.components.StarGlyph
import app.komet.ui.components.fixedSp
import app.komet.ui.components.gloss
import app.komet.ui.components.str
import app.komet.ui.components.subjectTone
import app.komet.ui.theme.K
import app.komet.ui.theme.ReadingFont

/** Utforsk: the child's favourites first, then every kind of task, whatever grade it belongs to. */
@Composable
fun ExploreScreen(vm: KometViewModel) {
    val profile = vm.profile ?: return
    val favorites = profile.favorites.mapNotNull(Curriculum::skill)
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        ScreenTopBar(
            title = S.explore.str(),
            onBack = { vm.back() },
            modifier = Modifier
                .widthIn(max = MaxContentWidth)
                .padding(horizontal = 20.dp, vertical = 8.dp),
        )
        LazyColumn(
            modifier = Modifier
                .widthIn(max = MaxContentWidth)
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (favorites.isNotEmpty()) {
                item(key = "favorites") { SectionHeading(S.favorites.str()) }
                items(favorites, key = { "fav-${it.id}" }) { skill ->
                    LevelRow(
                        skill = skill,
                        stars = profile.stars(skill.id),
                        favorite = true,
                        onPlay = { vm.startSkill(skill) },
                        onFavorite = { vm.toggleFavorite(skill.id) },
                    )
                }
            }
            Subject.entries.filter { subject -> Topics.all.any { it.subject == subject } }.forEach { subject ->
                item(key = "heading-$subject") {
                    SectionHeading(
                        S.subject(subject).str(),
                        Modifier.padding(top = 14.dp),
                        color = subjectTone(subject).accent,
                    )
                }
                item(key = "topics-$subject") {
                    TopicGrid(Topics.all.filter { it.subject == subject }) { topic, tileModifier ->
                        val skills = Topics.skills(topic)
                        TopicTile(
                            topic = topic,
                            detail = S.levelCount(skills.size).str(),
                            stars = "${Progression.earnedStars(profile, skills)} / ${skills.size * 3}",
                            onClick = { vm.open(app.komet.ui.Screen.Topic(topic)) },
                            modifier = tileModifier,
                        )
                    }
                }
            }
        }
    }
}

/** Every level of one kind of task, easiest first, all playable whatever the child's grade. */
@Composable
fun TopicScreen(vm: KometViewModel, topic: Topic) {
    val profile = vm.profile ?: return
    val skills = Topics.skills(topic)
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        ScreenTopBar(
            title = topic.title.str(),
            onBack = { vm.back() },
            modifier = Modifier
                .widthIn(max = MaxContentWidth)
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Pill("${Progression.earnedStars(profile, skills)} / ${skills.size * 3}", star = true)
        }
        LazyColumn(
            modifier = Modifier
                .widthIn(max = MaxContentWidth)
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (topic == Topic.CLOCK) {
                item { ClockWorkshopButton(onClick = { vm.open(app.komet.ui.Screen.ClockWorkshop) }, modifier = Modifier.fillMaxWidth()) }
            }
            items(skills, key = { it.id }) { skill ->
                LevelRow(
                    skill = skill,
                    stars = profile.stars(skill.id),
                    favorite = skill.id in profile.favorites,
                    onPlay = { vm.startSkill(skill) },
                    onFavorite = { vm.toggleFavorite(skill.id) },
                )
            }
        }
    }
}

@Composable
private fun SectionHeading(text: String, modifier: Modifier = Modifier, color: Color = K.Text) {
    Text(
        text,
        color = color,
        style = MaterialTheme.typography.headlineSmall,
        modifier = modifier.semantics { heading() },
    )
}

@Composable
private fun TopicGrid(topics: List<Topic>, tile: @Composable (Topic, Modifier) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val perRow = when {
            maxWidth >= 760.dp -> 4
            maxWidth >= 560.dp -> 3
            maxWidth >= 285.dp -> 2
            else -> 1
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            topics.chunked(perRow).forEach { row ->
                Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { topic -> tile(topic, Modifier.weight(1f).fillMaxHeight()) }
                    repeat(perRow - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun TopicTile(topic: Topic, detail: String, stars: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val tone = subjectTone(topic.subject)
    val face = lerp(K.Surface, tone.edge, 0.19f)
    val iconShape = RoundedCornerShape(14.dp)
    PressSurface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 138.dp),
        face = face,
        edge = K.SpaceTop,
        top = lerp(K.SurfaceHigh, tone.face, 0.18f),
        contentAlignment = Alignment.TopStart,
        contentPadding = PaddingValues(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Box(
                Modifier.size(46.dp)
                    .gloss(lerp(tone.edge, K.SurfaceLow, 0.35f), iconShape, top = tone.face)
                    .border(1.dp, tone.accent.copy(alpha = 0.52f), iconShape),
                contentAlignment = Alignment.Center,
            ) {
                if (isPictograph(topic.icon)) {
                    Text(topic.icon, fontSize = fixedSp(23.dp))
                } else {
                    GameText(
                        topic.icon,
                        style = MaterialTheme.typography.headlineSmall.copy(fontFamily = if (topic.subject == Subject.READING) ReadingFont else null),
                        fontSize = fixedSp(if (topic.icon.length > 2) 18.dp else 24.dp),
                        color = tone.accent,
                        maxLines = 1,
                    )
                }
            }
            Text(topic.title.str(), style = MaterialTheme.typography.titleMedium, color = K.Text)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = K.Muted)
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                StarGlyph(true, Modifier.size(13.dp))
                Text(stars, style = MaterialTheme.typography.labelMedium, color = K.GoldTop)
            }
        }
    }
}

/** One level: its medallion, name, grade and stars, with a heart to keep it among the favourites. */
@Composable
fun LevelRow(skill: Skill, stars: Int, favorite: Boolean, onPlay: () -> Unit, onFavorite: () -> Unit) {
    val tone = subjectTone(skill.subject)
    PressSurface(
        onClick = onPlay,
        modifier = Modifier.fillMaxWidth(),
        face = K.Surface,
        edge = K.SurfaceLow,
        shape = RoundedCornerShape(24.dp),
        contentAlignment = Alignment.CenterStart,
        contentPadding = PaddingValues(start = 12.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LevelMedallion(skill, 60.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                GameText(skill.title.str(), style = MaterialTheme.typography.titleLarge, maxLines = 2)
                Text(
                    skill.detail.str(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = K.Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        S.gradeShort[skill.grade.coerceIn(0, S.gradeShort.lastIndex)].str(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = tone.top,
                    )
                    StarRow(stars, size = 16.dp)
                }
            }
            RoundIconButton(
                icon = if (favorite) KometIcons.Heart else KometIcons.HeartOutline,
                description = (if (favorite) S.removeFavorite else S.addFavorite).str(),
                onClick = onFavorite,
                size = 52.dp,
                face = K.SurfaceLow,
                edge = K.SpaceTop,
                tint = if (favorite) K.RaceTop else K.Muted,
            )
        }
    }
}

/** The level's symbol on a glossy coin in its subject colour. */
@Composable
fun LevelMedallion(skill: Skill, size: Dp) {
    val tone = subjectTone(skill.subject)
    Box(
        Modifier
            .size(size)
            .border(2.dp, K.Outline, CircleShape)
            .gloss(tone.face, CircleShape, top = tone.top),
        contentAlignment = Alignment.Center,
    ) {
        LevelSymbol(skill, size)
    }
}

/** The level's symbol sized for a coin of [size]: game lettering, or the sign itself for pictographs. */
@Composable
fun LevelSymbol(skill: Skill, size: Dp) {
    val symbol = skill.symbol
    val fontSize = fixedSp(size * (if (symbol.length <= 2) 0.4f else if (symbol.length <= 3) 0.3f else 0.22f))
    if (isPictograph(symbol)) {
        // Emoji and signs have their own colours; a text outline would only smudge them.
        Text(symbol, fontSize = fontSize, color = Color.White, maxLines = 1)
    } else {
        GameText(
            symbol,
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = if (skill.subject == Subject.READING) ReadingFont else null),
            fontSize = fontSize,
            maxLines = 1,
        )
    }
}

/** Emoji and pictographic signs carry their own colours; game lettering would only smudge them. */
private fun isPictograph(text: String): Boolean = text.any { char ->
    val code = char.code
    // Mathematical operators (− × ÷) are letters to us; arrows, dingbats and emoji are pictures.
    Character.isSurrogate(char) || (code in 0x2190..0x2BFF && code !in 0x2200..0x22FF)
}
