package com.example.imageengine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.imageengine.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val imageEngine = ImageEnhancementEngine()
    private var currentBitmap: Bitmap? = null
    private var currentMode: ImageEnhancementEngine.Mode = ImageEnhancementEngine.Mode.PREMIUM
    private var currentProfile: ImageEnhancementEngine.Profile = ImageEnhancementEngine.Profile.PREMIUM

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@registerForActivityResult

        val stream = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(stream)
        if (bitmap == null) {
            Toast.makeText(this, "Unable to load image", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }

        currentBitmap = bitmap
        binding.originalImage.setImageBitmap(bitmap)
        applyEnhancement()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSliders()
        bindPresetButtons()

        binding.btnPick.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnEnhance.setOnClickListener {
            applyEnhancement()
        }

        binding.btnSave.setOnClickListener {
            saveProcessedImage()
        }
    }

    private fun bindPresetButtons() {
        binding.presetPremium.setOnClickListener {
            setMode(ImageEnhancementEngine.Mode.PREMIUM, ImageEnhancementEngine.Profile.PREMIUM)
        }

        binding.presetCinematic.setOnClickListener {
            setMode(ImageEnhancementEngine.Mode.CINEMATIC, ImageEnhancementEngine.Profile.CINEMATIC)
        }

        binding.presetNight.setOnClickListener {
            setMode(ImageEnhancementEngine.Mode.NIGHT, ImageEnhancementEngine.Profile.NIGHT)
        }

        binding.presetPortrait.setOnClickListener {
            setMode(ImageEnhancementEngine.Mode.PORTRAIT, ImageEnhancementEngine.Profile.PORTRAIT)
        }

        binding.presetHdr.setOnClickListener {
            setMode(ImageEnhancementEngine.Mode.HDR, ImageEnhancementEngine.Profile.HDR)
        }

        binding.presetGolden.setOnClickListener {
            setMode(ImageEnhancementEngine.Mode.GOLDEN, ImageEnhancementEngine.Profile.GOLDEN)
        }

        binding.presetCool.setOnClickListener {
            setMode(ImageEnhancementEngine.Mode.COOL, ImageEnhancementEngine.Profile.COOL)
        }

        binding.presetNatural.setOnClickListener {
            setMode(ImageEnhancementEngine.Mode.NATURAL, ImageEnhancementEngine.Profile.NATURAL)
        }

        binding.presetVivid.setOnClickListener {
            setMode(ImageEnhancementEngine.Mode.VIVID, ImageEnhancementEngine.Profile.VIVID)
        }
    }

    private fun setMode(mode: ImageEnhancementEngine.Mode, profile: ImageEnhancementEngine.Profile) {
        currentMode = mode
        currentProfile = profile
        syncSlidersFromProfile()
        applyEnhancement()
    }

    private fun setupSliders() {
        bindSlider(binding.exposureSlider, 0.8f, 1.8f, currentProfile.exposure) { value -> currentProfile = currentProfile.copy(exposure = value) }
        bindSlider(binding.contrastSlider, 0.9f, 1.8f, currentProfile.contrast) { value -> currentProfile = currentProfile.copy(contrast = value) }
        bindSlider(binding.saturationSlider, 0.8f, 1.8f, currentProfile.saturation) { value -> currentProfile = currentProfile.copy(saturation = value) }
        bindSlider(binding.sharpnessSlider, 0.2f, 1.4f, currentProfile.sharpness) { value -> currentProfile = currentProfile.copy(sharpness = value) }
        bindSlider(binding.denoiseSlider, 0.0f, 1.0f, currentProfile.denoise) { value -> currentProfile = currentProfile.copy(denoise = value) }
        bindSlider(binding.temperatureSlider, -1.0f, 1.0f, currentProfile.temperature) { value -> currentProfile = currentProfile.copy(temperature = value) }
        bindSlider(binding.tintSlider, -1.0f, 1.0f, currentProfile.tint) { value -> currentProfile = currentProfile.copy(tint = value) }
        bindSlider(binding.vignetteSlider, 0.0f, 0.5f, currentProfile.vignette) { value -> currentProfile = currentProfile.copy(vignette = value) }
        bindSlider(binding.grainSlider, 0.0f, 0.3f, currentProfile.grain) { value -> currentProfile = currentProfile.copy(grain = value) }
    }

    private fun bindSlider(
        slider: SeekBar,
        min: Float,
        max: Float,
        currentValue: Float,
        onChange: (Float) -> Unit
    ) {
        slider.max = 100
        val scaled = ((currentValue - min) / (max - min) * 100f).toInt().coerceIn(0, 100)
        slider.progress = scaled
        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = min + (progress / 100f) * (max - min)
                onChange(value)
                if (fromUser) applyEnhancement()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
    }

    private fun syncSlidersFromProfile() {
        binding.exposureSlider.progress = ((currentProfile.exposure - 0.8f) / (1.8f - 0.8f) * 100f).toInt().coerceIn(0, 100)
        binding.contrastSlider.progress = ((currentProfile.contrast - 0.9f) / (1.8f - 0.9f) * 100f).toInt().coerceIn(0, 100)
        binding.saturationSlider.progress = ((currentProfile.saturation - 0.8f) / (1.8f - 0.8f) * 100f).toInt().coerceIn(0, 100)
        binding.sharpnessSlider.progress = ((currentProfile.sharpness - 0.2f) / (1.4f - 0.2f) * 100f).toInt().coerceIn(0, 100)
        binding.denoiseSlider.progress = ((currentProfile.denoise - 0.0f) / (1.0f - 0.0f) * 100f).toInt().coerceIn(0, 100)
        binding.temperatureSlider.progress = (((currentProfile.temperature + 1f) / 2f) * 100f).toInt().coerceIn(0, 100)
        binding.tintSlider.progress = (((currentProfile.tint + 1f) / 2f) * 100f).toInt().coerceIn(0, 100)
        binding.vignetteSlider.progress = ((currentProfile.vignette / 0.5f) * 100f).toInt().coerceIn(0, 100)
        binding.grainSlider.progress = ((currentProfile.grain / 0.3f) * 100f).toInt().coerceIn(0, 100)
    }

    private fun applyEnhancement() {
        val original = currentBitmap ?: return
        val processed = imageEngine.enhance(original, currentProfile)
        binding.processedImage.setImageBitmap(processed)
    }

    private fun saveProcessedImage() {
        val bitmap = (binding.processedImage.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap ?: run {
            Toast.makeText(this, "Unable to export image", Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = "enhanced_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
        val outputFile = File(getExternalFilesDir(null), fileName)

        try {
            FileOutputStream(outputFile).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
            }
            Toast.makeText(this, "Saved: ${outputFile.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Save failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
