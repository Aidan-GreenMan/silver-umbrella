package digital.greenman.silverumbrella.domain.model

data class GeoDetails(
    /**
     * location is built up as follows:
     *
     * name, state, country
     */
    val location: String,
    /**
     * lat, lon
     */
    val coordinates: Pair<Double, Double>,

)