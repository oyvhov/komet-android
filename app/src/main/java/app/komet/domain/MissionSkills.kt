package app.komet.domain

/** Replayable story missions. All information needed to solve a task is also spoken aloud. */
object MissionCurriculum {
    private fun mission(id: String, title: Txt, grade: Int, length: Int = 6, generate: (QuestionContext) -> Question) =
        Skill(id, Subject.MATH, title, txt("Hjelp Bolt med eit romoppdrag", "Hjelp Bolt med et romoppdrag"), grade, "🚀", length, generate)

    private fun task(ctx: QuestionContext, key: String, story: Txt, question: Txt, result: Int,
                     explanation: Txt, hint: Visual, max: Int = 99, choices: Int = 4, showSupport: Boolean = false): Question = Question(
        key = key, prompt = question, visual = if (showSupport) Visual.Stack(listOf(Visual.Story(story, reading = false), hint)) else Visual.Story(story, reading = false),
        answer = choiceOfNumbers(result, ctx.random, count = choices, min = 0, max = max),
        speech = txt("${story.nn} ${question.nn}", "${story.nb} ${question.nb}"),
        explanation = explanation, hint = if (showSupport) null else hint,
    )

    private fun equation(a: Int, op: String, b: Int) = Visual.Equation(
        listOf(Token.Num(a), Token.Op(op), Token.Num(b), Token.Op("="), Token.Blank))

    val chapters = listOf(Chapter(
        "m_missions", Subject.MATH, txt("Bolt sine romoppdrag", "Bolts romoppdrag"),
        PlanetLook(0xFF4765AD, 0xFF91C9FF, 0xFF17264D, ring = true),
        listOf(
            mission("m_mission_collect", txt("Samle krystallar", "Samle krystaller"), 0, length = 5) { ctx ->
                val a = ctx.random.between(1, 4); val b = ctx.random.between(1, 5 - a)
                task(ctx, "$a+$b", txt("Bolt finn $a ${if (a == 1) "krystall" else "krystallar"}. Du finn $b til.", "Bolt finner $a ${if (a == 1) "krystall" else "krystaller"}. Du finner $b til."),
                    txt("Kor mange har de til saman?", "Hvor mange har dere til sammen?"), a + b,
                    txt("Tel begge gruppene: $a + $b = ${a + b}.", "Tell begge gruppene: $a + $b = ${a + b}."), Visual.AddGroups("💎", a, b), max = 5, choices = 3, showSupport = true)
            },
            mission("m_mission_lights", txt("Lys i basen"), 1) { ctx ->
                val total = ctx.random.between(6, 10); val on = ctx.random.between(1, total - 1)
                task(ctx, "$total-$on", txt("Basen har $total lamper. $on lyser allereie.", "Basen har $total lamper. $on lyser allerede."),
                    txt("Kor mange må du tenne?", "Hvor mange må du tenne?"), total - on,
                    txt("Trekk frå lampene som lyser: $total − $on = ${total - on}.", "Trekk fra lampene som lyser: $total − $on = ${total - on}."), Visual.TakeAway("💡", total, on), max = 10)
            },
            mission("m_mission_fuel10", txt("Ti energikuler"), 1) { ctx ->
                val have = ctx.random.between(1, 9)
                task(ctx, "$have", txt("Raketten treng 10 energikuler. Du har $have.", "Raketten trenger 10 energikuler. Du har $have."),
                    txt("Kor mange manglar?", "Hvor mange mangler?"), 10 - have,
                    txt("Fyll opp til 10: $have + ${10 - have} = 10."), Visual.TenFrames(have), max = 10)
            },
            mission("m_mission_route", txt("Finn landingsplassen"), 1) { ctx ->
                val start = ctx.random.between(0, 12); val step = 2
                val row = List(4) { start + it * step }
                task(ctx, "$start:$step", txt("Landingslysa viser ${row.joinToString(", ")}. Raketten skal til neste tal.", "Landingslysene viser ${row.joinToString(", ")}. Raketten skal til neste tall."),
                    txt("Kva tal kjem no?", "Hvilket tall kommer nå?"), start + 4 * step,
                    txt("Legg til $step kvar gong: ${row.last()} + $step = ${start + 4 * step}.", "Legg til $step hver gang: ${row.last()} + $step = ${start + 4 * step}."), Visual.NumberRow(row + listOf(null)), max = 20)
            },
            mission("m_mission_fuel", txt("Fyll raketten"), 2) { ctx ->
                val have = ctx.random.between(1, 19)
                task(ctx, "$have", txt("Raketten treng 20 energikuler. Du har $have.", "Raketten trenger 20 energikuler. Du har $have."),
                    txt("Kor mange manglar?", "Hvor mange mangler?"), 20 - have,
                    txt("Fyll opp til 20: $have + ${20 - have} = 20."), Visual.TenFrames(have), max = 20)
            },
            mission("m_mission_cargo", txt("Last romskipet"), 2) { ctx ->
                val a = ctx.random.between(5, 12); val b = ctx.random.between(2, 8); val used = ctx.random.between(1, a)
                task(ctx, "$a:$b:$used", txt("Skipet har $a kasser. Du lastar $b til. Så leverer du $used på månen.", "Skipet har $a kasser. Du laster $b til. Så leverer du $used på månen."),
                    txt("Kor mange kasser er att?", "Hvor mange kasser er igjen?"), a + b - used,
                    txt("Først $a + $b = ${a + b}. Så ${a + b} − $used = ${a + b - used}."),
                    Visual.Story(txt("Finn først kor mange kasser skipet har før levering.", "Finn først hvor mange kasser skipet har før levering."), false), max = 20)
            },
            mission("m_mission_robots", txt("Reparer robotane", "Reparer robotene"), 2) { ctx ->
                val robots = ctx.random.between(2, 6); val screws = ctx.random.between(2, 5)
                task(ctx, "$robots:$screws", txt("$robots robotar treng $screws skruar kvar.", "$robots roboter trenger $screws skruer hver."),
                    txt("Kor mange skruar treng du?", "Hvor mange skruer trenger du?"), robots * screws,
                    txt("$robots like grupper: $robots × $screws = ${robots * screws}."), Visual.Groups(robots, screws, "🔩"), max = 30)
            },
            mission("m_mission_picnic", txt("Rom-piknik"), 2) { ctx ->
                val people = ctx.random.between(2, 5); val each = ctx.random.between(2, 6); val total = people * each
                task(ctx, "$total:$people", txt("$people astronautar deler $total jordbær likt.", "$people astronauter deler $total jordbær likt."),
                    txt("Kor mange får kvar?", "Hvor mange får hver?"), each,
                    txt("$total : $people = $each. Alle får like mange."), Visual.Share(total, people, "🍓"), max = total)
            },
            mission("m_mission_measure", txt("Bygg ei bru", "Bygg en bro"), 2) { ctx ->
                val length = ctx.random.between(10, 40); val extra = ctx.random.between(2, 15)
                task(ctx, "$length:$extra", txt("Brua må vere ${length + extra} cm lang. Bolt har bygd $length cm.", "Broen må være ${length + extra} cm lang. Bolt har bygd $length cm."),
                    txt("Kor mange cm manglar?", "Hvor mange cm mangler?"), extra,
                    txt("${length + extra} cm − $length cm = $extra cm."), equation(length + extra, MINUS, length))
            },
            mission("m_mission_code10", txt("Koden med to siffer", "Koden med to sifre"), 2) { ctx ->
                val tens = ctx.random.between(1, 9); val ones = ctx.random.between(0, 9)
                task(ctx, "$tens:$ones", txt("Døra opnar med eit tal: $tens ${if (tens == 1) "tiar" else "tiarar"} og $ones ${if (ones == 1) "einar" else "einarar"}.", "Døren åpner med et tall: $tens ${if (tens == 1) "tier" else "tiere"} og $ones ${if (ones == 1) "ener" else "enere"}."),
                    txt("Kva er koden?", "Hva er koden?"), tens * 10 + ones,
                    txt("${tens * 10} + $ones = ${tens * 10 + ones}."), Visual.BaseTen(tens, ones))
            },
            mission("m_mission_packs", txt("Pakk proviant"), 3) { ctx ->
                val boxes = ctx.random.between(2, 8); val per = ctx.random.between(2, 5); val loose = ctx.random.between(1, per - 1)
                task(ctx, "$boxes:$per:$loose", txt("Du har $boxes fulle esker med $per matpakkar i kvar, og ${if (loose == 1) "1 laus matpakke" else "$loose lause matpakkar"}.", "Du har $boxes fulle esker med $per matpakker i hver, og ${if (loose == 1) "1 løs matpakke" else "$loose løse matpakker"}."),
                    txt("Kor mange matpakkar har du?", "Hvor mange matpakker har du?"), boxes * per + loose,
                    txt("Eskene: $boxes × $per = ${boxes * per}. Så ${boxes * per} + $loose = ${boxes * per + loose}."),
                    Visual.Story(txt("Rekn ut eskene først. Legg så til dei lause matpakkane.", "Regn ut eskene først. Legg så til de løse matpakkene."), false))
            },
            mission("m_mission_code", txt("Den hemmelege koden", "Den hemmelige koden"), 3) { ctx ->
                val hundreds = ctx.random.between(1, 9); val tens = ctx.random.between(0, 9); val ones = ctx.random.between(0, 9)
                val result = hundreds * 100 + tens * 10 + ones
                task(ctx, "$hundreds:$tens:$ones", txt("Døra opnar med eit tal: $hundreds ${if (hundreds == 1) "hundrar" else "hundrarar"}, $tens ${if (tens == 1) "tiar" else "tiarar"} og $ones ${if (ones == 1) "einar" else "einarar"}.", "Døren åpner med et tall: $hundreds ${if (hundreds == 1) "hundrer" else "hundrere"}, $tens ${if (tens == 1) "tier" else "tiere"} og $ones ${if (ones == 1) "ener" else "enere"}."),
                    txt("Kva er koden?", "Hva er koden?"), result,
                    txt("${hundreds * 100} + ${tens * 10} + $ones = $result."),
                    Visual.Story(txt("Hundrarplassen kjem først, så tiarplassen og einarplassen.", "Hundrerplassen kommer først, så tierplassen og enerplassen."), false), max = 999)
            },
        ),
    ))
}
