package com.example.theweather.helper

import android.content.Context
import android.util.Log
import androidx.work.*

import com.example.theweather.data.local.WeatherDataBase
import com.example.theweather.data.remote.ApiWeather
import com.example.theweather.data.remote.Constant
import com.example.theweather.model.WeatherResultCurrent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit

class APIWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    private val dao = WeatherDataBase.getInstance(applicationContext).weatherDAO
    companion object {
        const val API_LAT = "TheWeather_API_LAT"
        const val API_LON = "TheWeather_API_LON"
        const val API_LANG = "TheWeather_API_LANG"
        const val API_UNITS = "TheWeather_API_UNITS"
        const val NOTIFICATION_ID = "TheWeather_notification_id"
        const val API_ID = "TheWeather_API_ID"
        const val NOTIFICATION_DES = "TheWeather_notification_Des"
        const val NOTIFICATION_END = "TheWeather_notification_END"
        const val NOTIFICATION_START = "TheWeather_notification_START"
    }
    override fun doWork(): Result {
        return try {
            //Log.i("TAG", "doWork: Done")
            val lat = inputData.getDouble(API_LAT, 0.0)
            val lon = inputData.getDouble(API_LON, 0.0)
            val lang = inputData.getString(API_LANG)
            val units = inputData.getString(API_UNITS)
            val id = inputData.getDouble(API_ID, 0.0)
            if (units != null) {
                if (lang != null) {
                    fetchWeatherAPI(id, lat, lon, lang, units)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }



    private fun fetchWeatherAPI(id: Double, lat: Double, lon: Double, lang: String, units: String) {
        val currentTime = Calendar.getInstance().timeInMillis / 1000
        GlobalScope.launch {
            Dispatchers.IO
            val response = ApiWeather.getApiService()
                .getWeatherCurrent(
                    lat.toString(),
                    lon.toString(),
                    lang,
                    "minutely",
                    units,
                    Constant.APP_Key
                )
            if (response.isSuccessful) {
                val rnds = (5000..10000).random()
                withContext(Dispatchers.Main) {
                    insertCurrent(response.body()!!)
                    if (response.body()!!.alerts != null) {
                        for (i in response.body()!!.alerts!!) {
                            Log.i("TAG", "fetchWeatherAPI: ${i.event}")
                            notificationWorkerFun(
                                i.start.toLong(),
                                i.event,
                                i.start - currentTime,
                                i.end.toDouble(),
                                i.start.toDouble()
                            )
                        }
                    }
                }
            }
        }
    }

    private fun notificationWorkerFun(
        id: Long,
        alert: String,
        delay: Long,
        end: Double,
        start: Double
    ) {
        val workManager = WorkManager.getInstance(applicationContext)
        val data = workDataOf(
            NOTIFICATION_ID to id, NOTIFICATION_DES to alert,
            NOTIFICATION_END to end, NOTIFICATION_START to start
        )
        val notificationWork = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
            .setInputData(data).setInitialDelay(delay - 3600, TimeUnit.SECONDS).build()
        workManager.enqueue(notificationWork)
    }

    private fun insertCurrent(weatherResult: WeatherResultCurrent) {
        GlobalScope.launch {
            Dispatchers.IO
            dao.insertCurrentWeather(weatherResult)
        }
    }
}