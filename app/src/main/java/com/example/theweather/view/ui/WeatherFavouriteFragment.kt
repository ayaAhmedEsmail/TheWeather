package com.example.theweather.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.theweather.R
import com.example.theweather.databinding.FragmentWeatherFavouriteBinding
import com.example.theweather.view.adapters.DailyAdapter
import com.example.theweather.view.adapters.HourlyAdapter
import com.example.theweather.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.*


class WeatherFavouriteFragment : Fragment() {
    private lateinit var weatherViewModel: WeatherViewModel
    private lateinit var binding: FragmentWeatherFavouriteBinding
    private val args: WeatherFavouriteFragmentArgs by navArgs()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        weatherViewModel = ViewModelProvider(this).get(WeatherViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_weather_favourite, container, false)
        fetchWeather()
        return binding.root
    }

    private fun fetchWeather() {
        binding.tvCityNameFavourite.text = args.weatherDate.timezone
        binding.tvMainDescripationFavourite.text = "${args.weatherDate.current.weather[0].main} / ${args.weatherDate.current.weather[0].description}"
        binding.tvTempFavourite.text = args.weatherDate.current.temp.toString()
        binding.tvPressureFavourite.text = args.weatherDate.current.pressure.toString()
        binding.tvCloudsFavourite.text = args.weatherDate.current.clouds.toString()
        binding.tvHumidityFavourite.text = args.weatherDate.current.humidity.toString()
        binding.tvWindSpeedFavourite.text = args.weatherDate.current.wind_speed.toString()

        val sdf = SimpleDateFormat("EEE, dd MMM")
        val date = Date(args.weatherDate.current.dt.toLong() * 1000)

        binding.tvDataFavourite.text = sdf.format(date).toString()
        Glide.with(binding.root.context)
                .load("https://openweathermap.org/img/wn/${args.weatherDate.current.weather[0].icon}@4x.png")
                .into(binding.ivImageFavourite)

        binding.rcHourlyFavourite.apply {
            layoutManager =
                    LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = HourlyAdapter(args.weatherDate.hourly)
        }

        binding.rcDailyFavourite.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = DailyAdapter(args.weatherDate.daily)
        }
    }
}