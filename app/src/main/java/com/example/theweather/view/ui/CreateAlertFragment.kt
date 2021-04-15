package com.example.theweather.view.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateFormat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.createDataStore
import androidx.lifecycle.ViewModelProvider
import java.util.*
import androidx.datastore.preferences.core.edit
import androidx.navigation.findNavController
import com.example.theweather.R
import com.example.theweather.databinding.FragmentCreateAlertBinding
import com.example.theweather.model.CreateAlert
import com.example.theweather.viewmodel.WeatherViewModel

class CreateAlertFragment : Fragment(), TimePickerDialog.OnTimeSetListener,
    DatePickerDialog.OnDateSetListener {
    private lateinit var binding: FragmentCreateAlertBinding
    private lateinit var weatherViewModel: WeatherViewModel
    private lateinit var dataStore: DataStore<Preferences>

    private lateinit var c: Calendar
    private lateinit var cEnd: Calendar
    private lateinit var cStart: Calendar

    private lateinit var repetitionWord: String
    private lateinit var alarmOrNotificationWord: String

    private var repeated = arrayOf("No Repetition", "daily", "weekly", "monthly")
    private var alarmOrNotification = arrayOf("Notification", "Alarm")

    private var myStartHour = 0
    private var myStartMinute = 0
    private var myEndHour = 0
    private var myEndMinute = 0


    var day = 0
    var month = 0
    var year = 0
    var hour = 0
    var minute = 0
    var savedDay = 0
    var savedMonth = 0
    var savedYear = 0
    var savedHour = 0
    var savedMinute = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_alert, container, false)
        weatherViewModel = ViewModelProvider(this).get(WeatherViewModel::class.java)
        dataStore = requireContext().createDataStore(name = "settings")
        c = Calendar.getInstance()
        cEnd = Calendar.getInstance()
        cStart = Calendar.getInstance()
        binding.startTime.setOnClickListener {
            if (!TextUtils.isEmpty(binding.selectedDate.text)) {
                getStartTime()
            } else {
                Toast.makeText(context, "Please choose Notification Time", Toast.LENGTH_LONG).show()
            }
        }
        binding.endTime.setOnClickListener {
            if (!TextUtils.isEmpty(binding.selectedDate.text)) {
                getEndTime()
            } else {
                Toast.makeText(context, "Please choose Notification Time", Toast.LENGTH_LONG).show()

            }
        }
        binding.selectedDate.setOnClickListener(View.OnClickListener { pickDate() })
        getRepetition()
        getAlarmOrNotification()
        binding.btnDoneCustomNotification.setOnClickListener {
            if (!TextUtils.isEmpty(binding.startTime.text) &&
                !TextUtils.isEmpty(binding.endTime.text) &&
                !TextUtils.isEmpty(binding.selectedDate.text)
            ) {
                insertAlert(
                    CreateAlert(
                        c.timeInMillis,
                        cStart.timeInMillis,
                        cEnd.timeInMillis,
                        repetitionWord,
                        alarmOrNotificationWord
                    )
                )
                it.findNavController()
                    .navigate(R.id.action_createAlertFragment_to_homeFragment)
            } else {
                Toast.makeText(context, "Please Add All Data", Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }

    private fun getStartTime() {
        getDateTimeCalendar()
        val timePickerDialog = TimePickerDialog(
            activity, { _, hourOfDay, minute ->
                cStart[Calendar.DAY_OF_MONTH] = savedDay
                cStart[Calendar.HOUR_OF_DAY] = hourOfDay
                cStart[Calendar.MINUTE] = minute
                if (cStart.timeInMillis > c.timeInMillis) {
                    myStartHour = hourOfDay
                    myStartMinute = minute
                    binding.startTime.text = DateFormat.format("hh:mm aa", cStart)
                } else {
                    Toast.makeText(context, "Enter Valida Time", Toast.LENGTH_SHORT).show()
                }
            }, 12, 0, false
        )
        timePickerDialog.updateTime(myStartHour, myStartMinute)
        timePickerDialog.show()
    }

    private fun getEndTime() {
        getDateTimeCalendar()
        val timePickerDialog = TimePickerDialog(
            activity, { _, hourOfDay, minute ->
                cEnd[Calendar.DAY_OF_MONTH] = savedDay
                cEnd[Calendar.HOUR_OF_DAY] = hourOfDay
                cEnd[Calendar.MINUTE] = minute
                if (cEnd.timeInMillis > cStart.timeInMillis) {
                    myEndHour = hourOfDay
                    myEndMinute = minute
                    binding.endTime.text = DateFormat.format("hh:mm aa", cEnd)
                } else {
                    Toast.makeText(context, "Enter Valida Time", Toast.LENGTH_SHORT).show()
                }
            }, 12, 0, false
        )
        timePickerDialog.updateTime(myEndHour, myEndMinute)
        timePickerDialog.show()
    }

    private suspend fun saveToDataStore(key: String, value: String) {
        val dataStoreKey = stringPreferencesKey(key)
        dataStore.edit { settings ->
            settings[dataStoreKey] = value
        }
    }

    private fun getDateTimeCalendar() {
        val calendar = Calendar.getInstance()
        day = calendar.get(Calendar.DAY_OF_MONTH)
        month = calendar.get(Calendar.MONTH)
        year = calendar.get(Calendar.YEAR)
        hour = calendar.get(Calendar.HOUR)
        minute = calendar.get(Calendar.MINUTE)
    }

    private fun pickDate() {
        getDateTimeCalendar()
        DatePickerDialog(requireContext(), this, year, month, day).show()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        view?.minDate = Calendar.getInstance().timeInMillis
        savedYear = year
        savedMonth = month
        savedDay = dayOfMonth
        getDateTimeCalendar()
        TimePickerDialog(requireContext(), this, hour, minute, false).show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        c[Calendar.DAY_OF_MONTH] = savedDay
        c[Calendar.MONTH] = savedMonth
        c[Calendar.YEAR] = savedYear
        c[Calendar.HOUR_OF_DAY] = hourOfDay
        c[Calendar.MINUTE] = minute
        if (c.timeInMillis > Calendar.getInstance().timeInMillis) {
            savedHour = hourOfDay
            savedMinute = minute
            binding.selectedDate.text = DateFormat.format("EEE, d MMM hh:mm a", c)
            Log.i("TAG", "onDateSet: ${c.timeInMillis}")
        } else {
            Toast.makeText(context, "Enter Valida Time", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getRepetition() {
        binding.spinnerReapition.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    repetitionWord = parent.getItemAtPosition(position).toString()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, repeated)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerReapition.adapter = adapter
    }

    private fun getAlarmOrNotification() {
        binding.spinnerAlarm.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                alarmOrNotificationWord = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            alarmOrNotification
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerAlarm.adapter = adapter
    }

    private fun insertAlert(createAlert: CreateAlert) {
        weatherViewModel.insertAlert(createAlert)
    }
}