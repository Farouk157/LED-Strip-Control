package com.example.led_strip_control

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.*

@Composable
fun ColorPickerContent() {
    val controller = rememberColorPickerController()

    // List of sample colors for the hint list
    val colorHints = listOf(
        Color(0xFFFF0000), // Red
        Color(0xFF00FF00), // Green
        Color(0xFF0000FF), // Blue
        Color(0xFFFFFF00), // Yellow
        Color(0xFF00FFFF), // Cyan
        Color(0xFFFF00FF)  // Magenta
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp)
    ) {
        // Main content (Color Picker)
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                AlphaTile(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    controller = controller
                )
            }

            HsvColorPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
                    .padding(10.dp),
                controller = controller,
                onColorChanged = { color ->
                    // Log the RGB value when the color changes
                    val red = (color.color.red * 255).toInt()
                    val green = (color.color.green * 255).toInt()
                    val blue = (color.color.blue * 255).toInt()
                    Log.i("SHERIF", "RGB Value: R=$red, G=$green, B=$blue")
                }
            )

            AlphaSlider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .height(35.dp),
                controller = controller
            )

            BrightnessSlider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .height(35.dp),
                controller = controller
            )
        }

        // Color Hints (Small squares on the right side)
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .padding(end = 10.dp , top = 150.dp) // Adjust spacing from the right
                .align(Alignment.CenterEnd), // Position on the right side
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(colorHints) { color ->
                ColorHintSquare(color = color, onColorSelected = {
                    controller.selectByColor(color,true)
                })
            }
        }
    }
}


@Composable
fun ColorHintSquare(color: Color, onColorSelected: (Color) -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp) // Small square size
            .background(color)
            .clickable { onColorSelected(color) }
    )
}

@Preview(showBackground = true)
@Composable
fun ColorPickerPreview() {
    ColorPickerContent()
}
