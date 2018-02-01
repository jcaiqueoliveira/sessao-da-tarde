package lib.kanda.sessaodatarde

import com.nhaarman.mockito_kotlin.mock
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subscribers.TestSubscriber
import lib.kanda.sessaodatarde.util.Service
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import retrofit2.Response

/**
 * Created by jcosilva on 2/1/2018.
 */

class TransformerRequestTest {
    lateinit var testObserver: TestObserver<Any>
    lateinit var testScheduler: TestScheduler
    lateinit var testSubscribe: TestSubscriber<Any>

    private val judge = Referee(4)

    @Before
    fun setUp() {
        testObserver = TestObserver()
        testScheduler = TestScheduler()
    }

    @Test
    fun `should receive a terminate the stream with a polling exception`() {

        val mockService = mock<Service>()
        val response = mock<Response<String>>()

        `when`(mockService.requestSomething()).thenReturn(Observable.just(response))
        `when`(response.code()).thenReturn(202)

        mockService
                .requestSomething()
                .compose(TransformerRequest())
                .subscribe(testObserver)

        testObserver.assertError(PollingException.Polling)
    }


    @Test
    fun `should receive non polling state and finish stream without error`() {

        val mockService = mock<Service>()
        val response = mock<Response<String>>()

        `when`(mockService.requestSomething()).thenReturn(Observable.just(response))
        `when`(response.code()).thenReturn(200)


        mockService
                .requestSomething()
                .compose(TransformerRequest())
                .subscribe(testObserver)

        testObserver.assertComplete()
    }

}