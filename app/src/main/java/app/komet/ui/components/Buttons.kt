package app.komet.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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
    fun sfx(effect: Sfx)
    fun tap() = sfx(Sfx.TAP)
}

val LocalFeedback = staticCompositionLocalOf<Feedback> { object : Feedback { override fun sfx(effect: Sfx) = Unit } }

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
                .background(edge, shape),
        )
        Box(
            modifier = Modifier
                .padding(bottom = depth)
                .offset { IntOffset(0, sink.roundToPx()) }
                .background(face, shape)
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
    textColor: Color = K.Ink,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    PressSurface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 64.dp),
        face = face,
        edge = edge,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(26.dp))
            Text(
                text = text,
                color = textColor,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
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
