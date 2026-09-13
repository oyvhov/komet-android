package app.komet.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MathContentTest {

    private fun questions(id: String, count: Int = 400): List<Question> {
        val skill = Curriculum.skill(id) ?: error("missing $id")
        val context = QuestionContext(Random(id.hashCode()), Maalform.NYNORSK, LetterCase.UPPER)
        return List(count) { skill.generate(context) }
    }

    private fun answerOf(question: Question): Int = QuestionChecks.correctNumber(question.answer) ?: error("no number in ${question.key}")

    @Test
    fun `counting matches the objects shown`() {
        listOf("m_count5", "m_count10").forEach { id ->
            questions(id).forEach { q ->
                val objects = q.visual as Visual.Objects
                assertEquals(objects.count, answerOf(q))
            }
        }
        questions("m_count20").forEach { q -> assertEquals((q.visual as Visual.TenFrames).first, answerOf(q)) }
        questions("m_tensones").forEach { q ->
            val blocks = q.visual as Visual.BaseTen
            assertEquals(blocks.tens * 10 + blocks.ones, answerOf(q))
        }
    }

    @Test
    fun `number rows follow one step`() {
        listOf("m_findnumber", "m_skip", "m_numpatterns", "m_next10").forEach { id ->
            questions(id).forEach { q ->
                val row = (q.visual as Visual.NumberRow).items
                val filled = row.map { it ?: answerOf(q) }
                val steps = filled.zipWithNext { a, b -> b - a }.distinct()
                assertEquals("$id ${q.key}: $filled", 1, steps.size)
                assertTrue(filled.all { it >= 0 })
            }
        }
    }

    @Test
    fun `sums stay in their range and never go negative`() {
        questions("m_add5").forEach { assertTrue(answerOf(it) in 2..5) }
        questions("m_add10").forEach { assertTrue(answerOf(it) in 2..10) }
        questions("m_add20").forEach { assertTrue(answerOf(it) in 12..19) }
        questions("m_add20_bridge").forEach { assertTrue(answerOf(it) in 11..18) }
        questions("m_add100").forEach { assertTrue(answerOf(it) <= 99) }
        questions("m_sub20_bridge").forEach { assertTrue(answerOf(it) in 2..9) }
        listOf("m_sub5", "m_sub10", "m_sub20", "m_sub100", "m_mixed10", "m_mixed20", "m_missing10").forEach { id ->
            questions(id).forEach { assertTrue("$id ${it.key}", answerOf(it) >= 0) }
        }
    }

    @Test
    fun `take away pictures agree with the sum`() {
        questions("m_sub5").forEach { q ->
            val take = QuestionChecks.visuals(q.visual).filterIsInstance<Visual.TakeAway>().single()
            assertEquals(take.total - take.removed, answerOf(q))
        }
    }

    @Test
    fun `word problems and the shop have consistent answers`() {
        listOf("m_story10", "m_story20").forEach { id ->
            questions(id).forEach { q ->
                val explanation = q.explanation!!.nn
                val result = explanation.substringAfter("= ").trim().toInt()
                assertEquals("$id ${q.key}", result, answerOf(q))
                assertTrue(q.speech.nn.contains("?") && q.speech.nb.contains("?"))
            }
        }
        questions("m_shop").forEach { q ->
            val result = q.explanation!!.nn.substringAfter("= ").removeSuffix(" kr").trim().toInt()
            assertEquals(q.key, result, answerOf(q))
        }
    }

    @Test
    fun `money adds up`() {
        listOf("m_coins20", "m_coins100").forEach { id ->
            questions(id).forEach { q ->
                val money = q.visual as Visual.Money
                assertEquals(money.items.sum(), answerOf(q))
                assertTrue(money.items.all { it in listOf(1, 5, 10, 20, 50) })
            }
        }
    }

    @Test
    fun `balance is really balanced`() {
        questions("m_balance").forEach { q ->
            val balance = q.visual as Visual.Balance
            val answer = answerOf(q)
            val left = QuestionChecks.evaluate(balance.left)
            val right = QuestionChecks.evaluate(balance.right.map { if (it == Token.Blank) Token.Num(answer) else it })
            assertEquals(q.key, left, right)
        }
    }

    @Test
    fun `clocks show the time the answer names`() {
        questions("m_clock_hour").forEach { q ->
            val clock = q.visual as Visual.Clock
            assertEquals("klokka ${clock.hour}", QuestionChecks.correctLabel(q.answer))
            assertEquals(0, clock.minute)
        }
        questions("m_clock_half").forEach { q ->
            val answer = q.answer as Answer.Choice
            when (val visual = q.visual) {
                is Visual.Clock -> {
                    val next = visual.hour % 12 + 1
                    assertEquals(30, visual.minute)
                    assertEquals("halv $next", QuestionChecks.correctLabel(answer))
                }
                is Visual.Glyph -> {
                    val shown = visual.text.nn.removePrefix("halv ").toInt()
                    val face = answer.options[answer.correct] as Option.Clock
                    assertEquals(30, face.minute)
                    assertEquals(shown, face.hour % 12 + 1)
                }
                else -> error("unexpected visual $visual")
            }
        }
        questions("m_clock_quarter").forEach { q ->
            val clock = q.visual as Visual.Clock
            val label = QuestionChecks.correctLabel(q.answer)!!
            if (clock.minute == 15) assertEquals("kvart over ${clock.hour}", label) else assertEquals("kvart på ${clock.hour % 12 + 1}", label)
        }
    }

    @Test
    fun `shapes never treat a square as not a rectangle`() {
        questions("m_shapes").forEach { q ->
            val answer = q.answer as Answer.Choice
            val shapes = answer.options.map { (it as Option.Shape).item.shape }
            val target = shapes[answer.correct]
            if (target == ShapeKind.RECTANGLE) assertTrue(ShapeKind.SQUARE !in shapes)
            if (target == ShapeKind.SQUARE) assertTrue(ShapeKind.RECTANGLE !in shapes)
        }
    }

    @Test
    fun `patterns continue their unit`() {
        questions("m_patterns").forEach { q ->
            val items = (q.visual as Visual.Pattern).items
            val shown = items.dropLast(1).filterNotNull()
            val answer = q.answer as Answer.Choice
            val correct = (answer.options[answer.correct] as Option.Shape).item
            val sequence = shown + correct
            val period = (1..3).first { p -> sequence.indices.all { i -> i < p || sequence[i] == sequence[i - p] } }
            assertTrue("pattern ${q.key} has no period", period in 1..3)
            // No distractor may also continue the pattern.
            answer.options.forEachIndexed { index, option ->
                if (index != answer.correct) assertTrue((option as Option.Shape).item != correct)
            }
        }
    }

    @Test
    fun `even and odd are labelled correctly`() {
        questions("m_evenodd").forEach { q ->
            val n = (q.visual as Visual.Glyph).text.nn.toInt()
            val label = QuestionChecks.correctLabel(q.answer)
            assertEquals(if (n % 2 == 0) "Partal" else "Oddetal", label)
        }
    }

    @Test
    fun `corner counts are right`() {
        val corners = mapOf(ShapeKind.CIRCLE to 0, ShapeKind.TRIANGLE to 3, ShapeKind.SQUARE to 4, ShapeKind.RECTANGLE to 4, ShapeKind.PENTAGON to 5, ShapeKind.HEXAGON to 6)
        questions("m_corners").forEach { q ->
            val shape = (q.visual as Visual.Shape).item.shape
            assertEquals(corners.getValue(shape), answerOf(q))
        }
    }
}
