package digital.greenman.silverumbrella

import android.app.Application
import digital.greenman.silverumbrella.data.remote.OpenWeatherMapClient
import digital.greenman.silverumbrella.data.repository.GeoRepositoryImpl
import digital.greenman.silverumbrella.data.repository.WeatherRepositoryImpl
import kotlin.getValue

class MainApplication : Application() {
    private val apiClient by lazy { OpenWeatherMapClient(BuildConfig.API_KEY) }
    val weatherRepository by lazy { WeatherRepositoryImpl(apiClient.weatherApi) }
    val geoRepository by lazy { GeoRepositoryImpl(apiClient.geoApi) }
}