package app.komet.ui.scene

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Path
import android.graphics.Region
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import app.komet.domain.SolarDestination
import app.komet.ui.components.SpaceCardArt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.*

private val globes = ConcurrentHashMap<SolarDestination, ImageBitmap>()

@Composable
internal fun SolarGlobe(destination: SolarDestination, modifier: Modifier) {
    val globe by produceState(globes[destination], destination) {
        value = withContext(Dispatchers.Default) { globes.computeIfAbsent(destination, ::renderGlobe) }
    }
    val image = globe
    if (image == null) SpaceCardArt(destination.planet.art, modifier)
    else Image(image, contentDescription = null, modifier = modifier)
}

// Coastlines in longitude and latitude, viewed over the Atlantic.
private val continents by lazy {
    val path = Path()
    fun land(vararg points: Int) {
        path.moveTo(points[0].toFloat(), points[1].toFloat())
        for (i in 2 until points.size step 2) path.lineTo(points[i].toFloat(), points[i + 1].toFloat())
        path.close()
    }
    land(-17,15, -13,29, -5,36, 10,37, 13,33, 32,31, 42,12, 51,12, 44,0, 40,-12, 32,-27, 20,-35, 16,-28, 12,-17, 9,-4, 9,4, -2,5, -10,6)
    land(-10,36, -9,43, -2,44, -5,49, 5,52, 10,55, 6,59, 17,70, 30,71, 26,60, 42,66, 73,72, 105,77, 160,66, 172,55, 125,34, 107,17, 103,3, 94,20, 77,8, 69,23, 58,23, 44,12, 34,29, 27,41, 20,38, 14,45, 9,44, 0,38)
    land(-81,9, -73,12, -60,9, -51,4, -35,-6, -39,-19, -49,-29, -58,-35, -68,-55, -75,-48, -72,-30, -78,-9)
    land(-165,65, -140,70, -95,74, -64,59, -55,52, -68,45, -80,31, -81,25, -87,30, -97,21, -85,15, -80,9, -91,15, -106,22, -124,43)
    land(-54,59, -65,68, -53,81, -26,83, -18,76, -38,65)
    land(-11,51, -6,58, 0,53)
    land(44,-25, 44,-19, 50,-13, 49,-21)
    Region().apply { setPath(path, Region(-180, -90, 181, 91)) }
}

private fun grain(x: Int, y: Int): Double {
    var hash = x * 374761393 + y * 668265263
    hash = (hash xor (hash ushr 13)) * 1274126177
    return (hash ushr 8) / 16777215.0
}

private fun noise(x: Double, y: Double): Double {
    val i = floor(x).toInt()
    val j = floor(y).toInt()
    fun smooth(t: Double) = t * t * (3 - 2 * t)
    val u = smooth(x - i)
    val v = smooth(y - j)
    val top = grain(i,j) * (1-u) + grain(i+1,j) * u
    val bottom = grain(i,j+1) * (1-u) + grain(i+1,j+1) * u
    return top * (1-v) + bottom * v
}

private fun tint(a: Int, b: Int, mix: Double, light: Double = 1.0): Int {
    val f = mix.coerceIn(0.0, 1.0)
    fun channel(shift: Int): Int {
        val lo = (a ushr shift) and 255
        val hi = (b ushr shift) and 255
        return ((lo + (hi - lo) * f) * light).roundToInt().coerceIn(0, 255)
    }
    return android.graphics.Color.rgb(channel(16), channel(8), channel(0))
}

private fun renderGlobe(planet: SolarDestination): ImageBitmap {
    val size = 240
    val ringed = planet == SolarDestination.SATURN || planet == SolarDestination.URANUS
    val radius = size * (if (planet == SolarDestination.SATURN) 0.28f else if (ringed) 0.385f else 0.455f)
    // Sample the surface separately from the silhouette, rings and lighting. Those fine edges
    // are painted at full resolution by Skia, even on older Android devices.
    val samples = 64
    val pixels = IntArray(samples * samples)
    for (row in 0 until samples) for (column in 0 until samples) {
        val x = (column + 0.5) * 2 / samples - 1
        val y = (row + 0.5) * 2 / samples - 1
        val z = sqrt(max(0.0, 1 - x*x - y*y))
        val lat = asin(-y)
        val lon = atan2(x,z)
        val rough = noise(lon*7,lat*11)*0.58 + noise(lon*21,lat*29)*0.30 + grain(column,row)*0.12
        val band = sin(lat*26 + 0.95*sin(lon*3 + lat*6) + rough*1.5)
        val surface = when (planet) {
            SolarDestination.MERCURY -> tint(0x665E5B, 0xE7D9C5, rough)
            SolarDestination.VENUS -> tint(0xC98F50, 0xFFE9B2, 0.54 + 0.21*band + 0.26*sin(lat*9 + lon*3 + rough))
            SolarDestination.EARTH -> {
                val land = continents.contains(Math.toDegrees(lon).toInt()-18, Math.toDegrees(lat).toInt())
                val base = if (land) tint(0x647855, 0xCEBB88, rough + 0.22*cos(lat*8))
                    else tint(0x154971, 0x408DC1, rough)
                val cloud = ((noise(lon*5+sin(lat*8),lat*15)*0.7 + rough*0.3) - 0.54) * 3.4
                tint(base, 0xF5F7F0, max(cloud,(abs(lat)-1.17)*3))
            }
            SolarDestination.MARS -> tint(0x863F32, 0xF8AE78, rough + 0.18*sin(lon*5 + lat*11))
            SolarDestination.JUPITER -> {
                val storm = ((x-0.28)/0.19).pow(2) + ((y-0.27)/0.077).pow(2)
                if (storm < 1) tint(0xA5533E, 0xD09772, storm)
                else tint(0xAD765C, 0xF2DBB6, 0.6 + 0.34*band + 0.13*sin(lat*63 + rough))
            }
            SolarDestination.SATURN -> tint(0xBD9B73, 0xF7E3B9, 0.54+0.15*band+rough*0.2)
            SolarDestination.URANUS -> tint(0x63A8B4, 0xC5EFEB, 0.56+0.07*band)
            SolarDestination.NEPTUNE -> tint(0x274D9A, 0x7CB7E3, 0.44+0.18*band+0.20*rough)
        }
        pixels[row*samples + column] = surface
    }

    val texture = Bitmap.createBitmap(pixels, samples, samples, Bitmap.Config.ARGB_8888)
    val result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    val c = size / 2f
    val body = RectF(c-radius, c-radius, c+radius, c+radius)
    fun rings(startAngle: Float) {
        if (!ringed) return
        canvas.save()
        canvas.rotate(-20f, c, c)
        paint.style = Paint.Style.STROKE
        val lanes = if (planet == SolarDestination.SATURN) 24 else 2
        for (i in 0 until lanes) {
            if (lanes == 24 && i in 16..17) continue
            val ring = radius * (1.18f + i*0.026f)
            paint.strokeWidth = radius * if (lanes == 24) .024f else .007f
            paint.color = if (lanes == 2) 0xAAA5D7D3.toInt()
                else tint(0x8C7567, 0xEFDCB8, .55 + .24*sin(i*1.6))
            canvas.drawArc(c-ring, c-ring*.29f, c+ring, c+ring*.29f, startAngle, 180f, false, paint)
        }
        paint.style = Paint.Style.FILL
        canvas.restore()
    }

    rings(180f)
    canvas.save()
    canvas.clipPath(Path().apply { addOval(body, Path.Direction.CW) })
    paint.color = Color.WHITE
    canvas.drawBitmap(texture, null, body, paint)
    paint.shader = RadialGradient(c-radius*.53f, c-radius*.50f, radius*1.86f,
        intArrayOf(0x2CFFF4D4, Color.TRANSPARENT, 0x44111824, 0xE3070E19.toInt()),
        floatArrayOf(0f,.37f,.65f,1f), Shader.TileMode.CLAMP)
    canvas.drawCircle(c,c,radius,paint)
    paint.shader = LinearGradient(c-radius,c-radius,c+radius,c+radius,
        0x28D8EEFF,0x00000000,Shader.TileMode.CLAMP)
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 1.6f
    canvas.drawCircle(c,c,radius-.8f,paint)
    paint.shader = null
    paint.style = Paint.Style.FILL
    canvas.restore()
    rings(0f)
    return result.asImageBitmap()
}
