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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.komet.domain.Subject
import app.komet.ui.S
import app.komet.ui.theme.K

val MaxContentWidth = 720.dp

fun subjectColors(subject: Subject): Pair<Color, Color> =
    if (subject == Subject.MATH) K.Math to K.MathDeep else K.Reading to K.ReadingDeep

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
        if (onBack != null) {
            RoundIconButton(KometIcons.Back, S.back.str(), onBack, size = 52.dp)
        }
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
    }
}

@Composable
fun Panel(
    modifier: Modifier = Modifier,
    color: Color = K.Surface,
    padding: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .background(color)
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content,
    )
}

@Composable
fun Pill(text: String, modifier: Modifier = Modifier, icon: ImageVector? = null, iconTint: Color = K.Gold, star: Boolean = false) {
    Row(
        modifier = modifier
            .background(K.SurfaceHigh.copy(alpha = 0.85f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (star) StarGlyph(true, Modifier.size(20.dp))
        if (icon != null) Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        Text(text, color = K.Text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun ProgressTrack(progress: Float, modifier: Modifier = Modifier, color: Color = K.Gold, track: Color = K.SurfaceLow, height: Dp = 12.dp) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(500), label = "progress")
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(track),
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated)
                .clip(RoundedCornerShape(50))
                .background(color),
        )
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
                depth = 4.dp,
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
            ) {
                Text(label, color = if (active) K.Ink else K.Text, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
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
