package com.example.theweather.helper


import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color.RED
import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION
import android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE
import android.media.RingtoneManager.TYPE_NOTIFICATION
import android.media.RingtoneManager.getDefaultUri
import android.os.Build
import android.provider.AlarmClock
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DEFAULT_ALL
import androidx.core.app.NotificationCompat.PRIORITY_MAX
import androidx.core.content.ContextCompat.startActivity
import androidx.core.graphics.createBitmap
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.theweather.R
import com.example.theweather.data.local.WeatherDataBase
import com.example.theweather.view.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
class NotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    private val dao = WeatherDataBase.getInstance(applicationContext).weatherDAO

    companion object {
        const val NOTIFICATION_OR_ALARM = "TheWeather_NOTIFICATION_OR_ALARM"
        const val NOTIFICATION_ID = "TheWeather_notification_id"
        const val NOTIFICATION_NAME = "TheWeather"
        const val NOTIFICATION_CHANNEL = "TheWeather_channel_01"
        const val NOTIFICATION_DES = "TheWeather_notification_Des"
        const val NOTIFICATION_END = "TheWeather_notification_END"
        const val NOTIFICATION_START = "TheWeather_notification_START"
        const val NOTIFICATION_ALERT_DELETE = "TheWeather_notification_ALERT_DELETE"
    }

    override fun doWork(): Result {
        return try {
            val id = inputData.getLong(NOTIFICATION_ID, 0)
            val alertDes = inputData.getString(NOTIFICATION_DES)
            val isAlarm = inputData.getString(NOTIFICATION_OR_ALARM)
            val alertEnd = inputData.getDouble(NOTIFICATION_END, 0.0)
            val alertStart = inputData.getDouble(NOTIFICATION_START, 0.0)
            val alertId = inputData.getLong(NOTIFICATION_ALERT_DELETE, -1)
            if (alertDes != null) {
                when (isAlarm) {
                    "Notification" -> {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            sendNotification(alertId, id, alertDes, alertEnd, alertStart)
                        }
                    }
                    "Alarm" -> {
                        createAlarm(alertId,alertDes, alertStart)
                    }
                    else -> {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            sendNotification(alertId, id, alertDes, alertEnd, alertStart)
                        }
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

     //Create Alarm
    private fun createAlarm( alertId: Long, message: String, alertStart: Double) {
        val calendar = Calendar.getInstance()
        val dateStart = java.util.Date(alertStart.toLong() * 1000)
        calendar.time = dateStart
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_HOUR, calendar.get(Calendar.HOUR_OF_DAY))
            putExtra(AlarmClock.EXTRA_MINUTES, calendar.get(Calendar.MINUTE))
        }
         startActivity(applicationContext, intent, null)
        if (!alertId.equals(-1)) {
            deleteAlert(alertId)
        }
    }

    //Create Notification
    private fun sendNotification(alertId: Long, id: Long, alert: String, alertEnd: Double, alertStart: Double) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = getActivity(applicationContext, 0, intent, 0)
        intent.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(NOTIFICATION_ID, id)

        val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val bitmap = applicationContext.vectorToBitmap(R.drawable.ic_cloudy)
        val sdf = java.text.SimpleDateFormat("EEE, h:mm a")
        val dateEnd = java.util.Date(alertEnd.toLong() * 1000)
        val dateStart = java.util.Date(alertStart.toLong() * 1000)
        val titleNotification = "$alert"
        val startDes = "From ${sdf.format(dateStart)} to ${sdf.format(dateEnd)}"

        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL)
            .setLargeIcon(bitmap)
            .setSmallIcon(R.drawable.ic_cloudy)
            .setContentTitle(titleNotification)
            .setContentText(startDes)
            .setDefaults(DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(PRIORITY_MAX)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification.setChannelId(NOTIFICATION_CHANNEL)

            val ringtoneManager = getDefaultUri(TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder().setUsage(USAGE_NOTIFICATION_RINGTONE)
                .setContentType(CONTENT_TYPE_SONIFICATION).build()

            val channel = NotificationChannel(NOTIFICATION_CHANNEL, NOTIFICATION_NAME, IMPORTANCE_HIGH)
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            channel.setSound(ringtoneManager, audioAttributes)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(id.toInt(), notification.build())
        if (!alertId.equals(-1)) {
            deleteAlert(alertId)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("UseCompatLoadingForDrawables")
    fun Context.vectorToBitmap(drawableId: Int): Bitmap? {
        val drawable = getDrawable(drawableId) ?: return null
        val bitmap = createBitmap(
            drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun deleteAlert(alertId: Long) {
        GlobalScope.launch {
            Dispatchers.IO
            dao.deleteAlertID(alertId)
        }
    }
}