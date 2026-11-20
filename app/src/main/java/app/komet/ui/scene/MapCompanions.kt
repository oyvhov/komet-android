package app.komet.ui.scene

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.komet.audio.Sfx
import app.komet.domain.Progression
import app.komet.domain.ShopSlot
import app.komet.ui.KometViewModel
import app.komet.ui.S
import app.komet.ui.components.GameText
import app.komet.ui.components.LocalFeedback
import app.komet.ui.components.Panel
import app.komet.ui.components.PressSurface
import app.komet.ui.components.cappedSp
import app.komet.ui.components.str
import app.komet.ui.components.subjectTone
import app.komet.ui.theme.K
import kotlin.math.max
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** The astronaut's face in a round frame. */
@Composable
fun HeroBadge(look: app.komet.domain.HeroLook, time: State<Float> = remember { mutableFloatStateOf(0f) }, size: androidx.compose.ui.unit.Dp, gear: Gear = Gear.None) {
    Box(
        Modifier
            .size(size)
            .border(2.dp, K.Outline, CircleShape)
            .padding(2.dp)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(Color(0xFF3A4BB0), Color(0xFF151C5C))))
            .border((size.value * 0.05f).coerceAtLeast(2f).dp, Color.White.copy(alpha = 0.8f), CircleShape),
    ) {
        HeroPortrait(look, time, Modifier.fillMaxSize(), gear = gear)
    }
}

/** Bolt suggests what to do next: the unfinished round first, otherwise the next mission. */
@Composable
internal fun MissionBubble(vm: KometViewModel, time: State<Float>, modifier: Modifier) {
    val profile = vm.profile ?: return
    val resume = remember(profile) { Progression.resumable(profile) }
    val recommended = remember(profile) { Progression.recommended(profile) }
    val feedback = LocalFeedback.current
    var talking by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Bolt(
            time = time,
            paint = profile.equipped[ShopSlot.BOLT],
            mood = { if (talking) BoltMood.TALK else BoltMood.IDLE },
            modifier = Modifier
                .size(64.dp)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                    feedback.sfx(Sfx.BEEP)
                    talking = true
                    if (resume != null) vm.say(resume.first.title) else vm.say(recommended.title)
                    scope.launch {
                        delay(1800)
                        talking = false
                    }
                },
        )
        Panel(Modifier.weight(1f), padding = 10.dp) {
            val tone = subjectTone((resume?.first ?: recommended).subject)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    (if (resume != null) S.resumeLabel else S.nextMission).str(),
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = cappedSp(13.sp),
                    color = tone.accent,
                    fontWeight = FontWeight.Black,
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(Modifier.weight(1f, fill = false)) {
                        Text(
                            (resume?.first ?: recommended).title.str(),
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = cappedSp(20.sp),
                            lineHeight = cappedSp(25.sp),
                            color = K.Text,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            if (resume != null) S.resumeDetail(resume.second.done, resume.second.total).str() else S.subject(recommended.subject).str(),
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = cappedSp(14.sp),
                            color = K.Muted,
                            maxLines = 1,
                        )
                    }
                    PressSurface(
                        onClick = { if (resume != null) vm.resumeRound() else vm.startSkill(recommended) },
                        face = K.Gold,
                        edge = K.GoldDeep,
                        top = K.GoldTop,
                        shape = RoundedCornerShape(18.dp),
                        depth = 4.dp,
                        modifier = Modifier.heightIn(min = 64.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp),
                    ) {
                        GameText((if (resume != null) S.resume else S.start).str(), style = MaterialTheme.typography.titleMedium, fontSize = cappedSp(17.sp))
                    }
                }
            }
        }
    }
}
