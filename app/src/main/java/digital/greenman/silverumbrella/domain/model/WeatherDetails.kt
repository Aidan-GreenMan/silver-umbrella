package digital.greenman.silverumbrella.domain.model

data class WeatherDetails(
    val city: String,
    val condition: String,
    val description: String,
    val temperature: Double,
    val icons: List<String>
)