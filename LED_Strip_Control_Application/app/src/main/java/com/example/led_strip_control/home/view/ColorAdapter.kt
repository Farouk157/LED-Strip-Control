package com.example.led_strip_control.home.view

import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.led_strip_control.databinding.ItemColorBinding
import com.example.led_strip_control.pojo.ColorEntity
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.ContextCompat
import com.example.led_strip_control.R

class ColorAdapter(val action : OnMainClickListener) :
    ListAdapter<ColorEntity, ColorAdapter.ColorViewHolder>(ColorDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val binding = ItemColorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ColorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val color = getItem(position)
        holder.bind(color)
        holder.itemView.setOnClickListener {
            action.onColorClick(color)
        }
    }

    inner class ColorViewHolder(private val binding: ItemColorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(color: ColorEntity) {
            // Set the background color of the colorView
            binding.colorView.setBackgroundColor(
                android.graphics.Color.rgb(color.red, color.green, color.blue)
            )

        }
    }
}


fun setupSwipeToDelete(recyclerView: RecyclerView, adapter: ColorAdapter) {
    val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.UP) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            if (direction == ItemTouchHelper.UP) {
                val position = viewHolder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val colorId = adapter.currentList[position].id
                    adapter.action.onDeleteClick(colorId)
                }
            }
        }
        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            val itemView = viewHolder.itemView

            // Set up background and icon
            val background = Paint().apply { color = Color.RED }
            val deleteIcon = ContextCompat.getDrawable(recyclerView.context, R.drawable.ic_delete)
            val iconMargin = 10 // Margin between icon and bottom of the item, adjust as needed
            val cornerRadius = 16f // Adjust for rounded corners (in pixels)

            // Convert 100dp to pixels based on the device's density
            val density = recyclerView.context.resources.displayMetrics.density
            val backgroundHeight = 55 * density

            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                // Draw background for swipe up
                if (dY < 0) { // Swiping up
                    val backgroundRect = RectF(
                        itemView.left.toFloat(),
                        itemView.bottom - backgroundHeight.toFloat(),
                        itemView.right.toFloat(),
                        itemView.bottom.toFloat()
                    )
                    // Draw rounded rectangle as the background
                    c.drawRoundRect(backgroundRect, cornerRadius, cornerRadius, background)

                    // Draw delete icon within the reduced height area
                    val iconLeft = (itemView.left + (itemView.width - deleteIcon!!.intrinsicWidth) / 2)
                    val iconRight = iconLeft + deleteIcon.intrinsicWidth
                    val iconBottom = (itemView.bottom - (backgroundHeight / 2) + deleteIcon!!.intrinsicHeight / 2).toInt()
                    val iconTop = iconBottom - deleteIcon.intrinsicHeight

                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    deleteIcon.draw(c)
                }
            }

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }


    }

    val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
    itemTouchHelper.attachToRecyclerView(recyclerView)
}
