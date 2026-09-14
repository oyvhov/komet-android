package app.komet.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import app.komet.domain.Subject
import app.komet.ui.theme.K

/**
 * The colours of one filled game surface: a light [top] for the gloss, the [face] itself, the darker
 * [edge] it rests on, and an [accent] bright enough for text on the dark background.
 */
data class Tone(val top: Color, val face: Color, val edge: Color, val accent: Color = top)

object Tones {
    val Math = Tone(K.MathTop, K.Math, K.MathDeep)
    val Reading = Tone(K.ReadingTop, K.Reading, K.ReadingDeep)
    val Race = Tone(K.RaceTop, K.Race, K.RaceDeep)
    val Cards = Tone(K.CardsTop, K.Cards, K.CardsDeep)
    val Gold = Tone(K.GoldTop, K.Gold, K.GoldDeep)
    val Good = Tone(K.GoodTop, K.Good, K.GoodDeep)
    val Explore = Tone(K.ExploreTop, K.Explore, K.ExploreDeep)
    val English = Tone(K.EnglishTop, K.English, K.EnglishDeep)
    val Space = Tone(K.CosmosTop, K.Cosmos, K.CosmosDeep)
    val Dark = Tone(K.SurfaceHigh, K.Surface, K.SurfaceLow, K.Text)
}

fun subjectTone(subject: Subject): Tone = when (subject) {
    Subject.MATH -> Tones.Math
    Subject.READING -> Tones.Reading
    Subject.ENGLISH -> Tones.English
    Subject.SPACE -> Tones.Space
}

/**
 * Heavy lettering with a dark rim and a drop underneath, the way titles and buttons are lettered in
 * games. The rim scales with the text so small labels stay crisp and big titles look solid.
 */
@Composable
fun GameText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    color: Color = Color.White,
    outline: Color = K.Outline,
    fontSize: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    /** Shrink the text until it fits instead of cutting it off; for one-word titles in fixed tiles. */
    autoFit: Boolean = false,
) {
    val size = when {
        fontSize.isSpecified -> fontSize
        style.fontSize.isSpecified -> style.fontSize
        else -> 20.sp
    }
    val density = LocalDensity.current
    val sizePx = with(density) { size.toPx() }
    val rim = (sizePx * 0.15f).coerceIn(with(density) { 2.5.dp.toPx() }, with(density) { 9.dp.toPx() })
    val drop = rim * 0.55f
    // A smaller size than the style's (for example a capped one) keeps the style's line spacing in proportion.
    val lineHeight = if (style.lineHeight.isSp && style.fontSize.isSp && size.isSp) (size.value * style.lineHeight.value / style.fontSize.value).sp else style.lineHeight
    val base = style.merge(
        TextStyle(
            fontSize = size,
            lineHeight = lineHeight,
            fontWeight = style.fontWeight ?: FontWeight.Black,
            textAlign = textAlign ?: style.textAlign,
        ),
    )
    val fill = if (textAlign != null && textAlign != TextAlign.Start) Modifier.fillMaxWidth() else Modifier
    // The smallest size is fixed on screen, so shrinking still works when the system text is very big.
    val smallest = with(density) { 10.dp.toSp() }.let { if (it.value > size.value) size else it }
    val fit = if (autoFit) TextAutoSize.StepBased(minFontSize = smallest, maxFontSize = size, stepSize = 1.sp) else null
    Box(modifier, contentAlignment = Alignment.TopStart) {
        BasicText(
            text,
            style = base.copy(color = outline, drawStyle = Stroke(width = rim, join = StrokeJoin.Round), shadow = Shadow(outline, Offset(0f, drop), 0f)),
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            autoSize = fit,
            modifier = Modifier.matchParentSize(),
        )
        BasicText(
            text,
            style = base.copy(color = color),
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            autoSize = fit,
            modifier = if (autoFit) fill.then(Modifier.fillMaxWidth()) else fill,
        )
    }
}
