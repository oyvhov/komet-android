package app.komet.ui.scene

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.komet.domain.SolarDestination
import app.komet.ui.components.GameText
import app.komet.ui.components.OptionLook
import app.komet.ui.components.StarGlyph
import app.komet.ui.components.str
import app.komet.ui.theme.K

/** The three candidates stay in answer order, including after a miss or reveal. */
@Composable
internal fun PlanetHunt(planets: List<SolarDestination>, looks: List<OptionLook>, onChoose: (Int) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
        .background(Brush.verticalGradient(listOf(Color(0xFF17283F), K.SpaceTop))).padding(horizontal = 4.dp, vertical = 10.dp)) {
        val diameter = (maxWidth / planets.size * .80f).coerceIn(46.dp, 82.dp)
        Canvas(Modifier.matchParentSize()) {
            val route = Path().apply {
                moveTo(-10f, size.height * .65f)
                quadraticTo(center.x, -size.height * .05f, size.width + 10f, size.height * .65f)
            }
            drawPath(route, K.Faint.copy(alpha = .60f), style = Stroke(1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))))
            repeat(24) { i -> drawCircle(K.Text.copy(alpha = .32f), (if (i % 5 == 0) 1.3.dp else .7.dp).toPx(),
                Offset(size.width * ((i * 47) % 101) / 101, size.height * ((i * 31) % 67) / 67)) }
        }
        Row(Modifier.fillMaxWidth()) {
            planets.forEachIndexed { i, planet ->
                val look = looks[i]
                Column(Modifier.weight(1f).padding(top = if (i == planets.size / 2) 0.dp else 22.dp)
                    .clip(RoundedCornerShape(16.dp)).clickable(enabled = look == OptionLook.NORMAL, role = Role.Button) { onChoose(i) }
                    .alpha(if (look == OptionLook.WRONG || look == OptionLook.FADED) .48f else 1f)
                    .heightIn(min = 64.dp).padding(bottom = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(diameter)) {
                        SolarGlobe(planet, Modifier.fillMaxSize())
                        if (look == OptionLook.CORRECT) StarGlyph(true, Modifier.align(Alignment.BottomEnd).size(24.dp))
                    }
                    GameText(planet.planet.name.str(), style = MaterialTheme.typography.titleMedium, maxLines = 1, autoFit = true,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }
    }
}
