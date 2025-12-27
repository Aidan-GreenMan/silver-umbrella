package digital.greenman.silverumbrella.domain.repository

import digital.greenman.silverumbrella.domain.model.WeatherDetails

interface WeatherRepository {
    suspend fun getCurrentWeather(lat: Double, lon: Double): Result<WeatherDetails>
}
