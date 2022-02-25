package com.example.android.roomyweather.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LiteCity(
    @PrimaryKey
    val name: String,
    val timestamp: Long?
)
