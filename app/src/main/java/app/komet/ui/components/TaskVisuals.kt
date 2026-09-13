package app.komet.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.komet.domain.GlyphKind
import app.komet.domain.PatternItem
import app.komet.domain.ShapeKind
import app.komet.domain.Token
import app.komet.domain.Txt
import app.komet.domain.Visual
import app.komet.domain.inCase
import app.komet.ui.S
import app.komet.ui.theme.K
import app.komet.ui.theme.LocalMotion
import app.komet.ui.theme.LocalReading
import app.komet.ui.theme.LocalVisualBox
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/** State the task picture needs from the round: what is typed and whether the answer is known. */
data class VisualState(
    val input: String = "",
    val answered: Boolean = false,
    val correctValue: String? = null,
    val speechAvailable: Boolean = true,
)

@Composable
fun TaskVisual(
    visual: Visual,
    state: VisualState,
    onListen: (Txt) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        when (visual) {
            Visual.None -> Unit
            is Visual.Equation -> EquationView(visual.tokens, state)
            is Visual.Objects -> ObjectsView(visual.emoji, visual.count, visual.scattered)
            is Visual.AddGroups -> AddGroupsView(visual)
            is Visual.TakeAway -> TakeAwayView(visual)
            is Visual.TenFrames -> TenFramesView(visual.first, visual.second)
            is Visual.BaseTen -> BaseTenView(visual.tens, visual.ones)
            is Visual.Clock -> ClockFace(visual.hour, visual.minute, Modifier.size(minOf(300.dp, LocalVisualBox.current.width, LocalVisualBox.current.height)))
            is Visual.Money -> MoneyView(visual.items)
            is Visual.NumberRow -> NumberRowView(visual.items, state)
            is Visual.Pattern -> PatternView(visual.items)
            is Visual.Shape -> ShapeView(visual.item, Modifier.size(170.dp))
            is Visual.Glyph -> GlyphView(visual.text, visual.kind)
            is Visual.Picture -> PictureView(visual, state)
            is Visual.Groups -> GroupsView(visual)
            is Visual.Share -> ShareView(visual)
            is Visual.Balance -> BalanceView(visual, state)
            is Visual.Story -> StoryView(visual)
            is Visual.Listen -> ListenView(visual, state, onListen)
            is Visual.Stack -> {
                // Stacked pictures share the card's height instead of each claiming all of it.
                val box = LocalVisualBox.current
                val share = box.copy(height = (box.height - 14.dp * (visual.items.size - 1)) / visual.items.size)
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    androidx.compose.runtime.CompositionLocalProvider(LocalVisualBox provides share) {
                        visual.items.forEach { TaskVisual(it, state, onListen) }
                    }
                }
            }
            is Visual.Compare -> CompareView(visual.a, visual.b)
        }
    }
}

// ── Numbers ────────────────────────────────────────────────────────────────────────────────────

@Composable
private fun EquationView(tokens: List<Token>, state: VisualState) {
    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val characters = tokens.sumOf { token ->
            when (token) {
                is Token.Num -> token.value.toString().length
                is Token.Op -> 1
                Token.Blank -> 2
            } + 1
        }
        val size = minOf((maxWidth.value / (characters * 0.62f)).coerceIn(30f, 80f), LocalVisualBox.current.height.value * 0.6f).coerceAtLeast(30f).dp
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(size * 0.18f)) {
            tokens.forEach { token ->
                when (token) {
                    is Token.Num -> Text(token.value.toString(), color = K.Ink, fontSize = fixedSp(size), fontWeight = FontWeight.Black)
                    is Token.Op -> Text(token.symbol, color = K.InkMuted, fontSize = fixedSp(size * 0.9f), fontWeight = FontWeight.Bold)
                    Token.Blank -> BlankSlot(state, size)
                }
            }
        }
    }
}

@Composable
private fun BlankSlot(state: VisualState, size: Dp) {
    val shown = when {
        state.answered && state.correctValue != null -> state.correctValue
        state.input.isNotEmpty() -> state.input
        else -> "?"
    }
    val color = when {
        state.answered -> K.GoodDeep
        state.input.isNotEmpty() -> K.Ink
        else -> K.Race
    }
    Box(
        modifier = Modifier
            .widthIn(min = size * 1.3f)
            .height(size * 1.35f)
            .background(if (state.answered) K.Good.copy(alpha = 0.18f) else K.PaperShade, RoundedCornerShape(16.dp))
            .border(3.dp, color.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(shown, color = color, fontSize = fixedSp(size), fontWeight = FontWeight.Black)
    }
}

@Composable
private fun NumberRowView(items: List<Int?>, state: VisualState) {
    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val cell = min(76f, (maxWidth.value - (items.size - 1) * 8f) / items.size).dp
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { value ->
                val gap = value == null
                Box(
                    modifier = Modifier
                        .size(cell, cell * 1.1f)
                        .background(if (gap) K.Race.copy(alpha = 0.14f) else Color.White, RoundedCornerShape(14.dp))
                        .border(2.5.dp, if (gap) K.Race else K.PaperShade, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    val text = when {
                        !gap -> value.toString()
                        state.answered && state.correctValue != null -> state.correctValue
                        else -> "?"
                    }
                    Text(
                        text,
                        color = if (gap && state.answered) K.GoodDeep else if (gap) K.Race else K.Ink,
                        fontSize = fixedSp(cell * (if (text.length > 2) 0.34f else 0.46f)),
                        fontWeight = FontWeight.Black,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmojiText(emoji: String, size: Dp, modifier: Modifier = Modifier) {
    Text(emoji, fontSize = fixedSp(size), lineHeight = fixedSp(size * 1.15f), modifier = modifier)
}

/**
 * Picks an emoji size that fills the card: few objects become big pictures, many still fit.
 * [columns] and [rows] describe the grid the emoji will be laid out in.
 */
@Composable
private fun fittedSize(columns: Int, rows: Int, max: Dp = 104.dp, min: Dp = 24.dp): Dp {
    val box = LocalVisualBox.current
    val byWidth = box.width / (columns.coerceAtLeast(1) * 1.3f)
    val byHeight = box.height / (rows.coerceAtLeast(1) * 1.45f)
    return minOf(max, byWidth, byHeight).coerceAtLeast(min)
}

@Composable
private fun ObjectsView(emoji: String, count: Int, scattered: Boolean) {
    val size = if (scattered) {
        fittedSize(columns = 4, rows = ceil((count + 3) / 4f).toInt().coerceAtLeast(3), max = 80.dp)
    } else {
        fittedSize(columns = minOf(count, 5), rows = ceil(count / 5f).toInt())
    }
    if (scattered) {
        val columns = 4
        val rows = ceil((count + 3) / columns.toFloat()).toInt().coerceAtLeast(3)
        val cells = remember(emoji, count) { (0 until columns * rows).shuffled(Random(count * 31 + emoji.hashCode())).take(count).toSet() }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (row in 0 until rows) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (column in 0 until columns) {
                        Box(Modifier.size(size * 1.2f), contentAlignment = Alignment.Center) {
                            if (row * columns + column in cells) EmojiText(emoji, size)
                        }
                    }
                }
            }
        }
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            (0 until count).chunked(5).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    row.forEach { EmojiText(emoji, size) }
                }
            }
        }
    }
}

@Composable
private fun GroupBox(tint: Color, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .background(tint.copy(alpha = 0.14f), RoundedCornerShape(18.dp))
            .border(2.dp, tint.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun EmojiGrid(emoji: String, count: Int, itemSize: Dp, perRow: Int = 5, faded: Int = 0) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        (0 until count).chunked(perRow).forEach { row ->
            Row {
                row.forEach { index ->
                    val removed = index >= count - faded
                    Box(contentAlignment = Alignment.Center) {
                        EmojiText(emoji, itemSize, Modifier.alpha(if (removed) 0.3f else 1f))
                        if (removed) {
                            Canvas(Modifier.size(itemSize * 1.1f)) {
                                drawLine(K.Bad, Offset(size.width * 0.1f, size.height * 0.9f), Offset(size.width * 0.9f, size.height * 0.1f), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddGroupsView(visual: Visual.AddGroups) {
    val columns = minOf(visual.a, 5) + minOf(visual.b, 5) + 2
    val rows = ceil(maxOf(visual.a, visual.b) / 5f).toInt()
    val size = fittedSize(columns, rows * 2, max = 60.dp, min = 18.dp)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        GroupBox(K.shapes[3]) { EmojiGrid(visual.emoji, visual.a, size) }
        Text("+", color = K.InkMuted, fontSize = fixedSp(34.dp), fontWeight = FontWeight.Black)
        GroupBox(K.shapes[1]) { EmojiGrid(visual.emoji, visual.b, size) }
    }
}

@Composable
private fun TakeAwayView(visual: Visual.TakeAway) {
    val size = fittedSize(minOf(visual.total, 5), ceil(visual.total / 5f).toInt(), max = 80.dp)
    EmojiGrid(visual.emoji, visual.total, size, faded = visual.removed)
}

@Composable
private fun TenFramesView(first: Int, second: Int) {
    val total = first + second
    val frames = maxOf(1, ceil(total / 10f).toInt())
    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val frameWidth = min(300f, if (frames > 1 && maxWidth.value >= 560f) (maxWidth.value - 16f) / 2 else maxWidth.value).dp
        val cell = frameWidth / 5
        val layout: @Composable (@Composable () -> Unit) -> Unit = { content ->
            if (frames > 1 && maxWidth.value >= 560f) Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) { content() }
            else Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { content() }
        }
        layout {
            for (frame in 0 until frames) {
                Canvas(Modifier.size(frameWidth, cell * 2).clip(RoundedCornerShape(12.dp))) {
                    val c = cell.toPx()
                    drawRoundRect(Color.White, size = size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()))
                    for (i in 0 until 10) {
                        val global = frame * 10 + i
                        val x = (i % 5) * c
                        val y = (i / 5) * c
                        drawRect(K.PaperShade, Offset(x, y), Size(c, c), style = Stroke(2.dp.toPx()))
                        val center = Offset(x + c / 2, y + c / 2)
                        when {
                            global < first -> drawCircle(K.shapes[3], c * 0.34f, center)
                            global < total -> drawCircle(K.shapes[1], c * 0.34f, center)
                            else -> drawCircle(K.PaperShade, c * 0.34f, center, style = Stroke(2.dp.toPx()))
                        }
                    }
                    drawRoundRect(K.Ink.copy(alpha = 0.7f), size = size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()), style = Stroke(3.dp.toPx()))
                }
            }
        }
    }
}

@Composable
private fun BaseTenView(tens: Int, ones: Int) {
    val unit = if (tens > 6) 15.dp else 18.dp
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(tens) {
            Canvas(Modifier.size(unit, unit * 10)) {
                val u = unit.toPx()
                for (i in 0 until 10) {
                    drawRect(K.shapes[3], Offset(0f, i * u), Size(u, u))
                    drawRect(Color(0xFF1E4FA8), Offset(0f, i * u), Size(u, u), style = Stroke(1.5.dp.toPx()))
                }
            }
        }
        if (ones > 0) {
            Spacer(Modifier.width(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                (0 until ones).chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        row.forEach {
                            Box(
                                Modifier
                                    .size(unit)
                                    .background(K.shapes[1], RoundedCornerShape(3.dp))
                                    .border(1.5.dp, Color(0xFFB86E00), RoundedCornerShape(3.dp)),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompareView(a: Int, b: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        listOf(a, b).forEachIndexed { index, count ->
            Box(
                modifier = Modifier
                    .background(K.shapes[if (index == 0) 3 else 1].copy(alpha = 0.12f), RoundedCornerShape(18.dp))
                    .padding(12.dp)
                    .size(120.dp, 60.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.size(110.dp, 46.dp)) {
                    val cell = size.width / 5
                    for (i in 0 until count) {
                        val x = (i % 5) * cell + cell / 2
                        val y = (i / 5) * (size.height / 2) + size.height / 4
                        drawCircle(K.shapes[if (index == 0) 3 else 1], cell * 0.34f, Offset(x, y))
                    }
                }
            }
        }
    }
}

// ── Clock and money ────────────────────────────────────────────────────────────────────────────

@Composable
fun ClockFace(hour: Int, minute: Int, modifier: Modifier = Modifier, numerals: Boolean = true) {
    val measurer = rememberTextMeasurer()
    Canvas(modifier.semantics { contentDescription = "klokke" }) {
        val r = size.minDimension / 2 * 0.94f
        val c = center
        drawCircle(Color.White, r, c)
        drawCircle(K.Ink, r, c, style = Stroke(r * 0.05f))
        for (i in 0 until 60) {
            val angle = i * PI / 30 - PI / 2
            val major = i % 5 == 0
            val inner = r * (if (major) 0.82f else 0.88f)
            drawLine(
                if (major) K.Ink else K.InkMuted.copy(alpha = 0.5f),
                Offset(c.x + (cos(angle) * inner).toFloat(), c.y + (sin(angle) * inner).toFloat()),
                Offset(c.x + (cos(angle) * r * 0.93f).toFloat(), c.y + (sin(angle) * r * 0.93f).toFloat()),
                strokeWidth = if (major) r * 0.035f else r * 0.012f,
                cap = StrokeCap.Round,
            )
        }
        // Small faces keep only 12, 3, 6 and 9 so the numerals stay readable.
        val numeralScale = if (numerals) 0.16f else 0.26f
        val style = TextStyle(color = K.Ink, fontSize = (r * numeralScale / density / fontScale).sp, fontWeight = FontWeight.Bold)
        for (n in if (numerals) (1..12).toList() else listOf(12, 3, 6, 9)) {
            val angle = n * PI / 6 - PI / 2
            val layout = measurer.measure(n.toString(), style)
            val x = c.x + (cos(angle) * r * 0.66f).toFloat() - layout.size.width / 2f
            val y = c.y + (sin(angle) * r * 0.66f).toFloat() - layout.size.height / 2f
            drawText(layout, topLeft = Offset(x, y))
        }
        val minuteAngle = minute * PI / 30 - PI / 2
        val hourAngle = ((hour % 12) + minute / 60.0) * PI / 6 - PI / 2
        drawLine(K.Ink, c, Offset(c.x + (cos(hourAngle) * r * 0.46f).toFloat(), c.y + (sin(hourAngle) * r * 0.46f).toFloat()), strokeWidth = r * 0.085f, cap = StrokeCap.Round)
        drawLine(K.Race, c, Offset(c.x + (cos(minuteAngle) * r * 0.76f).toFloat(), c.y + (sin(minuteAngle) * r * 0.76f).toFloat()), strokeWidth = r * 0.05f, cap = StrokeCap.Round)
        drawCircle(K.Ink, r * 0.07f, c)
        drawCircle(K.Gold, r * 0.035f, c)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MoneyView(items: List<Int>) {
    // Real coins are small; on the card they grow with the room available, like everything else.
    val box = LocalVisualBox.current
    val naturalWidth = items.sumOf { if (it >= 50) 118 else 60 } + (items.size - 1) * 10
    val scale = (box.width.value / naturalWidth).coerceIn(1f, 1.9f).coerceAtMost(box.height.value / 90f).coerceAtLeast(1f)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { MoneyItem(it, scale) }
    }
}

@Composable
fun MoneyItem(value: Int, scale: Float = 1f) {
    if (value >= 50) {
        val color = if (value == 50) Color(0xFF6DBE8A) else Color(0xFFE0736B)
        Box(
            modifier = Modifier
                .size(118.dp * scale, 62.dp * scale)
                .background(Brush.linearGradient(listOf(color, color.copy(alpha = 0.75f))), RoundedCornerShape(8.dp))
                .border(2.dp, Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("$value kr", color = Color.White, fontSize = fixedSp(22.dp * scale), fontWeight = FontWeight.Black)
        }
        return
    }
    val gold = value >= 10
    val diameter = when (value) {
        1 -> 50.dp
        5 -> 60.dp
        10 -> 56.dp
        else -> 66.dp
    } * scale
    val face = if (gold) listOf(Color(0xFFFFE9A3), Color(0xFFE3B24A), Color(0xFFB9862A)) else listOf(Color(0xFFF4F6FA), Color(0xFFC5CBD6), Color(0xFF8E96A6))
    Box(Modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(diameter)) {
            val r = size.minDimension / 2
            drawCircle(Brush.radialGradient(face, Offset(center.x - r * 0.3f, center.y - r * 0.3f), r * 1.5f), r, center)
            drawCircle(Color.Black.copy(alpha = 0.18f), r * 0.92f, center, style = Stroke(r * 0.06f))
            if (!gold) drawCircle(K.Paper, r * 0.2f, Offset(center.x, center.y - r * 0.5f))
        }
        Text(
            value.toString(),
            color = if (gold) Color(0xFF5A3A00) else Color(0xFF3A4150),
            fontSize = fixedSp(diameter * 0.36f),
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(top = if (gold) 0.dp else diameter * 0.22f),
        )
    }
}

// ── Shapes and patterns ────────────────────────────────────────────────────────────────────────

fun DrawScope.drawShape(kind: ShapeKind, color: Color, outline: Boolean = true) {
    val w = size.width
    val h = size.height
    val s = min(w, h) * 0.9f
    val cx = w / 2
    val cy = h / 2
    val path = Path()
    when (kind) {
        ShapeKind.CIRCLE -> path.addOval(Rect(Offset(cx, cy), s / 2))
        ShapeKind.SQUARE -> path.addRect(Rect(cx - s * 0.42f, cy - s * 0.42f, cx + s * 0.42f, cy + s * 0.42f))
        ShapeKind.RECTANGLE -> path.addRect(Rect(cx - s * 0.5f, cy - s * 0.28f, cx + s * 0.5f, cy + s * 0.28f))
        ShapeKind.TRIANGLE -> {
            path.moveTo(cx, cy - s * 0.46f)
            path.lineTo(cx + s * 0.5f, cy + s * 0.4f)
            path.lineTo(cx - s * 0.5f, cy + s * 0.4f)
            path.close()
        }
        ShapeKind.PENTAGON, ShapeKind.HEXAGON -> {
            val sides = if (kind == ShapeKind.PENTAGON) 5 else 6
            val offset = if (sides == 5) -PI / 2 else 0.0
            for (i in 0 until sides) {
                val angle = offset + i * 2 * PI / sides
                val x = cx + (cos(angle) * s * 0.5f).toFloat()
                val y = cy + (sin(angle) * s * 0.5f).toFloat() + if (sides == 5) s * 0.04f else 0f
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
        }
        ShapeKind.STAR -> {
            for (i in 0 until 10) {
                val radius = if (i % 2 == 0) s * 0.5f else s * 0.21f
                val angle = -PI / 2 + i * PI / 5
                val x = cx + (radius * cos(angle)).toFloat()
                val y = cy + s * 0.04f + (radius * sin(angle)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
        }
        ShapeKind.HEART -> {
            path.moveTo(cx, cy + s * 0.42f)
            path.cubicTo(cx - s * 0.62f, cy + s * 0.02f, cx - s * 0.42f, cy - s * 0.52f, cx, cy - s * 0.2f)
            path.cubicTo(cx + s * 0.42f, cy - s * 0.52f, cx + s * 0.62f, cy + s * 0.02f, cx, cy + s * 0.42f)
            path.close()
        }
        ShapeKind.DIAMOND -> {
            path.moveTo(cx, cy - s * 0.5f)
            path.lineTo(cx + s * 0.36f, cy)
            path.lineTo(cx, cy + s * 0.5f)
            path.lineTo(cx - s * 0.36f, cy)
            path.close()
        }
    }
    drawPath(path, color)
    if (outline) drawPath(path, Color.Black.copy(alpha = 0.22f), style = Stroke(width = s * 0.035f))
}

@Composable
fun ShapeView(item: PatternItem, modifier: Modifier = Modifier) {
    val color = K.shapes[item.color.mod(K.shapes.size)]
    Canvas(modifier) { drawShape(item.shape, color) }
}

@Composable
private fun PatternView(items: List<PatternItem?>) {
    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val cell = min(56f, (maxWidth.value - (items.size - 1) * 6f) / items.size).dp
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            items.forEach { item ->
                if (item == null) {
                    val transition = rememberInfiniteTransition(label = "gap")
                    val pulse = transition.animateFloat(1f, 1.12f, infiniteRepeatable(tween(650), RepeatMode.Reverse), label = "pulse")
                    val motion = LocalMotion.current
                    Box(
                        modifier = Modifier
                            .size(cell)
                            .scale(if (motion) pulse.value else 1f)
                            .border(2.5.dp, K.Race, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("?", color = K.Race, fontSize = fixedSp(cell * 0.5f), fontWeight = FontWeight.Black)
                    }
                } else {
                    ShapeView(item, Modifier.size(cell))
                }
            }
        }
    }
}

// ── Reading ────────────────────────────────────────────────────────────────────────────────────

@Composable
private fun GlyphView(text: Txt, kind: GlyphKind) {
    val value = readingString(text, kind)
    when (kind) {
        GlyphKind.SENTENCE -> Text(
            value,
            color = K.Ink,
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold, lineHeight = 46.sp),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        GlyphKind.PLAIN -> Text(value, color = K.Ink, style = MaterialTheme.typography.displayMedium, textAlign = TextAlign.Center)
        else -> BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val base = when (kind) {
                GlyphKind.LETTER, GlyphKind.EXACT -> minOf(170f, LocalVisualBox.current.height.value * 0.8f)
                GlyphKind.NUMBER -> 110f
                else -> 76f
            }
            val size = min(base, maxWidth.value / (value.length.coerceAtLeast(1) * 0.62f)).dp
            Text(value, color = K.Ink, fontSize = fixedSp(size), fontWeight = FontWeight.Bold, maxLines = 1, lineHeight = fixedSp(size * 1.15f))
        }
    }
}

@Composable
private fun PictureView(visual: Visual.Picture, state: VisualState) {
    val prefs = LocalReading.current
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        EmojiText(visual.emoji, minOf(if (visual.emoji.length > 4) 80.dp else 150.dp, LocalVisualBox.current.height * (if (visual.word != null) 0.45f else 0.7f)).coerceAtLeast(56.dp))
        val word = visual.word
        if (word != null && visual.hideIndex == null && (!visual.revealWord || state.answered)) {
            // A whole word reads as a word; only a word with a gap is laid out letter by letter.
            Text(word.inCase(prefs.letterCase), color = K.Ink, fontSize = fixedSp(52.dp), fontWeight = FontWeight.Bold, maxLines = 1)
        } else if (word != null && visual.hideIndex != null) {
            val shown = word.inCase(prefs.letterCase)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
                shown.forEachIndexed { index, letter ->
                    val hidden = index == visual.hideIndex && !state.answered
                    Box(
                        modifier = Modifier
                            .widthIn(min = 40.dp)
                            .heightIn(min = 56.dp)
                            .then(if (hidden) Modifier.border(3.dp, K.Race, RoundedCornerShape(10.dp)) else Modifier),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (hidden) "" else letter.toString(),
                            color = if (index == visual.hideIndex && state.answered) K.GoodDeep else K.Ink,
                            fontSize = fixedSp(46.dp),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StoryView(visual: Visual.Story) {
    val text = if (visual.reading) readingString(visual.text, GlyphKind.SENTENCE) else visual.text.str()
    Text(
        text,
        color = K.Ink,
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold, lineHeight = 36.sp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
    )
}

@Composable
private fun ListenView(visual: Visual.Listen, state: VisualState, onListen: (Txt) -> Unit) {
    if (state.speechAvailable) {
        val transition = rememberInfiniteTransition(label = "listen")
        val pulse = transition.animateFloat(1f, 1.18f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "ring")
        val motion = LocalMotion.current
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(170.dp)) {
            Box(
                Modifier
                    .size(130.dp)
                    .scale(if (motion) pulse.value else 1f)
                    .background(K.Reading.copy(alpha = 0.18f), CircleShape),
            )
            RoundIconButton(
                icon = KometIcons.Speaker,
                description = S.listen.str(),
                onClick = { onListen(visual.spoken) },
                size = 112.dp,
                face = K.Reading,
                edge = K.ReadingDeep,
                tint = K.Ink,
            )
        }
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(S.findSame.str(), color = K.InkMuted, style = MaterialTheme.typography.titleMedium)
            GlyphView(visual.fallback, visual.kind)
        }
    }
}

// ── Groups, sharing, balance ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GroupsView(visual: Visual.Groups) {
    val size = if (visual.perGroup > 3 || visual.groups > 3) 26.dp else 32.dp
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(visual.groups) { group ->
            Box(
                modifier = Modifier
                    .background(K.shapes[group % K.shapes.size].copy(alpha = 0.13f), RoundedCornerShape(50))
                    .border(2.dp, K.shapes[group % K.shapes.size].copy(alpha = 0.4f), RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                EmojiGrid(visual.emoji, visual.perGroup, size, perRow = if (visual.perGroup > 4) 3 else 2)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShareView(visual: Visual.Share) {
    val per = visual.total / visual.plates
    val size = if (per > 3) 22.dp else 28.dp
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(visual.plates) {
            Box(
                modifier = Modifier
                    .widthIn(min = 92.dp)
                    .background(Color.White, RoundedCornerShape(50))
                    .border(3.dp, K.PaperShade, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                EmojiGrid(visual.emoji, per, size, perRow = 3)
            }
        }
    }
}

@Composable
private fun BalanceView(visual: Visual.Balance, state: VisualState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { PanTokens(visual.left, state) }
            Spacer(Modifier.width(24.dp))
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { PanTokens(visual.right, state) }
        }
        Canvas(
            Modifier
                .fillMaxWidth()
                .aspectRatio(4.2f),
        ) {
            val w = size.width
            val h = size.height
            val beamY = h * 0.18f
            drawLine(K.Ink, Offset(w * 0.08f, beamY), Offset(w * 0.92f, beamY), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
            for (x in listOf(w * 0.2f, w * 0.8f)) {
                drawLine(K.InkMuted, Offset(x, beamY), Offset(x, beamY - h * 0.1f), strokeWidth = 3.dp.toPx())
            }
            val pivot = Path().apply {
                moveTo(w / 2, beamY)
                lineTo(w / 2 + h * 0.35f, h * 0.9f)
                lineTo(w / 2 - h * 0.35f, h * 0.9f)
                close()
            }
            drawPath(pivot, K.Cards)
            drawLine(K.Ink, Offset(w / 2 - h * 0.6f, h * 0.92f), Offset(w / 2 + h * 0.6f, h * 0.92f), strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
        }
    }
}

@Composable
private fun PanTokens(tokens: List<Token>, state: VisualState) {
    Box(
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(2.dp, K.PaperShade, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            tokens.forEach { token ->
                when (token) {
                    is Token.Num -> Text(token.value.toString(), color = K.Ink, fontSize = fixedSp(40.dp), fontWeight = FontWeight.Black)
                    is Token.Op -> Text(token.symbol, color = K.InkMuted, fontSize = fixedSp(34.dp), fontWeight = FontWeight.Bold)
                    Token.Blank -> BlankSlot(state, 36.dp)
                }
            }
        }
    }
}

// ── Stars for the map and results ──────────────────────────────────────────────────────────────

@Composable
fun StarRow(stars: Int, modifier: Modifier = Modifier, size: Dp = 18.dp) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(3) { index -> StarGlyph(filled = index < stars, modifier = Modifier.size(size)) }
    }
}

/** A small check or cross badge used by the true/false options. */
@Composable
fun VerdictIcon(truth: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(44.dp)
            .background(if (truth) K.Good else K.Bad, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(if (truth) KometIcons.Check else KometIcons.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
    }
}

/** Dashed drop slot used by the word builder. */
@Composable
fun SlotOutline(modifier: Modifier = Modifier, active: Boolean) {
    Canvas(modifier) {
        drawRoundRect(
            color = if (active) K.Race else K.InkMuted.copy(alpha = 0.45f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
            style = Stroke(width = 3.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 9f))),
        )
    }
}
