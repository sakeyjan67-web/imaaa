package com.example.imageengine

import android.graphics.Bitmap
import kotlin.math.pow
import kotlin.math.sqrt

class ImageEnhancementEngine {

    enum class Mode {
        PREMIUM,
        CINEMATIC,
        NIGHT,
        PORTRAIT,
        HDR,
        GOLDEN,
        COOL,
        NATURAL,
        VIVID
    }

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
            val PREMIUM = Profile(
                exposure = 1.24f,
                contrast = 1.42f,
                saturation = 1.38f,
                sharpness = 1.0f,
                denoise = 0.72f,
                warmth = 0.12f,
                hdrStrength = 0.82f,
                highlightRecovery = 0.78f,
                vibrance = 1.20f,
                shadowLift = 0.42f,
                temperature = 0.18f,
                tint = 0.08f,
                vignette = 0.12f,
                grain = 0.04f,
                highlightBoost = 0.24f
            )

            val CINEMATIC = Profile(
                exposure = 1.18f,
                contrast = 1.58f,
                saturation = 1.28f,
                sharpness = 0.95f,
                denoise = 0.70f,
                warmth = 0.22f,
                hdrStrength = 0.92f,
                highlightRecovery = 0.88f,
                vibrance = 1.28f,
                shadowLift = 0.52f,
                temperature = 0.35f,
                tint = 0.12f,
                vignette = 0.18f,
                grain = 0.08f,
                highlightBoost = 0.30f
            )

            val NIGHT = Profile(
                exposure = 1.34f,
                contrast = 1.18f,
                saturation = 1.10f,
                sharpness = 0.72f,
                denoise = 0.88f,
                warmth = 0.04f,
                hdrStrength = 0.62f,
                highlightRecovery = 0.66f,
                vibrance = 1.05f,
                shadowLift = 0.66f,
                temperature = -0.18f,
                tint = 0.04f,
                vignette = 0.26f,
                grain = 0.12f,
                highlightBoost = 0.18f
            )

            val PORTRAIT = Profile(
                exposure = 1.12f,
                contrast = 1.25f,
                saturation = 1.18f,
                sharpness = 1.10f,
                denoise = 0.52f,
                warmth = 0.10f,
                hdrStrength = 0.48f,
                highlightRecovery = 0.58f,
                vibrance = 1.15f,
                shadowLift = 0.38f,
                temperature = 0.10f,
                tint = 0.06f,
                vignette = 0.12f,
                grain = 0.05f,
                highlightBoost = 0.20f
            )

            val HDR = Profile(
                exposure = 1.38f,
                contrast = 1.65f,
                saturation = 1.42f,
                sharpness = 1.08f,
                denoise = 0.60f,
                warmth = 0.08f,
                hdrStrength = 1.00f,
                highlightRecovery = 0.92f,
                vibrance = 1.32f,
                shadowLift = 0.58f,
                temperature = 0.12f,
                tint = 0.08f,
                vignette = 0.14f,
                grain = 0.04f,
                highlightBoost = 0.28f
            )

            val GOLDEN = Profile(
                exposure = 1.22f,
                contrast = 1.48f,
                saturation = 1.46f,
                sharpness = 0.90f,
                denoise = 0.60f,
                warmth = 0.42f,
                hdrStrength = 0.75f,
                highlightRecovery = 0.70f,
                vibrance = 1.36f,
                shadowLift = 0.46f,
                temperature = 0.72f,
                tint = 0.18f,
                vignette = 0.10f,
                grain = 0.06f,
                highlightBoost = 0.42f
            )

            val COOL = Profile(
                exposure = 1.16f,
                contrast = 1.36f,
                saturation = 1.18f,
                sharpness = 0.82f,
                denoise = 0.66f,
                warmth = -0.18f,
                hdrStrength = 0.68f,
                highlightRecovery = 0.62f,
                vibrance = 1.12f,
                shadowLift = 0.30f,
                temperature = -0.48f,
                tint = -0.16f,
                vignette = 0.10f,
                grain = 0.05f,
                highlightBoost = 0.18f
            )

            val NATURAL = Profile(
                exposure = 1.08f,
                contrast = 1.12f,
                saturation = 1.10f,
                sharpness = 0.60f,
                denoise = 0.35f,
                warmth = 0.05f,
                hdrStrength = 0.32f,
                highlightRecovery = 0.40f,
                vibrance = 1.00f,
                shadowLift = 0.28f,
                temperature = 0.04f,
                tint = 0.02f,
                vignette = 0.06f,
                grain = 0.03f,
                highlightBoost = 0.10f
            )

            val VIVID = Profile(
                exposure = 1.30f,
                contrast = 1.60f,
                saturation = 1.60f,
                sharpness = 1.12f,
                denoise = 0.56f,
                warmth = 0.18f,
                hdrStrength = 0.88f,
                highlightRecovery = 0.82f,
                vibrance = 1.48f,
                shadowLift = 0.44f,
                temperature = 0.20f,
                tint = 0.10f,
                vignette = 0.08f,
                grain = 0.04f,
                highlightBoost = 0.34f
            )
        }
    }

    fun enhance(bitmap: Bitmap, mode: Mode = Mode.PREMIUM): Bitmap {
        val profile = profileFor(mode)
        return enhance(bitmap, profile)
    }

    fun enhance(bitmap: Bitmap, profile: Profile): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val source = IntArray(width * height)
        bitmap.getPixels(source, 0, width, 0, 0, width, height)

        val denoised = applyDenoise(source, width, height, profile.denoise)
        val base = IntArray(source.size)

        var sumLuma = 0.0
        for (pixel in source) {
            val r = ((pixel shr 16) and 0xFF)
            val g = ((pixel shr 8) and 0xFF)
            val b = (pixel and 0xFF)
            sumLuma += 0.299 * r + 0.587 * g + 0.114 * b
        }
        val averageLuma = sumLuma / source.size.coerceAtLeast(1)

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
            rr += warmthBoost * (1.0f - (g / 255f))
            gg *= 1f - profile.warmth * 0.18f
            bb *= 1f + profile.warmth * 0.30f

            val neutral = 0.299f * rr + 0.587f * gg + 0.114f * bb
            rr = neutral + (rr - neutral) * profile.saturation * profile.vibrance
            gg = neutral + (gg - neutral) * profile.saturation * profile.vibrance
            bb = neutral + (bb - neutral) * profile.saturation * profile.vibrance

            val shadowLift = profile.shadowLift * (1.0f - luma / 255f)
            rr = rr + (128f - rr) * shadowLift
            gg = gg + (128f - gg) * shadowLift
            bb = bb + (128f - bb) * shadowLift

            val hdrLift = profile.hdrStrength * (1.0f - luma / 255f)
            rr = rr + (rr - neutral) * hdrLift
            gg = gg + (gg - neutral) * hdrLift
            bb = bb + (bb - neutral) * hdrLift

            rr = ((rr - 128f) * profile.contrast) + 128f
            gg = ((gg - 128f) * profile.contrast) + 128f
            bb = ((bb - 128f) * profile.contrast) + 128f

            val highlightScale = 1f + profile.highlightRecovery * (1f - (luma / 255f))
            rr = clamp(rr * highlightScale)
            gg = clamp(gg * highlightScale)
            bb = clamp(bb * highlightScale)

            val sharpenR = clamp(rr + (rr - denR) * profile.sharpness)
            val sharpenG = clamp(gg + (gg - denG) * profile.sharpness)
            val sharpenB = clamp(bb + (bb - denB) * profile.sharpness)

            base[idx] = argb(255, sharpenR, sharpenG, sharpenB)
        }

        val graded = applyColorGrading(base, width, height, profile)

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(graded, 0, width, 0, 0, width, height)
        return result
    }

    private fun applyColorGrading(pixels: IntArray, width: Int, height: Int, profile: Profile): IntArray {
        val out = IntArray(pixels.size)
        val temp = profile.temperature.coerceIn(-1f, 1f)
        val tint = profile.tint.coerceIn(-1f, 1f)
        val vignette = profile.vignette.coerceIn(0f, 1f)
        val grain = profile.grain.coerceIn(0f, 1f)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val idx = y * width + x
                val pixel = pixels[idx]
                var r = ((pixel shr 16) and 0xFF).toFloat()
                var g = ((pixel shr 8) and 0xFF).toFloat()
                var b = (pixel and 0xFF).toFloat()

                if (temp > 0f) {
                    r += temp * 36f
                    b -= temp * 22f
                } else {
                    b += (-temp) * 34f
                    r -= (-temp) * 18f
                }
                g += tint * 20f

                val lum = 0.299f * r + 0.587f * g + 0.114f * b
                val highlight = ((lum - 160f).coerceAtLeast(0f) / 95f) * profile.highlightBoost
                val shadow = ((128f - lum).coerceAtLeast(0f) / 128f) * profile.shadowLift

                r += highlight * 30f
                g += highlight * 16f
                b += highlight * 26f

                r += shadow * 18f
                g += shadow * 12f
                b += shadow * 22f

                val nx = (x.toFloat() / (width - 1).coerceAtLeast(1) - 0.5f) * 2f
                val ny = (y.toFloat() / (height - 1).coerceAtLeast(1) - 0.5f) * 2f
                val dist = sqrt(nx * nx + ny * ny)
                val vignetteScale = 1f - (dist * vignette)
                r *= vignetteScale.coerceAtLeast(0.35f)
                g *= vignetteScale.coerceAtLeast(0.35f)
                b *= vignetteScale.coerceAtLeast(0.35f)

                val grainNoise = (((x * 13 + y * 17 + idx) % 23) / 23f - 0.5f) * grain * 30f
                r += grainNoise
                g += grainNoise
                b += grainNoise

                out[idx] = argb(255, r, g, b)
            }
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

        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                var rSum = 0
                var gSum = 0
                var bSum = 0
                var count = 0

                for (dy in -1..1) {
                    val yy = y + dy
                    if (yy < 0 || yy >= height) continue
                    for (dx in -1..1) {
                        val xx = x + dx
                        if (xx < 0 || xx >= width) continue
                        val pixel = pixels[yy * width + xx]
                        rSum += (pixel shr 16) and 0xFF
                        gSum += (pixel shr 8) and 0xFF
                        bSum += pixel and 0xFF
                        count++
                    }
                }

                val src = pixels[index]
                val sr = ((src shr 16) and 0xFF).toFloat()
                val sg = ((src shr 8) and 0xFF).toFloat()
                val sb = (src and 0xFF).toFloat()

                val avgR = rSum / count.toFloat()
                val avgG = gSum / count.toFloat()
                val avgB = bSum / count.toFloat()

                val blendedR = lerp(sr, avgR, strength)
                val blendedG = lerp(sg, avgG, strength)
                val blendedB = lerp(sb, avgB, strength)

                output[index] = argb(255, blendedR, blendedG, blendedB)
            }
        }

        return output
    }

    private fun argb(alpha: Int, r: Float, g: Float, b: Float): Int {
        return (alpha shl 24) or (clamp(r) shl 16) or (clamp(g) shl 8) or clamp(b)
    }

    private fun clamp(value: Float): Int {
        return value.coerceIn(0f, 255f).toInt()
    }

    private fun lerp(start: Float, end: Float, factor: Float): Float {
        return start + (end - start) * factor
    }
}
