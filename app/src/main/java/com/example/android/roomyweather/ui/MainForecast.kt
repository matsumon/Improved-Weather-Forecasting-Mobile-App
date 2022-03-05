package com.example.android.roomyweather.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.roomyweather.BuildConfig
import com.example.android.roomyweather.R
import com.example.android.roomyweather.data.*
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar

/*
 * To use your own OpenWeather API key, create a file called `gradle.properties` in your
 * GRADLE_USER_HOME directory (this will usually be `$HOME/.gradle/` in MacOS/Linux and
 * `$USER_HOME/.gradle/` in Windows), and add the following line:
 *
 *   OPENWEATHER_API_KEY="<put_your_own_OpenWeather_API_key_here>"
 *
 * The Gradle build for this project is configured to automatically grab that value and store
 * it in the field `BuildConfig.OPENWEATHER_API_KEY` that's used below.  You can read more
 * about this setup on the following pages:
 *
 *   https://developer.android.com/studio/build/gradle-tips#share-custom-fields-and-resource-values-with-your-app-code
 *
 *   https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_configuration_properties
 *
 * Alternatively, you can just hard-code your API key below 🤷‍.
 */
const val OPENWEATHER_APPID = BuildConfig.OPENWEATHER_API_KEY
var publicViewModel:FiveDayForecastViewModel? = null
class MainForecast : Fragment(R.layout.main_forecast) {
//    private val tag = "MainActivity"

    private val viewModel: FiveDayForecastViewModel by viewModels()
    private val cityViewModel: CityViewModel by viewModels()
    private var cityName: String? = null
    private lateinit var forecastAdapter: ForecastAdapter

    private lateinit var forecastListRV: RecyclerView
    private lateinit var loadingErrorTV: TextView
    private lateinit var loadingIndicator: CircularProgressIndicator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        publicViewModel = viewModel
        loadingErrorTV = view.findViewById(R.id.tv_loading_error)
        loadingIndicator = view.findViewById(R.id.loading_indicator)
        forecastListRV = view.findViewById(R.id.rv_forecast_list)

        forecastAdapter = ForecastAdapter(::onForecastItemClick)

        forecastListRV.layoutManager = LinearLayoutManager(requireContext())
        forecastListRV.setHasFixedSize(true)
        forecastListRV.adapter = forecastAdapter
        settingsButton?.setOnClickListener(){
            val directions = MainForecastDirections.navigateToSettings()
            findNavController().navigate(directions)
            drawerLayouts?.close()
        }
        /*
         * Observe forecast data.  Whenever forecast data changes, display it in the RecyclerView.
         */
        viewModel.forecast.observe(viewLifecycleOwner) { forecast ->
            if (forecast != null) {
                forecastAdapter.updateForecast(forecast)
                forecastListRV.visibility = View.VISIBLE
                forecastListRV.scrollToPosition(0)
//                supportActionBar?.title = forecast.city.name
                cityName = forecast.city.name
            }
        }

        /*
         * Observe error message.  If an error message is present, display it.
         */
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                loadingErrorTV.text = getString(R.string.loading_error, error.message)
                loadingErrorTV.visibility = View.VISIBLE
            }
        }

        /*
         * Observe loading indicator.  When loading, display progress indicator and hide other
         * UI elements.
         */
        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            if (loading) {
                loadingIndicator.visibility = View.VISIBLE
                loadingErrorTV.visibility = View.INVISIBLE
                forecastListRV.visibility = View.INVISIBLE
            } else {
                loadingIndicator.visibility = View.INVISIBLE
            }
        }
        if(cityName != null) {
            Log.d("blue","HERE: $cityName")
            val current_time = System.currentTimeMillis()
            cityViewModel.insertCity(cityName!!,current_time)
            drawerAdapter.addCity(LiteCity(cityName!!,current_time))
        }
    }

    override fun onResume() {
        super.onResume()

        /*
         * Here, we're reading the current preference values and triggering a data fetching
         * operation in onResume().  This avoids the need to set up a preference change listener.
         * It also means that a new API call could potentially be made every time the activity
         * is resumed.  However, because of the basic caching that's implemented in the
         * FiveDayForecastRepository class, an API call will actually only be made whenever
         * the city or units setting changes (which is exactly what we want).
         */
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val city = sharedPrefs.getString(getString(R.string.pref_city_key), "Corvallis,OR,US")
        val units = sharedPrefs.getString(getString(R.string.pref_units_key), null)
        viewModel.loadFiveDayForecast(city, units, OPENWEATHER_APPID)
        if(city != null) {
            cityName=city
            Log.d("blue","Resume: $city")
            val current_time = System.currentTimeMillis()
            cityViewModel.insertCity(cityName!!,current_time)
            drawerAdapter.addCity(LiteCity(cityName!!,current_time))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.activity_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_map -> {
                viewForecastCityOnMap()
                true
            }
            R.id.action_settings -> {
//                val intent = Intent(this, SettingsActivity::class.java)
//                startActivity(intent)
//                true
                val directions = MainForecastDirections.navigateToSettings()
                findNavController().navigate(directions)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onForecastItemClick(forecastPeriod: ForecastPeriod) {
        val directions = MainForecastDirections.navigateToDetailedForecast(forecastCity = forecastAdapter.forecastCity, forecastPeriod = forecastPeriod)
        findNavController().navigate(directions)
//        val intent = Intent(this, ForecastDetailActivity::class.java).apply {
//            putExtra(EXTRA_FORECAST_PERIOD, forecastPeriod)
//            putExtra(EXTRA_FORECAST_CITY, forecastAdapter.forecastCity)
//        }
//        startActivity(intent)
    }

    /**
     * This method generates a geo URI to represent location of the city for which the forecast
     * is being displayed and uses an implicit intent to view that location on a map.
     */
    private fun viewForecastCityOnMap() {
        if (forecastAdapter.forecastCity != null) {
            val geoUri = Uri.parse(getString(
                R.string.geo_uri,
                forecastAdapter.forecastCity?.lat ?: 0.0,
                forecastAdapter.forecastCity?.lon ?: 0.0,
                11
            ))
            val intent = Intent(Intent.ACTION_VIEW, geoUri)
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                /*
                 * If there is no available app for viewing geo locations, display an error
                 * message in a Snackbar.
                 */
                Snackbar.make(
                    requireView(),
                    R.string.action_map_error,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }
}