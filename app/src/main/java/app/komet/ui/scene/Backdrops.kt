package app.komet.ui.scene

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.komet.domain.HeroLook
import app.komet.domain.Subject
import app.komet.ui.components.StarGlyph
import app.komet.ui.theme.K
import kotlin.random.Random

/**
 * The planet of [subject] as a calm, still backdrop for screens that are not scenes, such as a round of
 * tasks: its sky and a line of hills along the bottom, dimmed so text and answers stay easy to read.
 */
@Composable
fun SubjectBackdrop(subject: Subject, modifier: Modifier = Modifier) {
    val biome = Biomes.of(subject)
    val density = LocalDensity.current.density
    Box(
        modifier
            .fillMaxSize()
            .drawWithCache {
                val w = size.width
                val h = size.height
                val unit = SceneMetrics(w, h, density).unitPx
                fun hills(base: Float, amplitude: Float, kind: Ridge, seed: Int, stretch: Float): Path = Path().apply {
                    moveTo(0f, h)
                    var x = 0f
                    while (x <= w) {
                        lineTo(x, h * base - amplitude * unit * ridge(kind, x / unit * stretch, seed))
                        x += 6f * density
                    }
                    lineTo(w, h)
                    close()
                }
                val far = hills(0.9f, 0.16f, biome.farRidge, biome.seed, 1.4f)
                val mid = hills(0.95f, 0.08f, biome.midRidge, biome.seed + 7, 1.1f)
                val random = Random(biome.seed)
                val stars = List(70) { Offset(random.nextFloat() * w, random.nextFloat() * h * 0.6f) to (0.6f + random.nextFloat() * 1.2f) }
                val sky = Brush.verticalGradient(0f to biome.skyTop, 0.6f to biome.skyMid, 1f to lerp(biome.skyHorizon, biome.skyMid, 0.35f))
                val shade = Brush.verticalGradient(0f to K.SpaceTop.copy(alpha = 0.35f), 0.5f to K.SpaceTop.copy(alpha = 0.2f), 1f to K.SpaceTop.copy(alpha = 0.5f))
                onDrawBehind {
                    drawRect(sky)
                    // No moons or planets here: the sky stays calm behind the task.
                    for ((point, radius) in stars) drawCircle(Color.White.copy(alpha = 0.55f), radius * density, point)
                    drawPath(far, lerp(biome.far, K.SpaceTop, 0.25f))
                    drawPath(mid, lerp(biome.mid, biome.groundBottom, 0.45f))
                    drawRect(shade)
                }
            },
    )
}

/**
 * Progress through a round as a journey: the track fills in the world's colour, the astronaut's
 * helmet travels along it, and a gold star waits at the end.
 */
@Composable
fun JourneyTrack(progress: Float, color: Color, hero: HeroLook, modifier: Modifier = Modifier, height: Dp = 16.dp, gear: Gear = Gear.None) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(600), label = "journey")
    BoxWithConstraints(modifier.height(40.dp), contentAlignment = Alignment.CenterStart) {
        val badge = 36.dp
        val trackWidth = maxWidth - 20.dp
        Box(
            Modifier
                .padding(end = 20.dp)
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(50))
                .background(Brush.verticalGradient(listOf(lerp(K.SurfaceHigh, Color.Black, 0.35f), K.SurfaceHigh)))
                .border(1.dp, K.Outline.copy(alpha = 0.5f), RoundedCornerShape(50)),
        ) {
            if (animated > 0f) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animated)
                        .clip(RoundedCornerShape(50))
                        .background(Brush.verticalGradient(listOf(lerp(color, Color.White, 0.35f), color, lerp(color, Color.Black, 0.15f)))),
                )
            }
        }
        StarGlyph(filled = animated >= 1f, modifier = Modifier.align(Alignment.CenterEnd).size(30.dp))
        Box(
            Modifier
                .offset { IntOffset(((trackWidth - badge) * animated).roundToPx(), 0) }
                .size(badge),
        ) {
            HeroBadge(hero, size = badge, gear = gear)
        }
    }
}

/** Bolt reacting to an answer, for small spaces such as the feedback banner. */
@Composable
fun BoltReaction(correct: Boolean, modifier: Modifier = Modifier, paint: String? = null) {
    val time = rememberSceneTime()
    Canvas(modifier) {
        drawBolt(if (correct) BoltMood.HAPPY else BoltMood.TALK, time.value, paint)
    }
}
