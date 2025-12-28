package digital.greenman.silverumbrella.data.repository

import android.util.Log
import digital.greenman.silverumbrella.data.mapper.toDomain
import digital.greenman.silverumbrella.data.remote.GeoApi
import digital.greenman.silverumbrella.domain.model.GeoDetails
import digital.greenman.silverumbrella.domain.repository.GeoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "GeoRepositoryImpl"

class GeoRepositoryImpl(private val geoApi: GeoApi) : GeoRepository {
    override suspend fun getCities(city: String): Result<List<GeoDetails>> {
        return withContext(Dispatchers.IO) { // Switch to IO thread
            try {
                val response = geoApi.getCities(city)
                val data = response.body()

                if (response.isSuccessful && data != null) {
                    Result.success(data.map { it.toDomain() })
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Failed to fetch city data: ${response.code()} - $errorBody")

                    if (response.code() == 404) {
                        Result.failure(Exception("Please check the city name and try again."))
                    } else {
                        Result.failure(Exception("Failed to fetch city data: ${response.message()}"))
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Exception fetching city data", e)
                Result.failure(e)
            }
        }
    }
}
