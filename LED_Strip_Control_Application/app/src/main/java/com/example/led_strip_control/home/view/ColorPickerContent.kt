package com.example.led_strip_control.home.view

import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.*
import com.example.led_strip_control.R
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import io.mhssn.colorpicker.ColorPickerType


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ColorPickerContent(
    onAddColor: (Color) -> Unit,
    onColorChanged: (Color) -> Unit
) {
    val controller = rememberColorPickerController()
    val selectedColor = remember { mutableStateOf(Color(1f, 0f, 0f)) } // Default red color


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
            .padding(40.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
        ) {


            Box(
                modifier = Modifier
                    .padding(start = 10.dp, top = 30.dp, end = 20.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_add), // Replace with your actual resource ID
                    contentDescription = "Add Color",
                    modifier = Modifier
                        .size(45.dp) // Adjust size as needed
                        .clickable {
                            // Handle the add color button click
                            // Call your onAddColor() or any other logic here
                            Log.i("SHERIF", "$selectedColor.value.toString()")
                            onAddColor(selectedColor.value)
                            Log.i("SHERIF", "Add color clicked")
                        }
                )
            }

            io.mhssn.colorpicker.ColorPicker(
                type = ColorPickerType.Ring(
                    ringWidth = 30.dp,
                    previewRadius = 30.dp,
                    showDarknessBar = false,
                    showLightnessBar = false,
                    showColorPreview = true,
                    showAlphaBar = false,
                )
            ) { color ->
                val red = (color.red * 255).toInt()
                val green = (color.green * 255).toInt()
                val blue = (color.blue * 255).toInt()
                selectedColor.value = color
                onColorChanged(color)
                Log.i("SHERIF", "RGB Value: R=$red, G=$green, B=$blue")
            }

            //            HsvColorPicker(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(150.dp)
//                    .padding(10.dp),
//                controller = controller,
//                onColorChanged = { color ->
//                    val red = (color.color.red * 255).toInt()
//                    val green = (color.color.green * 255).toInt()
//                    val blue = (color.color.blue * 255).toInt()
//                    selectedColor.value = color.color
//                    onColorChanged(color.color)
//                    Log.i("SHERIF", "RGB Value: R=$red, G=$green, B=$blue")
//                }
//            )

//            BrightnessSlider(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(10.dp)
//                    .height(20.dp),
//                controller = controller
//            )
            // Add Image Button (ic_add)

        }


//        // Color Hints (Small squares on the right side)
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxHeight()
//                .padding(end = 10.dp, top = 40.dp) // Adjust spacing from the right
//                .align(Alignment.CenterEnd), // Position on the right side
//            verticalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            items(colorHints) { color ->
//                ColorHintSquare(color = color, onColorSelected = {
//                    controller.selectByColor(color,true)
//                    selectedColor.value = color
//                })
//            }
//        }
    }
}


//@Composable
//fun ColorHintSquare(color: Color, onColorSelected: (Color) -> Unit) {
//    Box(
//        modifier = Modifier
//            .size(15.dp) // Small square size
//            .background(color)
//            .clickable { onColorSelected(color) }
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun ColorPickerPreview() {
//    // Pass a dummy ViewModel in the preview
//}


////////////
