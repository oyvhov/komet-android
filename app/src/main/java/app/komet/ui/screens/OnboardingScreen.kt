package app.komet.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.komet.domain.LetterCase
import app.komet.domain.Maalform
import app.komet.ui.KometViewModel
import app.komet.ui.S
import app.komet.ui.components.Avatar
import app.komet.ui.components.BigButton
import app.komet.ui.components.KometIcons
import app.komet.ui.components.RocketArt
import app.komet.ui.components.RoundIconButton
import app.komet.ui.components.SelectCard
import app.komet.ui.components.avatarCount
import app.komet.ui.components.str
import app.komet.ui.theme.K
import app.komet.ui.theme.LocalReading
import app.komet.ui.theme.ReadingPrefs

private const val STEPS = 6

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(vm: KometViewModel, adding: Boolean) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var maalform by rememberSaveable { mutableStateOf(vm.profile?.maalform ?: Maalform.NYNORSK) }
    var name by rememberSaveable { mutableStateOf("") }
    var avatar by rememberSaveable { mutableIntStateOf(0) }
    var grade by rememberSaveable { mutableIntStateOf(1) }
    var letterCase by rememberSaveable { mutableStateOf(LetterCase.UPPER) }

    val canContinue = when (step) {
        1 -> name.isNotBlank()
        else -> true
    }

    fun advance() {
        if (!canContinue) return
        if (step < STEPS - 1) step++ else vm.createProfile(name, avatar, grade, maalform, letterCase)
    }

    CompositionLocalProvider(LocalReading provides ReadingPrefs(maalform, letterCase)) {
        Column(
            Modifier
                .fillMaxSize()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (step > 0 || adding) {
                    RoundIconButton(KometIcons.Back, S.back.str(), onClick = { if (step > 0) step-- else vm.back() }, size = 52.dp)
                } else {
                    Spacer(Modifier.size(52.dp))
                }
                Row(
                    Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                ) {
                    repeat(STEPS) { index ->
                        Box(
                            Modifier
                                .size(if (index == step) 26.dp else 10.dp, 10.dp)
                                .background(if (index <= step) K.Gold else K.SurfaceHigh, CircleShape),
                        )
                    }
                }
                Spacer(Modifier.size(52.dp))
            }

            AnimatedContent(
                targetState = step,
                transitionSpec = { (slideInHorizontally(tween(260)) { it / 6 } + fadeIn(tween(260))) togetherWith fadeOut(tween(120)) },
                modifier = Modifier.weight(1f),
                label = "step",
            ) { current ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Column(
                        Modifier
                            .widthIn(max = 560.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        when (current) {
                            0 -> {
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    RocketArt(Modifier.size(96.dp, 150.dp))
                                }
                                Title(S.appName.str())
                                Body(S.tagline.str())
                                Spacer(Modifier.height(4.dp))
                                Title(S.chooseMaalform.str(), small = true)
                                Body(S.chooseMaalformBody.str())
                                SelectCard(maalform == Maalform.NYNORSK, onClick = { maalform = Maalform.NYNORSK }) {
                                    Text(S.nynorsk.str(), style = MaterialTheme.typography.headlineSmall, color = K.Text)
                                    Text("«${S.nynorskSample.nn}»", style = MaterialTheme.typography.bodyLarge, color = K.Muted)
                                }
                                SelectCard(maalform == Maalform.BOKMAAL, onClick = { maalform = Maalform.BOKMAAL }) {
                                    Text(S.bokmaal.str(), style = MaterialTheme.typography.headlineSmall, color = K.Text)
                                    Text("«${S.bokmaalSample.nn}»", style = MaterialTheme.typography.bodyLarge, color = K.Muted)
                                }
                            }
                            1 -> {
                                Title(S.nameTitle.str())
                                val focus = remember { FocusRequester() }
                                LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it.take(20) },
                                    label = { Text(S.nameLabel.str()) },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.headlineSmall.copy(color = K.Text),
                                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, autoCorrectEnabled = false, imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { advance() }),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = K.Gold,
                                        unfocusedBorderColor = K.Line,
                                        focusedLabelColor = K.Gold,
                                        unfocusedLabelColor = K.Muted,
                                        cursorColor = K.Gold,
                                        focusedContainerColor = K.Surface,
                                        unfocusedContainerColor = K.Surface,
                                        focusedTextColor = K.Text,
                                        unfocusedTextColor = K.Text,
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focus),
                                )
                            }
                            2 -> {
                                Title(S.avatarTitle.str())
                                FlowRow(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
                                    verticalArrangement = Arrangement.spacedBy(14.dp),
                                ) {
                                    repeat(avatarCount) { index ->
                                        val chosen = index == avatar
                                        Box(
                                            Modifier
                                                .size(84.dp)
                                                .border(if (chosen) 4.dp else 0.dp, if (chosen) K.Gold else Color.Transparent, CircleShape)
                                                .padding(6.dp)
                                                .semantics {
                                                    selected = chosen
                                                    contentDescription = "Figur ${index + 1}"
                                                }
                                                .clickable(role = Role.RadioButton) {
                                                    avatar = index
                                                    vm.feedback.tap()
                                                },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Avatar(index, size = 72.dp)
                                        }
                                    }
                                }
                            }
                            3 -> {
                                Title(S.gradeTitle(name.trim()).str())
                                S.gradeNames.forEachIndexed { index, label ->
                                    SelectCard(grade == index, onClick = { grade = index }) {
                                        Text(label.str(), style = MaterialTheme.typography.headlineSmall, color = K.Text)
                                        Text(S.gradeDetails[index].str(), style = MaterialTheme.typography.bodyMedium, color = K.Muted)
                                    }
                                }
                            }
                            4 -> {
                                Title(S.caseTitle.str())
                                SelectCard(letterCase == LetterCase.UPPER, onClick = { letterCase = LetterCase.UPPER }) {
                                    Text("SOL · BIL · MUS", style = MaterialTheme.typography.headlineMedium, color = K.Text)
                                    Text(S.caseUpper.str() + " – " + S.caseUpperDetail.str(), style = MaterialTheme.typography.bodyMedium, color = K.Muted)
                                }
                                SelectCard(letterCase == LetterCase.LOWER, onClick = { letterCase = LetterCase.LOWER }) {
                                    Text("sol · bil · mus", style = MaterialTheme.typography.headlineMedium, color = K.Text)
                                    Text(S.caseLower.str() + " – " + S.caseLowerDetail.str(), style = MaterialTheme.typography.bodyMedium, color = K.Muted)
                                }
                            }
                            else -> {
                                Spacer(Modifier.height(12.dp))
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Avatar(avatar, size = 120.dp)
                                }
                                Title(S.readyTitle(name.trim()).str())
                                Body(S.readyBody.str())
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    RocketArt(Modifier.size(80.dp, 128.dp))
                                }
                            }
                        }
                    }
                }
            }

            BigButton(
                text = if (step == STEPS - 1) S.launch.str() else S.next.str(),
                onClick = { advance() },
                enabled = canContinue,
                icon = if (step == STEPS - 1) KometIcons.Rocket else null,
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            )
        }
    }
}

@Composable
private fun Title(text: String, small: Boolean = false) {
    app.komet.ui.components.GameText(
        text,
        style = if (small) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.displaySmall,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun Body(text: String) {
    Text(text, style = MaterialTheme.typography.bodyLarge, color = K.Muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
}
