package app.komet.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.komet.audio.Sfx
import app.komet.ui.theme.K

/** Sound and vibration for controls, provided once from the root so buttons stay plain composables. */
interface Feedback {
    fun sfx(effect: Sfx, volume: Float = 0.9f)
    fun tap() = sfx(Sfx.TAP)
}

val LocalFeedback = staticCompositionLocalOf<Feedback> { object : Feedback { override fun sfx(effect: Sfx, volume: Float) = Unit } }

/**
 * Paints a shape the way game buttons are painted: a gradient from a lighter top, a soft gloss over
 * the upper half and a thin dark rim.
 */
fun Modifier.gloss(face: Color, shape: Shape, top: Color = lerp(face, Color.White, 0.2f), rim: Float = 0.42f): Modifier =
    drawWithCache {
        val path = Path().apply { addOutline(shape.createOutline(size, layoutDirection, this@drawWithCache)) }
        val fill = Brush.verticalGradient(0f to top, 0.55f to face, 1f to lerp(face, Color.Black, 0.12f))
        // White keys keep only a whisper of gloss; coloured faces get a clear highlight.
        val shine = if (face.luminance() > 0.8f) 0.05f else 0.17f
        val highlight = Brush.verticalGradient(0f to Color.White.copy(alpha = shine), 0.5f to Color.White.copy(alpha = shine * 0.25f), 0.56f to Color.Transparent)
        val rimStroke = Stroke(width = 1.6.dp.toPx())
        onDrawBehind {
            drawPath(path, fill)
            drawPath(path, highlight)
            drawPath(path, K.Outline.copy(alpha = rim), style = rimStroke)
        }
    }

/**
 * A pressable slab with a darker edge underneath. On press the face sinks onto the edge, which
 * gives a physical click that children understand without any ripple.
 */
@Composable
fun PressSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    face: Color = K.SurfaceHigh,
    edge: Color = K.SurfaceLow,
    shape: Shape = RoundedCornerShape(22.dp),
    depth: Dp = 5.dp,
    enabled: Boolean = true,
    tapSound: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
    contentAlignment: Alignment = Alignment.Center,
    description: String? = null,
    top: Color = lerp(face, Color.White, 0.2f),
    content: @Composable BoxScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val sink by animateDpAsState(if (pressed && enabled) depth else 0.dp, tween(70), label = "press")
    val feedback = LocalFeedback.current
    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.45f)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = {
                    if (tapSound) feedback.tap()
                    onClick()
                },
            )
            .then(if (description != null) Modifier.semantics { contentDescription = description } else Modifier),
        propagateMinConstraints = true,
    ) {
        Box(
            Modifier
                .matchParentSize()
                .padding(top = depth)
                .gloss(edge, shape, top = edge, rim = 0.55f),
        )
        Box(
            modifier = Modifier
                .padding(bottom = depth)
                .offset { IntOffset(0, sink.roundToPx()) }
                .gloss(face, shape, top = top)
                .padding(contentPadding),
            contentAlignment = contentAlignment,
            content = content,
        )
    }
}

@Composable
fun BigButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    face: Color = K.Gold,
    edge: Color = K.GoldDeep,
    textColor: Color = Color.White,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    top: Color = lerp(face, Color.White, 0.25f),
) {
    PressSurface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 66.dp),
        face = face,
        edge = edge,
        top = top,
        depth = 6.dp,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Box {
                    Icon(icon, contentDescription = null, tint = K.Outline.copy(alpha = 0.7f), modifier = Modifier.offset(y = 2.dp).size(28.dp))
                    Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(28.dp))
                }
            }
            GameText(
                text = text,
                color = textColor,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}

@Composable
fun RoundIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    face: Color = K.SurfaceHigh,
    edge: Color = K.SurfaceLow,
    tint: Color = K.Text,
    enabled: Boolean = true,
) {
    PressSurface(
        onClick = onClick,
        modifier = modifier.size(size),
        face = face,
        edge = edge,
        shape = CircleShape,
        depth = 4.dp,
        enabled = enabled,
        contentPadding = PaddingValues(0.dp),
        description = description,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.46f))
    }
}

/** A plain label for places where game lettering would be too loud, such as the parents' pages. */
@Composable
fun PlainLabel(text: String, color: Color = K.Text) {
    Text(text, color = color, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
}
