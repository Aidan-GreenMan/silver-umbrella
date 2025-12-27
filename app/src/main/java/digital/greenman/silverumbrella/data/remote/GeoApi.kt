package digital.greenman.silverumbrella.data.remote

import digital.greenman.silverumbrella.data.model.GeoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GeoApi {
    @GET("geo/1.0/direct")
    suspend fun getCities(
        @Query("q") city: String,
        @Query("limit") limit: Int = 5
    ): Response<List<GeoResponse>>
}