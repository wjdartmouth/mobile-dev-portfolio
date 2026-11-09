package com.jianwang.portfolio.pulseweather
import android.app.Application
import com.jianwang.portfolio.pulseweather.data.WeatherCache
class PulseWeatherApp : Application() {
    lateinit var cache: WeatherCache private set
    override fun onCreate() {
        super.onCreate()
        cache = WeatherCache(this)
    }
}
