package digital.greenman.silverumbrella.ui.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import digital.greenman.silverumbrella.R
import digital.greenman.silverumbrella.domain.model.GeoDetails
import digital.greenman.silverumbrella.domain.model.WeatherDetails
import digital.greenman.silverumbrella.ui.theme.SilverUmbrellaTheme

@Composable
fun WeatherScreen(
    citiesState: CitiesState,
    weatherState: WeatherState,
    onSearch: (String) -> Unit,
    onCitySelected: (GeoDetails) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var cityQuery by remember { mutableStateOf("") }

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
                    onSearch(cityQuery)
                }),
                trailingIcon = {
                    Icon(
                        modifier = Modifier.clickable {
                            cityQuery = ""
                            onSearch(cityQuery)
                        },
                        imageVector = if (cityQuery.isNotBlank()) Icons.Default.Clear else Icons.Default.Search,
                        contentDescription = null,
                    )
                },
                singleLine = true,
                value = cityQuery,
                onValueChange = {
                    cityQuery = it
                    onSearch(cityQuery)
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
                        Text(
                            text = stringResource(R.string.city_label, weatherState.weather.city),
                            style = typography.headlineSmall
                        )
                        Text(
                            text = stringResource(
                                R.string.condition_label, weatherState.weather.condition
                            )
                        )
                        Text(
                            text = stringResource(
                                R.string.description_label, weatherState.weather.description
                            )
                        )
                        Text(
                            text = stringResource(
                                R.string.temperature_c_label, weatherState.weather.temperature
                            )
                        )
                        Row {
                            weatherState.weather.icons.forEach {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(Color.LightGray)
                                ) {
                                    AsyncImage(
                                        model = "https://openweathermap.org/img/wn/$it@2x.png",
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }
                        }
                    }
                }

                is WeatherState.Error -> {
                    LaunchedEffect(weatherState.message) {
                        onError(weatherState.message)
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
                                modifier = Modifier
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
                    LaunchedEffect(citiesState.message) {
                        onError(citiesState.message)
                    }
                }

                else -> {} // do nothing
            }
        }
    }
}

@Preview(showBackground = true)
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
            ), onSearch = {}, onCitySelected = {}, onError = {})
    }
}
