package lib.kanda.sessaodatarde

import lib.kanda.sessaodatarde.RETRY_STATUS.DONT_NEED_RETRY
import lib.kanda.sessaodatarde.RETRY_STATUS.NEED_RETRY


enum class RETRY_STATUS {
    NEED_RETRY,
    DONT_NEED_RETRY
}

typealias Retry = Int

abstract class Judge(
        val maxRetries: Retry = 1,
        val listErrorToRetry: MutableList<Throwable>,
        val listStopPooling: MutableList<Throwable>) {


    abstract fun timeToRetry(retryCount: Retry): Long

    abstract fun checkIfRetryIsNeeded(retryCount: Retry, err: Throwable): RETRY_STATUS

    fun isAnErrorToIgnore(err: Throwable): Boolean {
        return listStopPooling.contains(err)
    }

    fun isAnErrorToRetry(count: Int, err: Throwable): Boolean {
        return listErrorToRetry.contains(err) && !isMaxNumberOfRetries(count)
    }

    fun isMaxNumberOfRetries(retryCount: Retry): Boolean {
        return retryCount == maxRetries
    }

}

class Referee(
        retries: Retry = 1,
        listIgnore: MutableList<Throwable>,
        listRetry: MutableList<Throwable>) : Judge(retries, listRetry, listIgnore) {

    override fun timeToRetry(retryCount: Retry): Long {
        return 1000 * retryCount.toLong()
    }

    override fun checkIfRetryIsNeeded(retryCount: Retry, err: Throwable): RETRY_STATUS {
        if (isAnErrorToRetry(retryCount, err)) {
            NEED_RETRY
        }
        return DONT_NEED_RETRY
    }
}