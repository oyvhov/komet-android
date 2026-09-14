package app.komet

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import app.komet.ui.KometApp
import app.komet.ui.KometViewModel
import app.komet.ui.theme.KometTheme

class MainActivity : ComponentActivity() {

    private val viewModel: KometViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KometTheme {
                KometApp(viewModel)
            }
        }
        if (BuildConfig.DEBUG && savedInstanceState == null) handleDebugIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (BuildConfig.DEBUG) handleDebugIntent(intent)
    }

    /**
     * Debug builds only: lets verification jump straight to a screen or level, e.g.
     * `adb shell am start -f 0x20000000 -n app.komet.debug/app.komet.MainActivity --es skill m_clock_half`.
     * See docs/AI_INSTRUCTIONS.md for the full list.
     */
    private fun handleDebugIntent(intent: Intent) {
        val extras = intent.extras ?: return
        viewModel.debug(
            skill = extras.getString("skill"),
            screen = extras.getString("screen"),
            stars = extras.getInt("stars", -1),
            unlockAll = extras.getBoolean("unlockAll", false),
            solve = extras.getInt("solve", 0),
            miss = extras.getInt("miss", 0),
            speech = extras.getString("speech"),
            maalform = extras.getString("maalform"),
            letterCase = extras.getString("case"),
            backgroundMusic = extras.getString("music"),
        )
    }

    override fun onStart() {
        super.onStart()
        viewModel.onForeground()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onBackground()
    }
}
