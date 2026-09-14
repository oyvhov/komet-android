package app.komet.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.komet.BuildConfig
import app.komet.audio.Speaker
import app.komet.domain.Curriculum
import app.komet.domain.LetterCase
import app.komet.domain.Maalform
import app.komet.domain.Profile
import app.komet.domain.Progression
import app.komet.domain.Subject
import app.komet.ui.KometViewModel
import app.komet.ui.S
import app.komet.ui.Screen
import app.komet.ui.components.AnswerDisplay
import app.komet.ui.components.Avatar
import app.komet.ui.components.BigButton
import app.komet.ui.components.KometIcons
import app.komet.ui.components.Keypad
import app.komet.ui.components.PageColumn
import app.komet.ui.components.Panel
import app.komet.ui.components.Pill
import app.komet.ui.components.ProgressTrack
import app.komet.ui.components.ScreenTopBar
import app.komet.ui.components.SectionTitle
import app.komet.ui.components.SegmentedChoice
import app.komet.ui.components.avatarCount
import app.komet.ui.components.str
import app.komet.ui.components.subjectColors
import app.komet.ui.theme.K
import app.komet.update.UpdateMessage
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.random.Random

// ── Foreldrelås ────────────────────────────────────────────────────────────────────────────────

@Composable
fun ParentGateScreen(vm: KometViewModel) {
    var a by rememberSaveable { mutableIntStateOf(Random.nextInt(6, 10)) }
    var b by rememberSaveable { mutableIntStateOf(Random.nextInt(6, 10)) }
    var input by rememberSaveable { mutableStateOf("") }
    var misses by remember { mutableIntStateOf(0) }

    PageColumn(maxWidth = 520.dp) {
        ScreenTopBar(S.adultsOnly.str(), onBack = { vm.back() })
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .size(84.dp)
                    .background(K.SurfaceHigh, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(KometIcons.Lock, contentDescription = null, tint = K.Gold, modifier = Modifier.size(40.dp))
            }
        }
        Text(S.gateQuestion(a, b).str(), style = MaterialTheme.typography.displaySmall, color = K.Text, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        AnswerDisplay(input, answered = false, modifier = Modifier.align(Alignment.CenterHorizontally))
        Keypad(
            onDigit = { if (input.length < 3) input += it },
            onErase = { input = input.dropLast(1) },
            onSubmit = {
                if (input.toIntOrNull() == a * b) {
                    vm.back()
                    vm.open(Screen.Parent)
                } else {
                    misses++
                    input = ""
                    a = Random.nextInt(6, 10)
                    b = Random.nextInt(6, 10)
                }
            },
            canSubmit = input.isNotEmpty(),
            enabled = true,
            shakeCount = misses,
        )
    }
}

// ── Foreldresida ───────────────────────────────────────────────────────────────────────────────

@Composable
fun ParentScreen(vm: KometViewModel) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    PageColumn {
        ScreenTopBar(S.parents.str(), onBack = { vm.back() })
        val waiting = vm.updater.state.release
        if (waiting != null && tab != 1) {
            Panel(Modifier.fillMaxWidth(), color = K.Gold.copy(alpha = 0.16f)) {
                Text(S.updateReady(waiting.tag.removePrefix("v")).str(), style = MaterialTheme.typography.titleLarge, color = K.Text)
                BigButton(S.updateView.str(), onClick = { tab = 1 }, icon = KometIcons.Refresh, modifier = Modifier.fillMaxWidth())
            }
        }
        SegmentedChoice(
            options = listOf(S.progress.str(), S.settings.str(), S.profiles.str()),
            selected = tab,
            onSelect = { tab = it },
        )
        when (tab) {
            0 -> ProgressTab(vm)
            1 -> SettingsTab(vm)
            else -> ProfilesTab(vm)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ProgressTab(vm: KometViewModel) {
    val profile = vm.profile ?: return
    val today = vm.today
    val week = (6 downTo 0).map { today - it }
    val weekStats = week.map { profile.today(it) }
    val rounds = weekStats.sumOf { it.rounds }
    val answered = weekStats.sumOf { it.answered }
    val firstTry = weekStats.sumOf { it.firstTry }
    val minutes = weekStats.sumOf { it.seconds } / 60

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Avatar(profile.avatar, size = 48.dp)
        Column(Modifier.weight(1f)) {
            Text(profile.name, style = MaterialTheme.typography.headlineSmall, color = K.Text)
            Text(Progression.rank(profile.totalStars).title.str(), style = MaterialTheme.typography.bodyMedium, color = K.Gold)
        }
        Pill(profile.totalStars.toString(), star = true)
    }

    Text(S.last7.str(), style = MaterialTheme.typography.titleLarge, color = K.Text)
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatTile(S.rounds.str(), rounds.toString(), Modifier.weight(1f))
        StatTile(S.firstTryRate.str(), if (answered > 0) "${firstTry * 100 / answered} %" else "–", Modifier.weight(1f))
        StatTile(S.time.str(), S.minutes(minutes).str(), Modifier.weight(1f))
    }
    Panel(Modifier.fillMaxWidth()) {
        val maxSeconds = (weekStats.maxOfOrNull { it.seconds } ?: 0).coerceAtLeast(60)
        Row(Modifier.fillMaxWidth().height(120.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
            week.forEachIndexed { index, day ->
                val stats = weekStats[index]
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(Modifier.fillMaxWidth().height(90.dp)) {
                        val fraction = stats.seconds / maxSeconds.toFloat()
                        val barHeight = (size.height * fraction).coerceAtLeast(if (stats.seconds > 0) 6f else 3f)
                        drawRoundRect(
                            color = if (day == today) K.Gold else K.Reading,
                            topLeft = Offset(size.width * 0.2f, size.height - barHeight),
                            size = Size(size.width * 0.6f, barHeight),
                            cornerRadius = CornerRadius(6.dp.toPx()),
                        )
                    }
                    Text(
                        LocalDate.ofEpochDay(day).dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("nb-NO")).take(2),
                        style = MaterialTheme.typography.labelSmall,
                        color = K.Muted,
                    )
                }
            }
        }
    }

    Subject.entries.forEach { subject ->
        val (color, _) = subjectColors(subject)
        SectionTitle(S.subject(subject).str())
        Panel(Modifier.fillMaxWidth()) {
            Curriculum.chapters(subject).forEach { chapter ->
                val done = Progression.completed(profile, chapter.skills)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row {
                        Text(chapter.title.str(), style = MaterialTheme.typography.titleMedium, color = K.Text, modifier = Modifier.weight(1f))
                        Text(S.completedLevels(done, chapter.skills.size).str(), style = MaterialTheme.typography.bodySmall, color = K.Muted)
                    }
                    ProgressTrack(done / chapter.skills.size.toFloat(), Modifier.fillMaxWidth(), color = color, height = 8.dp)
                }
            }
        }
    }

    val measured = profile.skills.filter { it.value.answered >= 8 }.mapNotNull { (id, stats) ->
        Curriculum.skill(id)?.let { it to stats.firstTry / stats.answered.toFloat() }
    }
    SectionTitle(S.practiceMore.str())
    SkillList(measured.sortedBy { it.second }.take(3).filter { it.second < 0.85f })
    SectionTitle(S.strongest.str())
    SkillList(measured.sortedByDescending { it.second }.take(3))
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Panel(modifier, padding = 14.dp) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = K.Text)
        Text(label, style = MaterialTheme.typography.bodySmall, color = K.Muted)
    }
}

@Composable
private fun SkillList(items: List<Pair<app.komet.domain.Skill, Float>>) {
    Panel(Modifier.fillMaxWidth()) {
        if (items.isEmpty()) {
            Text(S.notEnoughData.str(), style = MaterialTheme.typography.bodyMedium, color = K.Muted)
        }
        items.forEach { (skill, rate) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(skill.title.str(), style = MaterialTheme.typography.titleMedium, color = K.Text)
                    Text(Curriculum.chapterOf(skill).title.str(), style = MaterialTheme.typography.bodySmall, color = K.Muted)
                }
                Text("${(rate * 100).toInt()} %", style = MaterialTheme.typography.titleLarge, color = if (rate >= 0.8f) K.Good else K.Reveal)
            }
        }
    }
}

@Composable
private fun SettingsTab(vm: KometViewModel) {
    val settings = vm.settings
    val context = LocalContext.current
    SectionTitle(S.soundAndSpeech.str())
    Panel(Modifier.fillMaxWidth()) {
        ToggleRow(S.music.str(), settings.music) { value -> vm.updateSettings { it.copy(music = value) } }
        ToggleRow(S.soundEffects.str(), settings.sound) { value -> vm.updateSettings { it.copy(sound = value) } }
        ToggleRow(S.readAloudSetting.str(), settings.speech) { value -> vm.updateSettings { it.copy(speech = value) } }
        ToggleRow(S.autoRead.str(), settings.autoRead, enabled = settings.speech) { value -> vm.updateSettings { it.copy(autoRead = value) } }
        ToggleRow(S.slowSpeech.str(), settings.slowSpeech, enabled = settings.speech) { value -> vm.updateSettings { it.copy(slowSpeech = value) } }
        ToggleRow(S.haptics.str(), settings.haptics) { value -> vm.updateSettings { it.copy(haptics = value) } }
    }
    Panel(Modifier.fillMaxWidth()) {
        Text(S.voice.str(), style = MaterialTheme.typography.titleMedium, color = K.Text)
        val status = when (vm.speaker.status) {
            Speaker.Status.READY -> S.voiceReady.str() + (vm.speaker.voiceName?.let { " · $it" } ?: "")
            Speaker.Status.LOADING -> S.voiceLoading.str()
            Speaker.Status.MISSING_NORWEGIAN -> S.voiceMissing.str()
            Speaker.Status.UNAVAILABLE -> S.voiceUnavailable.str()
        }
        Text(status, style = MaterialTheme.typography.bodyMedium, color = if (vm.speaker.status == Speaker.Status.READY) K.Good else K.Reveal)
        if (vm.speaker.status == Speaker.Status.READY) {
            Text(
                (if (vm.speaker.englishAvailable) S.englishVoiceReady else S.englishVoiceMissing).str(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (vm.speaker.englishAvailable) K.Good else K.Reveal,
            )
        }
        // Stacked, not side by side: «Opne taleinnstillingar» is too long to share a row without breaking a word.
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            BigButton(
                S.testVoice.str(),
                onClick = { vm.say(S.testSentence) },
                face = K.Reading,
                edge = K.ReadingDeep,
                icon = KometIcons.Speaker,
                enabled = vm.speaker.status == Speaker.Status.READY,
                modifier = Modifier.fillMaxWidth(),
            )
            BigButton(
                S.openTtsSettings.str(),
                onClick = {
                    val intent = Intent("com.android.settings.TTS_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    try {
                        context.startActivity(intent)
                    } catch (_: ActivityNotFoundException) {
                        runCatching {
                            context.startActivity(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        }
                    }
                },
                face = K.SurfaceHigh,
                edge = K.SurfaceLow,
                textColor = K.Text,
                icon = KometIcons.Settings,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    SectionTitle(S.dailyGoal.str())
    val goals = listOf(2, 3, 5)
    SegmentedChoice(
        options = goals.map { S.roundsCount(it).str() },
        selected = goals.indexOf(settings.dailyGoal).coerceAtLeast(0),
        onSelect = { index -> vm.updateSettings { it.copy(dailyGoal = goals[index]) } },
    )

    val profile = vm.profile
    if (profile != null) {
        SectionTitle(profile.name)
        ProfilePreferences(vm, profile)
    }

    SectionTitle(S.updates.str())
    UpdatePanel(vm)

    SectionTitle(S.aboutTitle.str())
    Panel(Modifier.fillMaxWidth()) {
        Text(S.aboutBody.str(), style = MaterialTheme.typography.bodyLarge, color = K.Text)
        Text(S.fontCredit.str(), style = MaterialTheme.typography.bodyMedium, color = K.Muted)
        Text(S.version(BuildConfig.VERSION_NAME).str(), style = MaterialTheme.typography.bodySmall, color = K.Muted)
    }
}

@Composable
private fun ProfilePreferences(vm: KometViewModel, profile: Profile) {
    Panel(Modifier.fillMaxWidth()) {
        Text(S.maalform.str(), style = MaterialTheme.typography.titleMedium, color = K.Text)
        SegmentedChoice(
            options = listOf(S.nynorsk.str(), S.bokmaal.str()),
            selected = if (profile.maalform == Maalform.NYNORSK) 0 else 1,
            onSelect = { index -> vm.updateProfile(profile.id) { it.copy(maalform = if (index == 0) Maalform.NYNORSK else Maalform.BOKMAAL) } },
        )
        Text(S.letters.str(), style = MaterialTheme.typography.titleMedium, color = K.Text)
        SegmentedChoice(
            options = listOf("ABC", "abc"),
            selected = if (profile.letterCase == LetterCase.UPPER) 0 else 1,
            onSelect = { index -> vm.updateProfile(profile.id) { it.copy(letterCase = if (index == 0) LetterCase.UPPER else LetterCase.LOWER) } },
        )
        Text(S.level.str(), style = MaterialTheme.typography.titleMedium, color = K.Text)
        SegmentedChoice(
            options = listOf("5 år", "1.", "2.", "3."),
            selected = profile.grade,
            onSelect = { index -> vm.updateProfile(profile.id) { it.copy(grade = index) } },
        )
        ToggleRow(S.unlockAll.str(), profile.unlockAll, detail = S.unlockAllDetail.str()) { value ->
            vm.updateProfile(profile.id) { it.copy(unlockAll = value) }
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, enabled: Boolean = true, detail: String? = null, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .semantics { stateDescription = if (checked) "på" else "av" }
            .clickable(enabled = enabled, role = Role.Switch) { onChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleMedium, color = if (enabled) K.Text else K.Faint)
            if (detail != null) Text(detail, style = MaterialTheme.typography.bodySmall, color = K.Muted)
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = K.Ink,
                checkedTrackColor = K.Gold,
                uncheckedThumbColor = K.Muted,
                uncheckedTrackColor = K.SurfaceLow,
                uncheckedBorderColor = K.Line,
            ),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfilesTab(vm: KometViewModel) {
    var confirmReset by remember { mutableStateOf<Profile?>(null) }
    var confirmDelete by remember { mutableStateOf<Profile?>(null) }
    var editing by remember { mutableStateOf<Profile?>(null) }

    vm.state.profiles.forEach { profile ->
        val active = profile.id == vm.profile?.id
        Panel(Modifier.fillMaxWidth(), color = if (active) K.SurfaceHigh else K.Surface) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Avatar(profile.avatar, size = 56.dp)
                Column(Modifier.weight(1f)) {
                    Text(profile.name, style = MaterialTheme.typography.headlineSmall, color = K.Text)
                    Text(
                        S.gradeNames[profile.grade].str() + " · " + (if (profile.maalform == Maalform.NYNORSK) S.nynorsk else S.bokmaal).str(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = K.Muted,
                    )
                }
                Pill(profile.totalStars.toString(), star = true)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (active) {
                    Pill(S.active.str(), icon = KometIcons.Check, iconTint = K.Good)
                } else {
                    SmallAction(S.use.str()) { vm.switchProfile(profile.id) }
                }
                SmallAction(S.nameLabel.str()) { editing = profile }
                SmallAction(S.resetProgress.str()) { confirmReset = profile }
                SmallAction(S.deleteProfile.str(), danger = true) { confirmDelete = profile }
            }
        }
    }
    BigButton(S.addProfile.str(), onClick = { vm.open(Screen.AddProfile) }, icon = KometIcons.Plus, modifier = Modifier.fillMaxWidth())

    confirmReset?.let { profile ->
        ConfirmDialog(
            body = S.resetBody(profile.name).str(),
            confirm = S.yesReset.str(),
            onConfirm = {
                vm.resetProgress(profile.id)
                confirmReset = null
            },
            onDismiss = { confirmReset = null },
        )
    }
    confirmDelete?.let { profile ->
        ConfirmDialog(
            body = S.deleteBody(profile.name).str(),
            confirm = S.yesDelete.str(),
            onConfirm = {
                confirmDelete = null
                vm.deleteProfile(profile.id)
            },
            onDismiss = { confirmDelete = null },
        )
    }
    editing?.let { profile ->
        EditProfileDialog(
            profile = profile,
            onSave = { name, avatar ->
                vm.updateProfile(profile.id) { it.copy(name = name.trim().ifBlank { it.name }.take(20), avatar = avatar) }
                editing = null
            },
            onDismiss = { editing = null },
        )
    }
}

@Composable
private fun SmallAction(text: String, danger: Boolean = false, onClick: () -> Unit) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = if (danger) K.Bad else K.Text,
        modifier = Modifier
            .background(K.SurfaceLow, RoundedCornerShape(50))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    )
}

@Composable
private fun ConfirmDialog(body: String, confirm: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = K.SurfaceHigh,
        title = { Text(S.areYouSure.str(), color = K.Text) },
        text = { Text(body, color = K.Muted, style = MaterialTheme.typography.bodyLarge) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirm, color = K.Bad, fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(S.cancel.str(), color = K.Text) } },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditProfileDialog(profile: Profile, onSave: (String, Int) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(profile.name) }
    var avatar by remember { mutableIntStateOf(profile.avatar) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = K.SurfaceHigh,
        title = { Text(profile.name, color = K.Text) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(20) },
                    label = { Text(S.nameLabel.str()) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, autoCorrectEnabled = false),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = K.Gold,
                        unfocusedBorderColor = K.Line,
                        focusedLabelColor = K.Gold,
                        unfocusedLabelColor = K.Muted,
                        cursorColor = K.Gold,
                        focusedTextColor = K.Text,
                        unfocusedTextColor = K.Text,
                    ),
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(avatarCount) { index ->
                        Box(
                            Modifier
                                .size(52.dp)
                                .background(if (index == avatar) K.Gold else Color.Transparent, CircleShape)
                                .padding(4.dp)
                                .semantics { contentDescription = "Figur ${index + 1}" }
                                .clickable(role = Role.RadioButton) { avatar = index },
                            contentAlignment = Alignment.Center,
                        ) {
                            Avatar(index, size = 44.dp)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(name, avatar) }) { Text(S.done.str(), color = K.Gold, fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(S.cancel.str(), color = K.Text) } },
    )
}

@Composable
private fun UpdatePanel(vm: KometViewModel) {
    val updater = vm.updater
    val state = updater.state
    Panel(Modifier.fillMaxWidth()) {
        val release = state.release
        if (release != null) {
            Text(S.updateReady(release.tag.removePrefix("v")).str(), style = MaterialTheme.typography.titleLarge, color = K.Gold)
            Text(
                S.updateSize(String.format(Locale.ROOT, "%.1f", release.size / (1024f * 1024f))).str(),
                style = MaterialTheme.typography.bodyMedium,
                color = K.Muted,
            )
            if (release.notes.isNotBlank()) {
                Text(release.notes.take(1200), style = MaterialTheme.typography.bodyMedium, color = K.Text)
            }
        } else {
            Text(S.installedVersion(BuildConfig.VERSION_NAME).str(), style = MaterialTheme.typography.titleMedium, color = K.Text)
        }
        val message = when (state.message) {
            null -> null
            UpdateMessage.CURRENT -> S.updateCurrent
            UpdateMessage.NETWORK -> S.updateNetwork
            UpdateMessage.INVALID -> S.updateInvalid
            UpdateMessage.STORAGE -> S.updateStorage
            UpdateMessage.PERMISSION -> S.updatePermission
            UpdateMessage.INSTALL -> S.updateInstallError
            UpdateMessage.ACCESS -> S.updateAccess
            UpdateMessage.RATE -> S.updateRate
        }
        if (message != null) {
            Text(message.str(), style = MaterialTheme.typography.bodyMedium, color = if (state.message == UpdateMessage.CURRENT) K.Good else K.Reveal)
        }
        if (state.checking) {
            LinearProgressIndicator(Modifier.fillMaxWidth(), color = K.Gold, trackColor = K.SurfaceHigh)
        }
        if (state.downloading) {
            ProgressTrack(state.progress, Modifier.fillMaxWidth(), color = K.Gold, track = K.SurfaceHigh)
            Text(S.updateProgress((state.progress * 100).toInt()).str(), style = MaterialTheme.typography.bodyMedium, color = K.Muted)
        }
        when {
            state.downloading -> BigButton(
                S.updateCancel.str(),
                onClick = updater::cancel,
                face = K.SurfaceHigh,
                edge = K.SurfaceLow,
                textColor = K.Text,
                modifier = Modifier.fillMaxWidth(),
            )
            state.ready -> BigButton(S.updateInstall.str(), onClick = updater::install, icon = KometIcons.Check, modifier = Modifier.fillMaxWidth())
            release != null -> BigButton(
                S.updateDownload.str(),
                onClick = updater::download,
                icon = KometIcons.Refresh,
                enabled = !state.checking,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        BigButton(
            S.updateCheck.str(),
            onClick = { updater.check(manual = true) },
            face = K.SurfaceHigh,
            edge = K.SurfaceLow,
            textColor = K.Text,
            enabled = !state.checking && !state.downloading,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(S.updateHint.str(), style = MaterialTheme.typography.bodySmall, color = K.Muted)
        ToggleRow(S.updateAuto.str(), state.automatic, detail = S.updateAutoHint.str()) { updater.setAutomatic(it) }
        ToggleRow(S.updatePreviews.str(), state.previews, detail = S.updatePreviewsHint.str()) { updater.setPreviews(it) }
    }
}
