package app.komet.ui.scene

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.komet.domain.Curriculum
import app.komet.domain.Adventures
import app.komet.domain.Subject
import app.komet.ui.components.PressSurface
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import app.komet.domain.Skill
import app.komet.domain.SolarDestination
import app.komet.domain.Txt
import app.komet.ui.KometViewModel
import app.komet.ui.S
import app.komet.ui.Screen
import app.komet.ui.components.BigButton
import app.komet.ui.components.GameText
import app.komet.ui.components.KometIcons
import app.komet.ui.components.ScreenTopBar
import app.komet.ui.components.StarRow
import app.komet.ui.components.str
import app.komet.ui.screens.ClockWorkshopButton
import app.komet.ui.screens.TopicGrid
import app.komet.ui.screens.TopicTile
import app.komet.ui.theme.K

@Composable
fun SolarWorldScreen(vm: KometViewModel, destination: SolarDestination) {
    val profile = vm.profile ?: return
    val mission = requireNotNull(Curriculum.skill(destination.missionId))
    val stars = profile.stars(mission.id)
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        ScreenTopBar(
            destination.planet.name.str(), { vm.back() },
            Modifier.widthIn(max = 1160.dp).padding(horizontal = 16.dp),
        )
        BoxWithConstraints(Modifier.widthIn(max = 1160.dp).fillMaxWidth().weight(1f)) {
            val wide = maxWidth >= (if (LocalDensity.current.fontScale > 1.5f) 860.dp else 660.dp)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 3.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (wide) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.Top) {
                            PlanetPlay(vm, destination, mission, stars, Modifier.weight(1f))
                            Column(Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                PlanetTopics(vm, destination)
                            }
                        }
                    }
                } else {
                    item { PlanetPlay(vm, destination, mission, stars, Modifier.fillMaxWidth()) }
                    item { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { PlanetTopics(vm, destination) } }
                }
            }
        }
    }
}

@Composable
private fun PlanetPlay(vm: KometViewModel, destination: SolarDestination, mission: Skill, stars: Int, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PlanetMission(destination, stars, Modifier.fillMaxWidth()) { vm.startSkill(mission) }
        destination.trail?.let { subject ->
            StoryLanding(vm, subject)
            BigButton(
                Txt("Fagstien").str(), { vm.open(Screen.World(subject)) }, Modifier.fillMaxWidth(),
                face = K.SurfaceHigh, edge = K.SurfaceLow,
            )
        }
    }
}

@Composable
internal fun StoryLanding(vm: KometViewModel, subject: Subject) {
    val profile = vm.profile ?: return
    val story = Adventures.of(subject)
    val done = Adventures.completed(profile, subject)
    val next = Adventures.next(profile, subject)
    PressSurface({ vm.open(Screen.Adventure(subject)) }, Modifier.fillMaxWidth(),
        face = K.Surface, edge = K.SurfaceLow, shape = RoundedCornerShape(22.dp),
        contentPadding = PaddingValues(0.dp)) {
        Column {
            Box(Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))) {
                AdventureArtwork(subject, done, Modifier.fillMaxSize())
                AdventureResident(subject, Modifier.align(Alignment.BottomStart).padding(start = 16.dp).size(78.dp))
            }
            Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                GameText(story.title.str(), style = MaterialTheme.typography.titleLarge)
                Text((next?.title ?: story.ending).str(), color = K.Muted, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun PlanetMission(destination: SolarDestination, stars: Int, modifier: Modifier, onPlay: () -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    val light = destination.light()
    Column(
        modifier.clip(shape)
            .background(Brush.verticalGradient(listOf(lerp(K.Surface, light, 0.14f), K.SpaceTop)))
            .border(1.dp, light.copy(alpha = 0.38f), shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(96.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.matchParentSize()) {
                    drawOval(
                        light.copy(alpha = 0.25f), Offset(size.width * 0.04f, size.height * 0.31f),
                        Size(size.width * 0.92f, size.height * 0.38f), style = Stroke(1.dp.toPx()),
                    )
                }
                SolarGlobe(destination, Modifier.fillMaxSize())
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val order = destination.planet.fromSun
                Text(
                    Txt("Planet $order frå sola", "Planet $order fra solen").str(),
                    color = light, style = MaterialTheme.typography.labelMedium,
                )
                GameText(Txt("Planetoppdrag").str(), style = MaterialTheme.typography.titleLarge)
                Text((if (stars > 0) Txt("Stempel i reisepasset") else Txt("Spel for eit stempel", "Spill for et stempel")).str(),
                    color = K.Muted, style = MaterialTheme.typography.bodyMedium)
                StarRow(stars, size = 16.dp)
            }
        }
        BigButton(
            S.start.str(), onPlay, Modifier.fillMaxWidth(), top = K.GoldTop,
            icon = KometIcons.Play,
        )
    }
}

@Composable
private fun PlanetTopics(vm: KometViewModel, destination: SolarDestination) {
    Text(
        Txt("Oppgåver", "Oppgaver").str(),
        style = MaterialTheme.typography.titleLarge,
        color = K.Text,
        modifier = Modifier.semantics { heading() },
    )
    if (destination == SolarDestination.SATURN) {
        ClockWorkshopButton({ vm.open(Screen.ClockWorkshop) }, Modifier.fillMaxWidth())
    }
    app.komet.domain.DiscoveryWorkshop.entries.filter { it.planet == destination }.forEach {
        app.komet.ui.screens.DiscoveryButton(vm, it)
    }
    TopicGrid(destination.topics) { topic, modifier ->
        TopicTile(topic, { vm.open(Screen.Topic(topic)) }, modifier)
    }
}
