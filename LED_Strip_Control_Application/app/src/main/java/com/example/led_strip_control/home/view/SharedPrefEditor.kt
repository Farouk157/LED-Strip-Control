package com.example.led_strip_control.home.view

import android.content.SharedPreferences

class SharedPrefEditor(val sharedPreferences: SharedPreferences) {

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

    fun saveFirstRunFlag(flag: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("first_run", flag)
        editor.apply()
    }
}
