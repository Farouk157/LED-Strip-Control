package com.example.led_strip_control.home.view

import com.example.led_strip_control.pojo.ColorEntity

interface OnMainClickListener {
    fun onDeleteClick(colorId: Int)
    fun onColorClick(color: ColorEntity)

    fun onManualModeClick()
    fun onAdaptiveModeClick()
    fun onAnimationModeClick()
}
