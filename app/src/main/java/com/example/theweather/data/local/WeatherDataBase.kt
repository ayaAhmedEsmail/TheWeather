package com.example.theweather.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.theweather.model.CreateAlert
import com.example.theweather.model.WeatherResult
import com.example.theweather.model.WeatherResultCurrent

@Database(
    entities = [WeatherResult::class, WeatherResultCurrent::class , CreateAlert::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WeatherDataBase : RoomDatabase() {
    abstract val weatherDAO: WeatherDAO
    companion object {
        @Volatile
        private var INSTANCE: WeatherDataBase? = null
        fun getInstance(context: Context): WeatherDataBase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        WeatherDataBase::class.java, "WeatherResult"
                    ).build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}


