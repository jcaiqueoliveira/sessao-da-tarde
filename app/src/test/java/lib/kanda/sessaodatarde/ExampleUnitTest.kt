package lib.kanda.sessaodatarde

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subscribers.TestSubscriber
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    lateinit var testObserver: TestObserver<Any>
    lateinit var testScheduler: TestScheduler
    lateinit var testSubscribe: TestSubscriber<Any>

    private val judge = Referee()

    @Before
    fun setUp() {
        testObserver = TestObserver()
        testScheduler = TestScheduler()
    }

    @Test
    fun `should emit a string and complete without error`() {
        val observable = Observable.just("").compose(Transformer1(judge))
        observable.subscribe(testObserver)

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertComplete()
    }

    @Test
    fun `should emit an error different than PollingError dont retry and complete with error`() {
        val t = Throwable()
        Observable
                .error<Any>(t)
                .observeOn(testScheduler)
                .compose(Transformer1(judge))
                .subscribe(testObserver)


        testScheduler.advanceTimeTo(1, TimeUnit.SECONDS)

        testObserver.assertError(t)

    }

    @Test
    fun `should not retry when external resource replies with success`() {
        val judge = mock<Judge>()
        Observable
                .just("")
                .compose(Transformer1(judge))
                .subscribe(testObserver)

        verifyZeroInteractions(judge)
        testObserver.assertComplete()
        // Given that REST API returned 200, for instance no pooling logic will be required
    }

    @Test
    fun `should not retry when server down at first API call`() {
        // No pooling logic when 5xy at first call
        assertFalse(judge.isAnErrorToRetry(0, InternalServerError()))
    }

    @Test
    fun `should not retry when client error at first API call`() {
        assertFalse(judge.isAnErrorToRetry(0, NotFoundException()))

        // No pooling logic when 4xy at first call
    }

    @Test
    fun `should retry and succeed when server replies success at some attempt`() {
        val events = listOf({ PollingException.Polling }, { "Hello" })
        val transformer = Transformer1(judge)
        var firstEmited = false
        Observable
                .fromCallable {

                    return@fromCallable if (!firstEmited) {
                        events.first()
                    } else {
                        events.last()
                    }
                }
                .map {
                    firstEmited = true
                    it
                }
                .compose(transformer)
                .observeOn(testScheduler)
                .subscribe(testObserver)


        testObserver.awaitTerminalEvent(2000, TimeUnit.MILLISECONDS)
        testObserver.assertComplete()
        testObserver.assertNoErrors()

    }

    @Test
    fun `should retry and fails when running out all attempts`() {

    }

    @Test
    fun `when retrying, networking failures dont modify pooling state`() {
        // !! checar isso aqui
    }

    @Test
    fun `when retrying, REST failures overrides pooling and are forwarded`() {
        // se der erro de rest sair do pooling
    }

}


