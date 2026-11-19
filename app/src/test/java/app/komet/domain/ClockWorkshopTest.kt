package app.komet.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClockWorkshopTest {
    @Test
    fun digitalAndAnalogStayInSyncAcrossNoonAndMidnight() {
        val beforeMidnight = ClockTime.at(23, 59)
        val midnight = beforeMidnight + 1
        assertEquals("00:00", midnight.digital)
        assertEquals(beforeMidnight, midnight + -1)
        assertEquals("12:00", (midnight + 720).digital)
        assertEquals(midnight.hourAngle, (midnight + 720).hourAngle, 0f)
        assertEquals(midnight.minuteAngle, (midnight + 720).minuteAngle, 0f)
        assertEquals(12, midnight.dialHour)
        assertEquals(105f, ClockTime.at(15, 30).hourAngle, 0f)
        assertEquals(180f, ClockTime.at(15, 30).minuteAngle, 0f)
        assertEquals("Halv 4", ClockTime.at(15, 30).spokenDial().nn)
        assertEquals("Kvart på 1", ClockTime.at(12, 45).spokenDial().nb)
    }

    @Test
    fun minuteHandCrossesTwelveInBothDirections() {
        assertEquals(6f, clockSweep(357f, 3f), 0f)
        assertEquals(-6f, clockSweep(3f, 357f), 0f)
        assertEquals("04:00", ClockTime.at(3, 55).moveMinuteTo(359f, 5).digital)
        assertEquals("03:55", ClockTime.at(4, 0).moveMinuteTo(330f, 5).digital)
        assertEquals("04:30", ClockTime.at(4, 0).moveMinuteTo(180f, 30).digital)
        assertEquals("03:30", ClockTime.at(3, 20).moveMinuteTo(177f, 15).digital)
    }

    @Test
    fun roundsHaveOneCorrectChoiceAndPlayableHandTargetsAtEveryLevel() {
        val expectedMinutes = mapOf(
            ClockLevel.HOURS to setOf(0),
            ClockLevel.HALVES to setOf(30),
            ClockLevel.QUARTERS to setOf(15, 30, 45),
            ClockLevel.FIVE_MINUTES to (0..55 step 5).toSet(),
        )
        for ((level, allowedMinutes) in expectedMinutes) {
            repeat(100) { seed ->
                val tasks = level.round(seed.toLong())
                assertEquals(6, tasks.size)
                assertEquals(6, tasks.map { it.target }.distinct().size)
                tasks.forEachIndexed { index, challenge ->
                    val (hour, minute) = challenge.target.digital.split(':').map(String::toInt)
                    val expected = ClockTime.fromMinutes(hour * 60 + minute)
                    assertTrue(hour in 1..12)
                    assertTrue(minute in allowedMinutes)
                    assertEquals((hour % 12) * 30f + minute / 2f, challenge.target.hourAngle, 0f)
                    assertEquals(minute * 6f, challenge.target.minuteAngle, 0f)
                    assertEquals(index % 2 == 1, challenge.setHands)
                    assertTrue(challenge.matches(expected))
                    assertFalse(challenge.matches(expected + 60))
                    if (challenge.setHands) {
                        assertFalse(challenge.matches(challenge.start))
                        assertEquals(0, (minute - challenge.start.minute) % level.minuteStep)
                    } else {
                        assertEquals(3, challenge.choices.distinct().size)
                        assertEquals(listOf(expected), challenge.choices.filter(challenge::matches))
                    }
                }
            }
        }
    }
}
