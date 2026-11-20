package app.komet.domain

enum class PostCargo(val word: String, val singular: Txt, val plural: Txt) {
    STAR("lum", Txt("stjerne"), Txt("stjerner")),
    SEED("pip", Txt("frø"), Txt("frø")),
    CRYSTAL("vek", Txt("krystall"), Txt("krystallar", "krystaller")),
}

fun postNumber(count: Int): String = when (count) {
    1 -> "ka"
    2 -> "tiri"
    3 -> "moro"
    else -> error("Unknown parcel quantity: $count")
}

data class PostParcel(val count: Int, val cargo: PostCargo) {
    val code: String get() = "${postNumber(count)} ${cargo.word}"
    val label: Txt get() = (if (count == 1) cargo.singular else cargo.plural).map { "$count $it" }
}

enum class PostJob { PACK, WRITE }

data class PostOrder(val job: PostJob, val target: PostParcel, val examples: List<PostParcel>) {
    val amounts = examples.map { it.count }.distinct().sorted()
    val contents = examples.map { it.cargo }.distinct().sorted()
    fun matches(count: Int?, cargo: PostCargo?) = count == target.count && cargo == target.cargo
}

/** The opening orders isolate one word. Later orders require combining words from different labels. */
val postOrders: List<PostOrder> = buildList {
    fun stars(n: Int) = PostParcel(n, PostCargo.STAR)
    fun seeds(n: Int) = PostParcel(n, PostCargo.SEED)
    fun gems(n: Int) = PostParcel(n, PostCargo.CRYSTAL)
    fun order(job: PostJob, target: PostParcel, vararg examples: PostParcel) =
        add(PostOrder(job, target, examples.toList()))
    order(PostJob.PACK, stars(2), stars(1), stars(2))
    order(PostJob.WRITE, seeds(1), gems(1), seeds(1))
    order(PostJob.PACK, seeds(2), stars(2), seeds(1))
    order(PostJob.WRITE, gems(1), gems(2), seeds(1))
    order(PostJob.PACK, gems(3), seeds(3), gems(1))
    order(PostJob.WRITE, stars(3), stars(2), gems(3))
    order(PostJob.PACK, stars(1), stars(3), seeds(1), gems(2))
    order(PostJob.WRITE, gems(2), seeds(2), gems(3), stars(1))
    order(PostJob.PACK, seeds(3), seeds(1), stars(2), gems(3))
    order(PostJob.WRITE, stars(2), stars(1), gems(2), seeds(3))
}
