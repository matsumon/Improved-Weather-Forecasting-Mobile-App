package com.example.android.roomyweather.data

class CityRepository (
        private val dao: CityDao
    ) {
    suspend fun insertCity(name: String, timestamp: Long? = null) = dao.insert(LiteCity(name, timestamp))
    suspend fun deleteCity(name: String, timestamp: Long? = null) = dao.delete(LiteCity(name, timestamp))
    fun getCity(name: String) = dao.getCity(name)
    fun getAllCities() = dao.getAllCities()
}