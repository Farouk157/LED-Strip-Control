package com.example.led_strip_control.home.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
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
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.graphics.toColor
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import com.example.led_strip_control.service_client.LedStripServiceClient

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
    private lateinit var txtMode: TextView
    private lateinit var txtAnimationMode: TextView

    private lateinit var ledStripServiceClient: LedStripServiceClient


    private val selectedMode = "manual"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //AIDL
        ledStripServiceClient = LedStripServiceClient.getInstance(this)


        //////////////
        varyingSubModesContainer = binding.varyingSubModesContainer
        btnSettings = binding.btnSettings
        btnFavourites = binding.btnFavourites
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

        binding.colorOverlay2?.setBackgroundColor(sharedPreferences.getInt("currentColor", getColor(R.color.your_background_color)))

        when (sharedPreferences.getString("SELECTED_MODE", "manual")) {
            "manual" -> {
                txtMode.text = getString(R.string.manual_mode)
                expandSection(manualModeButton, listOf(adaptiveModeButton, varyingModeButton))
                imgManualTick.visibility = View.VISIBLE
                imgAdaptiveTick.visibility = View.GONE
                imgVaryingTick.visibility = View.GONE
                txtAnimationMode.visibility = View.GONE
                varyingSubModesContainer.visibility = View.GONE
            }

            "adaptive" -> {
                txtMode.text = getString(R.string.adaptive_mode)
                expandSection(adaptiveModeButton, listOf(manualModeButton, varyingModeButton))
                imgManualTick.visibility = View.GONE
                imgAdaptiveTick.visibility = View.VISIBLE
                imgVaryingTick.visibility = View.GONE
                txtAnimationMode.visibility = View.GONE
                varyingSubModesContainer.visibility = View.GONE
            }

            "varying" -> {
                txtMode.text = getString(R.string.varying_mode)
                txtAnimationMode.text = sharedPreferences.getString("Variation", "Snake")
                expandSection(varyingModeButton, listOf(manualModeButton, adaptiveModeButton))
                imgManualTick.visibility = View.GONE
                imgAdaptiveTick.visibility = View.GONE
                imgVaryingTick.visibility = View.VISIBLE
                txtAnimationMode.visibility = View.VISIBLE
                varyingSubModesContainer.visibility = View.VISIBLE
                when (sharedPreferences.getString("Variation", getString(R.string.sake_animation))) {
                    getString(R.string.sake_animation) -> {
                        danceMode1Button.performClick()
                    }

                    getString(R.string.sake_animation) -> {
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
//                    binding.rvFavoriteColors.postDelayed({
//                        showRecyclerView(binding.rvFavoriteColors)
//                    }, 100)

                },
                onColorChanged = { color ->
                    val red = (color.red * 255).toInt()
                    val green = (color.green * 255).toInt()
                    val blue = (color.blue * 255).toInt()

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

                    // Handle the color background
                    setAmbientColor(Color.rgb(red, green, blue))
                    Log.i("SHERIF_COLOR_PICKER", "Live Color Changed: R=$red, G=$green, B=$blue")
                }
            )
        }


        //////////////////////////////////////////////////////////////////////////////////////
        // ________________________________________________________________________________________________
        // Modes

        selectedModeButton = manualModeButton

        btnSettings.setOnClickListener {
            toggleVisibility(binding.modeContainer)
            toggleVisibility(binding.shade2)
        }
        btnFavourites.setOnClickListener {
//            toggleVisibility(binding.rvFavoriteColors)
//            toggleRecyclerViewVisibility(binding.rvFavoriteColors)
            val color = sharedPreferences.getInt("currentColor", getColor(R.color.your_background_color))
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
            saveModeToPreferences("manual")
            selectedModeButton = manualModeButton
            txtMode.text = getString(R.string.manual_mode)

            resetContainerPosition(varyingSubModesContainer)
            expandSection(manualModeButton, listOf(adaptiveModeButton, varyingModeButton))
            imgManualTick.visibility = View.VISIBLE
            imgAdaptiveTick.visibility = View.GONE
            imgVaryingTick.visibility = View.GONE
            txtAnimationMode.visibility = View.GONE
            hideView(varyingSubModesContainer)
        }

        adaptiveModeButton.setOnClickListener {
            saveModeToPreferences("adaptive")
            selectedModeButton = adaptiveModeButton
            txtMode.text = getString(R.string.adaptive_mode)

            resetContainerPosition(varyingSubModesContainer)
            expandSection(adaptiveModeButton, listOf(manualModeButton, varyingModeButton))
            imgManualTick.visibility = View.GONE
            imgAdaptiveTick.visibility = View.VISIBLE
            imgVaryingTick.visibility = View.GONE
            txtAnimationMode.visibility = View.GONE
            hideView(varyingSubModesContainer)
        }

        varyingModeButton.setOnClickListener {
            saveModeToPreferences("varying")
            selectedModeButton = varyingModeButton
            txtMode.text = getString(R.string.varying_mode)

            expandSection(varyingModeButton, listOf(manualModeButton, adaptiveModeButton))
            imgManualTick.visibility = View.GONE
            imgAdaptiveTick.visibility = View.GONE
            imgVaryingTick.visibility = View.VISIBLE
            toggleVisibility(varyingSubModesContainer)
            toggleVisibility(txtAnimationMode)

            danceMode1Button.performClick()
        }

        danceMode1Button.setOnClickListener {
            saveVaryingModeToPreferences(getString(R.string.sake_animation))
            txtAnimationMode.text = getString(R.string.sake_animation)
        }

        danceMode2Button.setOnClickListener {
            saveVaryingModeToPreferences(getString(R.string.fade_animation))
            txtAnimationMode.text = getString(R.string.fade_animation)
            val statusStop = ledStripServiceClient.stopAllModes()
            val status = ledStripServiceClient.setGlobalFade()

            if (statusStop == null || !statusStop.success || status == null || !status.success) {
                val stopMessage = statusStop?.message ?: "stopAllModes() returned null"
                val fadeMessage = status?.message ?: "setGlobalFade() returned null"
                Log.e(TAG, "Operation failed. stopAllModes: $stopMessage, setGlobalFade: $fadeMessage")
            } else {
                Log.i(TAG, "Both operations succeeded. stopAllModes: ${statusStop.message}, setGlobalFade: ${status.message}")
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
            val statusSetColor = ledStripServiceClient.setColor(i, color.red, color.green, color.blue)
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

    private fun toggleRecyclerViewVisibility(recyclerView: View) {
        val isVisible = recyclerView.visibility == View.VISIBLE

        // Set up animations
        val translationXStart = if (isVisible) 0f else -recyclerView.width.toFloat()
        val translationXEnd = if (isVisible) -recyclerView.width.toFloat() else 0f
        val alphaStart = if (isVisible) 1f else 0f
        val alphaEnd = if (isVisible) 0f else 1f

        // Ensure RecyclerView is visible before starting the animation if showing
        if (!isVisible) recyclerView.visibility = View.VISIBLE

        // Create translation and alpha animations
        val translationAnimator =
            ObjectAnimator.ofFloat(recyclerView, "translationX", translationXStart, translationXEnd)
        val alphaAnimator = ObjectAnimator.ofFloat(recyclerView, "alpha", alphaStart, alphaEnd)

        // Play animations together
        AnimatorSet().apply {
            playTogether(translationAnimator, alphaAnimator)
            duration = 300
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (isVisible) {
                        recyclerView.visibility = View.GONE // Hide only after animation ends
                    }
                }
            })
            start()
        }
    }

    private fun showRecyclerView(recyclerView: View) {
        // If the RecyclerView is already visible, do nothing
        if (recyclerView.visibility == View.VISIBLE) return

        // Set up animations
        val translationXStart = -recyclerView.width.toFloat() // Start off-screen
        val translationXEnd = 0f // End fully visible
        val alphaStart = 0f
        val alphaEnd = 1f

        // Ensure RecyclerView is visible before starting the animation
        recyclerView.visibility = View.VISIBLE

        // Create translation and alpha animations
        val translationAnimator =
            ObjectAnimator.ofFloat(recyclerView, "translationX", translationXStart, translationXEnd)
        val alphaAnimator = ObjectAnimator.ofFloat(recyclerView, "alpha", alphaStart, alphaEnd)

        // Play animations together
        AnimatorSet().apply {
            playTogether(translationAnimator, alphaAnimator)
            duration = 300
            start()
        }
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
