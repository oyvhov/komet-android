package app.komet.ui.scene

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.komet.audio.Sfx
import app.komet.domain.Chapter
import app.komet.domain.Curriculum
import app.komet.domain.Progression
import app.komet.domain.Skill
import app.komet.domain.Subject
import app.komet.ui.KometViewModel
import app.komet.ui.S
import app.komet.ui.components.BigButton
import app.komet.ui.components.Feedback
import app.komet.ui.components.GameText
import app.komet.ui.components.KometIcons
import app.komet.ui.components.LocalFeedback
import app.komet.ui.components.Pill
import app.komet.ui.components.PlanetArt
import app.komet.ui.components.PressSurface
import app.komet.ui.components.RocketArt
import app.komet.ui.components.RoundIconButton
import app.komet.ui.components.StarRow
import app.komet.ui.components.cappedSp
import app.komet.ui.components.gloss
import app.komet.ui.components.shake
import app.komet.ui.components.str
import app.komet.ui.components.subjectTone
import app.komet.ui.screens.LevelMedallion
import app.komet.ui.screens.LevelSymbol
import app.komet.ui.theme.K
import app.komet.ui.theme.LocalMotion
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sin
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private class Station(val skill: Skill, val chapter: Chapter, val index: Int, val x: Float) {
    val y: Float = PlanetGround.pathY(x)
}

private class Signpost(val chapter: Chapter, val x: Float)

private class Critter(val x: Float, val kind: Int, val color: Color)

private class PlanetLayout(
    val metrics: SceneMetrics,
    val stations: List<Station>,
    val signs: List<Signpost>,
    val critters: List<Critter>,
    val padX: Float,
    val workshopX: Float,
    val goalX: Float,
    val width: Float,
    val geometry: PlanetGeometry,
    val medallion: Dp,
    val labelWidth: Dp,
    val heroHeight: Dp,
    /** How far left of a level the hero stands, so it never covers the level. */
    val standOffset: Float,
) {
    fun station(skill: Skill?): Station? = stations.firstOrNull { it.skill == skill }
}

private fun planetLayout(subject: Subject, biome: Biome, metrics: SceneMetrics): PlanetLayout {
    val spacing = max(0.19f, metrics.unitsOf(118.dp))
    // Room for the sign, then the hero standing before the first level.
    val signGap = max(0.3f, metrics.unitsOf(215.dp))
    val chapterGap = max(0.3f, metrics.unitsOf(180.dp))
    val padX = max(0.3f, metrics.unitsOf(150.dp))
    val workshopX = padX + max(0.32f, metrics.unitsOf(190.dp))
    val stations = ArrayList<Station>()
    val signs = ArrayList<Signpost>()
    val critters = ArrayList<Critter>()
    var cursor = workshopX + chapterGap
    for ((chapterIndex, chapter) in Curriculum.chapters(subject).withIndex()) {
        signs += Signpost(chapter, cursor)
        if (chapterIndex % 2 == 0) critters += Critter(cursor - chapterGap * 0.45f, chapterIndex % 3, Color(chapter.look.base))
        cursor += signGap
        chapter.skills.forEachIndexed { index, skill ->
            stations += Station(skill, chapter, index, cursor)
            if (index < chapter.skills.lastIndex) cursor += spacing
        }
        cursor += chapterGap
    }
    val goalX = cursor
    val width = goalX + max(0.45f, metrics.viewport * 0.35f)
    val unitDp = metrics.unitDp
    val medallion = (unitDp * 0.1f).coerceIn(62.dp, 92.dp)
    val labelWidth = (unitDp * spacing * 0.95f).coerceIn(84.dp, 150.dp)
    val heroHeight = (unitDp * 0.155f).coerceIn(88.dp, 132.dp)
    val standOffset = metrics.unitsOf(medallion / 2 + heroHeight * (2f / 3f) * 0.42f)
    val clear = stations.map { it.x } + signs.map { it.x } + critters.map { it.x } + listOf(padX, workshopX, goalX)
    val geometry = buildPlanetGeometry(biome, metrics, width, roadStart = padX, roadEnd = goalX, keepClear = clear)
    return PlanetLayout(metrics, stations, signs, critters, padX, workshopX, goalX, width, geometry, medallion, labelWidth, heroHeight, standOffset)
}

private sealed interface Selection {
    data class Level(val skill: Skill) : Selection
    data object Review : Selection
}

/** A subject as a planet to walk on: a winding path of levels through a landscape, with the hero on it. */
@Composable
fun PlanetScreen(vm: KometViewModel, subject: Subject) {
    val profile = vm.profile ?: return
    val biome = Biomes.of(subject)
    val tone = subjectTone(subject)
    val feedback = LocalFeedback.current
    val motion = LocalMotion.current
    val time = rememberSceneTime()
    val scope = rememberCoroutineScope()

    val recommended = remember(profile) { Progression.recommended(profile, subject) }
    val weak = remember(profile) { Progression.weakSkills(profile, subject) }
    // The level played a moment ago, if any: the hero starts there and walks on to the next one.
    val justPlayed = remember(subject) {
        val now = System.currentTimeMillis()
        Curriculum.skills(subject)
            .mapNotNull { skill -> profile.skills[skill.id]?.let { skill to it.lastPlayed } }
            .filter { now - it.second < 20 * 60 * 1000L }
            .maxByOrNull { it.second }?.first
    }
    val startSkill = justPlayed ?: recommended ?: Curriculum.skills(subject).first()
    val walkOn = justPlayed != null && recommended != null && justPlayed != recommended

    var selection by remember(subject) { mutableStateOf<Selection>(Selection.Level(recommended ?: startSkill)) }
    var pose by remember(subject) { mutableStateOf(if (walkOn) HeroPose.CHEER else HeroPose.WAVE) }
    var facingLeft by remember(subject) { mutableStateOf(false) }
    var boltMood by remember(subject) { mutableStateOf(BoltMood.IDLE) }
    val shakes = remember(subject) { mutableStateMapOf<String, Int>() }
    val hops = remember(subject) { mutableStateMapOf<Int, Long>() }
    val camera = rememberSceneCamera(subject)

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val landscape = maxWidth > maxHeight * 1.1f
        val cardWidth = if (landscape) (maxWidth * 0.4f).coerceIn(300.dp, 380.dp) else 0.dp
        val cardWidthPx = with(LocalDensity.current) { cardWidth.toPx() }
        // Where the chosen level should sit: in the middle of what the card leaves free.
        fun focusFraction(metrics: SceneMetrics) = if (landscape) (metrics.widthPx - cardWidthPx) / 2f / metrics.widthPx else 0.5f

        SceneViewport(
            camera = camera,
            layoutFor = { planetLayout(subject, biome, it) },
            worldWidth = { it.width },
            startLeft = { layout -> (layout.station(startSkill)?.x ?: layout.padX) - layout.metrics.viewport * focusFraction(layout.metrics) },
            topShare = 0.45f,
        ) { layout ->
            val metrics = metrics
            val fraction = focusFraction(metrics)
            val heroX = remember(subject) { Animatable((layout.station(startSkill)?.x ?: layout.padX) - layout.standOffset) }
            // Where the hero stands, so a new screen size puts it back at the same place.
            var heroSpot by remember(subject) { mutableStateOf<Skill?>(startSkill) }
            val walk = remember { arrayOfNulls<Job>(1) }

            fun walkTo(x: Float, spot: Skill?) {
                heroSpot = spot
                if (!motion) {
                    walk[0]?.cancel()
                    scope.launch { heroX.snapTo(x - layout.standOffset) }
                    camera.jumpTo(x - metrics.viewport * fraction)
                    return
                }
                val previous = walk[0]
                walk[0] = scope.launch {
                    previous?.cancelAndJoin()
                    walkHero(heroX, x - layout.standOffset, { pose = it }, { facingLeft = it }, feedback)
                }
                scope.launch { camera.glideTo(x, 700, fraction) }
            }

            LaunchedEffect(layout) {
                val spot = layout.station(heroSpot)
                if (spot != null && walk[0]?.isActive != true) heroX.snapTo(spot.x - layout.standOffset)
            }
            LaunchedEffect(subject) {
                delay(if (motion) 1300 else 0)
                val target = layout.station(recommended)
                if (walkOn && target != null) {
                    boltMood = BoltMood.HAPPY
                    walkTo(target.x, target.skill)
                    walk[0]?.join()
                    boltMood = BoltMood.IDLE
                }
                pose = HeroPose.IDLE
            }

            fun select(station: Station) {
                selection = Selection.Level(station.skill)
                if (!Progression.isUnlocked(profile, station.chapter, station.index)) {
                    shakes[station.skill.id] = (shakes[station.skill.id] ?: 0) + 1
                    feedback.sfx(Sfx.WRONG, 0.7f)
                    boltMood = BoltMood.SURPRISED
                    scope.launch {
                        delay(900)
                        boltMood = BoltMood.IDLE
                    }
                    return
                }
                walkTo(station.x, station.skill)
            }

            PlanetBackdrop(biome, layout.geometry, time)

            // Only what is near the view is composed; a planet can be long.
            val window by remember(layout) { derivedStateOf { floor(camera.position / 0.5f).toInt() } }
            val left = window * 0.5f - 0.8f
            val right = window * 0.5f + metrics.viewport + 1.3f

            if (layout.padX in left..right) LandingPad(layout.padX, time, onTap = { vm.back() })

            if (weak.isNotEmpty() && layout.workshopX in left..right) {
                Workshop(layout.workshopX, tone.face, time, selected = selection == Selection.Review) {
                    selection = Selection.Review
                    walkTo(layout.workshopX - 0.03f, null)
                }
            }

            for (sign in layout.signs) {
                if (sign.x in left..right) ChapterSign(sign, Progression.completed(profile, sign.chapter.skills))
            }

            for ((index, critter) in layout.critters.withIndex()) {
                if (critter.x !in left..right) continue
                CritterView(critter, time, hopStarted = { hops[index] }) {
                    hops[index] = System.currentTimeMillis()
                    feedback.sfx(Sfx.BOING, 0.8f)
                }
            }

            for (station in layout.stations) {
                if (station.x !in left..right) continue
                val skill = station.skill
                StationView(
                    station = station,
                    size = layout.medallion,
                    labelWidth = layout.labelWidth,
                    stars = profile.stars(skill.id),
                    unlocked = Progression.isUnlocked(profile, station.chapter, station.index),
                    recommended = skill == recommended,
                    selected = (selection as? Selection.Level)?.skill == skill,
                    shake = shakes[skill.id] ?: 0,
                    time = time,
                    onTap = { select(station) },
                )
            }

            if (layout.goalX in left..right) GoalMonument(layout.goalX, tone.top, time) { feedback.sfx(Sfx.SPARKLE) }

            val heroHeight = layout.heroHeight
            Astronaut(
                look = profile.hero,
                time = time,
                pose = { pose },
                facingLeft = { facingLeft },
                modifier = Modifier
                    .worldAt({ Offset(heroX.value, PlanetGround.pathY(heroX.value) + 0.012f) })
                    .size(heroHeight * (2f / 3f), heroHeight),
            )
            Bolt(
                time = time,
                mood = { boltMood },
                modifier = Modifier
                    .worldAt({
                        val side = if (facingLeft) 1f else -1f
                        Offset(heroX.value + side * 0.055f, PlanetGround.pathY(heroX.value) - metrics.unitsOf(heroHeight) - 0.035f)
                    })
                    .size(units(0.08f, 48.dp, 70.dp))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                        feedback.sfx(Sfx.BEEP)
                        boltMood = BoltMood.TALK
                        when (val chosen = selection) {
                            is Selection.Level -> vm.say(chosen.skill.title)
                            Selection.Review -> vm.say(S.review)
                        }
                        scope.launch {
                            delay(1600)
                            boltMood = BoltMood.IDLE
                        }
                    },
            )

            PlanetForeground(biome, layout.geometry, time)
        }

        Row(
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            RoundIconButton(KometIcons.Back, S.back.str(), onClick = { vm.back() }, size = 52.dp)
            NamePlate(S.subject(subject).str(), tone.face, tone.top)
            Spacer(Modifier.weight(1f))
            val skills = Curriculum.skills(subject)
            Pill("${Progression.earnedStars(profile, skills)} / ${skills.size * 3}", star = true)
        }

        val cardModifier = if (landscape) {
            Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.End + WindowInsetsSides.Bottom))
                .padding(top = 74.dp, end = 14.dp, bottom = 14.dp)
                .width(cardWidth)
        } else {
            Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                .padding(12.dp)
                .widthIn(max = 560.dp)
                .fillMaxWidth()
        }
        AnimatedContent(
            targetState = selection,
            transitionSpec = { (fadeIn(tween(180)) + slideInVertically(tween(220)) { it / 6 }) togetherWith fadeOut(tween(120)) },
            modifier = cardModifier,
            label = "levelCard",
        ) { chosen ->
            when (chosen) {
                is Selection.Level -> LevelCard(vm, chosen.skill, compact = landscape && maxHeight < 480.dp)
                Selection.Review -> ReviewCard(vm, subject, weak.map { it.title.str() })
            }
        }
    }
}

/** Walks the hero along the path to [target], with footsteps. */
private suspend fun walkHero(
    heroX: Animatable<Float, *>,
    target: Float,
    setPose: (HeroPose) -> Unit,
    setFacing: (Boolean) -> Unit,
    feedback: Feedback,
) {
    val distance = abs(target - heroX.value)
    if (distance < 0.01f) {
        setPose(HeroPose.IDLE)
        return
    }
    setFacing(target < heroX.value)
    setPose(HeroPose.WALK)
    val millis = (distance * 1700).toInt().coerceIn(260, 1900)
    try {
        coroutineScope {
            val steps = launch {
                while (true) {
                    feedback.sfx(Sfx.STEP, 0.35f)
                    delay(270)
                }
            }
            heroX.animateTo(target, tween(millis, easing = if (distance > 0.5f) FastOutSlowInEasing else LinearEasing))
            steps.cancel()
        }
    } finally {
        setPose(HeroPose.IDLE)
    }
}

@Composable
private fun NamePlate(text: String, face: Color, top: Color) {
    GameText(
        text,
        style = MaterialTheme.typography.headlineSmall,
        fontSize = cappedSp(22.sp),
        maxLines = 1,
        modifier = Modifier
            .border(2.dp, K.Outline, RoundedCornerShape(50))
            .gloss(face, RoundedCornerShape(50), top = top)
            .padding(horizontal = 18.dp, vertical = 6.dp),
    )
}

@Composable
private fun SceneScope.StationView(
    station: Station,
    size: Dp,
    labelWidth: Dp,
    stars: Int,
    unlocked: Boolean,
    recommended: Boolean,
    selected: Boolean,
    shake: Int,
    time: State<Float>,
    onTap: () -> Unit,
) {
    val skill = station.skill
    val tone = subjectTone(skill.subject)
    val half = metrics.unitsOf(size / 2)
    if (recommended || selected) {
        // Gold for the next mission, white for the chosen level.
        Canvas(
            Modifier
                .worldAt(station.x, station.y, anchorY = 0.5f)
                .size(size + 24.dp),
        ) {
            val pulse = 1f + 0.05f * sin(time.value * 5f)
            val radius = this.size.minDimension / 2 - 3.dp.toPx()
            if (recommended) drawCircle(K.Gold, radius * pulse, style = Stroke(4.dp.toPx()))
            if (selected) drawCircle(Color.White, radius * (if (recommended) 0.88f else 1f), style = Stroke(3.dp.toPx()))
        }
    }
    StarRow(stars, size = 17.dp, modifier = Modifier.worldAt(station.x, station.y - half - metrics.unitsOf(4.dp), anchorY = 1f))
    Box(
        Modifier
            .worldAt(station.x, station.y, anchorY = 0.5f)
            .shake(shake, true),
    ) {
        PressSurface(
            onClick = onTap,
            modifier = Modifier
                .size(size)
                .semantics { contentDescription = skill.title.nn },
            face = if (unlocked) tone.face else K.SurfaceHigh,
            edge = if (unlocked) tone.edge else K.SurfaceLow,
            top = if (unlocked) tone.top else K.SurfaceHigh,
            shape = CircleShape,
            depth = 5.dp,
            tapSound = unlocked,
            contentPadding = PaddingValues(0.dp),
        ) {
            if (unlocked) {
                LevelSymbol(skill, size)
            } else {
                Icon(KometIcons.Lock, contentDescription = null, tint = K.Faint, modifier = Modifier.size(size * 0.4f))
            }
        }
    }
    Text(
        skill.title.str(),
        style = MaterialTheme.typography.labelMedium,
        fontSize = cappedSp(14.sp),
        lineHeight = cappedSp(17.sp),
        color = if (unlocked) K.Text else K.Muted,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .worldAt(station.x, station.y + half + metrics.unitsOf(6.dp), anchorY = 0f)
            .widthIn(max = labelWidth)
            .background(K.SpaceTop.copy(alpha = 0.72f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

@Composable
private fun SceneScope.ChapterSign(sign: Signpost, completed: Int) {
    val look = sign.chapter.look
    val tone = subjectTone(sign.chapter.subject)
    Column(
        Modifier.worldAt(sign.x, PlanetGround.pathY(sign.x) - 0.02f, anchorY = 1f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            Modifier
                .widthIn(max = 200.dp)
                .border(2.dp, K.Outline, RoundedCornerShape(14.dp))
                .gloss(lerp(tone.edge, K.SpaceTop, 0.55f), RoundedCornerShape(14.dp), top = lerp(tone.edge, K.SpaceTop, 0.25f))
                .border(1.5.dp, tone.top.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                .padding(start = 6.dp, end = 12.dp, top = 5.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            PlanetArt(look, Modifier.size(34.dp), glow = false)
            Column {
                GameText(sign.chapter.title.str(), style = MaterialTheme.typography.titleMedium, fontSize = cappedSp(17.sp), maxLines = 1)
                Text("$completed / ${sign.chapter.skills.size}", style = MaterialTheme.typography.labelMedium, fontSize = cappedSp(14.sp), color = Color.White.copy(alpha = 0.85f), fontWeight = FontWeight.Bold)
            }
        }
        Canvas(Modifier.size(12.dp, 30.dp)) {
            drawRect(K.Outline, Offset(size.width * 0.22f, 0f), Size(size.width * 0.56f, size.height))
            drawRect(Color(0xFF9A7552), Offset(size.width * 0.34f, 0f), Size(size.width * 0.32f, size.height))
        }
    }
}

@Composable
private fun SceneScope.LandingPad(x: Float, time: State<Float>, onTap: () -> Unit) {
    val width = units(0.2f, 96.dp, 160.dp)
    Box(
        Modifier
            .worldAt(x, PlanetGround.pathY(x) + 0.03f, anchorY = 1f)
            .size(width, width * 1.2f)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, role = Role.Button, onClickLabel = S.back.str(), onClick = onTap),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Canvas(Modifier.size(width, width * 0.3f)) {
            val w = size.width
            val h = size.height
            drawOval(K.Outline, Offset(0f, h * 0.2f), Size(w, h * 0.8f))
            drawOval(Brush.verticalGradient(listOf(Color(0xFF9AA6D8), Color(0xFF4A5494)), startY = h * 0.25f, endY = h), Offset(w * 0.03f, h * 0.25f), Size(w * 0.94f, h * 0.66f))
            val blink = if (sin(time.value * 4f) > 0f) 1f else 0.35f
            for (i in 0 until 5) drawCircle(K.Gold.copy(alpha = blink), h * 0.07f, Offset(w * (0.16f + i * 0.17f), h * 0.58f))
        }
        RocketArt(
            Modifier
                .padding(bottom = width * 0.12f)
                .size(width * 0.5f, width * 0.82f),
            body = Color.White,
            accent = K.Race,
        )
    }
}

@Composable
private fun SceneScope.Workshop(x: Float, accent: Color, time: State<Float>, selected: Boolean, onTap: () -> Unit) {
    val size = units(0.13f, 72.dp, 110.dp)
    val ink = remember(accent) { PropInk(Color(0xFF3FAE6A), Color(0xFF6B3A1E), Color(0xFF7A86C8), accent) }
    Box(
        Modifier
            .worldAt(x, PlanetGround.pathY(x) + 0.02f, anchorY = 1f)
            .size(size * 1.3f, size)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, role = Role.Button, onClickLabel = S.review.str(), onClick = onTap),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawProp(PropKind.DOME, this.size.width / 2, this.size.height * 0.98f, this.size.height * 0.95f, ink, 2, time.value)
            if (selected) drawCircle(Color.White, this.size.minDimension * 0.58f, style = Stroke(3.dp.toPx()))
        }
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .size(34.dp)
                .border(2.dp, K.Outline, CircleShape)
                .gloss(accent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(KometIcons.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun SceneScope.CritterView(critter: Critter, time: State<Float>, hopStarted: () -> Long?, onTap: () -> Unit) {
    val size = units(0.085f, 48.dp, 80.dp)
    Canvas(
        Modifier
            .worldAt(critter.x, PlanetGround.pathY(critter.x) - 0.02f, anchorY = 1f)
            .size(size)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onTap),
    ) {
        // The clock is read every frame anyway, so the hop follows the wall clock.
        val t = time.value
        val started = hopStarted()
        val jump = if (started == null) 0f else ((System.currentTimeMillis() - started) / 520f).coerceIn(0f, 1f)
        drawCritter(critter.color, critter.kind, t, if (jump >= 1f) 0f else jump)
    }
}

@Composable
private fun SceneScope.GoalMonument(x: Float, accent: Color, time: State<Float>, onTap: () -> Unit) {
    val size = units(0.24f, 110.dp, 190.dp)
    Canvas(
        Modifier
            .worldAt(x, PlanetGround.pathY(x) + 0.02f, anchorY = 1f)
            .size(size * 0.8f, size)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onTap),
    ) {
        val w = this.size.width
        val h = this.size.height
        val t = time.value
        val glowCenter = Offset(w / 2, h * 0.36f)
        drawCircle(Brush.radialGradient(listOf(K.Gold.copy(alpha = 0.45f + 0.1f * sin(t * 2f)), Color.Transparent), glowCenter, w * 0.7f), w * 0.7f, glowCenter)
        drawRoundRect(K.Outline, Offset(w * 0.22f, h * 0.78f), Size(w * 0.56f, h * 0.2f), CornerRadius(8f))
        drawRoundRect(Brush.verticalGradient(listOf(Color(0xFF8C96D8), Color(0xFF3A437E)), startY = h * 0.78f, endY = h), Offset(w * 0.24f, h * 0.79f), Size(w * 0.52f, h * 0.18f), CornerRadius(6f))
        rotate(sin(t * 0.8f) * 6f, Offset(w / 2, h * 0.4f)) {
            val star = Path()
            for (i in 0 until 10) {
                val r = if (i % 2 == 0) w * 0.4f else w * 0.18f
                val a = -PI / 2 + i * PI / 5
                val px = w / 2 + (r * cos(a)).toFloat()
                val py = h * 0.4f + (r * sin(a)).toFloat()
                if (i == 0) star.moveTo(px, py) else star.lineTo(px, py)
            }
            star.close()
            drawPath(star, Brush.verticalGradient(listOf(K.GoldTop, K.Gold, K.GoldDeep), startY = h * 0.05f, endY = h * 0.75f))
            drawPath(star, K.Outline, style = Stroke(3.dp.toPx(), join = StrokeJoin.Round))
            drawCircle(Color.White.copy(alpha = 0.8f), w * 0.04f, Offset(w * 0.42f, h * 0.3f))
        }
        for (i in 0 until 4) {
            val phase = (t * 0.6f + i * 0.25f) % 1f
            val sx = w * (0.2f + 0.6f * ((i * 37 % 10) / 10f))
            val sy = h * (0.7f - phase * 0.6f)
            drawCircle(accent.copy(alpha = 1f - phase), 3.dp.toPx() * (1f - phase * 0.5f), Offset(sx, sy))
        }
    }
}

/** The chosen level: what it is, how it went, and one big button. */
@Composable
private fun LevelCard(vm: KometViewModel, skill: Skill, compact: Boolean) {
    val profile = vm.profile ?: return
    val tone = subjectTone(skill.subject)
    val unlocked = Progression.isUnlocked(profile, skill)
    val stars = profile.stars(skill.id)
    val favorite = skill.id in profile.favorites
    val shape = RoundedCornerShape(28.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .border(2.5.dp, K.Outline, shape)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(lerp(tone.edge, K.SpaceTop, 0.2f), lerp(tone.edge, K.SpaceTop, 0.72f))))
            .border(1.5.dp, Brush.verticalGradient(listOf(tone.top.copy(alpha = 0.5f), Color.Transparent)), shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (!compact) LevelMedallion(skill, 56.dp)
            Column(Modifier.weight(1f)) {
                GameText(skill.title.str(), style = MaterialTheme.typography.headlineSmall, fontSize = cappedSp(22.sp), maxLines = 2)
                Text(
                    Curriculum.chapterOf(skill).title.str() + " · " + S.gradeShort[skill.grade.coerceIn(0, 3)].str(),
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = cappedSp(14.sp),
                    color = K.Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            RoundIconButton(
                if (favorite) KometIcons.Heart else KometIcons.HeartOutline,
                (if (favorite) S.removeFavorite else S.addFavorite).str(),
                onClick = { vm.toggleFavorite(skill.id) },
                size = 46.dp,
                tint = if (favorite) K.RaceTop else K.Muted,
            )
            RoundIconButton(KometIcons.Speaker, S.readAloud.str(), onClick = { vm.say(skill.title) }, size = 46.dp)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StarRow(stars, size = 26.dp)
            if (!compact) {
                Text(skill.detail.str(), style = MaterialTheme.typography.bodyMedium, fontSize = cappedSp(15.sp), lineHeight = cappedSp(20.sp), color = K.Text, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            }
        }
        if (unlocked) {
            BigButton(
                text = if (stars > 0) S.playAgain.str() else S.start.str(),
                onClick = { vm.startSkill(skill) },
                icon = KometIcons.Play,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(K.SpaceTop.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(KometIcons.Lock, contentDescription = null, tint = K.Muted, modifier = Modifier.size(24.dp))
                Text(S.lockedHint.str(), style = MaterialTheme.typography.titleSmall, fontSize = cappedSp(15.sp), color = K.Muted)
            }
        }
    }
}

@Composable
private fun ReviewCard(vm: KometViewModel, subject: Subject, names: List<String>) {
    val tone = subjectTone(subject)
    val shape = RoundedCornerShape(28.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .border(2.5.dp, K.Outline, shape)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(lerp(tone.edge, K.SpaceTop, 0.2f), lerp(tone.edge, K.SpaceTop, 0.72f))))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier
                    .size(56.dp)
                    .border(2.dp, K.Outline, CircleShape)
                    .gloss(tone.face, CircleShape, top = tone.top),
                contentAlignment = Alignment.Center,
            ) {
                Icon(KometIcons.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
            }
            Column(Modifier.weight(1f)) {
                GameText(S.review.str(), style = MaterialTheme.typography.headlineSmall, fontSize = cappedSp(22.sp))
                Text(S.reviewDetail(names.joinToString(", ")).str(), style = MaterialTheme.typography.bodyMedium, fontSize = cappedSp(15.sp), lineHeight = cappedSp(20.sp), color = K.Text, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }
        BigButton(text = S.reviewStart.str(), onClick = { vm.startReview(subject) }, icon = KometIcons.Play, modifier = Modifier.fillMaxWidth())
    }
}
