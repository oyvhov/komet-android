package app.komet.domain

enum class AtlasCountry(val title: Txt, val capital: String, val description: Txt) {
    NO(Txt("Noreg", "Norge"), "Oslo", Txt("Raudt med blå kross i kvitt", "Rødt med blått kors i hvitt")),
    SE(Txt("Sverige"), "Stockholm", Txt("Blått med gul kross", "Blått med gult kors")),
    DK(Txt("Danmark"), "København", Txt("Raudt med kvit kross", "Rødt med hvitt kors")),
    FI(Txt("Finland"), "Helsinki", Txt("Kvitt med blå kross", "Hvitt med blått kors")),
    IS(Txt("Island"), "Reykjavík", Txt("Blått med raud kross i kvitt", "Blått med rødt kors i hvitt")),
    JP(Txt("Japan"), "Tokyo", Txt("Kvit botn med raud sirkel", "Hvit bunn med rød sirkel")),
    IT(Txt("Italia"), "Roma", Txt("Grønt, kvitt og raudt frå venstre", "Grønt, hvitt og rødt fra venstre")),
    FR(Txt("Frankrike"), "Paris", Txt("Blått, kvitt og raudt frå venstre", "Blått, hvitt og rødt fra venstre")),
    NG(Txt("Nigeria"), "Abuja", Txt("Grønt på begge sider av ei kvit stripe", "Grønt på begge sider av en hvit stripe")),
    PE(Txt("Peru"), "Lima", Txt("Raudt på begge sider av ei kvit stripe", "Rødt på begge sider av en hvit stripe")),
    PW(Txt("Palau"), "Ngerulmud", Txt("Lyseblått med gul sirkel")),
    BS(Txt("Bahamas"), "Nassau", Txt("Svart trekant og striper i turkis og gult")),
}

data class AtlasPlace(val name: Txt, val longitude: Int, val latitude: Int, val clue: Txt)

internal val atlasContinents = listOf(
    AtlasPlace(Txt("Europa"), 13, 51, Txt("Europa ligg nord for Afrika.", "Europa ligger nord for Afrika.")),
    AtlasPlace(Txt("Afrika"), 21, 4, Txt("Ekvator går gjennom Afrika.")),
    AtlasPlace(Txt("Asia"), 94, 48, Txt("Asia ligg aust for Europa.", "Asia ligger øst for Europa.")),
    AtlasPlace(Txt("Nord-Amerika"), -101, 47, Txt("Nord-Amerika har Stillehavet i vest og Atlanterhavet i aust.", "Nord-Amerika har Stillehavet i vest og Atlanterhavet i øst.")),
    AtlasPlace(Txt("Sør-Amerika"), -55, -12, Txt("Sør-Amerika ligg sør for Mellom-Amerika.", "Sør-Amerika ligger sør for Mellom-Amerika.")),
    AtlasPlace(Txt("Oseania"), 134, -25, Txt("Australia er ein del av Oseania.", "Australia er en del av Oseania.")),
    AtlasPlace(Txt("Antarktis"), 32, -78, Txt("Antarktis ligg rundt Sørpolen.", "Antarktis ligger rundt Sydpolen.")),
)
internal val atlasOceans = listOf(
    AtlasPlace(Txt("Stillehavet"), -139, 3, Txt("Stillehavet ligg mellom Amerika og Asia.", "Stillehavet ligger mellom Amerika og Asia.")),
    AtlasPlace(Txt("Atlanterhavet"), -36, 22, Txt("Atlanterhavet ligg mellom Amerika og Europa og Afrika.", "Atlanterhavet ligger mellom Amerika og Europa og Afrika.")),
    AtlasPlace(Txt("Indiahavet"), 77, -22, Txt("Indiahavet ligg sør for India.", "Indiahavet ligger sør for India.")),
    AtlasPlace(Txt("Nordishavet"), 72, 83, Txt("Nordishavet ligg rundt Nordpolen.", "Nordishavet ligger rundt Nordpolen.")),
    AtlasPlace(Txt("Sørishavet"), 68, -65, Txt("Sørishavet går rundt Antarktis.")),
)

/** The paper chart is north-up, A–D from west to east and 1–4 from north to south. */
data class ChartCell(val column: Int, val row: Int) {
    val address get() = "${'A' + column}${row + 1}"
}

object GeographyCurriculum {
    private val nordic = listOf(AtlasCountry.NO, AtlasCountry.SE, AtlasCountry.DK, AtlasCountry.FI, AtlasCountry.IS)
    private val countries = AtlasCountry.entries.toList()
    private val cells = List(16) { ChartCell(it % 4, it / 4) }
    private fun level(id: String, name: Txt, detail: Txt, grade: Int, symbol: String, length: Int = 6, make: (QuestionContext) -> Question) =
        Skill("g_$id", Subject.SPACE, name, detail, grade, symbol, length, make)

    private fun labels(ctx: QuestionContext, target: Txt, pool: List<Txt>) =
        choiceOf(target, pool.filter { it != target }.distinct().shuffled(ctx.random).take(2), ctx.random) { Option.Label(it, GlyphKind.PLAIN) }

    private fun flags(ctx: QuestionContext, pool: List<AtlasCountry>, match: Boolean = false, capital: Boolean = false): Question {
        val target = ctx.random.pick(pool)
        val prompt = when {
            match -> Txt("Finn det same flagget.", "Finn det samme flagget.")
            capital -> Txt("Kva er hovudstaden i ${target.title.nn}?", "Hva er hovedstaden i ${target.title.nb}?")
            else -> Txt("Kva land har dette flagget?", "Hvilket land har dette flagget?")
        }
        val why = if (capital) Txt("${target.capital} er hovudstaden i ${target.title.nn}.", "${target.capital} er hovedstaden i ${target.title.nb}.")
            else Txt("${target.title.nn}: ${target.description.nn}.", "${target.title.nb}: ${target.description.nb}.")
        val answer = when {
            match -> choiceOf(target, pool.filter { it != target }.shuffled(ctx.random).take(2), ctx.random) { Option.Flag(it) }
            capital -> labels(ctx, Txt(target.capital), pool.map { Txt(it.capital) })
            else -> labels(ctx, target.title, pool.map { it.title })
        }
        return Question(target.name, prompt, Visual.Flag(target), answer, explanation = why, reward = why)
    }

    private fun world(ctx: QuestionContext, pool: List<AtlasPlace>, landOrSea: Boolean = false): Question {
        val place = ctx.random.pick(pool)
        val land = place in atlasContinents
        val prompt = when {
            landOrSea -> Txt("Ligg prikken på land eller i havet?", "Ligger prikken på land eller i havet?")
            land -> Txt("Kva verdsdel er merkt?", "Hvilken verdensdel er merket?")
            else -> Txt("Kva verdshav er merkt?", "Hvilket verdenshav er merket?")
        }
        val landLabel = Txt("På land")
        val seaLabel = Txt("I havet")
        val correct = if (landOrSea) { if (land) landLabel else seaLabel } else place.name
        val why = if (landOrSea) Txt("${place.name.nn} er ${if (land) "ein verdsdel" else "eit hav"}.", "${place.name.nb} er ${if (land) "en verdensdel" else "et hav"}.") else place.clue
        return Question(place.name.nn, prompt, Visual.WorldPin(place), labels(ctx, correct,
            if (landOrSea) listOf(landLabel, seaLabel) else pool.map { it.name }), explanation = why, reward = why)
    }

    private enum class ChartTask { DIRECTION, ADDRESS, DISTANCE }
    private fun dock(ctx: QuestionContext): Question {
        // Separated berths leave room for a finger on narrow screens.
        val berths = mutableListOf<ChartCell>()
        for (cell in cells.shuffled(ctx.random)) {
            if (berths.none { kotlin.math.abs(it.column - cell.column) < 2 && kotlin.math.abs(it.row - cell.row) < 2 }) berths += cell
            if (berths.size == 3) break
        }
        val destination = ctx.random.pick(berths)
        val boat = ctx.random.pick(cells - berths.toSet())
        val why = Txt("Kaien ligg i kolonne ${'A' + destination.column}, på rad ${destination.row + 1}.",
            "Kaien ligger i kolonne ${'A' + destination.column}, på rad ${destination.row + 1}.")
        return Question("dock-${boat.address}-${destination.address}", Txt("Segl til ${destination.address}. Trykk på kaien.", "Seil til ${destination.address}. Trykk på kaien."),
            Visual.SeaChart(boat, destination, null, berths), Answer.Choice(berths.map { Option.Label(Txt(it.address), GlyphKind.PLAIN) }, berths.indexOf(destination)),
            explanation = why)
    }
    private fun chart(ctx: QuestionContext, task: ChartTask): Question {
        val boat = ctx.random.pick(cells)
        val tower = ctx.random.pick(cells.filter { (it.column == boat.column) xor (it.row == boat.row) })
        val scale = if (task == ChartTask.DISTANCE) ctx.random.pick(listOf(2, 5, 10)) else null
        val map = Visual.SeaChart(boat, tower, scale)
        val key = "${boat.address}-${tower.address}-${scale ?: 0}"
        return when (task) {
            ChartTask.DIRECTION -> {
                val names = listOf(Txt("Nord"), Txt("Aust", "Øst"), Txt("Sør"), Txt("Vest"))
                val index = when { tower.row < boat.row -> 0; tower.column > boat.column -> 1; tower.row > boat.row -> 2; else -> 3 }
                val instruction = listOf(Txt("oppover"), Txt("til høgre", "til høyre"), Txt("nedover"), Txt("til venstre"))[index]
                val why = Txt("Nord er opp. Frå båten går du ${instruction.nn} på kartet, mot ${names[index].nn.lowerNo()}.",
                    "Nord er opp. Fra båten går du ${instruction.nb} på kartet, mot ${names[index].nb.lowerNo()}.")
                Question(key, Txt("Kva retning er det frå båten til fyret?", "Hvilken retning er det fra båten til fyret?"),
                    map, labels(ctx, names[index], names), explanation = why)
            }
            ChartTask.ADDRESS -> {
                val why = Txt("Følg kolonne ${'A' + tower.column} og rad ${tower.row + 1}. Dei møtest i ${tower.address}.",
                    "Følg kolonne ${'A' + tower.column} og rad ${tower.row + 1}. De møtes i ${tower.address}.")
                Question(key, Txt("I kva rute står fyret?", "I hvilken rute står fyret?"), map,
                    labels(ctx, Txt(tower.address), cells.map { Txt(it.address) }), explanation = why)
            }
            ChartTask.DISTANCE -> {
                val steps = kotlin.math.abs(tower.column - boat.column) + kotlin.math.abs(tower.row - boat.row)
                val km = steps * requireNotNull(scale)
                val why = Txt("Frå båten til fyret er det $steps rutesteg på $scale km. $steps × $scale = $km km.",
                    "Fra båten til fyret er det $steps rutesteg på $scale km. $steps × $scale = $km km.")
                Question(key, Txt("Kor mange kilometer er det frå båten til fyret?", "Hvor mange kilometer er det fra båten til fyret?"), map,
                    labels(ctx, Txt("$km km"), (1..4).map { Txt("${it * scale} km") }), explanation = why)
            }
        }
    }

    val chapter = Chapter("g_jordatlas", Subject.SPACE, Txt("Geografi"), PlanetLook(0xFF3F9F94, 0xFFB9E1CC, 0xFF225E72), listOf(
        level("flag_pair", Txt("Flaggpar"), Txt("Finn to like flagg"), 0, "⚑") { flags(it, countries, match = true) },
        level("land_sea", Txt("Land eller hav?"), Txt("Sjå på verdskartet", "Se på verdenskartet"), 0, "≈") { world(it, atlasContinents + atlasOceans, landOrSea = true) },
        level("nordic", Txt("Flagg i Norden"), Txt("Fem land i Norden"), 1, "⚑", 5) { flags(it, nordic) },
        level("compass", Txt("Med kompass"), Txt("Finn kursen til fyret"), 1, "N") { chart(it, ChartTask.DIRECTION) },
        level("flags", Txt("Flagg frå verda", "Flagg fra verden"), Txt("Kjenn att tolv flagg", "Kjenn igjen tolv flagg"), 2, "⚑") { flags(it, countries) },
        level("grid", Txt("Rutefinnaren", "Rutefinneren"), Txt("Finn fyret og legg til kai"), 2, "B2") {
            if (it.random.nextBoolean()) dock(it) else chart(it, ChartTask.ADDRESS)
        },
        level("capitals", Txt("Byreisa i Norden", "Byreisen i Norden"), Txt("Finn hovudstaden", "Finn hovedstaden"), 2, "⌂", 5) { flags(it, nordic, capital = true) },
        level("continents", Txt("Dei sju verdsdelane", "De sju verdensdelene"), Txt("Les verdskartet", "Les verdenskartet"), 3, "◎", 7) { world(it, atlasContinents) },
        level("oceans", Txt("Over fem hav"), Txt("Finn verdshava", "Finn verdenshavene"), 3, "≈", 5) { world(it, atlasOceans) },
        level("scale", Txt("Mål sjøreisa", "Mål sjøreisen"), Txt("Bruk målestokken"), 3, "km") { chart(it, ChartTask.DISTANCE) },
        level("world_capitals", Txt("Byar i verda", "Byer i verden"), Txt("Tolv land og hovudstader", "Tolv land og hovedsteder"), 3, "⌂") { flags(it, countries, capital = true) },
    ))
}
