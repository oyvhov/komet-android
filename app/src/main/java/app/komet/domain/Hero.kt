package app.komet.domain

/** How the child's astronaut looks. Each field is an index into [HeroPalette]. */
data class HeroLook(
    val suit: Int = 0,
    val skin: Int = 1,
    val hair: Int = 1,
    val hairStyle: Int = 0,
) {
    /** The same look with every index inside its palette, for data read from disk. */
    fun safe(): HeroLook = HeroLook(
        suit = suit.mod(HeroPalette.suits.size),
        skin = skin.mod(HeroPalette.skins.size),
        hair = hair.mod(HeroPalette.hairs.size),
        hairStyle = hairStyle.mod(HeroPalette.HAIR_STYLES),
    )

    companion object {
        /** A look for a profile made before astronauts existed, based on the figure it had chosen. */
        fun fromAvatar(avatar: Int): HeroLook = HeroLook(suit = avatar.mod(HeroPalette.suits.size), skin = 1, hair = avatar.mod(3), hairStyle = avatar.mod(HeroPalette.HAIR_STYLES))
    }
}

object HeroPalette {
    /** Suit accents: deep and saturated, like the rest of Komet. */
    val suits: List<Long> = listOf(0xFFF26A0F, 0xFF1684E6, 0xFF23A049, 0xFF6C3FF0, 0xFFE02A50, 0xFFE39400, 0xFF0FA982, 0xFFD63F9C)
    val skins: List<Long> = listOf(0xFFFFE3CF, 0xFFF4C7A1, 0xFFDDA57A, 0xFFB97A4E, 0xFF8D5A34, 0xFF5E3A22)
    val hairs: List<Long> = listOf(0xFF2A1A12, 0xFF6B3F21, 0xFFB8732F, 0xFFEBC36A, 0xFFC9481F, 0xFF1D2140)
    const val HAIR_STYLES = 4
}
