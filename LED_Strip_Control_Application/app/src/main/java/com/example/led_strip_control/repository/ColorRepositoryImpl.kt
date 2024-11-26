package com.example.led_strip_control.repository

import com.example.led_strip_control.database.ColorLocalDataSource
import com.example.led_strip_control.pojo.ColorEntity
import kotlinx.coroutines.flow.Flow

class ColorRepositoryImpl(
    private val localDataSource: ColorLocalDataSource
) : ColorRepositoryInterface {

    override fun getAllColors(): Flow<List<ColorEntity>> {
        return localDataSource.getFavoriteColors()
    }

    override suspend fun addColor(color: ColorEntity): Boolean {
        return localDataSource.addFavoriteColor(color)
    }

    override suspend fun deleteColor(id: Int) {
        localDataSource.removeFavoriteColor(id)
    }
}

