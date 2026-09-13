package app.komet.domain

enum class SpecialArt { SUN, EARTH, JUPITER, BLACK_HOLE, AURORA, BIG_DIPPER, MOON_LANDING, ASTEROID }

sealed interface CardArt {
    data class Planet(val look: PlanetLook) : CardArt
    data class Emoji(val emoji: String) : CardArt
    data class Special(val kind: SpecialArt) : CardArt
}

/** A collectable card. The fact doubles as reading practice and can be read aloud. */
data class SpaceCard(val id: String, val title: Txt, val fact: Txt, val art: CardArt, val cost: Int)

object SpaceCards {

    val all: List<SpaceCard> = listOf(
        SpaceCard(
            "sola", txt("Sola", "Solen"),
            txt("Sola er ei stjerne. Ho er så stor at over ein million jordkloder får plass inni.", "Solen er en stjerne. Den er så stor at over en million jordkloder får plass inni."),
            CardArt.Special(SpecialArt.SUN), 3,
        ),
        SpaceCard(
            "manen", txt("Månen"),
            txt("Månen går rundt jorda. Tolv astronautar har gått på månen.", "Månen går rundt jorden. Tolv astronauter har gått på månen."),
            CardArt.Planet(PlanetLook(0xFFD2D2D6, 0xFFF2F2F4, 0xFF7C7C86, craters = true)), 8,
        ),
        SpaceCard(
            "mars", txt("Mars"),
            txt("Mars er raud fordi det er rust i støvet. Mars har den største vulkanen i solsystemet.", "Mars er rød fordi det er rust i støvet. Mars har den største vulkanen i solsystemet."),
            CardArt.Planet(PlanetLook(0xFFE0643C, 0xFFFFA27F, 0xFF8E2F16, craters = true)), 14,
        ),
        SpaceCard(
            "rakett", txt("Rakett"),
            txt("Ein rakett må fly veldig fort for å kome ut i verdsrommet.", "En rakett må fly veldig fort for å komme ut i verdensrommet."),
            CardArt.Emoji("🚀".asEmoji()), 20,
        ),
        SpaceCard(
            "jorda", txt("Jorda", "Jorden"),
            txt("Jorda er heimen vår. Ho er den einaste planeten vi veit om som har liv.", "Jorden er hjemmet vårt. Den er den eneste planeten vi vet om som har liv."),
            CardArt.Special(SpecialArt.EARTH), 27,
        ),
        SpaceCard(
            "saturn", txt("Saturn"),
            txt("Saturn har flotte ringar. Dei er laga av is og stein.", "Saturn har flotte ringer. De er laget av is og stein."),
            CardArt.Planet(PlanetLook(0xFFE9C98B, 0xFFFFF0CF, 0xFFA8834A, ring = true, bands = true)), 34,
        ),
        SpaceCard(
            "astronaut", txt("Astronaut"),
            txt("Astronautar svevar i romstasjonen. Dei må feste soveposen når dei skal sove.", "Astronauter svever i romstasjonen. De må feste soveposen når de skal sove."),
            CardArt.Emoji("🧑‍🚀"), 42,
        ),
        SpaceCard(
            "jupiter", txt("Jupiter"),
            txt("Jupiter er den største planeten. Den raude flekken er ein storm som er større enn jorda.", "Jupiter er den største planeten. Den røde flekken er en storm som er større enn jorden."),
            CardArt.Special(SpecialArt.JUPITER), 50,
        ),
        SpaceCard(
            "komet", txt("Komet"),
            txt("Ein komet er ein stor snøball av is og støv. Nær sola får han ein lang hale.", "En komet er en stor snøball av is og støv. Nær solen får den en lang hale."),
            CardArt.Emoji("☄".asEmoji()), 58,
        ),
        SpaceCard(
            "merkur", txt("Merkur"),
            txt("Merkur er planeten nærast sola. Eit år på Merkur varer berre 88 dagar.", "Merkur er planeten nærmest solen. Et år på Merkur varer bare 88 dager."),
            CardArt.Planet(PlanetLook(0xFFA9A9AE, 0xFFDADADF, 0xFF5E5E66, craters = true)), 67,
        ),
        SpaceCard(
            "stjerneskot", txt("Stjerneskot", "Stjerneskudd"),
            txt("Eit stjerneskot er ikkje ei stjerne. Det er ein liten stein som brenn opp i lufta.", "Et stjerneskudd er ikke en stjerne. Det er en liten stein som brenner opp i lufta."),
            CardArt.Emoji("🌠".asEmoji()), 76,
        ),
        SpaceCard(
            "venus", txt("Venus"),
            txt("Venus er den varmaste planeten. Det er varmare der enn i ein steikeomn.", "Venus er den varmeste planeten. Det er varmere der enn i en stekeovn."),
            CardArt.Planet(PlanetLook(0xFFE8C07A, 0xFFFFE9B8, 0xFFA7773A, bands = true)), 86,
        ),
        SpaceCard(
            "teleskop", txt("Teleskop"),
            txt("Med eit teleskop kan vi sjå ting som er veldig langt borte.", "Med et teleskop kan vi se ting som er veldig langt borte."),
            CardArt.Emoji("🔭".asEmoji()), 96,
        ),
        SpaceCard(
            "nordlys", txt("Nordlys"),
            txt("Nordlys kjem når små partiklar frå sola treffer lufta høgt over oss.", "Nordlys kommer når små partikler fra solen treffer lufta høyt over oss."),
            CardArt.Special(SpecialArt.AURORA), 107,
        ),
        SpaceCard(
            "uranus", txt("Uranus"),
            txt("Uranus ligg på sida og trillar rundt sola som ein ball.", "Uranus ligger på siden og triller rundt solen som en ball."),
            CardArt.Planet(PlanetLook(0xFF8FE3E8, 0xFFD5F8FA, 0xFF3F9AA3, ring = true)), 118,
        ),
        SpaceCard(
            "satellitt", txt("Satellitt"),
            txt("Satellittar går rundt jorda. Dei hjelper oss med vêrmelding og kart.", "Satellitter går rundt jorden. De hjelper oss med værmelding og kart."),
            CardArt.Emoji("🛰".asEmoji()), 130,
        ),
        SpaceCard(
            "neptun", txt("Neptun"),
            txt("Neptun er planeten lengst frå sola. Der blæs dei sterkaste vindane i solsystemet.", "Neptun er planeten lengst fra solen. Der blåser de sterkeste vindene i solsystemet."),
            CardArt.Planet(PlanetLook(0xFF4E6CF2, 0xFFA9B9FF, 0xFF22348F, bands = true)), 142,
        ),
        SpaceCard(
            "karlsvogna", txt("Karlsvogna", "Karlsvognen"),
            txt("Karlsvogna er sju stjerner som saman ser ut som ei stor øse.", "Karlsvognen er sju stjerner som sammen ser ut som en stor øse."),
            CardArt.Special(SpecialArt.BIG_DIPPER), 155,
        ),
        SpaceCard(
            "asteroide", txt("Asteroide"),
            txt("Asteroidar er steinar som går rundt sola. Dei fleste er mellom Mars og Jupiter.", "Asteroider er steiner som går rundt solen. De fleste er mellom Mars og Jupiter."),
            CardArt.Special(SpecialArt.ASTEROID), 168,
        ),
        SpaceCard(
            "pluto", txt("Pluto"),
            txt("Pluto er ein dvergplanet. Han har ein stor lys flekk som ser ut som eit hjarte.", "Pluto er en dvergplanet. Den har en stor lys flekk som ser ut som et hjerte."),
            CardArt.Planet(PlanetLook(0xFFD9B99B, 0xFFF5E3D0, 0xFF8C6A4E, craters = true)), 182,
        ),
        SpaceCard(
            "mjolkevegen", txt("Mjølkevegen", "Melkeveien"),
            txt("Mjølkevegen er galaksen vår. Han har over hundre milliardar stjerner.", "Melkeveien er galaksen vår. Den har over hundre milliarder stjerner."),
            CardArt.Emoji("🌌".asEmoji()), 196,
        ),
        SpaceCard(
            "manelanding", txt("Månelandinga", "Månelandingen"),
            txt("I 1969 gjekk dei første menneska på månen. Fotspora deira er der framleis.", "I 1969 gikk de første menneskene på månen. Fotsporene deres er der fortsatt."),
            CardArt.Special(SpecialArt.MOON_LANDING), 211,
        ),
        SpaceCard(
            "titan", txt("Titan"),
            txt("Titan er den største månen til Saturn. Der regnar det metan i staden for vatn.", "Titan er den største månen til Saturn. Der regner det metan i stedet for vann."),
            CardArt.Planet(PlanetLook(0xFFE0A84A, 0xFFFFD99A, 0xFF9A6A1E)), 226,
        ),
        SpaceCard(
            "svarthol", txt("Svart hol", "Svart hull"),
            txt("Eit svart hol dreg alt inn i seg. Ikkje ein gong lys kjem ut att.", "Et svart hull trekker alt inn i seg. Ikke engang lys kommer ut igjen."),
            CardArt.Special(SpecialArt.BLACK_HOLE), 242,
        ),
    )

    fun unlockedCount(totalStars: Int): Int = all.count { it.cost <= totalStars }

    fun next(totalStars: Int): SpaceCard? = all.firstOrNull { it.cost > totalStars }
}
