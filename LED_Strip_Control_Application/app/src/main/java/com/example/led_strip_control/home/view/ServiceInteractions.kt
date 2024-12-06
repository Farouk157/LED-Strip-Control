package com.example.led_strip_control.home.view

import android.content.SharedPreferences
import android.util.Log
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.example.led_strip_control.R
import com.example.led_strip_control.home.view.MainActivity.Companion.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

fun MainActivity.updateLedStripColor(red: Int, green: Int, blue: Int) {
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
}

fun MainActivity.updateLedStripBrightness(brightness: Int) {
    val statusBrightness = ledStripServiceClient.setBrightness(brightness)
    if (statusBrightness == null || !statusBrightness.success) {
        Log.e(TAG, "Failed to set the brightness")
    } else {
        Log.i(TAG, "Show Result: ${statusBrightness.message}")
    }
}

fun MainActivity.onManualModeSelected(sharedPreferences: SharedPreferences) {
    val color =
        sharedPreferences.getInt("currentColor", getColor(R.color.your_background_color))
    val brightness = sharedPreferences.getInt("Brightness", 100)
    val red = color.red
    val green = color.green
    val blue = color.blue

    i2cServiceApp.stop()
    job.cancel()
    updateLedStripColor(red, green, blue)
    updateLedStripBrightness(brightness)
}

fun MainActivity.onAdaptiveModeSelected() {
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

    val scope = CoroutineScope(Dispatchers.IO + job)

    scope.launch {

        launch {
            i2cServiceApp.run()
        }

        launch {
            while (isActive) {
                val lastValue = i2cServiceApp.getLastValue() ?: 0
                Log.i(TAG, "Last ADC Value: $lastValue")
                val statusBrightness = ledStripServiceClient.setBrightness(lastValue)
                if (statusBrightness == null || !statusBrightness.success) {
                    Log.e(TAG, "Failed to set the brightness")
                } else {
                    Log.i(TAG, "Show Result: ${statusBrightness.message}")
                }

                sharedPrefEditor.saveBrightnessToPreferences(lastValue)
                Log.i("adaptive", "Brightness Changed: $lastValue")

            }
        }
    }
}


fun MainActivity.onRandomAnimationModeSelected() {
    i2cServiceApp.stop()
    job.cancel()
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


fun MainActivity.onFadeAnimationModeSelected() {
    i2cServiceApp.stop()
    job.cancel()
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

fun MainActivity.onLedOn(sharedPreferences: SharedPreferences) {
    val mode = sharedPreferences.getString("SELECTED_MODE", "manual")
    val animation_mode =
        sharedPreferences.getString("Variation", getString(R.string.random_animation))

    when (mode) {
        "manual" -> {
            onManualModeSelected(sharedPreferences)
        }

        "adaptive" -> {
            onAdaptiveModeSelected()
        }

        "varying" -> {
            when (animation_mode) {
                getString(R.string.random_animation) -> {
                    onRandomAnimationModeSelected()
                }

                getString(R.string.fade_animation) -> {
                    onFadeAnimationModeSelected()
                }
            }
        }
    }
}

fun MainActivity.onLedOff() {
    i2cServiceApp.stop()
    val statusStop = ledStripServiceClient.stopAllModes()
    val statusClear = ledStripServiceClient.clear()
    val statusShow = ledStripServiceClient.show()

    if (statusStop == null || !statusStop.success ||
        statusClear == null || !statusClear.success ||
        statusShow == null || !statusShow.success) {

        val stopMessage = statusStop?.message ?: "stopAllModes() returned null"
        val clearMessage = statusClear?.message ?: "clear() returned null"
        val showMessage = statusShow?.message ?: "show() returned null"

        Log.e(
            TAG,
            "Operation failed. stopAllModes: $stopMessage, clear leds: $clearMessage, show: $showMessage"
        )
    } else {
        Log.i(
            TAG,
            "All operations succeeded. stopAllModes: ${statusStop.message}, clear leds: ${statusClear.message}, show: ${statusShow.message}"
        )
    }
}