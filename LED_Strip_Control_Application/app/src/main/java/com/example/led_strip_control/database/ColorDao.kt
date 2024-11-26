package com.example.led_strip_control.database

import androidx.room.*
import com.example.led_strip_control.pojo.ColorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ColorDao {
    @Query("SELECT * FROM colors")
    fun getAllColors(): Flow<List<ColorEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addColor(color: ColorEntity)

    @Delete
    suspend fun deleteColor(color: ColorEntity)

    @Query("DELETE FROM colors WHERE id = :id")
    suspend fun deleteColor(id: Int)

    @Query("SELECT COUNT(*) FROM colors WHERE red = :red AND green = :green AND blue = :blue")
    suspend fun findColor(red: Int, green: Int, blue: Int): Int
}
