package app.komet.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.komet.domain.*
import app.komet.ui.KometViewModel
import app.komet.ui.Screen
import app.komet.ui.components.*
import app.komet.ui.theme.K

@Composable
internal fun DiscoveryButton(vm: KometViewModel, kind: DiscoveryWorkshop) {
    val subtitle = when (kind) {
        DiscoveryWorkshop.PIZZA -> Txt("Del ein pizza", "Del en pizza")
        DiscoveryWorkshop.ROVER -> Txt("Programmer ein rover", "Programmer en rover")
        DiscoveryWorkshop.POST -> Txt("Knekk romspråket")
    }
    PressSurface(
        onClick = { vm.open(Screen.Workshop(kind)) },
        modifier = Modifier.fillMaxWidth().heightIn(min = 76.dp),
        face = K.SurfaceHigh, edge = K.SurfaceLow,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Canvas(Modifier.size(54.dp)) {
                when (kind) {
                    DiscoveryWorkshop.PIZZA -> drawPizza(4, 7)
                    DiscoveryWorkshop.ROVER -> drawRover(center, size.minDimension * .88f)
                    DiscoveryWorkshop.POST -> drawPostEnvelope(center, size.minDimension * .92f)
                }
            }
            Column(Modifier.weight(1f)) {
                GameText(kind.title.str(), style = MaterialTheme.typography.titleLarge, maxLines = 1, autoFit = true)
                Text(
                    subtitle.str(),
                    color = K.Muted, style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
fun DiscoveryScreen(vm: KometViewModel, kind: DiscoveryWorkshop) {
    val profile = vm.profile ?: return
    val count = when (kind) {
        DiscoveryWorkshop.PIZZA -> pizzaOrders.size
        DiscoveryWorkshop.ROVER -> marsRoutes.size
        DiscoveryWorkshop.POST -> postOrders.size
    }
    var index by rememberSaveable(profile.id, kind) {
        mutableIntStateOf((profile.nextWorkshopMission[kind] ?: 0).coerceIn(0, count - 1))
    }
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        ScreenTopBar(kind.title.str(), { vm.back() }, Modifier.widthIn(max = 960.dp).padding(horizontal = 16.dp)) {
            Text("${index + 1} / $count", color = K.Muted)
        }
        key(index, kind) {
            val next = (index + 1) % count
            val save = {
                vm.updateProfile(profile.id) { it.copy(nextWorkshopMission = it.nextWorkshopMission + (kind to next)) }
            }
            val nextLabel = if (next == 0) Txt("Spel igjen", "Spill igjen") else Txt("Neste oppdrag")
            when (kind) {
                DiscoveryWorkshop.PIZZA -> PizzaStudio(vm, pizzaOrders[index], nextLabel, save) { index = next }
                DiscoveryWorkshop.ROVER -> RoverStudio(vm, marsRoutes[index], nextLabel, save) { index = next }
                DiscoveryWorkshop.POST -> PostStudio(vm, postOrders[index], nextLabel, save) { index = next }
            }
        }
    }
}

@Composable
internal fun StudioLayout(
    vm: KometViewModel, prompt: Txt, board: @Composable () -> Unit, controls: @Composable () -> Unit,
) {
    LaunchedEffect(prompt) { if (vm.state.settings.autoRead) vm.say(prompt) }
    BoxWithConstraints(Modifier.widthIn(max = 960.dp).fillMaxSize().padding(horizontal = 16.dp)) {
        val wide = maxWidth >= 620.dp
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp, if (wide) Alignment.CenterVertically else Alignment.Top), horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GameText(prompt.str(), Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                if (vm.speechAvailable) RoundIconButton(KometIcons.Speaker, Txt("Høyr oppdraget", "Hør oppdraget").str(), { vm.say(prompt) }, size = 64.dp)
            }
            if (wide) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { board() }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) { controls() }
                }
            } else {
                board()
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) { controls() }
            }
        }
    }
}

@Composable
internal fun StudioNote(text: Txt) {
    Text(text.str(), color = K.Text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
}

@Composable
internal fun StudioKey(symbol: String, label: Txt, enabled: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val description = label.str()
    PressSurface(
        onClick, modifier.heightIn(min = 64.dp).semantics { contentDescription = description },
        face = K.SurfaceHigh, edge = K.SurfaceLow, enabled = enabled, contentPadding = PaddingValues(4.dp),
    ) { GameText(symbol, style = MaterialTheme.typography.headlineMedium) }
}
