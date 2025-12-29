package digital.greenman.silverumbrella.data.repository

import android.util.Log
import digital.greenman.silverumbrella.data.mapper.toDomain
import digital.greenman.silverumbrella.data.remote.GeoApi
import digital.greenman.silverumbrella.domain.model.AppException
import digital.greenman.silverumbrella.domain.model.GeoDetails
import digital.greenman.silverumbrella.domain.repository.GeoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.UnknownHostException

private const val TAG = "GeoRepositoryImpl"

class GeoRepositoryImpl(private val geoApi: GeoApi) : GeoRepository {
    override suspend fun getCities(city: String): Result<List<GeoDetails>> {
        return withContext(Dispatchers.IO) { // Switch to IO thread
            try {
                val response = geoApi.getCities(city)
                val data = response.body()

                if (response.isSuccessful && data != null) {
                    if (data.isEmpty()) {
                        Result.failure(AppException.EmptyResultsException())
                    } else {
                        Result.success(data.map { it.toDomain() })
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Failed to fetch city data: ${response.code()} - $errorBody")

                    val exception = when (response.code()) {
                        404 -> AppException.CityNotFoundException()
                        in 500..599 -> AppException.ServerException()
                        else -> AppException.UnknownException(message = response.message())
                    }
                    Result.failure(exception)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Exception fetching city data", e)
                val exception = when (e) {
                    is UnknownHostException -> AppException.NetworkUnavailableException()
                    is IOException -> AppException.NetworkUnavailableException()
                    else -> AppException.UnknownException(cause = e)
                }
                Result.failure(exception)
            }
        }
    }
}
