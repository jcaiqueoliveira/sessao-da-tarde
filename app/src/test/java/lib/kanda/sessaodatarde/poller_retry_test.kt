package lib.kanda.sessaodatarde

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

private val throwExceptionToRetry = { throw PollingException.Polling }
private val TIME_TO_WAIT = 1000L

private fun linearJudge(retries: Int) = object : Judge(retries) {
    override fun timeToRetry(retryCount: Retry): Long = TIME_TO_WAIT
    override fun checkIfRetryIsNeeded(retryCount: Retry, err: Throwable): RETRY_STATUS {
        return when (super.isRetryNeeded(retryCount, err)) {
            true -> RETRY_STATUS.NEED_RETRY
            false -> RETRY_STATUS.DONT_NEED_RETRY
        }
    }
}

private fun <T> defaultPollerRetry(retries: Int = 1, scheduler: Scheduler) =
        pollerRetry<T>(linearJudge(retries), scheduler)

private fun eventsSource(events: List<() -> Int>): Observable<Int> {
    var mutEvents = events
    return Observable
            .fromCallable {
                val event = mutEvents.first()
                mutEvents = mutEvents.drop(1)
                event.invoke()
            }
            .logLeStream("source")
}

@RunWith(JUnitPlatform::class)
class PollerRetryTest : Spek({
    describe("A stream with one event composed with a `pollerRetry` transformer") {
        val scheduler = Schedulers.trampoline()
        val events = listOf({ 5 })
        val stream = {
            eventsSource(events)
                    .compose(defaultPollerRetry(1, scheduler))
        }

        it("should complete successfully") {
            val ts = stream().test()
            ts.assertComplete()
        }

        it("should emit the original event") {
            val ts = stream().test()
            assert.that(ts.events[0] as List<Int>, equalTo(listOf(5)))
        }
    }

    describe("A stream with one error and an event composed with a `pollerRetry` transformer with 1 retry") {
        val scheduler = TestScheduler()
        val events = listOf(throwExceptionToRetry, { 5 })
        val stream = {
            eventsSource(events)
                    .compose(defaultPollerRetry(1, scheduler))
        }

        it("should complete successfully after 1 second") {
            val ts = stream().test()

            scheduler.advanceTimeBy(TIME_TO_WAIT, TimeUnit.MILLISECONDS)

            ts.assertComplete()
        }

        it("should emit the original event after 1 second") {
            val ts = stream().test()

            scheduler.advanceTimeBy(TIME_TO_WAIT, TimeUnit.MILLISECONDS)

            assert.that(ts.events[0] as List<Int>, equalTo(listOf(5)))
        }
    }

    describe("A stream with two errors and an event composed with a `pollerRetry` transformer with 1 retry") {
        val scheduler = TestScheduler()
        val events = listOf(throwExceptionToRetry, throwExceptionToRetry, { 5 })
        val stream = {
            eventsSource(events)
                    .compose(defaultPollerRetry(1, scheduler))
        }

        it("should complete with error after 1 second") {
            val ts = stream().test()

            scheduler.advanceTimeBy(TIME_TO_WAIT + 1000, TimeUnit.MILLISECONDS)

            ts.assertError(PollingException.Polling::class.java)
        }

        it("should not complete with error after 500 milliseconds") {
            val ts = stream().test()

            scheduler.advanceTimeBy(TIME_TO_WAIT / 2, TimeUnit.MILLISECONDS)

            ts.assertNotTerminated()
        }
    }

    describe("A stream with two errors and an event composed with a `pollerRetry` transformer with 2 retry") {
        val scheduler = TestScheduler()
        val events = listOf(throwExceptionToRetry, throwExceptionToRetry, { 5 })
        val stream = {
            eventsSource(events)
                    .compose(defaultPollerRetry(2, scheduler))
        }

        it("should complete successfully after 2 seconds") {
            val ts = stream().test()

            scheduler.advanceTimeBy(TIME_TO_WAIT * 2, TimeUnit.MILLISECONDS)

            ts.assertComplete()
        }

        it("should emit the original item after 2 seconds") {
            val ts = stream().test()

            scheduler.advanceTimeBy(TIME_TO_WAIT * 2, TimeUnit.MILLISECONDS)

            assert.that(ts.events[0] as List<Int>, equalTo(listOf(5)))
        }

        it("should not complete with error after 1 second") {
            val ts = stream().test()

            scheduler.advanceTimeBy(TIME_TO_WAIT, TimeUnit.MILLISECONDS)

            ts.assertNotTerminated()
        }
    }

    describe("A root observable transformed into a stream with an error and an event") {
        val scheduler = TestScheduler()
        val events = listOf(throwExceptionToRetry, { 5 })
        val stream = {
            eventsSource(events)
        }

        it("should resubscribe on a retry") {
            var rootSubscribed = 0
            val ts = Observable
                    .create<String> { emitter ->
                        emitter.onNext("Hello")
                    }
                    .logLeStream("root")
                    .doOnSubscribe { rootSubscribed++ }
                    .flatMap { stream() }
                    .compose(defaultPollerRetry(1, scheduler))
                    .test()

            scheduler.advanceTimeBy(TIME_TO_WAIT*2, TimeUnit.MILLISECONDS)

            ts.assertComplete()
            assert.that(rootSubscribed, equalTo(2))
        }

        it("should not resubscribe on retry if the retry is composed to a defered observable") {
            var rootSubscribed = 0
            val ts = Observable
                    .fromCallable { "Hello" }
                    .doOnSubscribe { rootSubscribed++ }
                    .concatMap {
                        Observable
                                .defer {
                                    eventsSource(events)
                                            .compose(defaultPollerRetry(1, scheduler))
                                }
                    }
                    .test()

            scheduler.advanceTimeBy(TIME_TO_WAIT, TimeUnit.MILLISECONDS)

            ts.assertComplete()
            assert.that(rootSubscribed, equalTo(1))
        }
    }
})

private fun <T> Observable<T>.logLeStream(tag: String) =
        this
                .doOnNext { println("$tag, onNext: $it") }
                .doOnError { println("$tag, onError: $it") }
                .doOnSubscribe { println("$tag, onSubscribe") }
                .doOnComplete { println("$tag, onComplete") }
