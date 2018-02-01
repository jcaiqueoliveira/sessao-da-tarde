package lib.kanda.sessaodatarde

import junit.framework.Assert.*
import org.junit.Test
import retrofit2.http.GET

/**
 * Created by jcosilva on 2/1/2018.
 */

class RetrofitTest {

    @Test
    fun `should create a retrofit instance and fail with invalid url `() {
        val baseURL = "ARandomUrl"
        val factory = WebService()
        try {
            factory.createRetrofit(baseURL)
            fail()
        } catch (e: Exception) {
            assertEquals(e.message, "Illegal URL: $baseURL")
        }
    }


    @Test
    fun `should create a retrofit instance with success when url is valid `() {
        val baseURL = "http://http.cat/"
        val factory = WebService()

        val retrofit = factory.createRetrofit(baseURL)
        assertNotNull(retrofit)
    }


    @Test
    fun `should create a instance of a service`() {
        val factory = WebService().createRetrofit("http://http.cat/").create(Service::class.java)
        assertTrue(factory is Service)
    }

}

interface Service {
    @GET
    fun requestSomething()

}