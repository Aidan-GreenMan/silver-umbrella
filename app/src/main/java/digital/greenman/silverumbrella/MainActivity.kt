package digital.greenman.silverumbrella

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import digital.greenman.silverumbrella.ui.theme.SilverUmbrellaTheme
import digital.greenman.silverumbrella.ui.weather.CitiesViewModel
import digital.greenman.silverumbrella.ui.weather.CitiesViewModelFactory
import digital.greenman.silverumbrella.ui.weather.WeatherScreen
import digital.greenman.silverumbrella.ui.weather.WeatherViewModel
import digital.greenman.silverumbrella.ui.weather.WeatherViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    // ViewModels
    private val weatherViewModel: WeatherViewModel by viewModels {
        WeatherViewModelFactory((application as MainApplication).weatherRepository)
    }

    private val citiesViewModel: CitiesViewModel by viewModels {
        CitiesViewModelFactory((application as MainApplication).geoRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val keyboardController = LocalSoftwareKeyboardController.current
            val focusManager = LocalFocusManager.current

            val snackBarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            val weatherState by weatherViewModel.weatherState.collectAsStateWithLifecycle()
            val citiesState by citiesViewModel.citiesState.collectAsStateWithLifecycle()

            SilverUmbrellaTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackBarHostState,
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.ime)
                                // position snackbar above textfield
                                .padding(bottom = 40.dp)
                        )
                    }) { innerPadding ->
                    WeatherScreen(
                        modifier = Modifier.padding(innerPadding),
                        weatherState = weatherState,
                        citiesState = citiesState,
                        onSearch = {
                            weatherViewModel.resetToIdle() // clear weather data
                            citiesViewModel.getCities(it)
                        },
                        onCitySelected = {
                            keyboardController?.hide()
                            focusManager.clearFocus()

                            citiesViewModel.resetToIdle()
                            weatherViewModel.getCurrentWeather(
                                lat = it.coordinates.first,
                                lon = it.coordinates.second
                            )
                        },
                        onError = {
                            scope.launch {
                                snackBarHostState.showSnackbar(it)
                            }
                        }
                    )
                }
            }
        }
    }
}
