package lib.kanda.sessaodatarde.util

import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.GET

/**
 * Created by jcosilva on 2/1/2018.
 * This file contains some class useful to write tests
 */

interface Service {
    @GET
    fun requestSomething(): Observable<Response<String>>
}

class AModel(val name1: String, val name2: String)