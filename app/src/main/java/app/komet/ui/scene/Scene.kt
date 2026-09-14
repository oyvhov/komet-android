package app.komet.ui.scene

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FloatExponentialDecaySpec
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.komet.ui.theme.LocalMotion
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * How a scene sits on the screen. Positions are in scene units: the scene is one unit tall, and a unit
 * is the screen height, except on tall, narrow screens where it shrinks so a portrait phone still shows
 * a good slice of the world. Extra height becomes sky above and ground below.
 */
@Immutable
class SceneMetrics(val widthPx: Float, val heightPx: Float, val density: Float, private val topShare: Float = 0.5f) {
    val unitPx: Float = max(1f, min(heightPx, widthPx * 1.5f))

    /** Visible width in units. */
    val viewport: Float get() = widthPx / unitPx

    /** Visible height in units, never below one. */
    val heightUnits: Float get() = heightPx / unitPx

    /** Units of extra room above the scene band on screens taller than the scene. */
    val top: Float get() = (heightUnits - 1f) * topShare

    /** Screen y in pixels for scene [y]. */
    fun y(y: Float): Float = (y + top) * unitPx

    /** Scene units for a length in dp, for keeping touch targets and text readable. */
    fun unitsOf(dp: Dp): Float = dp.value * density / unitPx

    val unitDp: Dp get() = (unitPx / density).dp

    override fun equals(other: Any?): Boolean =
        other is SceneMetrics && other.widthPx == widthPx && other.heightPx == heightPx && other.density == density && other.topShare == topShare

    override fun hashCode(): Int = (widthPx.hashCode() * 31 + heightPx.hashCode()) * 31 + density.hashCode()
}

/** The camera over a side-scrolling scene. [position] is the left edge of the view in units. */
@Stable
class SceneCamera {
    private var started = false

    var worldWidth by mutableFloatStateOf(1f)
        private set

    var position by mutableFloatStateOf(0f)
        private set

    var viewport by mutableFloatStateOf(1f)
        private set

    private val maxPosition: Float get() = max(0f, worldWidth - viewport)

    fun jumpTo(left: Float) {
        // A world narrower than the screen stays centred.
        position = if (worldWidth <= viewport) (worldWidth - viewport) / 2 else left.coerceIn(0f, maxPosition)
    }

    fun centerOn(x: Float) = jumpTo(x - viewport / 2)

    /** Glides so [x] ends up at [screenFraction] of the view's width (the middle by default). */
    suspend fun glideTo(x: Float, millis: Int = 900, screenFraction: Float = 0.5f) {
        val target = if (worldWidth <= viewport) (worldWidth - viewport) / 2 else (x - viewport * screenFraction).coerceIn(0f, maxPosition)
        animate(position, target, animationSpec = tween(millis, easing = FastOutSlowInEasing)) { value, _ -> position = value }
    }

    internal suspend fun fling(velocity: Float) {
        animateDecay(position, velocity, FloatExponentialDecaySpec(frictionMultiplier = 1.4f, absVelocityThreshold = 0.01f)) { value, _ ->
            jumpTo(value)
        }
    }

    internal fun configure(worldWidth: Float, viewport: Float, startLeft: () -> Float) {
        this.worldWidth = worldWidth
        this.viewport = viewport
        if (!started) {
            started = true
            jumpTo(startLeft())
        } else {
            jumpTo(position)
        }
    }
}

@Composable
fun rememberSceneCamera(key: Any?): SceneCamera = remember(key) { SceneCamera() }

/** Everything a scene needs to place things: the camera and the metrics. */
@Stable
class SceneScope internal constructor(val camera: SceneCamera, val metrics: SceneMetrics, box: BoxScope) : BoxScope by box {

    val unitDp: Dp get() = metrics.unitDp

    /** Screen x in pixels for world [x] on a layer that moves at [parallax] times the camera. */
    fun screenX(x: Float, parallax: Float = 1f): Float = (x - camera.position * parallax) * metrics.unitPx

    fun units(value: Float): Dp = metrics.unitDp * value

    fun units(value: Float, min: Dp, max: Dp): Dp = (metrics.unitDp * value).coerceIn(min, max)

    /**
     * Places a composable so its anchor (fractions of its own size) sits at world ([x], [y]). Only the
     * placement reads the camera, so panning moves things on their own layer without redrawing them.
     */
    fun Modifier.worldAt(x: Float, y: Float, parallax: Float = 1f, anchorX: Float = 0.5f, anchorY: Float = 1f): Modifier =
        worldAt({ Offset(x, y) }, parallax, anchorX, anchorY)

    /** Like [worldAt], for things that move: [position] is read during placement only. */
    fun Modifier.worldAt(position: () -> Offset, parallax: Float = 1f, anchorX: Float = 0.5f, anchorY: Float = 1f): Modifier =
        layout { measurable, constraints ->
            val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
            layout(placeable.width, placeable.height) {
                val at = position()
                val px = screenX(at.x, parallax) - placeable.width * anchorX
                val py = metrics.y(at.y) - placeable.height * anchorY
                placeable.placeWithLayer(IntOffset(px.roundToInt(), py.roundToInt()))
            }
        }
}

/**
 * A full-screen, side-scrolling scene. Drag or fling to look around. [layoutFor] turns the screen
 * metrics into the scene's own layout (so spacing can respect finger sizes), and [worldWidth] reads the
 * width of that layout.
 */
@Composable
fun <L> SceneViewport(
    camera: SceneCamera,
    layoutFor: (SceneMetrics) -> L,
    worldWidth: (L) -> Float,
    /** Where the view's left edge starts, the first time the scene is shown. */
    startLeft: (L) -> Float,
    modifier: Modifier = Modifier,
    /** How much of the extra height on tall screens goes above the scene rather than below it. */
    topShare: Float = 0.5f,
    onUserPan: () -> Unit = {},
    content: @Composable SceneScope.(L) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val motion = remember { arrayOfNulls<Job>(1) }
    val density = LocalDensity.current.density
    BoxWithConstraints(modifier.fillMaxSize().clipToBounds()) {
        val metrics = SceneMetrics(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat(), density, topShare)
        val layout = remember(metrics) { layoutFor(metrics) }
        camera.configure(worldWidth(layout), metrics.viewport) { startLeft(layout) }
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(camera, metrics) {
                    // Watched before the children see the touch: once a finger pans the scene, the
                    // things under it move along with it, so a pan must never end as a tap on them.
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        motion[0]?.cancel()
                        val tracker = VelocityTracker()
                        tracker.addPosition(down.uptimeMillis, down.position)
                        var dragging = false
                        var travel = 0f
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            tracker.addPosition(change.uptimeMillis, change.position)
                            if (!change.pressed) {
                                if (dragging) {
                                    change.consume()
                                    val velocity = -tracker.calculateVelocity().x / metrics.unitPx
                                    motion[0] = scope.launch { camera.fling(velocity) }
                                }
                                break
                            }
                            val dx = change.position.x - change.previousPosition.x
                            if (!dragging) {
                                travel += dx
                                if (abs(travel) > viewConfiguration.touchSlop) {
                                    dragging = true
                                    onUserPan()
                                }
                            }
                            if (dragging) {
                                camera.jumpTo(camera.position - dx / metrics.unitPx)
                                change.consume()
                            }
                        }
                    }
                },
        ) {
            SceneScope(camera, metrics, this).content(layout)
        }
    }
}

/**
 * Seconds since the scene appeared, advancing every frame, for idle animations. Read it only while
 * drawing or placing, so it never recomposes anything. Frozen when the system turns animations off.
 */
@Composable
fun rememberSceneTime(): State<Float> {
    val motion = LocalMotion.current
    val time = remember { mutableFloatStateOf(0f) }
    if (motion) {
        LaunchedEffect(Unit) {
            val start = withFrameNanos { it }
            while (true) {
                withFrameNanos { now -> time.floatValue = (now - start) / 1_000_000_000f }
            }
        }
    }
    return time
}
