package app.komet.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.komet.domain.Subject
import app.komet.ui.theme.K

val MaxContentWidth = 720.dp

/** The bright accent and the dark edge of a subject; see [subjectTone] for the full set. */
fun subjectColors(subject: Subject): Pair<Color, Color> = subjectTone(subject).let { it.accent to it.edge }

/** One readable, centred column. Wide windows get margins instead of stretched content. */
@Composable
fun PageColumn(
    modifier: Modifier = Modifier,
    scroll: Boolean = true,
    maxWidth: Dp = MaxContentWidth,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .fillMaxWidth()
                .fillMaxHeight()
                .then(if (scroll) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                .padding(contentPadding),
            verticalArrangement = verticalArrangement,
            content = content,
        )
    }
}

@Composable
fun ScreenTopBar(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            title,
            style = MaterialTheme.typography.headlineMedium,
            color = K.Text,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        trailing()
        // Every screen on top of the map closes with the same red X in the corner.
        if (onBack != null) CloseButton(onClick = onBack)
    }
}

@Composable
fun Panel(
    modifier: Modifier = Modifier,
    color: Color = K.Surface,
    padding: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(26.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(Brush.verticalGradient(listOf(lerp(color, Color.White, 0.05f), lerp(color, K.SpaceTop, 0.25f))))
            .border(1.5.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.13f), Color.White.copy(alpha = 0.02f))), shape)
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content,
    )
}

@Composable
fun Pill(text: String, modifier: Modifier = Modifier, icon: ImageVector? = null, iconTint: Color = K.Gold, star: Boolean = false) {
    Row(
        modifier = modifier
            .background(K.SurfaceLow.copy(alpha = 0.9f), RoundedCornerShape(50))
            .border(1.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (star) StarGlyph(true, Modifier.size(20.dp))
        if (icon != null) Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        // Short numbers beside an icon; they follow big system text only part of the way so bars keep their room.
        Text(text, color = K.Text, style = MaterialTheme.typography.labelLarge, fontSize = cappedSp(17.sp), lineHeight = cappedSp(22.sp), fontWeight = FontWeight.ExtraBold, maxLines = 1)
    }
}

@Composable
fun ProgressTrack(progress: Float, modifier: Modifier = Modifier, color: Color = K.Gold, track: Color = K.SurfaceLow, height: Dp = 12.dp) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(500), label = "progress")
    // A sunken groove with a glossy bar in it, like a health bar in a game.
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(Brush.verticalGradient(listOf(lerp(track, Color.Black, 0.35f), track)))
            .border(1.dp, K.Outline.copy(alpha = 0.5f), RoundedCornerShape(50)),
    ) {
        if (animated > 0f) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animated)
                    .clip(RoundedCornerShape(50))
                    .background(Brush.verticalGradient(listOf(lerp(color, Color.White, 0.35f), color, lerp(color, Color.Black, 0.15f))))
                    .border(1.dp, K.Outline.copy(alpha = 0.35f), RoundedCornerShape(50)),
            )
        }
    }
}

/** A row of equal buttons where exactly one is chosen. */
@Composable
fun SegmentedChoice(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { index, label ->
            val active = index == selected
            PressSurface(
                onClick = { onSelect(index) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp),
                face = if (active) K.Gold else K.SurfaceHigh,
                edge = if (active) K.GoldDeep else K.SurfaceLow,
                top = if (active) K.GoldTop else lerp(K.SurfaceHigh, Color.White, 0.2f),
                depth = 4.dp,
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
            ) {
                if (active) {
                    GameText(label, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
                } else {
                    Text(label, color = K.Text, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

/** A large selectable card for setup choices. */
@Composable
fun SelectCard(selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    PressSurface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .border(if (selected) 3.dp else 0.dp, if (selected) K.Gold else Color.Transparent, RoundedCornerShape(22.dp)),
        face = if (selected) K.SurfaceHigh else K.Surface,
        edge = if (selected) K.GoldDeep else K.SurfaceLow,
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp), content = content)
            if (selected) {
                Spacer(Modifier.size(8.dp))
                Box(
                    Modifier
                        .size(34.dp)
                        .background(K.Gold, RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(KometIcons.Check, contentDescription = null, tint = K.Ink, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleLarge,
        color = K.Text,
        modifier = modifier
            .padding(top = 6.dp)
            .semantics { heading() },
    )
}
