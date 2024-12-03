package com.example.led_strip_control.home.view

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.PaintDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.animation.addListener
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.example.led_strip_control.R
import com.example.led_strip_control.database.ColorLocalDataSourceImpl
import com.example.led_strip_control.databinding.ActivityMainBinding
import com.example.led_strip_control.home.view_model.ColorViewModel
import com.example.led_strip_control.home.view_model.ColorViewModelFactory
import com.example.led_strip_control.pojo.ColorEntity
import com.example.led_strip_control.repository.ColorRepositoryImpl
import com.example.led_strip_control.service_client.I2cServiceApp
import com.example.led_strip_control.service_client.I2cServiceClient
import kotlinx.coroutines.launch
import com.example.led_strip_control.service_client.LedStripServiceClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MainActivity : AppCompatActivity(), OnMainClickListener {

    companion object {
        private const val TAG = "SHERIF_MAIN_ACTIVITY"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var colorAdapter: ColorAdapter
    private lateinit var colorViewModel: ColorViewModel

    private lateinit var varyingSubModesContainer: View
    private lateinit var colorOverlay2: View

    private lateinit var sharedPreferences: SharedPreferences;

    private lateinit var selectedModeButton: Button
    private lateinit var btnSettings: Button
    private lateinit var btnFavourites: Button
    private lateinit var btnToggleLED: Button
    private lateinit var txtMode: TextView
    private lateinit var txtAnimationMode: TextView

    private lateinit var ledStripServiceClient: LedStripServiceClient
    private lateinit var i2cServiceClient: I2cServiceClient
    private lateinit var i2cServiceApp: I2cServiceApp


    private val selectedMode = "manual"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //AIDL
        ledStripServiceClient = LedStripServiceClient.getInstance(this)
        i2cServiceClient = I2cServiceClient.getInstance(this)
        i2cServiceApp = I2cServiceApp(i2cServiceClient)


        //////////////
        varyingSubModesContainer = binding.varyingSubModesContainer
        btnSettings = binding.btnSettings
        btnFavourites = binding.btnFavourites
        btnToggleLED = binding.btnToggleLED
        txtMode = binding.txtMode
        txtAnimationMode = binding.txtAnimationMode

        val manualModeButton = binding.manualModeButton
        val adaptiveModeButton = binding.adaptiveModeButton
        val varyingModeButton = binding.varyingModeButton
        val danceMode1Button = binding.danceMode1Button
        val danceMode2Button = binding.danceMode2Button
        val colorOverlay: View = binding.colorOverlay
        val imgManualTick: View = binding.imgManualTick
        val imgAdaptiveTick: View = binding.imgAdaptiveTick
        val imgVaryingTick: View = binding.imgVaryingTick
        colorOverlay2 = binding.colorOverlay2

        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)

        binding.colorOverlay2?.setBackgroundColor(
            sharedPreferences.getInt(
                "currentColor",
                getColor(R.color.your_background_color)
            )
        )

        animateView(binding.shade2, "right", "hide", 0)
        animateView(binding.modeContainer, "right", "hide", 0)
        
        when (sharedPreferences.getBoolean("LED_ON_OFF", false)) {
            true -> {
                btnToggleLED.foreground =
                    resources.getDrawable(R.drawable.toggle_led_button_on_foreground, null)
                binding.txtLEDStatus.text = getString(R.string.ambient_light_on)
            }

            false -> {
                btnToggleLED.foreground =
                    resources.getDrawable(R.drawable.toggle_led_button_off_foreground, null)
                binding.txtLEDStatus.text = getString(R.string.ambient_light_off)

            }
        }

        when (sharedPreferences.getString("SELECTED_MODE", "manual")) {
            "manual" -> {
                txtMode.text = getString(R.string.manual_mode)
                expandSection(manualModeButton, listOf(adaptiveModeButton, varyingModeButton))
                imgManualTick.visibility = View.VISIBLE
                imgAdaptiveTick.visibility = View.GONE
                imgVaryingTick.visibility = View.GONE
                txtAnimationMode.visibility = View.GONE
                varyingSubModesContainer.visibility = View.GONE

                // Change the visibility of color picker
                animateView(binding.rvFavoriteColors, "left", "show", 300)
                animateView(binding.composeColorPicker, "left", "show", 300)
                animateView(binding.btnFavourites, "left", "show", 300)
            }

            "adaptive" -> {
                txtMode.text = getString(R.string.adaptive_mode)
                expandSection(adaptiveModeButton, listOf(manualModeButton, varyingModeButton))
                imgManualTick.visibility = View.GONE
                imgAdaptiveTick.visibility = View.VISIBLE
                imgVaryingTick.visibility = View.GONE
                txtAnimationMode.visibility = View.GONE
                varyingSubModesContainer.visibility = View.GONE

                animateView(binding.rvFavoriteColors, "left", "hide", 0)
                animateView(binding.composeColorPicker, "left", "hide", 0)
                animateView(binding.btnFavourites, "left", "hide", 0)
            }

            "varying" -> {
                binding.composeColorPicker.translationX =
                    -binding.composeColorPicker.width.toFloat()
                binding.rvFavoriteColors.translationX = -binding.rvFavoriteColors.width.toFloat()

                txtMode.text = getString(R.string.varying_mode)
                txtAnimationMode.text = sharedPreferences.getString("Variation", "Snake")
                expandSection(varyingModeButton, listOf(manualModeButton, adaptiveModeButton))
                imgManualTick.visibility = View.GONE
                imgAdaptiveTick.visibility = View.GONE
                imgVaryingTick.visibility = View.VISIBLE
                txtAnimationMode.visibility = View.VISIBLE
                varyingSubModesContainer.visibility = View.VISIBLE

                // Change the visibility of color picker
                animateView(binding.rvFavoriteColors, "left", "hide", 0)
                animateView(binding.composeColorPicker, "left", "hide", 0)
                animateView(binding.btnFavourites, "left", "hide", 0)

                when (sharedPreferences.getString("Variation", null.toString())) {
                    getString(R.string.random_animation) -> {
                        danceMode1Button.performClick()
                    }

                    getString(R.string.fade_animation) -> {
                        danceMode2Button.performClick()
                    }
                }
            }

        }

        // Initialize ViewModel
        val colorRepository = ColorRepositoryImpl(ColorLocalDataSourceImpl.getInstance(this))
        colorViewModel = ViewModelProvider(
            this,
            ColorViewModelFactory(colorRepository)
        ).get(ColorViewModel::class.java)

        // Initialize the ColorAdapter for RecyclerView
        colorAdapter = ColorAdapter(this)

        // Set the RecyclerView adapter
        binding.rvFavoriteColors?.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvFavoriteColors?.adapter = colorAdapter

        binding.rvFavoriteColors?.let { setupSwipeToDelete(it, colorAdapter) }

        // Collect the colors from ViewModel using StateFlow
        lifecycleScope.launch {
            colorViewModel.colors.collect { colorList ->
                // Submit the updated list to the adapter
                colorAdapter.submitList(colorList)
            }
        }

        binding.composeColorPicker?.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.composeColorPicker?.setContent {
            ColorPickerContent(
                onAddColor = { color ->
                    val colorEntity = ColorEntity(
                        red = (color.red * 255).toInt(),
                        green = (color.green * 255).toInt(),
                        blue = (color.blue * 255).toInt()
                    )
                    colorViewModel.addColor(colorEntity)
                    binding.rvFavoriteColors.postDelayed({
                        scrollToLastItem(binding.rvFavoriteColors)
                    }, 100)

                },
                onColorChanged = { color ->
                    val red = (color.red * 255).toInt()
                    val green = (color.green * 255).toInt()
                    val blue = (color.blue * 255).toInt()

                    setAmbientColor(Color.rgb(red, green, blue))

                    val statusStop = ledStripServiceClient.stopAllModes()
                    if (statusStop == null || !statusStop.success) {
                        Log.e(TAG, "Failed to stop all modes")
                    }

                    for (i in 0 until 8) {
                        val statusSetColor = ledStripServiceClient.setColor(i, red, green, blue)
                        if (statusSetColor == null || !statusSetColor.success) {
                            Log.e(TAG, "Failed to set color at index $i")
                            continue // Skip the current iteration if setColor fails
                        } else {
                            Log.i(TAG, "SetColor Result: ${statusSetColor.message}")
                        }

                        val statusShow = ledStripServiceClient.show()
                        if (statusShow == null || !statusShow.success) {
                            Log.e(TAG, "Failed to show color changes at iteration $i")
                        } else {
                            Log.i(TAG, "Show Result: ${statusShow.message}")
                        }
                    }

                    Log.i("SHERIF_COLOR_PICKER", "Live Color Changed: R=$red, G=$green, B=$blue")
                },

                onBrightnessChanged = { brightness ->
                    // Handle the brightness change
                    val statusBrightness = ledStripServiceClient.setBrightness(brightness)
                    if (statusBrightness == null || !statusBrightness.success) {
                        Log.e(TAG, "Failed to set the brightness")
                    } else {
                        Log.i(TAG, "Show Result: ${statusBrightness.message}")
                    }
                    saveBrightnessToPreferences(brightness)
                    Log.i("SHERIF_COLOR_PICKER", "Brightness Changed: $brightness")
                }
            )
        }


        //////////////////////////////////////////////////////////////////////////////////////
        // ________________________________________________________________________________________________
        // Modes

        selectedModeButton = manualModeButton

        btnToggleLED.setOnClickListener {
            // toggle radio button
            val LED_status_ON: Boolean =
                getSharedPreferences("AppPreferences", MODE_PRIVATE).getBoolean("LED_ON_OFF", true)

            if (LED_status_ON) {
                saveLedOnOffToPreferences(false)
                btnToggleLED.foreground =
                    resources.getDrawable(R.drawable.toggle_led_button_off_foreground, null)
                binding.txtLEDStatus.text = getString(R.string.ambient_light_off)
            } else {
                saveLedOnOffToPreferences(true)
                btnToggleLED.foreground =
                    resources.getDrawable(R.drawable.toggle_led_button_on_foreground, null)
                binding.txtLEDStatus.text = getString(R.string.ambient_light_on)
            }
        }

        btnSettings.setOnClickListener {
            if (binding.modeContainer.visibility == View.VISIBLE) {
                animateView(binding.shade2, "right", "hide", 300)
                animateView(binding.modeContainer, "right", "hide", 300)
                ObjectAnimator.ofFloat(btnSettings, "rotation", 30f, 0f).setDuration(150).start()
            } else {
                animateView(binding.shade2, "right", "show", 300)
                animateView(binding.modeContainer, "right", "show", 300)
                ObjectAnimator.ofFloat(btnSettings, "rotation", 0f, 30f).setDuration(150).start()
            }
        }
        btnFavourites.setOnClickListener {
            val color =
                sharedPreferences.getInt("currentColor", getColor(R.color.your_background_color))
            val colorEntity = ColorEntity(
                red = color.red,
                green = color.green,
                blue = color.blue
            )
            colorViewModel.addColor(colorEntity)
            binding.rvFavoriteColors.postDelayed({
                scrollToLastItem(binding.rvFavoriteColors)
            }, 100)
        }

        manualModeButton.setOnClickListener {

            i2cServiceApp.stop()
            val statusStop = ledStripServiceClient.stopAllModes()
            if (statusStop == null || !statusStop.success) {
                val stopMessage = statusStop?.message ?: "stopAllModes() returned null"
                Log.e(
                    TAG,
                    "Operation failed. stopAllModes: $stopMessage"
                )
            } else {
                Log.i(
                    TAG,
                    "operations succeeded. stopAllModes: ${statusStop.message}"
                )
            }


            saveModeToPreferences("manual")
            saveVaryingModeToPreferences(null.toString())
            binding.radioGroup.clearCheck()
            selectedModeButton = manualModeButton
            txtMode.text = getString(R.string.manual_mode)

            resetContainerPosition(varyingSubModesContainer)
            expandSection(manualModeButton, listOf(adaptiveModeButton, varyingModeButton))
            imgManualTick.visibility = View.VISIBLE
            imgAdaptiveTick.visibility = View.GONE
            imgVaryingTick.visibility = View.GONE
            txtAnimationMode.visibility = View.GONE
            hideView(varyingSubModesContainer)

            // Change the visibility of color picker
            animateView(binding.rvFavoriteColors, "left", "show", 300)
            animateView(binding.composeColorPicker, "left", "show", 300)
            animateView(binding.btnFavourites, "left", "show", 300)
        }

        adaptiveModeButton.setOnClickListener {
            saveModeToPreferences("adaptive")
            saveVaryingModeToPreferences(null.toString())
            binding.radioGroup.clearCheck()
            selectedModeButton = adaptiveModeButton
            txtMode.text = getString(R.string.adaptive_mode)

            resetContainerPosition(varyingSubModesContainer)
            expandSection(adaptiveModeButton, listOf(manualModeButton, varyingModeButton))
            imgManualTick.visibility = View.GONE
            imgAdaptiveTick.visibility = View.VISIBLE
            imgVaryingTick.visibility = View.GONE
            txtAnimationMode.visibility = View.GONE
            hideView(varyingSubModesContainer)

            // Change the visibility of color picker
            animateView(binding.rvFavoriteColors, "left", "hide", 300)
            animateView(binding.composeColorPicker, "left", "hide", 300)
            animateView(binding.btnFavourites, "left", "hide", 300)

            val statusStop = ledStripServiceClient.stopAllModes()
            if (statusStop == null || !statusStop.success) {
                val stopMessage = statusStop?.message ?: "stopAllModes() returned null"
                Log.e(
                    TAG,
                    "Operation failed. stopAllModes: $stopMessage"
                )
            } else {
                Log.i(
                    TAG,
                    "operations succeeded. stopAllModes: ${statusStop.message}"
                )
            }

            CoroutineScope(Dispatchers.IO).launch {
                i2cServiceApp.run()
            }

            val lastValue = i2cServiceApp.getLastValue()
            Log.i(TAG, "Last ADC Value: $lastValue")

        }

        varyingModeButton.setOnClickListener {
            hideView(txtAnimationMode)
            saveVaryingModeToPreferences(null.toString())
            binding.radioGroup.clearCheck()
            // close ambient light //

            selectedModeButton = varyingModeButton
            txtMode.text = getString(R.string.varying_mode)

            expandSection(varyingModeButton, listOf(manualModeButton, adaptiveModeButton))
            imgManualTick.visibility = View.GONE
            imgAdaptiveTick.visibility = View.GONE
            imgVaryingTick.visibility = View.VISIBLE
            toggleVisibility(varyingSubModesContainer)
//            toggleVisibility(txtAnimationMode)

            // Change the visibility of color picker
            animateView(binding.rvFavoriteColors, "left", "hide", 300)
            animateView(binding.composeColorPicker, "left", "hide", 300)
            animateView(binding.btnFavourites, "left", "hide", 300)

        }

        danceMode1Button.setOnClickListener {
            saveModeToPreferences("varying")
            saveVaryingModeToPreferences(getString(R.string.random_animation))
            txtAnimationMode.text = getString(R.string.random_animation)
            showView(txtAnimationMode)

            i2cServiceApp.stop()
            val statusStop = ledStripServiceClient.stopAllModes()
            val statusRandom = ledStripServiceClient.setRandom()
            if (statusStop == null || !statusStop.success || statusRandom == null || !statusRandom.success) {
                val stopMessage = statusStop?.message ?: "stopAllModes() returned null"
                val randomMessage = statusRandom?.message ?: "setRandom() returned null"
                Log.e(
                    TAG,
                    "Operation failed. stopAllModes: $stopMessage, setRandom: $randomMessage"
                )
            } else {
                Log.i(
                    TAG,
                    "Both operations succeeded. stopAllModes: ${statusStop.message}, setRandom: ${statusRandom.message}"
                )
            }
        }

        danceMode2Button.setOnClickListener {
            saveVaryingModeToPreferences(getString(R.string.fade_animation))
            txtAnimationMode.text = getString(R.string.fade_animation)
            showView(txtAnimationMode)

            i2cServiceApp.stop()
            val statusStop = ledStripServiceClient.stopAllModes()
            val statusFade = ledStripServiceClient.setGlobalFade()

            if (statusStop == null || !statusStop.success || statusFade == null || !statusFade.success) {
                val stopMessage = statusStop?.message ?: "stopAllModes() returned null"
                val fadeMessage = statusFade?.message ?: "setGlobalFade() returned null"
                Log.e(
                    TAG,
                    "Operation failed. stopAllModes: $stopMessage, setGlobalFade: $fadeMessage"
                )
            } else {
                Log.i(
                    TAG,
                    "Both operations succeeded. stopAllModes: ${statusStop.message}, setGlobalFade: ${statusFade.message}"
                )
            }
        }


        ////////////////////////////////////////////////////////////////////////////////

    }

    override fun onDestroy() {
        super.onDestroy()
        ledStripServiceClient.unbindService()
    }


    override fun onDeleteClick(colorId: Int) {
        colorViewModel.deleteColor(colorId)
    }

    override fun onColorClick(color: ColorEntity) {
        setAmbientColor(Color.rgb(color.red, color.green, color.blue))
        // Set The Color To The Led
        val statusStop = ledStripServiceClient.stopAllModes()
        if (statusStop == null || !statusStop.success) {
            Log.e(TAG, "Failed to stop all modes")
        }

        for (i in 0 until 8) {
            val statusSetColor =
                ledStripServiceClient.setColor(i, color.red, color.green, color.blue)
            if (statusSetColor == null || !statusSetColor.success) {
                Log.e(TAG, "Failed to set color at index $i")
                continue // Skip the current iteration if setColor fails
            } else {
                Log.i(TAG, "SetColor Result: ${statusSetColor.message}")
            }

            val statusShow = ledStripServiceClient.show()
            if (statusShow == null || !statusShow.success) {
                Log.e(TAG, "Failed to show color changes at iteration $i")
            } else {
                Log.i(TAG, "Show Result: ${statusShow.message}")
            }
        }
    }


    fun saveColorToPreferences(color: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt("currentColor", color)
        editor.apply()
    }

    fun saveBrightnessToPreferences(brightness: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt("Brightness", brightness)
        editor.apply()
    }

    fun saveModeToPreferences(mode: String) {
        val editor = sharedPreferences.edit()
        editor.putString("SELECTED_MODE", mode)
        editor.apply()
    }

    fun saveVaryingModeToPreferences(variation: String) {
        val editor = sharedPreferences.edit()
        editor.putString("Variation", variation)
        editor.apply()
    }

    fun saveLedOnOffToPreferences(led_status: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("LED_ON_OFF", led_status)
        editor.apply()
    }

    fun setAmbientColor(color: Int) {
        setRadialGradient(
            listOf(
                getColor(android.R.color.transparent),
                getColor(R.color.your_background_color),
            )
        )
        fadeToColor(Color.rgb(color.red, color.green, color.blue))
        saveColorToPreferences(Color.rgb(color.red, color.green, color.blue))
    }


    /////////////////////////
    // Animation

    private fun expandSection(selectedButton: View, otherButtons: List<Button>) {
        // Reset all buttons to their initial state
        val currentMode = sharedPreferences.getString("SELECTED_MODE", "default_value")

        val allButtons = otherButtons + selectedButton
        for (button in allButtons) {
            ObjectAnimator.ofFloat(button, "scaleX", button.scaleX, 1f).apply {
                duration = 300
                start()
            }
            ObjectAnimator.ofFloat(button, "scaleY", button.scaleY, 1f).apply {
                duration = 300
                start()
            }
            ObjectAnimator.ofFloat(button, "alpha", button.alpha, 1f).apply {
                duration = 300
                start()
            }
        }

        // shrink selected mode
        for (button in otherButtons) {
            ObjectAnimator.ofFloat(button, "alpha", 1f, 0.5f).apply {
                duration = 300
                start()
            }
        }

        // Highlight the selected button
        ObjectAnimator.ofFloat(selectedButton, "scaleX", 1f, 1.2f).apply {
            duration = 300
            start()
        }
        ObjectAnimator.ofFloat(selectedButton, "scaleY", 1f, 1.2f).apply {
            duration = 300
            start()
        }
    }


    private fun toggleVisibility(view: View) {
        val isVisible = view.visibility == View.VISIBLE
        view.visibility = if (isVisible) View.GONE else View.VISIBLE
        ObjectAnimator.ofFloat(view, "alpha", if (isVisible) 1f else 0f, if (isVisible) 0f else 1f)
            .apply {
                duration = 300
                start()
            }
    }

    private fun showView(view: View) {
        if (view.visibility == View.GONE) {
            ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
                duration = 300
                start()
            }.addListener(onEnd = { view.visibility = View.VISIBLE })
        }
    }

    private fun hideView(view: View) {
        if (view.visibility == View.VISIBLE) { // Only hide if visible
            ObjectAnimator.ofFloat(view, "alpha", 1f, 0f).apply {
                duration = 300
                start()
            }.addListener(onEnd = { view.visibility = View.GONE })
        }
    }

    private fun resetContainerPosition(view: View) {
        ObjectAnimator.ofFloat(view, "translationY", view.translationY, 0f).apply {
            duration = 300
            start()
        }
    }

    fun animateView(
        view: View,
        direction: String,
        action: String, // "show" or "hide"
        duration: Long = 300
    ) {
        // Determine if it's a show or hide animation
        val isShow = action.lowercase() == "show"

        // Set initial and final translation and alpha values
        val translationXStart: Float
        val translationXEnd: Float
        val alphaStart: Float
        val alphaEnd: Float

        if (isShow) {
            // Prepare for "show" animation
            if (view.visibility == View.VISIBLE) return // Already visible
            translationXStart = when (direction.lowercase()) {
                "left" -> -view.width.toFloat()
                "right" -> view.width.toFloat()
                else -> throw IllegalArgumentException("Direction must be 'left' or 'right'")
            }
            translationXEnd = 0f
            alphaStart = 0f
            alphaEnd = 1f

            // Ensure view is visible before animating
            view.visibility = View.VISIBLE
        } else {
            // Prepare for "hide" animation
            if (view.visibility != View.VISIBLE) return // Already hidden
            translationXStart = 0f
            translationXEnd = when (direction.lowercase()) {
                "left" -> -view.width.toFloat()
                "right" -> view.width.toFloat()
                else -> throw IllegalArgumentException("Direction must be 'left' or 'right'")
            }
            alphaStart = 1f
            alphaEnd = 0f
        }

        // Create translation and alpha animations
        val translationAnimator =
            ObjectAnimator.ofFloat(view, "translationX", translationXStart, translationXEnd)
        val alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", alphaStart, alphaEnd)

        // Play animations together
        AnimatorSet().apply {
            playTogether(translationAnimator, alphaAnimator)
            this.duration = duration
            start()
        }.addListener(onEnd = {
            // Set final visibility state based on action
            view.visibility = if (isShow) View.VISIBLE else View.GONE
        })
    }


    private fun scrollToLastItem(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager

        if (layoutManager is LinearLayoutManager) {
            val itemCount = recyclerView.adapter?.itemCount ?: 0
            if (itemCount > 0) {
                val lastPosition = itemCount - 1

                // Create a custom SmoothScroller
                val smoothScroller = object : LinearSmoothScroller(recyclerView.context) {
                    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                        // Adjust the scroll duration to 300ms
                        return super.calculateSpeedPerPixel(displayMetrics) * 300 / 1000f
                    }
                }

                // Set the target position to the last item
                smoothScroller.targetPosition = lastPosition
                layoutManager.startSmoothScroll(smoothScroller)
            }
        }
    }


    fun setRadialGradient(colors: List<Int>) {
        val gradient = RadialGradient(
            binding.colorOverlay.width / 1.5f, // Center X
            binding.colorOverlay.height / 1.5f, // Center Y
            1200f, // Radius
            colors.toIntArray(), // Colors array
            null, // Stops (can be null for evenly spaced)
            Shader.TileMode.CLAMP // Gradient mode
        )
        binding.colorOverlay.background = PaintDrawable().apply {
            paint.shader = gradient
        }
    }

    fun fadeToColor(newColor: Int) {
        val fadeIn = ObjectAnimator.ofInt(
            colorOverlay2,
            "backgroundColor",
            sharedPreferences.getInt("currentColor", Color.TRANSPARENT),
            newColor
        )
        fadeIn.duration = 1000 // 1 second duration
        fadeIn.setEvaluator(ArgbEvaluator())
        fadeIn.start()
    }
}

