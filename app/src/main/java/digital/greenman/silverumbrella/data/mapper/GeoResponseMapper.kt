package digital.greenman.silverumbrella.data.mapper

import digital.greenman.silverumbrella.data.model.GeoResponse
import digital.greenman.silverumbrella.domain.model.GeoDetails

fun GeoResponse.toDomain(): GeoDetails {
    val location = listOfNotNull(name, state, country)
        .filter { it.isNotBlank() }
        .joinToString(", ")

    return GeoDetails(
        location,
        coordinates = Pair(lat, lon)
    )
}