package lib.kanda.sessaodatarde

import android.util.Log
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import java.util.concurrent.TimeUnit


typealias Retry = Int

/**
 * Created by jcosilva on 1/24/2018.
 * Este transformer deve receber um estado de Pooling e executar um número de tentativas que podem ser definidas no tra
 * Para o desenvolvimento da solução foi pesquisado sobre backOff Rx
 **/
class Transformer1(val retries: Retry = 4) : ObservableTransformer<Any, Any> {
    override fun apply(upstream: Observable<Any>): ObservableSource<Any> {
        return upstream.retryWhen { errors ->
            errors.scan(0, { errorCount, err ->
                if (errorCount > retries || err != PoolingException.Pooling) {
                    throw err
                }
                (errorCount + 1)
            }).flatMap { retryCount ->
//                val mathPow = Math.pow(4.toDouble(), retryCount.toDouble())
                Observable.interval(1, TimeUnit.SECONDS)
            }
        }
    }
}

