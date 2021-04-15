package com.example.theweather.view.ui

import android.location.Geocoder
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs

import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.theweather.R

class MapsFragment : Fragment() {
    private lateinit var geocoder: Geocoder
    private lateinit var bundle: Bundle
    private val args: MapsFragmentArgs by navArgs()

    private val callback = OnMapReadyCallback { googleMap ->
        googleMap.setOnMapClickListener {
            bundle = bundleOf("lat" to it.latitude, "lon" to it.longitude)
            if (args.toHomeFragment) {
                Navigation.findNavController(requireView()).navigate(R.id.action_mapsFragment_to_homeFragment, bundle)
            } else {
                Navigation.findNavController(requireView()).navigate(R.id.action_mapsFragment_to_favouriteFragment, bundle)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_maps, container, false)
        geocoder = Geocoder(context)
        bundle = Bundle()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

}