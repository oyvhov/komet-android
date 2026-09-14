package app.komet.ui.scene

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.komet.domain.Progression
import app.komet.domain.ShopSlot
import app.komet.ui.KometViewModel
import app.komet.ui.S
import app.komet.ui.Screen
import app.komet.ui.components.BigButton
import app.komet.ui.components.GameText
import app.komet.ui.components.KometDialog
import app.komet.ui.components.KometIcons
import app.komet.ui.components.NuggetGlyph
import app.komet.ui.components.Pill
import app.komet.ui.components.PressSurface
import app.komet.ui.components.ProgressTrack
import app.komet.ui.components.StarGlyph
import app.komet.ui.components.cappedSp
import app.komet.ui.components.fixedSp
import app.komet.ui.components.str
import app.komet.ui.theme.K

/**
 * The window behind the profile picture: the astronaut on a little stage, how far the child has come,
 * and three big doors – change the astronaut, the shop, and another player.
 */
@Composable
fun ProfileWindow(vm: KometViewModel, time: State<Float>, onClose: () -> Unit) {
    val profile = vm.profile ?: return
    var choosing by remember { mutableStateOf(false) }
    KometDialog(onClose = onClose, maxWidth = 560.dp) {
        if (choosing) {
            GameText(S.whoPlays.str(), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(end = 24.dp))
            vm.state.profiles.forEach { other ->
                PressSurface(
                    onClick = {
                        vm.switchProfile(other.id)
                        onClose()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    face = if (other.id == profile.id) K.SurfaceHigh else K.Surface,
                    edge = K.SurfaceLow,
                    contentAlignment = Alignment.CenterStart,
                    contentPadding = PaddingValues(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        HeroBadge(other.hero, time, 56.dp, Gear.of(other.equipped))
                        GameText(other.name, style = MaterialTheme.typography.titleLarge, maxLines = 1, modifier = Modifier.weight(1f))
                        Pill(other.totalStars.toString(), star = true)
                    }
                }
            }
            BigButton(S.back.str(), onClick = { choosing = false }, face = K.SurfaceHigh, edge = K.SurfaceLow, textColor = K.Text, icon = KometIcons.Back, modifier = Modifier.fillMaxWidth())
            return@KometDialog
        }

        // The astronaut on a glowing stage, with Bolt
        Box(Modifier.fillMaxWidth().height(230.dp), contentAlignment = Alignment.BottomCenter) {
            Canvas(Modifier.fillMaxWidth().height(230.dp)) {
                val c = Offset(size.width / 2, size.height * 0.6f)
                drawCircle(Brush.radialGradient(listOf(K.Cosmos.copy(alpha = 0.5f), Color.Transparent), c, size.height * 0.62f), size.height * 0.62f, c)
                // The stage sits under the astronaut, who stands left of centre to make room for Bolt.
                val stageCenter = size.width / 2 - 33.dp.toPx()
                val stageWidth = 150.dp.toPx()
                drawOval(Brush.verticalGradient(listOf(Color(0xFF3A4BB0), Color(0xFF151C5C)), startY = size.height - 30.dp.toPx(), endY = size.height), Offset(stageCenter - stageWidth / 2, size.height - 30.dp.toPx()), Size(stageWidth, 26.dp.toPx()))
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Astronaut(
                    look = profile.hero,
                    time = time,
                    pose = { HeroPose.WAVE },
                    gear = Gear.of(profile.equipped),
                    modifier = Modifier.padding(bottom = 10.dp).size(136.dp, 204.dp),
                )
                Bolt(time, Modifier.padding(bottom = 120.dp).size(66.dp), mood = { BoltMood.HAPPY }, paint = profile.equipped[ShopSlot.BOLT])
            }
        }

        GameText(profile.name, style = MaterialTheme.typography.displaySmall, fontSize = cappedSp(34.sp), textAlign = TextAlign.Center, maxLines = 1, autoFit = true, modifier = Modifier.fillMaxWidth())

        // Rank and how far to the next one
        val rank = Progression.rank(profile.totalStars)
        val next = Progression.nextRank(profile.totalStars)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                Modifier
                    .size(38.dp)
                    .border(2.dp, K.Outline, CircleShape)
                    .background(Brush.verticalGradient(listOf(K.GoldTop, K.Gold, K.GoldDeep)), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                GameText((Progression.ranks.indexOf(rank) + 1).toString(), style = MaterialTheme.typography.titleMedium, fontSize = fixedSp(18.dp))
            }
            GameText(rank.title.str(), style = MaterialTheme.typography.headlineSmall, color = K.GoldTop, fontSize = cappedSp(24.sp))
        }
        if (next != null) {
            val span = (next.minStars - rank.minStars).coerceAtLeast(1)
            ProgressTrack((profile.totalStars - rank.minStars) / span.toFloat(), Modifier.fillMaxWidth(), color = K.Gold, track = K.SurfaceLow, height = 16.dp)
            Text(
                S.starsToRank(next.minStars - profile.totalStars, next.title.str()).str(),
                style = MaterialTheme.typography.titleSmall,
                fontSize = cappedSp(15.sp),
                color = K.Muted,
                fontWeight = FontWeight.Bold,
            )
        } else {
            Text(S.topRank.str(), style = MaterialTheme.typography.titleSmall, color = K.GoldTop, fontWeight = FontWeight.Bold)
        }

        // What the child has gathered
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(profile.totalStars.toString(), S.totalStars.str()) { StarGlyph(true, Modifier.size(34.dp)) }
            StatTile(profile.nuggets.toString(), S.nuggets.str()) { NuggetGlyph(Modifier.size(34.dp)) }
            StatTile(profile.currentStreak(vm.today).toString(), S.daysInRow.str()) {
                Icon(KometIcons.Flame, contentDescription = null, tint = Color(0xFFFF8A3D), modifier = Modifier.size(32.dp))
            }
        }

        BigButton(
            S.editHero.str(),
            onClick = {
                onClose()
                vm.open(Screen.HeroEditor)
            },
            face = K.Cosmos,
            edge = K.CosmosDeep,
            top = K.CosmosTop,
            icon = KometIcons.Shirt,
            modifier = Modifier.fillMaxWidth(),
        )
        BigButton(
            S.shop.str(),
            onClick = {
                onClose()
                vm.open(Screen.Shop)
            },
            icon = KometIcons.Bag,
            modifier = Modifier.fillMaxWidth(),
        )
        if (vm.state.profiles.size > 1) {
            BigButton(
                S.switchPlayer.str(),
                onClick = { choosing = true },
                face = K.SurfaceHigh,
                edge = K.SurfaceLow,
                textColor = K.Text,
                icon = KometIcons.Swap,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun RowScope.StatTile(value: String, label: String, icon: @Composable () -> Unit) {
    Column(
        Modifier
            .weight(1f)
            .background(K.SpaceTop.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
            .border(1.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        icon()
        GameText(value, style = MaterialTheme.typography.headlineSmall, fontSize = cappedSp(24.sp), maxLines = 1)
        Text(label, style = MaterialTheme.typography.labelMedium, fontSize = cappedSp(13.sp), color = K.Muted, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
    }
}
