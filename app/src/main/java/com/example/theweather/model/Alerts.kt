package com.example.theweather.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Alerts(
    @SerializedName("sender_name") val sender_name: String = "",
    @SerializedName("event") val event: String = "",
    @SerializedName("start") val start: Int = 0,
    @SerializedName("end") val end: Int = 0,
    @SerializedName("description") val description: String = ""
) : Parcelable
