package app.komet.domain

import org.junit.Test
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Draws every writing glyph with numbered strokes and direction arrows to build/reports/strokes, so a
 * person can check the stroke order against stavskrift without starting the app.
 */
class StrokeSheetExport {

    private val palette = listOf(Color(0xE0245E), Color(0x1F77D0), Color(0x2E9E4F), Color(0xD9822B), Color(0x8E44AD), Color(0x16A2A2))

    @Test
    fun `export stroke sheets`() {
        val dir = File("build/reports/strokes").apply { mkdirs() }
        sheet(File(dir, "capitals.png"), WordBank.alphabet)
        sheet(File(dir, "smalls.png"), WordBank.alphabet.map { it.lowerNo() })
        sheet(File(dir, "digits.png"), (0..9).map { it.toString() })
    }

    private fun sheet(file: File, symbols: List<String>) {
        val columns = 8
        val cellW = 230
        val cellH = 330
        val rows = (symbols.size + columns - 1) / columns
        val image = BufferedImage(columns * cellW, rows * cellH, BufferedImage.TYPE_INT_RGB)
        val g = image.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.color = Color.WHITE
        g.fillRect(0, 0, image.width, image.height)
        val scale = 1.7f
        symbols.forEachIndexed { index, symbol ->
            val glyph = Strokes.glyph(symbol) ?: return@forEachIndexed
            val ox = (index % columns) * cellW + cellW / 2f - (glyph.minX + glyph.maxX) / 2f * scale
            val oy = (index / columns) * cellH + 60f
            fun px(p: Pt) = (ox + p.x * scale) to (oy + p.y * scale)
            g.color = Color(0xCCCCDD)
            g.stroke = BasicStroke(1f)
            val lines = if (glyph.zone == TraceZone.CAPITAL) listOf(0f, 50f, 100f) else listOf(0f, 50f, 100f, 150f)
            lines.forEach { y ->
                val yy = (oy + y * scale).toInt()
                g.drawLine((index % columns) * cellW + 8, yy, (index % columns) * cellW + cellW - 8, yy)
            }
            g.color = Color(0x333344)
            g.font = Font("SansSerif", Font.BOLD, 20)
            g.drawString(symbol, (index % columns) * cellW + 10, (index / columns) * cellH + 24)
            glyph.strokes.forEachIndexed { strokeIndex, stroke ->
                val color = palette[strokeIndex % palette.size]
                g.color = color
                if (stroke.isDot) {
                    val (x, y) = px(stroke.start)
                    g.fillOval((x - 7).toInt(), (y - 7).toInt(), 14, 14)
                } else {
                    val path = Path2D.Float()
                    stroke.points.forEachIndexed { i, p ->
                        val (x, y) = px(p)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    g.stroke = BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
                    g.draw(path)
                    // Arrow heads along the stroke show the writing direction.
                    var d = 22f
                    while (d < stroke.length - 6f) {
                        val p = stroke.pointAt(d)
                        val dir = stroke.directionAt(d)
                        val (x, y) = px(p)
                        val bx = x - dir.x * 9
                        val by = y - dir.y * 9
                        g.stroke = BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
                        g.drawLine(bx.toInt() + (-dir.y * 6).toInt(), by.toInt() + (dir.x * 6).toInt(), x.toInt(), y.toInt())
                        g.drawLine(bx.toInt() - (-dir.y * 6).toInt(), by.toInt() - (dir.x * 6).toInt(), x.toInt(), y.toInt())
                        d += 34f
                    }
                }
                val (sx, sy) = px(stroke.start)
                g.color = color
                g.fillOval((sx - 12).toInt(), (sy - 12).toInt(), 24, 24)
                g.color = Color.WHITE
                g.font = Font("SansSerif", Font.BOLD, 15)
                g.drawString("${strokeIndex + 1}", sx - 4.5f, sy + 5.5f)
            }
        }
        g.dispose()
        ImageIO.write(image, "png", file)
    }
}
