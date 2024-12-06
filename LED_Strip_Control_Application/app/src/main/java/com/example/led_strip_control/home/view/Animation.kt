package com.example.led_strip_control.home.view

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.PaintDrawable
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import androidx.core.animation.addListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

/////////////////////////
// Animation

fun expandSection(selectedButton: View, otherButtons: List<Button>) {
    // Reset all buttons to their initial state
//    val currentMode = sharedPreferences.getString("SELECTED_MODE", "default_value")

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


fun toggleVisibility(view: View) {
    val isVisible = view.visibility == View.VISIBLE
    view.visibility = if (isVisible) View.GONE else View.VISIBLE
    ObjectAnimator.ofFloat(view, "alpha", if (isVisible) 1f else 0f, if (isVisible) 0f else 1f)
        .apply {
            duration = 300
            start()
        }
}

fun showView(view: View) {
    if (view.visibility == View.GONE) {
        ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
            duration = 300
            start()
        }.addListener(onEnd = { view.visibility = View.VISIBLE })
    }
}

fun hideView(view: View) {
    if (view.visibility == View.VISIBLE) { // Only hide if visible
        ObjectAnimator.ofFloat(view, "alpha", 1f, 0f).apply {
            duration = 300
            start()
        }.addListener(onEnd = { view.visibility = View.GONE })
    }
}

fun resetContainerPosition(view: View) {
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


fun scrollToLastItem(recyclerView: RecyclerView) {
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


//fun setRadialGradient(colors: List<Int>) {
//    val gradient = RadialGradient(
//        binding.colorOverlay.width / 1.5f, // Center X
//        binding.colorOverlay.height / 1.5f, // Center Y
//        1200f, // Radius
//        colors.toIntArray(), // Colors array
//        null, // Stops (can be null for evenly spaced)
//        Shader.TileMode.CLAMP // Gradient mode
//    )
//    binding.colorOverlay.background = PaintDrawable().apply {
//        paint.shader = gradient
//    }
//}

fun fadeToColor(overlay: View, startColor: Int, newColor: Int) {
    val fadeIn = ObjectAnimator.ofInt(
        overlay,
        "backgroundColor",
        startColor,
        newColor
    )
    fadeIn.duration = 1000 // 1 second duration
    fadeIn.setEvaluator(ArgbEvaluator())
    fadeIn.start()
}