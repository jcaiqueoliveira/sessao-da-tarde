package lib.kanda.sessaodatarde

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit

fun <T> pollerRetry(judge: Judge, timerScheduler: Scheduler) =
        ObservableTransformer<T, T> { upstream ->
            upstream.retryWhen { errorObs ->
                data class Step(val index: Int, val throwable: Throwable, val status: RETRY_STATUS)

                val range = Observable.range(0, judge.maxRetries + 1)

                errorObs
                        .zipWith(range, BiFunction<Throwable, Int, Step> { throwable, index ->
                            val status = judge.checkIfRetryIsNeeded(index, throwable)
                            Step(index, throwable, status)
                        })
                        .flatMap { step ->
                            when (step.status) {
                                RETRY_STATUS.NEED_RETRY ->
                                    Observable.timer(
                                            judge.timeToRetry(step.index),
                                            TimeUnit.MILLISECONDS,
                                            timerScheduler)
                                RETRY_STATUS.DONT_NEED_RETRY ->
                                    Observable.error(step.throwable)
                            }
                        }
            }
        }
