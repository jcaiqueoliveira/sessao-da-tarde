package lib.kanda.sessaodatarde

import lib.kanda.sessaodatarde.RETRY_STATUS.DONT_NEED_RETRY
import lib.kanda.sessaodatarde.RETRY_STATUS.NEED_RETRY


enum class RETRY_STATUS {
    NEED_RETRY,
    DONT_NEED_RETRY
}

typealias Retry = Int

abstract class Judge(
        val maxRetries: Retry = 1) {

    abstract fun timeToRetry(retryCount: Retry): Long

    abstract fun checkIfRetryIsNeeded(retryCount: Retry, err: Throwable): RETRY_STATUS


    private fun isAPollingException(err: Throwable): Boolean {
        return err is PollingException
    }

    private fun isANetworkException(err: Throwable): Boolean {
        return err is NetworkException
    }

    private fun isAApiException(err: Throwable): Boolean {
        return err is ApiException
    }

    fun isRetryNeeded(count: Int, err: Throwable): Boolean {
        return when {
            isMaxNumberOfRetries(count) -> false
            isANetworkException(err) -> true
            isAPollingException(err) -> true
            isAApiException(err) -> false
            else -> false
        }
    }

    fun isMaxNumberOfRetries(retryCount: Retry): Boolean {
        return retryCount == maxRetries
    }

}

class Referee(retries: Retry = 1) : Judge(retries) {

    override fun timeToRetry(retryCount: Retry): Long {
        return 1000
    }

    override fun checkIfRetryIsNeeded(retryCount: Retry, err: Throwable): RETRY_STATUS {
        return when (isRetryNeeded(retryCount, err)) {
            true -> NEED_RETRY
            false -> DONT_NEED_RETRY
        }
    }
}