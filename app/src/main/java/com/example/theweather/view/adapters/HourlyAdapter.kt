package com.example.theweather.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.theweather.R
import com.example.theweather.databinding.HourlyViewBinding
import com.example.theweather.model.Hourly

class HourlyAdapter(private var hourlyList: List<Hourly>) :
    RecyclerView.Adapter<HourlyAdapter.HourlyViewHolder>() {

    class HourlyViewHolder(private var binding: HourlyViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Hourly) {
            val sdf = java.text.SimpleDateFormat("h a")
            val date = java.util.Date(item.dt.toLong() * 1000)
            binding.tvHourlyTime.text = sdf.format(date).toString()
            binding.tvHourlyTemp.text = item.temp.toString()
            Glide.with(binding.root.context)
                .load("https://openweathermap.org/img/wn/${item.weather[0].icon}@2x.png")
                .into(binding.ivHourlyImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyViewHolder {
        return HourlyViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.hourly_view,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: HourlyViewHolder, position: Int) {
        holder.bind(hourlyList[position])
    }

    override fun getItemCount(): Int {
        return hourlyList.size
    }
}