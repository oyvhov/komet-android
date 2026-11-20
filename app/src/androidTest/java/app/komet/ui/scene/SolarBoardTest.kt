package app.komet.ui.scene

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import app.komet.domain.LetterCase
import app.komet.domain.Maalform
import app.komet.domain.SolarDestination
import app.komet.ui.theme.KometTheme
import app.komet.ui.theme.LocalMotion
import app.komet.ui.theme.LocalReading
import app.komet.ui.theme.ReadingPrefs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class SolarBoardTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun allEightPlanetsFitAndOpenTheRightDestinationOnASmallPhone() {
        var opened: SolarDestination? = null
        compose.setContent {
            TestReading(motion = false) {
                Box(Modifier.width(296.dp)) {
                    SolarBoard(SolarDestination.EARTH, setOf(SolarDestination.VENUS)) { opened = it }
                }
            }
        }
        for (destination in SolarDestination.entries) {
            compose.onNodeWithText(destination.planet.name.nb).assertIsDisplayed().performClick()
            compose.runOnIdle { assertEquals(destination, opened) }
        }
    }

    @Test
    fun rocketFinishesItsFlightBeforeOpeningTheFirstSelectedPlanet() {
        var opened: SolarDestination? = null
        compose.setContent {
            TestReading(motion = true) {
                Box(Modifier.width(296.dp).height(460.dp)) {
                    val time = remember { mutableFloatStateOf(0f) }
                    SolarJourney(SolarDestination.MERCURY, null, emptySet(), rocketPaint(null), time) { opened = it }
                }
            }
        }
        compose.mainClock.autoAdvance = false

        compose.onNodeWithText(SolarDestination.VENUS.planet.name.nb).assertIsDisplayed().performClick()
        compose.mainClock.advanceTimeBy(450)
        compose.runOnIdle { assertNull(opened) }
        compose.onNodeWithText(SolarDestination.MERCURY.planet.name.nb).performClick()

        compose.mainClock.advanceTimeBy(650)
        compose.runOnIdle { assertEquals(SolarDestination.VENUS, opened) }
    }

    @Composable
    private fun TestReading(motion: Boolean, content: @Composable () -> Unit) {
        KometTheme {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale = 2f),
                LocalMotion provides motion,
                LocalReading provides ReadingPrefs(Maalform.BOKMAAL, LetterCase.LOWER),
                content = content,
            )
        }
    }
}
