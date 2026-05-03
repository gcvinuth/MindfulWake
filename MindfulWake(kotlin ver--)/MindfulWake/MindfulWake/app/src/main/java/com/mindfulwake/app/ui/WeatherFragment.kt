package com.mindfulwake.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.mindfulwake.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class WeatherFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_weather, container, false)

        val tempView = view.findViewById<TextView>(R.id.weather_temp)
        val descView = view.findViewById<TextView>(R.id.weather_desc)
        val locationView = view.findViewById<TextView>(R.id.weather_location)
        val statusView = view.findViewById<TextView>(R.id.weather_status)
        val refreshBtn = view.findViewById<Button>(R.id.refresh_weather_btn)
        val cityInput = view.findViewById<EditText>(R.id.city_input)
        val searchBtn = view.findViewById<Button>(R.id.search_city_btn)
        val detailsLayout = view.findViewById<LinearLayout>(R.id.weather_details)
        val highLow = view.findViewById<TextView>(R.id.weather_high_low)
        val feelsLike = view.findViewById<TextView>(R.id.weather_feels_like)
        val humidity = view.findViewById<TextView>(R.id.weather_humidity)
        val wind = view.findViewById<TextView>(R.id.weather_wind)

        fun fetchForCoords(lat: Double, lon: Double, city: String? = null) {
            statusView.text = "Loading…"
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
                            "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m" +
                            "&daily=temperature_2m_max,temperature_2m_min&timezone=auto&forecast_days=1"
                    val json = JSONObject(URL(url).readText())
                    val current = json.getJSONObject("current")
                    val daily = json.getJSONObject("daily")
                    val temp = current.getDouble("temperature_2m").toInt()
                    val feelsLikeTemp = current.getDouble("apparent_temperature").toInt()
                    val hum = current.getInt("relative_humidity_2m")
                    val windSpeed = current.getDouble("wind_speed_10m").toInt()
                    val code = current.getInt("weather_code")
                    val maxTemp = daily.getJSONArray("temperature_2m_max").getDouble(0).toInt()
                    val minTemp = daily.getJSONArray("temperature_2m_min").getDouble(0).toInt()

                    val weatherIcon = getWeatherIcon(code)
                    val weatherDesc = getWeatherDesc(code)

                    withContext(Dispatchers.Main) {
                        tempView.text = "$temp°C"
                        descView.text = "$weatherIcon $weatherDesc"
                        highLow.text = "H: $maxTemp°  L: $minTemp°"
                        feelsLike.text = "Feels like: $feelsLikeTemp°"
                        humidity.text = "Humidity: $hum%"
                        wind.text = "Wind: $windSpeed km/h"
                        locationView.text = city ?: "Current Location"
                        statusView.text = ""
                        detailsLayout.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        statusView.text = "Failed to load weather. Check connection."
                    }
                }
            }
        }

        fun requestLocation() {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 200)
                statusView.text = "Location permission required. Use city search below."
                return
            }
            val lm = requireContext().getSystemService(LocationManager::class.java)
            statusView.text = "Getting location…"
            try {
                val loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    ?: lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (loc != null) fetchForCoords(loc.latitude, loc.longitude)
                else statusView.text = "Could not get location. Try city search."
            } catch (e: Exception) {
                statusView.text = "Location error. Try city search."
            }
        }

        fun searchCity(name: String) {
            statusView.text = "Searching for $name…"
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val url = "https://geocoding-api.open-meteo.com/v1/search?name=${android.net.Uri.encode(name)}&count=1&format=json"
                    val json = JSONObject(URL(url).readText())
                    val results = json.optJSONArray("results")
                    if (results != null && results.length() > 0) {
                        val r = results.getJSONObject(0)
                        val lat = r.getDouble("latitude")
                        val lon = r.getDouble("longitude")
                        val cityName = r.getString("name")
                        withContext(Dispatchers.Main) { fetchForCoords(lat, lon, cityName) }
                    } else {
                        withContext(Dispatchers.Main) { statusView.text = "City not found." }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { statusView.text = "Search failed." }
                }
            }
        }

        refreshBtn.setOnClickListener { requestLocation() }
        searchBtn.setOnClickListener {
            val city = cityInput.text.toString().trim()
            if (city.isNotEmpty()) searchCity(city)
        }

        requestLocation()
        return view
    }

    private fun getWeatherIcon(code: Int) = when (code) {
        0 -> "☀️"; 1 -> "🌤️"; 2 -> "⛅"; 3 -> "☁️"
        45, 48 -> "🌫️"; in 51..67 -> "🌧️"; in 71..77 -> "🌨️"
        95, 96, 99 -> "⛈️"; else -> "🌡️"
    }

    private fun getWeatherDesc(code: Int) = when (code) {
        0 -> "Clear sky"; 1 -> "Mainly clear"; 2 -> "Partly cloudy"; 3 -> "Overcast"
        45 -> "Fog"; 48 -> "Icy fog"; 51 -> "Light drizzle"; 61 -> "Light rain"
        63 -> "Moderate rain"; 65 -> "Heavy rain"; 71 -> "Light snow"; 73 -> "Moderate snow"
        75 -> "Heavy snow"; 95 -> "Thunderstorm"; else -> "Cloudy"
    }
}
