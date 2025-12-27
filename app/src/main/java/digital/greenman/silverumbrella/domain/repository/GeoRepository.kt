package digital.greenman.silverumbrella.domain.repository

import digital.greenman.silverumbrella.domain.model.GeoDetails

interface GeoRepository {
    suspend fun getCities(city: String): Result<List<GeoDetails>>
}