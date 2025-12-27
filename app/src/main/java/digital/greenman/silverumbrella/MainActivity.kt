package digital.greenman.silverumbrella

import android.os.Bundle
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
import digital.greenman.silverumbrella.ui.theme.SilverUmbrellaTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

            // TODO: BuildConfig API key
            val apiClient = OpenWeatherMapClient("")

        lifecycleScope.launch {
            val cityData = apiClient.geoApi.getCities("Cape Town").body()?.firstOrNull()

            println("City Data: $cityData")
            if (cityData != null) {
                val weatherData =
                    apiClient.weatherApi.getCurrentWeather(cityData.lat, cityData.lon).body()
                println("Weather Data: $weatherData")
            }
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