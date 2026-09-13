package app.komet.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Komet's own 24-unit icon family: rounded 2.2 strokes, drawn in code like Spole's icons so the
 * app carries one visual language and no icon library.
 */
object KometIcons {
    private fun icon(name: String, filled: Boolean = false, draw: PathBuilder.() -> Unit): ImageVector =
        ImageVector.Builder(name = "Komet.$name", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
            .apply {
                if (filled) {
                    path(fill = SolidColor(Color.Black), pathBuilder = draw)
                } else {
                    path(
                        fill = null,
                        stroke = SolidColor(Color.Black),
                        strokeLineWidth = 2.2f,
                        strokeLineCap = StrokeCap.Round,
                        strokeLineJoin = StrokeJoin.Round,
                        pathBuilder = draw,
                    )
                }
            }
            .build()

    private fun PathBuilder.circle(cx: Float, cy: Float, r: Float) {
        moveTo(cx - r, cy)
        arcToRelative(r, r, 0f, true, true, 2 * r, 0f)
        arcToRelative(r, r, 0f, true, true, -2 * r, 0f)
        close()
    }

    val Close = icon("Close") {
        moveTo(6.5f, 6.5f); lineTo(17.5f, 17.5f)
        moveTo(17.5f, 6.5f); lineTo(6.5f, 17.5f)
    }

    val Back = icon("Back") {
        moveTo(15f, 5f); lineTo(8f, 12f); lineTo(15f, 19f)
    }

    val Speaker = icon("Speaker") {
        moveTo(4f, 9.5f); lineTo(7.5f, 9.5f); lineTo(12f, 5.5f); lineTo(12f, 18.5f); lineTo(7.5f, 14.5f); lineTo(4f, 14.5f); close()
        moveTo(15.5f, 9f); quadTo(17.5f, 12f, 15.5f, 15f)
        moveTo(18.5f, 6.5f); quadTo(22.5f, 12f, 18.5f, 17.5f)
    }

    val Lock = icon("Lock") {
        moveTo(6f, 11f); lineTo(18f, 11f); lineTo(18f, 20f); lineTo(6f, 20f); close()
        moveTo(8.5f, 11f); lineTo(8.5f, 8f)
        arcToRelative(3.5f, 3.5f, 0f, false, true, 7f, 0f)
        lineTo(15.5f, 11f)
    }

    val Check = icon("Check") {
        moveTo(5f, 12.5f); lineTo(10f, 17.5f); lineTo(19f, 7f)
    }

    val Backspace = icon("Backspace") {
        moveTo(9f, 5.5f); lineTo(20f, 5.5f); lineTo(20f, 18.5f); lineTo(9f, 18.5f); lineTo(3.5f, 12f); close()
        moveTo(12.5f, 9.5f); lineTo(16.5f, 14.5f)
        moveTo(16.5f, 9.5f); lineTo(12.5f, 14.5f)
    }

    val Settings = icon("Settings") {
        moveTo(4f, 7f); lineTo(20f, 7f)
        moveTo(4f, 12f); lineTo(20f, 12f)
        moveTo(4f, 17f); lineTo(20f, 17f)
        circle(9f, 7f, 1.6f)
        circle(15f, 12f, 1.6f)
        circle(8f, 17f, 1.6f)
    }

    val Play = icon("Play", filled = true) {
        moveTo(8f, 5f); lineTo(19.5f, 12f); lineTo(8f, 19f); close()
    }

    val Bulb = icon("Bulb") {
        moveTo(9.5f, 17.5f); lineTo(14.5f, 17.5f)
        moveTo(10.5f, 20.5f); lineTo(13.5f, 20.5f)
        moveTo(9.5f, 17.5f); curveTo(9.5f, 15.5f, 6f, 13.5f, 6f, 9.5f)
        arcToRelative(6f, 6f, 0f, false, true, 12f, 0f)
        curveTo(18f, 13.5f, 14.5f, 15.5f, 14.5f, 17.5f)
    }

    val Cards = icon("Cards") {
        moveTo(4f, 8f); lineTo(14f, 8f); lineTo(14f, 20f); lineTo(4f, 20f); close()
        moveTo(8f, 5f); lineTo(8f, 4f); lineTo(20f, 4f); lineTo(20f, 17f); lineTo(17f, 17f)
    }

    val Chart = icon("Chart") {
        moveTo(5f, 20f); lineTo(5f, 12f)
        moveTo(12f, 20f); lineTo(12f, 5f)
        moveTo(19f, 20f); lineTo(19f, 14f)
    }

    val Person = icon("Person") {
        circle(12f, 8f, 4f)
        moveTo(4.5f, 20.5f)
        arcToRelative(7.5f, 7.5f, 0f, false, true, 15f, 0f)
    }

    val Plus = icon("Plus") {
        moveTo(12f, 5f); lineTo(12f, 19f)
        moveTo(5f, 12f); lineTo(19f, 12f)
    }

    val Trash = icon("Trash") {
        moveTo(4.5f, 7f); lineTo(19.5f, 7f)
        moveTo(9.5f, 7f); lineTo(9.5f, 4.5f); lineTo(14.5f, 4.5f); lineTo(14.5f, 7f)
        moveTo(6.5f, 7f); lineTo(7.5f, 20f); lineTo(16.5f, 20f); lineTo(17.5f, 7f)
    }

    val Refresh = icon("Refresh") {
        moveTo(19.5f, 12f)
        arcToRelative(7.5f, 7.5f, 0f, true, true, -2.2f, -5.3f)
        moveTo(19.5f, 4f); lineTo(19.5f, 8.5f); lineTo(15f, 8.5f)
    }

    val Flame = icon("Flame", filled = true) {
        moveTo(12.5f, 2f)
        curveTo(13.5f, 6f, 18.5f, 8f, 18.5f, 14f)
        arcToRelative(6.5f, 6.5f, 0f, false, true, -13f, 0f)
        curveTo(5.5f, 11f, 7f, 9f, 8.5f, 7.5f)
        curveTo(8.5f, 9.5f, 9.5f, 11f, 10.5f, 11f)
        curveTo(10.5f, 7.5f, 10.5f, 5f, 12.5f, 2f)
        close()
    }

    val Home = icon("Home") {
        moveTo(3.5f, 11f); lineTo(12f, 4f); lineTo(20.5f, 11f)
        moveTo(6f, 9.5f); lineTo(6f, 20f); lineTo(18f, 20f); lineTo(18f, 9.5f)
    }

    val Swap = icon("Swap") {
        moveTo(6f, 8f); lineTo(18f, 8f)
        moveTo(15f, 5f); lineTo(18f, 8f); lineTo(15f, 11f)
        moveTo(18f, 16f); lineTo(6f, 16f)
        moveTo(9f, 13f); lineTo(6f, 16f); lineTo(9f, 19f)
    }

    val Rocket = icon("Rocket") {
        moveTo(12f, 2.5f)
        curveTo(15.5f, 5f, 16.5f, 9f, 16f, 14f)
        lineTo(8f, 14f)
        curveTo(7.5f, 9f, 8.5f, 5f, 12f, 2.5f)
        close()
        circle(12f, 8.5f, 1.6f)
        moveTo(8f, 12f); lineTo(5f, 16f); lineTo(8f, 16f)
        moveTo(16f, 12f); lineTo(19f, 16f); lineTo(16f, 16f)
        moveTo(10.5f, 17f); lineTo(12f, 21f); lineTo(13.5f, 17f)
    }

    val Info = icon("Info") {
        circle(12f, 12f, 9f)
        moveTo(12f, 11f); lineTo(12f, 16.5f)
        moveTo(12f, 7.6f); lineTo(12f, 7.8f)
    }

    val Star = icon("Star", filled = true) { starPath(12f, 12.5f, 10.5f, 4.6f) }

    val Stop = icon("Stop") {
        moveTo(7f, 7f); lineTo(17f, 7f); lineTo(17f, 17f); lineTo(7f, 17f); close()
    }
}

/** Five-pointed star around ([cx], [cy]) pointing up. Shared by icons and canvas art. */
fun PathBuilder.starPath(cx: Float, cy: Float, outer: Float, inner: Float) {
    for (i in 0 until 10) {
        val radius = if (i % 2 == 0) outer else inner
        val angle = -PI / 2 + i * PI / 5
        val x = cx + (radius * cos(angle)).toFloat()
        val y = cy + (radius * sin(angle)).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}
