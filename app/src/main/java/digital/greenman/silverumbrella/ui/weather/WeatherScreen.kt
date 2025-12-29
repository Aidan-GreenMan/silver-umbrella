package digital.greenman.silverumbrella.ui.weather

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import digital.greenman.silverumbrella.R
import digital.greenman.silverumbrella.domain.model.AppException
import digital.greenman.silverumbrella.domain.model.GeoDetails
import digital.greenman.silverumbrella.domain.model.WeatherDetails
import digital.greenman.silverumbrella.ui.theme.SilverUmbrellaTheme

@Composable
fun getErrorMessage(error: AppException): String {
    return when (error) {
        is AppException.NetworkUnavailableException -> stringResource(R.string.error_network_unavailable)
        is AppException.CityNotFoundException -> stringResource(R.string.error_city_not_found)
        is AppException.ServerException -> stringResource(R.string.error_server)
        is AppException.EmptyResultsException -> stringResource(R.string.error_empty_results)
        is AppException.UnknownException -> stringResource(R.string.error_unknown)
    }
}

@Composable
fun WeatherScreen(
    citiesState: CitiesState,
    weatherState: WeatherState,
    onSearch: (query: String, debounce: Boolean) -> Unit,
    onCitySelected: (GeoDetails) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var cityQuery by remember { mutableStateOf("") }

    val gradientColors = listOf(
        colorResource(R.color.weather_gradient_start),
        colorResource(R.color.weather_gradient_end)
    )

    val cardBackgroundColor = colorResource(R.color.weather_card_bg)
    val contentColor = colorResource(R.color.weather_content)
    val descriptionColor = colorResource(R.color.weather_description)

    Scaffold(
        modifier = modifier
            .imePadding() // adjust layout for keyboard
            .fillMaxSize(), bottomBar = {
            TextField(
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search,
                    autoCorrectEnabled = false,
                ),
                keyboardActions = KeyboardActions(onSearch = {
                    onSearch(cityQuery, false)
                }),
                trailingIcon = {
                    Icon(
                        modifier = Modifier.clickable {
                            cityQuery = ""
                            onSearch(cityQuery, false)
                        },
                        imageVector = if (cityQuery.isNotBlank()) Icons.Default.Clear else Icons.Default.Search,
                        contentDescription = null,
                    )
                },
                singleLine = true,
                value = cityQuery,
                onValueChange = {
                    cityQuery = it
                    onSearch(cityQuery, true)
                },
                label = { Text(stringResource(R.string.search_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = gradientColors
                    )
                )
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            if (citiesState is CitiesState.Loading || weatherState is WeatherState.Loading) {
                CircularProgressIndicator()
            }

            when (weatherState) {
                is WeatherState.Success -> {
                    if (weatherState.weather != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = cardBackgroundColor,
                                contentColor = contentColor
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = weatherState.weather.city,
                                    style = typography.headlineMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${weatherState.weather.temperature}°C",
                                    style = typography.displayLarge
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = weatherState.weather.condition,
                                    style = typography.titleMedium
                                )
                                Text(
                                    text = weatherState.weather.description,
                                    style = typography.bodyMedium,
                                    color = descriptionColor
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    weatherState.weather.icons.forEach { iconCode ->
                                        AsyncImage(
                                            model = "https://openweathermap.org/img/wn/$iconCode@2x.png",
                                            contentDescription = null,
                                            modifier = Modifier.size(80.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                is WeatherState.Error -> {
                    val message = getErrorMessage(weatherState.exception)
                    LaunchedEffect(message) {
                        onError(message)
                    }
                }

                else -> {} // do nothing
            }

            when (citiesState) {
                is CitiesState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(bottom = 8.dp),
                        // Push cities list down
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        items(
                            items = citiesState.cities,
                            key = { city -> "${city.location} - ${city.coordinates.first}, ${city.coordinates.second}" }) { city ->
                            Text(
                                text = city.location,
                                color = contentColor,
                                modifier = Modifier
                                    .background(cardBackgroundColor)
                                    .fillMaxWidth()
                                    .clickable {
                                        @Suppress("AssignedValueIsNeverRead")
                                        cityQuery = city.location
                                        onCitySelected(city)
                                    }
                                    .padding(16.dp))
                            HorizontalDivider()
                        }
                    }
                }

                is CitiesState.Error -> {
                    val message = getErrorMessage(citiesState.exception)
                    LaunchedEffect(message) {
                        onError(message)
                    }
                }

                else -> {} // do nothing
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun WeatherScreenPreview() {
    SilverUmbrellaTheme {
        WeatherScreen(
            weatherState = WeatherState.Success(
                WeatherDetails(
                    "Preview City",
                    "Preview Condition",
                    "Preview Description",
                    25.0,
                    listOf("50d", "13d")
                )
            ), citiesState = CitiesState.Success(
                listOf(
                    GeoDetails("Preview City, Country", Pair(0.0, 0.0)),
                    GeoDetails("Preview City, Country1", Pair(1.0, 1.0)),
                    GeoDetails("Preview City, Country2", Pair(2.0, 2.0))
                )
            ), onSearch = { _, _ -> }, onCitySelected = {}, onError = {})
    }
}
