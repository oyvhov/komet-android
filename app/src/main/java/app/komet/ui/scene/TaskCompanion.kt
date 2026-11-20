package app.komet.ui.scene

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.komet.audio.Speaker
import app.komet.domain.ShopSlot
import app.komet.ui.CompanionMessage
import app.komet.ui.CompanionReaction
import app.komet.ui.KometViewModel
import app.komet.ui.Phase
import app.komet.ui.RoundState
import app.komet.ui.S
import app.komet.ui.components.BigButton
import app.komet.ui.components.GameText
import app.komet.ui.components.KometDialog
import app.komet.ui.components.KometIcons
import app.komet.ui.components.PressSurface
import app.komet.ui.components.str
import app.komet.ui.theme.K
import app.komet.ui.theme.ReadingFont

private val CompanionMessage.mood: BoltMood
    get() = when (reaction) {
        CompanionReaction.READY -> BoltMood.IDLE
        CompanionReaction.CHEER -> BoltMood.HAPPY
        CompanionReaction.RETRY, CompanionReaction.HINT, CompanionReaction.EXPLAIN -> BoltMood.HELPING
    }

/** A single, generous tap target, shared by the phone toolbar and the tablet's character scene. */
@Composable
fun TaskCompanionButton(
    message: CompanionMessage,
    paint: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    enabled: Boolean = true,
) {
    val time = rememberSceneTime()
    val description = message.title.str()
    PressSurface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(size).semantics { stateDescription = description },
        shape = CircleShape,
        face = K.SurfaceHigh,
        edge = K.SurfaceLow,
        depth = 3.dp,
        contentPadding = PaddingValues(5.dp),
        description = S.boltHelp.str(),
    ) {
        Bolt(time, Modifier.size(size - 10.dp), mood = { message.mood }, paint = paint)
    }
}

/** Opens only when the child asks. The task stays put until this conversation is closed. */
@Composable
fun TaskCompanionDialog(
    vm: KometViewModel,
    round: RoundState,
    message: CompanionMessage,
    onClose: () -> Unit,
) {
    val time = rememberSceneTime()
    LaunchedEffect(round, round.index) { vm.say(message.speech) }
    DisposableEffect(vm) { onDispose { vm.speaker.stop() } }
    KometDialog(onClose = onClose, closeButtonSize = 64.dp) {
        Bolt(time, Modifier.size(104.dp), mood = { message.mood }, paint = vm.profile?.equipped?.get(ShopSlot.BOLT))
        GameText(
            message.title.str(),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        )
        Text(
            Speaker.plain(message.detail.str()),
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = ReadingFont),
            color = K.Text,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        if (vm.speechAvailable) {
            BigButton(S.readAloud.str(), onClick = { vm.say(message.speech) }, icon = KometIcons.Speaker, modifier = Modifier.fillMaxWidth())
        }
        if (round.phase == Phase.ANSWERING && round.question.hint != null && !round.hintShown) {
            BigButton(S.boltShowHint.str(), onClick = { vm.showHint(); onClose() }, icon = KometIcons.Bulb, modifier = Modifier.fillMaxWidth())
        }
        BigButton(S.boltBackToTask.str(), onClick = onClose, modifier = Modifier.fillMaxWidth())
    }
}
