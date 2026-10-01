package ru.yandex.buggyweatherapp.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.yandex.buggyweatherapp.model.Location
import ru.yandex.buggyweatherapp.model.WeatherData
import ru.yandex.buggyweatherapp.repository.LocationRepository
import ru.yandex.buggyweatherapp.repository.WeatherRepository
import ru.yandex.buggyweatherapp.utils.ImageLoader
import java.util.Timer
import java.util.TimerTask

class WeatherViewModel : ViewModel() {
    
    // ОШИБКА 2
    private var locationRepository: LocationRepository? = null
    
    private val weatherRepository = WeatherRepository()

    private val _weatherData = MutableLiveData<WeatherData>()
    val weatherData: LiveData<WeatherData> = _weatherData

    private val _currentLocation = MutableLiveData<Location>()
    val currentLocation: LiveData<Location> = _currentLocation

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _cityName = MutableLiveData<String>()
    val cityName: LiveData<String> = _cityName
    
    // ОШИБКА 3
    private var refreshTimer: Timer? = null
    
    fun initialize(context: Context) {
        if (locationRepository == null) {
            locationRepository = LocationRepository(context.applicationContext)
        }
        fetchCurrentLocationWeather()
        startAutoRefresh()
    }
    
    fun fetchCurrentLocationWeather() {
        _isLoading.value = true
        _error.value = null
        
        locationRepository?.getCurrentLocation { location ->
            if (location != null) {
                // ОШИБКА 4
                _currentLocation.postValue(location)
                
                val cityNameFromLocation = locationRepository?.getCityNameFromLocation(location)
                _cityName.postValue(cityNameFromLocation ?: "")
                
                getWeatherForLocation(location)
            } else {
                _isLoading.postValue(false)
                _error.postValue("Unable to get current location")
            }
        }
    }
    
    fun getWeatherForLocation(location: Location) {
        _isLoading.postValue(true)
        _error.postValue(null)
        
        weatherRepository.getWeatherData(location) { data, exception ->
            _isLoading.postValue(false)
            if (data != null) {
                _weatherData.postValue(data)
            } else {
                _error.postValue(exception?.message ?: "Unknown error")
            }
        }
    }
    
    fun searchWeatherByCity(city: String) {
        if (city.isBlank()) {
            _error.value = "City name cannot be empty"
            return
        }
        
        _isLoading.value = true
        _error.value = null
        
        weatherRepository.getWeatherByCity(city) { data, exception ->
            _isLoading.postValue(false)
            if (data != null) {
                _weatherData.postValue(data)
                _cityName.postValue(data.cityName)
                _currentLocation.postValue(Location(0.0, 0.0, data.cityName))
            } else {
                _error.postValue(exception?.message ?: "Unknown error")
            }
        }
    }
    
    fun formatTemperature(temp: Double): String {
        return "${temp.toInt()}°C"
    }
    
    fun loadWeatherIcon(iconCode: String) {
        viewModelScope.launch {
            val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"
            ImageLoader.loadImage(iconUrl)
        }
    }
    
    // ОШИБКА 5
    private fun startAutoRefresh() {
        refreshTimer?.cancel()
        refreshTimer = Timer()
        refreshTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                _currentLocation.value?.let { location ->
                    getWeatherForLocation(location)
                }
            }
        }, 60000, 60000)
    }
    
    fun toggleFavorite() {
        _weatherData.value?.let { current ->
            // ОШИБКА 6
            val updated = current.copy(isFavorite = !current.isFavorite)
            _weatherData.value = updated
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        refreshTimer?.cancel()
        refreshTimer = null
    }
}