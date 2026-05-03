package com.mindfulwake.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.mindfulwake.data.models.*
import com.mindfulwake.data.repository.*
import com.mindfulwake.services.AlarmScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// ===== ALARM VIEWMODEL =====
class AlarmViewModel(application: Application) : AndroidViewModel(application) {
    private val db = MindfulWakeDatabase.getDatabase(application)
    private val alarmRepo = AlarmRepository(db)
    private val questionRepo = QuestionRepository(db)

    val alarms = alarmRepo.allAlarms

    fun createAlarm(
        hour: Int, minute: Int, label: String,
        questionCount: Int, questionSource: QuestionSource,
        difficulty: Difficulty, repeatDays: Set<Int>
    ) {
        viewModelScope.launch {
            val alarm = Alarm(
                hour = hour, minute = minute, label = label,
                questionCount = questionCount, questionSource = questionSource,
                difficulty = difficulty, repeatDays = repeatDays
            )
            val id = alarmRepo.insert(alarm)
            val saved = alarm.copy(id = id.toInt())
            AlarmScheduler.schedule(getApplication(), saved)
        }
    }

    fun toggleAlarm(alarm: Alarm, enabled: Boolean) {
        viewModelScope.launch {
            alarmRepo.setEnabled(alarm.id, enabled)
            if (enabled) AlarmScheduler.schedule(getApplication(), alarm)
            else AlarmScheduler.cancel(getApplication(), alarm.id)
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            AlarmScheduler.cancel(getApplication(), alarm.id)
            alarmRepo.delete(alarm)
        }
    }

    suspend fun getAlarmById(id: Int) = alarmRepo.getById(id)

    suspend fun getQuestionsForAlarm(alarm: Alarm) = questionRepo.getQuestionsForAlarm(alarm)
}

// ===== QUESTION VIEWMODEL =====
class QuestionViewModel(application: Application) : AndroidViewModel(application) {
    private val db = MindfulWakeDatabase.getDatabase(application)
    private val repo = QuestionRepository(db)

    val customQuestions = repo.customQuestions

    fun addQuestion(q: Question) = viewModelScope.launch { repo.insert(q) }
    fun deleteQuestion(q: Question) = viewModelScope.launch { repo.delete(q) }
    fun updateQuestion(q: Question) = viewModelScope.launch { repo.update(q) }
}

// ===== STATS VIEWMODEL =====
class StatsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = MindfulWakeDatabase.getDatabase(application)
    private val repo = StatsRepository(db)

    val totalAlarms = flow { emit(repo.getTotalAlarms()) }.stateIn(viewModelScope, SharingStarted.Lazily, 0)
    val averageAccuracy = flow { emit(repo.getAverageAccuracy()) }.stateIn(viewModelScope, SharingStarted.Lazily, null)
    val totalSnoozes = flow { emit(repo.getTotalSnoozes()) }.stateIn(viewModelScope, SharingStarted.Lazily, 0)
}

// ===== WEATHER API =====
interface OpenWeatherApi {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse

    @GET("forecast")
    suspend fun getForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("cnt") count: Int = 40
    ): ForecastResponse
}

data class WeatherResponse(
    val name: String,
    val main: MainData,
    val weather: List<WeatherDesc>,
    val wind: WindData,
    val sys: SysData
)
data class MainData(val temp: Double, val feels_like: Double, val humidity: Int)
data class WeatherDesc(val description: String, val icon: String)
data class WindData(val speed: Double)
data class SysData(val sunrise: Long, val sunset: Long)
data class ForecastResponse(val list: List<ForecastItem>)
data class ForecastItem(val dt: Long, val main: MainData, val weather: List<WeatherDesc>)

// ===== WEATHER VIEWMODEL =====
class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val api = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OpenWeatherApi::class.java)

    // Demo key - user should replace with their own key
    private val apiKey = "YOUR_OPENWEATHER_API_KEY"

    private val _weatherState = MutableStateFlow<WeatherData?>(null)
    val weatherState = _weatherState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun fetchWeather(lat: Double = 18.5204, lon: Double = 73.8567) { // Default: Pune
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val current = api.getCurrentWeather(lat, lon, apiKey)
                val forecast = try {
                    api.getForecast(lat, lon, apiKey)
                } catch (_: Exception) { null }

                // Group forecast by day
                val forecastDays = forecast?.list
                    ?.groupBy { java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date(it.dt * 1000)) }
                    ?.entries?.take(5)
                    ?.map { (_, items) ->
                        ForecastDay(
                            date = items.first().dt,
                            tempMin = items.minOf { it.main.temp },
                            tempMax = items.maxOf { it.main.temp },
                            description = items.first().weather.firstOrNull()?.description ?: "",
                            icon = items.first().weather.firstOrNull()?.icon ?: ""
                        )
                    } ?: emptyList()

                _weatherState.value = WeatherData(
                    temperature = current.main.temp,
                    feelsLike = current.main.feels_like,
                    humidity = current.main.humidity,
                    description = current.weather.firstOrNull()?.description ?: "",
                    icon = current.weather.firstOrNull()?.icon ?: "",
                    windSpeed = current.wind.speed * 3.6,
                    cityName = current.name,
                    sunrise = current.sys.sunrise,
                    sunset = current.sys.sunset,
                    forecast = forecastDays
                )
            } catch (e: Exception) {
                // Demo data for testing
                _weatherState.value = WeatherData(
                    temperature = 28.0, feelsLike = 32.0, humidity = 65,
                    description = "partly cloudy", icon = "02d",
                    windSpeed = 15.0, cityName = "Pune",
                    sunrise = System.currentTimeMillis() / 1000 - 21600,
                    sunset = System.currentTimeMillis() / 1000 + 14400,
                    forecast = listOf(
                        ForecastDay(System.currentTimeMillis() / 1000 + 86400, 22.0, 32.0, "sunny", "01d"),
                        ForecastDay(System.currentTimeMillis() / 1000 + 172800, 24.0, 30.0, "cloudy", "03d"),
                        ForecastDay(System.currentTimeMillis() / 1000 + 259200, 20.0, 28.0, "rain", "10d"),
                        ForecastDay(System.currentTimeMillis() / 1000 + 345600, 22.0, 31.0, "sunny", "01d"),
                        ForecastDay(System.currentTimeMillis() / 1000 + 432000, 23.0, 29.0, "cloudy", "02d"),
                    )
                )
                _error.value = null // Using demo data
            } finally {
                _isLoading.value = false
            }
        }
    }
}