package com.example.theweather.helper


import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.annotation.RequiresApi
import java.util.*

class HelperClass(base: Context?) :
    android.content.ContextWrapper(base) {
    companion object {
        @RequiresApi(Build.VERSION_CODES.N)
        fun changeLang(
            context: Context,
            newLocale: Locale?
        ): HelperClass {
            var context = context
            val res = context.resources
            val configuration = res.configuration
            configuration.setLocale(newLocale)
            val localeList = LocaleList(newLocale)
            LocaleList.setDefault(localeList)
            configuration.setLocales(localeList)
            context = context.createConfigurationContext(configuration)
            return HelperClass(context)
        }

    }
}