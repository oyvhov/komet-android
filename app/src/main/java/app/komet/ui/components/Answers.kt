package app.komet.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.komet.domain.Answer
import app.komet.domain.GlyphKind
import app.komet.domain.Option
import app.komet.domain.inCase
import app.komet.ui.S
import app.komet.ui.theme.K
import app.komet.ui.theme.LocalMotion
import app.komet.ui.theme.LocalReading

enum class OptionLook { NORMAL, WRONG, CORRECT, FADED }

private fun Option.isCompact(): Boolean = when (this) {
    is Option.Label -> kind == GlyphKind.NUMBER || kind == GlyphKind.LETTER || kind == GlyphKind.EXACT ||
        (kind == GlyphKind.PLAIN && text.nn.length <= 7 && text.nb.length <= 7)
    is Option.Shape, is Option.Verdict, is Option.Claps, is Option.Clock, is Option.Picture -> true
}

/** Grid of large answer buttons. Long words get one full-width row each on a phone. */
@Composable
fun ChoiceGrid(
    answer: Answer.Choice,
    looks: List<OptionLook>,
    shakeTarget: Int,
    shakeCount: Int,
    onChoose: (Int) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val count = answer.options.size
        val compact = answer.options.all { it.isCompact() }
        val columns = when {
            !compact && maxWidth < 560.dp -> 1
            !compact -> minOf(count, 2)
            count == 4 && maxWidth < 560.dp -> 2
            answer.options.first() is Option.Picture && count == 4 -> 2
            else -> count
        }
        val spacing = 12.dp
        Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
            answer.options.indices.chunked(columns).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    row.forEach { index ->
                        OptionButton(
                            option = answer.options[index],
                            look = looks[index],
                            onClick = { onChoose(index) },
                            enabled = enabled && looks[index] == OptionLook.NORMAL,
                            modifier = Modifier
                                .weight(1f)
                                .shake(if (shakeTarget == index) shakeCount else 0, shakeTarget == index),
                        )
                    }
                    repeat(columns - row.size) { Box(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
fun OptionButton(option: Option, look: OptionLook, onClick: () -> Unit, enabled: Boolean, modifier: Modifier = Modifier) {
    val (face, edge) = when (look) {
        OptionLook.NORMAL, OptionLook.FADED -> K.Key to K.KeyEdge
        OptionLook.WRONG -> Color(0xFFFFE3E3) to Color(0xFFE9A9A9)
        OptionLook.CORRECT -> K.Good to K.GoodDeep
    }
    val minHeight = when (option) {
        is Option.Picture -> 118.dp
        is Option.Clock -> 128.dp
        is Option.Shape -> 104.dp
        else -> 96.dp
    }
    PressSurface(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = minHeight)
            .alpha(if (look == OptionLook.FADED) 0.45f else 1f),
        face = face,
        edge = edge,
        enabled = enabled || look != OptionLook.NORMAL,
        tapSound = false,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
    ) {
        val ink = if (look == OptionLook.CORRECT) Color.White else if (look == OptionLook.WRONG) K.BadDeep else K.Ink
        OptionContent(option, ink)
    }
}

@Composable
fun OptionContent(option: Option, ink: Color) {
    when (option) {
        is Option.Label -> {
            val text = readingString(option.text, option.kind)
            val size: Dp = when (option.kind) {
                GlyphKind.NUMBER -> if (text.length > 3) 34.dp else 50.dp
                GlyphKind.LETTER, GlyphKind.EXACT -> 56.dp
                GlyphKind.WORD -> 32.dp
                GlyphKind.SENTENCE_WORD, GlyphKind.SENTENCE -> 26.dp
                GlyphKind.PLAIN -> 26.dp
            }
            Text(
                text,
                color = ink,
                fontSize = if (option.kind == GlyphKind.NUMBER || option.kind == GlyphKind.LETTER || option.kind == GlyphKind.EXACT) fixedSp(size) else MaterialTheme.typography.headlineMedium.fontSize,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
        }
        is Option.Picture -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(option.emoji, fontSize = fixedSp(if (option.caption == null) 62.dp else 50.dp))
            option.caption?.let {
                Text(
                    it.inCase(LocalReading.current.letterCase),
                    color = ink,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                )
            }
        }
        is Option.Shape -> ShapeView(option.item, Modifier.size(72.dp))
        is Option.Clock -> ClockFace(option.hour, option.minute, Modifier.size(104.dp), numerals = false)
        is Option.Verdict -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            VerdictIcon(option.truth)
            Text(
                (if (option.truth) S.trueLabel else S.falseLabel).str(),
                color = ink,
                style = MaterialTheme.typography.headlineMedium,
            )
        }
        is Option.Claps -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(option.count.toString(), color = ink, fontSize = fixedSp(38.dp), fontWeight = FontWeight.Black)
            Text("👏".repeat(option.count), fontSize = fixedSp(18.dp))
        }
    }
}

/** Text of the right answer, for the reveal banner and the blank in an equation. */
@Composable
fun answerText(answer: Answer): String? = when (answer) {
    is Answer.NumberInput -> answer.correct.toString()
    is Answer.Choice -> when (val option = answer.options[answer.correct]) {
        is Option.Label -> readingString(option.text, option.kind)
        is Option.Picture -> option.caption?.inCase(LocalReading.current.letterCase)
        is Option.Verdict -> (if (option.truth) S.trueLabel else S.falseLabel).str()
        is Option.Claps -> option.count.toString()
        is Option.Shape, is Option.Clock -> null
    }
    is Answer.Build -> {
        val prefs = LocalReading.current
        if (answer.kind == GlyphKind.LETTER) {
            answer.target.joinToString("").inCase(prefs.letterCase)
        } else {
            answer.target.joinToString(" ") { prefs.render(app.komet.domain.txt(it), answer.kind) }
        }
    }
}

// ── Keypad ─────────────────────────────────────────────────────────────────────────────────────

@Composable
fun AnswerDisplay(value: String, answered: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .widthIn(min = 150.dp)
            .heightIn(min = 72.dp)
            .background(if (answered) K.Good.copy(alpha = 0.2f) else K.Paper, RoundedCornerShape(20.dp))
            .border(3.dp, if (answered) K.Good else K.Race.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(value.ifEmpty { "?" }, color = if (answered) K.GoodDeep else if (value.isEmpty()) K.Race else K.Ink, fontSize = fixedSp(44.dp), fontWeight = FontWeight.Black)
    }
}

@Composable
fun Keypad(
    onDigit: (Int) -> Unit,
    onErase: () -> Unit,
    onSubmit: () -> Unit,
    canSubmit: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    shakeCount: Int = 0,
) {
    val rows = listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9), listOf(-1, 0, -2))
    Column(modifier.shake(shakeCount, true), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { key ->
                    val keyModifier = Modifier
                        .weight(1f)
                        .heightIn(min = 64.dp)
                    when (key) {
                        -1 -> PressSurface(
                            onClick = onErase,
                            modifier = keyModifier,
                            face = K.SurfaceHigh,
                            edge = K.SurfaceLow,
                            enabled = enabled,
                            description = S.erase.str(),
                            contentPadding = PaddingValues(6.dp),
                        ) { Icon(KometIcons.Backspace, contentDescription = null, tint = K.Text, modifier = Modifier.size(30.dp)) }
                        -2 -> PressSurface(
                            onClick = onSubmit,
                            modifier = keyModifier,
                            face = K.Gold,
                            edge = K.GoldDeep,
                            enabled = enabled && canSubmit,
                            tapSound = false,
                            description = S.confirm.str(),
                            contentPadding = PaddingValues(6.dp),
                        ) { Icon(KometIcons.Check, contentDescription = null, tint = K.Ink, modifier = Modifier.size(34.dp)) }
                        else -> PressSurface(
                            onClick = { onDigit(key) },
                            modifier = keyModifier,
                            face = K.Key,
                            edge = K.KeyEdge,
                            enabled = enabled,
                            contentPadding = PaddingValues(6.dp),
                        ) { Text(key.toString(), color = K.Ink, fontSize = fixedSp(34.dp), fontWeight = FontWeight.Black) }
                    }
                }
            }
        }
    }
}

// ── Word builder ───────────────────────────────────────────────────────────────────────────────

/** The slots being filled. Drawn on the paper card, under the picture, so the word grows where the child looks. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BuildSlots(answer: Answer.Build, placed: List<Int>, revealed: Boolean, modifier: Modifier = Modifier) {
    val letters = answer.kind == GlyphKind.LETTER
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        answer.target.forEachIndexed { position, expected ->
            val filled = when {
                revealed -> expected
                position < placed.size -> answer.tiles[placed[position]]
                else -> null
            }
            val active = !revealed && position == placed.size
            Box(
                modifier = Modifier
                    .widthIn(min = if (letters) 58.dp else 76.dp)
                    .heightIn(min = 68.dp)
                    .background(
                        when {
                            revealed -> K.Reveal.copy(alpha = 0.28f)
                            filled != null -> K.Good.copy(alpha = 0.22f)
                            else -> K.PaperShade
                        },
                        RoundedCornerShape(14.dp),
                    )
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (filled == null) SlotOutline(Modifier.matchParentSize(), active)
                Text(
                    filled?.let { readingString(app.komet.domain.txt(it), answer.kind) } ?: "",
                    color = K.Ink,
                    fontSize = if (letters) fixedSp(42.dp) else MaterialTheme.typography.headlineSmall.fontSize,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

/** The tiles to tap. After three misses the next right tile pulses in gold. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BuildTiles(
    answer: Answer.Build,
    placed: List<Int>,
    misses: Int,
    shakeTarget: Int,
    shakeCount: Int,
    enabled: Boolean,
    onTile: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val letters = answer.kind == GlyphKind.LETTER
    val nextExpected = answer.target.getOrNull(placed.size)
    val helpTile = if (misses >= 3 && nextExpected != null) {
        answer.tiles.indices.firstOrNull { it !in placed && answer.tiles[it] == nextExpected }
    } else {
        null
    }
    val transition = rememberInfiniteTransition(label = "help")
    val pulse = transition.animateFloat(1f, 1.08f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "helpPulse")
    val motion = LocalMotion.current
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        answer.tiles.forEachIndexed { index, tile ->
            val used = index in placed
            val help = index == helpTile
            PressSurface(
                onClick = { onTile(index) },
                modifier = Modifier
                    .widthIn(min = if (letters) 64.dp else 80.dp)
                    .heightIn(min = 72.dp)
                    .alpha(if (used) 0f else 1f)
                    .scale(if (help && motion) pulse.value else 1f)
                    .shake(if (shakeTarget == index) shakeCount else 0, shakeTarget == index)
                    .then(if (help) Modifier.border(3.dp, K.Gold, RoundedCornerShape(22.dp)) else Modifier),
                face = K.Key,
                edge = K.KeyEdge,
                enabled = enabled && !used,
                tapSound = false,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    readingString(app.komet.domain.txt(tile), answer.kind),
                    color = K.Ink,
                    fontSize = if (letters) fixedSp(38.dp) else MaterialTheme.typography.headlineSmall.fontSize,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}
