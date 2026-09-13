package app.komet.domain

/** Hand-written reading material. Every line exists in nynorsk and bokmål. */
object ReadingContent {

    /** «Sant eller usant»: short statements a six-year-old can judge from everyday knowledge. */
    val trueFalse: List<Pair<Txt, Boolean>> = listOf(
        txt("Sola er varm.") to true,
        txt("Ei ku kan fly.", "En ku kan fly.") to false,
        txt("Is er kald.") to true,
        txt("Ein fisk bur i vatnet.", "En fisk bor i vannet.") to true,
        txt("Ein bil har fire hjul.", "En bil har fire hjul.") to true,
        txt("Katten seier voff.", "Katten sier voff.") to false,
        txt("Snø er kvit.", "Snø er hvit.") to true,
        txt("Ein elefant er liten.", "En elefant er liten.") to false,
        txt("Ein hund har tre bein.", "En hund har tre bein.") to false,
        txt("Månen er på himmelen.") to true,
        txt("Eit eple kan snakke.", "Et eple kan snakke.") to false,
        txt("Vi søv i ei seng.", "Vi sover i en seng.") to true,
        txt("Graset er grønt.", "Gresset er grønt.") to true,
        txt("Ein fugl har fjører.", "En fugl har fjær.") to true,
        txt("Mars er ein planet.", "Mars er en planet.") to true,
        txt("Sola er kald.", "Solen er kald.") to false,
        txt("Ein mus er større enn ein hest.", "En mus er større enn en hest.") to false,
        txt("Vi har to auge.", "Vi har to øyne.") to true,
        txt("Is er varm.") to false,
        txt("Ein rakett kan fly til månen.", "En rakett kan fly til månen.") to true,
        txt("Fisken kan gå på land.") to false,
        txt("Jorda er ein planet.", "Jorden er en planet.") to true,
        txt("Om natta er det mørkt.", "Om natten er det mørkt.") to true,
        txt("Ein stein er mjuk.", "En stein er myk.") to false,
        txt("Bananen er blå.") to false,
        txt("Ein kanin et gulrot.", "En kanin spiser gulrot.") to true,
        txt("Ein tiger har striper.", "En tiger har striper.") to true,
        txt("Vi et suppe med gaffel.", "Vi spiser suppe med gaffel.") to false,
        txt("Ein båt kan segle.", "En båt kan seile.") to true,
        txt("Ei klokke viser tida.", "En klokke viser tiden.") to true,
    )

    /** Sentence templates for «Les og finn biletet». «#» is replaced by article and noun. */
    val pictureSentences: List<Txt> = listOf(
        txt("Eg ser #.", "Jeg ser #."),
        txt("Her er #."),
        txt("Det er #."),
        txt("Eg har #.", "Jeg har #."),
        txt("Sjå, der er #!", "Se, der er #!"),
    )

    data class Gap(val sentence: Txt, val answer: Txt, val wrong: List<Txt>)

    /** «Finn ordet»: «___» marks the gap. */
    val gaps: List<Gap> = listOf(
        Gap(txt("Katten drikk ___.", "Katten drikker ___."), txt("mjølk", "melk"), listOf(txt("sko"), txt("bil"))),
        Gap(txt("Eg søv i ei ___.", "Jeg sover i en ___."), txt("seng"), listOf(txt("sol"), txt("bok"))),
        Gap(txt("Vi drikk av ein ___.", "Vi drikker av en ___."), txt("kopp"), listOf(txt("stein"), txt("sko"))),
        Gap(txt("Sola er ___.", "Solen er ___."), txt("gul"), listOf(txt("grøn", "grønn"), txt("firkanta", "firkantet"))),
        Gap(txt("Ein bil har fire ___.", "En bil har fire ___."), txt("hjul"), listOf(txt("auge", "øyne"), txt("hender"))),
        Gap(txt("Om vinteren kjem det ___.", "Om vinteren kommer det ___."), txt("snø"), listOf(txt("bananar", "bananer"), txt("sko"))),
        Gap(txt("Fisken sym i ___.", "Fisken svømmer i ___."), txt("vatnet", "vannet"), listOf(txt("senga", "sengen"), txt("lufta"))),
        Gap(txt("Eg les ei ___.", "Jeg leser en ___."), txt("bok"), listOf(txt("gulrot"), txt("sokk"))),
        Gap(txt("Kua seier ___.", "Kua sier ___."), txt("mø"), listOf(txt("voff"), txt("mjau"))),
        Gap(txt("Raketten flyg til ___.", "Raketten flyr til ___."), txt("månen"), listOf(txt("badekaret"), txt("skoen"))),
        Gap(txt("Hunden seier ___.", "Hunden sier ___."), txt("voff"), listOf(txt("mø"), txt("kvakk"))),
        Gap(txt("Ein elefant er veldig ___.", "En elefant er veldig ___."), txt("stor"), listOf(txt("liten"), txt("blå"))),
        Gap(txt("Det regnar. Eg tek på meg ___.", "Det regner. Jeg tar på meg ___."), txt("regnjakke"), listOf(txt("solbriller"), txt("badebukse"))),
        Gap(txt("Eg et ein ___.", "Jeg spiser en ___."), txt("banan"), listOf(txt("stol"), txt("buss"))),
        Gap(txt("Fuglen sit i eit ___.", "Fuglen sitter i et ___."), txt("tre"), listOf(txt("hjul"), txt("glas", "glass"))),
    )

    /** «Ordne orda»: short sentences without repeated words, so any correct order is unambiguous. */
    val orderedSentences: List<Txt> = listOf(
        txt("Eg har ein hund.", "Jeg har en hund."),
        txt("Sola skin i dag.", "Solen skinner i dag."),
        txt("Katten er svart."),
        txt("Vi spelar fotball.", "Vi spiller fotball."),
        txt("Månen er stor."),
        txt("Ho et ein banan.", "Hun spiser en banan."),
        txt("Raketten flyg fort.", "Raketten flyr fort."),
        txt("Eg liker å lese.", "Jeg liker å lese."),
        txt("Mars er ein planet.", "Mars er en planet."),
        txt("Bilen er raud.", "Bilen er rød."),
        txt("Han har ein ball.", "Han har en ball."),
        txt("Fuglen syng i treet.", "Fuglen synger i treet."),
        txt("Det snør ute."),
        txt("Eg går til skulen.", "Jeg går til skolen."),
    )

    data class Story(val text: Txt, val question: Txt, val answer: Txt, val wrong: List<Txt>)

    val stories: List<Story> = listOf(
        Story(
            txt("Ola har ein hund. Hunden heiter Rex. Rex liker å leike.", "Ola har en hund. Hunden heter Rex. Rex liker å leke."),
            txt("Kva heiter hunden?", "Hva heter hunden?"),
            txt("Rex"), listOf(txt("Ola"), txt("Bamse")),
        ),
        Story(
            txt("Emma skal til månen. Ho flyg med ein rakett. Raketten er raud.", "Emma skal til månen. Hun flyr med en rakett. Raketten er rød."),
            txt("Kva farge har raketten?", "Hvilken farge har raketten?"),
            txt("raud", "rød"), listOf(txt("blå"), txt("grøn", "grønn")),
        ),
        Story(
            txt("Det snør ute. Noah lagar ein snømann. Snømannen har ei gulrot som nase.", "Det snør ute. Noah lager en snømann. Snømannen har en gulrot som nese."),
            txt("Kva har snømannen som nase?", "Hva har snømannen som nese?"),
            txt("ei gulrot", "en gulrot"), listOf(txt("ein stein", "en stein"), txt("eit eple", "et eple")),
        ),
        Story(
            txt("Nora har tre fiskar. Ein fisk er gul. To fiskar er blå.", "Nora har tre fisker. En fisk er gul. To fisker er blå."),
            txt("Kor mange fiskar er blå?", "Hvor mange fisker er blå?"),
            txt("to"), listOf(txt("tre"), txt("ein", "en")),
        ),
        Story(
            txt("Jakob et frukost. Han et eit egg og drikk mjølk.", "Jakob spiser frokost. Han spiser et egg og drikker melk."),
            txt("Kva drikk Jakob?", "Hva drikker Jakob?"),
            txt("mjølk", "melk"), listOf(txt("vatn", "vann"), txt("saft")),
        ),
        Story(
            txt("Sofie er på stranda. Sola skin. Ho badar i havet.", "Sofie er på stranden. Solen skinner. Hun bader i havet."),
            txt("Kvar er Sofie?", "Hvor er Sofie?"),
            txt("på stranda", "på stranden"), listOf(txt("på skulen", "på skolen"), txt("i skogen")),
        ),
        Story(
            txt("Isak har ein ny sykkel. Sykkelen er grøn. Isak syklar til skulen.", "Isak har en ny sykkel. Sykkelen er grønn. Isak sykler til skolen."),
            txt("Kva farge har sykkelen?", "Hvilken farge har sykkelen?"),
            txt("grøn", "grønn"), listOf(txt("raud", "rød"), txt("gul")),
        ),
        Story(
            txt("Mars er ein raud planet. Det er kaldt på Mars. Ingen menneske bur der.", "Mars er en rød planet. Det er kaldt på Mars. Ingen mennesker bor der."),
            txt("Korleis er det på Mars?", "Hvordan er det på Mars?"),
            txt("kaldt"), listOf(txt("varmt"), txt("vått")),
        ),
        Story(
            txt("Ella har bursdag. Ho blir sju år. Ho får ei kake med sju lys.", "Ella har bursdag. Hun blir sju år. Hun får en kake med sju lys."),
            txt("Kor gammal blir Ella?", "Hvor gammel blir Ella?"),
            txt("sju år"), listOf(txt("seks år"), txt("åtte år")),
        ),
        Story(
            txt("Filip ser ein rev i skogen. Reven har ein lang, raud hale. Han spring fort.", "Filip ser en rev i skogen. Reven har en lang, rød hale. Den løper fort."),
            txt("Kva ser Filip i skogen?", "Hva ser Filip i skogen?"),
            txt("ein rev", "en rev"), listOf(txt("ein bjørn", "en bjørn"), txt("ein elg", "en elg")),
        ),
        Story(
            txt("Astronauten bur på romstasjonen. Ho et mat frå ein pose. Om kvelden søv ho i ein sovepose.", "Astronauten bor på romstasjonen. Hun spiser mat fra en pose. Om kvelden sover hun i en sovepose."),
            txt("Kvar bur astronauten?", "Hvor bor astronauten?"),
            txt("på romstasjonen"), listOf(txt("på månen"), txt("i ein båt", "i en båt")),
        ),
        Story(
            txt("Det regnar. Nora tek på seg regnkle og støvlar. Ho hoppar i vasspyttane.", "Det regner. Nora tar på seg regntøy og støvler. Hun hopper i sølepyttene."),
            txt("Kva vêr er det?", "Hva slags vær er det?"),
            txt("regn"), listOf(txt("sol"), txt("snø")),
        ),
    )

    /** High-frequency words. Index-aligned so a word and its bokmål form share a slot. */
    val sightWords: List<Txt> = listOf(
        txt("eg", "jeg"), txt("du"), txt("han"), txt("ho", "hun"), txt("vi"), txt("dei", "de"),
        txt("og"), txt("i"), txt("på"), txt("er"), txt("det"), txt("ein", "en"),
        txt("ikkje", "ikke"), txt("ja"), txt("nei"), txt("med"), txt("til"), txt("av"),
        txt("ut"), txt("inn"), txt("opp"), txt("ned"), txt("kva", "hva"), txt("kor", "hvor"),
    )
}
