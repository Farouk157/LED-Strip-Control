package com.example.led_strip_control.pojo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "colors")
data class ColorEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val red: Int,
    val green: Int,
    val blue: Int
)

