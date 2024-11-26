package com.example.led_strip_control.home.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.led_strip_control.databinding.ItemColorBinding
import com.example.led_strip_control.pojo.ColorEntity

class ColorAdapter(private val onDeleteClick: (Int) -> Unit) :
    ListAdapter<ColorEntity, ColorAdapter.ColorViewHolder>(ColorDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val binding = ItemColorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ColorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val color = getItem(position)
        holder.bind(color)
    }

    inner class ColorViewHolder(private val binding: ItemColorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(color: ColorEntity) {
            binding.redText.text = "Red: ${color.red}"
            binding.greenText.text = "Green: ${color.green}"
            binding.blueText.text = "Blue: ${color.blue}"

            binding.deleteButton.setOnClickListener {
                onDeleteClick(color.id)
            }
        }
    }
}
