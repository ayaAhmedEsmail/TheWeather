package com.example.theweather.view.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.createDataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.bumptech.glide.Glide
import com.google.android.gms.location.*
import com.example.theweather.R
import com.example.theweather.helper.APIWorker
import com.example.theweather.helper.NotificationWorker
import com.example.theweather.databinding.FragmentHomeBinding
import com.example.theweather.model.WeatherResultCurrent
import com.example.theweather.view.adapters.DailyAdapter
import com.example.theweather.view.adapters.HourlyAdapter
import com.example.theweather.viewmodel.WeatherViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit


class HomeFragment : Fragment() {

    private lateinit var weatherViewModel: WeatherViewModel
    private lateinit var binding: FragmentHomeBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient//get location address
    private lateinit var locationRequest: LocationRequest //to get access
    private lateinit var locationCallback: LocationCallback
    private lateinit var dataStore: DataStore<Preferences>
    private var makeNotification: Boolean = false
    var myLatitude: Double = -1.0
    var myLongitude: Double = -1.0
    private var lang: String = Locale.getDefault().language
    private var units: String = "metric"
    private var anotherLocation: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        weatherViewModel = ViewModelProvider(this).get(WeatherViewModel::class.java)

    }

    override fun onStart() {
        super.onStart()
        if (ContextCompat.checkSelfPermission(requireActivity(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireActivity(),Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            loadSettings()
            loadLocation()
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), 1000
            )
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        dataStore = requireContext().createDataStore(name = "settings")

        return binding.root
    }

    private fun loadLocation() {
        if (anotherLocation) {
            if (hasInternetConnection()) {
                if (arguments != null) {
                    myLatitude = requireArguments().getDouble("lat")
                    myLongitude = requireArguments().getDouble("lon")
                    lifecycleScope.launch {
                        saveToDataStore("mapLat", requireArguments().getDouble("lat").toString())
                        saveToDataStore("mapLon", requireArguments().getDouble("lon").toString())
                    }
                    fetchWeather(myLatitude, myLongitude, lang, units)
                } else {
                    lifecycleScope.launch {
                        val l = readFromDataStore("mapLat")
                        val n = readFromDataStore("mapLon")
                        if (l != null && n != null) {
                            fetchWeather(l.toDouble(), n.toDouble(), lang, units)
                        } else {
                            lifecycleScope.launch {
                                val l = readFromDataStore("currentLat")
                                displayCurrentWeather(l!!.toDouble())
                            }
                        }
                    }
                }
            }
            else {
                lifecycleScope.launch {
                    val l = readFromDataStore("mapLat")
                    displayCurrentWeather(l!!.toDouble())
                }
            }
        } else {
            getCurrentLocation()
        }
    }

    companion object {
        const val NOTIFICATION_ID = "TheWeather_notification_id"
        const val NOTIFICATION_DES = "TheWeather_notification_Des"
        const val NOTIFICATION_END = "TheWeather_notification_END"
        const val NOTIFICATION_START = "TheWeather_notification_START"
        const val NOTIFICATION_ALERT_DELETE = "TheWeather_notification_ALERT_DELETE"
        const val NOTIFICATION_OR_ALARM = "TheWeather_NOTIFICATION_OR_ALARM"
        const val API_ID = "TheWeather_API_ID"
        const val API_LAT = "TheWeather_API_LAT"
        const val API_LON = "TheWeather_API_LON"
        const val API_LANG = "TheWeather_API_LANG"
        const val API_UNITS = "TheWeather_API_UNITS"
    }

    private fun notificationWorkerFun(
        alertId: Long,
        id: Long,
        alert: String,
        delay: Long,
        end: Double,
        start: Double,
        alarmOrNotification: String
    ) {
        val workManager = WorkManager.getInstance(requireContext())
        val data = workDataOf(
            NOTIFICATION_OR_ALARM to alarmOrNotification,
            NOTIFICATION_ALERT_DELETE to alertId, NOTIFICATION_ID to id, NOTIFICATION_DES to alert,
            NOTIFICATION_END to end, NOTIFICATION_START to start
        )
        val notificationWork = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
            .setInputData(data).setInitialDelay(delay, TimeUnit.SECONDS).build()
        workManager.enqueueUniqueWork(
            alertId.toString(),
            ExistingWorkPolicy.REPLACE,
            notificationWork
        )

    }

    private fun apiWorkerFun(myLatitude: Double, myLongitude: Double, lang: String, units: String) {
        val data = workDataOf(
            API_ID to myLatitude, API_LAT to myLatitude,
            API_LON to myLongitude, API_LANG to lang, API_UNITS to units
        )
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val apiWork = PeriodicWorkRequest.Builder(APIWorker::class.java, 8, TimeUnit.HOURS)
            .setConstraints(constraints).setInputData(data).build()
        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork("Api", ExistingPeriodicWorkPolicy.REPLACE, apiWork)
    }


    private fun loadSettings() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        lang = sharedPreferences.getString("languageSystem", Locale.getDefault().language)!!
        units = sharedPreferences.getString("unitSystem", "metric")!!
        anotherLocation = sharedPreferences.getBoolean("deviceLocation", false)
        makeNotification = sharedPreferences.getBoolean("notifications", false)
    }

    private suspend fun saveToDataStore(key: String, value: String) {
        val dataStoreKey = stringPreferencesKey(key)
        dataStore.edit { settings ->
            settings[dataStoreKey] = value
        }
    }

    private suspend fun readFromDataStore(key: String): String? {
        val dataStoreKey = stringPreferencesKey(key)
        val preferences = dataStore.data.first()
        return preferences[dataStoreKey]
    }

    private fun insertDB(weatherResult: WeatherResultCurrent) {
        weatherViewModel.insertCurrent(weatherResult)
    }

    private fun hasInternetConnection(): Boolean {
        return weatherViewModel.hasInternetConnection()
    }

    private fun convertArabic(arabicStr: String): String {
        val chArr = arabicStr.toCharArray()
        val sb = StringBuilder()
        for (ch in chArr) {
            when {
                Character.isDigit(ch) -> {
                    sb.append(Character.getNumericValue(ch))
                }
                ch == '٫' -> {
                    sb.append(".")
                }
                else -> {
                    sb.append(ch)
                }
            }
        }
        return sb.toString()
    }

    private fun displayCurrentWeather(lat: Double) {
        val xLat: String = "%.4f".format(lat)
        var yLat: String = convertArabic(xLat)
        val currentTime = Calendar.getInstance().timeInMillis
        weatherViewModel.weatherCurrentList.observe(this, Observer { it -> val weather = it.find {
            it.lat == yLat.toDouble()
            }
            if (weather != null) {
                displayView(weather)
                weatherViewModel.alertList.observe(this, Observer {
                    if (it != null) {
                        for (i in it) {
                            if (weather.alerts != null) {
                                var notificationDate = i.selectedDT - currentTime
                                var customNotificationDateEnd = i.endTime / 1000
                                var customNotificationDateStart = i.startTime / 1000
                                var notificationDateEnd = weather.alerts!![0].end
                                var notificationDateStart = weather.alerts!![0].start
                                if (notificationDateStart in customNotificationDateStart..customNotificationDateEnd
                                    || notificationDateEnd in customNotificationDateStart..customNotificationDateEnd
                                ) {
                                    notificationWorkerFun(
                                        i.selectedDT, i.selectedDT,
                                        weather.alerts!![0].event,
                                        notificationDate / 1000,
                                        notificationDateEnd.toDouble(),
                                        notificationDateStart.toDouble(), i.alarmOrNotification
                                    )
                                } else {
                                    var notificationDate = i.selectedDT - currentTime
                                    var notificationDateEnd = i.endTime
                                    var notificationDateStart = i.startTime
                                    notificationWorkerFun(
                                        i.selectedDT,
                                        i.selectedDT,
                                        "Every Thing is Good Enjoy Your Day",
                                        notificationDate / 1000,
                                        notificationDateEnd.toDouble() / 1000,
                                        notificationDateStart.toDouble() / 1000,
                                        i.alarmOrNotification
                                    )
                                }
                            } else {
                                var notificationDate = i.selectedDT - currentTime
                                var notificationDateEnd = i.endTime
                                var notificationDateStart = i.startTime
                                notificationWorkerFun(
                                    i.selectedDT,
                                    i.selectedDT,
                                    "Every Thing is Good Enjoy Your Day",
                                    notificationDate / 1000,
                                    notificationDateEnd.toDouble() / 1000,
                                    notificationDateStart.toDouble() / 1000, i.alarmOrNotification
                                )
                            }
                        }
                    }
                })
            }
        })
    }

    private fun displayView(weather: WeatherResultCurrent) {
        binding.tvCityName.text = weather.timezone
        binding.tvMainDescripation.text =
            "${weather.current.weather[0].main} / ${weather.current.weather[0].description}"
        binding.tvTemp.text = weather.current.temp.toString()
        binding.tvPressure.text = weather.current.pressure.toString()
        binding.tvClouds.text = weather.current.clouds.toString()
        binding.tvHumidity.text = weather.current.humidity.toString()
        binding.tvWindSpeed.text = weather.current.wind_speed.toString()
        val sdf = java.text.SimpleDateFormat("EEE, dd MMM")
        val date = weather.current.dt.toLong().times(1000).let { it1 ->
            java.util.Date(it1)
        }
        binding.tvData.text = sdf.format(date).toString()
        Glide.with(binding.root.context)
            .load("https://openweathermap.org/img/wn/${weather.current.weather[0].icon}@4x.png")
            .into(binding.ivImage)
        binding.rcHourly.apply {
            layoutManager =
                LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = HourlyAdapter(weather.hourly)
        }
        binding.rcDaily.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = DailyAdapter(weather.daily)

        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {

        var locationManager: LocationManager =
            activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        ) {
            fusedLocationProviderClient.lastLocation.apply {
                addOnFailureListener {}
                addOnSuccessListener {
                    if (it != null) {
                        myLatitude = it.latitude
                        myLongitude = it.longitude
                        lifecycleScope.launch {
                            saveToDataStore("currentLat", it.latitude.toString())
                            saveToDataStore("currentLon", it.longitude.toString())
                        }
                        if (hasInternetConnection()) {
                            fetchWeather(myLatitude, myLongitude, lang, units)
                        } else {
                            displayCurrentWeather(it.latitude)
                        }
                    } else {
                        locationRequest = LocationRequest()
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            .setInterval(10000)
                            .setFastestInterval(10000)
                            .setNumUpdates(1)

                        locationCallback = object : LocationCallback() {
                            override fun onLocationResult(locationResult: LocationResult?) {
                                super.onLocationResult(locationResult)
                                if (locationResult?.lastLocation != null) {
                                    myLatitude = locationResult.lastLocation.latitude
                                    myLongitude = locationResult.lastLocation.longitude
                                    fetchWeather(myLatitude, myLongitude, lang, units)
                                }
                            }
                        }

                        try {
                            fusedLocationProviderClient.requestLocationUpdates(
                                locationRequest, locationCallback, Looper.myLooper()
                            )
                        } catch (se: SecurityException) {
                            Log.e(
                                "Location Error",
                                "Lost location permissions. Couldn't remove updates. $se"
                            )
                            checkAndStartLocationUpdates()
                        }
                    }
                }
            }
        } else {
            startActivity(
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    private fun checkAndStartLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(), //instance of current activity that is trying to subscribe to get location changes
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                1000  //A Constant showing the request to get access to location permissions
            )
        } else {
            //retry to subscribe to location changes
            getCurrentLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000 && (grantResults.isNotEmpty())) {
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        requireContext(),
                        "Please Give ${permissions[i]}",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }
            }
            //retry to subscribe to location changes
            getCurrentLocation()
        }
    }


    private fun fetchWeather(myLatitude: Double, myLongitude: Double, lang: String, units: String) {
        weatherViewModel.fetchWeatherCurrent(myLatitude, myLongitude, lang, units)?.observe(viewLifecycleOwner, Observer<WeatherResultCurrent> {
                if (it != null) {
                    insertDB(it)
                    displayCurrentWeather(it.lat)
                    if (!makeNotification) {
                        apiWorkerFun(it.lat, it.lon, lang, units)
                    }
                }
            })
    }
}