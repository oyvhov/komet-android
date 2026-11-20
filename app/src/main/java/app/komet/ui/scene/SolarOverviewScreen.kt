package app.komet.ui.scene

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import app.komet.ui.components.RoundIconButton
import app.komet.ui.components.KometIcons
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.komet.domain.CardArt
import app.komet.domain.Progression
import app.komet.domain.SolarDestination
import app.komet.domain.Txt
import app.komet.ui.KometViewModel
import app.komet.ui.Screen
import app.komet.ui.components.GameText
import app.komet.ui.components.LocalFeedback
import app.komet.ui.components.ScreenTopBar
import app.komet.ui.components.StarGlyph
import app.komet.ui.components.cappedSp
import app.komet.ui.components.str
import app.komet.ui.theme.K
import kotlin.math.hypot

@Composable
fun SolarOverviewScreen(vm: KometViewModel) {
    val profile = vm.profile ?: return
    val next = SolarDestination.forSkill(Progression.resumable(profile)?.first ?: Progression.recommended(profile))
    val stamps = SolarDestination.stamps(profile)
    val hunt = { vm.startSkill(requireNotNull(app.komet.domain.Curriculum.skill("s_planet_clues"))) }
    val open: (SolarDestination) -> Unit = { if (vm.screen == Screen.SolarOverview) vm.open(Screen.SolarWorld(it)) }
    var showBook by rememberSaveable { mutableStateOf(false) }
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        val spread = maxWidth >= 880.dp && maxWidth > maxHeight
        val stacked = !spread && maxWidth >= 600.dp && maxHeight >= 680.dp
        val compact = !spread && !stacked
        Column(Modifier.widthIn(max = 1280.dp).fillMaxSize().padding(horizontal = 14.dp)) {
            ScreenTopBar((if (compact && showBook) passportTitle else Txt("Kartoversikt")).str(), { vm.back() }) {
                if (compact) RoundIconButton(if (showBook) KometIcons.Compass else KometIcons.Cards,
                    (if (showBook) Txt("Kartoversikt") else passportTitle).str(), { showBook = !showBook }, size = 64.dp)
            }
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val height = maxHeight
                val mapHeight = height.coerceAtLeast(if (maxWidth >= 510.dp) 240.dp else 365.dp)
                when {
                    spread -> Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1.65f).heightIn(max = 620.dp).fillMaxHeight()) { SolarBoard(next, stamps, open) }
                        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(vertical = 8.dp)) { PlanetPassport(stamps, open, hunt) }
                    }
                    stacked -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 16.dp)) {
                        Box(Modifier.fillMaxWidth().height((height * .54f).coerceIn(360.dp, 560.dp))) { SolarBoard(next, stamps, open) }
                        PlanetPassport(stamps, open, hunt)
                    }
                    showBook -> Column(Modifier.verticalScroll(rememberScrollState()).padding(vertical = 8.dp)) { PlanetPassport(stamps, open, hunt) }
                    else -> Column(Modifier.verticalScroll(rememberScrollState())) {
                        Box(Modifier.fillMaxWidth().height(mapHeight)) { SolarBoard(next, stamps, open) }
                    }
                }
            }
        }
    }
}

private val portraitStops = listOf(
    Offset(.30f,.44f), Offset(.68f,.38f), Offset(.70f,.60f), Offset(.27f,.62f),
    Offset(.23f,.25f), Offset(.73f,.17f), Offset(.75f,.84f), Offset(.26f,.89f),
)
private val landscapeStops = listOf(
    Offset(.40f,.38f), Offset(.58f,.66f), Offset(.655f,.31f), Offset(.29f,.70f),
    Offset(.24f,.28f), Offset(.805f,.75f), Offset(.895f,.26f), Offset(.08f,.74f),
)

@Composable
internal fun SolarBoard(next: SolarDestination?, completed: Set<SolarDestination>, onOpen: (SolarDestination) -> Unit) {
    val feedback = LocalFeedback.current
    BoxWithConstraints(Modifier.fillMaxSize().clipToBounds()) {
        val wide = maxWidth >= 510.dp && maxWidth > maxHeight
        val points = if (wide) landscapeStops else portraitStops
        val labelWidth = (maxWidth * if (wide) .17f else .37f).coerceIn(64.dp, 130.dp)
        val unit = minOf(maxWidth * if (wide) .12f else .24f, maxHeight * .18f, 102.dp)
        SolarOrbits(points, Modifier.matchParentSize())
        SolarDestination.entries.forEachIndexed { index, planet ->
            val p = points[index]
            val size = unit * (1.0f - (planet.planet.sizeRank - 1)*.055f)
            // A fixed name area keeps the centre of the illustration exactly on its orbit.
            val hitHeight = (size + 27.dp).coerceAtLeast(64.dp)
            val left = (maxWidth*p.x - labelWidth/2).coerceIn(0.dp, (maxWidth-labelWidth).coerceAtLeast(0.dp))
            val top = (maxHeight*p.y - size/2).coerceIn(0.dp, (maxHeight-hitHeight).coerceAtLeast(0.dp))
            Column(
                Modifier.offset(left, top).size(labelWidth, hitHeight)
                    .clickable(role = Role.Button) { feedback.tap(); onOpen(planet) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(Modifier.size(size)) {
                    SolarGlobe(planet, Modifier.fillMaxSize())
                    if (planet in completed) StarGlyph(true, Modifier.align(Alignment.BottomEnd).size(15.dp))
                }
                GameText(
                    planet.planet.name.str(), Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleSmall, fontSize = cappedSp(15.sp),
                    color = if (planet == next) K.GoldTop else K.Text,
                    maxLines = 1, autoFit = true, textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun SolarOrbits(points: List<Offset>, modifier: Modifier) {
    Canvas(modifier) {
        val sun = Offset(size.width*.50f, size.height*.49f)
        val stretch = size.height / size.width * .84f
        val radius = (size.minDimension*.068f).coerceAtMost(40.dp.toPx())
        val glow = radius*4.2f
        drawCircle(Brush.radialGradient(listOf(Color(0x56DA8955), Color(0x1A9B614B), Color.Transparent), sun, glow), glow, sun)
        points.forEach { point ->
            val rx = hypot(point.x*size.width-sun.x, (point.y*size.height-sun.y)/stretch)
            val ry = rx*stretch
            drawOval(Color(0x487F9DB1), sun-Offset(rx,ry), Size(2*rx,2*ry), style = Stroke(.8.dp.toPx()))
        }
        drawCircle(Brush.radialGradient(listOf(Color(0xFFFFF8CA), Color(0xFFFFCB76), Color(0xFFE68446)),
            sun-Offset(radius*.3f,radius*.35f),radius*1.46f), radius, sun)
    }
}

internal fun SolarDestination.light(): Color =
    (planet.art as? CardArt.Planet)?.look?.let { Color(it.light) } ?: K.ReadingTop
