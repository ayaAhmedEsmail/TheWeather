package com.example.theweather.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class CreateAlert(
    @PrimaryKey
    var selectedDT: Long,
    var startTime: Long,
    var endTime: Long,
    var repeat: String,
    var alarmOrNotification: String
) : Parcelable