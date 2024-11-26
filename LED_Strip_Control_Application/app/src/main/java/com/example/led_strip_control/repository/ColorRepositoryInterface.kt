package com.example.led_strip_control.repository

import com.example.led_strip_control.pojo.ColorEntity
import kotlinx.coroutines.flow.Flow

interface ColorRepositoryInterface {
    fun getAllColors(): Flow<List<ColorEntity>>
    suspend fun addColor(color: ColorEntity): Boolean
    suspend fun deleteColor(id: Int)
}

