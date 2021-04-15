package com.example.theweather.view.ui
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.example.theweather.R
import com.example.theweather.databinding.FragmentFavouriteBinding
import com.example.theweather.model.WeatherResult
import com.example.theweather.view.adapters.WeatherFavouriteAdapter
import com.example.theweather.viewmodel.WeatherViewModel
import java.util.*


class FavouriteFragment : Fragment(), CellClickListener {
    private lateinit var binding: FragmentFavouriteBinding
    private lateinit var weatherViewModel: WeatherViewModel
    private lateinit var lang: String
    private lateinit var units: String
    private lateinit var adapter: WeatherFavouriteAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        weatherViewModel = ViewModelProvider(this).get(WeatherViewModel::class.java)
    }
    override fun onStart() {
        super.onStart()
        loadSettings()
        initRecyclerView()
        if (arguments != null) {
            fetchWeather(
                requireArguments().getDouble("lat"),
                requireArguments().getDouble("lon"),
                lang,
                units
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_favourite, container, false)
        binding.btnFab.setOnClickListener {
            it.findNavController().navigate(R.id.action_favouriteFragment_to_mapsFragment)
        }
        ItemTouchHelper(itemTouchHelper).apply {
            attachToRecyclerView(binding.rcWeatherFavourite)
        }
        return binding.root
    }

    private val itemTouchHelper = object : ItemTouchHelper.SimpleCallback(
        0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val removeAt = adapter.getWeatherList().removeAt(position)
            deleteFromDB(removeAt)
            adapter.notifyDataSetChanged()
            Snackbar.make(requireView(), "Successfully deleted", Snackbar.LENGTH_LONG).apply {
                setAction("Undo") {
                    // Add To Room
                    insertDB(removeAt)
                }
                show()
            }
        }
    }

    private fun loadSettings() {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        lang = sp.getString("languageSystem", "en")!!
        units = sp.getString("unitSystem", "metric")!!
    }

    private fun fetchWeather(myLatitude: Double, myLongitude: Double, lang: String, units: String) {
        weatherViewModel.fetchWeather(myLatitude, myLongitude, lang, units)
            ?.observe(viewLifecycleOwner,
                Observer<WeatherResult> {
                    if (it != null) {
                        insertDB(it)
                    }
                })
    }

    private fun initRecyclerView() {
        binding.rcWeatherFavourite.layoutManager = LinearLayoutManager(context)
        adapter = WeatherFavouriteAdapter(this@FavouriteFragment)
        binding.rcWeatherFavourite.adapter = adapter
        displayWeatherList()
    }

    private fun insertDB(weatherResult: WeatherResult) {
        weatherViewModel.insert(weatherResult)
    }

    private fun deleteFromDB(weatherResult: WeatherResult) {
        weatherViewModel.delete(weatherResult)
    }

    private fun displayWeatherList() {
        weatherViewModel.weatherList.observe(this, Observer {
            adapter.setList(it)
            adapter.notifyDataSetChanged()
        })

    }

    override fun onCellClickListener(data: WeatherResult) {
        val action =
            FavouriteFragmentDirections.actionFavouriteFragmentToWeatherFavouriteFragment(data)
        view?.let {
            Navigation.findNavController(it)
                .navigate(action)
        }
    }
}

interface CellClickListener {
    fun onCellClickListener(data: WeatherResult)
}
