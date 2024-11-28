package com.example.led_strip_control.home.view

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.led_strip_control.database.ColorLocalDataSourceImpl
import com.example.led_strip_control.databinding.ActivityMainBinding
import com.example.led_strip_control.home.view_model.ColorViewModel
import com.example.led_strip_control.home.view_model.ColorViewModelFactory
import com.example.led_strip_control.pojo.ColorEntity
import com.example.led_strip_control.repository.ColorRepositoryImpl
import com.example.led_strip_control.service_client.LedStripServiceClient


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var colorAdapter: ColorAdapter
    private lateinit var colorViewModel: ColorViewModel

    private lateinit var ledStripServiceClient: LedStripServiceClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ledStripServiceClient = LedStripServiceClient.getInstance(this)
        ledStripServiceClient.bindService()

        for (i in 0 until 8) {
            ledStripServiceClient.setColor(i, 0,255, 0)
        }


        val colorRepository = ColorRepositoryImpl(ColorLocalDataSourceImpl.getInstance(this))
        colorViewModel = ViewModelProvider(
            this,
            ColorViewModelFactory(colorRepository)
        ).get(ColorViewModel::class.java)

        colorAdapter = ColorAdapter { colorId ->
            colorViewModel.deleteColor(colorId)
        }

        binding.rvFavoriteColors?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvFavoriteColors?.adapter = colorAdapter

        binding.rvFavoriteColors?.let { setupSwipeToDelete(it, colorAdapter) }


        lifecycleScope.launchWhenStarted {
            colorViewModel.colors.collect { colorList ->
                colorAdapter.submitList(colorList)
            }
        }


        binding.composeColorPicker?.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.composeColorPicker?.setContent {
            ColorPickerContent(
                onAddColor = { color ->
                    val colorEntity = ColorEntity(
                        red = (color.red * 255).toInt(),
                        green = (color.green * 255).toInt(),
                        blue = (color.blue * 255).toInt()
                    )
                    colorViewModel.addColor(colorEntity)
                },
                onColorChanged = { color ->
                    val red = (color.red * 255).toInt()
                    val green = (color.green * 255).toInt()
                    val blue = (color.blue * 255).toInt()
                    // Handle the color background
                    for (i in 0 until 8) {
                        ledStripServiceClient.setColor(i,red,green,blue)
                    }
                    Log.i("SHERIF_COLOR_PICKER", "Live Color Changed: R=$red, G=$green, B=$blue")
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ledStripServiceClient.unbindService()
    }
}
