package app.komet.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.awaitCancellation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.komet.domain.LetterCase
import app.komet.domain.Maalform
import app.komet.ui.components.LocalFeedback
import app.komet.ui.components.SpaceBackground
import app.komet.ui.components.str
import app.komet.ui.screens.CollectionScreen
import app.komet.ui.screens.ExploreScreen
import app.komet.ui.screens.TopicScreen
import app.komet.ui.screens.HomeScreen
import app.komet.ui.screens.OnboardingScreen
import app.komet.ui.screens.ParentGateScreen
import app.komet.ui.screens.ParentScreen
import app.komet.ui.screens.PlayScreen
import app.komet.ui.screens.RaceMenuScreen
import app.komet.ui.screens.RaceScreen
import app.komet.ui.screens.ResultScreen
import app.komet.ui.screens.WorldScreen
import app.komet.ui.theme.K
import app.komet.ui.theme.LocalReading
import app.komet.ui.theme.ReadingPrefs

@Composable
fun KometApp(vm: KometViewModel) {
    val profile = vm.state.activeProfile
    val prefs = ReadingPrefs(profile?.maalform ?: Maalform.NYNORSK, profile?.letterCase ?: LetterCase.UPPER)
    var confirmQuit by remember { mutableStateOf(false) }

    // Look for a new version whenever the app comes to the front; the updater rate-limits itself.
    val lifecycle = LocalLifecycleOwner.current
    LaunchedEffect(lifecycle) {
        lifecycle.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            vm.updater.checkIfDue()
            awaitCancellation()
        }
    }

    CompositionLocalProvider(LocalReading provides prefs, LocalFeedback provides vm.feedback) {
        val screen = vm.stack.lastOrNull() ?: Screen.Home

        BackHandler(enabled = vm.stack.size > 1) {
            if (screen == Screen.Play) confirmQuit = true else vm.back()
        }

        SpaceBackground(
            modifier = Modifier.fillMaxSize(),
            twinkle = screen != Screen.Play && screen != Screen.Race,
        ) {
            AnimatedContent(
                targetState = screen,
                transitionSpec = {
                    (fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 0.98f)) togetherWith fadeOut(tween(140))
                },
                label = "screen",
            ) { target ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                ) {
                    when (target) {
                        Screen.Onboarding -> OnboardingScreen(vm, adding = false)
                        Screen.AddProfile -> OnboardingScreen(vm, adding = true)
                        Screen.Home -> HomeScreen(vm)
                        is Screen.World -> WorldScreen(vm, target.subject)
                        Screen.Play -> PlayScreen(vm, onQuit = { confirmQuit = true })
                        Screen.Result -> ResultScreen(vm)
                        Screen.Collection -> CollectionScreen(vm)
                        Screen.Explore -> ExploreScreen(vm)
                        is Screen.Topic -> TopicScreen(vm, target.topic)
                        Screen.RaceMenu -> RaceMenuScreen(vm)
                        Screen.Race -> RaceScreen(vm)
                        Screen.ParentGate -> ParentGateScreen(vm)
                        Screen.Parent -> ParentScreen(vm)
                    }
                }
            }
        }

        if (confirmQuit) {
            AlertDialog(
                onDismissRequest = { confirmQuit = false },
                containerColor = K.SurfaceHigh,
                title = { Text(S.quitTitle.str(), style = MaterialTheme.typography.headlineSmall, color = K.Text) },
                text = { Text(S.quitBody.str(), style = MaterialTheme.typography.bodyLarge, color = K.Muted) },
                confirmButton = {
                    TextButton(onClick = { confirmQuit = false }) {
                        Text(S.keepGoing.str(), color = K.Gold, style = MaterialTheme.typography.labelLarge)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        confirmQuit = false
                        vm.quitRound()
                    }) {
                        Text(S.quit.str(), color = K.Muted, style = MaterialTheme.typography.labelLarge)
                    }
                },
            )
        }
    }
}
