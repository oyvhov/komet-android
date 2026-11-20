package app.komet.ui.scene

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.komet.domain.Adventures
import app.komet.domain.Subject
import app.komet.ui.KometViewModel
import app.komet.ui.S
import app.komet.ui.Screen
import app.komet.ui.components.BigButton
import app.komet.ui.components.CloseButton
import app.komet.ui.components.GameText
import app.komet.ui.components.KometIcons
import app.komet.ui.components.StarGlyph
import app.komet.ui.components.str
import app.komet.ui.theme.K

@Composable
fun AdventureScreen(vm: KometViewModel, destination: Screen.Adventure) {
    val profile = vm.profile ?: return
    val subject = destination.subject
    val story = Adventures.of(subject)
    val completed = Adventures.completed(profile, subject)
    val quest = Adventures.next(profile, subject)
    val arrived = Adventures.quest(destination.celebrationId)?.takeIf { it.subject == subject && it.id in profile.adventures }
    val speech = quest?.request ?: story.ending

    val details: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                AdventureResident(subject, Modifier.size(76.dp))
                Column(Modifier.weight(1f)) {
                    GameText(story.resident, style = MaterialTheme.typography.headlineMedium)
                    Text(S.adventureProgress(completed, story.quests.size).str(), color = K.Muted, style = MaterialTheme.typography.titleSmall)
                }
            }
            if (arrived != null && quest != null) {
                Text(arrived.thanks.str(), style = MaterialTheme.typography.titleMedium, color = adventureColor(subject))
            }
            GameText((quest?.title ?: S.adventureDone).str(), style = MaterialTheme.typography.headlineMedium)
            Text(speech.str(), style = MaterialTheme.typography.titleMedium, color = K.Text)
            if (quest != null) {
                BigButton(
                    (if (profile.activeRound?.adventureId == quest.id) S.adventureContinue else S.adventureStart).str(),
                    onClick = { vm.startAdventure(subject) }, icon = KometIcons.Play, modifier = Modifier.fillMaxWidth(),
                )
            }
            if (vm.speechAvailable) {
                BigButton(S.readAloud.str(), onClick = { vm.say(speech) }, icon = KometIcons.Speaker,
                    face = K.SurfaceHigh, edge = K.SurfaceLow, modifier = Modifier.fillMaxWidth())
            }
            // Every chapter remains visible, including completed ones, so the change has a story.
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(K.SpaceTop.copy(alpha = .65f)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                story.quests.forEachIndexed { index, chapter ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        StarGlyph(index < completed, Modifier.size(28.dp))
                        Text(chapter.title.str(), color = if (index == completed) K.GoldTop else K.Text,
                            style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    }
                }
            }
            BigButton(S.adventureToPlanet.str(), onClick = { vm.back() }, face = K.SurfaceHigh, edge = K.SurfaceLow,
                icon = KometIcons.Rocket, modifier = Modifier.fillMaxWidth())
        }
    }

    Box(Modifier.fillMaxSize()) {
        SubjectBackdrop(subject)
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GameText(story.title.str(), style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth().padding(end = 80.dp).heightIn(min = 64.dp))
            BoxWithConstraints(Modifier.weight(1f)) {
                if (maxWidth >= 840.dp && maxWidth > maxHeight) {
                    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Box(Modifier.weight(1.25f).fillMaxHeight().clip(RoundedCornerShape(32.dp))) {
                            AdventureArtwork(subject, completed, Modifier.fillMaxSize(), reveal = arrived != null)
                            AdventureResident(subject, Modifier.align(Alignment.BottomStart).padding(22.dp).size(140.dp))
                        }
                        Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(end = 8.dp)) { details() }
                    }
                } else {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        Box(Modifier.fillMaxWidth().aspectRatio(1.77f).clip(RoundedCornerShape(26.dp))) {
                            AdventureArtwork(subject, completed, Modifier.fillMaxSize(), reveal = arrived != null)
                            AdventureResident(subject, Modifier.align(Alignment.BottomStart).padding(12.dp).size(84.dp))
                        }
                        details()
                    }
                }
            }
        }
        CloseButton(onClick = { vm.back() }, size = 64.dp,
            modifier = Modifier.align(Alignment.TopEnd).safeDrawingPadding().padding(16.dp))
    }
}

/** The default invitation on a planet; choosing any level still opens ordinary free practice. */
@Composable
fun AdventureInvitation(vm: KometViewModel, subject: Subject) {
    val profile = vm.profile ?: return
    val story = Adventures.of(subject)
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(K.SpaceTop.copy(alpha = .95f))
        .verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AdventureResident(subject, Modifier.size(64.dp))
            Column(Modifier.weight(1f)) {
                GameText(story.title.str(), style = MaterialTheme.typography.titleLarge)
                Text(S.adventureProgress(Adventures.completed(profile, subject), story.quests.size).str(), color = K.Muted,
                    style = MaterialTheme.typography.labelLarge)
            }
        }
        BigButton((if (Adventures.next(profile, subject) != null) S.adventureMeet(story.resident) else S.adventureSee).str(),
            onClick = { vm.open(Screen.Adventure(subject)) }, icon = KometIcons.Rocket, modifier = Modifier.fillMaxWidth())
    }
}
