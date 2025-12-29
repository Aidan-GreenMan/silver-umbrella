package digital.greenman.silverumbrella.ui.weather

import digital.greenman.silverumbrella.domain.model.AppException
import digital.greenman.silverumbrella.domain.model.WeatherDetails
import digital.greenman.silverumbrella.domain.repository.WeatherRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    private lateinit var viewModel: WeatherViewModel
    private val repository: WeatherRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = WeatherViewModel(repository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() {
        assertEquals(WeatherState.Idle, viewModel.weatherState.value)
    }

    @Test
    fun `resetToIdle sets state to Idle`() {
        // Given
        viewModel.resetToIdle()

        // Then
        assertEquals(WeatherState.Idle, viewModel.weatherState.value)
    }

    @Test
    fun `getCurrentWeather updates state to Success when repository returns data`() = runTest(testDispatcher) {
        // Given
        val lat = 37.7749
        val lon = -122.4194
        val weatherDetails = WeatherDetails(
            city = "San Francisco",
            condition = "Clear",
            description = "Clear sky",
            temperature = 20,
            icons = listOf("icon_url")
        )
        coEvery { repository.getCurrentWeather(lat, lon) } returns Result.success(weatherDetails)

        // When
        viewModel.getCurrentWeather(lat, lon)

        // Then
        advanceUntilIdle()
        
        val currentState = viewModel.weatherState.value
        assert(currentState is WeatherState.Success)
        assertEquals(weatherDetails, (currentState as WeatherState.Success).weather)
    }

    @Test
    fun `getCurrentWeather updates state to Error when repository returns failure`() = runTest(testDispatcher) {
        // Given
        val lat = 37.7749
        val lon = -122.4194
        val exception = AppException.NetworkUnavailableException()
        coEvery { repository.getCurrentWeather(lat, lon) } returns Result.failure(exception)

        // When
        viewModel.getCurrentWeather(lat, lon)

        // Then
        advanceUntilIdle()

        val currentState = viewModel.weatherState.value
        assert(currentState is WeatherState.Error)
        assertEquals(exception, (currentState as WeatherState.Error).exception)
    }

    @Test
    fun `getCurrentWeather updates state to Error with UnknownException when non-AppException occurs`() = runTest(testDispatcher) {
        // Given
        val lat = 37.7749
        val lon = -122.4194
        val exception = RuntimeException("Some random error")
        coEvery { repository.getCurrentWeather(lat, lon) } returns Result.failure(exception)

        // When
        viewModel.getCurrentWeather(lat, lon)

        // Then
        advanceUntilIdle()

        val currentState = viewModel.weatherState.value
        assert(currentState is WeatherState.Error)
        val errorState = currentState as WeatherState.Error
        assert(errorState.exception is AppException.UnknownException)
        assertEquals(exception, errorState.exception.cause)
    }
}
