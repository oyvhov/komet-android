package app.komet.ui.scene

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.progressSemantics
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
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
fun JourneyTrack(done: Int, steps: Int, color: Color, hero: HeroLook, modifier: Modifier = Modifier, gear: Gear = Gear.None) {
    val progress = done.toFloat() / steps
    val motion = app.komet.ui.theme.LocalMotion.current
    val travel = animateFloatAsState(progress, tween(if (motion) 500 else 0), label = "journey")
    BoxWithConstraints(modifier.height(54.dp).progressSemantics(progress), contentAlignment = Alignment.CenterStart) {
        val badge = 34.dp
        val length = (maxWidth - badge).coerceAtLeast(0.dp)
        fun rise(fraction: Float) = (-7).dp * kotlin.math.sin(fraction * kotlin.math.PI).toFloat()
        Canvas(Modifier.fillMaxSize()) {
            fun point(f: Float) = Offset(badge.toPx() / 2 + length.toPx() * f, center.y + rise(f).toPx())
            val route = Path().apply {
                val start = point(0f)
                moveTo(start.x, start.y)
                for (i in 1..32) {
                    val p = point(i / 32f)
                    lineTo(p.x, p.y)
                }
            }
            drawPath(route, K.Faint.copy(alpha = .58f), style = Stroke(2.dp.toPx()))
            for (i in 0 until steps) {
                val stop = point(i.toFloat() / steps)
                val reached = i <= done
                drawCircle(if (reached) color else K.Faint, 4.dp.toPx(), stop)
                if (!reached) drawCircle(K.SpaceTop, 2.dp.toPx(), stop)
            }
        }
        StarGlyph(filled = done == steps, modifier = Modifier.align(Alignment.CenterEnd).size(badge))
        Box(Modifier.offset { IntOffset((length * travel.value).roundToPx(), rise(travel.value).roundToPx()) }.size(badge)) {
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
