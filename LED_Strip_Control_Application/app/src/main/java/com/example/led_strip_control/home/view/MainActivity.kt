package com.example.led_strip_control.home.view

import android.animation.ObjectAnimator
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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

class MainActivity : AppCompatActivity(), OnMainClickListener {

    companion object {
        const val TAG = "SHERIF_MAIN_ACTIVITY"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var colorAdapter: ColorAdapter
    private lateinit var colorViewModel: ColorViewModel

    // Declare Shared Preference
    lateinit var sharedPreferences: SharedPreferences
    lateinit var sharedPrefEditor: SharedPrefEditor

    // Declare AIDL Client Services
    lateinit var ledStripServiceClient: LedStripServiceClient
    lateinit var i2cServiceClient: I2cServiceClient
    lateinit var i2cServiceApp: I2cServiceApp

    // Declare UI components
    private lateinit var btnSettings: Button
    private lateinit var btnFavourites: Button
    private lateinit var btnToggleLED: Button
    private lateinit var selectedModeButton: Button
    private lateinit var manualModeButton: Button
    private lateinit var adaptiveModeButton: Button
    private lateinit var varyingModeButton: Button
    private lateinit var danceMode1Button: Button
    private lateinit var danceMode2Button: Button
    private lateinit var imgManualTick: View
    private lateinit var imgAdaptiveTick: View
    private lateinit var imgVaryingTick: View
    private lateinit var rvFavoriteColors: View

    private lateinit var varyingSubModesContainer: View
    private lateinit var colorOverlay: View
    private lateinit var colorOverlay2: View

    private lateinit var txtMode: TextView
    private lateinit var txtAnimationMode: TextView

    private val selectedMode = "manual"

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Define UI Components
        varyingSubModesContainer = binding.varyingSubModesContainer
        btnSettings = binding.btnSettings
        btnFavourites = binding.btnFavourites
        btnToggleLED = binding.btnToggleLED
        txtMode = binding.txtMode
        txtAnimationMode = binding.txtAnimationMode
        manualModeButton = binding.manualModeButton
        adaptiveModeButton = binding.adaptiveModeButton
        varyingModeButton = binding.varyingModeButton
        danceMode1Button = binding.danceMode1Button
        danceMode2Button = binding.danceMode2Button
        colorOverlay = binding.colorOverlay
        imgManualTick = binding.imgManualTick
        imgAdaptiveTick = binding.imgAdaptiveTick
        imgVaryingTick = binding.imgVaryingTick
        colorOverlay2 = binding.colorOverlay2
        rvFavoriteColors = binding.rvFavoriteColors

        // Define Shared Preference
        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        sharedPrefEditor = SharedPrefEditor(sharedPreferences)

        // Define AIDL Client Services
        ledStripServiceClient = LedStripServiceClient.getInstance(this)
        i2cServiceClient = I2cServiceClient.getInstance(this)
        i2cServiceApp = I2cServiceApp(i2cServiceClient)

        // Set initial color overlay
        binding.colorOverlay2.setBackgroundColor(
            sharedPreferences.getInt(
                "currentColor",
                Color.TRANSPARENT
            )
        )
        rvFavoriteColors.post {
            rvFavoriteColors.layoutParams.width = (rvFavoriteColors.parent as View).width / 2
        }

        sharedPrefEditor.saveFirstRunFlag(false)

        // Hide the mode container initially based on saved shared preference
        animateView(binding.modeContainer, "right", "hide", 0)
        animateView(binding.shade2, "right", "hide", 0)

        // Initialize UI when LED is ON or OFF
        when (sharedPreferences.getBoolean("LED_ON_OFF", false)) {
            true -> {
                onLedOn(sharedPreferences)
                btnToggleLED.foreground =
                    resources.getDrawable(R.drawable.toggle_led_button_on_foreground, null)
                binding.txtLEDStatus.text = getString(R.string.ambient_light_on)
            }

            false -> {
                onLedOff()
                btnToggleLED.foreground =
                    resources.getDrawable(R.drawable.toggle_led_button_off_foreground, null)
                binding.txtLEDStatus.text = getString(R.string.ambient_light_off)
            }
        }

        // Initialize Modes realted UI based on saved shared preference
        when (sharedPreferences.getString("SELECTED_MODE", "manual")) {
            "manual" -> {
                onManualModeClick()
            }

            "adaptive" -> {
                onAdaptiveModeClick()
            }

            "varying" -> {
                binding.composeColorPicker.translationX = -binding.composeColorPicker.width.toFloat()
                binding.composeColorSlider.translationX = -binding.composeColorSlider.width.toFloat()
                binding.rvFavoriteColors.translationX = -binding.rvFavoriteColors.width.toFloat()

                txtAnimationMode.text = sharedPreferences.getString("Variation", "Snake")
                txtAnimationMode.visibility = View.VISIBLE

                onAnimationModeClick()

                when (sharedPreferences.getString("Variation", null.toString())) {
                    getString(R.string.random_animation) -> {
                        onRandomAnimationModeSelected()
                        danceMode1Button.performClick()
                    }

                    getString(R.string.fade_animation) -> {
                        onFadeAnimationModeSelected()
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


        // Set content of the ComposeView to the ColorPicker
        binding.composeColorPicker?.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.composeColorPicker?.setContent {
            ColorPickerContent(
                // define callback function for Color change recompose events
                onColorChanged = { color ->
                    val red = (color.red * 255).toInt()
                    val green = (color.green * 255).toInt()
                    val blue = (color.blue * 255).toInt()

                    // Handle the color change
                    updateLedStripColor(red, green, blue)
                    Log.i(
                        "SHERIF_COLOR_PICKER",
                        "Live Color Changed: R=$red, G=$green, B=$blue"
                    )

                    // Update the Color shared preference and set the background color
                    fadeToColor(
                        binding.colorOverlay2,
                        sharedPreferences.getInt("currentColor", Color.TRANSPARENT),
                        Color.rgb(red, green, blue)
                    )
                    sharedPrefEditor.saveColorToPreferences(Color.rgb(red, green, blue))
                    updateColorSlider()

                    val LED_status_ON: Boolean =
                        getSharedPreferences(
                            "AppPreferences",
                            MODE_PRIVATE
                        ).getBoolean("LED_ON_OFF", true)

                    if (!LED_status_ON) {
                        btnToggleLED.performClick()
                    }
                }
            )
        }


        // ________________________________________________________________________________________________
        // Event Handlers

        selectedModeButton = manualModeButton

        btnToggleLED.setOnClickListener {
            val LED_status_ON: Boolean =
                getSharedPreferences("AppPreferences", MODE_PRIVATE).getBoolean("LED_ON_OFF", true)

            if (LED_status_ON) {
                onLedOff()
                sharedPrefEditor.saveLedOnOffToPreferences(false)
                btnToggleLED.foreground =
                    resources.getDrawable(R.drawable.toggle_led_button_off_foreground, null)
                binding.txtLEDStatus.text = getString(R.string.ambient_light_off)

            } else {
                onLedOn(sharedPreferences)
                sharedPrefEditor.saveLedOnOffToPreferences(true)
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
            onManualModeClick()
            val ledStatusON: Boolean =
                getSharedPreferences("AppPreferences", MODE_PRIVATE).getBoolean("LED_ON_OFF", true)

            if (!ledStatusON) {
                btnToggleLED.performClick()
            }
        }

        adaptiveModeButton.setOnClickListener {
            onAdaptiveModeClick()
            val ledStatusON: Boolean =
                getSharedPreferences("AppPreferences", MODE_PRIVATE).getBoolean("LED_ON_OFF", true)

            if (!ledStatusON) {
                btnToggleLED.performClick()
            }
        }

        varyingModeButton.setOnClickListener {
            hideView(txtAnimationMode)
            sharedPrefEditor.saveVaryingModeToPreferences(null.toString())
            binding.radioGroup.clearCheck()
            onAnimationModeClick()
            // close ambient light //

        }

        danceMode1Button.setOnClickListener {
            onRandomAnimationModeSelected()

            sharedPrefEditor.saveModeToPreferences("varying")
            sharedPrefEditor.saveVaryingModeToPreferences(getString(R.string.random_animation))
            txtAnimationMode.text = getString(R.string.random_animation)
            showView(txtAnimationMode)
            // Set LedStrip On
            val ledStatusON: Boolean =
                getSharedPreferences("AppPreferences", MODE_PRIVATE).getBoolean("LED_ON_OFF", true)
            if (!ledStatusON) {
                btnToggleLED.performClick()
            }
        }

        danceMode2Button.setOnClickListener {
            onFadeAnimationModeSelected()

            sharedPrefEditor.saveModeToPreferences("varying")
            sharedPrefEditor.saveVaryingModeToPreferences(getString(R.string.fade_animation))
            txtAnimationMode.text = getString(R.string.fade_animation)
            showView(txtAnimationMode)
            // Set LedStrip On
            val ledStatusON: Boolean =
                getSharedPreferences("AppPreferences", MODE_PRIVATE).getBoolean("LED_ON_OFF", true)
            if (!ledStatusON) {
                btnToggleLED.performClick()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ledStripServiceClient.unbindService()
    }

    @OptIn(ExperimentalComposeUiApi::class)
    private fun updateColorSlider() {
        // Set content of the ComposeView to the ColorSlider
        binding.composeColorSlider?.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.composeColorSlider?.setContent {
            ColorSlideBar(
                currentColor = androidx.compose.ui.graphics.Color(
                    sharedPreferences.getInt(
                        "currentColor",
                        Color.TRANSPARENT
                    )
                ),
                onProgress = { brightness ->
                    var bright: Int = (brightness * 100).toInt()
                    updateLedStripBrightness(bright)
                    sharedPrefEditor.saveBrightnessToPreferences(bright)
                    Log.i("SHERIF_COLOR_PICKER", "Brightness Changed: $bright")

                    val LED_status_ON: Boolean =
                        getSharedPreferences(
                            "AppPreferences",
                            MODE_PRIVATE
                        ).getBoolean("LED_ON_OFF", true)

                    if (!LED_status_ON) {
                        btnToggleLED.performClick()
                    }
                }
            )
        }
    }


    override fun onDeleteClick(colorId: Int) {
        colorViewModel.deleteColor(colorId)
    }

    override fun onColorClick(color: ColorEntity) {
        fadeToColor(
            binding.colorOverlay2,
            sharedPreferences.getInt("currentColor", Color.TRANSPARENT),
            Color.rgb(color.red, color.green, color.blue)
        )
        sharedPrefEditor.saveColorToPreferences(Color.rgb(color.red, color.green, color.blue))
        updateColorSlider()
        Log.i(
            "SHERIF_COLOR_PICKER",
            "onColorClick: R=${color.red}, G=${color.green}, B=${color.blue}"
        )

        val LED_status_ON: Boolean =
            getSharedPreferences("AppPreferences", MODE_PRIVATE).getBoolean("LED_ON_OFF", true)

        if (!LED_status_ON) {
            btnToggleLED.performClick()
        }

        updateLedStripColor(color.red, color.green, color.blue)
    }

    override fun onManualModeClick() {
        onManualModeSelected(sharedPreferences)

        sharedPrefEditor.saveModeToPreferences("manual")
        sharedPrefEditor.saveVaryingModeToPreferences(null.toString())
        binding.radioGroup.clearCheck()
        selectedModeButton = manualModeButton

        binding.varyingSubModesContainer.translationX = -binding.varyingSubModesContainer.width.toFloat()
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
        animateView(binding.composeColorSlider, "left", "show", 300)
        animateView(binding.btnFavourites, "left", "show", 300)
        animateView(binding.varyingSubModesContainer, "left", "hide", 300)
    }

    override fun onAdaptiveModeClick() {
        onAdaptiveModeSelected()

        sharedPrefEditor.saveModeToPreferences("adaptive")
        sharedPrefEditor.saveVaryingModeToPreferences(null.toString())
        binding.radioGroup.clearCheck()
        selectedModeButton = adaptiveModeButton

        binding.varyingSubModesContainer.translationX = -binding.varyingSubModesContainer.width.toFloat()
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
        animateView(binding.composeColorSlider, "left", "hide", 300)
        animateView(binding.btnFavourites, "left", "hide", 300)
        animateView(binding.varyingSubModesContainer, "left", "hide", 300)

    }

    override fun onAnimationModeClick() {
        selectedModeButton = varyingModeButton
        txtMode.text = getString(R.string.varying_mode)

        expandSection(varyingModeButton, listOf(manualModeButton, adaptiveModeButton))
        imgManualTick.visibility = View.GONE
        imgAdaptiveTick.visibility = View.GONE
        imgVaryingTick.visibility = View.VISIBLE
        showView(varyingSubModesContainer)

        // Change the visibility of color picker
        animateView(binding.rvFavoriteColors, "left", "hide", 300)
        animateView(binding.composeColorPicker, "left", "hide", 300)
        animateView(binding.composeColorSlider, "left", "hide", 300)
        animateView(binding.btnFavourites, "left", "hide", 300)
        animateView(binding.varyingSubModesContainer, "left", "show", 300)


    }
}

