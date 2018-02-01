package lib.kanda.sessaodatarde

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import retrofit2.Response

/**
 * Created by jcosilva on 2/1/2018.
 */
class TransformerRequest : ObservableTransformer<Response<*>, Any> {
    override fun apply(upstream: Observable<Response<*>>): ObservableSource<Any> {
        return upstream.map {
            return@map when (it.code()) {
                202 -> throw PollingException.Polling
                else -> it
            }
        }
    }
}