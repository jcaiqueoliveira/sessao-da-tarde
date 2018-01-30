package lib.kanda.sessaodatarde

/**
 * Created by jcosilva on 1/25/2018.
 */

sealed class PollingException : Throwable(){
    object Polling : PollingException()

}

sealed class ServerException : Throwable()
class InternalServerError : ServerException()

sealed class ApiException : Throwable()
class NotFoundException : ApiException()