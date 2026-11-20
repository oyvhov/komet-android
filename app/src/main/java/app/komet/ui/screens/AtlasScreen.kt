package app.komet.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import app.komet.R
import app.komet.domain.*
import app.komet.ui.*
import app.komet.ui.components.*
import app.komet.ui.theme.K
import app.komet.ui.theme.LocalVisualBox

private val atlasMint = Color(0xFFA9DBCB)
private val atlasRule = Color(0xFF406065)
private val atlasGold = Color(0xFFEBC583)
private val atlasPanel = Color(0xFF183C44)

@Composable
private fun AtlasSurface(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF193C42), Color(0xFF102A32), Color(0xFF101F2A))))) {
        Image(painterResource(R.drawable.atlas_land), null, Modifier.align(Alignment.TopEnd).fillMaxWidth(.68f).aspectRatio(2f),
            colorFilter = ColorFilter.tint(Color.White.copy(alpha = .045f)))
        Box(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.TopCenter) { content() }
    }
}

@Composable
private fun AtlasHeading(title: String, detail: String, onQuit: () -> Unit, onHelp: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.weight(1f)) {
            Text(detail, style = MaterialTheme.typography.labelLarge, color = atlasMint)
            Text(title, color = K.Paper, style = MaterialTheme.typography.headlineMedium)
        }
        if (onHelp != null) RoundIconButton(KometIcons.Bulb, S.hint.str(), onHelp, size = 64.dp, face = atlasPanel, edge = K.SurfaceLow)
        CloseButton(onQuit, size = 64.dp)
    }
    HorizontalDivider(color = atlasRule)
}

@Composable
internal fun AtlasIndex(vm: KometViewModel) {
    val profile = vm.profile ?: return
    var grade by rememberSaveable(profile.id) { mutableIntStateOf(profile.grade.coerceIn(0, 3)) }
    var choosingGrade by rememberSaveable { mutableStateOf(false) }
    AtlasSurface {
        BoxWithConstraints(Modifier.widthIn(max = 1120.dp).fillMaxSize().padding(horizontal = 18.dp)) {
            val wide = maxWidth >= 680.dp
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                AtlasHeading(Txt("Geografi").str(), Txt("Jordatlas").str(), { vm.back() })
                val grades: @Composable () -> Unit = {
                    val columns = if (wide || LocalDensity.current.fontScale >= 1.5f) 2 else 4
                    Column {
                        Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).clickable(role = Role.Button) { choosingGrade = !choosingGrade }
                            .padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(S.gradeShort[grade].str(), Modifier.weight(1f), color = atlasGold, style = MaterialTheme.typography.titleMedium)
                            Text((if (choosingGrade) Txt("Lukk") else Txt("Byt trinn", "Bytt trinn")).str(), color = atlasMint)
                        }
                        HorizontalDivider(color = atlasRule)
                        if (choosingGrade) (0..3).chunked(columns).forEach { group ->
                            Row {
                                group.forEach { index ->
                                    val active = grade == index
                                    Box(Modifier.weight(1f).heightIn(min = 64.dp).background(if (active) atlasPanel else Color.Transparent)
                                        .clickable(role = Role.Tab) { grade = index; choosingGrade = false }.semantics { selected = active }.padding(4.dp),
                                        contentAlignment = Alignment.Center) {
                                        Text(S.gradeShort[index].str(), fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                            color = if (active) atlasGold else K.Text)
                                    }
                                }
                            }
                        }
                    }
                }
                val levels: @Composable () -> Unit = {
                    Column {
                        GeographyCurriculum.chapter.skills.filter { it.grade == grade }.forEach { skill ->
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f).heightIn(min = 76.dp).clickable(role = Role.Button) { vm.startSkill(skill) }
                                    .padding(vertical = 12.dp), verticalArrangement = Arrangement.Center) {
                                    Text(skill.title.str(), color = K.Paper, style = MaterialTheme.typography.titleLarge)
                                    Text(skill.detail.str(), color = atlasMint, style = MaterialTheme.typography.bodyMedium)
                                    val stars = profile.stars(skill.id)
                                    if (stars > 0) StarRow(stars)
                                }
                                val favorite = skill.id in profile.favorites
                                Box(Modifier.size(64.dp).clickable(role = Role.Button) { vm.toggleFavorite(skill.id) }, contentAlignment = Alignment.Center) {
                                    Icon(if (favorite) KometIcons.Heart else KometIcons.HeartOutline,
                                        (if (favorite) S.removeFavorite else S.addFavorite).str(), tint = if (favorite) atlasGold else atlasMint)
                                }
                            }
                            HorizontalDivider(color = atlasRule)
                        }
                    }
                }
                if (wide) Row(Modifier.fillMaxWidth().padding(top = 30.dp), horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                    Column(Modifier.weight(.85f)) {
                        Image(painterResource(R.drawable.atlas_land), null, Modifier.fillMaxWidth().aspectRatio(2f), colorFilter = ColorFilter.tint(atlasMint))
                        grades()
                    }
                    Box(Modifier.weight(1f)) { levels() }
                } else {
                    grades()
                    levels()
                }
            }
        }
    }
}

@Composable
internal fun AtlasRound(vm: KometViewModel, round: RoundState, quit: () -> Unit, help: () -> Unit) {
    val question = round.question
    val answer = question.answer as Answer.Choice
    val answered = round.phase != Phase.ANSWERING
    val dock = (question.visual as? Visual.SeaChart)?.harbours?.isNotEmpty() == true
    AtlasSurface {
        BoxWithConstraints(Modifier.widthIn(max = 1220.dp).fillMaxSize().padding(horizontal = 16.dp)) {
            val wide = maxWidth >= 700.dp && maxHeight >= 400.dp
            val spreadHeight = (maxHeight - 166.dp).coerceAtLeast(0.dp)
            val scene: @Composable () -> Unit = {
                BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    val height = when (question.visual) {
                        is Visual.SeaChart -> minOf(maxWidth / 1.16f, if (wide) 380.dp else 280.dp)
                        is Visual.Flag -> minOf(maxWidth * .60f, if (wide) 300.dp else 184.dp)
                        else -> maxWidth / 2
                    }
                    CompositionLocalProvider(LocalVisualBox provides DpSize(maxWidth, height)) {
                        when (val v = question.visual) {
                            is Visual.Flag -> AtlasFlag(v.country, Modifier.fillMaxWidth(.86f).height(height))
                            is Visual.SeaChart -> SeaChart(v, docked = dock && answered, disabled = round.disabled, onDock = vm::choose)
                            is Visual.WorldPin -> AtlasMap(v.place)
                            else -> Unit
                        }
                    }
                }
            }
            val controls: @Composable () -> Unit = {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val heading = when {
                        !answered -> question.prompt.str()
                        dock -> {
                            val berth = (question.visual as Visual.SeaChart).tower.address
                            if (round.phase == Phase.CORRECT) Txt("Framme ved kai $berth", "Fremme ved kai $berth").str() else Txt("Rett kai er $berth", "Riktig kai er $berth").str()
                        }
                        else -> answerText(answer).orEmpty()
                    }
                    Text(heading, color = K.Paper, style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Serif, fontWeight = FontWeight.Medium))
                    if (answered) {
                        Text(requireNotNull(question.explanation).str(), color = atlasMint, style = MaterialTheme.typography.titleMedium)
                        BigButton(S.onward.str(), vm::next, face = atlasPanel, edge = K.SurfaceLow)
                    } else {
                        if (round.misses > 0) Text(S.tryAgain.str(), color = atlasGold)
                        if (answer.options.first() is Option.Flag) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                answer.options.forEachIndexed { i, opt ->
                                    val enabled = i !in round.disabled
                                    Box(Modifier.weight(1f).heightIn(min = 84.dp).clip(RoundedCornerShape(6.dp))
                                        .background(if (enabled) atlasPanel else Color.Transparent)
                                        .clickable(enabled = enabled, role = Role.Button) { vm.choose(i) }, contentAlignment = Alignment.Center) {
                                        if (enabled) AtlasFlag((opt as Option.Flag).country, Modifier.fillMaxWidth(.9f).height(58.dp))
                                        else Text("—", color = atlasMint)
                                    }
                                }
                            }
                        } else if (!dock) answer.options.forEachIndexed { i, opt ->
                            val enabled = i !in round.disabled
                            Column {
                                Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).clickable(enabled = enabled, role = Role.Button) { vm.choose(i) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Canvas(Modifier.size(12.dp)) {
                                        drawCircle(if (enabled) atlasGold else atlasRule, style = androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx()))
                                    }
                                    Text((opt as Option.Label).text.str(), color = if (enabled) K.Paper else atlasMint.copy(alpha = .6f),
                                        style = MaterialTheme.typography.titleLarge)
                                }
                                HorizontalDivider(color = atlasRule)
                            }
                        }
                    }
                }
            }
            key(round.index) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 16.dp)) {
                    AtlasHeading(round.source.title.str(), "${Txt("Geografi").str()}  ·  ${round.index + 1} / ${round.questions.size}", quit, help)
                    Spacer(Modifier.height(if (wide) 34.dp else 16.dp))
                    if (wide) {
                        Row(Modifier.fillMaxWidth().heightIn(min = spreadHeight), horizontalArrangement = Arrangement.spacedBy(36.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.weight(1.5f)) { scene() }
                            Box(Modifier.weight(1f)) { controls() }
                        }
                    } else {
                        scene()
                        Spacer(Modifier.height(18.dp))
                        controls()
                    }
                }
            }
        }
    }
}
