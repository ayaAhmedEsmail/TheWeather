package com.example.theweather.data.remote


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiWeather {
    private fun getInstance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constant.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getApiService(): IApiWeatherServices {
        return getInstance().create(IApiWeatherServices::class.java)
    }
}