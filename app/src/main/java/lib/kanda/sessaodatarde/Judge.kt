package lib.kanda.sessaodatarde

import lib.kanda.sessaodatarde.RETRY_STATUS.DONT_NEED_RETRY
import lib.kanda.sessaodatarde.RETRY_STATUS.NEED_RETRY


enum class RETRY_STATUS {
    NEED_RETRY,
    DONT_NEED_RETRY
}

typealias Retry = Int

abstract class Judge(var maxRetries: Retry = 0) {

    private val listErrorToRetry: ArrayList<Throwable> = arrayListOf(PollingException.Polling)

    private val listStopPooling: ArrayList<Throwable> = arrayListOf(PollingException.Polling)

    abstract fun timeToRetry(retryCount: Retry): Long

    abstract fun checkIfRetryIsNeeded(retryCount: Retry, err: Throwable): RETRY_STATUS

    fun <T : Throwable> addErrorToRetry(listError: List<T>) = listErrorToRetry.addAll(listError)

    fun <T : Throwable> addErrorToIgnore(listError: List<T>) = listStopPooling.addAll(listError)

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

class Referee : Judge(4) {

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