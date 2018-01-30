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


    fun isAnErrorToRetry(count: Int, err: Throwable): Boolean {
        return err is PollingException.Polling && !isMaxNumberOfRetries(count)
    }

    fun isMaxNumberOfRetries(retryCount: Retry): Boolean {
        return retryCount == maxRetries
    }

}

class Referee(retries: Retry = 1) : Judge(retries) {

    override fun timeToRetry(retryCount: Retry): Long {
        return 1000 * retryCount.toLong()
    }

    override fun checkIfRetryIsNeeded(retryCount: Retry, err: Throwable): RETRY_STATUS {
        return when (isAnErrorToRetry(retryCount, err)) {
            true -> NEED_RETRY
            false -> DONT_NEED_RETRY
        }
    }
}