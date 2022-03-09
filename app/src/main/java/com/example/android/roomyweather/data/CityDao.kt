package com.example.android.roomyweather.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(city: LiteCity)

    @Delete
    suspend fun delete(city: LiteCity)

    @Query(
        "SELECT * FROM LiteCity WHERE name = :name LIMIT 1"
    )
    fun getCity(name: String): Flow<LiteCity?>

    @Query(
        "SELECT * FROM LiteCity"
    )
    fun getAllCities(): List<LiteCity?>

}