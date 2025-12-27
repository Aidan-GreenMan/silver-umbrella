package digital.greenman.silverumbrella.data.model

data class GeoResponse(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String,
    val state: String?
)
