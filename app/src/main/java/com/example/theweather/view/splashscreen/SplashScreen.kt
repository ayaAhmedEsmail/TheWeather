package com.example.theweather.view.splashscreen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.example.theweather.R
import com.example.theweather.databinding.ActivitySplashScreenBinding
import com.example.theweather.view.ui.MainActivity

@Suppress("DEPRECATION")
class SplashScreen : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash_screen)
        val splashIcon: ImageView = findViewById(R.id.Splash_icon)
        val titleTheWeather: TextView = findViewById(R.id.titleTheWeather)

        val iconAnimation =AnimationUtils.loadAnimation(this,R.anim.icon_animation)
        val titleAnimation =AnimationUtils.loadAnimation(this,R.anim.text_animation)


        splashIcon.animation = iconAnimation
        titleTheWeather.animation = titleAnimation


        //Home page after 4s
        val homeIntent = Intent(this@SplashScreen , MainActivity::class.java)
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(homeIntent)
            finish()
        }, 4000)

    }
}