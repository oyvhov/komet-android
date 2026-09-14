package app.komet.ui.theme

import android.app.Activity
import android.provider.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import app.komet.R
import app.komet.domain.GlyphKind
import app.komet.domain.LetterCase
import app.komet.domain.Maalform

/**
 * Komet's palette. Deep space behind everything, warm paper for the task itself, and one strong
 * colour per world so a child learns where they are from colour alone.
 */
object K {
    val SpaceTop = Color(0xFF060A24)
    val SpaceBottom = Color(0xFF141A4E)
    val Nebula = Color(0xFF3A2F9A)

    val Surface = Color(0xFF1A2361)
    val SurfaceHigh = Color(0xFF28327D)
    val SurfaceLow = Color(0xFF0F153E)
    val Line = Color(0xFF36428F)

    /** The dark rim around game text, buttons and stars. */
    val Outline = Color(0xFF070A22)

    val Text = Color(0xFFF7F8FF)
    val Muted = Color(0xFFB4BCEB)
    val Faint = Color(0xFF7F88C0)

    val Paper = Color(0xFFFFFBF3)
    val PaperShade = Color(0xFFF1EADB)
    val Ink = Color(0xFF1B1744)
    val InkMuted = Color(0xFF5D5A84)

    val Gold = Color(0xFFFFC21A)
    val GoldTop = Color(0xFFFFE27A)
    val GoldDeep = Color(0xFFC27400)

    // Each world has a deep, saturated face, a light top for the gloss and a dark edge underneath.
    val Math = Color(0xFFF26A0F)
    val MathTop = Color(0xFFFFB347)
    val MathDeep = Color(0xFFA9420A)
    val Reading = Color(0xFF1684E6)
    val ReadingTop = Color(0xFF52D5FF)
    val ReadingDeep = Color(0xFF0B4FA8)
    val Race = Color(0xFFE02A50)
    val RaceTop = Color(0xFFFF7D8C)
    val RaceDeep = Color(0xFF951636)
    // Space cards are treasure, so they are gold; the violet belongs to the Space world.
    val Cards = Color(0xFFE39400)
    val CardsTop = Color(0xFFFFD34E)
    val CardsDeep = Color(0xFF955100)
    val Cosmos = Color(0xFF6C3FF0)
    val CosmosTop = Color(0xFFB38FFF)
    val CosmosDeep = Color(0xFF4222B0)

    val English = Color(0xFF23A049)
    val EnglishTop = Color(0xFF6EE58E)
    val EnglishDeep = Color(0xFF12692E)

    val Explore = Color(0xFF0FA982)
    val ExploreTop = Color(0xFF5CEBC4)
    val ExploreDeep = Color(0xFF08684F)

    val Good = Color(0xFF28CC5E)
    val GoodTop = Color(0xFF7DF29B)
    val GoodDeep = Color(0xFF14853A)
    val Bad = Color(0xFFFF5A64)
    val BadDeep = Color(0xFFB8303D)
    val Reveal = Color(0xFFFFB547)

    val Key = Color(0xFFFFFFFF)
    val KeyEdge = Color(0xFFC9CEEF)

    /** Colours for shapes and patterns. Chosen to stay distinct for common colour-vision deficiencies. */
    val shapes = listOf(
        Color(0xFFFF5D5D),
        Color(0xFFFFB02E),
        Color(0xFF2FBF71),
        Color(0xFF3D8BFF),
        Color(0xFF9B6BFF),
        Color(0xFFFF6FC0),
    )
}

/**
 * Andika by SIL International (SIL Open Font License 1.1, see assets/licenses) for everything a child
 * reads in a task: single-storey a and g as taught in school, and letters that are hard to mix up.
 * Interface chrome and maths digits keep the system font.
 */
val ReadingFont = FontFamily(
    Font(R.font.andika_regular, FontWeight.Normal),
    Font(R.font.andika_bold, FontWeight.Bold),
)

/** The font for a piece of task text: letters, words and sentences are reading material. */
fun GlyphKind.fontFamily(): FontFamily? = when (this) {
    GlyphKind.LETTER, GlyphKind.WORD, GlyphKind.SENTENCE, GlyphKind.SENTENCE_WORD, GlyphKind.EXACT -> ReadingFont
    GlyphKind.NUMBER, GlyphKind.PLAIN -> null
}

/** Profile-level reading preferences that every task renderer needs. */
data class ReadingPrefs(val maalform: Maalform, val letterCase: LetterCase)

val LocalReading = staticCompositionLocalOf { ReadingPrefs(Maalform.NYNORSK, LetterCase.UPPER) }

/** Read once at the root: a binder call per animated element would be wasteful. */
val LocalMotion = compositionLocalOf { true }

/** Room the task picture may use, so small counts get big pictures and large counts still fit. */
val LocalVisualBox = compositionLocalOf { androidx.compose.ui.unit.DpSize(360.dp, 420.dp) }

private val kometColors = darkColorScheme(
    primary = K.Gold,
    onPrimary = K.Ink,
    primaryContainer = K.SurfaceHigh,
    onPrimaryContainer = K.Text,
    secondary = K.Reading,
    onSecondary = K.Ink,
    tertiary = K.Math,
    background = K.SpaceTop,
    onBackground = K.Text,
    surface = K.Surface,
    onSurface = K.Text,
    surfaceVariant = K.SurfaceHigh,
    onSurfaceVariant = K.Muted,
    surfaceContainer = K.Surface,
    surfaceContainerHigh = K.SurfaceHigh,
    surfaceContainerHighest = K.SurfaceHigh,
    surfaceContainerLow = K.SurfaceLow,
    surfaceContainerLowest = K.SpaceTop,
    outline = K.Line,
    outlineVariant = K.Line,
    error = K.Bad,
    onError = K.Ink,
)

private val sans = FontFamily.SansSerif

private val kometTypography = Typography(
    displayLarge = TextStyle(fontFamily = sans, fontWeight = FontWeight.Black, fontSize = 56.sp, lineHeight = 60.sp, letterSpacing = (-1).sp),
    displayMedium = TextStyle(fontFamily = sans, fontWeight = FontWeight.Black, fontSize = 44.sp, lineHeight = 50.sp, letterSpacing = (-0.8).sp),
    displaySmall = TextStyle(fontFamily = sans, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.6).sp),
    headlineLarge = TextStyle(fontFamily = sans, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = (-0.4).sp),
    headlineMedium = TextStyle(fontFamily = sans, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = (-0.3).sp),
    headlineSmall = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 22.sp),
    labelMedium = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 18.sp),
    labelSmall = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp),
)

@Composable
fun KometTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }
    val context = LocalContext.current
    val motion = remember {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
        }.getOrDefault(true)
    }
    CompositionLocalProvider(LocalMotion provides motion) {
        MaterialTheme(colorScheme = kometColors, typography = kometTypography, content = content)
    }
}
