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
import androidx.compose.ui.draw.scale
import io.mhssn.colorpicker.ColorPickerType


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ColorPickerContent(
    onAddColor: (Color) -> Unit,
    onColorChanged: (Color) -> Unit
) {
    val controller = rememberColorPickerController()
    val selectedColor = remember { mutableStateOf(Color(1f, 0f, 0f)) } // Default red color

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
                    ringWidth = 35.dp,
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
                selectedColor.value = color
                onColorChanged(color)
                Log.i("SHERIF", "RGB Value: R=$red, G=$green, B=$blue")
            }

//            BrightnessSlider(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(10.dp)
//                    .height(20.dp),
//                controller = controller
//            )

        }


    }
}

