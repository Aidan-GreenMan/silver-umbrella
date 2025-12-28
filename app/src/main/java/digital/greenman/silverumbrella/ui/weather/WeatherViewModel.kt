package digital.greenman.silverumbrella.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import digital.greenman.silverumbrella.domain.model.WeatherDetails
import digital.greenman.silverumbrella.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _weatherState = MutableStateFlow<WeatherState>(WeatherState.Idle)
    val weatherState: StateFlow<WeatherState> = _weatherState.asStateFlow()

    fun resetToIdle() {
        _weatherState.value = WeatherState.Idle
    }

    fun getCurrentWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            _weatherState.value = WeatherState.Loading

            val result = repository.getCurrentWeather(lat, lon)

            result
                .onSuccess { weather ->
                    _weatherState.value = WeatherState.Success(weather)
                }
                .onFailure { error ->
                    _weatherState.value = WeatherState.Error("Error: ${error.message}")
                }
        }
    }
}

sealed class WeatherState {
    object Idle : WeatherState()
    object Loading : WeatherState()
    data class Success(val weather: WeatherDetails?) : WeatherState()
    data class Error(val message: String) : WeatherState()
}

/**
 * No DI framework implemented, this is a work around to pass the repository to the ViewModel.
 */
class WeatherViewModelFactory(private val repo: WeatherRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeatherViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}