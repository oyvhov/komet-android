package app.komet.ui.scene

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.komet.audio.Sfx
import app.komet.domain.HeroLook
import app.komet.domain.HeroPalette
import app.komet.ui.KometViewModel
import app.komet.ui.S
import app.komet.ui.components.BigButton
import app.komet.ui.components.GameText
import app.komet.ui.components.KometIcons
import app.komet.ui.components.LocalFeedback
import app.komet.ui.components.RoundIconButton
import app.komet.ui.components.str
import app.komet.ui.theme.K

/**
 * Builds the child's astronaut: a big figure that waves at every change, and rows of suit, skin, hair
 * and hairstyle to choose from. On wide screens the figure stands beside the choices.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeroCreator(look: HeroLook, onChange: (HeroLook) -> Unit, modifier: Modifier = Modifier) {
    val time = rememberSceneTime()
    val feedback = LocalFeedback.current
    var cheerAt by remember { mutableFloatStateOf(-10f) }
    fun choose(next: HeroLook) {
        feedback.sfx(Sfx.SPARKLE, 0.5f)
        cheerAt = time.value
        onChange(next)
    }

    BoxWithConstraints(modifier.fillMaxWidth()) {
        val wide = maxWidth >= 600.dp
        val figure: @Composable () -> Unit = {
            Box(Modifier.size(170.dp, 230.dp), contentAlignment = Alignment.BottomCenter) {
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(
                        Brush.radialGradient(listOf(Color(HeroPalette.suits[look.safe().suit]).copy(alpha = 0.45f), Color.Transparent), Offset(size.width / 2, size.height * 0.55f), size.width * 0.6f),
                        size.width * 0.6f,
                        Offset(size.width / 2, size.height * 0.55f),
                    )
                }
                Astronaut(
                    look = look,
                    time = time,
                    pose = { if (time.value - cheerAt < 1.2f) HeroPose.CHEER else HeroPose.WAVE },
                    modifier = Modifier.size(140.dp, 210.dp),
                )
            }
        }
        val choices: @Composable () -> Unit = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ChoiceRow(S.heroSuit.str()) {
                    HeroPalette.suits.forEachIndexed { index, color ->
                        Swatch(Color(color), selected = look.suit == index, description = "${S.heroSuit.str()} ${index + 1}") { choose(look.copy(suit = index)) }
                    }
                }
                ChoiceRow(S.heroSkin.str()) {
                    HeroPalette.skins.forEachIndexed { index, color ->
                        Swatch(Color(color), selected = look.skin == index, description = "${S.heroSkin.str()} ${index + 1}") { choose(look.copy(skin = index)) }
                    }
                }
                ChoiceRow(S.heroHair.str()) {
                    HeroPalette.hairs.forEachIndexed { index, color ->
                        Swatch(Color(color), selected = look.hair == index, description = "${S.heroHair.str()} ${index + 1}") { choose(look.copy(hair = index)) }
                    }
                }
                val styleLabel = S.heroHairStyle.str()
                ChoiceRow(styleLabel) {
                    repeat(HeroPalette.HAIR_STYLES) { index ->
                        val chosen = look.hairStyle == index
                        Box(
                            Modifier
                                .size(62.dp)
                                .border(if (chosen) 4.dp else 2.dp, if (chosen) K.Gold else K.Outline, CircleShape)
                                .padding(4.dp)
                                .semantics {
                                    selected = chosen
                                    contentDescription = "$styleLabel ${index + 1}"
                                }
                                .clickable(role = Role.RadioButton) { choose(look.copy(hairStyle = index)) },
                        ) {
                            HeroBadge(look.copy(hairStyle = index), time, 54.dp)
                        }
                    }
                }
            }
        }
        if (wide) {
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp), verticalAlignment = Alignment.CenterVertically) {
                figure()
                Box(Modifier.weight(1f)) { choices() }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                figure()
                choices()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChoiceRow(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.titleMedium, color = K.Muted, fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            content()
        }
    }
}

@Composable
private fun Swatch(color: Color, selected: Boolean, description: String, onClick: () -> Unit) {
    Canvas(
        Modifier
            .size(48.dp)
            .semantics {
                this.selected = selected
                contentDescription = description
            }
            .clickable(role = Role.RadioButton, onClick = onClick),
    ) {
        val r = size.minDimension / 2
        if (selected) drawCircle(K.Gold, r)
        val inner = if (selected) r - 5.dp.toPx() else r - 2.dp.toPx()
        drawCircle(K.Outline, inner)
        drawCircle(
            Brush.radialGradient(listOf(lerp(color, Color.White, 0.35f), color, lerp(color, Color.Black, 0.25f)), center - Offset(inner * 0.35f, inner * 0.35f), inner * 1.6f),
            inner - 2.dp.toPx(),
        )
        drawCircle(Color.White.copy(alpha = 0.45f), inner * 0.2f, center - Offset(inner * 0.4f, inner * 0.4f))
        if (selected) drawCircle(Color.White, inner - 2.dp.toPx(), style = Stroke(2.dp.toPx()))
    }
}

/** Lets the child change the astronaut later, from the star map. */
@Composable
fun HeroEditorScreen(vm: KometViewModel) {
    val profile = vm.profile ?: return
    var look by remember(profile.id) { mutableStateOf(profile.hero) }
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            Modifier
                .widthIn(max = 900.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RoundIconButton(KometIcons.Back, S.back.str(), onClick = { vm.back() }, size = 52.dp)
            GameText(S.editHero.str(), style = MaterialTheme.typography.headlineMedium, maxLines = 1, modifier = Modifier.weight(1f))
        }
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .widthIn(max = 900.dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            HeroCreator(look, onChange = { look = it })
        }
        BigButton(
            text = S.done.str(),
            onClick = {
                vm.updateHero(look)
                vm.back()
            },
            icon = KometIcons.Check,
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        )
    }
}
