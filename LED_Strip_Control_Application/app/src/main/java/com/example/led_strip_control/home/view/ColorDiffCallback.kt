package com.example.led_strip_control.home.view

import androidx.recyclerview.widget.DiffUtil
import com.example.led_strip_control.pojo.ColorEntity

class ColorDiffCallback : DiffUtil.ItemCallback<ColorEntity>() {
    override fun areItemsTheSame(oldItem: ColorEntity, newItem: ColorEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ColorEntity, newItem: ColorEntity): Boolean {
        return oldItem == newItem
    }
}
