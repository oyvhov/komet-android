package app.komet.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail

/** Shared structural checks that every generated task must pass. */
object QuestionChecks {

    fun structure(skill: Skill, question: Question) {
        val where = "${skill.id} «${question.key}»"
        assertTrue("$where has no key", question.key.isNotBlank())
        assertTrue("$where has an empty prompt", question.prompt.nn.isNotBlank() && question.prompt.nb.isNotBlank())
        assertTrue("$where has an empty speech text", question.speech.nn.isNotBlank() && question.speech.nb.isNotBlank())
        when (val answer = question.answer) {
            is Answer.Choice -> {
                assertTrue("$where must offer 2–4 options, has ${answer.options.size}", answer.options.size in 2..4)
                assertEquals("$where has duplicate options: ${answer.options}", answer.options.size, answer.options.distinct().size)
                val labels = answer.options.filterIsInstance<Option.Label>()
                if (labels.size == answer.options.size) {
                    assertEquals("$where has labels that read the same in nynorsk", labels.size, labels.map { it.text.nn.lowercase() }.distinct().size)
                    assertEquals("$where has labels that read the same in bokmål", labels.size, labels.map { it.text.nb.lowercase() }.distinct().size)
                }
                val pictures = answer.options.filterIsInstance<Option.Picture>()
                if (pictures.size == answer.options.size) {
                    assertEquals("$where shows the same picture twice", pictures.size, pictures.map { it.emoji }.distinct().size)
                }
            }
            is Answer.NumberInput -> assertTrue("$where expects ${answer.correct}", answer.correct in 0..999)
            is Answer.Trace -> assertTrue("$where asks to write «${answer.symbol}», which has no strokes", Strokes.glyph(answer.symbol) != null)
            is Answer.Build -> {
                assertTrue("$where has nothing to build", answer.target.isNotEmpty())
                val tiles = answer.tiles.groupingBy { it }.eachCount()
                answer.target.groupingBy { it }.eachCount().forEach { (value, count) ->
                    assertTrue("$where lacks tile «$value»", (tiles[value] ?: 0) >= count)
                }
            }
        }
        visuals(question.visual).forEach { visual ->
            if (visual is Visual.Equation) equation(where, visual.tokens, question.answer)
        }
    }

    fun visuals(visual: Visual): List<Visual> = when (visual) {
        is Visual.Stack -> visual.items.flatMap(::visuals)
        else -> listOf(visual)
    }

    /** The numeric value of the right answer, whatever form the answer takes. */
    fun correctNumber(answer: Answer): Int? = when (answer) {
        is Answer.NumberInput -> answer.correct
        is Answer.Choice -> (answer.options[answer.correct] as? Option.Label)?.text?.nn?.takeWhile { it.isDigit() }?.toIntOrNull()
        is Answer.Build, is Answer.Trace -> null
    }

    fun correctLabel(answer: Answer): String? =
        ((answer as? Answer.Choice)?.options?.get(answer.correct) as? Option.Label)?.text?.nn

    private fun equation(where: String, tokens: List<Token>, answer: Answer) {
        val blanks = tokens.count { it == Token.Blank }
        if (blanks != 1) return
        val equals = tokens.indexOf(Token.Op("="))
        if (equals < 0) {
            // «a ? b» with a sign to choose.
            val a = (tokens[0] as Token.Num).value
            val b = (tokens[2] as Token.Num).value
            val sign = correctLabel(answer)
            val expected = when {
                a < b -> "<"
                a > b -> ">"
                else -> "="
            }
            assertEquals("$where picks the wrong sign", expected, sign)
            return
        }
        val left = tokens.subList(0, equals)
        val right = tokens.subList(equals + 1, tokens.size)
        val value = correctNumber(answer) ?: throw AssertionError("$where has no numeric answer")
        val filledLeft = left.map { if (it == Token.Blank) Token.Num(value) else it }
        val filledRight = right.map { if (it == Token.Blank) Token.Num(value) else it }
        assertEquals("$where: ${render(tokens)} is not true for $value", evaluate(filledLeft), evaluate(filledRight))
    }

    private fun render(tokens: List<Token>) = tokens.joinToString(" ") {
        when (it) {
            is Token.Num -> it.value.toString()
            is Token.Op -> it.symbol
            Token.Blank -> "?"
        }
    }

    /** Evaluates +, −, × and : with the usual precedence. Division must be exact. */
    fun evaluate(tokens: List<Token>): Int {
        val values = mutableListOf<Int>()
        val ops = mutableListOf<String>()
        var index = 0
        values += (tokens[index++] as Token.Num).value
        while (index < tokens.size) {
            val op = (tokens[index++] as Token.Op).symbol
            val next = (tokens[index++] as Token.Num).value
            when (op) {
                TIMES -> values[values.lastIndex] = values.last() * next
                DIVIDE -> {
                    assertEquals("division is not exact", 0, values.last() % next)
                    values[values.lastIndex] = values.last() / next
                }
                else -> {
                    ops += op
                    values += next
                }
            }
        }
        var result = values.first()
        ops.forEachIndexed { i, op ->
            result = if (op == "+") result + values[i + 1] else result - values[i + 1]
        }
        return result
    }
}
