package app.komet.domain

/** A planet in the solar system. [fromSun] is 1 for Mercury; [sizeRank] is 1 for the biggest. */
data class PlanetInfo(val id: String, val name: Txt, val fromSun: Int, val sizeRank: Int, val clue: Txt) {
    val art: CardArt get() = SpaceContent.cardArt(id)
}

/** A statement a child can check: true or false, with a short reason. */
data class SpaceFact(val statement: Txt, val truth: Boolean, val why: Txt)

/** A picture question: which thing in space fits the clue? */
data class SpaceThing(val id: String, val name: Txt, val art: CardArt, val clue: Txt)

object SpaceContent {

    fun cardArt(id: String): CardArt = SpaceCards.all.first { it.id == id }.art

    val planets: List<PlanetInfo> = listOf(
        PlanetInfo("merkur", txt("Merkur"), 1, 8, txt("Eg er planeten nærast sola.", "Jeg er planeten nærmest solen.")),
        PlanetInfo("venus", txt("Venus"), 2, 6, txt("Eg er den varmaste planeten.", "Jeg er den varmeste planeten.")),
        PlanetInfo("jorda", txt("Jorda", "Jorden"), 3, 5, txt("Eg er planeten der du bur.", "Jeg er planeten der du bor.")),
        PlanetInfo("mars", txt("Mars"), 4, 7, txt("Eg er den raude planeten.", "Jeg er den røde planeten.")),
        PlanetInfo("jupiter", txt("Jupiter"), 5, 1, txt("Eg er den største planeten.", "Jeg er den største planeten.")),
        PlanetInfo("saturn", txt("Saturn"), 6, 2, txt("Eg har dei flottaste ringane.", "Jeg har de flotteste ringene.")),
        PlanetInfo("uranus", txt("Uranus"), 7, 3, txt("Eg ligg på sida og trillar rundt sola.", "Jeg ligger på siden og triller rundt solen.")),
        PlanetInfo("neptun", txt("Neptun"), 8, 4, txt("Eg er planeten lengst frå sola.", "Jeg er planeten lengst fra solen.")),
    )

    /** Planets a child can tell apart from the drawing alone. */
    val recognisable: List<PlanetInfo> get() = planets.filter { it.id in setOf("jorda", "mars", "jupiter", "saturn") }

    val sunAndEarth: List<SpaceThing> = listOf(
        SpaceThing("sola", txt("Sola", "Solen"), cardArt("sola"), txt("Kva gir oss lys og varme?", "Hva gir oss lys og varme?")),
        SpaceThing("jorda", txt("Jorda", "Jorden"), cardArt("jorda"), txt("Kva planet bur vi på?", "Hvilken planet bor vi på?")),
        SpaceThing("manen", txt("Månen"), cardArt("manen"), txt("Kva går rundt jorda?", "Hva går rundt jorden?")),
    )

    val spaceTravel: List<SpaceThing> = listOf(
        SpaceThing("astronaut", txt("Astronaut"), cardArt("astronaut"), txt("Kven reiser ut i verdsrommet?", "Hvem reiser ut i verdensrommet?")),
        SpaceThing("rakett", txt("Rakett"), cardArt("rakett"), txt("Kva kan ta oss ut i verdsrommet?", "Hva kan ta oss ut i verdensrommet?")),
        SpaceThing("satellitt", txt("Satellitt"), cardArt("satellitt"), txt("Kva går rundt jorda og hjelper oss med vêrmeldinga?", "Hva går rundt jorden og hjelper oss med værmeldingen?")),
        SpaceThing("teleskop", txt("Teleskop"), cardArt("teleskop"), txt("Kva bruker vi for å sjå langt ut i verdsrommet?", "Hva bruker vi for å se langt ut i verdensrommet?")),
    )

    val skyThings: List<SpaceThing> = listOf(
        SpaceThing("nordlys", txt("Nordlys"), cardArt("nordlys"), txt("Kva kan vi sjå som grøne lys på himmelen i Noreg?", "Hva kan vi se som grønne lys på himmelen i Norge?")),
        SpaceThing("karlsvogna", txt("Karlsvogna", "Karlsvognen"), cardArt("karlsvogna"), txt("Kva er sju stjerner som ser ut som ei øse?", "Hva er sju stjerner som ser ut som en øse?")),
        SpaceThing("stjerneskot", txt("Stjerneskot", "Stjerneskudd"), cardArt("stjerneskot"), txt("Kva er ein liten stein som brenn opp i lufta?", "Hva er en liten stein som brenner opp i lufta?")),
        SpaceThing("komet", txt("Komet"), cardArt("komet"), txt("Kva er ein snøball av is og støv med lang hale?", "Hva er en snøball av is og støv med lang hale?")),
    )

    /** Moon phases shown with the emoji every phone has. */
    val moonPhases: List<Pair<String, Txt>> = listOf(
        "🌕" to txt("Fullmåne"),
        "🌓" to txt("Halvmåne"),
        "🌑" to txt("Nymåne"),
    )

    val sunFacts: List<SpaceFact> = listOf(
        SpaceFact(txt("Sola er ei stjerne.", "Solen er en stjerne."), true, txt("Sola er ei stjerne som er nær oss.", "Solen er en stjerne som er nær oss.")),
        SpaceFact(txt("Sola går rundt jorda.", "Solen går rundt jorden."), false, txt("Det er jorda som går rundt sola.", "Det er jorden som går rundt solen.")),
        SpaceFact(txt("Jorda bruker eitt år på å gå rundt sola.", "Jorden bruker ett år på å gå rundt solen."), true, txt("Ein tur rundt sola tek eitt år.", "En tur rundt solen tar ett år.")),
        SpaceFact(txt("Det blir natt når vår side av jorda snur seg bort frå sola.", "Det blir natt når vår side av jorden snur seg bort fra solen."), true, txt("Jorda snurrar rundt seg sjølv kvart døgn.", "Jorden snurrer rundt seg selv hvert døgn.")),
        SpaceFact(txt("Sola er kald.", "Solen er kald."), false, txt("Sola er veldig, veldig varm.", "Solen er veldig, veldig varm.")),
        SpaceFact(txt("Det er dag på heile jorda samtidig.", "Det er dag på hele jorden samtidig."), false, txt("Når det er dag hos oss, er det natt på andre sida.", "Når det er dag hos oss, er det natt på andre siden.")),
        SpaceFact(txt("Sola er mykje større enn jorda.", "Solen er mye større enn jorden."), true, txt("Over ein million jordkloder får plass inni sola.", "Over en million jordkloder får plass inni solen.")),
        SpaceFact(txt("Du kan sjå rett på sola utan å skade auga.", "Du kan se rett på solen uten å skade øynene."), false, txt("Sola er så sterk at ho kan skade auga. Sjå aldri rett på henne.", "Solen er så sterk at den kan skade øynene. Se aldri rett på den.")),
    )

    val moonFacts: List<SpaceFact> = listOf(
        SpaceFact(txt("Månen går rundt jorda.", "Månen går rundt jorden."), true, txt("Månen bruker om lag ein månad på ein tur rundt jorda.", "Månen bruker omtrent en måned på en tur rundt jorden.")),
        SpaceFact(txt("Det finst luft å puste i på månen.", "Det finnes luft å puste i på månen."), false, txt("På månen må du ha romdrakt.", "På månen må du ha romdrakt.")),
        SpaceFact(txt("Månen lyser av seg sjølv.", "Månen lyser av seg selv."), false, txt("Månen lyser fordi sola skin på han.", "Månen lyser fordi solen skinner på den.")),
        SpaceFact(txt("Menneske har gått på månen.", "Mennesker har gått på månen."), true, txt("Dei første gjekk på månen i 1969.", "De første gikk på månen i 1969.")),
        SpaceFact(txt("På månen kan du hoppe høgare enn på jorda.", "På månen kan du hoppe høyere enn på jorden."), true, txt("Månen dreg mykje mindre i deg enn jorda gjer.", "Månen trekker mye mindre i deg enn jorden gjør.")),
        SpaceFact(txt("Månen er større enn jorda.", "Månen er større enn jorden."), false, txt("Jorda er om lag fire gonger så brei som månen.", "Jorden er omtrent fire ganger så bred som månen.")),
        SpaceFact(txt("Månen har mange krater.", "Månen har mange kratere."), true, txt("Krater er hol etter steinar som har krasja inn i månen.", "Kratere er hull etter steiner som har krasjet inn i månen.")),
        SpaceFact(txt("Månen endrar form fordi nokon et av han.", "Månen endrer form fordi noen spiser av den."), false, txt("Vi ser berre ulike delar av den sida sola lyser på.", "Vi ser bare ulike deler av den siden solen lyser på.")),
    )

    val travelFacts: List<SpaceFact> = listOf(
        SpaceFact(txt("Astronautar svevar inne i romstasjonen.", "Astronauter svever inne i romstasjonen."), true, txt("Dei fell rundt jorda heile tida, så dei svevar.", "De faller rundt jorden hele tiden, så de svever.")),
        SpaceFact(txt("I verdsrommet kan du puste utan hjelm.", "I verdensrommet kan du puste uten hjelm."), false, txt("Det er inga luft i verdsrommet.", "Det er ingen luft i verdensrommet.")),
        SpaceFact(txt("Ein rakett må fly veldig fort for å kome ut i verdsrommet.", "En rakett må fly veldig fort for å komme ut i verdensrommet."), true, txt("Raketten må vere mange gonger raskare enn eit fly.", "Raketten må være mange ganger raskere enn et fly.")),
        SpaceFact(txt("Romstasjonen går rundt jorda.", "Romstasjonen går rundt jorden."), true, txt("Han går rundt jorda mange gonger kvart døgn.", "Den går rundt jorden mange ganger hvert døgn.")),
        SpaceFact(txt("Astronautar et mat med gaffel og tallerken som heime.", "Astronauter spiser mat med gaffel og tallerken som hjemme."), false, txt("Maten ville svevd vekk, så han ligg i posar.", "Maten ville svevd bort, så den ligger i poser.")),
    )

    val skyFacts: List<SpaceFact> = listOf(
        SpaceFact(txt("Stjernene er soler langt borte.", "Stjernene er soler langt borte."), true, txt("Mange stjerner er større enn sola vår.", "Mange stjerner er større enn solen vår.")),
        SpaceFact(txt("Eit stjerneskot er ei stjerne som fell ned.", "Et stjerneskudd er en stjerne som faller ned."), false, txt("Det er ein liten stein som brenn opp i lufta.", "Det er en liten stein som brenner opp i lufta.")),
        SpaceFact(txt("Vi kan sjå nordlys i Noreg om vinteren.", "Vi kan se nordlys i Norge om vinteren."), true, txt("Nordlyset er lettast å sjå i nord når det er mørkt.", "Nordlyset er lettest å se i nord når det er mørkt.")),
        SpaceFact(txt("Mjølkevegen er galaksen vår.", "Melkeveien er galaksen vår."), true, txt("Sola er ei av mange milliardar stjerner i Mjølkevegen.", "Solen er en av mange milliarder stjerner i Melkeveien.")),
        SpaceFact(txt("Ein komet får lang hale når han kjem nær sola.", "En komet får lang hale når den kommer nær solen."), true, txt("Sola varmar isen, og det blir ein hale av gass og støv.", "Solen varmer isen, og det blir en hale av gass og støv.")),
        SpaceFact(txt("Det er berre éi stjerne på himmelen.", "Det er bare én stjerne på himmelen."), false, txt("Det finst fleire stjerner enn vi kan telje.", "Det finnes flere stjerner enn vi kan telle.")),
    )

    val allFacts: List<SpaceFact> get() = sunFacts + moonFacts + travelFacts + skyFacts
}
