package lib.kanda.sessaodatarde

/**
 * Created by jcosilva on 1/25/2018.
 */

sealed class PollingException : Throwable(){
    object Polling : PollingException()

}