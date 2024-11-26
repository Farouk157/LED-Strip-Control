package com.example.led_strip_control.home.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.led_strip_control.database.ColorUiState
import com.example.led_strip_control.pojo.ColorEntity
import com.example.led_strip_control.repository.ColorRepositoryInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ColorViewModel(private val repository: ColorRepositoryInterface) : ViewModel() {
    private val _uiState = MutableStateFlow<ColorUiState>(ColorUiState.Idle)
    val uiState: StateFlow<ColorUiState> = _uiState.asStateFlow()

    private val _colors = MutableStateFlow<List<ColorEntity>>(emptyList())
    val colors: StateFlow<List<ColorEntity>> = _colors.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllColors()
                .collectLatest { colorList ->
                    _colors.value = colorList
                }
        }
    }

    fun addColor(color: ColorEntity) {
        viewModelScope.launch {
            val added = repository.addColor(color)
            if (added) {
                _uiState.value = ColorUiState.ColorAdded
            } else {
                _uiState.value = ColorUiState.ColorAlreadyExists
            }
        }
    }

    fun deleteColor(id: Int) {
        viewModelScope.launch {
            repository.deleteColor(id)
            _uiState.value = ColorUiState.ColorRemoved(id)
        }
    }

    fun resetUiState() {
        _uiState.value = ColorUiState.Idle
    }
}
