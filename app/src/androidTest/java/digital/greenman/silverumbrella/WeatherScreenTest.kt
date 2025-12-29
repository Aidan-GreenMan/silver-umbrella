package digital.greenman.silverumbrella

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import digital.greenman.silverumbrella.domain.model.AppException
import digital.greenman.silverumbrella.domain.model.GeoDetails
import digital.greenman.silverumbrella.domain.model.WeatherDetails
import digital.greenman.silverumbrella.ui.weather.CitiesState
import digital.greenman.silverumbrella.ui.weather.WeatherScreen
import digital.greenman.silverumbrella.ui.weather.WeatherState
import org.junit.Rule
import org.junit.Test

class WeatherScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun searchFieldIsDisplayed() {
        composeTestRule.setContent {
            WeatherScreen(
                citiesState = CitiesState.Idle,
                weatherState = WeatherState.Idle,
                onSearch = {_,_->},
                onCitySelected = {},
                onError = {}
            )
        }

        val searchPlaceholder = context.getString(R.string.search_placeholder)
        composeTestRule.onNodeWithText(searchPlaceholder).assertIsDisplayed()
    }

    @Test
    fun loadingIndicatorIsDisplayed_whenCitiesLoading() {
        composeTestRule.setContent {
            WeatherScreen(
                citiesState = CitiesState.Loading,
                weatherState = WeatherState.Idle,
                onSearch = {_,_->},
                onCitySelected = {},
                onError = {}
            )
        }

        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
    }

    @Test
    fun loadingIndicatorIsDisplayed_whenWeatherLoading() {
        composeTestRule.setContent {
            WeatherScreen(
                citiesState = CitiesState.Idle,
                weatherState = WeatherState.Loading,
                onSearch = {_,_->},
                onCitySelected = {},
                onError = {}
            )
        }

        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
    }

    @Test
    fun weatherDetailsAreDisplayed_whenWeatherSuccess() {
        val weatherDetails = WeatherDetails(
            city = "London",
            condition = "Cloudy",
            description = "scattered clouds",
            temperature = 15,
            icons = listOf("03d")
        )

        composeTestRule.setContent {
            WeatherScreen(
                citiesState = CitiesState.Idle,
                weatherState = WeatherState.Success(weatherDetails),
                onSearch = {_,_->},
                onCitySelected = {},
                onError = {}
            )
        }

        composeTestRule.onNodeWithText(weatherDetails.city).assertIsDisplayed()
        composeTestRule.onNodeWithText("${weatherDetails.temperature}°C").assertIsDisplayed()
        composeTestRule.onNodeWithText(weatherDetails.condition).assertIsDisplayed()
        composeTestRule.onNodeWithText(weatherDetails.description).assertIsDisplayed()
    }

    @Test
    fun citiesListIsDisplayed_whenCitiesSuccess() {
        val cities = listOf(
            GeoDetails("London, GB", Pair(51.5, -0.12)),
            GeoDetails("London, CA", Pair(42.98, -81.24))
        )

        composeTestRule.setContent {
            WeatherScreen(
                citiesState = CitiesState.Success(cities),
                weatherState = WeatherState.Idle,
                onSearch = {_,_->},
                onCitySelected = {},
                onError = {}
            )
        }

        cities.forEach { city ->
            composeTestRule.onNodeWithText(city.location).assertIsDisplayed()
        }
    }
    
    @Test
    fun onSearch_isCalled_whenTextEntered() {
        var searchQuery = ""
        composeTestRule.setContent {
            WeatherScreen(
                citiesState = CitiesState.Idle,
                weatherState = WeatherState.Idle,
                onSearch = { query, _ -> searchQuery = query },
                onCitySelected = {},
                onError = {}
            )
        }
        
        val searchPlaceholder = context.getString(R.string.search_placeholder)
        composeTestRule.onNodeWithText(searchPlaceholder).performTextInput("Lon")
        assert(searchQuery == "Lon")
    }

    @Test
    fun onCitySelected_isCalled_whenCityClicked() {
        var selectedCity: GeoDetails? = null
        val cities = listOf(
            GeoDetails("Paris, FR", Pair(48.85, 2.35))
        )

        composeTestRule.setContent {
            WeatherScreen(
                citiesState = CitiesState.Success(cities),
                weatherState = WeatherState.Idle,
                onSearch = {_,_->},
                onCitySelected = { selectedCity = it },
                onError = {}
            )
        }

        composeTestRule.onNodeWithText("Paris, FR").performClick()
        assert(selectedCity == cities[0])
    }

    @Test
    fun onError_isCalled_whenWeatherError() {
        var errorMessage = ""
        val error = AppException.NetworkUnavailableException()
        composeTestRule.setContent {
            WeatherScreen(
                citiesState = CitiesState.Idle,
                weatherState = WeatherState.Error(error),
                onSearch = {_,_->},
                onCitySelected = {},
                onError = { errorMessage = it }
            )
        }

        val expectedMessage = context.getString(R.string.error_network_unavailable)
        composeTestRule.waitForIdle() 
        assert(errorMessage == expectedMessage)
    }

    @Test
    fun onError_isCalled_whenCitiesError() {
        var errorMessage = ""
        val error = AppException.CityNotFoundException()
        composeTestRule.setContent {
            WeatherScreen(
                citiesState = CitiesState.Error(error),
                weatherState = WeatherState.Idle,
                onSearch = {_,_->},
                onCitySelected = {},
                onError = { errorMessage = it }
            )
        }

        val expectedMessage = context.getString(R.string.error_city_not_found)
        composeTestRule.waitForIdle()
        assert(errorMessage == expectedMessage)
    }
}
