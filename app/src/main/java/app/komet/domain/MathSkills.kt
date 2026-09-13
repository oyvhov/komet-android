package app.komet.domain

import kotlin.random.Random

const val MINUS = "−"
const val TIMES = "×"
const val DIVIDE = ":"

private val countables = listOf("⭐", "🚀", "🪐", "🌙", "🍎", "⚽", "🐟", "🦖", "🚗", "💎", "🎈", "🍓").map { it.asEmoji() }

private fun equation(vararg parts: Any): Visual.Equation = Visual.Equation(
    parts.map { part ->
        when (part) {
            is Int -> Token.Num(part)
            "?" -> Token.Blank
            is String -> Token.Op(part)
            else -> error("Unsupported token $part")
        }
    },
)

private fun opWord(op: String): Txt = when (op) {
    "+" -> txt("pluss")
    MINUS -> txt("minus")
    TIMES -> txt("gonger", "ganger")
    DIVIDE -> txt("delt på")
    else -> txt(op)
}

/** «Kva er 3 pluss 4?» */
private fun whatIs(a: Int, op: String, b: Int): Txt {
    val word = opWord(op)
    return txt("Kva er $a ${word.nn} $b?", "Hva er $a ${word.nb} $b?")
}

private fun done(a: Int, op: String, b: Int, result: Int): Txt = txt("$a $op $b = $result")

private fun math(
    id: String,
    title: Txt,
    detail: Txt,
    grade: Int,
    symbol: String,
    length: Int = 8,
    generate: (QuestionContext) -> Question,
) = Skill(id, Subject.MATH, title, detail, grade, symbol, length, generate)

// ── Word problems ──────────────────────────────────────────────────────────────────────────────

private data class Kid(val name: String, val girl: Boolean) {
    fun pronoun(): Txt = if (girl) txt("ho", "hun") else txt("han")
    fun Pronoun(): Txt = if (girl) txt("Ho", "Hun") else txt("Han")
}

private val kids = listOf(
    Kid("Ola", false), Kid("Emma", true), Kid("Noah", false), Kid("Nora", true),
    Kid("Jakob", false), Kid("Sofie", true), Kid("Filip", false), Kid("Ella", true),
    Kid("Isak", false), Kid("Maja", true), Kid("Emil", false), Kid("Ingrid", true),
)

private data class Thing(val emoji: String, val one: Txt, val many: Txt, val food: Boolean) {
    fun count(n: Int): Txt = if (n == 1) one else many
}

private val things = listOf(
    Thing("🍎".asEmoji(), txt("eple"), txt("eple", "epler"), food = true),
    Thing("⭐".asEmoji(), txt("stjerne"), txt("stjerner"), food = false),
    Thing("🎈".asEmoji(), txt("ballong"), txt("ballongar", "ballonger"), food = false),
    Thing("🚗".asEmoji(), txt("lekebil"), txt("lekebilar", "lekebiler"), food = false),
    Thing("🐚".asEmoji(), txt("skjel", "skjell"), txt("skjel", "skjell"), food = false),
    Thing("🍪".asEmoji(), txt("kjeks"), txt("kjeks"), food = true),
    Thing("⚽".asEmoji(), txt("ball"), txt("ballar", "baller"), food = false),
    Thing("🍓".asEmoji(), txt("jordbær"), txt("jordbær"), food = true),
    Thing("🧁".asEmoji(), txt("muffins"), txt("muffins"), food = true),
    Thing("🚀".asEmoji(), txt("rakett"), txt("rakettar", "raketter"), food = false),
    Thing("🍌".asEmoji(), txt("banan"), txt("bananar", "bananer"), food = true),
)

private enum class StoryKind { GET_MORE, TOGETHER, GIVE_AWAY, EAT, HOW_MANY_MORE }

private fun wordProblem(r: Random, max: Int, allowCompare: Boolean, keypad: Boolean): Question {
    val kinds = StoryKind.entries.filter { allowCompare || it != StoryKind.HOW_MANY_MORE }
    val kind = r.pick(kinds)
    val thing = if (kind == StoryKind.EAT) r.pick(things.filter { it.food }) else r.pick(things)
    val (kid, other) = r.pickDistinct(kids, 2)
    val n = kid.name
    val p = kid.pronoun()
    val P = kid.Pronoun()
    val result: Int
    val text: Txt
    val hint: Visual
    val explanation: Txt
    when (kind) {
        StoryKind.GET_MORE -> {
            val a = r.between(1, max - 1)
            val b = r.between(1, max - a)
            result = a + b
            val c = thing.count(a)
            text = txt(
                "$n har $a ${c.nn}. ${P.nn} får $b til. Kor mange ${thing.many.nn} har ${p.nn} no?",
                "$n har $a ${c.nb}. ${P.nb} får $b til. Hvor mange ${thing.many.nb} har ${p.nb} nå?",
            )
            hint = Visual.AddGroups(thing.emoji, a, b)
            explanation = done(a, "+", b, result)
        }
        StoryKind.TOGETHER -> {
            val a = r.between(1, max - 1)
            val b = r.between(1, max - a)
            result = a + b
            val c = thing.count(a)
            text = txt(
                "$n har $a ${c.nn}. ${other.name} har $b. Kor mange ${thing.many.nn} har dei til saman?",
                "$n har $a ${c.nb}. ${other.name} har $b. Hvor mange ${thing.many.nb} har de til sammen?",
            )
            hint = Visual.AddGroups(thing.emoji, a, b)
            explanation = done(a, "+", b, result)
        }
        StoryKind.GIVE_AWAY -> {
            val a = r.between(2, max)
            val b = r.between(1, a - 1)
            result = a - b
            val c = thing.count(a)
            text = txt(
                "$n har $a ${c.nn}. ${P.nn} gir bort $b. Kor mange har ${p.nn} att?",
                "$n har $a ${c.nb}. ${P.nb} gir bort $b. Hvor mange har ${p.nb} igjen?",
            )
            hint = Visual.TakeAway(thing.emoji, a, b)
            explanation = done(a, MINUS, b, result)
        }
        StoryKind.EAT -> {
            val a = r.between(2, max)
            val b = r.between(1, a - 1)
            result = a - b
            val c = thing.count(a)
            text = txt(
                "Det ligg $a ${c.nn} på bordet. $n et $b. Kor mange ligg att?",
                "Det ligger $a ${c.nb} på bordet. $n spiser $b. Hvor mange ligger igjen?",
            )
            hint = Visual.TakeAway(thing.emoji, a, b)
            explanation = done(a, MINUS, b, result)
        }
        StoryKind.HOW_MANY_MORE -> {
            val a = r.between(3, max)
            val b = r.between(1, a - 1)
            result = a - b
            val c = thing.count(a)
            text = txt(
                "$n har $a ${c.nn}. ${other.name} har $b. Kor mange fleire har $n?",
                "$n har $a ${c.nb}. ${other.name} har $b. Hvor mange flere har $n?",
            )
            hint = Visual.Stack(listOf(Visual.Objects(thing.emoji, a), Visual.Objects(thing.emoji, b)))
            explanation = done(a, MINUS, b, result)
        }
    }
    val answer = if (keypad) Answer.NumberInput(result) else choiceOfNumbers(result, r, count = 4, min = 0, max = max + 2)
    return Question(
        key = text.nn,
        prompt = txt("Les og rekn.", "Les og regn."),
        visual = Visual.Story(text, reading = false),
        answer = answer,
        speech = text,
        explanation = explanation,
        hint = hint,
    )
}

// ── Shop ───────────────────────────────────────────────────────────────────────────────────────

private data class Ware(val emoji: String, val name: Txt)

private val wares = listOf(
    Ware("🍦".asEmoji(), txt("ein is", "en is")),
    Ware("🧸".asEmoji(), txt("ein bamse", "en bamse")),
    Ware("✏".asEmoji(), txt("ein blyant", "en blyant")),
    Ware("🎈".asEmoji(), txt("ein ballong", "en ballong")),
    Ware("🍌".asEmoji(), txt("ein banan", "en banan")),
    Ware("🍫".asEmoji(), txt("ein sjokolade", "en sjokolade")),
    Ware("📕".asEmoji(), txt("ei bok", "en bok")),
    Ware("🪁".asEmoji(), txt("ein drage", "en drage")),
    Ware("⚽".asEmoji(), txt("ein fotball", "en fotball")),
    Ware("🍎".asEmoji(), txt("eit eple", "et eple")),
)

private fun capital(text: Txt) = text.map { value -> value.replaceFirstChar { it.uppercaseChar() } }

private fun shopQuestion(r: Random): Question {
    return if (r.nextBoolean()) {
        val ware = r.pick(wares)
        val paid = r.pick(listOf(10, 20))
        val price = r.between(1, paid - 1)
        val change = paid - price
        val name = capital(ware.name)
        val text = txt(
            "${name.nn} kostar $price kr. Du betaler med $paid kr. Kor mykje får du att?",
            "${name.nb} koster $price kr. Du betaler med $paid kr. Hvor mye får du tilbake?",
        )
        Question(
            key = text.nn,
            prompt = txt("Handle i butikken", "Handle i butikken"),
            visual = Visual.Stack(listOf(Visual.Picture(ware.emoji), Visual.Story(text, reading = false))),
            answer = moneyChoice(change, r, 0, paid),
            speech = text,
            explanation = txt("$paid $MINUS $price = $change kr"),
            hint = Visual.Money(listOf(paid)),
        )
    } else {
        val (first, second) = r.pickDistinct(wares, 2)
        val a = r.between(1, 10)
        val b = r.between(1, 10)
        val text = txt(
            "Du kjøper ${first.name.nn} til $a kr og ${second.name.nn} til $b kr. Kor mykje kostar det til saman?",
            "Du kjøper ${first.name.nb} til $a kr og ${second.name.nb} til $b kr. Hvor mye koster det til sammen?",
        )
        Question(
            key = text.nn,
            prompt = txt("Handle i butikken", "Handle i butikken"),
            visual = Visual.Stack(listOf(Visual.Picture(first.emoji + " " + second.emoji), Visual.Story(text, reading = false))),
            answer = moneyChoice(a + b, r, 2, 22),
            speech = text,
            explanation = txt("$a + $b = ${a + b} kr"),
        )
    }
}

private fun moneyChoice(correct: Int, r: Random, min: Int, max: Int): Answer.Choice {
    val (values, index) = numberChoices(correct, r, count = 4, min = min, max = max)
    return Answer.Choice(values.map { Option.Label(txt("$it kr"), GlyphKind.PLAIN) }, index)
}

// ── Clock ──────────────────────────────────────────────────────────────────────────────────────

private fun wrap12(hour: Int): Int = ((hour - 1).mod(12)) + 1

private fun hourLabel(hour: Int) = Option.Label(txt("klokka $hour"), GlyphKind.PLAIN)

// ── Shapes ─────────────────────────────────────────────────────────────────────────────────────

private data class ShapeName(
    val shape: ShapeKind,
    val indefinite: Txt,
    val definite: Txt,
    /** With article: «ein sirkel», «eit kvadrat», «ei stjerne». */
    val withArticle: Txt,
    val corners: Int?,
)

private val shapeNames = listOf(
    ShapeName(ShapeKind.CIRCLE, txt("sirkel"), txt("sirkelen"), txt("ein sirkel", "en sirkel"), 0),
    ShapeName(ShapeKind.TRIANGLE, txt("trekant"), txt("trekanten"), txt("ein trekant", "en trekant"), 3),
    ShapeName(ShapeKind.SQUARE, txt("kvadrat"), txt("kvadratet"), txt("eit kvadrat", "et kvadrat"), 4),
    ShapeName(ShapeKind.RECTANGLE, txt("rektangel"), txt("rektangelet"), txt("eit rektangel", "et rektangel"), 4),
    ShapeName(ShapeKind.PENTAGON, txt("femkant"), txt("femkanten"), txt("ein femkant", "en femkant"), 5),
    ShapeName(ShapeKind.HEXAGON, txt("sekskant"), txt("sekskanten"), txt("ein sekskant", "en sekskant"), 6),
    ShapeName(ShapeKind.STAR, txt("stjerne"), txt("stjerna", "stjernen"), txt("ei stjerne", "en stjerne"), null),
    ShapeName(ShapeKind.HEART, txt("hjarte", "hjerte"), txt("hjartet", "hjertet"), txt("eit hjarte", "et hjerte"), null),
)

private fun shapeName(kind: ShapeKind) = shapeNames.first { it.shape == kind }

/** A square is also a rectangle, so a square must never be offered as «not a rectangle». */
private fun compatibleDistractor(target: ShapeKind, other: ShapeKind): Boolean =
    other != target && !(target == ShapeKind.RECTANGLE && other == ShapeKind.SQUARE) &&
        !(target == ShapeKind.SQUARE && other == ShapeKind.RECTANGLE)

private fun colors(r: Random, count: Int): List<Int> = (0 until 6).shuffled(r).take(count)

// ── Money ──────────────────────────────────────────────────────────────────────────────────────

private fun coins(r: Random, maxSum: Int, allowed: List<Int>, minCount: Int, maxCount: Int): List<Int> {
    while (true) {
        val count = r.between(minCount, maxCount)
        val picked = List(count) { r.pick(allowed) }
        if (picked.sum() in 2..maxSum) return picked.sortedDescending()
    }
}

// ── Curriculum ─────────────────────────────────────────────────────────────────────────────────

object MathCurriculum {

    val chapters: List<Chapter> = listOf(
        Chapter(
            id = "m_tal",
            subject = Subject.MATH,
            title = txt("Tal", "Tall"),
            look = PlanetLook(0xFFB8C4D9, 0xFFE8EEF8, 0xFF6E7B94, craters = true),
            skills = listOf(
                math("m_count5", txt("Tel til 5", "Tell til 5"), txt("Tel ting", "Tell ting"), 0, "5") { ctx ->
                    val r = ctx.random
                    val n = r.between(1, 5)
                    val emoji = r.pick(countables)
                    Question(
                        key = "$n$emoji",
                        prompt = txt("Kor mange ser du?", "Hvor mange ser du?"),
                        visual = Visual.Objects(emoji, n),
                        answer = choiceOfNumbers(n, r, count = 3, min = 1, max = 6),
                        explanation = txt("Det er $n."),
                    )
                },
                math("m_count10", txt("Tel til 10", "Tell til 10"), txt("Tel ting", "Tell ting"), 0, "10") { ctx ->
                    val r = ctx.random
                    val n = r.between(4, 10)
                    val emoji = r.pick(countables)
                    Question(
                        key = "$n$emoji",
                        prompt = txt("Kor mange ser du?", "Hvor mange ser du?"),
                        visual = Visual.Objects(emoji, n, scattered = r.chance(0.4)),
                        answer = choiceOfNumbers(n, r, count = 4, min = 1, max = 12),
                        explanation = txt("Det er $n."),
                        hint = Visual.TenFrames(n),
                    )
                },
                math("m_next10", txt("Før og etter"), txt("Tal frå 0 til 10", "Tall fra 0 til 10"), 0, "→") { ctx ->
                    val r = ctx.random
                    val after = r.nextBoolean()
                    if (after) {
                        val n = r.between(0, 9)
                        val row = if (n >= 1) listOf(n - 1, n, null) else listOf(n, null)
                        Question(
                            key = "a$n",
                            prompt = txt("Kva tal kjem etter $n?", "Hvilket tall kommer etter $n?"),
                            visual = Visual.NumberRow(row),
                            answer = choiceOfNumbers(n + 1, r, count = 3, min = 0, max = 11, likely = listOf(n - 1)),
                            explanation = txt("${n + 1} kjem etter $n.", "${n + 1} kommer etter $n."),
                        )
                    } else {
                        val n = r.between(1, 10)
                        val row = if (n <= 9) listOf(null, n, n + 1) else listOf(null, n)
                        Question(
                            key = "b$n",
                            prompt = txt("Kva tal kjem før $n?", "Hvilket tall kommer før $n?"),
                            visual = Visual.NumberRow(row),
                            answer = choiceOfNumbers(n - 1, r, count = 3, min = 0, max = 11, likely = listOf(n + 1)),
                            explanation = txt("${n - 1} kjem før $n.", "${n - 1} kommer før $n."),
                        )
                    }
                },
                math("m_compare10", txt("Størst og minst"), txt("Samanlikn tal", "Sammenlign tall"), 0, ">") { ctx ->
                    val r = ctx.random
                    val a = r.between(0, 10)
                    var b = r.between(0, 10)
                    while (b == a) b = r.between(0, 10)
                    val biggest = r.nextBoolean()
                    val correct = if (biggest) maxOf(a, b) else minOf(a, b)
                    Question(
                        key = "$a-$b-$biggest",
                        prompt = if (biggest) txt("Kva tal er størst?", "Hvilket tall er størst?") else txt("Kva tal er minst?", "Hvilket tall er minst?"),
                        visual = Visual.Compare(a, b),
                        answer = Answer.Choice(listOf(numberLabel(a), numberLabel(b)), if (correct == a) 0 else 1),
                        explanation = if (biggest) txt("${maxOf(a, b)} er størst.") else txt("${minOf(a, b)} er minst."),
                    )
                },
                math("m_count20", txt("Tel til 20", "Tell til 20"), txt("Tiarrammer", "Tierrammer"), 1, "20") { ctx ->
                    val r = ctx.random
                    val n = r.between(11, 20)
                    Question(
                        key = "$n",
                        prompt = txt("Kor mange prikkar er det?", "Hvor mange prikker er det?"),
                        visual = Visual.TenFrames(n),
                        answer = choiceOfNumbers(n, r, count = 4, min = 10, max = 22, likely = listOf(n - 10 + 1)),
                        explanation = txt("10 og ${n - 10} blir $n.", "10 og ${n - 10} blir $n."),
                    )
                },
                math("m_findnumber", txt("Finn talet", "Finn tallet"), txt("Tal opp til 20", "Tall opp til 20"), 1, "?") { ctx ->
                    val r = ctx.random
                    val start = r.between(0, 16)
                    val gap = r.between(1, 4)
                    val row = (0 until 5).map { if (it == gap) null else start + it }
                    val answer = start + gap
                    Question(
                        key = "$start-$gap",
                        prompt = txt("Kva tal manglar?", "Hvilket tall mangler?"),
                        visual = Visual.NumberRow(row),
                        answer = choiceOfNumbers(answer, r, count = 4, min = 0, max = 22),
                        explanation = txt("$answer manglar.", "$answer mangler."),
                    )
                },
                math("m_tensones", txt("Tiarar og einarar", "Tiere og enere"), txt("Tal opp til 99", "Tall opp til 99"), 1, "10") { ctx ->
                    val r = ctx.random
                    val tens = r.between(1, 9)
                    val ones = r.between(0, 9)
                    val n = tens * 10 + ones
                    val swapped = ones * 10 + tens
                    Question(
                        key = "$n",
                        prompt = txt("Kva tal er dette?", "Hvilket tall er dette?"),
                        visual = Visual.BaseTen(tens, ones),
                        answer = choiceOfNumbers(n, r, count = 4, min = 1, max = 99, likely = listOfNotNull(swapped.takeIf { ones > 0 && it != n }, n + 10, n - 1)),
                        explanation = txt("$tens tiarar og $ones einarar er $n.", "$tens tiere og $ones enere er $n."),
                    )
                },
                math("m_skip", txt("Hoppetelling"), txt("Tel med 2, 5 og 10", "Tell med 2, 5 og 10"), 1, "+2") { ctx ->
                    val r = ctx.random
                    val step = r.pick(listOf(2, 5, 10))
                    val start = step * r.between(0, 4)
                    val gap = r.between(2, 4)
                    val row = (0 until 5).map { if (it == gap) null else start + it * step }
                    val answer = start + gap * step
                    Question(
                        key = "$step-$start-$gap",
                        prompt = txt("Tel med $step. Kva tal manglar?", "Tell med $step. Hvilket tall mangler?"),
                        visual = Visual.NumberRow(row),
                        answer = choiceOfNumbers(answer, r, count = 4, min = 0, max = 100, likely = listOf(answer + 1, answer - step, answer + step)),
                        explanation = txt("Du legg til $step kvar gong.", "Du legger til $step hver gang."),
                    )
                },
                math("m_compare100", txt("Større eller mindre"), txt("Teikna <, > og =", "Tegnene <, > og ="), 2, "<") { ctx ->
                    val r = ctx.random
                    val a = r.between(10, 99)
                    val b = if (r.chance(0.2)) a else r.between(10, 99)
                    val sign = when {
                        a < b -> "<"
                        a > b -> ">"
                        else -> "="
                    }
                    val signs = listOf("<", ">", "=")
                    Question(
                        key = "$a-$b",
                        prompt = txt("Vel rett teikn", "Velg riktig tegn"),
                        visual = equation(a, "?", b),
                        answer = Answer.Choice(signs.map { Option.Label(txt(it), GlyphKind.NUMBER) }, signs.indexOf(sign)),
                        speech = txt("Er $a større enn, mindre enn eller lik $b?"),
                        explanation = when (sign) {
                            "<" -> txt("$a er mindre enn $b.")
                            ">" -> txt("$a er større enn $b.")
                            else -> txt("$a er lik $b.")
                        },
                    )
                },
                math("m_evenodd", txt("Partal og oddetal", "Partall og oddetall"), txt("Tal opp til 20", "Tall opp til 20"), 2, "2") { ctx ->
                    val r = ctx.random
                    val n = r.between(1, 20)
                    val even = n % 2 == 0
                    val options = listOf(
                        Option.Label(txt("Partal", "Partall"), GlyphKind.PLAIN),
                        Option.Label(txt("Oddetal", "Oddetall"), GlyphKind.PLAIN),
                    )
                    Question(
                        key = "$n",
                        prompt = txt("Er $n eit partal eller eit oddetal?", "Er $n et partall eller et oddetall?"),
                        visual = Visual.Glyph(txt("$n"), GlyphKind.NUMBER),
                        answer = Answer.Choice(options, if (even) 0 else 1),
                        explanation = txt("Partal kan delast i to like store grupper.", "Partall kan deles i to like store grupper."),
                        hint = Visual.Objects("🧦".asEmoji(), n),
                    )
                },
            ),
        ),
        Chapter(
            id = "m_pluss",
            subject = Subject.MATH,
            title = txt("Pluss"),
            look = PlanetLook(0xFFFFB25C, 0xFFFFE1A8, 0xFFC4622A, bands = true),
            skills = listOf(
                math("m_add5", txt("Pluss til 5"), txt("Med ting", "Med ting"), 0, "+") { ctx ->
                    val r = ctx.random
                    val a = r.between(1, 4)
                    val b = r.between(1, 5 - a)
                    val emoji = r.pick(countables)
                    Question(
                        key = "$a+$b",
                        prompt = whatIs(a, "+", b),
                        visual = Visual.Stack(listOf(equation(a, "+", b, "=", "?"), Visual.AddGroups(emoji, a, b))),
                        answer = choiceOfNumbers(a + b, r, count = 3, min = 1, max = 6),
                        explanation = done(a, "+", b, a + b),
                    )
                },
                math("m_add10", txt("Pluss til 10"), txt("Vel svaret", "Velg svaret"), 1, "+") { ctx ->
                    val r = ctx.random
                    val a = r.between(1, 9)
                    val b = r.between(1, 10 - a)
                    Question(
                        key = "$a+$b",
                        prompt = whatIs(a, "+", b),
                        visual = equation(a, "+", b, "=", "?"),
                        answer = choiceOfNumbers(a + b, r, count = 4, min = 1, max = 12),
                        explanation = done(a, "+", b, a + b),
                        hint = Visual.TenFrames(a, b),
                    )
                },
                math("m_add10_keys", txt("Pluss i hovudet", "Pluss i hodet"), txt("Skriv svaret", "Skriv svaret"), 1, "+") { ctx ->
                    val r = ctx.random
                    val a = r.between(1, 9)
                    val b = r.between(1, 10 - a)
                    Question(
                        key = "$a+$b",
                        prompt = whatIs(a, "+", b),
                        visual = equation(a, "+", b, "=", "?"),
                        answer = Answer.NumberInput(a + b),
                        explanation = done(a, "+", b, a + b),
                        hint = Visual.TenFrames(a, b),
                    )
                },
                math("m_friends10", txt("Tiarvenner", "Tiervenner"), txt("Tal som blir 10", "Tall som blir 10"), 1, "10") { ctx ->
                    val r = ctx.random
                    val a = r.between(1, 9)
                    Question(
                        key = "$a",
                        prompt = txt("Kva manglar for å få 10?", "Hva mangler for å få 10?"),
                        visual = Visual.Stack(listOf(equation(a, "+", "?", "=", 10), Visual.TenFrames(a))),
                        answer = Answer.NumberInput(10 - a),
                        speech = txt("$a pluss kva blir 10?", "$a pluss hva blir 10?"),
                        explanation = done(a, "+", 10 - a, 10),
                    )
                },
                math("m_doubles", txt("Dobling"), txt("Det same to gonger", "Det samme to ganger"), 1, "2×") { ctx ->
                    val r = ctx.random
                    val n = r.between(1, 10)
                    Question(
                        key = "$n",
                        prompt = txt("Kva er dobbelt av $n?", "Hva er dobbelt av $n?"),
                        visual = equation(n, "+", n, "=", "?"),
                        answer = choiceOfNumbers(n * 2, r, count = 4, min = 0, max = 22, likely = listOf(n * 2 - 1, n * 2 + 1, n + 1)),
                        explanation = done(n, "+", n, n * 2),
                        hint = Visual.AddGroups(r.pick(countables), n, n),
                    )
                },
                math("m_add20", txt("Pluss til 20"), txt("Utan å gå over tiaren", "Uten å gå over tieren"), 1, "+") { ctx ->
                    val r = ctx.random
                    val teen = r.between(11, 18)
                    val b = r.between(1, 9 - teen % 10)
                    val first = if (r.chance(0.3)) b else teen
                    val second = if (first == b) teen else b
                    Question(
                        key = "$first+$second",
                        prompt = whatIs(first, "+", second),
                        visual = equation(first, "+", second, "=", "?"),
                        answer = choiceOfNumbers(teen + b, r, count = 4, min = 10, max = 22),
                        explanation = done(first, "+", second, teen + b),
                        hint = Visual.TenFrames(teen, b),
                    )
                },
                math("m_add20_bridge", txt("Over tiaren", "Over tieren"), txt("Pluss til 20", "Pluss til 20"), 2, "+") { ctx ->
                    val r = ctx.random
                    val a = r.between(2, 9)
                    val b = r.between(11 - a, 9)
                    val sum = a + b
                    Question(
                        key = "$a+$b",
                        prompt = whatIs(a, "+", b),
                        visual = equation(a, "+", b, "=", "?"),
                        answer = Answer.NumberInput(sum),
                        explanation = txt(
                            "Fyll opp tiaren: $a + ${10 - a} = 10. Så 10 + ${sum - 10} = $sum.",
                            "Fyll opp tieren: $a + ${10 - a} = 10. Så 10 + ${sum - 10} = $sum.",
                        ),
                        hint = Visual.TenFrames(a, b),
                    )
                },
                math("m_add_tens", txt("Heile tiarar", "Hele tiere"), txt("30 + 40", "30 + 40"), 2, "+10") { ctx ->
                    val r = ctx.random
                    val a = r.between(1, 8)
                    val b = r.between(1, 9 - a)
                    val sum = (a + b) * 10
                    Question(
                        key = "$a+$b",
                        prompt = whatIs(a * 10, "+", b * 10),
                        visual = equation(a * 10, "+", b * 10, "=", "?"),
                        answer = choiceOfNumbers(sum, r, count = 4, min = 10, max = 100, likely = listOf(sum + 10, sum - 10, a + b)),
                        explanation = txt("$a tiarar og $b tiarar er ${a + b} tiarar: $sum.", "$a tiere og $b tiere er ${a + b} tiere: $sum."),
                        hint = Visual.Stack(listOf(Visual.BaseTen(a, 0), Visual.BaseTen(b, 0))),
                    )
                },
                math("m_add100", txt("Pluss til 100"), txt("Utan veksling", "Uten veksling"), 2, "+") { ctx ->
                    val r = ctx.random
                    val a: Int
                    val b: Int
                    if (r.nextBoolean()) {
                        val tens = r.between(1, 9)
                        val ones = r.between(0, 8)
                        a = tens * 10 + ones
                        b = r.between(1, 9 - ones)
                    } else {
                        val at = r.between(1, 8)
                        val ao = r.between(0, 8)
                        a = at * 10 + ao
                        b = r.between(1, 9 - at) * 10 + r.between(0, 9 - ao)
                    }
                    Question(
                        key = "$a+$b",
                        prompt = whatIs(a, "+", b),
                        visual = equation(a, "+", b, "=", "?"),
                        answer = Answer.NumberInput(a + b),
                        explanation = done(a, "+", b, a + b),
                        hint = Visual.Stack(listOf(Visual.BaseTen(a / 10, a % 10), Visual.BaseTen(b / 10, b % 10))),
                    )
                },
                math("m_add3", txt("Tre tal", "Tre tall"), txt("Pluss til 20", "Pluss til 20"), 2, "+++") { ctx ->
                    val r = ctx.random
                    val a = r.between(1, 6)
                    val b = r.between(1, 6)
                    val c = r.between(1, 6)
                    Question(
                        key = "$a+$b+$c",
                        prompt = txt("Kva er $a pluss $b pluss $c?", "Hva er $a pluss $b pluss $c?"),
                        visual = equation(a, "+", b, "+", c, "=", "?"),
                        answer = Answer.NumberInput(a + b + c),
                        explanation = txt("$a + $b = ${a + b}. ${a + b} + $c = ${a + b + c}."),
                    )
                },
            ),
        ),
        Chapter(
            id = "m_minus",
            subject = Subject.MATH,
            title = txt("Minus"),
            look = PlanetLook(0xFF7FD6C2, 0xFFD2F7EE, 0xFF2E8C7B, bands = true),
            skills = listOf(
                math("m_sub5", txt("Minus til 5"), txt("Med ting", "Med ting"), 0, MINUS) { ctx ->
                    val r = ctx.random
                    val total = r.between(2, 5)
                    val removed = r.between(1, total)
                    val emoji = r.pick(countables)
                    Question(
                        key = "$total-$removed",
                        prompt = whatIs(total, MINUS, removed),
                        visual = Visual.Stack(listOf(equation(total, MINUS, removed, "=", "?"), Visual.TakeAway(emoji, total, removed))),
                        answer = choiceOfNumbers(total - removed, r, count = 3, min = 0, max = 5),
                        explanation = done(total, MINUS, removed, total - removed),
                    )
                },
                math("m_sub10", txt("Minus til 10"), txt("Vel svaret", "Velg svaret"), 1, MINUS) { ctx ->
                    val r = ctx.random
                    val total = r.between(3, 10)
                    val removed = r.between(1, total)
                    Question(
                        key = "$total-$removed",
                        prompt = whatIs(total, MINUS, removed),
                        visual = equation(total, MINUS, removed, "=", "?"),
                        answer = choiceOfNumbers(total - removed, r, count = 4, min = 0, max = 10),
                        explanation = done(total, MINUS, removed, total - removed),
                        hint = Visual.TakeAway(r.pick(countables), total, removed),
                    )
                },
                math("m_sub10_keys", txt("Minus i hovudet", "Minus i hodet"), txt("Skriv svaret", "Skriv svaret"), 1, MINUS) { ctx ->
                    val r = ctx.random
                    val total = r.between(3, 10)
                    val removed = r.between(1, total)
                    Question(
                        key = "$total-$removed",
                        prompt = whatIs(total, MINUS, removed),
                        visual = equation(total, MINUS, removed, "=", "?"),
                        answer = Answer.NumberInput(total - removed),
                        explanation = done(total, MINUS, removed, total - removed),
                        hint = Visual.TakeAway(r.pick(countables), total, removed),
                    )
                },
                math("m_halves", txt("Halvering"), txt("Del i to", "Del i to"), 1, "½") { ctx ->
                    val r = ctx.random
                    val n = r.between(1, 10) * 2
                    val emoji = r.pick(countables)
                    Question(
                        key = "$n",
                        prompt = txt("Kva er halvparten av $n?", "Hva er halvparten av $n?"),
                        visual = Visual.Objects(emoji, n),
                        answer = choiceOfNumbers(n / 2, r, count = 4, min = 0, max = 20, likely = listOf(n, n / 2 + 1)),
                        explanation = txt("${n / 2} + ${n / 2} = $n"),
                        hint = Visual.Share(n, 2, emoji),
                    )
                },
                math("m_sub20", txt("Minus til 20"), txt("Utan å gå under tiaren", "Uten å gå under tieren"), 1, MINUS) { ctx ->
                    val r = ctx.random
                    val a = r.between(11, 19)
                    val b = r.between(1, a % 10)
                    Question(
                        key = "$a-$b",
                        prompt = whatIs(a, MINUS, b),
                        visual = equation(a, MINUS, b, "=", "?"),
                        answer = choiceOfNumbers(a - b, r, count = 4, min = 0, max = 20),
                        explanation = done(a, MINUS, b, a - b),
                        hint = Visual.TakeAway(r.pick(countables), a, b),
                    )
                },
                math("m_sub20_bridge", txt("Under tiaren", "Under tieren"), txt("Minus til 20", "Minus til 20"), 2, MINUS) { ctx ->
                    val r = ctx.random
                    val a = r.between(11, 18)
                    val ones = a % 10
                    val b = r.between(ones + 1, 9)
                    val result = a - b
                    Question(
                        key = "$a-$b",
                        prompt = whatIs(a, MINUS, b),
                        visual = equation(a, MINUS, b, "=", "?"),
                        answer = Answer.NumberInput(result),
                        explanation = txt(
                            "Ta først bort $ones, då er du på 10. Så 10 $MINUS ${b - ones} = $result.",
                            "Ta først bort $ones, da er du på 10. Så 10 $MINUS ${b - ones} = $result.",
                        ),
                        hint = Visual.TakeAway(r.pick(countables), a, b),
                    )
                },
                math("m_sub_tens", txt("Minus tiarar", "Minus tiere"), txt("70 $MINUS 30"), 2, "−10") { ctx ->
                    val r = ctx.random
                    val a = r.between(2, 9)
                    val b = r.between(1, a)
                    val result = (a - b) * 10
                    Question(
                        key = "$a-$b",
                        prompt = whatIs(a * 10, MINUS, b * 10),
                        visual = equation(a * 10, MINUS, b * 10, "=", "?"),
                        answer = choiceOfNumbers(result, r, count = 4, min = 0, max = 90, likely = listOf(result + 10, a - b)),
                        explanation = done(a * 10, MINUS, b * 10, result),
                        hint = Visual.BaseTen(a, 0),
                    )
                },
                math("m_sub100", txt("Minus til 100"), txt("Utan veksling", "Uten veksling"), 2, MINUS) { ctx ->
                    val r = ctx.random
                    val at = r.between(2, 9)
                    val ao = r.between(1, 9)
                    val a = at * 10 + ao
                    val b = if (r.nextBoolean()) r.between(1, ao) else r.between(1, at - 1) * 10 + r.between(0, ao)
                    Question(
                        key = "$a-$b",
                        prompt = whatIs(a, MINUS, b),
                        visual = equation(a, MINUS, b, "=", "?"),
                        answer = Answer.NumberInput(a - b),
                        explanation = done(a, MINUS, b, a - b),
                        hint = Visual.BaseTen(at, ao),
                    )
                },
            ),
        ),
        Chapter(
            id = "m_blanda",
            subject = Subject.MATH,
            title = txt("Blanda og tekst", "Blandet og tekst"),
            look = PlanetLook(0xFFC79BFF, 0xFFEBDDFF, 0xFF7446C2, ring = true),
            skills = listOf(
                math("m_mixed10", txt("Pluss og minus"), txt("Tal opp til 10", "Tall opp til 10"), 1, "±") { ctx ->
                    val r = ctx.random
                    if (r.nextBoolean()) {
                        val a = r.between(1, 9)
                        val b = r.between(1, 10 - a)
                        Question("$a+$b", whatIs(a, "+", b), equation(a, "+", b, "=", "?"), choiceOfNumbers(a + b, r, 4, 0, 12), explanation = done(a, "+", b, a + b), hint = Visual.TenFrames(a, b))
                    } else {
                        val a = r.between(2, 10)
                        val b = r.between(1, a)
                        Question("$a-$b", whatIs(a, MINUS, b), equation(a, MINUS, b, "=", "?"), choiceOfNumbers(a - b, r, 4, 0, 10), explanation = done(a, MINUS, b, a - b), hint = Visual.TakeAway(r.pick(countables), a, b))
                    }
                },
                math("m_missing10", txt("Kva manglar?", "Hva mangler?"), txt("Finn det som manglar", "Finn det som mangler"), 1, "□") { ctx ->
                    val r = ctx.random
                    when (r.nextInt(3)) {
                        0 -> {
                            val a = r.between(1, 8)
                            val b = r.between(1, 10 - a)
                            val c = a + b
                            Question("a$a+?=$c", txt("Kva tal manglar?", "Hvilket tall mangler?"), equation(a, "+", "?", "=", c), Answer.NumberInput(b),
                                speech = txt("$a pluss kva blir $c?", "$a pluss hva blir $c?"), explanation = done(a, "+", b, c), hint = Visual.TenFrames(a))
                        }
                        1 -> {
                            val a = r.between(1, 8)
                            val b = r.between(1, 10 - a)
                            val c = a + b
                            Question("b?+$b=$c", txt("Kva tal manglar?", "Hvilket tall mangler?"), equation("?", "+", b, "=", c), Answer.NumberInput(a),
                                speech = txt("Kva pluss $b blir $c?", "Hva pluss $b blir $c?"), explanation = done(a, "+", b, c))
                        }
                        else -> {
                            val a = r.between(3, 10)
                            val b = r.between(1, a - 1)
                            val c = a - b
                            Question("c$a-?=$c", txt("Kva tal manglar?", "Hvilket tall mangler?"), equation(a, MINUS, "?", "=", c), Answer.NumberInput(b),
                                speech = txt("$a minus kva blir $c?", "$a minus hva blir $c?"), explanation = done(a, MINUS, b, c))
                        }
                    }
                },
                math("m_story10", txt("Tekstoppgåver", "Tekstoppgaver"), txt("Tal opp til 10", "Tall opp til 10"), 1, "Aa", length = 6) { ctx ->
                    wordProblem(ctx.random, max = 10, allowCompare = false, keypad = false)
                },
                math("m_mixed20", txt("Blanda til 20", "Blandet til 20"), txt("Skriv svaret", "Skriv svaret"), 2, "±") { ctx ->
                    val r = ctx.random
                    if (r.nextBoolean()) {
                        val a = r.between(2, 18)
                        val b = r.between(1, 20 - a)
                        Question("$a+$b", whatIs(a, "+", b), equation(a, "+", b, "=", "?"), Answer.NumberInput(a + b), explanation = done(a, "+", b, a + b), hint = Visual.TenFrames(a, b))
                    } else {
                        val a = r.between(5, 20)
                        val b = r.between(1, a)
                        Question("$a-$b", whatIs(a, MINUS, b), equation(a, MINUS, b, "=", "?"), Answer.NumberInput(a - b), explanation = done(a, MINUS, b, a - b), hint = Visual.TakeAway(r.pick(countables), a, b))
                    }
                },
                math("m_balance", txt("Vekta", "Vekten"), txt("Like mykje på kvar side", "Like mye på hver side"), 2, "⚖") { ctx ->
                    val r = ctx.random
                    val sum = r.between(5, 18)
                    val a = r.between(1, sum - 1)
                    val b = sum - a
                    var c = r.between(1, sum - 1)
                    var guard = 0
                    while ((c == a || c == b) && guard < 20) {
                        c = r.between(1, sum - 1)
                        guard++
                    }
                    val d = sum - c
                    Question(
                        key = "$a+$b=$c+?",
                        prompt = txt("Kva tal manglar?", "Hvilket tall mangler?"),
                        visual = Visual.Balance(listOf(Token.Num(a), Token.Op("+"), Token.Num(b)), listOf(Token.Num(c), Token.Op("+"), Token.Blank)),
                        answer = Answer.NumberInput(d),
                        speech = txt(
                            "Vekta skal vere lik. $a pluss $b er det same som $c pluss kva?",
                            "Vekten skal være lik. $a pluss $b er det samme som $c pluss hva?",
                        ),
                        explanation = txt("$a + $b = $sum, og $c + $d = $sum."),
                    )
                },
                math("m_story20", txt("Tekst til 20"), txt("Skriv svaret", "Skriv svaret"), 2, "Aa", length = 6) { ctx ->
                    wordProblem(ctx.random, max = 20, allowCompare = true, keypad = true)
                },
            ),
        ),
        Chapter(
            id = "m_former",
            subject = Subject.MATH,
            title = txt("Former og mønster"),
            look = PlanetLook(0xFFFF8FB1, 0xFFFFD6E3, 0xFFC2416B, craters = true),
            skills = listOf(
                math("m_shapes", txt("Former"), txt("Finn forma", "Finn formen"), 0, "▲") { ctx ->
                    val r = ctx.random
                    val basic = listOf(ShapeKind.CIRCLE, ShapeKind.TRIANGLE, ShapeKind.SQUARE, ShapeKind.RECTANGLE, ShapeKind.STAR, ShapeKind.HEART)
                    val target = r.pick(basic)
                    val others = basic.filter { compatibleDistractor(target, it) }.shuffled(r).take(2)
                    val palette = colors(r, 3)
                    val kinds = (others + target).shuffled(r)
                    val name = shapeName(target)
                    Question(
                        key = "${target.name}-${kinds.joinToString()}",
                        prompt = txt("Trykk på ${name.definite.nn}", "Trykk på ${name.definite.nb}"),
                        visual = Visual.None,
                        answer = Answer.Choice(kinds.mapIndexed { i, kind -> Option.Shape(PatternItem(kind, palette[i])) }, kinds.indexOf(target)),
                        explanation = txt("Dette er ${name.withArticle.nn}.", "Dette er ${name.withArticle.nb}."),
                    )
                },
                math("m_patterns", txt("Mønster"), txt("Kva kjem no?", "Hva kommer nå?"), 0, "◆") { ctx ->
                    val r = ctx.random
                    val shapes = listOf(ShapeKind.CIRCLE, ShapeKind.TRIANGLE, ShapeKind.SQUARE, ShapeKind.STAR, ShapeKind.HEART, ShapeKind.DIAMOND).shuffled(r)
                    val palette = colors(r, 3)
                    val units = listOf(PatternItem(shapes[0], palette[0]), PatternItem(shapes[1], palette[1]), PatternItem(shapes[2], palette[2]))
                    val unit = when (r.nextInt(4)) {
                        0 -> listOf(units[0], units[1])
                        1 -> listOf(units[0], units[0], units[1])
                        2 -> listOf(units[0], units[1], units[1])
                        else -> listOf(units[0], units[1], units[2])
                    }
                    val length = if (unit.size == 2) r.between(5, 7) else r.between(6, 8)
                    val sequence = List(length + 1) { unit[it % unit.size] }
                    val correct = sequence.last()
                    val distractors = (units + PatternItem(shapes[3], palette[0])).distinct().filter { it != correct }.shuffled(r).take(2)
                    val options = (distractors + correct).shuffled(r)
                    Question(
                        key = sequence.joinToString { "${it.shape}${it.color}" },
                        prompt = txt("Kva kjem no?", "Hva kommer nå?"),
                        visual = Visual.Pattern(sequence.dropLast(1) + null),
                        answer = Answer.Choice(options.map { Option.Shape(it) }, options.indexOf(correct)),
                        explanation = txt("Mønsteret gjentek seg.", "Mønsteret gjentar seg."),
                    )
                },
                math("m_corners", txt("Hjørne"), txt("Tel hjørna", "Tell hjørnene"), 1, "◢") { ctx ->
                    val r = ctx.random
                    val shape = r.pick(shapeNames.filter { it.corners != null })
                    val corners = shape.corners!!
                    val values = (listOf(0, 3, 4, 5, 6).filter { it != corners }.shuffled(r).take(3) + corners).sorted()
                    Question(
                        key = shape.shape.name,
                        prompt = txt("Kor mange hjørne har forma?", "Hvor mange hjørner har formen?"),
                        visual = Visual.Shape(PatternItem(shape.shape, r.nextInt(6))),
                        answer = Answer.Choice(values.map(::numberLabel), values.indexOf(corners)),
                        explanation = if (corners == 0) {
                            txt("Ein sirkel har ingen hjørne.", "En sirkel har ingen hjørner.")
                        } else {
                            val subject = capital(shape.withArticle)
                            txt("${subject.nn} har $corners hjørne.", "${subject.nb} har $corners hjørner.")
                        },
                    )
                },
                math("m_numpatterns", txt("Talmønster", "Tallmønster"), txt("Finn regelen", "Finn regelen"), 1, "…") { ctx ->
                    val r = ctx.random
                    val step = r.pick(listOf(2, 3, 5, 10, -1, -2))
                    val start = if (step > 0) r.between(0, 20) else r.between(10, 30)
                    val gap = r.between(2, 4)
                    val row = (0 until 5).map { if (it == gap) null else start + it * step }
                    val answer = start + gap * step
                    Question(
                        key = "$step-$start-$gap",
                        prompt = txt("Kva tal manglar?", "Hvilket tall mangler?"),
                        visual = Visual.NumberRow(row),
                        answer = choiceOfNumbers(answer, r, count = 4, min = 0, max = 80, likely = listOf(answer + 1, answer - 1, answer + step)),
                        explanation = if (step > 0) txt("Talet aukar med $step kvar gong.", "Tallet øker med $step hver gang.") else txt("Talet minkar med ${-step} kvar gong.", "Tallet minker med ${-step} hver gang."),
                    )
                },
                math("m_shape_names", txt("Namn på former", "Navn på former"), txt("Kva heiter forma?", "Hva heter formen?"), 1, "●") { ctx ->
                    val r = ctx.random
                    val target = r.pick(shapeNames.filter { it.shape != ShapeKind.HEART })
                    val others = shapeNames.filter { it.shape != ShapeKind.HEART && compatibleDistractor(target.shape, it.shape) }.shuffled(r).take(2)
                    val options = (others + target).shuffled(r)
                    Question(
                        key = target.shape.name,
                        prompt = txt("Kva heiter denne forma?", "Hva heter denne formen?"),
                        visual = Visual.Shape(PatternItem(target.shape, r.nextInt(6))),
                        answer = Answer.Choice(options.map { Option.Label(it.indefinite, GlyphKind.PLAIN) }, options.indexOf(target)),
                    )
                },
            ),
        ),
        Chapter(
            id = "m_klokka",
            subject = Subject.MATH,
            title = txt("Klokka og pengar", "Klokka og penger"),
            look = PlanetLook(0xFFFFD85C, 0xFFFFF3BF, 0xFFC99A12, ring = true),
            skills = listOf(
                math("m_clock_hour", txt("Heile timar", "Hele timer"), txt("Klokka 3", "Klokka 3"), 1, "3") { ctx ->
                    val r = ctx.random
                    val hour = r.between(1, 12)
                    val others = listOf(wrap12(hour + 1), wrap12(hour - 1), wrap12(hour + 6), 12).filter { it != hour }.distinct().shuffled(r).take(2)
                    Question(
                        key = "$hour",
                        prompt = txt("Kva er klokka?", "Hva er klokka?"),
                        visual = Visual.Clock(hour, 0),
                        answer = choiceOf(hour, others, r, ::hourLabel),
                        explanation = txt(
                            "Den korte viseren peikar på $hour. Den lange peikar på 12.",
                            "Den korte viseren peker på $hour. Den lange peker på 12.",
                        ),
                    )
                },
                math("m_coins20", txt("Tel pengar", "Tell penger"), txt("Myntar opp til 20 kr", "Mynter opp til 20 kr"), 1, "kr") { ctx ->
                    val r = ctx.random
                    val items = coins(r, 20, listOf(1, 5, 10, 20), 2, 4)
                    val sum = items.sum()
                    Question(
                        key = items.joinToString("+"),
                        prompt = txt("Kor mykje pengar er det?", "Hvor mye penger er det?"),
                        visual = Visual.Money(items),
                        answer = moneyChoice(sum, r, 1, 25),
                        explanation = txt(items.joinToString(" + ") + " = $sum kr"),
                    )
                },
                math("m_clock_half", txt("Halve timar", "Halve timer"), txt("Halv fire", "Halv fire"), 2, "½") { ctx ->
                    val r = ctx.random
                    val hour = r.between(1, 12)
                    val next = wrap12(hour + 1)
                    if (r.nextBoolean()) {
                        val options = listOf(
                            Option.Label(txt("halv $next"), GlyphKind.PLAIN),
                            Option.Label(txt("halv $hour"), GlyphKind.PLAIN),
                            Option.Label(txt("klokka $next"), GlyphKind.PLAIN),
                        )
                        val order = options.indices.shuffled(r)
                        Question(
                            key = "read$hour",
                            prompt = txt("Kva er klokka?", "Hva er klokka?"),
                            visual = Visual.Clock(hour, 30),
                            answer = Answer.Choice(order.map { options[it] }, order.indexOf(0)),
                            explanation = txt(
                                "Halv $next er ein halvtime før klokka $next.",
                                "Halv $next er en halvtime før klokka $next.",
                            ),
                        )
                    } else {
                        val faces = listOf(Option.Clock(hour, 30), Option.Clock(next, 30), Option.Clock(next, 0))
                        val order = faces.indices.shuffled(r)
                        Question(
                            key = "find$hour",
                            prompt = txt("Kva klokke viser halv $next?", "Hvilken klokke viser halv $next?"),
                            visual = Visual.Glyph(txt("halv $next"), GlyphKind.PLAIN),
                            answer = Answer.Choice(order.map { faces[it] }, order.indexOf(0)),
                            explanation = txt(
                                "Halv $next er ein halvtime før klokka $next.",
                                "Halv $next er en halvtime før klokka $next.",
                            ),
                        )
                    }
                },
                math("m_coins100", txt("Pengar til 100", "Penger til 100"), txt("Myntar og setlar", "Mynter og sedler"), 2, "kr") { ctx ->
                    val r = ctx.random
                    val items = coins(r, 100, listOf(1, 5, 10, 20, 50), 2, 5)
                    val sum = items.sum()
                    Question(
                        key = items.joinToString("+"),
                        prompt = txt("Kor mykje pengar er det?", "Hvor mye penger er det?"),
                        visual = Visual.Money(items),
                        answer = Answer.NumberInput(sum),
                        explanation = txt(items.joinToString(" + ") + " = $sum kr"),
                    )
                },
                math("m_shop", txt("Butikken"), txt("Handle og få att", "Handle og få tilbake"), 2, "🛒", length = 6) { ctx ->
                    shopQuestion(ctx.random)
                },
                math("m_clock_quarter", txt("Kvart over og på"), txt("Kvarter", "Kvarter"), 3, "¼") { ctx ->
                    val r = ctx.random
                    val hour = r.between(1, 12)
                    val next = wrap12(hour + 1)
                    val over = r.nextBoolean()
                    val correct = if (over) txt("kvart over $hour") else txt("kvart på $next")
                    val wrong = if (over) txt("kvart på $hour") else txt("kvart over $next")
                    val options = listOf(correct, wrong, txt("halv $next"))
                    val order = options.indices.shuffled(r)
                    Question(
                        key = "$hour-$over",
                        prompt = txt("Kva er klokka?", "Hva er klokka?"),
                        visual = Visual.Clock(hour, if (over) 15 else 45),
                        answer = Answer.Choice(order.map { Option.Label(options[it], GlyphKind.PLAIN) }, order.indexOf(0)),
                        explanation = if (over) {
                            txt("Kvart over $hour er 15 minutt etter klokka $hour.", "Kvart over $hour er 15 minutter etter klokka $hour.")
                        } else {
                            txt("Kvart på $next er 15 minutt før klokka $next.", "Kvart på $next er 15 minutter før klokka $next.")
                        },
                    )
                },
            ),
        ),
        Chapter(
            id = "m_gonge",
            subject = Subject.MATH,
            title = txt("Gonge og dele", "Gange og dele"),
            look = PlanetLook(0xFF6FA8FF, 0xFFCFE2FF, 0xFF2F5FC2, bands = true, ring = true),
            skills = listOf(
                math("m_groups", txt("Grupper"), txt("Like store grupper", "Like store grupper"), 2, "⁂") { ctx ->
                    val r = ctx.random
                    val groups = r.between(2, 4)
                    val per = r.between(2, 5)
                    val emoji = r.pick(countables)
                    val repeated = (List(groups) { per }).flatMap { listOf<Any>(it, "+") }.dropLast(1)
                    Question(
                        key = "$groups×$per",
                        prompt = txt("Kor mange er det til saman?", "Hvor mange er det til sammen?"),
                        visual = Visual.Stack(listOf(Visual.Groups(groups, per, emoji), equation(*(repeated + listOf("=", "?")).toTypedArray()))),
                        answer = choiceOfNumbers(groups * per, r, count = 4, min = 2, max = 30, likely = listOf(groups + per, groups * per + per)),
                        speech = txt("$groups grupper med $per i kvar. Kor mange er det til saman?", "$groups grupper med $per i hver. Hvor mange er det til sammen?"),
                        explanation = txt("$groups $TIMES $per = ${groups * per}"),
                    )
                },
                math("m_times_2_5_10", txt("Gonge 2, 5, 10", "Gange 2, 5, 10"), txt("Vel svaret", "Velg svaret"), 2, TIMES) { ctx ->
                    val r = ctx.random
                    val table = r.pick(listOf(2, 5, 10))
                    val other = r.between(1, 10)
                    val (a, b) = if (r.nextBoolean()) table to other else other to table
                    Question(
                        key = "$a×$b",
                        prompt = whatIs(a, TIMES, b),
                        visual = equation(a, TIMES, b, "=", "?"),
                        answer = choiceOfNumbers(a * b, r, count = 4, min = 0, max = 100, likely = listOf(a * b + table, a * b - table, a + b)),
                        explanation = done(a, TIMES, b, a * b),
                        hint = if (a * b <= 30) Visual.Groups(b, a, r.pick(countables)) else null,
                    )
                },
                math("m_share", txt("Dele likt"), txt("Del på fleire", "Del på flere"), 2, DIVIDE) { ctx ->
                    val r = ctx.random
                    val plates = r.between(2, 5)
                    val per = r.between(1, 5)
                    val total = plates * per
                    val emoji = r.pick(countables)
                    Question(
                        key = "$total:$plates",
                        prompt = txt("Del likt på $plates. Kor mange får kvar?", "Del likt på $plates. Hvor mange får hver?"),
                        visual = Visual.Stack(listOf(Visual.Objects(emoji, total), equation(total, DIVIDE, plates, "=", "?"))),
                        answer = choiceOfNumbers(per, r, count = 4, min = 1, max = 12, likely = listOf(per + 1, plates)),
                        explanation = done(total, DIVIDE, plates, per),
                        hint = Visual.Share(total, plates, emoji),
                    )
                },
                math("m_times_1_5", txt("Gongetabellen 1–5", "Gangetabellen 1–5"), txt("Skriv svaret", "Skriv svaret"), 3, TIMES) { ctx ->
                    val r = ctx.random
                    val a = r.between(1, 5)
                    val b = r.between(1, 10)
                    Question("$a×$b", whatIs(a, TIMES, b), equation(a, TIMES, b, "=", "?"), Answer.NumberInput(a * b), explanation = done(a, TIMES, b, a * b))
                },
                math("m_times_all", txt("Heile gongetabellen", "Hele gangetabellen"), txt("1 til 10"), 3, TIMES) { ctx ->
                    val r = ctx.random
                    val a = r.between(2, 10)
                    val b = r.between(2, 10)
                    Question("$a×$b", whatIs(a, TIMES, b), equation(a, TIMES, b, "=", "?"), Answer.NumberInput(a * b), explanation = done(a, TIMES, b, a * b))
                },
            ),
        ),
    )
}

/** Questions for «Rakettløpet»: fast, three options, no keypad. */
enum class RaceMode(val title: Txt, val symbol: String) {
    ADD10(txt("Pluss til 10"), "+"),
    SUB10(txt("Minus til 10"), MINUS),
    MIXED20(txt("Pluss og minus til 20"), "±"),
    MULTIPLY(txt("Gonge med 2, 5 og 10", "Gange med 2, 5 og 10"), TIMES);

    fun question(r: Random): Question {
        val (a, op, b) = when (this) {
            ADD10 -> r.between(0, 10).let { a -> Triple(a, "+", r.between(0, 10 - a)) }
            SUB10 -> r.between(1, 10).let { a -> Triple(a, MINUS, r.between(0, a)) }
            MIXED20 -> if (r.nextBoolean()) {
                r.between(1, 19).let { a -> Triple(a, "+", r.between(1, 20 - a)) }
            } else {
                r.between(2, 20).let { a -> Triple(a, MINUS, r.between(1, a)) }
            }
            MULTIPLY -> r.pick(listOf(2, 5, 10)).let { t -> if (r.nextBoolean()) Triple(t, TIMES, r.between(1, 10)) else Triple(r.between(1, 10), TIMES, t) }
        }
        val result = when (op) {
            "+" -> a + b
            MINUS -> a - b
            else -> a * b
        }
        return Question(
            key = "$a$op$b",
            prompt = whatIs(a, op, b),
            visual = equation(a, op, b, "=", "?"),
            answer = choiceOfNumbers(result, r, count = 3, min = 0, max = if (this == MULTIPLY) 100 else 20),
        )
    }
}
