package lib.kanda.sessaodatarde

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


typealias Retry = Int

/**
 * Created by jcosilva on 1/24/2018.
 * Este transformer deve receber um estado de Polling e executar um número de tentativas que podem ser definidas no tra
 * Para o desenvolvimento da solução foi pesquisado sobre backOff Rx
 **/
class Transformer1(val retries: Retry = 4, val scheduler: Scheduler = Schedulers.trampoline()) : ObservableTransformer<Any, Any> {
    override fun apply(upstream: Observable<Any>): ObservableSource<Any> {
        return upstream.retryWhen { errors ->
            errors
                    .scan(-1, { errorCount, err ->
                        if (errorCount > retries || err != PollingException.Polling) {
                            throw err
                        }
                        (errorCount + 1)
                    })
                    .filter { it >= 0 }
                    .flatMap { retryCount ->
                        //                val mathPow = Math.pow(4.toDouble(), retryCount.toDouble())
                        Observable.interval(1, TimeUnit.SECONDS,scheduler).take(1)
                    }
        }
    }
}

///**
// * Transformer fornecido no exemplo da documentação do retry when
// * */
//class Transformer2 : ObservableTransformer<Any, Any> {
//    override fun apply(upstream: Observable<Any>): ObservableSource<Any> {
//        return upstream.retryWhen { attempts ->
//            attempts.zipWith(Observable.range(1, 3), BiFunction<Throwable, Int, Int> { _, i: Int -> i }).flatMap({ i: Int ->
//                println("delay retry by $i second(s)")
//                Observable.timer(i.toLong(), TimeUnit.SECONDS)
//            })
//        }
//    }
//}