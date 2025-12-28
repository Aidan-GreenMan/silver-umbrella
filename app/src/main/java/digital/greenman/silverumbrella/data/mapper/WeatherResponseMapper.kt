package digital.greenman.silverumbrella.data.mapper

import digital.greenman.silverumbrella.data.model.WeatherResponse
import digital.greenman.silverumbrella.domain.model.WeatherDetails

fun WeatherResponse.toDomain(): WeatherDetails {
    val condition =
        if (weather.isNotEmpty())
            weather.filter { it.main.isNotBlank() }
                .joinToString(". ") { it.main }
        else "Unknown"

    val description =
        if (weather.isNotEmpty())
            weather.filter { it.description.isNotBlank() }
                .joinToString(". ") { it.description }
        else "Unknown"

    return WeatherDetails(
        city = name,
        condition,
        description,
        temperature = main.temp,
        icons = weather.map { it.icon }
    )
}