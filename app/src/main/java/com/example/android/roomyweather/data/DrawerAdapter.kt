package com.example.android.roomyweather.data
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.roomyweather.R
import com.example.android.roomyweather.ui.OPENWEATHER_APPID
import com.example.android.roomyweather.ui.drawerLayouts
import com.example.android.roomyweather.ui.publicViewModel
import com.google.android.material.card.MaterialCardView
import com.google.android.material.navigation.NavigationView
import java.text.SimpleDateFormat
import java.util.*

class DrawerAdapter : RecyclerView.Adapter<DrawerAdapter.ViewHolder>() {
    val cities: MutableList<LiteCity> = mutableListOf()

    override fun getItemCount() = this.cities.size
    fun addCity(city: LiteCity, position: Int = 0) {
        val city_exists = this.cities.firstOrNull{it.name == city.name}
        if(city_exists != null) {
            city_exists.timestamp = city.timestamp
        } else {
            this.cities.add(position, city)
        }
        cities.sortByDescending { it.timestamp }
        this.notifyDataSetChanged()
        Log.d("Blue","HERE IN addCity ${city}")
        Log.d("Blue","HERE IN addCity ${cities}")
    }

    fun deleteCity(position: Int): LiteCity {
        val city = this.cities.removeAt(position)
        this.notifyItemRemoved(position)
        return city
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.drawer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(this.cities[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val cityView: TextView = view.findViewById(R.id.drawer_city)
        private val dateView: TextView = view.findViewById(R.id.drawer_date)
        private val card: MaterialCardView = view.findViewById(R.id.rv_card)
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(view.getContext())
        var editor = sharedPrefs.edit()
        val city = sharedPrefs.getString("city", "Corvallis,OR,US")
//        init {
//            card.setOnClickListener() {
//                Log.d("blue","CARD CLICK$it ${cityView.text}")
//            }
//        }

        fun bind(city: LiteCity) {
            val simpleDate = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
            val currentDate = simpleDate.format(city.timestamp)
            this.cityView.text = city.name
            this.dateView.text = currentDate
            card.setOnClickListener() {
                Log.d("blue", "CARD CLICK${city}")
                editor.putString("city", city.name)
                editor.apply()
                val city = sharedPrefs.getString("city", "Corvallis,OR,US")
                val units = sharedPrefs.getString("units", "metric")
                Log.d("blue", "${city}")
                drawerLayouts?.close()
                publicViewModel?.loadFiveDayForecast(city, units, OPENWEATHER_APPID)
                val city_exists = cities.firstOrNull{it.name == city}
                if(city_exists != null) {
                    city_exists.timestamp = System.currentTimeMillis()
                }
                cities.sortByDescending { it.timestamp }
                notifyDataSetChanged()
            }
        }
    }
}