package com.example.theweather.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.theweather.model.CreateAlert
import com.example.theweather.model.WeatherResult
import com.example.theweather.model.WeatherResultCurrent

@Dao
interface WeatherDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrentWeather(weatherResultCurrent: WeatherResultCurrent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weatherResult: WeatherResult)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(createAlert: CreateAlert)

    @Delete
    suspend fun deleteWeather(weatherResult: WeatherResult)

    @Delete
    suspend fun deleteAlert(createAlert: CreateAlert)

    @Query("DELETE FROM CreateAlert WHERE selectedDT = :alertId")
    suspend fun deleteAlertID(alertId: Long)

    @Query("SELECT * FROM WeatherResult")
    fun getAllWeather(): LiveData<List<WeatherResult>>

    @Query("SELECT * FROM WeatherResultCurrent")
    fun getAllCurrent(): LiveData<List<WeatherResultCurrent>>

    @Query("SELECT * FROM CreateAlert")
    fun getAllAlert(): LiveData<List<CreateAlert>>
}

