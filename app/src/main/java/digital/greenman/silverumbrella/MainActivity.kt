package digital.greenman.silverumbrella

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import digital.greenman.silverumbrella.data.remote.OpenWeatherMapClient
import digital.greenman.silverumbrella.data.repository.GeoRepositoryImpl
import digital.greenman.silverumbrella.data.repository.WeatherRepositoryImpl
import digital.greenman.silverumbrella.ui.theme.SilverUmbrellaTheme
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val apiClient = OpenWeatherMapClient(BuildConfig.API_KEY)
        val geoRepo = GeoRepositoryImpl(apiClient.geoApi)
        val weatherRepo = WeatherRepositoryImpl(apiClient.weatherApi)

        val errorHandler: (Throwable) -> Unit = { error ->
            Log.e(TAG, "Error: $error")
        }

        lifecycleScope.launch {
            geoRepo.getCities("Cape Town").onSuccess { cities ->
                    val firstCity = cities.first()
                    println("City Data: $firstCity")
                    weatherRepo.getCurrentWeather(
                        firstCity.coordinates.first,
                        firstCity.coordinates.second
                    ).onSuccess { weatherData ->
                        println("Weather Data: $weatherData")
                    }.onFailure(errorHandler)
                }.onFailure(errorHandler)
        }

        setContent {
            SilverUmbrellaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SilverUmbrellaTheme {
        Greeting("Android")
    }
}