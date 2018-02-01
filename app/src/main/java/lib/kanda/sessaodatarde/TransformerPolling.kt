package lib.kanda.sessaodatarde

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


/**
 * Created by jcosilva on 1/24/2018.
 * Este transformer deve receber um estado de Polling e executar um número de tentativas
 * que podem ser definidas no tra para o desenvolvimento da solução foi pesquisado sobre backOff
 * Foi criado uma exception para representar um estado de Polling, dado que um pooling acorreu é
 * feito um numero N de retentativas, as retentativas continuam mesmo que ocorra uma exception
 * diferente de Polling
 **/

class TransformerPolling(
        val judge: Judge,
        val scheduler: Scheduler = Schedulers.computation()) : ObservableTransformer<Any, Any> {
    private val DEFAULT_VALUE = 0
    private val FIRST = 1L

    override fun apply(upstream: Observable<Any>): ObservableSource<Any> {
        return upstream.retryWhen { errors ->
            errors
                    .scan(DEFAULT_VALUE, { errorCount, err ->
                        return@scan when (judge.checkIfRetryIsNeeded(errorCount, err)) {
                            RETRY_STATUS.NEED_RETRY -> (errorCount + 1)
                            RETRY_STATUS.DONT_NEED_RETRY -> throw err
                        }
                    })
                    .filter { it > 0 }
                    .switchMap { retryCount ->
                        Observable
                                .interval(
                                        judge.timeToRetry(retryCount),
                                        TimeUnit.MILLISECONDS,
                                        scheduler
                                )
                                .take(FIRST)
                    }
        }
    }
}