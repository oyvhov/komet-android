package app.komet.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.komet.audio.Sfx
import app.komet.domain.Chapter
import app.komet.domain.Curriculum
import app.komet.domain.Progression
import app.komet.domain.Skill
import app.komet.domain.Subject
import app.komet.ui.KometViewModel
import app.komet.ui.S
import app.komet.ui.components.KometIcons
import app.komet.ui.components.LocalFeedback
import app.komet.ui.components.MaxContentWidth
import app.komet.ui.components.Pill
import app.komet.ui.components.PlanetArt
import app.komet.ui.components.PressSurface
import app.komet.ui.components.ScreenTopBar
import app.komet.ui.components.StarRow
import app.komet.ui.components.fixedSp
import app.komet.ui.components.shake
import app.komet.ui.components.str
import app.komet.ui.components.subjectColors
import app.komet.ui.components.GameText
import app.komet.ui.components.Tone
import app.komet.ui.components.gloss
import app.komet.ui.components.subjectTone
import androidx.compose.foundation.border
import app.komet.ui.theme.K
import app.komet.ui.theme.LocalMotion
import app.komet.ui.theme.ReadingFont

private val zigzag = listOf(0f, 0.55f, 0f, -0.55f)
private val NodeColumnWidth = 128.dp
private val NodeSize = 84.dp

private sealed interface MapRow {
    data class Header(val chapter: Chapter) : MapRow
    data class Node(val chapter: Chapter, val index: Int) : MapRow
}

@Composable
fun WorldScreen(vm: KometViewModel, subject: Subject) {
    val profile = vm.profile ?: return
    val chapters = Curriculum.chapters(subject)
    val skills = Curriculum.skills(subject)
    val recommended = remember(profile) { Progression.recommended(profile, subject) }
    val weak = remember(profile) { Progression.weakSkills(profile, subject) }
    val (accent, accentDeep) = subjectColors(subject)
    val rows = remember(subject) {
        chapters.flatMap { chapter -> listOf<MapRow>(MapRow.Header(chapter)) + chapter.skills.indices.map { MapRow.Node(chapter, it) } }
    }
    val listState = rememberLazyListState()
    LaunchedEffect(subject) {
        val target = rows.indexOfFirst { it is MapRow.Node && it.chapter.skills[it.index] == recommended }
        if (target > 2) listState.scrollToItem(target - 2)
    }

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        ScreenTopBar(
            title = S.subject(subject).str(),
            onBack = { vm.back() },
            modifier = Modifier
                .widthIn(max = MaxContentWidth)
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Pill("${Progression.earnedStars(profile, skills)} / ${skills.size * 3}", star = true)
        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .widthIn(max = MaxContentWidth)
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 40.dp),
        ) {
            if (weak.isNotEmpty()) {
                item(key = "review") {
                    val names = weak.map { it.title.str() }.joinToString(", ")
                    ReviewCard(names, subjectTone(subject)) { vm.startReview(subject) }
                }
            }
            items(rows.size, key = { index ->
                when (val row = rows[index]) {
                    is MapRow.Header -> "h-${row.chapter.id}"
                    is MapRow.Node -> row.chapter.skills[row.index].id
                }
            }) { index ->
                when (val row = rows[index]) {
                    is MapRow.Header -> ChapterHeader(row.chapter, profile.let { p -> Progression.completed(p, row.chapter.skills) })
                    is MapRow.Node -> {
                        val skill = row.chapter.skills[row.index]
                        NodeRow(
                            skill = skill,
                            index = row.index,
                            count = row.chapter.skills.size,
                            stars = profile.stars(skill.id),
                            unlocked = Progression.isUnlocked(profile, row.chapter, row.index),
                            recommended = skill == recommended,
                            color = accent,
                            deep = accentDeep,
                            onPlay = { vm.startSkill(skill) },
                        )
                    }
                }
            }
        }
    }
}

/** «Repetisjon»: the levels that are hard right now, mixed into one round. */
@Composable
private fun ReviewCard(names: String, tone: Tone, onStart: () -> Unit) {
    PressSurface(
        onClick = onStart,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        face = K.Surface,
        edge = K.SurfaceLow,
        shape = RoundedCornerShape(26.dp),
        contentAlignment = Alignment.CenterStart,
        contentPadding = PaddingValues(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                Modifier
                    .size(58.dp)
                    .border(2.dp, K.Outline, CircleShape)
                    .gloss(tone.face, CircleShape, top = tone.top),
                contentAlignment = Alignment.Center,
            ) {
                Icon(KometIcons.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
            }
            Column(Modifier.weight(1f)) {
                GameText(S.review.str(), style = MaterialTheme.typography.titleLarge)
                Text(S.reviewDetail(names).str(), style = MaterialTheme.typography.bodyMedium, color = K.Muted, maxLines = 2)
            }
            GameText(S.reviewStart.str(), style = MaterialTheme.typography.titleMedium, color = tone.top)
        }
    }
}

@Composable
private fun ChapterHeader(chapter: Chapter, completed: Int) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 18.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PlanetArt(chapter.look, Modifier.size(78.dp))
        Column(Modifier.weight(1f)) {
            Text(chapter.title.str(), style = MaterialTheme.typography.headlineMedium, color = K.Text)
            Text(S.levels(completed, chapter.skills.size).str(), style = MaterialTheme.typography.bodyMedium, color = K.Muted)
        }
    }
}

@Composable
private fun NodeRow(
    skill: Skill,
    index: Int,
    count: Int,
    stars: Int,
    unlocked: Boolean,
    recommended: Boolean,
    color: Color,
    deep: Color,
    onPlay: () -> Unit,
) {
    val feedback = LocalFeedback.current
    var lockedShake by remember { mutableIntStateOf(0) }
    val bias = zigzag[index % zigzag.size]
    val previousBias = if (index > 0) zigzag[(index - 1) % zigzag.size] else null
    val nextBias = if (index < count - 1) zigzag[(index + 1) % zigzag.size] else null
    val motion = LocalMotion.current

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val width = maxWidth
        fun xFor(b: Float) = ((width - NodeColumnWidth) / 2) * (1 + b) + NodeColumnWidth / 2
        val lineColor = if (unlocked) color.copy(alpha = 0.55f) else K.Line
        Canvas(Modifier.matchParentSize()) {
            val centerY = (14.dp + NodeSize / 2).toPx()
            val x = xFor(bias).toPx()
            val dots = PathEffect.dashPathEffect(floatArrayOf(1f, 18f))
            if (previousBias != null) {
                val px = ((xFor(previousBias) + xFor(bias)) / 2).toPx()
                drawLine(lineColor, Offset(px, 0f), Offset(x, centerY), strokeWidth = 7.dp.toPx(), cap = StrokeCap.Round, pathEffect = dots)
            }
            if (nextBias != null) {
                val nx = ((xFor(nextBias) + xFor(bias)) / 2).toPx()
                drawLine(lineColor, Offset(x, centerY), Offset(nx, size.height), strokeWidth = 7.dp.toPx(), cap = StrokeCap.Round, pathEffect = dots)
            }
        }
        Column(
            modifier = Modifier
                .width(NodeColumnWidth)
                .align(BiasAlignment(bias, -1f))
                .padding(top = 14.dp, bottom = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(NodeSize + 22.dp)) {
                if (recommended) {
                    val transition = rememberInfiniteTransition(label = "next")
                    val pulse = transition.animateFloat(0.92f, 1.08f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "nextPulse")
                    Canvas(
                        Modifier
                            .size(NodeSize + 20.dp)
                            .scale(if (motion) pulse.value else 1f),
                    ) {
                        drawCircle(K.Gold, style = Stroke(4.dp.toPx()))
                    }
                }
                PressSurface(
                    onClick = {
                        if (unlocked) {
                            onPlay()
                        } else {
                            lockedShake++
                            feedback.sfx(Sfx.WRONG)
                        }
                    },
                    modifier = Modifier
                        .size(NodeSize)
                        .shake(lockedShake, true)
                        .semantics { contentDescription = skill.title.nn },
                    face = if (unlocked) color else K.SurfaceHigh,
                    edge = if (unlocked) deep else K.SurfaceLow,
                    shape = CircleShape,
                    depth = 6.dp,
                    tapSound = unlocked,
                    contentPadding = PaddingValues(0.dp),
                ) {
                    if (unlocked) {
                        val size = fixedSp(if (skill.symbol.length <= 2) 30.dp else if (skill.symbol.length <= 3) 23.dp else 17.dp)
                        if (skill.symbol.any { Character.isSurrogate(it) || (it.code in 0x2190..0x2BFF && it.code !in 0x2200..0x22FF) }) {
                            Text(skill.symbol, fontSize = size, maxLines = 1)
                        } else {
                            GameText(
                                skill.symbol,
                                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = if (skill.subject == Subject.READING) ReadingFont else null),
                                fontSize = size,
                                maxLines = 1,
                            )
                        }
                    } else {
                        Icon(KometIcons.Lock, contentDescription = null, tint = K.Faint, modifier = Modifier.size(30.dp))
                    }
                }
            }
            // The label sits on its own dark plate so the dotted route passes behind it, not through it.
            Column(
                modifier = Modifier
                    .background(K.SpaceTop.copy(alpha = 0.88f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                StarRow(stars, size = 18.dp)
                Text(
                    skill.title.str(),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (unlocked) K.Text else K.Faint,
                    textAlign = TextAlign.Center,
                )
                if (recommended) {
                    Text(
                        S.nextMission.str(),
                        style = MaterialTheme.typography.labelSmall,
                        color = K.Gold,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
        }
    }
}
