package lib.kanda.sessaodatarde

import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subscribers.TestSubscriber
import org.junit.Assert.assertEquals
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
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }


    @Before
    fun setUp() {
        testObserver = TestObserver()

        testScheduler = TestScheduler()
    }

    @Test
    fun `should emit a string and complete without error`() {
        val observable = Observable.just("").compose(Transformer1(4))
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
                .compose(Transformer1())
                .subscribe(testObserver)


        testScheduler.advanceTimeTo(1, TimeUnit.SECONDS)

        testObserver.assertError(t)

    }
}


