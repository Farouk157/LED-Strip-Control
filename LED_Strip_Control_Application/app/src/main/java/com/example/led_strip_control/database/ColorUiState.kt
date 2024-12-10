package com.example.led_strip_control.database

sealed class ColorUiState {
    object Idle : ColorUiState()
    object ColorAdded : ColorUiState()
    data class ColorRemoved(val id: Int) : ColorUiState()
    object ColorAlreadyExists : ColorUiState()
}
