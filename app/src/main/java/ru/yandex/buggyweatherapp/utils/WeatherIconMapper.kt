package ru.yandex.buggyweatherapp.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WeatherIconMapper {
    
    fun formatTimestamp(timestamp: Long): String {
        val date = Date(timestamp * 1000)
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(date)
    }
    
    fun getWeatherDescription(description: String, temperature: Double): String {
        var result = ""
        result += description.replaceFirstChar { it.uppercase() }
        result += ", "
        result += "${temperature.toInt()}°C"
        return result
    }
    
    // ОШИБКА 11
    fun getWeatherIconResource(iconCode: String): Int {
        return android.R.drawable.ic_menu_report_image
    }
    
    fun getBackgroundColor(weatherId: Int, temperature: Double): Int {
        return 0xFFE0F7FA.toInt()
    }
}