package app.komet.ui.scene

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.komet.domain.SolarDestination
import app.komet.domain.Txt
import app.komet.ui.components.LocalFeedback
import app.komet.ui.components.str
import app.komet.ui.components.BigButton
import app.komet.ui.components.KometIcons
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal val passportTitle = Txt("Reisepass")
private val ink = Color(0xFF244653)
private val paper = Color(0xFFF2E8CD)

/** One impression in the child's atlas. The dotted placeholders stay recognisable without colour. */
@Composable
internal fun PlanetSeal(planet: SolarDestination, earned: Boolean, modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val r = size.minDimension * .46f
            val thin = r * .035f
            if (earned) {
                drawCircle(ink, r)
                drawCircle(paper.copy(alpha = .75f), r * .92f, style = Stroke(thin))
                repeat(24) { i ->
                    val angle = 2 * PI * i / 24
                    val v = Offset(cos(angle).toFloat(), sin(angle).toFloat())
                    drawLine(paper.copy(alpha = .75f), center + v * (r * .83f), center + v * (r * .9f), thin)
                }
            } else {
                drawCircle(ink.copy(alpha = .06f), r)
                drawCircle(ink.copy(alpha = .52f), r, style = Stroke(thin,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(r * .15f, r * .09f))))
            }
        }
        if (earned) SolarGlobe(planet, Modifier.fillMaxSize(.70f))
        else Text(planet.planet.fromSun.toString(), color = ink, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
internal fun PlanetPassport(stamps: Set<SolarDestination>, onOpen: (SolarDestination) -> Unit, onHunt: () -> Unit) {
    val shape = RoundedCornerShape(8.dp, 22.dp, 22.dp, 8.dp)
    val feedback = LocalFeedback.current
    Column(Modifier.fillMaxWidth().clip(shape)
        .background(Brush.horizontalGradient(listOf(Color(0xFFC9B989), paper, Color(0xFFF9F3E3)),
            endX = with(LocalDensity.current) { 40.dp.toPx() }))
        .border(1.dp, Color(0xFFBFAA7E), shape).padding(start = 18.dp, end = 12.dp, top = 16.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(passportTitle.str(), Modifier.weight(1f), color = ink, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text("${stamps.size} / ${SolarDestination.entries.size}", color = ink, style = MaterialTheme.typography.titleSmall)
        }
        Text((if (stamps.size == SolarDestination.entries.size) Txt("Du har stempel frå alle åtte planetane!", "Du har stempel fra alle åtte planetene!")
            else Txt("Fullfør eit planetoppdrag og få eit stempel.", "Fullfør et planetoppdrag og få et stempel.")).str(),
            style = MaterialTheme.typography.bodyMedium, color = ink)
        Spacer(Modifier.fillMaxWidth().height(1.dp).background(ink.copy(alpha = .24f)))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val cols = if (maxWidth >= 312.dp && (LocalDensity.current.fontScale < 1.5f || maxWidth >= 500.dp)) 4 else 2
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SolarDestination.entries.chunked(cols).forEach { planets ->
                    Row {
                        planets.forEach { planet ->
                            val owned = planet in stamps
                            val status = (if (owned) Txt("Stempel samla", "Stempel samlet") else Txt("Ledig stempelplass")).str()
                            Column(Modifier.weight(1f).heightIn(min = 64.dp).clip(RoundedCornerShape(12.dp))
                                .clickable(role = Role.Button) { feedback.tap(); onOpen(planet) }
                                .semantics { contentDescription = status }.padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally) {
                                PlanetSeal(planet, owned, Modifier.size(48.dp))
                                Text(planet.planet.name.str(), color = ink, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }
        BigButton(Txt("Planetjakt").str(), onHunt, Modifier.fillMaxWidth(), face = ink, edge = Color(0xFF112D37), icon = KometIcons.Compass)
    }
}
