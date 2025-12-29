package digital.greenman.silverumbrella.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import digital.greenman.silverumbrella.domain.model.AppException
import digital.greenman.silverumbrella.domain.model.GeoDetails
import digital.greenman.silverumbrella.domain.repository.GeoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CitiesViewModel(private val repository: GeoRepository) : ViewModel() {

    private val _citiesState = MutableStateFlow<CitiesState>(CitiesState.Idle)
    val citiesState: StateFlow<CitiesState> = _citiesState.asStateFlow()

    private var searchJob: Job? = null

    fun resetToIdle() {
        _citiesState.value = CitiesState.Idle
        searchJob?.cancel()
    }

    /**
     * Fetches a list of cities matching the provided search query.
     *
     * This function updates [citiesState] with the result. If [debounce] is true,
     * the search is delayed by 500ms to avoid excessive network requests while typing.
     *
     * @param query The text string to search for. Must be at least 3 characters long.
     * @param debounce If true, adds a 500ms delay before executing the search.
     */
    fun getCities(query: String, debounce: Boolean) {

        searchJob?.cancel()

        if (query.isBlank() || query.length < 3) {
            _citiesState.value = CitiesState.Idle
            return
        }

        searchJob = viewModelScope.launch {
            if (debounce) delay(500) // Debounce
            _citiesState.value = CitiesState.Loading

            val result = repository.getCities(query)

            result
                .onSuccess { cities ->
                    // only show 5 items to ensure UI stability
                    _citiesState.value = CitiesState.Success(cities.distinct().take(5))
                }
                .onFailure { error ->
                    val appException =
                        error as? AppException ?: AppException.UnknownException(cause = error)
                    _citiesState.value = CitiesState.Error(appException)
                }
        }
    }
}

sealed class CitiesState {
    object Idle : CitiesState()
    object Loading : CitiesState()
    data class Success(val cities: List<GeoDetails>) : CitiesState()
    data class Error(val exception: AppException) : CitiesState()
}

/**
 * No DI framework implemented, this is a work around to pass the repository to the ViewModel.
 */
class CitiesViewModelFactory(private val repo: GeoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CitiesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CitiesViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
