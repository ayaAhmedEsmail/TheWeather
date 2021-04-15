package com.example.theweather.view.adapters
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.theweather.R
import com.example.theweather.databinding.DailyViewBinding
import com.example.theweather.model.Daily

class DailyAdapter(private var dailyList: List<Daily>) :
    RecyclerView.Adapter<DailyAdapter.DailyViewHolder>() {

    class DailyViewHolder(private var binding: DailyViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Daily) {
            val sdf = java.text.SimpleDateFormat("EEE")
            val date = java.util.Date(item.dt.toLong() * 1000)
            binding.tvDailyDay.text = sdf.format(date).toString()
            binding.tvDailyTemp.text = item.temp.day.toString()
            binding.tvDailyDesc.text = item.weather[0].main
            Glide.with(binding.root.context)
                .load("https://openweathermap.org/img/wn/${item.weather[0].icon}@4x.png")
                .into(binding.ivDailyImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyViewHolder {
        return DailyViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.daily_view, parent, false))
    }

    override fun onBindViewHolder(holder: DailyViewHolder, position: Int) {
        holder.bind(dailyList[position])
    }

    override fun getItemCount(): Int {
        return dailyList.size
    }

}