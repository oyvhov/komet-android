package app.komet.ui

import app.komet.domain.txt

/** Interface text in both written standards. Content text lives with the tasks in `domain`. */
object S {
    val appName = txt("Komet")
    val tagline = txt("Rekning og lesing i verdsrommet", "Regning og lesing i verdensrommet")

    // Oppstart
    val chooseMaalform = txt("Nynorsk eller bokmål?")
    val chooseMaalformBody = txt("Vel den målforma barnet lærer på skulen.", "Velg målformen barnet lærer på skolen.")
    val nynorsk = txt("Nynorsk")
    val bokmaal = txt("Bokmål")
    val nynorskSample = txt("Eg les og reknar")
    val bokmaalSample = txt("Jeg leser og regner")
    val nameTitle = txt("Kva heiter romfararen?", "Hva heter romfareren?")
    val nameLabel = txt("Namn", "Navn")
    val avatarTitle = txt("Vel ein figur", "Velg en figur")
    fun gradeTitle(name: String) = txt("Kor langt har $name kome?", "Hvor langt har $name kommet?")
    val gradeNames = listOf(txt("5 år – førskule", "5 år – førskole"), txt("1. klasse"), txt("2. klasse"), txt("3. klasse"))
    val gradeDetails = listOf(
        txt("Telling, former og dei første bokstavane", "Telling, former og de første bokstavene"),
        txt("Pluss og minus til 20, bokstavar og ord", "Pluss og minus til 20, bokstaver og ord"),
        txt("Tal til 100, klokka og setningar", "Tall til 100, klokka og setninger"),
        txt("Gongetabellen, kvarter og tekstar", "Gangetabellen, kvarter og tekster"),
    )
    val caseTitle = txt("Korleis skal bokstavane sjå ut?", "Hvordan skal bokstavene se ut?")
    val caseUpper = txt("STORE BOKSTAVAR", "STORE BOKSTAVER")
    val caseLower = txt("små bokstavar", "små bokstaver")
    val caseUpperDetail = txt("Vanleg i starten av 1. klasse", "Vanlig i starten av 1. klasse")
    val caseLowerDetail = txt("Slik bøker og tekstar ser ut", "Slik bøker og tekster ser ut")
    val next = txt("Neste")
    val back = txt("Tilbake")
    val launch = txt("Skyt opp!")
    fun readyTitle(name: String) = txt("Klar, $name?")
    val readyBody = txt("Samle stjerner, lås opp planetar og finn romkort.", "Samle stjerner, lås opp planeter og finn romkort.")

    // Heim
    fun hello(name: String) = txt("Hei, $name!")
    val nextMission = txt("Neste oppdrag")
    val start = txt("Start")
    val resumeLabel = txt("Uferdig runde")
    fun resumeDetail(done: Int, total: Int) = txt("$done av $total oppgåver er gjort", "$done av $total oppgaver er gjort")
    val resume = txt("Hald fram", "Fortsett")
    val dailyMission = txt("Dagens oppdrag")
    fun roundsOf(done: Int, goal: Int) = txt("$done av $goal rundar", "$done av $goal runder")
    val goalDone = txt("Fullført! Bra jobba i dag.", "Fullført! Bra jobbet i dag.")
    val math = txt("Matte")
    val reading = txt("Norsk")
    val english = txt("Engelsk")
    val space = txt("Verdsrommet", "Verdensrommet")
    fun subject(subject: app.komet.domain.Subject) = when (subject) {
        app.komet.domain.Subject.MATH -> math
        app.komet.domain.Subject.READING -> reading
        app.komet.domain.Subject.ENGLISH -> english
        app.komet.domain.Subject.SPACE -> space
    }
    val race = txt("Rakettløp")
    val raceDetail = txt("Rekn så fort du kan", "Regn så fort du kan")
    val cards = txt("Romkort")
    fun cardsOf(done: Int, total: Int) = txt("$done av $total kort")
    fun streak(days: Int) = if (days == 1) txt("1 dag på rad") else txt("$days dagar på rad", "$days dager på rad")
    val parents = txt("Foreldre")
    val switchProfile = txt("Byt profil", "Bytt profil")
    val whoPlays = txt("Kven skal spele?", "Hvem skal spille?")
    val newBadge = txt("Nytt!")
    fun nextRank(title: String) = txt("Neste rang: $title")
    val explore = txt("Utforsk")
    val exploreDetail = txt("Finn oppgåvene du likar best", "Finn oppgavene du liker best")
    val favorites = txt("Favorittane dine", "Favorittene dine")
    val favoritesHint = txt("Trykk på hjartet ved eit nivå for å leggje det her.", "Trykk på hjertet ved et nivå for å legge det her.")
    val addFavorite = txt("Legg til i favorittar", "Legg til i favoritter")
    val removeFavorite = txt("Fjern frå favorittar", "Fjern fra favoritter")
    fun levelCount(count: Int) = txt("$count nivå")
    val gradeShort = listOf(txt("Førskule", "Førskole"), txt("1. klasse"), txt("2. klasse"), txt("3. klasse"))

    // Kart
    fun levels(done: Int, total: Int) = txt("$done av $total nivå")
    val lockedHint = txt("Fullfør nivået før først", "Fullfør nivået før først")
    val chapterLocked = txt("Låst")

    // Runde
    val quitTitle = txt("Vil du avslutte runden?")
    val quitBody = txt("Svara dine er lagra. Du kan halde fram seinare frå heimeskjermen.", "Svarene dine er lagret. Du kan fortsette senere fra hjemskjermen.")
    val quit = txt("Avslutt")
    val keepGoing = txt("Hald fram", "Fortsett")
    val praise = listOf(
        txt("Heilt rett!", "Helt riktig!"),
        txt("Supert!"),
        txt("Kjempebra!"),
        txt("Knallbra!"),
        txt("Rett!", "Riktig!"),
        txt("Stjernebra!"),
    )
    val tryAgain = txt("Prøv igjen")
    val answerWas = txt("Rett svar", "Riktig svar")
    val onward = txt("Vidare", "Videre")
    val hint = txt("Hjelp")
    val listen = txt("Lytt")
    val erase = txt("Slett")
    val confirm = txt("Ferdig")
    val trueLabel = txt("Sant")
    val falseLabel = txt("Usant")
    fun claps(count: Int) = if (count == 1) txt("1 staving", "1 stavelse") else txt("$count stavingar", "$count stavelser")
    val findSame = txt("Finn den same", "Finn den samme")
    val closeRound = txt("Lukk runden")
    val readQuestion = txt("Les oppgåva høgt", "Les oppgaven høyt")

    // Resultat
    val resultGreat = txt("Fantastisk!")
    val resultGood = txt("Bra jobba!", "Bra jobbet!")
    val resultDone = txt("Fullført!")
    val resultTry = txt("Godt forsøk!")
    fun score(firstTry: Int, total: Int) = txt("$firstTry av $total rett på første forsøk", "$firstTry av $total riktige på første forsøk")
    val nextLevel = txt("Neste nivå")
    val playAgain = txt("Spel igjen", "Spill igjen")
    val home = txt("Heim", "Hjem")
    val newCard = txt("Nytt romkort!")
    fun newRank(title: String) = txt("Ny rang: $title")
    val goalReached = txt("Dagens oppdrag er fullført!")
    fun raceScore(score: Int) = txt("$score rette svar", "$score riktige svar")
    val newRecord = txt("Ny rekord!")
    fun record(best: Int) = txt("Rekord: $best")
    val seeCard = txt("Sjå kortet", "Se kortet")

    // Romkort
    fun nextCardAt(stars: Int) = txt("Neste kort ved $stars stjerner")
    val allCards = txt("Du har alle korta!", "Du har alle kortene!")
    val readAloud = txt("Les høgt", "Les høyt")
    val close = txt("Lukk")

    // Rakettløp
    val chooseRace = txt("Vel eit løp", "Velg et løp")
    val raceRules = txt("Svar på så mange du klarer på 60 sekund.", "Svar på så mange du klarer på 60 sekunder.")
    val go = txt("Start!")
    val timeUp = txt("Tida er ute!", "Tiden er ute!")

    // Foreldre
    val adultsOnly = txt("Berre for vaksne", "Bare for voksne")
    fun gateQuestion(a: Int, b: Int) = txt("Kva er $a × $b?", "Hva er $a × $b?")
    val progress = txt("Framgang", "Fremgang")
    val settings = txt("Innstillingar", "Innstillinger")
    val profiles = txt("Profilar", "Profiler")
    val totalStars = txt("Stjerner")
    val rounds = txt("Rundar", "Runder")
    val firstTryRate = txt("Rett første gong", "Riktig første gang")
    val last7 = txt("Siste 7 dagar", "Siste 7 dager")
    fun minutes(value: Int) = txt("$value min")
    val time = txt("Tid")
    val practiceMore = txt("Øv meir på", "Øv mer på")
    val strongest = txt("Sterkast", "Sterkest")
    val notEnoughData = txt("Spel nokre rundar først, så dukkar det opp her.", "Spill noen runder først, så dukker det opp her.")
    fun completedLevels(done: Int, total: Int) = txt("$done av $total nivå fullført")
    val soundAndSpeech = txt("Lyd og tale")
    val soundEffects = txt("Lydeffektar", "Lydeffekter")
    val readAloudSetting = txt("Opplesing")
    val autoRead = txt("Les oppgåvene høgt automatisk", "Les oppgavene høyt automatisk")
    val slowSpeech = txt("Roleg tale", "Rolig tale")
    val haptics = txt("Vibrasjon")
    val dailyGoal = txt("Dagleg mål", "Daglig mål")
    fun roundsCount(n: Int) = if (n == 1) txt("1 runde") else txt("$n rundar", "$n runder")
    val voice = txt("Norsk stemme")
    val voiceReady = txt("Klar")
    val voiceLoading = txt("Startar …", "Starter …")
    val voiceMissing = txt("Manglar norsk stemme. Installer «Norsk» i taleinnstillingane.", "Mangler norsk stemme. Installer «Norsk» i taleinnstillingene.")
    val voiceUnavailable = txt("Fann ingen talemotor på eininga.", "Fant ingen talemotor på enheten.")
    val englishVoiceReady = txt("Engelsk stemme: klar")
    val englishVoiceMissing = txt(
        "Engelsk stemme manglar. Engelske ord blir viste som tekst i staden for å bli lesne høgt.",
        "Engelsk stemme mangler. Engelske ord vises som tekst i stedet for å bli lest høyt.",
    )
    val openTtsSettings = txt("Opne taleinnstillingar", "Åpne taleinnstillinger")
    val testVoice = txt("Test stemma", "Test stemmen")
    val testSentence = txt("Hei! Eg er klar til å lese for deg.", "Hei! Jeg er klar til å lese for deg.")
    val maalform = txt("Målform")
    val letters = txt("Bokstavar", "Bokstaver")
    val level = txt("Nivå")
    val unlockAll = txt("Opne alle nivå", "Åpne alle nivåer")
    val unlockAllDetail = txt("Barnet kan velje fritt på kartet.", "Barnet kan velge fritt på kartet.")
    val resetProgress = txt("Nullstill framgang", "Nullstill fremgang")
    val deleteProfile = txt("Slett profil")
    val addProfile = txt("Legg til profil")
    val areYouSure = txt("Er du sikker?")
    fun resetBody(name: String) = txt("Alle stjerner og all framgang for $name blir sletta.", "Alle stjerner og all fremgang for $name blir slettet.")
    fun deleteBody(name: String) = txt("Profilen til $name og all framgang blir sletta.", "Profilen til $name og all fremgang blir slettet.")
    val cancel = txt("Avbryt")
    val yesReset = txt("Nullstill")
    val yesDelete = txt("Slett")
    val active = txt("Aktiv")
    val use = txt("Bruk")
    val aboutTitle = txt("Om Komet")
    val fontCredit = txt(
        "Skrifta i oppgåvene er Andika frå SIL International, fri under SIL Open Font License 1.1.",
        "Skriften i oppgavene er Andika fra SIL International, fri under SIL Open Font License 1.1.",
    )
    val aboutBody = txt(
        "Ingen reklame, ingen kjøp i appen og inga sporing. Den einaste nettkontakten er når Komet ser etter ny versjon på GitHub, og det kan du slå av. All framgang blir lagra berre på denne eininga.",
        "Ingen reklame, ingen kjøp i appen og ingen sporing. Den eneste nettkontakten er når Komet ser etter ny versjon på GitHub, og det kan du slå av. All fremgang lagres bare på denne enheten.",
    )

    // Appoppdateringar
    val updates = txt("Appoppdateringar", "Appoppdateringer")
    fun updateReady(version: String) = txt("Komet $version er klar")
    fun installedVersion(version: String) = txt("Du har Komet $version")
    val updateCheck = txt("Sjekk no", "Sjekk nå")
    val updateDownload = txt("Last ned oppdatering")
    val updateInstall = txt("Installer oppdatering")
    val updateCancel = txt("Avbryt")
    val updateView = txt("Sjå oppdatering", "Se oppdatering")
    fun updateProgress(percent: Int) = txt("Lastar ned · $percent %", "Laster ned · $percent %")
    fun updateSize(megabytes: String) = txt("Nedlasting · $megabytes MB")
    val updateAuto = txt("Sjekk automatisk")
    val updateAutoHint = txt(
        "Ser etter ny versjon på GitHub når appen blir opna, høgst to gonger om dagen.",
        "Ser etter ny versjon på GitHub når appen åpnes, maks to ganger om dagen.",
    )
    val updatePreviews = txt("Testutgåver", "Testutgaver")
    val updatePreviewsHint = txt("Ta med utgåver som ikkje er ferdig testa.", "Ta med utgaver som ikke er ferdig testet.")
    val updateHint = txt(
        "Framgang og profilar blir verande. Android ber deg godkjenne installasjonen.",
        "Fremgang og profiler blir værende. Android ber deg godkjenne installasjonen.",
    )
    val updateCurrent = txt("Du har den nyaste versjonen.", "Du har den nyeste versjonen.")
    val updateNetwork = txt("Fekk ikkje kontakt med GitHub. Prøv igjen.", "Fikk ikke kontakt med GitHub. Prøv igjen.")
    val updateInvalid = txt("Oppdateringa kunne ikkje stadfestast. Ingenting vart installert.", "Oppdateringen kunne ikke bekreftes. Ingenting ble installert.")
    val updateStorage = txt("Kunne ikkje lagre oppdateringa. Sjekk ledig plass.", "Kunne ikke lagre oppdateringen. Sjekk ledig plass.")
    val updatePermission = txt(
        "Tillat at Komet installerer oppdateringar. Gå så tilbake og vel «Installer oppdatering».",
        "Tillat at Komet installerer oppdateringer. Gå så tilbake og velg «Installer oppdatering».",
    )
    val updateInstallError = txt("Android kunne ikkje opne installasjonen. Prøv igjen.", "Android kunne ikke åpne installasjonen. Prøv igjen.")
    val updateAccess = txt("Utgåva er ikkje tilgjengeleg no. Prøv igjen seinare.", "Utgaven er ikke tilgjengelig nå. Prøv igjen senere.")
    val updateRate = txt("GitHub avgrensar førespurnader. Vent litt før du sjekkar igjen.", "GitHub begrenser forespørsler. Vent litt før du sjekker igjen.")
    fun version(name: String) = txt("Versjon $name")
    val done = txt("Ferdig")
}
