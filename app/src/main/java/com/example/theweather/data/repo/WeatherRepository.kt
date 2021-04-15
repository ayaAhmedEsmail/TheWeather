package com.example.theweather.data.repo

import androidx.lifecycle.MutableLiveData
import com.example.theweather.data.local.WeatherDAO
import com.example.theweather.data.remote.ApiWeather
import com.example.theweather.data.remote.Constant
import com.example.theweather.model.CreateAlert
import com.example.theweather.model.WeatherResult
import com.example.theweather.model.WeatherResultCurrent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


//to get data from local or remote
class WeatherRepository( private val db: WeatherDAO) {

    private var weatherResponseMutableLiveData: MutableLiveData<WeatherResult> = MutableLiveData()
    private var weatherResponseMutableLiveDataCurrent: MutableLiveData<WeatherResultCurrent> =
        MutableLiveData()

    fun fetchWeatherAPI(lat: Double, lon: Double, lang: String, units: String): MutableLiveData<WeatherResult> {
        GlobalScope.launch {
            Dispatchers.IO
            val response = ApiWeather.getApiService().getWeather(
                    lat.toString(),
                    lon.toString(),
                    lang,
                    "minutely",
                    units,
                    Constant.APP_Key )
            if (response.isSuccessful) {
                withContext(Dispatchers.Main) {
                    weatherResponseMutableLiveData.value = response.body()
                }
            }
        }
        return weatherResponseMutableLiveData
    }

    fun fetchWeatherAPICurrent(lat: Double, lon: Double, lang: String, units: String): MutableLiveData<WeatherResultCurrent> {
        GlobalScope.launch {
            Dispatchers.IO
            val response = ApiWeather.getApiService().getWeatherCurrent(
                    lat.toString(),
                    lon.toString(),
                    lang,
                    "minutely",
                    units,
                    Constant.APP_Key
                )
            if (response.isSuccessful) {
                withContext(Dispatchers.Main) {
                    weatherResponseMutableLiveDataCurrent.value = response.body()
                }
            }
        }
        return weatherResponseMutableLiveDataCurrent
    }

    fun insert(weatherResult: WeatherResult) {
        GlobalScope.launch {
            Dispatchers.IO
            db.insertWeather(weatherResult)
        }
    }
    fun insertAlert(createAlert: CreateAlert) {
        GlobalScope.launch {
            Dispatchers.IO
            db.insertAlert(createAlert)
        }
    }
    fun insertCurrent(weatherResult: WeatherResultCurrent) {
        GlobalScope.launch {
            Dispatchers.IO
            db.insertCurrentWeather(weatherResult)
        }
    }

    fun delete(weatherResult: WeatherResult) {
        GlobalScope.launch {
            Dispatchers.IO
            db.deleteWeather(weatherResult)
        }
    }
    fun deleteAlert(createAlert: CreateAlert) {
        GlobalScope.launch {
            Dispatchers.IO
            db.deleteAlert(createAlert)
        }
    }
    val weatherList = db.getAllWeather()
    val weatherCurrentList = db.getAllCurrent()
    val alertList = db.getAllAlert()


}