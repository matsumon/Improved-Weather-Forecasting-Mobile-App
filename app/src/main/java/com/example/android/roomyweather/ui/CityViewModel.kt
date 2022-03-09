package com.example.android.roomyweather.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.android.roomyweather.data.AppDatabase
import com.example.android.roomyweather.data.CityRepository
import kotlinx.coroutines.launch

class CityViewModel(application: Application) : AndroidViewModel(application)  {
    private val repository = CityRepository(
        AppDatabase.getInstance(application).cityDao()
    )

    val bookmarkedRepos = repository.getAllCities()

    fun insertCity(name:String, timestamp: Long? = null) {
        viewModelScope.launch {
            repository.insertCity(name, timestamp)
        }
    }

    fun deleteCity(name:String) {
        viewModelScope.launch {
            repository.deleteCity(name)
        }
    }

    fun getCity(name: String) = repository.getCity(name).asLiveData()
}