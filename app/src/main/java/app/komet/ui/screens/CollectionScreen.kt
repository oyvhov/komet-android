package app.komet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.komet.domain.SpaceCard
import app.komet.domain.SpaceCards
import app.komet.ui.KometViewModel
import app.komet.ui.S
import app.komet.ui.components.BigButton
import app.komet.ui.components.KometDialog
import app.komet.ui.components.GameText
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import app.komet.ui.components.KometIcons
import app.komet.ui.components.MaxContentWidth
import app.komet.ui.components.Pill
import app.komet.ui.components.PressSurface
import app.komet.ui.components.ProgressTrack
import app.komet.ui.components.ScreenTopBar
import app.komet.ui.components.SpaceCardArt
import app.komet.ui.components.StarGlyph
import app.komet.ui.components.str
import app.komet.ui.components.drawRays
import app.komet.ui.scene.rememberSceneTime
import app.komet.ui.theme.K
import app.komet.ui.theme.ReadingFont
import kotlin.math.sin
import kotlin.math.cos

@Composable
fun CollectionScreen(vm: KometViewModel) {
    val profile = vm.profile ?: return
    val unlocked = SpaceCards.unlockedCount(profile.totalStars)
    val next = SpaceCards.next(profile.totalStars)
    val largeText = LocalDensity.current.fontScale >= 1.5f
    val configuration = LocalConfiguration.current
    val wide = configuration.screenWidthDp >= 900 && configuration.screenWidthDp > configuration.screenHeightDp
    var open by remember { mutableStateOf<SpaceCard?>(null) }

    LaunchedEffect(unlocked) { vm.markCardsSeen() }

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        ScreenTopBar(
            title = S.cards.str(),
            onBack = { vm.back() },
            modifier = Modifier
                .widthIn(max = if (wide) 1200.dp else MaxContentWidth)
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Pill(profile.totalStars.toString(), star = true)
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = if (largeText) 220.dp else if (wide) 180.dp else 150.dp),
            modifier = Modifier
                .widthIn(max = 1200.dp)
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(Modifier.padding(bottom = 6.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GameText(S.cardsOf(unlocked, SpaceCards.all.size).str(), style = MaterialTheme.typography.titleLarge)
                    ProgressTrack(unlocked / SpaceCards.all.size.toFloat(), Modifier.fillMaxWidth(), color = K.Cards, track = K.SurfaceHigh)
                    Text(
                        if (next != null) S.nextCardAt(next.cost).str() else S.allCards.str(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = K.Muted,
                    )
                }
            }
            items(SpaceCards.all.size) { index ->
                val card = SpaceCards.all[index]
                CardTile(card, owned = index < unlocked, rarity = rarityOf(index), onOpen = { open = card })
            }
        }
    }

    open?.let { card ->
        val rarity = rarityOf(SpaceCards.all.indexOf(card))
        KometDialog(onClose = { open = null }, maxWidth = if (wide) 1000.dp else 480.dp) {
            if (wide) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(32.dp), verticalAlignment = Alignment.CenterVertically) {
                    OpenedCardArt(card, rarity, Modifier.weight(1.15f).aspectRatio(1.1f))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        CardReading(vm, card)
                    }
                }
            } else {
                OpenedCardArt(card, rarity, Modifier.fillMaxWidth().aspectRatio(1.4f))
                CardReading(vm, card)
            }
        }
    }
}

/** Later cards are rarer: silver, blue, violet and finally gold frames. */
private fun rarityOf(index: Int): Color = when (index * 4 / SpaceCards.all.size) {
    0 -> Color(0xFFB8C6DB)
    1 -> Color(0xFF4DA3FF)
    2 -> Color(0xFFB57BFF)
    else -> K.Gold
}

@Composable
private fun OpenedCardArt(card: SpaceCard, rarity: Color, modifier: Modifier) {
    Box(
        modifier.clip(RoundedCornerShape(24.dp)).background(K.SpaceTop)
            .border(2.dp, metallicFrame(rarity), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center,
    ) { CardShowcase(card, rarity) }
}

@Composable
private fun CardReading(vm: KometViewModel, card: SpaceCard) {
    GameText(card.title.str(), style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
    Text(card.fact.str(), style = MaterialTheme.typography.headlineSmall.copy(fontFamily = ReadingFont, fontWeight = FontWeight.Normal), color = K.Text, textAlign = TextAlign.Center)
    if (vm.speechAvailable) {
        val spoken = card.title.str() + ". " + card.fact.str()
        BigButton(S.readAloud.str(), onClick = { vm.sayPlain(spoken) }, face = K.Reading, edge = K.ReadingDeep, icon = KometIcons.Speaker, modifier = Modifier.fillMaxWidth())
    }
}

private fun metallicFrame(rarity: Color): Brush = Brush.linearGradient(
    listOf(lerp(rarity, Color.White, 0.65f), rarity, lerp(rarity, K.SpaceTop, 0.55f), rarity, Color.White),
)

/** The opened card gets a quiet, floating display; its reading text stays still. */
@Composable
private fun CardShowcase(card: SpaceCard, rarity: Color) {
    val time = rememberSceneTime()
    Canvas(Modifier.fillMaxSize()) {
        val t = time.value
        val radius = size.minDimension * 0.62f
        drawCircle(Brush.radialGradient(listOf(rarity.copy(alpha = 0.28f), rarity.copy(alpha = 0.08f), Color.Transparent), center, radius), radius, center)
        drawRays(center, radius, rarity, t * 0.025f)
        for (i in 0 until 12) {
            val a = i * 2.39996f
            val distance = size.minDimension * (0.26f + (i % 4) * 0.055f)
            val point = center + Offset(cos(a) * distance * 1.45f, sin(a) * distance)
            val shine = (0.5f + 0.5f * sin(t * 1.3f + i))
            val arm = (1.3f + shine * 1.6f).dp.toPx()
            val color = lerp(rarity, Color.White, 0.65f).copy(alpha = 0.3f + shine * 0.5f)
            drawLine(color, point - Offset(arm, 0f), point + Offset(arm, 0f), 1.dp.toPx(), StrokeCap.Round)
            drawLine(color, point - Offset(0f, arm), point + Offset(0f, arm), 1.dp.toPx(), StrokeCap.Round)
        }
    }
    SpaceCardArt(
        card.art,
        Modifier.fillMaxSize().padding(20.dp).graphicsLayer {
            translationY = sin(time.value * 1.15f) * 4.dp.toPx()
            rotationZ = sin(time.value * 0.8f) * 1.4f
        },
        emojiSize = 140.dp,
    )
}

@Composable
private fun CardTile(card: SpaceCard, owned: Boolean, rarity: Color, onOpen: () -> Unit) {
    PressSurface(
        onClick = onOpen,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 190.dp),
        face = if (owned) lerp(K.Surface, rarity, 0.18f) else K.SurfaceLow,
        edge = if (owned) lerp(K.SurfaceLow, rarity, 0.45f) else K.SpaceTop,
        enabled = owned,
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(12.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.15f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.radialGradient(listOf(if (owned) lerp(K.SpaceTop, rarity, 0.22f) else K.SpaceTop, K.SpaceTop)))
                    .border(if (owned) 3.dp else 0.dp, metallicFrame(if (owned) rarity else Color.Transparent), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (owned) {
                    SpaceCardArt(card.art, Modifier.fillMaxSize().padding(10.dp), emojiSize = 84.dp)
                } else {
                    Icon(KometIcons.Lock, contentDescription = null, tint = K.Faint, modifier = Modifier.size(36.dp))
                }
            }
            if (owned) {
                GameText(card.title.str(), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    StarGlyph(true, Modifier.size(18.dp))
                    Text(card.cost.toString(), style = MaterialTheme.typography.titleMedium, color = K.Muted)
                }
            }
        }
    }
}
