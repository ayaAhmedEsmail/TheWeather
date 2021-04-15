package com.example.theweather.view.ui
import android.os.Bundle

import android.content.Intent
import androidx.navigation.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat

import com.example.theweather.R
import com.example.theweather.view.ui.MainActivity
import java.util.*

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val mapFragment: SwitchPreferenceCompat? = findPreference("deviceLocation")
        val weatherAlertFragment: Preference? = findPreference("customNotification")
        val languageSystem: Preference? = findPreference("languageSystem")

        mapFragment?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                val isChecked = newValue as? Boolean ?: false
                if (isChecked) {
                    val action =
                        SettingsFragmentDirections.actionSettingsFragmentToMapsFragment(true)
                    view?.findNavController()?.navigate(action)
                }
                true
            }
        weatherAlertFragment?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            view?.findNavController()
                ?.navigate(R.id.action_settingsFragment_to_weatherAlertFragment)
            true
        }
        languageSystem?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, _ ->
                startActivity(Intent(requireContext(), MainActivity::class.java))
                true
            }

    }

}