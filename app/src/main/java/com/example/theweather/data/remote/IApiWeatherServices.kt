package com.example.theweather.data.remote
import com.example.theweather.model.WeatherResult
import com.example.theweather.model.WeatherResultCurrent
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface IApiWeatherServices {
    @GET("data/2.5/onecall?")
    suspend fun getWeather(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("lang") lang: String,
        @Query("exclude") exclude: String,
        @Query("units") units: String,
        @Query("APPID") app_id: String
    ): Response<WeatherResult>

    @GET("data/2.5/onecall?")
    suspend fun getWeatherCurrent(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("lang") lang: String,
        @Query("exclude") exclude: String,
        @Query("units") units: String,
        @Query("APPID") app_id: String
    ): Response<WeatherResultCurrent>
}
