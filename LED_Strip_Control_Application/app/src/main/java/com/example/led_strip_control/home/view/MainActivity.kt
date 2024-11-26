package com.example.led_strip_control.home.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.led_strip_control.database.ColorLocalDataSourceImpl
import com.example.led_strip_control.databinding.ActivityMainBinding
import com.example.led_strip_control.home.view_model.ColorViewModel
import com.example.led_strip_control.home.view_model.ColorViewModelFactory
import com.example.led_strip_control.pojo.ColorEntity
import com.example.led_strip_control.repository.ColorRepositoryImpl


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var colorAdapter: ColorAdapter
    private lateinit var colorViewModel: ColorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        val colorRepository = ColorRepositoryImpl(ColorLocalDataSourceImpl.getInstance(this))
        colorViewModel = ViewModelProvider(
            this,
            ColorViewModelFactory(colorRepository)
        ).get(ColorViewModel::class.java)

        // Initialize the ColorAdapter for RecyclerView
        colorAdapter = ColorAdapter { colorId ->
            colorViewModel.deleteColor(colorId)
        }

        // Set the RecyclerView adapter
        binding.rvFavoriteColors?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvFavoriteColors?.adapter = colorAdapter

        // Collect the colors from ViewModel using StateFlow
        lifecycleScope.launchWhenStarted {
            colorViewModel.colors.collect { colorList ->
                // Submit the updated list to the adapter
                colorAdapter.submitList(colorList)
            }
        }

        // Add new color on button click
        binding.btnAddToFavorite?.setOnClickListener {
            val color = ColorEntity(id = 0, red = 255, green = 0, blue = 0) // Example red color
            colorViewModel.addColor(color)
        }

        // Set the Compose content in the ComposeView for color picker
        binding.composeColorPicker?.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.composeColorPicker?.setContent {
            ColorPickerContent()
        }
    }
}




//class MainActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityMainBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        val view = binding.root
//        setContentView(view)
//
//        // Set the Compose content in the ComposeView
//        binding.composeColorPicker?.setViewCompositionStrategy(
//            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
//        )
//        binding.composeColorPicker?.setContent {
//            ColorPickerContent()
//        }
//    }
//}
