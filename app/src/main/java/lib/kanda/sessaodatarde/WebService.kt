package lib.kanda.sessaodatarde

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

/**
 * Created by jcosilva on 2/1/2018.
 */

class WebService {
    fun createRetrofit(url: String): Retrofit {
        return Retrofit
                .Builder()
                .baseUrl(url)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
    }
}

