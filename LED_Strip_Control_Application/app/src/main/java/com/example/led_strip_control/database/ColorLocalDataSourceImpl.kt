package com.example.led_strip_control.database

import android.content.Context
import com.example.led_strip_control.pojo.ColorEntity
import kotlinx.coroutines.flow.Flow

class ColorLocalDataSourceImpl(private val context: Context) : ColorLocalDataSource {

    private val colorDao : ColorDao = AppDatabase.getDatabase(context).colorDao()

    override fun getFavoriteColors(): Flow<List<ColorEntity>> = colorDao.getAllColors()

    override suspend fun addFavoriteColor(color: ColorEntity): Boolean {
        val exists = colorDao.findColor(color.red, color.green, color.blue) > 0
        if (!exists) {
            colorDao.addColor(color)
            return true
        }
        return false
    }

    override suspend fun removeFavoriteColor(id: Int) {
        colorDao.deleteColor(id)
    }

    companion object {
        @Volatile
        private var INSTANCE: ColorLocalDataSource? = null

        fun getInstance(context: Context): ColorLocalDataSource {
            return INSTANCE ?: synchronized(this) {
                val instance = ColorLocalDataSourceImpl(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
