package lib.kanda.sessaodatarde

/**
 * Created by jcosilva on 1/25/2018.
 */

sealed class PollingException : Throwable() {
    object Polling : PollingException()

}

sealed class ServerException : Throwable() {
    object InternalServerError : ServerException()
}

sealed class ApiException : Throwable() {
    object NotFoundException : ApiException()
}

sealed class NetworkException : Throwable() {
    object ConnectionSpike : NetworkException()
}