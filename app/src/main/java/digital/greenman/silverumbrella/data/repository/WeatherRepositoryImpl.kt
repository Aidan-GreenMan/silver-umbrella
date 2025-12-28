package digital.greenman.silverumbrella.data.repository

import android.util.Log
import digital.greenman.silverumbrella.data.mapper.toDomain
import digital.greenman.silverumbrella.data.remote.WeatherApi
import digital.greenman.silverumbrella.domain.model.WeatherDetails
import digital.greenman.silverumbrella.domain.repository.WeatherRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "WeatherRepositoryImpl"

class WeatherRepositoryImpl(private val weatherApi: WeatherApi) : WeatherRepository {
    override suspend fun getCurrentWeather(lat: Double, lon: Double): Result<WeatherDetails> {
        return withContext(Dispatchers.IO) { // Switch to IO thread
            try {
                val response = weatherApi.getCurrentWeather(lat, lon)
                val data = response.body()

                if (response.isSuccessful && data != null) {
                    Result.success(data.toDomain())
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Failed to fetch weather data: ${response.code()} - $errorBody")

                    if (response.code() == 404) {
                        Result.failure(Exception("Location not found."))
                    } else {
                        Result.failure(Exception("Failed to fetch weather data: ${response.message()}"))
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Exception fetching weather data", e)
                Result.failure(e)
            }
        }
    }
}
