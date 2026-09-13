package app.komet.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import app.komet.domain.GlyphKind
import app.komet.domain.LetterCase
import app.komet.domain.Txt
import app.komet.domain.inCase
import app.komet.domain.upperNo
import app.komet.ui.theme.LocalMotion
import app.komet.ui.theme.LocalReading
import app.komet.ui.theme.ReadingPrefs

/** Resolves interface text in the active profile's written standard. */
@Composable
@ReadOnlyComposable
fun Txt.str(): String = get(LocalReading.current.maalform)

/** Resolves task text and applies the profile's letter case where the kind asks for it. */
@Composable
@ReadOnlyComposable
fun readingString(text: Txt, kind: GlyphKind): String = LocalReading.current.render(text, kind)

fun ReadingPrefs.render(text: Txt, kind: GlyphKind): String {
    val raw = text.get(maalform)
    return when (kind) {
        GlyphKind.LETTER, GlyphKind.WORD -> raw.inCase(letterCase)
        GlyphKind.SENTENCE -> raw.inCase(letterCase, sentence = true)
        GlyphKind.SENTENCE_WORD -> if (letterCase == LetterCase.UPPER) raw.upperNo() else raw
        GlyphKind.NUMBER, GlyphKind.EXACT, GlyphKind.PLAIN -> raw
    }
}

/**
 * A text size that renders at [size] regardless of the system font scale. Only for things that are
 * really pictures — emoji, clock numerals, the big digits of a task — never for sentences.
 */
@Composable
@ReadOnlyComposable
fun fixedSp(size: Dp): TextUnit = with(LocalDensity.current) { size.toSp() }

/** Shakes sideways each time [trigger] changes while [active] is true. */
@Composable
fun Modifier.shake(trigger: Int, active: Boolean): Modifier {
    val offset = remember { Animatable(0f) }
    val motion = LocalMotion.current
    LaunchedEffect(trigger) {
        if (trigger > 0 && active && motion) {
            for (x in listOf(-14f, 12f, -9f, 6f, -3f, 0f)) offset.animateTo(x, tween(45))
        }
    }
    return this.offset { IntOffset(offset.value.dp.roundToPx(), 0) }
}
