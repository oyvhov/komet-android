package app.komet.domain

import kotlin.random.Random

enum class Subject { MATH, READING }

/** How a piece of text in a task is drawn. Reading kinds follow the profile's [LetterCase]. */
enum class GlyphKind {
    /** Digits and signs. Never changes case. */
    NUMBER,
    /** A single letter in reading tasks. */
    LETTER,
    /** A word in reading tasks. */
    WORD,
    /** A whole sentence: initial capital in lower-case mode. */
    SENTENCE,
    /** A word inside a sentence (word-order tiles): only changes in upper-case mode. */
    SENTENCE_WORD,
    /** Shown exactly as written, used when the case itself is the question. */
    EXACT,
    /** Ordinary interface text such as «halv 4» or «Partal». */
    PLAIN,
}

enum class ShapeKind { CIRCLE, TRIANGLE, SQUARE, RECTANGLE, PENTAGON, HEXAGON, STAR, HEART, DIAMOND }

/** A coloured shape. [color] indexes the six-colour shape palette in the UI. */
data class PatternItem(val shape: ShapeKind, val color: Int)

sealed interface Token {
    data class Num(val value: Int) : Token
    data class Op(val symbol: String) : Token
    data object Blank : Token
}

/** What the task shows. Every visual is drawn by the app itself; there are no image assets. */
sealed interface Visual {
    data object None : Visual
    data class Equation(val tokens: List<Token>) : Visual
    data class Objects(val emoji: String, val count: Int, val scattered: Boolean = false) : Visual
    data class AddGroups(val emoji: String, val a: Int, val b: Int) : Visual
    data class TakeAway(val emoji: String, val total: Int, val removed: Int) : Visual
    data class TenFrames(val first: Int, val second: Int = 0) : Visual
    data class BaseTen(val tens: Int, val ones: Int) : Visual
    data class Clock(val hour: Int, val minute: Int) : Visual
    data class Money(val items: List<Int>) : Visual
    data class NumberRow(val items: List<Int?>) : Visual
    data class Pattern(val items: List<PatternItem?>) : Visual
    data class Shape(val item: PatternItem) : Visual
    data class Glyph(val text: Txt, val kind: GlyphKind) : Visual
    /**
     * A picture with an optional caption. [hideIndex] replaces one letter of the caption with a gap.
     * With [revealWord] the caption stays hidden until the task is answered.
     */
    data class Picture(
        val emoji: String,
        val word: String? = null,
        val hideIndex: Int? = null,
        val revealWord: Boolean = false,
    ) : Visual
    data class Groups(val groups: Int, val perGroup: Int, val emoji: String) : Visual
    data class Share(val total: Int, val plates: Int, val emoji: String) : Visual
    data class Balance(val left: List<Token>, val right: List<Token>) : Visual
    /** A short text. [reading] texts follow the profile's letter case; maths word problems do not. */
    data class Story(val text: Txt, val reading: Boolean = true) : Visual
    /** A large listen button: the task is to find what is heard. [fallback] is shown when speech is off. */
    data class Listen(val spoken: Txt, val fallback: Txt, val kind: GlyphKind) : Visual
    data class Stack(val items: List<Visual>) : Visual
    data class Compare(val a: Int, val b: Int) : Visual
}

sealed interface Option {
    data class Label(val text: Txt, val kind: GlyphKind) : Option
    data class Picture(val emoji: String, val caption: String? = null) : Option
    data class Shape(val item: PatternItem) : Option
    data class Clock(val hour: Int, val minute: Int) : Option
    data class Verdict(val truth: Boolean) : Option
    data class Claps(val count: Int) : Option
}

sealed interface Answer {
    data class Choice(val options: List<Option>, val correct: Int) : Answer {
        init {
            require(correct in options.indices) { "correct index outside options" }
        }
    }

    data class NumberInput(val correct: Int) : Answer

    /**
     * Tap tiles in order. [target] holds canonical values (letters upper case, words as written);
     * [tiles] contains every target value plus distractors. Equal tiles are interchangeable.
     */
    data class Build(val target: List<String>, val tiles: List<String>, val kind: GlyphKind) : Answer
}

data class Question(
    /** Identity inside a round, used to avoid asking the same thing twice. */
    val key: String,
    val prompt: Txt,
    val visual: Visual,
    val answer: Answer,
    /** What is read aloud when the task appears. Reading tasks never say the word the child should read. */
    val speech: Txt = prompt,
    /** Read aloud once the answer is known, e.g. the word that was just decoded. */
    val reward: Txt? = null,
    /** Shown when the answer is revealed after two misses. */
    val explanation: Txt? = null,
    /** Optional support that the lightbulb reveals. */
    val hint: Visual? = null,
)

class QuestionContext(
    val random: Random,
    val maalform: Maalform = Maalform.NYNORSK,
    val letterCase: LetterCase = LetterCase.UPPER,
)

data class PlanetLook(
    val base: Long,
    val light: Long,
    val dark: Long,
    val ring: Boolean = false,
    val bands: Boolean = false,
    val craters: Boolean = false,
)

class Skill(
    val id: String,
    val subject: Subject,
    val title: Txt,
    val detail: Txt,
    /** 0 = førskule, 1 = 1. klasse, 2 = 2. klasse, 3 = 3. klasse. */
    val grade: Int,
    /** Short label drawn on the map node: a number, a sign or a letter. */
    val symbol: String,
    val length: Int = 8,
    val generate: (QuestionContext) -> Question,
)

class Chapter(
    val id: String,
    val subject: Subject,
    val title: Txt,
    val look: PlanetLook,
    val skills: List<Skill>,
)
