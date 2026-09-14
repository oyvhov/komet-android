package app.komet.ui.scene

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
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
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.komet.audio.Sfx
import app.komet.domain.Curriculum
import app.komet.domain.Progression
import app.komet.domain.SpaceCards
import app.komet.domain.Subject
import app.komet.ui.KometViewModel
import app.komet.ui.S
import app.komet.ui.Screen
import app.komet.ui.components.GameText
import app.komet.ui.components.KometIcons
import app.komet.ui.components.LocalFeedback
import app.komet.ui.components.Pill
import app.komet.ui.components.PressSurface
import app.komet.ui.components.RoundIconButton
import app.komet.ui.components.StarGlyph
import app.komet.ui.components.Tone
import app.komet.ui.components.Tones
import app.komet.ui.components.fixedSp
import app.komet.ui.components.gloss
import app.komet.ui.components.str
import app.komet.ui.components.subjectTone
import app.komet.ui.theme.K
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** The places on the star map, in the order the orbit visits them. */
enum class MapPlace { MATH, READING, RACE, ENGLISH, CARDS, SPACE, EXPLORE }

private class PlaceSpot(val place: MapPlace, val x: Float, val y: Float, val radius: Float)

private class MapStar(val x: Float, val y: Float, val size: Float, val layer: Int, val phase: Float)

private class MapLayout(
    val metrics: SceneMetrics,
    val spots: List<PlaceSpot>,
    val width: Float,
    val stars: List<MapStar>,
) {
    fun spot(place: MapPlace): PlaceSpot = spots.first { it.place == place }
}

private val STAR_PARALLAX = floatArrayOf(0.08f, 0.22f, 0.45f)

private fun mapLayout(metrics: SceneMetrics): MapLayout {
    val spacing = max(0.38f, metrics.unitsOf(220.dp))
    val radius = max(0.13f, metrics.unitsOf(58.dp)).coerceAtMost(metrics.unitsOf(120.dp))
    // Places zigzag up and down, so tall screens show more than one at a time.
    val heights = listOf(0.34f, 0.64f, 0.33f, 0.64f, 0.34f, 0.63f, 0.35f)
    val startX = max(0.62f, metrics.unitsOf(300.dp))
    val spots = MapPlace.entries.mapIndexed { index, place ->
        val size = when (place) {
            MapPlace.SPACE -> radius * 1.1f
            MapPlace.RACE, MapPlace.CARDS, MapPlace.EXPLORE -> radius * 0.88f
            else -> radius
        }
        PlaceSpot(place, startX + index * spacing, heights[index], size)
    }
    val width = spots.last().x + max(0.6f, metrics.viewport * 0.4f)
    val random = Random(7)
    val stars = List(260) { index ->
        val layer = if (index < 150) 0 else if (index < 230) 1 else 2
        val span = (width * STAR_PARALLAX[layer]) + metrics.viewport + 1f
        MapStar(-0.5f + random.nextFloat() * span, -0.6f + random.nextFloat() * (metrics.heightUnits + 0.4f), 0.5f + random.nextFloat() * (layer + 1f), layer, random.nextFloat() * 6.28f)
    }
    return MapLayout(metrics, spots, width, stars)
}

private fun MapPlace.tone(): Tone = when (this) {
    MapPlace.MATH -> Tones.Math
    MapPlace.READING -> Tones.Reading
    MapPlace.ENGLISH -> Tones.English
    MapPlace.SPACE -> Tones.Space
    MapPlace.RACE -> Tones.Race
    MapPlace.CARDS -> Tones.Cards
    MapPlace.EXPLORE -> Tones.Explore
}

private fun MapPlace.subject(): Subject? = when (this) {
    MapPlace.MATH -> Subject.MATH
    MapPlace.READING -> Subject.READING
    MapPlace.ENGLISH -> Subject.ENGLISH
    MapPlace.SPACE -> Subject.SPACE
    else -> null
}

fun mapPlaceOf(subject: Subject): MapPlace = when (subject) {
    Subject.MATH -> MapPlace.MATH
    Subject.READING -> MapPlace.READING
    Subject.ENGLISH -> MapPlace.ENGLISH
    Subject.SPACE -> MapPlace.SPACE
}

/** Where the rocket waits beside a place. */
private fun parked(spot: PlaceSpot): Offset = Offset(spot.x - spot.radius * 1.25f, spot.y - spot.radius * 0.95f)

/** The star map: home. A sun, the planets of each subject and the other places, joined by an orbit. */
@Composable
fun StarMapScreen(vm: KometViewModel) {
    val profile = vm.profile ?: return
    val feedback = LocalFeedback.current
    val time = rememberSceneTime()
    val scope = rememberCoroutineScope()
    val camera = rememberSceneCamera(Unit)
    val recommended = remember(profile) { Progression.recommended(profile) }
    val resume = remember(profile) { Progression.resumable(profile) }
    val unlockedCards = SpaceCards.unlockedCount(profile.totalStars)
    val flight = remember { Animatable(0f) }
    var flying by remember { mutableStateOf<Pair<MapPlace, MapPlace>?>(null) }
    var picker by remember { mutableStateOf(false) }
    val here = vm.mapPlace

    fun open(place: MapPlace) {
        vm.mapPlace = place
        when (place) {
            MapPlace.RACE -> vm.open(Screen.RaceMenu)
            MapPlace.CARDS -> vm.open(Screen.Collection)
            MapPlace.EXPLORE -> vm.open(Screen.Explore)
            else -> vm.open(Screen.World(place.subject()!!))
        }
    }

    Box(Modifier.fillMaxSize()) {
        SceneViewport(
            camera = camera,
            layoutFor = ::mapLayout,
            worldWidth = { it.width },
            startLeft = { layout -> layout.spot(here).x - layout.metrics.viewport / 2 },
            topShare = 0.5f,
        ) { layout ->
            val metrics = metrics

            fun fly(place: MapPlace) {
                if (flying != null) return
                if (place == vm.mapPlace) {
                    open(place)
                    return
                }
                val from = vm.mapPlace
                flying = from to place
                feedback.sfx(Sfx.WHOOSH)
                scope.launch {
                    launch { camera.glideTo(layout.spot(place).x, 1150) }
                    flight.snapTo(0f)
                    flight.animateTo(1f, tween(1150, easing = FastOutSlowInEasing))
                    vm.mapPlace = place
                    flying = null
                    delay(140)
                    open(place)
                }
            }

            SpaceBackdrop(layout, time)
            OrbitLine(layout)
            Sun(time)

            for (spot in layout.spots) {
                PlaceView(
                    spot = spot,
                    time = time,
                    label = when (spot.place) {
                        MapPlace.RACE -> S.race.str()
                        MapPlace.CARDS -> S.cards.str()
                        MapPlace.EXPLORE -> S.explore.str()
                        else -> S.subject(spot.place.subject()!!).str()
                    },
                    detail = when (val subject = spot.place.subject()) {
                        null -> when (spot.place) {
                            MapPlace.CARDS -> S.cardsOf(unlockedCards, SpaceCards.all.size).str()
                            else -> null
                        }
                        else -> Curriculum.skills(subject).let { "${Progression.earnedStars(profile, it)} / ${it.size * 3}" }
                    },
                    showStar = spot.place.subject() != null,
                    badge = if (spot.place == MapPlace.CARDS && unlockedCards > profile.seenCards) S.newBadge.str() else null,
                    next = resume == null && spot.place.subject() == recommended.subject,
                    onTap = { fly(spot.place) },
                )
            }

            RocketTrail(layout, flight, { flying })
            val rocketSize = units(0.1f, 44.dp, 76.dp)
            Box(
                Modifier
                    .worldAt({
                        val route = flying
                        if (route == null) {
                            parked(layout.spot(vm.mapPlace)) + Offset(0f, sin(time.value * 2.2f) * 0.008f)
                        } else {
                            flightPoint(layout, route, flight.value)
                        }
                    }, anchorY = 0.5f)
                    .size(rocketSize * 0.62f, rocketSize)
                    .graphicsLayer {
                        val route = flying
                        rotationZ = if (route == null) 28f + sin(time.value * 1.7f) * 3f else flightAngle(layout, route, flight.value)
                    },
            ) {
                app.komet.ui.components.RocketArt(Modifier.fillMaxSize(), body = Color.White, accent = K.Race)
            }
        }

        MapOverlay(
            vm = vm,
            time = time,
            onProfile = { if (vm.state.profiles.size > 1) picker = true },
        )

        MissionBubble(
            vm = vm,
            time = time,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(12.dp),
        )
    }

    if (picker) ProfilePicker(vm, time) { picker = false }
}

private fun bezier(a: Offset, b: Offset, c: Offset, d: Offset, t: Float): Offset {
    val u = 1 - t
    return a * (u * u * u) + b * (3 * u * u * t) + c * (3 * u * t * t) + d * (t * t * t)
}

private fun controls(layout: MapLayout, route: Pair<MapPlace, MapPlace>): List<Offset> {
    val from = parked(layout.spot(route.first))
    val to = parked(layout.spot(route.second))
    val lift = 0.22f
    val reach = (to.x - from.x) * 0.3f
    return listOf(from, Offset(from.x + reach, from.y - lift), Offset(to.x - reach, to.y - lift), to)
}

private fun flightPoint(layout: MapLayout, route: Pair<MapPlace, MapPlace>, t: Float): Offset {
    val (a, b, c, d) = controls(layout, route)
    return bezier(a, b, c, d, t.coerceIn(0f, 1f))
}

private fun flightAngle(layout: MapLayout, route: Pair<MapPlace, MapPlace>, t: Float): Float {
    val ahead = flightPoint(layout, route, (t + 0.02f).coerceAtMost(1f))
    val behind = flightPoint(layout, route, (t - 0.02f).coerceAtLeast(0f))
    val dx = ahead.x - behind.x
    val dy = ahead.y - behind.y
    return (atan2(dx, -dy) * 180f / PI.toFloat())
}

@Composable
private fun SceneScope.RocketTrail(layout: MapLayout, flight: Animatable<Float, *>, route: () -> Pair<MapPlace, MapPlace>?) {
    val camera = camera
    val metrics = metrics
    Spacer(
        Modifier
            .fillMaxSize()
            .drawBehind {
                val path = route() ?: return@drawBehind
                val t = flight.value
                val u = metrics.unitPx
                for (k in 1..16) {
                    val p = t - k * 0.013f
                    if (p <= 0f) break
                    val point = flightPoint(layout, path, p)
                    val screen = Offset((point.x - camera.position) * u, metrics.y(point.y + 0.03f))
                    val fade = 1f - k / 17f
                    drawCircle(lerp(Color(0xFFFFE27A), Color(0xFFFF6A3D), k / 16f).copy(alpha = 0.75f * fade), (0.018f * u * fade).coerceAtLeast(2f), screen)
                }
            },
    )
}

@Composable
private fun SceneScope.SpaceBackdrop(layout: MapLayout, time: State<Float>) {
    val camera = camera
    val metrics = metrics
    Spacer(
        Modifier
            .fillMaxSize()
            .drawBehind {
                val u = metrics.unitPx
                val cam = camera.position
                val t = time.value
                drawRect(Brush.verticalGradient(listOf(Color(0xFF040722), K.SpaceTop, Color(0xFF12184A))))
                // Nebulae drift slowly behind everything.
                val nebulae = listOf(
                    Triple(Offset(0.5f, 0.2f), 0.9f, Color(0xFF5B2FB0)),
                    Triple(Offset(1.9f, 0.85f), 1.1f, Color(0xFF155E8C)),
                    Triple(Offset(3.2f, 0.15f), 0.9f, Color(0xFFB02F7A)),
                    Triple(Offset(4.4f, 0.8f), 1f, Color(0xFF2F7AB0)),
                )
                for ((center, radius, color) in nebulae) {
                    val c = Offset((center.x - cam * 0.15f) * u, metrics.y(center.y))
                    drawCircle(Brush.radialGradient(listOf(color.copy(alpha = 0.32f), Color.Transparent), c, radius * u), radius * u, c)
                }
                // A spiral galaxy far away.
                val galaxy = Offset((2.6f - cam * 0.1f) * u, metrics.y(0.12f))
                rotate(t * 2f, galaxy) {
                    for (arm in 0 until 2) {
                        for (i in 0 until 26) {
                            val a = i * 0.32f + arm * PI.toFloat()
                            val r = 0.004f * u + i * 0.0032f * u
                            drawCircle(Color(0xFFD8C8FF).copy(alpha = 0.45f * (1f - i / 26f)), (0.004f * u * (1f - i / 30f)).coerceAtLeast(1f), galaxy + Offset(cos(a) * r, sin(a) * r * 0.55f))
                        }
                    }
                }
                drawCircle(Brush.radialGradient(listOf(Color(0xFFFFF1D6).copy(alpha = 0.7f), Color.Transparent), galaxy, 0.03f * u), 0.03f * u, galaxy)
                for (star in layout.stars) {
                    val x = (star.x - cam * STAR_PARALLAX[star.layer]) * u
                    if (x < -10f || x > size.width + 10f) continue
                    val y = metrics.y(star.y)
                    val twinkle = 0.55f + 0.45f * sin(t * (0.8f + star.layer * 0.5f) + star.phase)
                    val radius = star.size * metrics.density * 0.8f
                    drawCircle(Color.White.copy(alpha = (0.35f + star.layer * 0.22f) * twinkle), radius, Offset(x, y))
                    if (star.layer == 2 && star.size > 2.4f) {
                        val glint = radius * 3.2f * twinkle
                        drawLine(Color.White.copy(alpha = 0.5f * twinkle), Offset(x - glint, y), Offset(x + glint, y), strokeWidth = 1f)
                        drawLine(Color.White.copy(alpha = 0.5f * twinkle), Offset(x, y - glint), Offset(x, y + glint), strokeWidth = 1f)
                    }
                }
            },
    )
}

@Composable
private fun SceneScope.OrbitLine(layout: MapLayout) {
    val camera = camera
    val metrics = metrics
    val path = remember(layout) {
        val u = metrics.unitPx
        val points = listOf(Offset(0f, 0.5f)) + layout.spots.map { Offset(it.x, it.y) } + Offset(layout.width, layout.spots.last().y)
        Path().apply {
            moveTo(points[0].x * u, metrics.y(points[0].y))
            for (i in 0 until points.size - 1) {
                val p0 = points[(i - 1).coerceAtLeast(0)]
                val p1 = points[i]
                val p2 = points[i + 1]
                val p3 = points[(i + 2).coerceAtMost(points.lastIndex)]
                val c1 = p1 + (p2 - p0) * (1f / 6f)
                val c2 = p2 - (p3 - p1) * (1f / 6f)
                cubicTo(c1.x * u, metrics.y(c1.y), c2.x * u, metrics.y(c2.y), p2.x * u, metrics.y(p2.y))
            }
        }
    }
    Spacer(
        Modifier
            .fillMaxSize()
            .drawBehind {
                translate(-camera.position * metrics.unitPx, 0f) {
                    drawPath(path, Color(0xFF7F8CFF).copy(alpha = 0.12f), style = Stroke(14.dp.toPx(), cap = StrokeCap.Round))
                    drawPath(
                        path,
                        Color.White.copy(alpha = 0.45f),
                        style = Stroke(4.dp.toPx(), cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(0.1f, 16.dp.toPx()))),
                    )
                }
            },
    )
}

@Composable
private fun SceneScope.Sun(time: State<Float>) {
    val camera = camera
    val metrics = metrics
    Spacer(
        Modifier
            .fillMaxSize()
            .drawBehind {
                val u = metrics.unitPx
                val t = time.value
                val center = Offset((-0.04f - camera.position) * u, metrics.y(0.5f))
                val r = 0.3f * u
                if (center.x - r * 3f > size.width) return@drawBehind
                drawCircle(
                    Brush.radialGradient(
                        0f to Color(0xFFFFC94A).copy(alpha = 0.55f),
                        0.35f to Color(0xFFFF8A2E).copy(alpha = 0.22f),
                        1f to Color.Transparent,
                        center = center,
                        radius = r * 3f,
                    ),
                    r * 3f,
                    center,
                )
                // Soft flares turning slowly, brighter at the root.
                rotate(t * 3f, center) {
                    for (i in 0 until 12) {
                        val a = i * PI.toFloat() / 6f
                        val length = r * (1.55f + 0.18f * sin(t * 1.6f + i * 1.7f))
                        val width = r * 0.16f
                        val dir = Offset(cos(a), sin(a))
                        val side = Offset(-dir.y, dir.x)
                        val flare = Path().apply {
                            moveTo(center.x + side.x * width + dir.x * r * 0.8f, center.y + side.y * width + dir.y * r * 0.8f)
                            lineTo(center.x + dir.x * length, center.y + dir.y * length)
                            lineTo(center.x - side.x * width + dir.x * r * 0.8f, center.y - side.y * width + dir.y * r * 0.8f)
                            close()
                        }
                        drawPath(flare, Brush.radialGradient(listOf(Color(0xFFFFE08A).copy(alpha = 0.55f), Color.Transparent), center, length))
                    }
                }
                drawCircle(Brush.radialGradient(listOf(Color(0xFFFFFDF0), Color(0xFFFFE27A), Color(0xFFFFA62E), Color(0xFFF26A0F)), center - Offset(r * 0.2f, r * 0.25f), r * 1.3f), r, center)
                drawCircle(Color.White.copy(alpha = 0.18f + 0.06f * sin(t * 1.3f)), r * 0.7f, center - Offset(r * 0.18f, r * 0.2f))
            },
    )
}

private val orbitGlyphs = mapOf(
    MapPlace.MATH to listOf("1", "2", "3", "+"),
    MapPlace.READING to listOf("A", "B", "C"),
    MapPlace.ENGLISH to listOf("Hi", "Go", "Yes"),
)

@Composable
private fun SceneScope.PlaceView(
    spot: PlaceSpot,
    time: State<Float>,
    label: String,
    detail: String?,
    showStar: Boolean,
    badge: String?,
    next: Boolean,
    onTap: () -> Unit,
) {
    val tone = spot.place.tone()
    val diameter = units(spot.radius * 2)
    val measurer = rememberTextMeasurer()
    val glyphs = remember(spot.place, diameter) {
        orbitGlyphs[spot.place].orEmpty().map { glyph ->
            val style = TextStyle(fontSize = (diameter.value * 0.13f).coerceIn(13f, 20f).sp, fontWeight = FontWeight.Black)
            measurer.measure(glyph, style)
        }
    }
    Canvas(
        Modifier
            .worldAt(spot.x, spot.y, anchorY = 0.5f)
            .size(diameter * 1.9f)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, role = Role.Button, onClickLabel = label, onClick = onTap),
    ) {
        val t = time.value
        val c = center
        val r = diameter.toPx() / 2
        when (spot.place) {
            MapPlace.MATH, MapPlace.READING, MapPlace.ENGLISH, MapPlace.SPACE -> {
                drawOrbiters(glyphs, c, r, t, front = false, tone = tone)
                drawCircle(Brush.radialGradient(listOf(tone.face.copy(alpha = 0.4f), Color.Transparent), c, r * 1.75f), r * 1.75f, c)
                drawMapPlanet(spot.place, c, r, t)
                if (spot.place == MapPlace.SPACE) drawSatellite(c, r, t)
                drawOrbiters(glyphs, c, r, t, front = true, tone = tone)
            }
            MapPlace.RACE -> drawRaceRing(c, r, t)
            MapPlace.CARDS -> drawCardVault(c, r, t, badge != null)
            MapPlace.EXPLORE -> drawObservatory(c, r, t)
        }
    }
    if (next) {
        Canvas(
            Modifier
                .worldAt({ Offset(spot.x, spot.y - spot.radius * 1.35f - 0.02f * (1f + sin(time.value * 4f))) }, anchorY = 1f)
                .size(30.dp, 26.dp),
        ) {
            val arrow = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2, size.height)
                close()
            }
            drawPath(arrow, K.Gold)
            drawPath(arrow, K.Outline, style = Stroke(2.5.dp.toPx()))
        }
    }
    Column(
        Modifier.worldAt(spot.x, spot.y + spot.radius * 1.05f + 0.012f, anchorY = 0f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            Modifier
                .border(2.dp, K.Outline, RoundedCornerShape(50))
                .gloss(tone.face, RoundedCornerShape(50), top = tone.top)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onTap)
                .padding(horizontal = 14.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            GameText(label, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            if (badge != null) {
                GameText(
                    badge,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .border(1.5.dp, K.Outline, RoundedCornerShape(50))
                        .background(Brush.verticalGradient(listOf(K.RaceTop, K.Race)), RoundedCornerShape(50))
                        .padding(horizontal = 7.dp, vertical = 1.dp),
                )
            }
        }
        if (detail != null) {
            Row(
                Modifier
                    .padding(top = 4.dp)
                    .background(K.SpaceTop.copy(alpha = 0.75f), RoundedCornerShape(50))
                    .padding(horizontal = 9.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (showStar) StarGlyph(true, Modifier.size(15.dp))
                Text(detail, style = MaterialTheme.typography.labelMedium, color = K.Text, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}

/** A subject's planet: its own surface, lit by the sun on the left, with a rim of atmosphere. */
private fun DrawScope.drawMapPlanet(place: MapPlace, c: Offset, r: Float, t: Float) {
    val (light, base, dark) = when (place) {
        MapPlace.MATH -> Triple(Color(0xFFFFC46B), Color(0xFFF2780F), Color(0xFF7A2A06))
        MapPlace.READING -> Triple(Color(0xFF9FEAFF), Color(0xFF1E88E5), Color(0xFF0A2F78))
        MapPlace.ENGLISH -> Triple(Color(0xFF8FE6FF), Color(0xFF2E8FD8), Color(0xFF0C3A7A))
        else -> Triple(Color(0xFFD9C6FF), Color(0xFF7B4DF2), Color(0xFF2E1680))
    }
    val ringRect = Rect(c.x - r * 1.75f, c.y - r * 0.42f, c.x + r * 1.75f, c.y + r * 0.42f)
    if (place == MapPlace.SPACE) {
        rotate(-16f, c) {
            drawArc(K.Outline, 180f, 180f, false, ringRect.topLeft, ringRect.size, style = Stroke(r * 0.2f))
            drawArc(Brush.horizontalGradient(listOf(light, Color(0xFFFFE27A), light), startX = ringRect.left, endX = ringRect.right), 180f, 180f, false, ringRect.topLeft, ringRect.size, style = Stroke(r * 0.13f))
        }
    }
    val body = Path().apply { addOval(Rect(c, r)) }
    drawCircle(Brush.radialGradient(listOf(light, base, dark), c - Offset(r * 0.45f, r * 0.45f), r * 1.9f), r, c)
    clipPath(body) {
        when (place) {
            MapPlace.MATH -> {
                // Bands with soft waves, and a storm drifting through them.
                for (i in 0 until 7) {
                    val y = c.y - r + (i + 0.5f) * (2 * r / 7)
                    val band = Path().apply {
                        moveTo(c.x - r, y - r * 0.07f)
                        var x = -1f
                        while (x <= 1f) {
                            lineTo(c.x + x * r, y - r * 0.07f + sin(x * 6f + i + t * 0.4f) * r * 0.025f)
                            x += 0.1f
                        }
                        lineTo(c.x + r, y + r * 0.07f)
                        lineTo(c.x - r, y + r * 0.07f)
                        close()
                    }
                    drawPath(band, (if (i % 2 == 0) dark else light).copy(alpha = if (i % 2 == 0) 0.22f else 0.18f))
                }
                val storm = Offset(c.x + r * 0.28f - ((t * 0.02f) % 0.6f) * r, c.y + r * 0.32f)
                drawOval(Color(0xFFFFE2B0).copy(alpha = 0.85f), storm - Offset(r * 0.24f, r * 0.12f), Size(r * 0.48f, r * 0.24f))
                drawOval(Color(0xFFD4541A).copy(alpha = 0.8f), storm - Offset(r * 0.14f, r * 0.065f), Size(r * 0.28f, r * 0.13f))
            }
            MapPlace.READING -> {
                // An ocean world with clouds sweeping round it.
                val drift = (t * 0.03f) % 2f
                for (i in 0 until 6) {
                    val y = c.y - r * 0.7f + i * r * 0.28f
                    val x = c.x - r * 1.4f + ((i * 0.37f + drift) % 2f) * r * 1.6f
                    drawRoundRect(Color.White.copy(alpha = 0.55f), Offset(x, y), Size(r * (0.7f + (i % 3) * 0.25f), r * 0.1f), CornerRadius(r * 0.05f))
                    drawRoundRect(Color.White.copy(alpha = 0.35f), Offset(x + r * 0.2f, y + r * 0.1f), Size(r * 0.45f, r * 0.07f), CornerRadius(r * 0.035f))
                }
            }
            MapPlace.ENGLISH -> {
                // Green lands on a blue sea, turning slowly.
                val shift = ((t * 0.025f) % 2f) * r
                val land = Color(0xFF3FBF5A)
                val landDark = Color(0xFF1F7A36)
                for (copy in 0..1) {
                    val dx = -r * 1.6f + shift + copy * r * 2f
                    drawOval(landDark, Offset(c.x + dx - r * 0.1f, c.y - r * 0.62f), Size(r * 0.95f, r * 0.62f))
                    drawOval(land, Offset(c.x + dx, c.y - r * 0.58f), Size(r * 0.8f, r * 0.5f))
                    drawOval(land, Offset(c.x + dx + r * 0.35f, c.y - r * 0.05f), Size(r * 0.45f, r * 0.75f))
                    drawOval(landDark, Offset(c.x + dx + r * 0.9f, c.y + r * 0.2f), Size(r * 0.5f, r * 0.35f))
                    drawOval(land, Offset(c.x + dx + r * 0.95f, c.y + r * 0.22f), Size(r * 0.38f, r * 0.26f))
                }
                drawOval(Color.White.copy(alpha = 0.9f), Offset(c.x - r * 0.55f, c.y - r * 1.06f), Size(r * 1.1f, r * 0.3f))
                drawRoundRect(Color.White.copy(alpha = 0.45f), Offset(c.x - r * 0.9f + shift * 0.3f, c.y + r * 0.45f), Size(r * 0.8f, r * 0.08f), CornerRadius(r * 0.04f))
            }
            else -> {
                for (i in 0 until 5) {
                    val y = c.y - r + (i + 0.5f) * (2 * r / 5)
                    drawRect((if (i % 2 == 0) dark else light).copy(alpha = 0.18f), Offset(c.x - r, y - r * 0.09f), Size(2 * r, r * 0.18f))
                }
            }
        }
        // Night side and a thin bright rim towards the sun.
        drawCircle(Brush.radialGradient(listOf(Color.Transparent, Color.Transparent, Color(0xFF050818).copy(alpha = 0.55f)), c - Offset(r * 0.55f, r * 0.45f), r * 2.1f), r, c)
        drawCircle(Color.White.copy(alpha = 0.3f), r * 0.3f, c - Offset(r * 0.42f, r * 0.45f))
    }
    drawArc(light.copy(alpha = 0.8f), 150f, 110f, false, c - Offset(r * 0.96f, r * 0.96f), Size(r * 1.92f, r * 1.92f), style = Stroke(r * 0.045f, cap = StrokeCap.Round))
    drawCircle(K.Outline.copy(alpha = 0.7f), r, c, style = Stroke(r * 0.035f))
    if (place == MapPlace.SPACE) {
        rotate(-16f, c) {
            drawArc(K.Outline, 0f, 180f, false, ringRect.topLeft, ringRect.size, style = Stroke(r * 0.2f))
            drawArc(Brush.horizontalGradient(listOf(light, Color(0xFFFFE27A), light), startX = ringRect.left, endX = ringRect.right), 0f, 180f, false, ringRect.topLeft, ringRect.size, style = Stroke(r * 0.13f))
        }
    }
}

private fun DrawScope.drawOrbiters(glyphs: List<TextLayoutResult>, c: Offset, r: Float, t: Float, front: Boolean, tone: Tone) {
    if (glyphs.isEmpty()) return
    for ((i, glyph) in glyphs.withIndex()) {
        val a = t * 0.45f + i * 2f * PI.toFloat() / glyphs.size
        val depth = sin(a)
        if ((depth >= 0f) != front) continue
        val p = c + Offset(cos(a) * r * 1.5f, depth * r * 0.45f)
        val scale = 0.82f + 0.22f * depth
        val moon = max(glyph.size.width, glyph.size.height) * 0.62f
        withTransform({ scale(scale, scale, pivot = p) }) {
            drawCircle(K.Outline, moon + 2.5f, p)
            drawCircle(Brush.radialGradient(listOf(tone.top, tone.face, tone.edge), p - Offset(moon * 0.35f, moon * 0.4f), moon * 1.6f), moon, p)
            drawCircle(Color.White.copy(alpha = 0.5f), moon * 0.22f, p - Offset(moon * 0.42f, moon * 0.42f))
            val topLeft = p - Offset(glyph.size.width / 2f, glyph.size.height / 2f)
            drawText(glyph, color = K.Outline, topLeft = topLeft + Offset(0f, 2f))
            drawText(glyph, color = Color.White, topLeft = topLeft)
        }
        if (!front) drawCircle(K.SpaceTop.copy(alpha = 0.35f), moon * scale, p)
    }
}

private fun DrawScope.drawSatellite(c: Offset, r: Float, t: Float) {
    val a = -t * 0.7f
    val p = c + Offset(cos(a) * r * 1.35f, sin(a) * r * 1.1f)
    rotate(a * 57f, p) {
        drawRect(Color(0xFF3D8BFF), p + Offset(-r * 0.32f, -r * 0.06f), Size(r * 0.2f, r * 0.12f))
        drawRect(Color(0xFF3D8BFF), p + Offset(r * 0.12f, -r * 0.06f), Size(r * 0.2f, r * 0.12f))
        drawCircle(Color.White, r * 0.07f, p)
        drawCircle(K.Outline, r * 0.07f, p, style = Stroke(2f))
    }
}

private fun DrawScope.drawRaceRing(c: Offset, r: Float, t: Float) {
    drawCircle(Brush.radialGradient(listOf(K.Race.copy(alpha = 0.35f), Color.Transparent), c, r * 1.6f), r * 1.6f, c)
    val track = Rect(c.x - r * 1.45f, c.y - r * 0.55f, c.x + r * 1.45f, c.y + r * 0.55f)
    // The asteroid the track circles
    val rock = Path().apply {
        val random = Random(3)
        for (i in 0 until 12) {
            val a = i * 2 * PI / 12
            val rr = r * (0.52f + random.nextFloat() * 0.12f)
            val x = c.x + (cos(a) * rr).toFloat()
            val y = c.y + (sin(a) * rr * 0.9f).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    rotate(-10f, c) {
        drawArc(K.Outline, 180f, 180f, false, track.topLeft, track.size, style = Stroke(r * 0.2f))
        drawArc(Color.White, 180f, 180f, false, track.topLeft, track.size, style = Stroke(r * 0.13f))
        drawArc(Color(0xFF1A1440), 180f, 180f, false, track.topLeft, track.size, style = Stroke(r * 0.13f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(r * 0.12f, r * 0.12f))))
    }
    drawPath(rock, Brush.radialGradient(listOf(Color(0xFFE08A7A), Color(0xFFB0443A), Color(0xFF5C1E1E)), c - Offset(r * 0.2f, r * 0.2f), r * 0.9f))
    drawPath(rock, K.Outline, style = Stroke(r * 0.05f))
    drawCircle(Color(0x55330E0E), r * 0.12f, c + Offset(-r * 0.15f, r * 0.1f))
    drawCircle(Color(0x55330E0E), r * 0.08f, c + Offset(r * 0.22f, -r * 0.15f))
    val a = t * 1.6f
    val racer = Offset(c.x + cos(a) * r * 1.45f, c.y + sin(a) * r * 0.55f)
    val front = sin(a) >= 0
    rotate(-10f, c) {
        if (front) {
            drawArc(K.Outline, 0f, 180f, false, track.topLeft, track.size, style = Stroke(r * 0.2f))
            drawArc(Color.White, 0f, 180f, false, track.topLeft, track.size, style = Stroke(r * 0.13f))
            drawArc(Color(0xFF1A1440), 0f, 180f, false, track.topLeft, track.size, style = Stroke(r * 0.13f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(r * 0.12f, r * 0.12f))))
        }
        drawCircle(K.GoldTop.copy(alpha = 0.5f), r * 0.18f, racer)
        drawCircle(K.Gold, r * 0.1f, racer)
        drawCircle(K.Outline, r * 0.1f, racer, style = Stroke(2f))
        if (!front) {
            drawArc(K.Outline, 0f, 180f, false, track.topLeft, track.size, style = Stroke(r * 0.2f))
            drawArc(Color.White, 0f, 180f, false, track.topLeft, track.size, style = Stroke(r * 0.13f))
            drawArc(Color(0xFF1A1440), 0f, 180f, false, track.topLeft, track.size, style = Stroke(r * 0.13f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(r * 0.12f, r * 0.12f))))
        }
    }
}

private fun DrawScope.drawCardVault(c: Offset, r: Float, t: Float, glowing: Boolean) {
    drawCircle(Brush.radialGradient(listOf(K.Gold.copy(alpha = if (glowing) 0.6f else 0.35f), Color.Transparent), c, r * 1.7f), r * 1.7f, c)
    val spin = cos(t * 1.1f)
    val w = r * 1.1f
    val h = r * 1.5f
    withTransform({ scale(spin, 1f, pivot = c) }) {
        val topLeft = c - Offset(w / 2, h / 2 + sin(t * 2f) * r * 0.06f)
        val shape = CornerRadius(r * 0.16f)
        drawRoundRect(K.Outline, topLeft - Offset(r * 0.04f, r * 0.04f), Size(w + r * 0.08f, h + r * 0.08f), CornerRadius(r * 0.2f))
        if (spin >= 0f) {
            drawRoundRect(Brush.verticalGradient(listOf(K.GoldTop, K.Gold, K.CardsDeep), startY = topLeft.y, endY = topLeft.y + h), topLeft, Size(w, h), shape)
            drawRoundRect(Color.White.copy(alpha = 0.7f), topLeft + Offset(r * 0.1f, r * 0.1f), Size(w - r * 0.2f, h - r * 0.2f), CornerRadius(r * 0.1f), style = Stroke(r * 0.04f))
            val star = Path()
            val sc = topLeft + Offset(w / 2, h / 2)
            for (i in 0 until 10) {
                val rr = if (i % 2 == 0) r * 0.34f else r * 0.15f
                val a = -PI / 2 + i * PI / 5
                val x = sc.x + (rr * cos(a)).toFloat()
                val y = sc.y + (rr * sin(a)).toFloat()
                if (i == 0) star.moveTo(x, y) else star.lineTo(x, y)
            }
            star.close()
            drawPath(star, Color.White)
            drawPath(star, K.Outline, style = Stroke(r * 0.04f))
        } else {
            drawRoundRect(Brush.verticalGradient(listOf(Color(0xFF6C3FF0), Color(0xFF2A1680)), startY = topLeft.y, endY = topLeft.y + h), topLeft, Size(w, h), shape)
            for (i in 0 until 5) drawCircle(Color.White.copy(alpha = 0.7f), r * 0.03f, topLeft + Offset(w * (0.2f + (i * 37 % 10) / 14f), h * (0.15f + i * 0.17f)))
        }
    }
    for (i in 0 until 5) {
        val phase = (t * 0.5f + i * 0.2f) % 1f
        val a = i * 1.3f
        val p = c + Offset(cos(a) * r * (0.8f + phase * 0.7f), sin(a) * r * (0.8f + phase * 0.7f))
        val s = r * 0.07f * (1f - phase)
        drawLine(Color.White.copy(alpha = 1f - phase), p - Offset(s, 0f), p + Offset(s, 0f), strokeWidth = 2f)
        drawLine(Color.White.copy(alpha = 1f - phase), p - Offset(0f, s), p + Offset(0f, s), strokeWidth = 2f)
    }
}

private fun DrawScope.drawObservatory(c: Offset, r: Float, t: Float) {
    drawCircle(Brush.radialGradient(listOf(K.Explore.copy(alpha = 0.35f), Color.Transparent), c, r * 1.7f), r * 1.7f, c)
    // A sweeping beam from the telescope
    val beamOrigin = c + Offset(r * 0.18f, -r * 0.42f)
    val sweep = -60f + sin(t * 0.6f) * 25f
    rotate(sweep, beamOrigin) {
        val beam = Path().apply {
            moveTo(beamOrigin.x, beamOrigin.y)
            lineTo(beamOrigin.x + r * 2.2f, beamOrigin.y - r * 0.35f)
            lineTo(beamOrigin.x + r * 2.2f, beamOrigin.y + r * 0.35f)
            close()
        }
        drawPath(beam, Brush.horizontalGradient(listOf(K.ExploreTop.copy(alpha = 0.45f), Color.Transparent), startX = beamOrigin.x, endX = beamOrigin.x + r * 2.2f))
    }
    val moon = c + Offset(0f, r * 0.35f)
    drawCircle(Brush.radialGradient(listOf(Color(0xFFD9DEEA), Color(0xFF8E97B0), Color(0xFF4A5170)), moon - Offset(r * 0.25f, r * 0.25f), r * 1.1f), r * 0.72f, moon)
    drawCircle(K.Outline, r * 0.72f, moon, style = Stroke(r * 0.05f))
    clipPath(Path().apply { addOval(Rect(moon, r * 0.72f)) }) {
        drawCircle(Color(0x33000000), r * 0.14f, moon + Offset(-r * 0.3f, r * 0.2f))
        drawCircle(Color(0x33000000), r * 0.1f, moon + Offset(r * 0.32f, r * 0.3f))
    }
    val dome = Rect(c.x - r * 0.42f, c.y - r * 0.62f, c.x + r * 0.42f, c.y + r * 0.22f)
    rotate(sweep + 60f, beamOrigin) {
        drawLine(K.Outline, beamOrigin - Offset(r * 0.1f, -r * 0.05f), beamOrigin + Offset(r * 0.5f, -r * 0.18f), strokeWidth = r * 0.24f, cap = StrokeCap.Round)
        drawLine(Color(0xFFE8ECF8), beamOrigin - Offset(r * 0.1f, -r * 0.05f), beamOrigin + Offset(r * 0.5f, -r * 0.18f), strokeWidth = r * 0.15f, cap = StrokeCap.Round)
    }
    drawArc(Brush.linearGradient(listOf(Color.White, Color(0xFFB9C3DE)), start = dome.topLeft, end = dome.bottomRight), 180f, 180f, true, dome.topLeft, dome.size)
    drawArc(K.Outline, 180f, 180f, true, dome.topLeft, dome.size, style = Stroke(r * 0.05f))
    drawRect(K.Explore, Offset(c.x - r * 0.06f, dome.top + r * 0.04f), Size(r * 0.12f, dome.height / 2 - r * 0.04f))
}

/** Profile, stars and the parents' door along the top; today's mission underneath. */
@Composable
private fun MapOverlay(vm: KometViewModel, time: State<Float>, onProfile: () -> Unit) {
    val profile = vm.profile ?: return
    val today = vm.today
    val rank = Progression.rank(profile.totalStars)
    val nextRank = Progression.nextRank(profile.totalStars)
    val streak = profile.currentStreak(today)
    val roundsToday = profile.today(today).rounds
    val goal = vm.settings.dailyGoal
    Column(
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier
                    .weight(1f, fill = false)
                    .background(K.SpaceTop.copy(alpha = 0.72f), RoundedCornerShape(50))
                    .border(1.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(50))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, role = Role.Button, onClickLabel = S.switchProfile.str(), onClick = onProfile)
                    .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box {
                    HeroBadge(profile.hero, time, 52.dp)
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 4.dp, y = 4.dp)
                            .size(24.dp)
                            .border(2.dp, K.Outline, CircleShape)
                            .background(Brush.verticalGradient(listOf(K.GoldTop, K.Gold, K.GoldDeep)), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        GameText((Progression.ranks.indexOf(rank) + 1).toString(), style = MaterialTheme.typography.labelMedium, fontSize = fixedSp(12.dp))
                    }
                }
                Column {
                    GameText(profile.name, style = MaterialTheme.typography.titleLarge, maxLines = 1)
                    Text(rank.title.str(), style = MaterialTheme.typography.labelMedium, color = K.Gold, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (nextRank != null) {
                        val span = (nextRank.minStars - rank.minStars).coerceAtLeast(1)
                        val progress = ((profile.totalStars - rank.minStars) / span.toFloat()).coerceIn(0f, 1f)
                        Box(
                            Modifier
                                .padding(top = 3.dp)
                                .size(96.dp, 6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(K.SurfaceLow),
                        ) {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .drawBehind { drawRect(K.Gold, size = Size(size.width * progress, size.height)) },
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.weight(0.01f))
            Pill(profile.totalStars.toString(), star = true)
            Box {
                RoundIconButton(KometIcons.Lock, S.parents.str(), onClick = { vm.open(Screen.ParentGate) }, size = 48.dp, tint = K.Muted)
                if (vm.updater.state.release != null) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .size(14.dp)
                            .border(2.dp, K.Outline, CircleShape)
                            .background(K.Gold, CircleShape),
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier
                    .background(K.SpaceTop.copy(alpha = 0.72f), RoundedCornerShape(50))
                    .border(1.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(S.dailyMission.str(), style = MaterialTheme.typography.labelLarge, color = if (roundsToday >= goal) K.GoodTop else K.Text, fontWeight = FontWeight.ExtraBold)
                repeat(goal.coerceAtMost(5)) { index -> StarGlyph(filled = index < roundsToday, modifier = Modifier.size(20.dp)) }
            }
            if (streak > 0) Pill(S.streak(streak).str(), icon = KometIcons.Flame, iconTint = Color(0xFFFF8A3D))
        }
    }
}

/** The astronaut's face in a round frame. */
@Composable
fun HeroBadge(look: app.komet.domain.HeroLook, time: State<Float>, size: androidx.compose.ui.unit.Dp) {
    Box(
        Modifier
            .size(size)
            .border(2.dp, K.Outline, CircleShape)
            .padding(2.dp)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(Color(0xFF3A4BB0), Color(0xFF151C5C))))
            .border((size.value * 0.05f).coerceAtLeast(2f).dp, Color.White.copy(alpha = 0.8f), CircleShape),
    ) {
        HeroPortrait(look, time, Modifier.fillMaxSize())
    }
}

/** Bolt suggests what to do next: the unfinished round first, otherwise the next mission. */
@Composable
private fun MissionBubble(vm: KometViewModel, time: State<Float>, modifier: Modifier) {
    val profile = vm.profile ?: return
    val resume = remember(profile) { Progression.resumable(profile) }
    val recommended = remember(profile) { Progression.recommended(profile) }
    val feedback = LocalFeedback.current
    var talking by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Row(modifier, verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        Bolt(
            time = time,
            mood = { if (talking) BoltMood.TALK else BoltMood.IDLE },
            modifier = Modifier
                .size(76.dp)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                    feedback.sfx(Sfx.BEEP)
                    talking = true
                    if (resume != null) vm.say(resume.first.title) else vm.say(recommended.title)
                    scope.launch {
                        delay(1800)
                        talking = false
                    }
                },
        )
        SpeechBubble(Modifier.widthIn(max = 330.dp).padding(bottom = 46.dp), tailAt = 0.08f) {
            val tone = subjectTone((resume?.first ?: recommended).subject)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    (if (resume != null) S.resumeLabel else S.nextMission).str().uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = tone.edge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(Modifier.weight(1f, fill = false)) {
                        Text(
                            (resume?.first ?: recommended).title.str(),
                            style = MaterialTheme.typography.titleLarge,
                            color = K.Ink,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            if (resume != null) S.resumeDetail(resume.second.done, resume.second.total).str() else S.subject(recommended.subject).str(),
                            style = MaterialTheme.typography.labelMedium,
                            color = K.InkMuted,
                            maxLines = 1,
                        )
                    }
                    PressSurface(
                        onClick = { if (resume != null) vm.resumeRound() else vm.startSkill(recommended) },
                        face = K.Gold,
                        edge = K.GoldDeep,
                        top = K.GoldTop,
                        shape = RoundedCornerShape(18.dp),
                        depth = 4.dp,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 9.dp),
                    ) {
                        GameText((if (resume != null) S.resume else S.start).str(), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfilePicker(vm: KometViewModel, time: State<Float>, onClose: () -> Unit) {
    val profile = vm.profile ?: return
    AlertDialog(
        onDismissRequest = onClose,
        containerColor = K.SurfaceHigh,
        title = { Text(S.whoPlays.str(), color = K.Text, style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                vm.state.profiles.forEach { other ->
                    PressSurface(
                        onClick = {
                            vm.switchProfile(other.id)
                            onClose()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        face = if (other.id == profile.id) K.Surface else K.SurfaceLow,
                        edge = K.SpaceTop,
                        contentAlignment = Alignment.CenterStart,
                        contentPadding = PaddingValues(12.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            HeroBadge(other.hero, time, 48.dp)
                            Text(other.name, style = MaterialTheme.typography.titleLarge, color = K.Text, modifier = Modifier.weight(1f))
                            Pill(other.totalStars.toString(), star = true)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onClose) { Text(S.close.str(), color = K.Gold) }
        },
    )
}
