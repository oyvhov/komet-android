package app.komet.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.komet.ui.S
import app.komet.ui.theme.K
import kotlin.math.cos
import kotlin.math.sin

/**
 * The one way to close things in Komet: a big, round, red button with a thick white X. Children learn it
 * once and find it in the same corner everywhere.
 */
@Composable
fun CloseButton(onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 56.dp) {
    PressSurface(
        onClick = onClick,
        modifier = modifier.size(size),
        face = K.Race,
        edge = K.RaceDeep,
        top = K.RaceTop,
        shape = CircleShape,
        depth = 4.dp,
        contentPadding = PaddingValues(0.dp),
        description = S.close.str(),
    ) {
        Canvas(Modifier.size(size * 0.4f)) {
            val a = Offset(0f, 0f)
            val b = Offset(this.size.width, this.size.height)
            val c = Offset(this.size.width, 0f)
            val d = Offset(0f, this.size.height)
            val outline = this.size.minDimension * 0.34f
            val core = this.size.minDimension * 0.2f
            drawLine(K.Outline, a, b, strokeWidth = outline, cap = StrokeCap.Round)
            drawLine(K.Outline, c, d, strokeWidth = outline, cap = StrokeCap.Round)
            drawLine(Color.White, a, b, strokeWidth = core, cap = StrokeCap.Round)
            drawLine(Color.White, c, d, strokeWidth = core, cap = StrokeCap.Round)
        }
    }
}

/**
 * A popup in the game's style: a glossy panel over a dark veil, with the red X sticking out of its top
 * corner. A tap on the veil closes it as well.
 */
@Composable
fun KometDialog(onClose: () -> Unit, maxWidth: Dp = 520.dp, content: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            Modifier
                .fillMaxSize()
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClose)
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.widthIn(max = maxWidth).fillMaxWidth()) {
                val shape = RoundedCornerShape(32.dp)
                Column(
                    Modifier
                        .padding(top = 20.dp, end = 12.dp)
                        .fillMaxWidth()
                        .clip(shape)
                        .background(Brush.verticalGradient(listOf(lerp(K.SurfaceHigh, Color.White, 0.04f), K.Surface, lerp(K.Surface, K.SpaceTop, 0.4f))))
                        .border(2.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.04f))), shape)
                        // Taps inside the panel stay inside.
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                        .verticalScroll(rememberScrollState())
                        .padding(start = 22.dp, end = 22.dp, top = 26.dp, bottom = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    content = content,
                )
                CloseButton(onClose, Modifier.align(Alignment.TopEnd))
            }
        }
    }
}

/** A yes-or-no question as a [KometDialog]: big buttons, the safe choice first. */
@Composable
fun ConfirmPopup(
    title: String,
    body: String?,
    confirm: String,
    onConfirm: () -> Unit,
    dismiss: String,
    onClose: () -> Unit,
    confirmFace: Color = K.Gold,
    confirmEdge: Color = K.GoldDeep,
    art: (@Composable () -> Unit)? = null,
) {
    KometDialog(onClose = onClose) {
        art?.invoke()
        GameText(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(end = 24.dp))
        if (body != null) {
            Text(body, style = MaterialTheme.typography.bodyLarge, color = K.Muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
        BigButton(confirm, onClick = onConfirm, face = confirmFace, edge = confirmEdge, modifier = Modifier.fillMaxWidth())
        BigButton(dismiss, onClick = onClose, face = K.SurfaceHigh, edge = K.SurfaceLow, textColor = K.Text, modifier = Modifier.fillMaxWidth())
    }
}

/** A gold nugget, lit from the top left like the stars. */
@Composable
fun NuggetGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier) { drawNugget(center, size.minDimension * 0.46f) }
}

/** Draws a gold nugget centred on [center]; [shine] from 0 to 1 moves a glint over it. */
fun DrawScope.drawNugget(center: Offset, radius: Float, shine: Float = 0.3f) {
    val shape = Path().apply {
        val points = listOf(-0.95f to 0.2f, -0.72f to -0.52f, -0.12f to -0.9f, 0.55f to -0.72f, 0.98f to -0.05f, 0.7f to 0.66f, -0.05f to 0.84f, -0.7f to 0.7f)
        points.forEachIndexed { index, (x, y) ->
            val p = Offset(center.x + x * radius, center.y + y * radius * 0.82f)
            if (index == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
        }
        close()
    }
    drawPath(shape, K.Outline.copy(alpha = 0.55f), style = Stroke(radius * 0.3f, join = StrokeJoin.Round))
    drawPath(shape, Brush.linearGradient(listOf(K.GoldTop, K.Gold, K.GoldDeep), start = center - Offset(radius, radius), end = center + Offset(radius, radius)))
    // Facets
    val facet = Path().apply {
        moveTo(center.x - 0.12f * radius, center.y - 0.74f * radius)
        lineTo(center.x + 0.18f * radius, center.y - 0.05f * radius)
        lineTo(center.x - 0.72f * radius, center.y + 0.16f * radius)
        close()
    }
    drawPath(facet, Color.White.copy(alpha = 0.35f))
    drawLine(K.GoldDeep.copy(alpha = 0.6f), Offset(center.x + 0.18f * radius, center.y - 0.05f * radius), Offset(center.x + 0.62f * radius, center.y + 0.46f * radius), strokeWidth = radius * 0.08f)
    drawPath(shape, K.Outline, style = Stroke(radius * 0.12f, join = StrokeJoin.Round))
    // A glint that twinkles
    val glint = center + Offset(-0.34f * radius, -0.4f * radius)
    val arm = radius * (0.22f + 0.18f * shine)
    drawLine(Color.White, glint - Offset(arm, 0f), glint + Offset(arm, 0f), strokeWidth = radius * 0.09f, cap = StrokeCap.Round)
    drawLine(Color.White, glint - Offset(0f, arm), glint + Offset(0f, arm), strokeWidth = radius * 0.09f, cap = StrokeCap.Round)
}

/** The number of gold nuggets, in a dark pill like the stars. */
@Composable
fun NuggetPill(count: Int, modifier: Modifier = Modifier, big: Boolean = false) {
    Row(
        modifier = modifier
            .background(K.SurfaceLow.copy(alpha = 0.9f), RoundedCornerShape(50))
            .border(1.5.dp, K.Gold.copy(alpha = 0.35f), RoundedCornerShape(50))
            .padding(horizontal = if (big) 16.dp else 12.dp, vertical = if (big) 9.dp else 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        NuggetGlyph(Modifier.size(if (big) 28.dp else 22.dp))
        Text(
            count.toString(),
            color = K.GoldTop,
            style = if (big) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

/** A small burst of rays behind something precious. */
fun DrawScope.drawRays(center: Offset, radius: Float, color: Color, turn: Float) {
    for (i in 0 until 12) {
        val a = turn + i * (Math.PI.toFloat() / 6f)
        val tip = center + Offset(cos(a), sin(a)) * radius
        val side = Offset(-sin(a), cos(a)) * (radius * 0.08f)
        val ray = Path().apply {
            moveTo(center.x + side.x, center.y + side.y)
            lineTo(tip.x, tip.y)
            lineTo(center.x - side.x, center.y - side.y)
            close()
        }
        drawPath(ray, Brush.radialGradient(listOf(color.copy(alpha = 0.45f), Color.Transparent), center, radius))
    }
}
