package app.komet.domain

/**
 * Kinds of tasks a child can look for across grades and planets: «I like plus» should find every
 * plus level, from 1 + 2 to 64 + 25.
 */
enum class Topic(val subject: Subject, val title: Txt, val icon: String) {
    COUNTING(Subject.MATH, txt("Tal og telling", "Tall og telling"), "123"),
    PLUS(Subject.MATH, txt("Pluss"), "+"),
    MINUS(Subject.MATH, txt("Minus"), "−"),
    MIXED(Subject.MATH, txt("Pluss og minus"), "±"),
    WORD_PROBLEMS(Subject.MATH, txt("Tekstoppgåver", "Tekstoppgaver"), "📝"),
    SHAPES(Subject.MATH, txt("Former og mønster"), "🔷"),
    CLOCK(Subject.MATH, txt("Klokka"), "🕒"),
    MONEY(Subject.MATH, txt("Pengar", "Penger"), "🪙"),
    TIMES(Subject.MATH, txt("Gonge og dele", "Gange og dele"), "×"),
    WRITE_DIGITS(Subject.MATH, txt("Skriv tal", "Skriv tall"), "✏️"),
    LETTERS(Subject.READING, txt("Bokstavar", "Bokstaver"), "Aa"),
    SOUNDS(Subject.READING, txt("Lydar", "Lyder"), "👂"),
    RHYME(Subject.READING, txt("Rim og stavingar", "Rim og stavelser"), "🎵"),
    WORDS(Subject.READING, txt("Les ord"), "📖"),
    SPELLING(Subject.READING, txt("Byggje ord", "Bygge ord"), "🧩"),
    SENTENCES(Subject.READING, txt("Setningar og tekstar", "Setninger og tekster"), "📜"),
    WRITE_LETTERS(Subject.READING, txt("Skriv bokstavar", "Skriv bokstaver"), "✍️"),
    EN_PHRASES(Subject.ENGLISH, txt("Snakk engelsk"), "💬"),
    EN_COLOURS_NUMBERS(Subject.ENGLISH, txt("Fargar og tal", "Farger og tall"), "🎨"),
    EN_WORDS(Subject.ENGLISH, txt("Engelske ord"), "🐶"),
    EN_SPELL(Subject.ENGLISH, txt("Stav på engelsk"), "ABC"),
}

object Topics {

    private val bySkill: Map<String, Topic> = buildMap {
        fun put(topic: Topic, vararg ids: String) = ids.forEach { put(it, topic) }
        put(Topic.COUNTING, "m_count5", "m_count10", "m_next10", "m_compare10", "m_count20", "m_findnumber", "m_tensones", "m_skip", "m_compare100", "m_evenodd")
        put(Topic.PLUS, "m_add5", "m_add10", "m_add10_keys", "m_friends10", "m_doubles", "m_add20", "m_add20_bridge", "m_add_tens", "m_add100", "m_add3")
        put(Topic.MINUS, "m_sub5", "m_sub10", "m_sub10_keys", "m_halves", "m_sub20", "m_sub20_bridge", "m_sub_tens", "m_sub100")
        put(Topic.MIXED, "m_mixed10", "m_missing10", "m_mixed20", "m_balance")
        put(Topic.WORD_PROBLEMS, "m_story10", "m_story20")
        put(Topic.SHAPES, "m_shapes", "m_patterns", "m_corners", "m_numpatterns", "m_shape_names")
        put(Topic.CLOCK, "m_clock_hour", "m_clock_half", "m_clock_quarter")
        put(Topic.MONEY, "m_coins20", "m_coins100", "m_shop")
        put(Topic.TIMES, "m_groups", "m_times_2_5_10", "m_share", "m_times_1_5", "m_times_all")
        put(Topic.WRITE_DIGITS, "m_write1", "m_write2")
        put(Topic.LETTERS, "r_letters1", "r_letters2", "r_letters3", "r_case", "r_vowels", "r_letters4", "r_abc", "r_letters5")
        put(Topic.SOUNDS, "r_first1", "r_first2", "r_last", "r_missing")
        put(Topic.RHYME, "r_syllables", "r_rhyme")
        put(Topic.WORDS, "r_word_pic", "r_pic_word", "r_sight", "r_compound")
        put(Topic.SPELLING, "r_build3", "r_build4", "r_build5")
        put(Topic.SENTENCES, "r_sentence_pic", "r_truefalse", "r_missing_word", "r_word_order", "r_story")
        put(Topic.WRITE_LETTERS, "r_write1", "r_write2", "r_write3", "r_write4", "r_write5")
        put(Topic.EN_PHRASES, "e_hello1", "e_hello2", "e_hello3")
        put(Topic.EN_COLOURS_NUMBERS, "e_colours_listen", "e_colours_read", "e_numbers5", "e_numbers10", "e_numbers_read")
        put(Topic.EN_WORDS, "e_animals_listen", "e_animals_read", "e_animals_translate", "e_food_listen", "e_food_read", "e_body_listen", "e_family_listen", "e_clothes_listen", "e_me_read", "e_things_listen", "e_things_translate")
        put(Topic.EN_SPELL, "e_spell3", "e_spell5")
    }

    fun of(skill: Skill): Topic? = bySkill[skill.id]

    /** The topics that have levels, in the order they are shown. */
    val all: List<Topic> get() = Topic.entries.filter { topic -> Curriculum.skills.any { bySkill[it.id] == topic } }

    /** Every level of a topic, easiest first, whatever the child's grade. */
    fun skills(topic: Topic): List<Skill> {
        val order = Curriculum.skills.withIndex().associate { it.value.id to it.index }
        return Curriculum.skills.filter { bySkill[it.id] == topic }.sortedWith(compareBy({ it.grade }, { order[it.id] }))
    }
}
