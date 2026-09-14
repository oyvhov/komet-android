package app.komet.domain

/** Where a bought thing goes: on the astronaut, on Bolt or on the rocket. */
enum class ShopSlot(val title: Txt) {
    HELMET(txt("Hjelm")),
    VISOR(txt("Visir")),
    PATTERN(txt("Mønster")),
    PACK(txt("På ryggen")),
    ANTENNA(txt("Antenne")),
    BADGE(txt("Pynt")),
    BOLT(txt("Bolt")),
    ROCKET(txt("Rakett")),
}

data class ShopItem(val id: String, val slot: ShopSlot, val name: Txt, val price: Int)

/** Everything the shop sells. Ids are stored in profiles, so an id must never change meaning. */
object Shop {
    val items: List<ShopItem> = listOf(
        ShopItem("helmet_stripe", ShopSlot.HELMET, txt("Racingstripe"), 12),
        ShopItem("helmet_fins", ShopSlot.HELMET, txt("Dragekam"), 30),
        ShopItem("helmet_gold", ShopSlot.HELMET, txt("Gullhjelm"), 45),
        ShopItem("helmet_crystal", ShopSlot.HELMET, txt("Krystallhjelm"), 60),

        ShopItem("visor_mint", ShopSlot.VISOR, txt("Grønt visir"), 8),
        ShopItem("visor_red", ShopSlot.VISOR, txt("Raudt visir", "Rødt visir"), 8),
        ShopItem("visor_gold", ShopSlot.VISOR, txt("Gullvisir"), 25),
        ShopItem("visor_rainbow", ShopSlot.VISOR, txt("Regnbogevisir", "Regnbuevisir"), 35),

        ShopItem("pattern_stripes", ShopSlot.PATTERN, txt("Striper"), 10),
        ShopItem("pattern_stars", ShopSlot.PATTERN, txt("Stjerner"), 15),
        ShopItem("pattern_lightning", ShopSlot.PATTERN, txt("Lyn"), 20),
        ShopItem("pattern_flames", ShopSlot.PATTERN, txt("Flammar", "Flammer"), 25),
        ShopItem("pattern_galaxy", ShopSlot.PATTERN, txt("Galaksedrakt"), 50),

        ShopItem("pack_cape", ShopSlot.PACK, txt("Kappe"), 20),
        ShopItem("pack_jet", ShopSlot.PACK, txt("Jetpakke"), 40),
        ShopItem("pack_wings", ShopSlot.PACK, txt("Robotvengjer", "Robotvinger"), 55),

        ShopItem("antenna_star", ShopSlot.ANTENNA, txt("Stjerneantenne"), 8),
        ShopItem("antenna_heart", ShopSlot.ANTENNA, txt("Hjarteantenne", "Hjerteantenne"), 8),
        ShopItem("antenna_double", ShopSlot.ANTENNA, txt("Dobbel antenne"), 12),
        ShopItem("antenna_propeller", ShopSlot.ANTENNA, txt("Propell"), 25),

        ShopItem("badge_bowtie", ShopSlot.BADGE, txt("Sløyfe"), 8),
        ShopItem("badge_medal", ShopSlot.BADGE, txt("Medalje"), 15),
        ShopItem("badge_scarf", ShopSlot.BADGE, txt("Skjerf"), 18),
        ShopItem("badge_shades", ShopSlot.BADGE, txt("Solbriller"), 22),

        ShopItem("bolt_pink", ShopSlot.BOLT, txt("Rosa Bolt"), 12),
        ShopItem("bolt_green", ShopSlot.BOLT, txt("Grøn Bolt", "Grønn Bolt"), 12),
        ShopItem("bolt_gold", ShopSlot.BOLT, txt("Gull-Bolt"), 30),
        ShopItem("bolt_night", ShopSlot.BOLT, txt("Natt-Bolt"), 35),

        ShopItem("rocket_blue", ShopSlot.ROCKET, txt("Blå rakett"), 10),
        ShopItem("rocket_green", ShopSlot.ROCKET, txt("Grøn rakett", "Grønn rakett"), 10),
        ShopItem("rocket_gold", ShopSlot.ROCKET, txt("Gullrakett"), 30),
        ShopItem("rocket_stripes", ShopSlot.ROCKET, txt("Regnbogerakett", "Regnbuerakett"), 40),
    )

    private val byId = items.associateBy { it.id }

    fun item(id: String): ShopItem? = byId[id]

    fun items(slot: ShopSlot): List<ShopItem> = items.filter { it.slot == slot }
}

sealed interface Purchase {
    data class Bought(val profile: Profile, val item: ShopItem) : Purchase
    data class NotEnough(val missing: Int) : Purchase
    data object AlreadyOwned : Purchase
    data object Unknown : Purchase
}

/**
 * Gold nuggets: earned by playing and spent in the shop. Only ever play money – nothing here costs or
 * earns real money.
 */
object Wallet {
    /** What every new astronaut starts with, enough for something small straight away. */
    const val WELCOME = 15

    /** Extra for reaching today's mission. */
    const val DAILY_BONUS = 5

    /** Extra for a new record in the rocket race. */
    const val RECORD_BONUS = 3

    /** A nugget picked up on a planet. */
    const val PICKUP = 1

    /** A profile made before nuggets existed gets the welcome sum and a share of its stars. */
    fun startingSum(totalStars: Int): Int = WELCOME + (totalStars / 2).coerceIn(0, 45)

    /** One nugget per star in the round, and the daily bonus when the mission was reached now. */
    fun roundReward(stars: Int, goalReachedNow: Boolean): Int = stars.coerceIn(0, 3) + if (goalReachedNow) DAILY_BONUS else 0

    fun buy(profile: Profile, itemId: String): Purchase {
        val item = Shop.item(itemId) ?: return Purchase.Unknown
        if (itemId in profile.owned) return Purchase.AlreadyOwned
        if (profile.nuggets < item.price) return Purchase.NotEnough(item.price - profile.nuggets)
        // A new thing is put on at once: that is what the child bought it for.
        return Purchase.Bought(
            profile.copy(
                nuggets = profile.nuggets - item.price,
                owned = profile.owned + itemId,
                equipped = profile.equipped + (item.slot to itemId),
            ),
            item,
        )
    }

    /** Puts on something the child owns; anything else leaves the profile as it was. */
    fun equip(profile: Profile, itemId: String): Profile {
        val item = Shop.item(itemId) ?: return profile
        if (itemId !in profile.owned) return profile
        return profile.copy(equipped = profile.equipped + (item.slot to itemId))
    }

    fun unequip(profile: Profile, slot: ShopSlot): Profile = profile.copy(equipped = profile.equipped - slot)

    /** Levels with a nugget waiting on the planet: finished at least once and not picked up yet. */
    fun pickups(profile: Profile, subject: Subject): List<Skill> =
        Curriculum.skills(subject).filter { profile.stars(it.id) > 0 && it.id !in profile.collectedNuggets }

    /** Picks up the nugget by a finished level, once. Returns null when there is none to take. */
    fun collect(profile: Profile, skillId: String): Profile? {
        if (skillId in profile.collectedNuggets || profile.stars(skillId) <= 0) return null
        return profile.copy(nuggets = profile.nuggets + PICKUP, collectedNuggets = profile.collectedNuggets + skillId)
    }
}
