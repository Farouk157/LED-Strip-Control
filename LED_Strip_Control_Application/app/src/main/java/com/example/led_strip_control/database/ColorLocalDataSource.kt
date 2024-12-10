package com.example.led_strip_control.database

import com.example.led_strip_control.pojo.ColorEntity
import kotlinx.coroutines.flow.Flow

interface ColorLocalDataSource {
    fun getFavoriteColors(): Flow<List<ColorEntity>>
    suspend fun addFavoriteColor(color: ColorEntity): Boolean
    suspend fun removeFavoriteColor(id: Int)
}
