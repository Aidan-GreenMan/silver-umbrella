package digital.greenman.silverumbrella.ui.weather

import digital.greenman.silverumbrella.domain.model.AppException
import digital.greenman.silverumbrella.domain.model.GeoDetails
import digital.greenman.silverumbrella.domain.repository.GeoRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CitiesViewModelTest {

    private lateinit var viewModel: CitiesViewModel
    private val repository: GeoRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CitiesViewModel(repository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() {
        assertEquals(CitiesState.Idle, viewModel.citiesState.value)
    }

    @Test
    fun `resetToIdle sets state to Idle`() {
        // Given
        viewModel.resetToIdle()

        // Then
        assertEquals(CitiesState.Idle, viewModel.citiesState.value)
    }

    @Test
    fun `getCities with empty query sets state to Idle`() {
        // When
        viewModel.getCities("", false)

        // Then
        assertEquals(CitiesState.Idle, viewModel.citiesState.value)
    }

    @Test
    fun `getCities with short query sets state to Idle`() {
        // When
        viewModel.getCities("ab", false)

        // Then
        assertEquals(CitiesState.Idle, viewModel.citiesState.value)
    }

    @Test
    fun `getCities debounces execution`() = runTest(testDispatcher) {
        // Given
        val query = "Cape Town"
        val cities = listOf(
            GeoDetails("Cape Town, Western Cape, ZA", -33.9288301 to 18.4172197)
        )
        coEvery { repository.getCities(query) } returns Result.success(cities)

        // When
        viewModel.getCities(query, true)

        // Advance time partially but not enough for debounce
        advanceTimeBy(400)
        
        // Then: Should still be Idle because delay is 500ms
        assertEquals(CitiesState.Idle, viewModel.citiesState.value)

        // Advance past debounce time
        advanceTimeBy(101)
        testScheduler.runCurrent()
        
        // Then: check final state.
        val currentState = viewModel.citiesState.value
        assertTrue(currentState is CitiesState.Success)
    }

    @Test
    fun `getCities updates state to Success with distinct items limited to 5`() = runTest(testDispatcher) {
        // Given
        val query = "London"
        val duplicateCity = GeoDetails("London, UK", 51.5074 to -0.1278)
        val city2 = GeoDetails("London, ON, CA", 42.9849 to -81.2453)
        val city3 = GeoDetails("London, OH, USA", 39.8865 to -83.4483)
        val city4 = GeoDetails("London, KY, USA", 37.1290 to -84.0833)
        val city5 = GeoDetails("London, AR, USA", 35.3284 to -93.2524)
        val city6 = GeoDetails("London, TX, USA", 30.7093 to -99.5786) // Should be excluded by take(5)
        
        val citiesList = listOf(duplicateCity, duplicateCity, city2, city3, city4, city5, city6)
        
        coEvery { repository.getCities(query) } returns Result.success(citiesList)

        // When
        viewModel.getCities(query, false)

        advanceUntilIdle()

        // Then
        val currentState = viewModel.citiesState.value
        assertTrue(currentState is CitiesState.Success)
        val successState = currentState as CitiesState.Success
        
        // Verify duplicate was removed and list limited
        assertEquals(5, successState.cities.size)
        assertEquals(duplicateCity, successState.cities[0])
        assertEquals(city2, successState.cities[1])
    }

    @Test
    fun `getCities updates state to Error when repository returns failure`() = runTest(testDispatcher) {
        // Given
        val query = "Unknown"
        val exception = AppException.CityNotFoundException()
        coEvery { repository.getCities(query) } returns Result.failure(exception)

        // When
        viewModel.getCities(query, false)

        advanceUntilIdle()

        // Then
        val currentState = viewModel.citiesState.value
        assertTrue(currentState is CitiesState.Error)
        assertEquals(exception, (currentState as CitiesState.Error).exception)
    }

    @Test
    fun `getCities updates state to Error with UnknownException for generic errors`() = runTest(testDispatcher) {
        // Given
        val query = "Error"
        val exception = RuntimeException("Something went wrong")
        coEvery { repository.getCities(query) } returns Result.failure(exception)

        // When
        viewModel.getCities(query, false)

        advanceUntilIdle()

        // Then
        val currentState = viewModel.citiesState.value
        assertTrue(currentState is CitiesState.Error)

        val errorState = currentState as CitiesState.Error
        assertTrue(errorState.exception is AppException.UnknownException)
        assertEquals(exception, errorState.exception.cause)
    }
}
