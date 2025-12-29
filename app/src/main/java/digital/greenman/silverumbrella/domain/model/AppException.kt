package digital.greenman.silverumbrella.domain.model

sealed class AppException : Exception() {
    class NetworkUnavailableException : AppException()
    class CityNotFoundException : AppException()
    class ServerException : AppException()
    class EmptyResultsException : AppException()
    data class UnknownException(
        override val message: String? = null,
        override val cause: Throwable? = null
    ) : AppException()
}
