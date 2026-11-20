package app.komet.ui

import app.komet.domain.Answer
import app.komet.domain.Curriculum
import app.komet.domain.Maalform
import app.komet.domain.Question
import app.komet.domain.Visual
import app.komet.domain.txt
import org.junit.Assert.*
import org.junit.Test

class CompanionMessageTest {
    private fun task(key: String = "first") = Question(
        key = key,
        prompt = txt("Vel eit tal", "Velg et tall"),
        visual = Visual.None,
        answer = Answer.NumberInput(7),
        speech = txt("Oppgåva $key", "Oppgaven $key"),
        reward = txt("Hemmeløna", "Hemmeligbelønningen"),
        explanation = txt("Fasiten er sju", "Fasiten er syv"),
        hint = Visual.Objects("⭐", 7),
    )

    private fun round() = RoundState(Curriculum.skill("m_add10")!!, listOf(task(), task("second")))

    @Test fun `asking Bolt before answering never leaks rewards or explanations`() {
        val round = round()
        for (misses in 0..2) for (hint in listOf(false, true)) {
            round.misses = misses
            round.hintShown = hint
            val message = round.companionMessage()
            for (form in Maalform.entries) {
                assertTrue(message.speech.get(form).contains(round.question.speech.get(form)))
                assertFalse(message.speech.get(form).contains(round.question.explanation!!.get(form)))
                assertFalse(message.speech.get(form).contains(round.question.reward!!.get(form)))
            }
        }
    }

    @Test fun `correct after mistakes celebrates and explains instead of asking to retry`() {
        val round = round()
        round.misses = 1
        round.hintShown = true
        round.phase = Phase.CORRECT
        val message = round.companionMessage()
        assertEquals(CompanionReaction.CHEER, message.reaction)
        for (form in Maalform.entries) {
            assertTrue(message.speech.get(form).contains(round.question.explanation!!.get(form)))
            assertFalse(message.speech.get(form).contains(S.tryAgain.get(form)))
        }
    }

    @Test fun `revealed answer offers the actual explanation in each language`() {
        val round = round()
        round.misses = 2
        round.phase = Phase.REVEALED
        val message = round.companionMessage()
        assertEquals(CompanionReaction.EXPLAIN, message.reaction)
        assertTrue(message.speech.nn.contains("Fasiten er sju"))
        assertTrue(message.speech.nb.contains("Fasiten er syv"))
    }

    @Test fun `next task clears help and retry responses even though shake count is retained`() {
        val round = round()
        round.misses = 1
        round.shakeCount = 1
        round.hintShown = true
        round.index++
        round.resetForNext()
        val message = round.companionMessage()
        assertEquals(CompanionReaction.READY, message.reaction)
        assertTrue(message.speech.nn.contains("Oppgåva second"))
        assertTrue(message.speech.nb.contains("Oppgaven second"))
        assertFalse(message.speech.nn.contains("first"))
    }

    @Test fun `support only refers to a hint if the task has one and it is visible`() {
        val round = round()
        round.misses = 1
        assertEquals(CompanionReaction.RETRY, round.companionMessage().reaction)
        round.hintShown = true
        assertEquals(CompanionReaction.HINT, round.companionMessage().reaction)
        val noHint = RoundState(round.skill, listOf(task().copy(hint = null)))
        noHint.hintShown = true
        assertEquals(CompanionReaction.READY, noHint.companionMessage().reaction)
    }
}
