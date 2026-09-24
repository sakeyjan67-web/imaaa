package com.example.imageengine

import android.graphics.Bitmap
import kotlin.math.sqrt

class ImageEnhancementEngine {

    enum class Mode { PREMIUM, CINEMATIC, NIGHT, PORTRAIT, HDR, GOLDEN, COOL, NATURAL, VIVID }

    data class Profile(
        val exposure: Float = 1.18f,
        val contrast: Float = 1.30f,
        val saturation: Float = 1.28f,
        val sharpness: Float = 0.85f,
        val denoise: Float = 0.55f,
        val warmth: Float = 0.08f,
        val hdrStrength: Float = 0.70f,
        val highlightRecovery: Float = 0.60f,
        val vibrance: Float = 1.15f,
        val shadowLift: Float = 0.35f,
        val temperature: Float = 0.0f,
        val tint: Float = 0.0f,
        val vignette: Float = 0.10f,
        val grain: Float = 0.06f,
        val highlightBoost: Float = 0.20f
    ) {
        companion object {
            val PREMIUM = Profile(1.24f, 1.42f, 1.38f, 1.0f, .72f, .12f, .82f, .78f, 1.20f, .42f, .18f, .08f, .12f, .04f, .24f)
            val CINEMATIC = Profile(1.18f, 1.58f, 1.28f, .95f, .70f, .22f, .92f, .88f, 1.28f, .52f, .35f, .12f, .18f, .08f, .30f)
            val NIGHT = Profile(1.34f, 1.18f, 1.10f, .72f, .88f, .04f, .62f, .66f, 1.05f, .66f, -.18f, .04f, .26f, .12f, .18f)
            val PORTRAIT = Profile(1.12f, 1.25f, 1.18f, 1.10f, .52f, .10f, .48f, .58f, 1.15f, .38f, .10f, .06f, .12f, .05f, .20f)
            val HDR = Profile(1.38f, 1.65f, 1.42f, 1.08f, .60f, .08f, 1.0f, .92f, 1.32f, .58f, .12f, .08f, .14f, .04f, .28f)
            val GOLDEN = Profile(1.22f, 1.48f, 1.46f, .90f, .60f, .42f, .75f, .70f, 1.36f, .46f, .72f, .18f, .10f, .06f, .42f)
            val COOL = Profile(1.16f, 1.36f, 1.18f, .82f, .66f, -.18f, .68f, .62f, 1.12f, .30f, -.48f, -.16f, .10f, .05f, .18f)
            val NATURAL = Profile(1.08f, 1.12f, 1.10f, .60f, .35f, .05f, .32f, .40f, 1.0f, .28f, .04f, .02f, .06f, .03f, .10f)
            val VIVID = Profile(1.30f, 1.60f, 1.60f, 1.12f, .56f, .18f, .88f, .82f, 1.48f, .44f, .20f, .10f, .08f, .04f, .34f)
        }
    }

    fun enhance(bitmap: Bitmap, mode: Mode = Mode.PREMIUM): Bitmap = enhance(bitmap, profileFor(mode))

    fun enhance(bitmap: Bitmap, profile: Profile): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val source = IntArray(width * height)
        bitmap.getPixels(source, 0, width, 0, 0, width, height)
        val denoised = applyDenoise(source, width, height, profile.denoise)
        val base = IntArray(source.size)

        var sumLuma = 0f
        for (pixel in source) {
            val r = ((pixel shr 16) and 0xFF).toFloat()
            val g = ((pixel shr 8) and 0xFF).toFloat()
            val b = (pixel and 0xFF).toFloat()
            sumLuma += 0.299f * r + 0.587f * g + 0.114f * b
        }
        val averageLuma = sumLuma / source.size.coerceAtLeast(1).toFloat()

        for (idx in source.indices) {
            val src = source[idx]
            val r = ((src shr 16) and 0xFF).toFloat()
            val g = ((src shr 8) and 0xFF).toFloat()
            val b = (src and 0xFF).toFloat()
            val denR = ((denoised[idx] shr 16) and 0xFF).toFloat()
            val denG = ((denoised[idx] shr 8) and 0xFF).toFloat()
            val denB = (denoised[idx] and 0xFF).toFloat()
            val luma = 0.299f * r + 0.587f * g + 0.114f * b
            val exposureGain = profile.exposure * (128f / (averageLuma + 8f))
            var rr = r * exposureGain
            var gg = g * exposureGain
            var bb = b * exposureGain
            val warmthBoost = profile.warmth * 28f
            rr += warmthBoost * (1f - g / 255f)
            gg *= 1f - profile.warmth * .18f
            bb *= 1f + profile.warmth * .30f
            val neutral = .299f * rr + .587f * gg + .114f * bb
            rr = neutral + (rr - neutral) * profile.saturation * profile.vibrance
            gg = neutral + (gg - neutral) * profile.saturation * profile.vibrance
            bb = neutral + (bb - neutral) * profile.saturation * profile.vibrance
            val shadowLift = profile.shadowLift * (1f - luma / 255f)
            rr += (128f - rr) * shadowLift
            gg += (128f - gg) * shadowLift
            bb += (128f - bb) * shadowLift
            val hdrLift = profile.hdrStrength * (1f - luma / 255f)
            rr += (rr - neutral) * hdrLift
            gg += (gg - neutral) * hdrLift
            bb += (bb - neutral) * hdrLift
            rr = (rr - 128f) * profile.contrast + 128f
            gg = (gg - 128f) * profile.contrast + 128f
            bb = (bb - 128f) * profile.contrast + 128f
            val highlightScale = 1f + profile.highlightRecovery * (1f - luma / 255f)
            rr *= highlightScale
            gg *= highlightScale
            bb *= highlightScale
            val sharpenR = rr + (rr - denR) * profile.sharpness
            val sharpenG = gg + (gg - denG) * profile.sharpness
            val sharpenB = bb + (bb - denB) * profile.sharpness
            base[idx] = argb(255, sharpenR, sharpenG, sharpenB)
        }

        val graded = applyColorGrading(base, width, height, profile)
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also {
            it.setPixels(graded, 0, width, 0, 0, width, height)
        }
    }

    private fun applyColorGrading(pixels: IntArray, width: Int, height: Int, profile: Profile): IntArray {
        val out = IntArray(pixels.size)
        val temp = profile.temperature.coerceIn(-1f, 1f)
        val tint = profile.tint.coerceIn(-1f, 1f)
        val vignette = profile.vignette.coerceIn(0f, 1f)
        val grain = profile.grain.coerceIn(0f, 1f)
        for (y in 0 until height) for (x in 0 until width) {
            val idx = y * width + x
            val pixel = pixels[idx]
            var r = ((pixel shr 16) and 0xFF).toFloat()
            var g = ((pixel shr 8) and 0xFF).toFloat()
            var b = (pixel and 0xFF).toFloat()
            if (temp > 0f) { r += temp * 36f; b -= temp * 22f }
            else { b += -temp * 34f; r -= -temp * 18f }
            g += tint * 20f
            val lum = .299f * r + .587f * g + .114f * b
            val highlight = ((lum - 160f).coerceAtLeast(0f) / 95f) * profile.highlightBoost
            val shadow = ((128f - lum).coerceAtLeast(0f) / 128f) * profile.shadowLift
            r += highlight * 30f + shadow * 18f
            g += highlight * 16f + shadow * 12f
            b += highlight * 26f + shadow * 22f
            val nx = (x.toFloat() / (width - 1).coerceAtLeast(1) - .5f) * 2f
            val ny = (y.toFloat() / (height - 1).coerceAtLeast(1) - .5f) * 2f
            val scale = (1f - sqrt(nx * nx + ny * ny) * vignette).coerceAtLeast(.35f)
            r *= scale; g *= scale; b *= scale
            val noise = (((x * 13 + y * 17 + idx) % 23) / 23f - .5f) * grain * 30f
            out[idx] = argb(255, r + noise, g + noise, b + noise)
        }
        return out
    }

    private fun profileFor(mode: Mode): Profile = when (mode) {
        Mode.PREMIUM -> Profile.PREMIUM
        Mode.CINEMATIC -> Profile.CINEMATIC
        Mode.NIGHT -> Profile.NIGHT
        Mode.PORTRAIT -> Profile.PORTRAIT
        Mode.HDR -> Profile.HDR
        Mode.GOLDEN -> Profile.GOLDEN
        Mode.COOL -> Profile.COOL
        Mode.NATURAL -> Profile.NATURAL
        Mode.VIVID -> Profile.VIVID
    }

    private fun applyDenoise(pixels: IntArray, width: Int, height: Int, amount: Float): IntArray {
        if (amount <= 0f) return pixels.copyOf()
        val strength = amount.coerceIn(0f, 1f)
        val output = IntArray(pixels.size)
        for (y in 0 until height) for (x in 0 until width) {
            val index = y * width + x
            var rSum = 0; var gSum = 0; var bSum = 0; var count = 0
            for (dy in -1..1) {
                val yy = y + dy
                if (yy !in 0 until height) continue
                for (dx in -1..1) {
                    val xx = x + dx
                    if (xx !in 0 until width) continue
                    val pixel = pixels[yy * width + xx]
                    rSum += pixel shr 16 and 0xFF
                    gSum += pixel shr 8 and 0xFF
                    bSum += pixel and 0xFF
                    count++
                }
            }
            val src = pixels[index]
            val sr = (src shr 16 and 0xFF).toFloat()
            val sg = (src shr 8 and 0xFF).toFloat()
            val sb = (src and 0xFF).toFloat()
            output[index] = argb(255, lerp(sr, rSum / count.toFloat(), strength), lerp(sg, gSum / count.toFloat(), strength), lerp(sb, bSum / count.toFloat(), strength))
        }
        return output
    }

    private fun argb(alpha: Int, r: Float, g: Float, b: Float): Int =
        (alpha shl 24) or (clamp(r) shl 16) or (clamp(g) shl 8) or clamp(b)

    private fun clamp(value: Float): Int = value.coerceIn(0f, 255f).toInt()

    private fun lerp(start: Float, end: Float, factor: Float): Float =
        start + (end - start) * factor
}
