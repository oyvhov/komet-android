package app.komet.ui.scene

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.komet.audio.Sfx
import app.komet.BuildConfig
import app.komet.domain.Progression
import app.komet.domain.ShopSlot
import app.komet.domain.SolarDestination
import app.komet.domain.SpaceCards
import app.komet.domain.Txt
import app.komet.ui.components.cappedSp
import app.komet.ui.components.GameText
import app.komet.ui.components.KometIcons
import app.komet.ui.components.KometDialog
import app.komet.ui.components.LocalFeedback
import app.komet.ui.components.NuggetGlyph
import app.komet.ui.components.Pill
import app.komet.ui.components.RoundIconButton
import app.komet.ui.components.StarGlyph
import app.komet.ui.components.str
import app.komet.ui.KometViewModel
import app.komet.ui.S
import app.komet.ui.Screen
import app.komet.ui.theme.K
import app.komet.ui.theme.LocalMotion
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private data class PlanetSpot(val planet: SolarDestination, val x: Float, val y: Float, val radius: Float) {
    val dock: Offset get() = Offset(x - radius * 0.82f, y - radius * 0.83f)
}

private data class MapStar(val x: Float, val y: Float, val size: Float, val layer: Int, val phase: Float)

private data class MapLayout(val spots: List<PlanetSpot>, val width: Float, val viewport: Float, val stars: List<MapStar>) {
    fun spot(planet: SolarDestination): PlanetSpot = spots[planet.ordinal]
}

private val STAR_PARALLAX = floatArrayOf(0.08f, 0.22f, 0.45f)

private fun mapLayout(metrics: SceneMetrics): MapLayout {
    val short = metrics.heightPx / metrics.density < 475f
    val portrait = metrics.viewport < 0.9f
    val spacing = if (portrait) max(0.225f, metrics.unitsOf(115.dp)) else max(0.29f, metrics.unitsOf(142.dp))
    val first = max(0.42f, metrics.viewport * 0.48f)
    val baseRadius = 0.135f.coerceIn(metrics.unitsOf(44.dp), metrics.unitsOf(if (short) 53.dp else 81.dp))
    val spots = SolarDestination.entries.mapIndexed { index, planet ->
        val y = if (index % 2 == 0) (if (short) 0.42f else 0.35f) else 0.56f
        val scale = 1f - (planet.planet.sizeRank - 1) * 0.035f
        PlanetSpot(planet, first + index * spacing, y, baseRadius * scale)
    }
    val width = spots.last().x + metrics.viewport * 0.53f
    val random = Random(7)
    val stars = List(240) { index ->
        val layer = if (index < 140) 0 else if (index < 205) 1 else 2
        val span = width * STAR_PARALLAX[layer] + metrics.viewport + 1f
        MapStar(
            random.nextFloat() * span - 0.5f, random.nextFloat() * metrics.heightUnits - metrics.top,
            0.5f + random.nextFloat() * (layer + 1f), layer, random.nextFloat() * 6.28f,
        )
    }
    return MapLayout(spots, width, metrics.viewport, stars)
}

@Composable
fun StarMapScreen(vm: KometViewModel) {
    val profile = vm.profile ?: return
    val time = rememberSceneTime()
    var picker by remember { mutableStateOf(false) }
    var showMission by remember { mutableStateOf(false) }
    val next = remember(profile) {
        SolarDestination.forSkill(Progression.resumable(profile)?.first ?: Progression.recommended(profile))
    }
    val completed = remember(profile) {
        SolarDestination.stamps(profile)
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = maxWidth >= 580.dp && maxWidth > maxHeight
        val compact = wide && maxWidth < 760.dp
        key(profile.id) {
            SolarJourney(vm.parkedPlanet, next, completed, rocketPaint(profile.equipped[ShopSlot.ROCKET]), time) {
                if (vm.screen == Screen.Home && vm.profile?.id == profile.id) vm.open(Screen.SolarWorld(it))
            }
        }
        Column(
            Modifier.align(Alignment.TopCenter).windowInsetsPadding(WindowInsets.safeDrawing)
                .widthIn(max = 960.dp).fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            if (wide) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) { MapProfile(vm, compact) { picker = true } }
                    Box(Modifier.weight(1.25f)) { MapActions(vm) }
                }
            } else {
                MapProfile(vm, onProfile = { picker = true })
                MapActions(vm)
            }
        }
        val roomForMission = maxWidth >= 840.dp && maxHeight >= 510.dp
        Box(
            Modifier.align(Alignment.BottomCenter).windowInsetsPadding(WindowInsets.safeDrawing)
                .widthIn(max = if (roomForMission) 980.dp else 560.dp).fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            if (roomForMission) {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.Bottom) {
                    MissionBubble(vm, time, Modifier.weight(1.15f))
                    Box(Modifier.weight(1f)) { MapShortcuts(vm, time, onMission = null) }
                }
            } else MapShortcuts(vm, time) { showMission = true }
        }
    }
    if (showMission) {
        KometDialog(onClose = { showMission = false }) {
            MissionBubble(vm, time, Modifier.fillMaxWidth().padding(top = 36.dp))
        }
    }
    if (picker) ProfileWindow(vm, time) { picker = false }
}

@Composable
private fun MapActions(vm: KometViewModel) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        MapAction(Txt("Kartoversikt"), KometIcons.Compass, Modifier.weight(1f)) { vm.open(Screen.SolarOverview) }
        MapAction(Txt("Alle oppgåver", "Alle oppgaver"), KometIcons.Cards, Modifier.weight(1f)) { vm.open(Screen.Explore) }
    }
}

@Composable
private fun MapAction(label: Txt, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    val feedback = LocalFeedback.current
    Box(modifier.heightIn(min = 64.dp).clickable(role = Role.Button) { feedback.tap(); onClick() },
        contentAlignment = Alignment.Center) {
        Row(
            Modifier.fillMaxWidth().background(Color(0xB41B293C), CircleShape)
                .border(1.dp, Color(0x457E99B4), CircleShape).padding(horizontal = 9.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        ) {
            Icon(icon, null, Modifier.size(19.dp), tint = K.GoldTop)
            GameText(label.str(), Modifier.weight(1f, fill = false), style = MaterialTheme.typography.titleSmall, maxLines = 1, autoFit = true)
        }
    }
}

@Composable
internal fun SolarJourney(
    parked: SolarDestination,
    next: SolarDestination?,
    completed: Set<SolarDestination>,
    paint: RocketPaint,
    time: State<Float>,
    onOpen: (SolarDestination) -> Unit,
) {
    val camera = rememberSceneCamera(Unit)
    val motion = LocalMotion.current
    val feedback = LocalFeedback.current
    val open by rememberUpdatedState(onOpen)
    var destination by remember { mutableStateOf<SolarDestination?>(null) }
    val progress = remember(destination) { Animatable(0f) }

    SceneViewport(
        camera, ::mapLayout, worldWidth = { it.width },
        startLeft = { it.spot(parked).x - it.viewport / 2f },
        onUserPan = { destination = null },
    ) { layout ->
        val origin = layout.spot(parked)
        val arrival = destination?.let(layout::spot)
        LaunchedEffect(arrival, motion) {
            if (arrival == null) return@LaunchedEffect
            if (motion) {
                if (origin.x !in camera.position..(camera.position + camera.viewport)) camera.centerOn(origin.x)
                val duration = 920 + abs(arrival.planet.ordinal - origin.planet.ordinal) * 65
                coroutineScope {
                    launch { camera.glideTo(arrival.x, duration) }
                    progress.animateTo(1f, tween(duration, easing = FastOutSlowInEasing))
                }
            }
            open(arrival.planet)
            destination = null
        }
        SpaceBackdrop(layout, time)
        OrbitLine(layout)
        Sun()
        for (spot in layout.spots) {
            JourneyPlanet(spot, spot.planet == next, spot.planet in completed) {
                if (destination == null) {
                    if (motion && spot.planet != parked) {
                        feedback.sfx(Sfx.WHOOSH)
                        destination = spot.planet
                    } else {
                        open(spot.planet)
                    }
                }
            }
        }
        SolarFlight(
            from = { Offset(screenX(origin.dock.x), metrics.y(origin.dock.y)) },
            to = { arrival?.dock?.let { Offset(screenX(it.x), metrics.y(it.y)) } },
            progress = { progress.value },
            flying = arrival != null,
            paint = paint,
            rocketHeight = units(0.11f, 44.dp, 63.dp),
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun SceneScope.JourneyPlanet(spot: PlanetSpot, next: Boolean, completed: Boolean, onClick: () -> Unit) {
    val diameter = units(spot.radius * 2f)
    val feedback = LocalFeedback.current
    Column(
        Modifier.worldAt(spot.x, spot.y - spot.radius, anchorY = 0f).width(diameter + 22.dp)
            .clickable(interactionSource = null, indication = null, role = Role.Button) {
                feedback.tap()
                onClick()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(Modifier.size(diameter), contentAlignment = Alignment.Center) {
            if (next) {
                Canvas(Modifier.matchParentSize()) {
                    drawCircle(
                        Brush.radialGradient(listOf(K.Gold.copy(alpha = 0.42f), Color.Transparent)),
                        size.minDimension * 0.61f,
                    )
                }
            }
            SolarGlobe(spot.planet, Modifier.fillMaxSize())
            if (completed) StarGlyph(true, Modifier.align(Alignment.BottomEnd).size(19.dp))
        }
        GameText(
            spot.planet.planet.name.str(), Modifier.fillMaxWidth(), color = if (next) K.GoldTop else K.Text,
            style = MaterialTheme.typography.titleSmall, fontSize = cappedSp(17.sp),
            textAlign = TextAlign.Center, autoFit = true, maxLines = 1,
        )
    }
}

@Composable
private fun MapProfile(vm: KometViewModel, compact: Boolean = false, onProfile: () -> Unit) {
    val profile = vm.profile ?: return
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier.weight(1f).heightIn(min = 64.dp)
                .clickable(role = Role.Button, onClickLabel = S.switchProfile.str(), onClick = onProfile),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                HeroBadge(profile.hero, size = 39.dp, gear = Gear.of(profile.equipped))
                if (!compact) {
                    Column(Modifier.weight(1f)) {
                        GameText(profile.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, autoFit = true)
                        Text(
                            Progression.rank(profile.totalStars).title.str(),
                            color = K.GoldTop, style = MaterialTheme.typography.labelMedium,
                            fontSize = cappedSp(12.sp), maxLines = 1,
                        )
                    }
                }
            }
        }
        Pill(profile.totalStars.toString(), star = true)
        Box {
            RoundIconButton(KometIcons.Lock, S.parents.str(), onClick = { vm.open(Screen.ParentGate) }, size = 48.dp)
            if (!BuildConfig.PLAY_STORE && vm.updater.state.release != null) {
                Box(Modifier.align(Alignment.TopEnd).size(12.dp).background(K.Gold, CircleShape))
            }
        }
    }
}

@Composable
private fun MapShortcuts(vm: KometViewModel, time: State<Float>, onMission: (() -> Unit)?) {
    val profile = vm.profile ?: return
    val shape = RoundedCornerShape(25.dp)
    Row(Modifier.fillMaxWidth().background(Color(0xEF172338), shape)
        .border(1.dp, Color(0x50627B98), shape).padding(horizontal = 3.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically) {
        if (onMission != null) Column(
            Modifier.weight(1f).heightIn(min = 64.dp).clickable(role = Role.Button, onClick = onMission),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
        ) {
            Bolt(time, Modifier.size(32.dp), paint = profile.equipped[ShopSlot.BOLT])
            GameText(Txt("Oppdrag").str(), style = MaterialTheme.typography.labelMedium, maxLines = 1, autoFit = true)
        }
        MapShortcut(S.race, KometIcons.Rocket, K.RaceTop, Modifier.weight(1f)) { vm.open(Screen.RaceMenu) }
        val newCard = SpaceCards.unlockedCount(profile.totalStars) > profile.seenCards
        MapShortcut(S.cards, KometIcons.Cards, K.GoldTop, Modifier.weight(1f), newCard) { vm.open(Screen.Collection) }
        MapShortcut(S.shop, null, K.GoldTop, Modifier.weight(1f), count = profile.nuggets) { vm.open(Screen.Shop) }
    }
}

@Composable
private fun MapShortcut(
    label: Txt, icon: ImageVector?, tint: Color, modifier: Modifier,
    badge: Boolean = false, count: Int? = null, onClick: () -> Unit,
) {
    val feedback = LocalFeedback.current
    Box(modifier.heightIn(min = 64.dp).clickable(role = Role.Button) { feedback.tap(); onClick() },
        contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box {
                    if (icon == null) NuggetGlyph(Modifier.size(22.dp))
                    else Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
                    if (badge) Box(Modifier.align(Alignment.TopEnd).size(6.dp).background(K.Gold, CircleShape))
                }
                if (count != null) Text(count.toString(), color = tint, fontSize = cappedSp(13.sp), maxLines = 1)
            }
            GameText(label.str(), style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, maxLines = 1, autoFit = true)
        }
    }
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
                drawRect(Brush.verticalGradient(listOf(Color(0xFF070C19), Color(0xFF111B32), Color(0xFF0A1425))))
                // Nebulae drift slowly behind everything.
                val nebulae = listOf(
                    Triple(Offset(0.5f, 0.2f), 0.9f, Color(0xFF5B2FB0)),
                    Triple(Offset(1.9f, 0.85f), 1.1f, Color(0xFF155E8C)),
                    Triple(Offset(3.2f, 0.15f), 0.9f, Color(0xFFB02F7A)),
                    Triple(Offset(4.4f, 0.8f), 1f, Color(0xFF2F7AB0)),
                )
                for ((center, radius, color) in nebulae) {
                    val c = Offset((center.x - cam * 0.15f) * u, metrics.y(center.y))
                    drawCircle(Brush.radialGradient(listOf(color.copy(alpha = 0.18f), Color.Transparent), c, radius * u), radius * u, c)
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
                    drawPath(path, Color(0xFF7F8CFF).copy(alpha = 0.12f), style = Stroke(4.dp.toPx(), cap = StrokeCap.Round))
                    drawPath(
                        path,
                        Color.White.copy(alpha = 0.45f),
                        style = Stroke(1.2.dp.toPx(), cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 9.dp.toPx()))),
                    )
                }
            },
    )
}

@Composable
private fun SceneScope.Sun() {
    val camera = camera
    val metrics = metrics
    Spacer(
        Modifier
            .fillMaxSize()
            .drawBehind {
                val u = metrics.unitPx
                val center = Offset((-0.04f - camera.position) * u, metrics.y(0.5f))
                val r = 0.22f * u
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
                drawCircle(Brush.radialGradient(listOf(Color(0xFFFFFDF0), Color(0xFFFFE27A), Color(0xFFFFA62E), Color(0xFFF26A0F)), center - Offset(r * 0.2f, r * 0.25f), r * 1.3f), r, center)

            },
    )
}
