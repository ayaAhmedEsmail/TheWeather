package com.example.theweather.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.*
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.theweather.helper.WeatherApp
import com.example.theweather.data.local.WeatherDataBase
import com.example.theweather.data.repo.WeatherRepository
import com.example.theweather.model.CreateAlert
import com.example.theweather.model.WeatherResult
import com.example.theweather.model.WeatherResultCurrent


class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = WeatherDataBase.getInstance(getApplication()).weatherDAO

    var weatherRepository: WeatherRepository = WeatherRepository(dao)


    fun fetchWeather(lat: Double, lon: Double, lang: String, units: String): MutableLiveData<WeatherResult>? {
        try {
            if (hasInternetConnection()) {
                return weatherRepository.fetchWeatherAPI(lat, lon, lang, units)
            } else {
                Toast.makeText(getApplication(), "No Internet Connection", Toast.LENGTH_LONG).show()
            }
        } catch (t: Throwable) {
            Toast.makeText(getApplication(), "No Internet Connection", Toast.LENGTH_LONG).show()
        }
        return null
    }

    fun fetchWeatherCurrent(lat: Double, lon: Double, lang: String, units: String): MutableLiveData<WeatherResultCurrent>? {
        try {
            if (hasInternetConnection()) {
                return weatherRepository.fetchWeatherAPICurrent(lat, lon, lang, units)
            }else {
                Toast.makeText(getApplication(), "No Internet Connection", Toast.LENGTH_LONG).show()
            }
        } catch (t: Throwable) {
            Toast.makeText(getApplication(), "No Internet Connection", Toast.LENGTH_LONG).show()
        }
        return null
    }

    fun insert(weatherResult: WeatherResult) {
        weatherRepository.insert(weatherResult)
    }
    fun insertAlert(createAlert: CreateAlert) {
        weatherRepository.insertAlert(createAlert)
    }
    fun insertCurrent(weatherResult: WeatherResultCurrent) {
        weatherRepository.insertCurrent(weatherResult)
    }

    fun delete(weatherResult: WeatherResult) {
        weatherRepository.delete(weatherResult)
    }
    fun deleteAlert(createAlert: CreateAlert) {
        weatherRepository.deleteAlert(createAlert)
    }
    val weatherList = weatherRepository.weatherList
    val weatherCurrentList = weatherRepository.weatherCurrentList
    val alertList = weatherRepository.alertList

    // check Internet Connection
    fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<WeatherApp>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                else -> false
            }
        }
        return false
    }
}