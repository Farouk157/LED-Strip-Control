package com.example.led_strip_control.home.view

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draw.scale
import io.mhssn.colorpicker.ColorPickerType

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ColorPickerContent(
    onColorChanged: (Color) -> Unit
) {
    val controller = rememberColorPickerController()
//    val selectedColor = remember { mutableStateOf(Color(1f, 0f, 0f)) } // Default red color

    Box(
        modifier = Modifier
            .size(300.dp)
            .padding(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            io.mhssn.colorpicker.ColorPicker(
                type = ColorPickerType.Ring(
                    ringWidth = 30.dp,
                    previewRadius = 30.dp,
                    showDarknessBar = false,
                    showLightnessBar = false,
                    showColorPreview = true,
                    showAlphaBar = false,
                ),
                modifier = Modifier
                    .scale(1.3f)
            ) { color ->
                val red = (color.red * 255).toInt()
                val green = (color.green * 255).toInt()
                val blue = (color.blue * 255).toInt()
//                selectedColor.value = colo

                onColorChanged(color)

                // Log the RGB values and brightness percentage
                Log.i("SHERIF", "RGB Value: R=$red, G=$green, B=$blue")
            }
        }

    }
}

