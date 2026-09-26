package app.komet.domain

/** Small, self-contained works. There are no points or deadlines in this workshop. */
object MontessoriMaterials {
    val rods = (1..5).toList()
    val spindleLabels = (0..5).toList()
    const val spindleCount = 15

    fun rodsInOrder(placed: List<Int>): Boolean =
        placed.size == rods.size && placed.indices.all { placed[it] == rods[it] }

    fun remainingSpindles(boxes: List<Int>): Int =
        (spindleCount - boxes.sum()).coerceAtLeast(0)

    fun spindlesMatch(boxes: List<Int>): Boolean =
        boxes.size == spindleLabels.size &&
            boxes.all { it >= 0 } &&
            boxes.sum() == spindleCount &&
            boxes.indices.all { boxes[it] == spindleLabels[it] }

    fun canAddSpindle(boxes: List<Int>, index: Int): Boolean =
        index in spindleLabels.indices && boxes.size == spindleLabels.size &&
            boxes[index] < 9 && remainingSpindles(boxes) > 0
}
