package app.komet.ui.scene

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import app.komet.domain.Subject
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

enum class SkyObject { RINGED_PLANET, AURORA, MOONS, EARTH }

enum class Ridge { MESAS, PEAKS, HILLS, DUNES }

/** The look of a planet's surface: colours, the shape of its mountains and what grows there. */
@Immutable
class Biome(
    val skyTop: Color,
    val skyMid: Color,
    val skyHorizon: Color,
    val far: Color,
    val mid: Color,
    val groundTop: Color,
    val groundBottom: Color,
    val path: Color,
    val pathEdge: Color,
    val ink: PropInk,
    val sky: SkyObject,
    val farRidge: Ridge,
    val midRidge: Ridge,
    /** Big things standing along the horizon behind the path. */
    val backProps: List<PropKind>,
    /** Small things in front of the path. */
    val frontProps: List<PropKind>,
    val seed: Int,
)

object Biomes {
    /** Talplaneten: a warm canyon at sunset, full of glowing crystals. */
    val math = Biome(
        skyTop = Color(0xFF14083C),
        skyMid = Color(0xFF55206E),
        skyHorizon = Color(0xFFF0703E),
        far = Color(0xFF6A2A68),
        mid = Color(0xFF9C3A4E),
        groundTop = Color(0xFFE88A30),
        groundBottom = Color(0xFF7E3212),
        path = Color(0xFFFFD796),
        pathEdge = Color(0xFF6A260C),
        ink = PropInk(leaf = Color(0xFF3FAE6A), bark = Color(0xFF6B3A1E), stone = Color(0xFFC0643A), accent = Color(0xFFFFC21A)),
        sky = SkyObject.RINGED_PLANET,
        farRidge = Ridge.MESAS,
        midRidge = Ridge.DUNES,
        backProps = listOf(PropKind.CRYSTALS, PropKind.CACTUS, PropKind.DOME, PropKind.ROCK, PropKind.CRYSTALS, PropKind.LAMP, PropKind.CACTUS),
        frontProps = listOf(PropKind.ROCK, PropKind.CRYSTALS, PropKind.GRASS),
        seed = 11,
    )

    /** Bokstavplaneten: a blue forest at night with glowing mushrooms and northern lights. */
    val reading = Biome(
        skyTop = Color(0xFF040A2C),
        skyMid = Color(0xFF0D2C72),
        skyHorizon = Color(0xFF2289C9),
        far = Color(0xFF15407C),
        mid = Color(0xFF0E5586),
        groundTop = Color(0xFF1B86A8),
        groundBottom = Color(0xFF0A2C5A),
        path = Color(0xFFB9F2FF),
        pathEdge = Color(0xFF062848),
        ink = PropInk(leaf = Color(0xFF4F7CF0), bark = Color(0xFF3A2A6A), stone = Color(0xFF4E7FA8), accent = Color(0xFF52D5FF)),
        sky = SkyObject.AURORA,
        farRidge = Ridge.PEAKS,
        midRidge = Ridge.HILLS,
        backProps = listOf(PropKind.MUSHROOM, PropKind.PINE, PropKind.TREE, PropKind.LAMP, PropKind.MUSHROOM, PropKind.DOME, PropKind.PINE),
        frontProps = listOf(PropKind.GRASS, PropKind.MUSHROOM, PropKind.ROCK),
        seed = 23,
    )

    /** Engelskplaneten: green hills in the last golden light, with two moons rising. */
    val english = Biome(
        skyTop = Color(0xFF081B3E),
        skyMid = Color(0xFF1B557E),
        skyHorizon = Color(0xFFE8B34A),
        far = Color(0xFF285F6E),
        mid = Color(0xFF217A55),
        groundTop = Color(0xFF4BB553),
        groundBottom = Color(0xFF125E2C),
        path = Color(0xFFF4E3A8),
        pathEdge = Color(0xFF0E3A1E),
        ink = PropInk(leaf = Color(0xFF2F9E4A), bark = Color(0xFF6B4424), stone = Color(0xFF8C9AA8), accent = Color(0xFFFFE27A)),
        sky = SkyObject.MOONS,
        farRidge = Ridge.HILLS,
        midRidge = Ridge.HILLS,
        backProps = listOf(PropKind.TREE, PropKind.DOME, PropKind.TREE, PropKind.FLOWERS, PropKind.PINE, PropKind.LAMP, PropKind.TREE),
        frontProps = listOf(PropKind.FLOWERS, PropKind.GRASS, PropKind.ROCK),
        seed = 31,
    )

    /** Rombasen: a violet moon with a base, dishes and the Earth in the sky. */
    val space = Biome(
        skyTop = Color(0xFF02020F),
        skyMid = Color(0xFF161048),
        skyHorizon = Color(0xFF4F3DAE),
        far = Color(0xFF2C2268),
        mid = Color(0xFF41358A),
        groundTop = Color(0xFF9087CC),
        groundBottom = Color(0xFF30266A),
        path = Color(0xFFE6E0FF),
        pathEdge = Color(0xFF1C144C),
        ink = PropInk(leaf = Color(0xFF7B5CF0), bark = Color(0xFF3A3070), stone = Color(0xFF7A70B8), accent = Color(0xFFB38FFF)),
        sky = SkyObject.EARTH,
        farRidge = Ridge.PEAKS,
        midRidge = Ridge.DUNES,
        backProps = listOf(PropKind.DISH, PropKind.ROCK, PropKind.DOME, PropKind.ANTENNA, PropKind.CRYSTALS, PropKind.ROCK, PropKind.LAMP),
        frontProps = listOf(PropKind.ROCK, PropKind.CRYSTALS, PropKind.ROCK),
        seed = 43,
    )

    fun of(subject: Subject): Biome = when (subject) {
        Subject.MATH -> math
        Subject.READING -> reading
        Subject.ENGLISH -> english
        Subject.SPACE -> space
    }
}

/** Deterministic smooth noise, so a planet looks the same every visit. */
internal object Noise {
    private fun hash(i: Int, seed: Int): Float {
        var h = i * 374761393 + seed * 668265263
        h = (h xor (h ushr 13)) * 1274126177
        h = h xor (h ushr 16)
        return (h and 0xFFFF) / 65535f
    }

    fun smooth(x: Float, seed: Int): Float {
        val i = floor(x).toInt()
        val f = x - i
        val u = f * f * (3 - 2 * f)
        val a = hash(i, seed)
        return a + (hash(i + 1, seed) - a) * u
    }

    fun fbm(x: Float, seed: Int): Float = smooth(x, seed) * 0.57f + smooth(x * 2.03f, seed + 1) * 0.29f + smooth(x * 4.1f, seed + 2) * 0.14f
}

private fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
    val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
    return t * t * (3 - 2 * t)
}

/** Height of a ridge at [x], from 0 (low) to about 1 (tall). */
private fun ridge(kind: Ridge, x: Float, seed: Int): Float = when (kind) {
    Ridge.HILLS -> Noise.fbm(x * 1.5f, seed)
    Ridge.PEAKS -> {
        val sharp = 1f - abs(Noise.smooth(x * 3.1f, seed + 5) * 2f - 1f)
        Noise.fbm(x * 1.8f, seed) * 0.55f + sharp * sharp * 0.5f
    }
    Ridge.MESAS -> {
        val n = Noise.fbm(x * 1.2f, seed)
        smoothstep(0.44f, 0.52f, n) * 0.72f + Noise.smooth(x * 9f, seed + 3) * 0.06f + n * 0.2f
    }
    Ridge.DUNES -> {
        val p = x * 2.4f + 0.9f * sin(x * 2.4f)
        (0.5f + 0.5f * sin(p)) * 0.6f + Noise.fbm(x * 1.1f, seed) * 0.4f
    }
}

/** Where the path runs, in scene units. Shared by the landscape and everything standing on it. */
object PlanetGround {
    fun pathY(x: Float): Float = 0.665f + 0.065f * sin(1.7f * x + 0.6f) + 0.026f * sin(4.1f * x + 1.9f)
    fun horizon(x: Float): Float = 0.47f + 0.02f * sin(2.3f * x + 0.3f) + 0.011f * sin(5.7f * x + 2.2f)
}

/** A prop placed in the world: x and foot y in units, height in units. */
@Immutable
class PropSpot(val kind: PropKind, val x: Float, val y: Float, val height: Float, val variant: Int, val mirror: Boolean)

/** The pixel paths of a planet's landscape, built once for a screen size. */
class PlanetGeometry(
    val farRidge: Path,
    val midRidge: Path,
    val ground: Path,
    val road: Path,
    val midProps: List<PropSpot>,
    val backProps: List<PropSpot>,
    val frontProps: List<PropSpot>,
    val foreground: List<PropSpot>,
    val stars: List<Offset>,
)

private const val FAR = 0.22f
private const val MID = 0.5f
private const val FORE = 1.35f

/**
 * Builds the landscape for a world [width] units wide, from [roadStart] to [roadEnd], leaving room
 * around [keepClear] (the stations) so nothing hides a level.
 */
fun buildPlanetGeometry(biome: Biome, metrics: SceneMetrics, width: Float, roadStart: Float, roadEnd: Float, keepClear: List<Float>): PlanetGeometry {
    val u = metrics.unitPx
    val viewport = metrics.viewport
    val minCamera = min(0f, (width - viewport) / 2)
    val maxCamera = max(0f, width - viewport)
    val bottom = metrics.heightPx + 4f
    fun range(parallax: Float) = (minCamera * parallax - 0.6f)..(maxCamera * parallax + viewport + 0.6f)

    fun ridgePath(kind: Ridge, parallax: Float, base: Float, amplitude: Float, seed: Int): Path {
        val span = range(parallax)
        return Path().apply {
            moveTo(span.start * u, bottom)
            var x = span.start
            while (x <= span.endInclusive) {
                lineTo(x * u, metrics.y(base - amplitude * ridge(kind, x / parallax.coerceAtLeast(0.2f) * 0.5f, seed)))
                x += 0.015f
            }
            lineTo(span.endInclusive * u, bottom)
            close()
        }
    }

    val far = ridgePath(biome.farRidge, FAR, base = 0.47f, amplitude = 0.26f, seed = biome.seed)
    val mid = ridgePath(biome.midRidge, MID, base = 0.5f, amplitude = 0.13f, seed = biome.seed + 7)

    val groundSpan = range(1f)
    val ground = Path().apply {
        moveTo(groundSpan.start * u, bottom)
        var x = groundSpan.start
        while (x <= groundSpan.endInclusive) {
            lineTo(x * u, metrics.y(PlanetGround.horizon(x)))
            x += 0.02f
        }
        lineTo(groundSpan.endInclusive * u, bottom)
        close()
    }
    val road = Path().apply {
        var x = roadStart
        moveTo(x * u, metrics.y(PlanetGround.pathY(x)))
        while (x < roadEnd) {
            x = min(roadEnd, x + 0.02f)
            lineTo(x * u, metrics.y(PlanetGround.pathY(x)))
        }
    }

    val random = Random(biome.seed)
    fun clearOf(x: Float, room: Float) = keepClear.none { abs(it - x) < room }

    var last: PropKind? = null
    fun pick(kinds: List<PropKind>): PropKind {
        var kind = kinds[random.nextInt(kinds.size)]
        if (kind == last && kinds.distinct().size > 1) kind = kinds.first { it != last }
        last = kind
        return kind
    }

    val midProps = buildList {
        val span = range(MID)
        var x = span.start
        while (x < span.endInclusive) {
            val kind = pick(biome.backProps)
            val foot = 0.5f - 0.13f * ridge(biome.midRidge, x / MID * 0.5f, biome.seed + 7) + 0.03f
            add(PropSpot(kind, x, foot, 0.07f + random.nextFloat() * 0.05f, random.nextInt(6), random.nextBoolean()))
            x += 0.1f + random.nextFloat() * 0.16f
        }
    }
    val backProps = buildList {
        var x = groundSpan.start
        while (x < groundSpan.endInclusive) {
            val kind = pick(biome.backProps)
            val tall = when (kind) {
                PropKind.ROCK -> 0.06f + random.nextFloat() * 0.03f
                PropKind.GRASS, PropKind.FLOWERS -> 0.05f + random.nextFloat() * 0.03f
                PropKind.DOME -> 0.1f + random.nextFloat() * 0.03f
                else -> 0.12f + random.nextFloat() * 0.08f
            }
            add(PropSpot(kind, x, PlanetGround.horizon(x) + 0.012f + random.nextFloat() * 0.03f, tall, random.nextInt(6), random.nextBoolean()))
            x += 0.14f + random.nextFloat() * 0.18f
        }
    }
    val frontProps = buildList {
        var x = groundSpan.start
        while (x < groundSpan.endInclusive) {
            if (clearOf(x, 0.07f)) {
                val kind = pick(biome.frontProps)
                val below = random.nextFloat() < 0.6f
                val y = if (below) PlanetGround.pathY(x) + 0.17f + random.nextFloat() * 0.07f else PlanetGround.pathY(x) - 0.085f
                add(PropSpot(kind, x, y, 0.035f + random.nextFloat() * 0.03f, random.nextInt(6), random.nextBoolean()))
            }
            x += 0.09f + random.nextFloat() * 0.14f
        }
    }
    val lowerProps = buildList {
        var x = groundSpan.start + 0.1f
        while (x < groundSpan.endInclusive) {
            // Well below the level names, so these need no room around the levels.
            val kind = pick(biome.backProps.filter { it != PropKind.LAMP && it != PropKind.ANTENNA })
            val tall = if (kind == PropKind.DOME || kind == PropKind.ROCK) 0.07f else 0.1f + random.nextFloat() * 0.05f
            add(PropSpot(kind, x, PlanetGround.pathY(x) + 0.31f + random.nextFloat() * 0.06f, tall, random.nextInt(6), random.nextBoolean()))
            x += 0.32f + random.nextFloat() * 0.32f
        }
    }
    val foreground = buildList {
        val span = range(FORE)
        var x = span.start
        val footY = metrics.heightUnits - metrics.top + 0.02f
        while (x < span.endInclusive) {
            add(PropSpot(PropKind.GRASS, x, footY, 0.09f + random.nextFloat() * 0.07f, random.nextInt(6), random.nextBoolean()))
            x += 0.4f + random.nextFloat() * 0.45f
        }
    }
    val stars = List(90) { Offset(random.nextFloat(), random.nextFloat()) }
    return PlanetGeometry(far, mid, ground, road, midProps, backProps, frontProps + lowerProps, foreground, stars)
}

/** The sky, the mountains, the ground and the path, drawn behind everything that stands on the planet. */
@Composable
fun SceneScope.PlanetBackdrop(biome: Biome, geometry: PlanetGeometry, time: State<Float>) {
    val camera = camera
    val metrics = metrics
    val farInk = remember(biome) { PropInk(biome.ink.leaf, biome.ink.bark, biome.ink.stone, biome.ink.accent, flat = lerp(biome.mid, biome.skyHorizon, 0.12f)) }
    Spacer(
        Modifier
            .fillMaxSize()
            .drawBehind {
                val u = metrics.unitPx
                val cam = camera.position
                val t = time.value
                val horizonY = metrics.y(0.47f)

                drawRect(Brush.verticalGradient(0f to biome.skyTop, 0.55f to biome.skyMid, 1f to biome.skyHorizon, startY = 0f, endY = horizonY))
                drawRect(biome.skyHorizon, Offset(0f, horizonY), Size(size.width, size.height - horizonY))

                // Stars fade out towards the warm horizon.
                val drift = cam * 0.02f
                for ((i, star) in geometry.stars.withIndex()) {
                    val x = ((star.x - drift / (metrics.viewport + 0.3f)).mod(1f)) * size.width
                    val y = star.y * horizonY * 0.8f
                    val fade = 1f - y / (horizonY * 0.8f)
                    val twinkle = 0.55f + 0.45f * sin(t * (1.1f + (i % 5) * 0.3f) + i)
                    drawCircle(Color.White.copy(alpha = (0.75f * fade * twinkle).coerceIn(0f, 1f)), (0.6f + (i % 3) * 0.5f) * metrics.density, Offset(x, y))
                }
                drawSkyObject(biome, metrics, cam, t)

                // Glow where the sky meets the land.
                drawRect(
                    Brush.verticalGradient(listOf(Color.Transparent, biome.skyHorizon.copy(alpha = 0.55f), Color.Transparent), startY = horizonY - 0.25f * u, endY = horizonY + 0.08f * u),
                    Offset(0f, horizonY - 0.25f * u),
                    Size(size.width, 0.33f * u),
                )

                translate(-cam * FAR * u, 0f) {
                    drawPath(geometry.farRidge, Brush.verticalGradient(listOf(lerp(biome.far, biome.skyHorizon, 0.25f), biome.far), startY = metrics.y(0.2f), endY = metrics.y(0.5f)))
                }
                translate(-cam * MID * u, 0f) {
                    val left = cam * MID - 0.3f
                    val right = cam * MID + metrics.viewport + 0.3f
                    for (prop in geometry.midProps) {
                        if (prop.x < left || prop.x > right) continue
                        drawProp(prop.kind, prop.x * u, metrics.y(prop.y), prop.height * u, farInk, prop.variant, t, prop.mirror)
                    }
                    drawPath(geometry.midRidge, Brush.verticalGradient(listOf(lerp(biome.mid, biome.skyHorizon, 0.15f), lerp(biome.mid, biome.groundBottom, 0.3f)), startY = metrics.y(0.36f), endY = metrics.y(0.52f)))
                }
                translate(-cam * u, 0f) {
                    val left = cam - 0.4f
                    val right = cam + metrics.viewport + 0.4f
                    drawPath(geometry.ground, Brush.verticalGradient(listOf(lerp(biome.groundTop, Color.White, 0.08f), biome.groundTop, biome.groundBottom), startY = metrics.y(0.45f), endY = metrics.y(1.05f)))
                    // A rim of light along the top of the ground.
                    drawPath(geometry.ground, lerp(biome.groundTop, Color.White, 0.35f).copy(alpha = 0.5f), style = Stroke(0.006f * u))
                    for (prop in geometry.backProps) {
                        if (prop.x < left || prop.x > right) continue
                        drawProp(prop.kind, prop.x * u, metrics.y(prop.y), prop.height * u, biome.ink, prop.variant, t, prop.mirror)
                    }
                    val roadWidth = max(0.058f * u, 22f * metrics.density)
                    drawPath(geometry.road, Color.Black.copy(alpha = 0.18f), style = Stroke(roadWidth * 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    drawPath(geometry.road, biome.pathEdge, style = Stroke(roadWidth * 1.22f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    drawPath(geometry.road, biome.path, style = Stroke(roadWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    drawPath(
                        geometry.road,
                        lerp(biome.path, biome.pathEdge, 0.22f),
                        style = Stroke(roadWidth * 0.22f, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(roadWidth * 0.1f, roadWidth * 0.9f))),
                    )
                    for (prop in geometry.frontProps) {
                        if (prop.x < left || prop.x > right) continue
                        drawProp(prop.kind, prop.x * u, metrics.y(prop.y), prop.height * u, biome.ink, prop.variant, t, prop.mirror)
                    }
                }
            },
    )
}

/** Dark shapes close to the camera that frame the scene and give it depth. Drawn above everything. */
@Composable
fun SceneScope.PlanetForeground(biome: Biome, geometry: PlanetGeometry, time: State<Float>) {
    val camera = camera
    val metrics = metrics
    val ink = remember(biome) { PropInk(biome.ink.leaf, biome.ink.bark, biome.ink.stone, biome.ink.accent, flat = lerp(biome.groundBottom, Color.Black, 0.45f)) }
    Spacer(
        Modifier
            .fillMaxSize()
            .drawBehind {
                val u = metrics.unitPx
                val cam = camera.position
                translate(-cam * FORE * u, 0f) {
                    val left = cam * FORE - 0.5f
                    val right = cam * FORE + metrics.viewport + 0.5f
                    for (prop in geometry.foreground) {
                        if (prop.x < left || prop.x > right) continue
                        drawProp(prop.kind, prop.x * u, metrics.y(prop.y), prop.height * u, ink, prop.variant, time.value, prop.mirror)
                    }
                }
            },
    )
}

private fun DrawScope.drawSkyObject(biome: Biome, metrics: SceneMetrics, camera: Float, t: Float) {
    val u = metrics.unitPx
    val drift = camera * 0.04f * u
    when (biome.sky) {
        SkyObject.RINGED_PLANET -> {
            val center = Offset(size.width * 0.58f - drift, metrics.y(0.17f))
            drawRingedPlanet(center, 0.1f * u, Color(0xFF2FB5A6), Color(0xFF9DF5E4), Color(0xFF12545A))
            drawCircle(Color(0xFFFFE9C2).copy(alpha = 0.9f), 0.018f * u, Offset(size.width * 0.28f - drift * 0.6f, metrics.y(0.1f)))
        }
        SkyObject.AURORA -> {
            // Ribbons of light: bright along a waving line, fading above and below, with faint rays.
            val colors = listOf(Color(0xFF3EF0A0), Color(0xFF3ED6C8), Color(0xFF9A7CFF))
            for (band in 0 until 3) {
                val color = colors[band]
                val baseY = metrics.y(0.06f + band * 0.07f)
                val steps = 28
                val line = Path()
                val points = (0..steps).map { i ->
                    val x = size.width * i / steps
                    Offset(x, baseY + sin(i * 0.5f + band * 1.7f + t * 0.3f + camera * 0.5f) * 0.05f * u)
                }
                line.moveTo(points[0].x, points[0].y)
                for (point in points.drop(1)) line.lineTo(point.x, point.y)
                drawPath(line, color.copy(alpha = 0.16f), style = Stroke(0.09f * u, cap = StrokeCap.Round))
                drawPath(line, color.copy(alpha = 0.22f), style = Stroke(0.04f * u, cap = StrokeCap.Round))
                drawPath(line, color.copy(alpha = 0.35f), style = Stroke(0.012f * u, cap = StrokeCap.Round))
                for ((i, point) in points.withIndex()) {
                    if (i % 2 == 1) continue
                    val flicker = 0.5f + 0.5f * sin(t * 1.3f + i * 0.9f + band)
                    drawLine(
                        Brush.verticalGradient(listOf(color.copy(alpha = 0.18f * flicker), Color.Transparent), startY = point.y, endY = point.y + 0.14f * u),
                        point,
                        point + Offset(0f, 0.14f * u),
                        strokeWidth = 0.012f * u,
                    )
                }
            }
            val moon = Offset(size.width * 0.56f - drift, metrics.y(0.13f))
            val r = 0.055f * u
            drawCircle(Brush.radialGradient(listOf(Color(0xFFE8F4FF).copy(alpha = 0.28f), Color.Transparent), moon, r * 2.6f), r * 2.6f, moon)
            val crescent = Path().apply { addOval(Rect(moon, r)) }
            clipPath(crescent) {
                drawCircle(Color(0xFFF2F7FF), r, moon)
                drawCircle(biome.skyMid, r * 0.92f, moon + Offset(r * 0.45f, -r * 0.2f))
            }
        }
        SkyObject.MOONS -> {
            val big = Offset(size.width * 0.56f - drift, metrics.y(0.15f))
            drawCircle(Brush.radialGradient(listOf(Color(0xFFFFF1C9).copy(alpha = 0.3f), Color.Transparent), big, 0.16f * u), 0.16f * u, big)
            drawCircle(Brush.radialGradient(listOf(Color(0xFFFFF6DC), Color(0xFFF1CF8A), Color(0xFFB98A4C)), big - Offset(0.02f * u, 0.02f * u), 0.1f * u), 0.07f * u, big)
            drawCircle(Color(0xFFB98A4C).copy(alpha = 0.35f), 0.014f * u, big + Offset(0.02f * u, 0.015f * u))
            drawCircle(Color(0xFFB98A4C).copy(alpha = 0.3f), 0.009f * u, big + Offset(-0.025f * u, -0.01f * u))
            val small = Offset(size.width * 0.3f - drift * 0.7f, metrics.y(0.08f))
            drawCircle(Brush.radialGradient(listOf(Color(0xFFE6F0FF), Color(0xFF9FB4E8)), small - Offset(0.008f * u, 0.008f * u), 0.04f * u), 0.028f * u, small)
        }
        SkyObject.EARTH -> {
            val earth = Offset(size.width * 0.58f - drift, metrics.y(0.16f))
            val r = 0.085f * u
            drawCircle(Brush.radialGradient(listOf(Color(0xFF3D8BFF).copy(alpha = 0.35f), Color.Transparent), earth, r * 1.8f), r * 1.8f, earth)
            drawCircle(Brush.radialGradient(listOf(Color(0xFF7CC4FF), Color(0xFF2C7BE5), Color(0xFF123E8C)), earth - Offset(r * 0.35f, r * 0.4f), r * 1.6f), r, earth)
            clipPath(Path().apply { addOval(Rect(earth, r)) }) {
                val land = Color(0xFF3FBF6A)
                rotate(t * 2f, earth) {
                    drawOval(land, earth + Offset(-r * 0.7f, -r * 0.55f), Size(r * 0.8f, r * 0.55f))
                    drawOval(land, earth + Offset(-r * 0.3f, -r * 0.05f), Size(r * 0.45f, r * 0.8f))
                    drawOval(land, earth + Offset(r * 0.25f, -r * 0.35f), Size(r * 0.7f, r * 0.45f))
                }
                drawOval(Color.White.copy(alpha = 0.75f), earth + Offset(-r * 0.5f, -r * 1.02f), Size(r, r * 0.28f))
                drawCircle(Brush.radialGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)), earth - Offset(r * 0.5f, r * 0.5f), r * 2.2f), r, earth)
            }
        }
    }
    // A shooting star now and then.
    val cycle = (t % 9f) / 9f
    if (cycle < 0.08f) {
        val p = cycle / 0.08f
        val start = Offset(size.width * (0.15f + 0.5f * ((floor(t / 9f).toInt() * 37 % 10) / 10f)), metrics.y(0.02f))
        val head = start + Offset(0.5f * u * p, 0.18f * u * p)
        val tail = head - Offset(0.12f * u, 0.045f * u)
        drawLine(Brush.linearGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.9f * (1f - p))), tail, head), tail, head, strokeWidth = 2.2f * metrics.density, cap = StrokeCap.Round)
    }
}

/** A planet with a ring, lit from the upper left. */
fun DrawScope.drawRingedPlanet(center: Offset, radius: Float, base: Color, light: Color, dark: Color) {
    drawCircle(Brush.radialGradient(listOf(light.copy(alpha = 0.22f), Color.Transparent), center, radius * 2f), radius * 2f, center)
    val ring = Rect(center.x - radius * 1.9f, center.y - radius * 0.5f, center.x + radius * 1.9f, center.y + radius * 0.5f)
    rotate(-14f, center) {
        drawArc(light.copy(alpha = 0.5f), 180f, 180f, false, ring.topLeft, ring.size, style = Stroke(radius * 0.16f))
    }
    drawCircle(Brush.radialGradient(listOf(light, base, dark), center - Offset(radius * 0.35f, radius * 0.4f), radius * 1.7f), radius, center)
    clipPath(Path().apply { addOval(Rect(center, radius)) }) {
        for (i in 0 until 5) {
            val y = center.y - radius + (i + 0.5f) * (2 * radius / 5)
            drawRect(dark.copy(alpha = if (i % 2 == 0) 0.18f else 0.07f), Offset(center.x - radius, y - radius * 0.08f), Size(2 * radius, radius * 0.16f))
        }
    }
    rotate(-14f, center) {
        drawArc(light.copy(alpha = 0.85f), 0f, 180f, false, ring.topLeft, ring.size, style = Stroke(radius * 0.16f))
    }
}

