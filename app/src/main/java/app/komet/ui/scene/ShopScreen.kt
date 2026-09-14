package app.komet.ui.scene

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.komet.domain.Purchase
import app.komet.domain.Shop
import app.komet.domain.ShopItem
import app.komet.domain.ShopSlot
import app.komet.ui.KometViewModel
import app.komet.ui.S
import app.komet.ui.components.BigButton
import app.komet.ui.components.CloseButton
import app.komet.ui.components.ConfettiBurst
import app.komet.ui.components.ConfirmPopup
import app.komet.ui.components.GameText
import app.komet.ui.components.KometIcons
import app.komet.ui.components.NuggetGlyph
import app.komet.ui.components.NuggetPill
import app.komet.ui.components.PressSurface
import app.komet.ui.components.RocketArt
import app.komet.ui.components.StarGlyph
import app.komet.ui.components.cappedSp
import app.komet.ui.components.str
import app.komet.ui.theme.K

/**
 * The shop: spend gold nuggets on things for the astronaut, Bolt and the rocket. Tap a thing to try it
 * on, then one big button buys it, puts it on or takes it off. Buying always asks first.
 */
@Composable
fun ShopScreen(vm: KometViewModel) {
    val profile = vm.profile ?: return
    val time = rememberSceneTime()
    var slot by rememberSaveable { mutableStateOf(ShopSlot.HELMET) }
    // The thing being looked at in this slot; null is the plain look.
    var chosenId by rememberSaveable(slot) { mutableStateOf(profile.equipped[slot]) }
    var confirm by remember { mutableStateOf<ShopItem?>(null) }
    var celebrations by remember { mutableIntStateOf(0) }
    var cheerAt by remember { mutableFloatStateOf(-10f) }
    val chosen = chosenId?.let(Shop::item)
    val preview = if (chosen != null) profile.equipped + (slot to chosen.id) else profile.equipped - slot

    fun choose(item: ShopItem?) {
        chosenId = item?.id
        vm.feedback.tap()
        if (item != null) vm.say(item.name)
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(KometIcons.Bag, contentDescription = null, tint = K.GoldTop, modifier = Modifier.size(34.dp))
                GameText(S.shop.str(), style = MaterialTheme.typography.headlineMedium, fontSize = cappedSp(28.sp), maxLines = 1, modifier = Modifier.weight(1f))
                NuggetPill(profile.nuggets, big = true)
                CloseButton(onClick = { vm.back() })
            }
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                val wide = maxWidth >= 720.dp && maxWidth > maxHeight
                val short = maxHeight < 640.dp
                val stage: @Composable (Modifier) -> Unit = { modifier ->
                    Stage(slot, preview, profile.hero, time, { if (time.value - cheerAt < 1.6f) HeroPose.CHEER else HeroPose.IDLE }, modifier)
                }
                val shelf: @Composable (Modifier) -> Unit = { modifier ->
                    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SlotTabs(slot, onSelect = { slot = it; vm.feedback.tap() })
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(132.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            item(key = "plain-$slot") {
                                ItemCard(null, slot, profile.hero, profile.equipped, time, selected = chosen == null, owned = true, equippedNow = profile.equipped[slot] == null, affordable = true) { choose(null) }
                            }
                            items(Shop.items(slot), key = { it.id }) { item ->
                                ItemCard(
                                    item = item,
                                    slot = slot,
                                    look = profile.hero,
                                    equipped = profile.equipped,
                                    time = time,
                                    selected = chosen == item,
                                    owned = item.id in profile.owned,
                                    equippedNow = profile.equipped[slot] == item.id,
                                    affordable = profile.nuggets >= item.price,
                                ) { choose(item) }
                            }
                        }
                        ActionBar(
                            vm = vm,
                            slot = slot,
                            chosen = chosen,
                            onBuy = { confirm = it },
                            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp),
                        )
                    }
                }
                if (wide) {
                    Row(Modifier.fillMaxSize()) {
                        stage(Modifier.weight(0.8f).fillMaxHeight())
                        shelf(Modifier.weight(1.2f).fillMaxHeight())
                    }
                } else {
                    Column(Modifier.fillMaxSize()) {
                        stage(Modifier.fillMaxWidth().height(if (short) 150.dp else 220.dp))
                        shelf(Modifier.weight(1f).fillMaxWidth())
                    }
                }
            }
        }
        if (celebrations > 0) ConfettiBurst(celebrations, Modifier.fillMaxSize())
    }

    confirm?.let { item ->
        val boughtText = S.bought(item.name.str())
        ConfirmPopup(
            title = S.confirmBuy(item.name.str(), item.price).str(),
            body = null,
            confirm = S.yesBuy.str(),
            onConfirm = {
                confirm = null
                if (vm.buy(item.id) is Purchase.Bought) {
                    celebrations++
                    cheerAt = time.value
                    vm.say(boughtText)
                }
            },
            dismiss = S.notNow.str(),
            onClose = { confirm = null },
            art = {
                Box(Modifier.size(130.dp), contentAlignment = Alignment.Center) {
                    ItemArt(item, slot, profile.hero, profile.equipped + (item.slot to item.id), time, Modifier.fillMaxSize())
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NuggetGlyph(Modifier.size(30.dp))
                    GameText(item.price.toString(), style = MaterialTheme.typography.headlineMedium, color = K.GoldTop)
                }
            },
        )
    }
}

/** The big preview: the astronaut, Bolt or the rocket, wearing what is being looked at. */
@Composable
private fun Stage(slot: ShopSlot, preview: Map<ShopSlot, String>, look: app.komet.domain.HeroLook, time: State<Float>, pose: () -> HeroPose, modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2, size.height * 0.55f)
            val r = size.minDimension * 0.55f
            drawCircle(Brush.radialGradient(listOf(K.Gold.copy(alpha = 0.28f), Color.Transparent), c, r), r, c)
            val stageWidth = size.minDimension * 0.7f
            drawOval(
                Brush.verticalGradient(listOf(Color(0xFF3A4BB0), Color(0xFF151C5C)), startY = size.height * 0.86f, endY = size.height * 0.97f),
                Offset(size.width / 2 - stageWidth / 2, size.height * 0.86f),
                Size(stageWidth, size.height * 0.1f),
            )
        }
        when (slot) {
            ShopSlot.BOLT -> Bolt(time, Modifier.fillMaxHeight(0.62f).padding(bottom = 16.dp), mood = { BoltMood.HAPPY }, paint = preview[ShopSlot.BOLT])
            ShopSlot.ROCKET -> {
                val paint = rocketPaint(preview[ShopSlot.ROCKET])
                BoxWithConstraints(Modifier.fillMaxHeight(0.8f)) {
                    RocketArt(Modifier.size(maxHeight * 0.55f, maxHeight), body = paint.body, accent = paint.accent, stripes = paint.stripes)
                }
            }
            else -> BoxWithConstraints(Modifier.fillMaxHeight(0.92f), contentAlignment = Alignment.BottomCenter) {
                Astronaut(look, time, Modifier.padding(bottom = maxHeight * 0.03f).size(maxHeight * 0.62f, maxHeight * 0.93f), pose = pose, gear = Gear.of(preview))
            }
        }
    }
}

@Composable
private fun SlotTabs(selected: ShopSlot, onSelect: (ShopSlot) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ShopSlot.entries.forEach { slot ->
            val active = slot == selected
            val label = slot.title.str()
            PressSurface(
                onClick = { onSelect(slot) },
                modifier = Modifier
                    .heightIn(min = 64.dp)
                    .semantics {
                        this.selected = active
                        contentDescription = label
                    },
                face = if (active) K.Gold else K.SurfaceHigh,
                edge = if (active) K.GoldDeep else K.SurfaceLow,
                top = if (active) K.GoldTop else lerp(K.SurfaceHigh, Color.White, 0.15f),
                shape = RoundedCornerShape(20.dp),
                depth = 4.dp,
                tapSound = false,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SlotIcon(slot, Modifier.size(30.dp))
                    GameText(label, style = MaterialTheme.typography.titleMedium, fontSize = cappedSp(17.sp), maxLines = 1)
                }
            }
        }
    }
}

/** A small drawing for each shelf, so the tabs can be found without reading. */
@Composable
private fun SlotIcon(slot: ShopSlot, modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val c = center
        val line = Stroke(w * 0.07f)
        when (slot) {
            ShopSlot.HELMET -> {
                drawCircle(Color.White, w * 0.42f, c)
                drawCircle(K.Outline, w * 0.42f, c, style = line)
                drawOval(Color(0xFF24357E), Offset(w * 0.3f, h * 0.3f), Size(w * 0.56f, h * 0.44f))
            }
            ShopSlot.VISOR -> {
                drawOval(Brush.horizontalGradient(listOf(Color(0xFF5634B0), Color(0xFF2A62B8), Color(0xFF1E8E48), Color(0xFFB88A10))), Offset(w * 0.08f, h * 0.25f), Size(w * 0.84f, h * 0.5f))
                drawOval(K.Outline, Offset(w * 0.08f, h * 0.25f), Size(w * 0.84f, h * 0.5f), style = line)
            }
            ShopSlot.PATTERN -> drawPath(starPath(c, w * 0.46f), K.Gold)
            ShopSlot.PACK -> for (x in listOf(0.18f, 0.52f)) {
                drawRoundRect(Color(0xFFB6C0D9), Offset(w * x, h * 0.12f), Size(w * 0.3f, h * 0.62f), CornerRadius(w * 0.15f))
                drawRoundRect(K.Outline, Offset(w * x, h * 0.12f), Size(w * 0.3f, h * 0.62f), CornerRadius(w * 0.15f), style = line)
                drawOval(Color(0xFFFF8A2E), Offset(w * (x + 0.06f), h * 0.76f), Size(w * 0.18f, h * 0.22f))
            }
            ShopSlot.ANTENNA -> {
                drawLine(K.Outline, Offset(w * 0.5f, h * 0.95f), Offset(w * 0.5f, h * 0.35f), strokeWidth = w * 0.12f)
                drawCircle(K.Gold, w * 0.2f, Offset(w * 0.5f, h * 0.28f))
                drawCircle(K.Outline, w * 0.2f, Offset(w * 0.5f, h * 0.28f), style = line)
            }
            ShopSlot.BADGE -> {
                drawCircle(K.Gold, w * 0.3f, Offset(w * 0.5f, h * 0.62f))
                drawCircle(K.Outline, w * 0.3f, Offset(w * 0.5f, h * 0.62f), style = line)
                drawRect(Color(0xFF1684E6), Offset(w * 0.36f, 0f), Size(w * 0.28f, h * 0.32f))
            }
            ShopSlot.BOLT -> drawBolt(BoltMood.HAPPY, 0f)
            ShopSlot.ROCKET -> {
                drawRoundRect(Color.White, Offset(w * 0.32f, h * 0.08f), Size(w * 0.36f, h * 0.7f), CornerRadius(w * 0.18f))
                drawRoundRect(K.Outline, Offset(w * 0.32f, h * 0.08f), Size(w * 0.36f, h * 0.7f), CornerRadius(w * 0.18f), style = line)
                drawCircle(Color(0xFF3D8BFF), w * 0.09f, Offset(w * 0.5f, h * 0.36f))
                drawOval(Color(0xFFFF8A2E), Offset(w * 0.4f, h * 0.76f), Size(w * 0.2f, h * 0.22f))
            }
        }
    }
}

@Composable
private fun ItemCard(
    item: ShopItem?,
    slot: ShopSlot,
    look: app.komet.domain.HeroLook,
    equipped: Map<ShopSlot, String>,
    time: State<Float>,
    selected: Boolean,
    owned: Boolean,
    equippedNow: Boolean,
    affordable: Boolean,
    onClick: () -> Unit,
) {
    val name = (item?.name ?: S.plain).str()
    PressSurface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                this.selected = selected
                contentDescription = name
            }
            .then(if (selected) Modifier.border(4.dp, K.GoldTop, RoundedCornerShape(24.dp)) else Modifier),
        face = if (selected) lerp(K.SurfaceHigh, K.Gold, 0.25f) else K.Surface,
        edge = K.SurfaceLow,
        shape = RoundedCornerShape(24.dp),
        depth = 5.dp,
        tapSound = false,
        contentPadding = PaddingValues(10.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(Modifier.fillMaxWidth().height(112.dp), contentAlignment = Alignment.Center) {
                val worn = if (item != null) equipped + (slot to item.id) else equipped - slot
                ItemArt(item, slot, look, worn, time, Modifier.fillMaxSize())
            }
            Text(
                name,
                style = MaterialTheme.typography.titleSmall,
                fontSize = cappedSp(15.sp),
                color = K.Text,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            when {
                equippedNow -> Tag(S.inUse.str(), K.Good)
                // The plain look is free and needs no tag until it is worn.
                item == null -> Unit
                owned -> Tag(S.owned.str(), K.Surface)
                item != null -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    NuggetGlyph(Modifier.size(22.dp))
                    GameText(item.price.toString(), style = MaterialTheme.typography.titleMedium, color = if (affordable) K.GoldTop else K.Muted)
                }
            }
        }
    }
}

@Composable
private fun Tag(text: String, color: Color) {
    GameText(
        text,
        style = MaterialTheme.typography.labelLarge,
        fontSize = cappedSp(14.sp),
        maxLines = 1,
        modifier = Modifier
            .background(color, RoundedCornerShape(50))
            .border(1.5.dp, K.Outline.copy(alpha = 0.6f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 2.dp),
    )
}

/** What a shop thing looks like on its own card. */
@Composable
private fun ItemArt(item: ShopItem?, slot: ShopSlot, look: app.komet.domain.HeroLook, worn: Map<ShopSlot, String>, time: State<Float>, modifier: Modifier) {
    when (slot) {
        ShopSlot.BOLT -> Bolt(time, modifier.padding(8.dp), paint = worn[ShopSlot.BOLT])
        ShopSlot.ROCKET -> {
            val paint = rocketPaint(worn[ShopSlot.ROCKET])
            Box(modifier, contentAlignment = Alignment.Center) {
                RocketArt(Modifier.size(56.dp, 96.dp), body = paint.body, accent = paint.accent, stripes = paint.stripes)
            }
        }
        // Things for the head show best in close-up; the rest need the whole figure.
        ShopSlot.HELMET, ShopSlot.VISOR, ShopSlot.ANTENNA -> Box(modifier, contentAlignment = Alignment.Center) {
            HeroBadge(look, time, 96.dp, Gear.of(worn))
        }
        else -> Box(modifier, contentAlignment = Alignment.BottomCenter) {
            Astronaut(look, time, Modifier.size(72.dp, 108.dp), gear = Gear.of(worn))
        }
    }
}

/** One big button for what can be done with the chosen thing, or why it cannot be bought yet. */
@Composable
private fun ActionBar(vm: KometViewModel, slot: ShopSlot, chosen: ShopItem?, onBuy: (ShopItem) -> Unit, modifier: Modifier) {
    val profile = vm.profile ?: return
    val wearing = profile.equipped[slot]
    Box(modifier.fillMaxWidth().widthIn(max = 640.dp)) {
        when {
            chosen == null -> if (wearing != null) {
                BigButton(S.takeOff.str(), onClick = { vm.unequip(slot) }, face = K.SurfaceHigh, edge = K.SurfaceLow, textColor = K.Text, modifier = Modifier.fillMaxWidth())
            } else {
                InUse()
            }
            wearing == chosen.id -> BigButton(S.takeOff.str(), onClick = { vm.unequip(slot) }, face = K.SurfaceHigh, edge = K.SurfaceLow, textColor = K.Text, modifier = Modifier.fillMaxWidth())
            chosen.id in profile.owned -> BigButton(S.putOn.str(), onClick = { vm.equip(chosen.id) }, face = K.Good, edge = K.GoodDeep, top = K.GoodTop, icon = KometIcons.Check, modifier = Modifier.fillMaxWidth())
            profile.nuggets >= chosen.price -> BigButton(S.buyFor(chosen.price).str(), onClick = { onBuy(chosen) }, icon = KometIcons.Bag, modifier = Modifier.fillMaxWidth())
            else -> Column(
                Modifier
                    .fillMaxWidth()
                    .background(K.SpaceTop.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
                    .border(1.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(22.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                GameText(S.notEnough.str(), style = MaterialTheme.typography.titleLarge, fontSize = cappedSp(20.sp), textAlign = TextAlign.Center)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    NuggetGlyph(Modifier.size(22.dp))
                    Text(S.missing(chosen.price - profile.nuggets).str(), style = MaterialTheme.typography.titleSmall, fontSize = cappedSp(15.sp), color = K.GoldTop, fontWeight = FontWeight.Bold)
                }
                Text(S.earnMore.str(), style = MaterialTheme.typography.bodyMedium, fontSize = cappedSp(15.sp), color = K.Muted, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun InUse() {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 66.dp)
            .background(K.SpaceTop.copy(alpha = 0.6f), RoundedCornerShape(22.dp)),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StarGlyph(true, Modifier.size(24.dp))
        GameText(" " + S.inUse.str(), style = MaterialTheme.typography.titleLarge)
    }
}
