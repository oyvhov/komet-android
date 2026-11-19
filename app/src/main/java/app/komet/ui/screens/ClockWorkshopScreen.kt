package app.komet.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.komet.domain.ClockTime
import app.komet.domain.Txt
import app.komet.ui.KometViewModel
import app.komet.ui.S
import app.komet.ui.components.CloseButton
import app.komet.ui.components.GameText
import app.komet.ui.components.KometIcons
import app.komet.ui.components.Panel
import app.komet.ui.components.PressSurface
import app.komet.ui.components.RoundIconButton
import app.komet.ui.components.fixedSp
import app.komet.ui.components.str
import app.komet.ui.theme.K

private val workshopName = Txt("Klokkeverkstad", "Klokkeverksted")
private val clockName = Txt("Klokka")
internal val moveHands = Txt("Dra i visarane eller bruk knappane.", "Dra i viserne eller bruk knappene.")
private val changeHalfDay = Txt("Flytt tida 12 timar fram", "Flytt tiden 12 timer fram")

@Composable
fun ClockWorkshopButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    PressSurface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 64.dp),
        face = K.Gold,
        edge = K.GoldDeep,
        top = K.GoldTop,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.clearAndSetSemantics {}) {
                WorkshopDial(ClockTime.at(10, 10), Modifier.size(36.dp))
            }
            GameText(workshopName.str(), style = MaterialTheme.typography.titleMedium, maxLines = 1, autoFit = true)
        }
    }
}

@Composable
fun ClockWorkshopScreen(vm: KometViewModel) {
    var practice by rememberSaveable { mutableStateOf(false) }
    val modes = rememberSaveableStateHolder()
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BoxWithConstraints(Modifier.widthIn(max = 1060.dp).fillMaxWidth().padding(horizontal = 16.dp)) {
            val wide = maxWidth >= 650.dp
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GameText(
                        clockName.str(),
                        modifier = Modifier.weight(1f).semantics { heading() },
                        style = MaterialTheme.typography.headlineMedium,
                        maxLines = 1,
                        autoFit = true,
                    )
                    if (wide) ClockModes(practice, { practice = it }, Modifier.widthIn(max = 278.dp))
                    CloseButton(onClick = { vm.back() })
                }
                if (!wide) ClockModes(practice, { practice = it }, Modifier.fillMaxWidth())
            }
        }
        modes.SaveableStateProvider(practice.toString()) {
            val scroll = rememberScrollState()
            Box(
                Modifier.weight(1f).widthIn(max = 1060.dp).fillMaxWidth()
                    .verticalScroll(scroll).padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 18.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                if (practice) ClockPractice(vm, scroll) else ClockPlayground(vm)
            }
        }
    }
}

@Composable
private fun ClockModes(practice: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier) {
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier.background(K.SurfaceLow, shape).border(1.dp, K.Line.copy(alpha = 0.5f), shape).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ClockMode(Txt("Leik", "Lek"), !practice, { onChange(false) })
        ClockMode(Txt("Øv"), practice, { onChange(true) })
    }
}

@Composable
private fun RowScope.ClockMode(label: Txt, active: Boolean, onClick: () -> Unit) {
    PressSurface(
        onClick = onClick,
        modifier = Modifier.weight(1f).heightIn(min = 64.dp).semantics { selected = active },
        face = if (active) K.SurfaceHigh else K.SurfaceLow,
        top = if (active) lerp(K.SurfaceHigh, K.ReadingTop, 0.28f) else K.Surface,
        edge = K.SpaceTop,
        shape = RoundedCornerShape(18.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 9.dp),
    ) {
        GameText(label.str(), color = if (active) K.GoldTop else K.Muted, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun ClockPlayground(vm: KometViewModel) {
    var minutes by rememberSaveable { mutableIntStateOf(9 * 60 + 30) }
    val time = ClockTime.fromMinutes(minutes)
    val daytime = time.hour in 6..17
    val dayPart = when (time.hour) {
        in 0..5 -> Txt("Natt")
        in 6..11 -> Txt("Morgon", "Morgen")
        in 12..17 -> Txt("Ettermiddag")
        else -> Txt("Kveld")
    }
    ClockStage(
        dial = { size ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                WorkshopDial(time, Modifier.size(size), onChange = { minutes = it.totalMinutes })
                ClockInstruction(moveHands)
            }
        },
        controls = {
            ClockPanel {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            DayOrb(daytime, Modifier.size(16.dp))
                            Text(dayPart.str(), color = K.Muted, style = MaterialTheme.typography.labelLarge)
                        }
                        DigitalTime(time)
                    }
                    if (vm.speechAvailable) {
                        RoundIconButton(
                            KometIcons.Speaker, S.readAloud.str(), size = 64.dp,
                            onClick = { vm.say(time.spokenDigital() + Txt(" ") + time.spokenDial()) },
                        )
                    }
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        time.spokenDial().str(),
                        modifier = Modifier.weight(1f),
                        color = K.Text,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    WorkshopAction(
                        Txt("+12 t"),
                        { minutes = (time + 720).totalMinutes },
                        modifier = Modifier.widthIn(min = 82.dp, max = 106.dp),
                        description = changeHalfDay.str(),
                    )
                }
            }
            ClockAdjustments(time, 1, { minutes = it.totalMinutes })
        },
    )
}

@Composable
private fun DayOrb(daytime: Boolean, modifier: Modifier) {
    Canvas(modifier) {
        val radius = size.minDimension * 0.36f
        val color = if (daytime) ClockGold else K.ReadingTop
        drawCircle(Brush.radialGradient(listOf(color.copy(alpha = 0.48f), Color.Transparent)), radius = radius * 1.4f)
        drawCircle(color, radius = radius)
        if (!daytime) drawCircle(K.Surface, radius = radius * 0.83f, center = center.copy(x = center.x + radius * 0.53f, y = center.y - radius * 0.4f))
    }
}

@Composable
internal fun ClockStage(
    dial: @Composable (Dp) -> Unit,
    controls: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        val availableWidth = maxWidth
        if (availableWidth >= 620.dp) {
            Row(
                Modifier.widthIn(max = 900.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1.05f), contentAlignment = Alignment.Center) {
                    dial((availableWidth * 0.46f).coerceAtMost(416.dp))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp), content = controls)
            }
        } else {
            Column(
                Modifier.widthIn(max = 440.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                dial((availableWidth * 0.82f).coerceAtMost(320.dp))
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp), content = controls)
            }
        }
    }
}

@Composable
internal fun ClockPanel(content: @Composable ColumnScope.() -> Unit) {
    Panel(Modifier.fillMaxWidth(), padding = 14.dp, content = content)
}

@Composable
internal fun DigitalTime(time: ClockTime) {
    val digits = time.digital
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(color = ClockGold)) { append(digits.take(2)) }
            withStyle(SpanStyle(color = K.Muted)) { append(':') }
            withStyle(SpanStyle(color = ClockMint)) { append(digits.takeLast(2)) }
        },
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = fixedSp(40.dp),
        lineHeight = fixedSp(46.dp),
        maxLines = 1,
        softWrap = false,
        modifier = Modifier
            .background(K.SpaceTop.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
            .border(1.dp, K.Line.copy(alpha = 0.42f), RoundedCornerShape(12.dp))
            .padding(horizontal = 9.dp, vertical = 2.dp),
    )
}

@Composable
internal fun ClockAdjustments(
    time: ClockTime,
    minuteStep: Int,
    onChange: (ClockTime) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AdjustHand(
            Txt("Time"), 60, "1", ClockGold,
            Txt("Trekk frå ein time", "Trekk fra én time"),
            Txt("Legg til ein time", "Legg til én time"),
            time, onChange,
        )
        if (minuteStep < 60) {
            val norwegianMinutes = if (minuteStep == 1) "minutt" else "minutter"
            AdjustHand(
                Txt("Minutt", "Minutter"), minuteStep, minuteStep.toString(), ClockMint,
                Txt("Trekk frå $minuteStep minutt", "Trekk fra $minuteStep $norwegianMinutes"),
                Txt("Legg til $minuteStep minutt", "Legg til $minuteStep $norwegianMinutes"),
                time, onChange,
            )
        }
    }
}

@Composable
private fun RowScope.AdjustHand(
    title: Txt,
    amount: Int,
    number: String,
    color: Color,
    decrease: Txt,
    increase: Txt,
    time: ClockTime,
    onChange: (ClockTime) -> Unit,
) {
    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            title.str(),
            color = color,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            HandButton("−$number", decrease, color, Modifier.weight(1f)) { onChange(time + -amount) }
            HandButton("+$number", increase, color, Modifier.weight(1f)) { onChange(time + amount) }
        }
    }
}

@Composable
private fun HandButton(label: String, description: Txt, color: Color, modifier: Modifier, onClick: () -> Unit) {
    PressSurface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 64.dp),
        face = lerp(K.Surface, color, 0.075f),
        edge = K.SurfaceLow,
        description = description.str(),
        contentPadding = PaddingValues(horizontal = 5.dp, vertical = 8.dp),
    ) {
        GameText(label, color = color, fontSize = fixedSp(23.dp), textAlign = TextAlign.Center, maxLines = 1, autoFit = true)
    }
}

@Composable
internal fun WorkshopAction(
    text: Txt,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    primary: Boolean = false,
    description: String? = null,
) {
    PressSurface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 64.dp),
        face = if (primary) K.Gold else K.SurfaceHigh,
        edge = if (primary) K.GoldDeep else K.SurfaceLow,
        description = description,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 9.dp),
    ) {
        GameText(text.str(), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, maxLines = 1, autoFit = true)
    }
}

@Composable
internal fun ClockInstruction(text: Txt) {
    Text(
        text.str(),
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = K.Muted,
        style = MaterialTheme.typography.bodyMedium,
    )
}
