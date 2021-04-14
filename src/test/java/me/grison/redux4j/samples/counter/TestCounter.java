package me.grison.redux4j.samples.counter;

import io.vavr.collection.HashMap;
import io.vavr.collection.Stream;
import io.vavr.control.Try;
import me.grison.redux4j.Middleware;
import me.grison.redux4j.Reducer;
import me.grison.redux4j.Store;
import org.hamcrest.CoreMatchers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * @author Alexandre Grison (a.grison@gmail.com)
 */
public class TestCounter {
    static final String INC = "INCREMENT";
    static final String DEC = "DECREMENT";

    final Reducer<String, Integer> reducer =
            (action, state) -> state + HashMap.of(INC, 1, DEC, -1).getOrElse(action, 0);

    final AtomicInteger consumerCount = new AtomicInteger(0);
    final Consumer<Integer> consumer = (state) -> consumerCount.incrementAndGet();

    final AtomicLong m1Start = new AtomicLong(0);
    final AtomicLong m1End = new AtomicLong(0);
    final Middleware<Integer, String> m1 = (store, action, next) -> {
        m1Start.set(System.currentTimeMillis());
        sleep(50);
        next.accept(store, action, null);
        sleep(50);
        m1End.set(System.currentTimeMillis());
    };

    final AtomicLong m2Start = new AtomicLong(0);
    final AtomicLong m2End = new AtomicLong(0);
    final Middleware<Integer, String> m2 = (store, action, next) -> {
        m2Start.set(System.currentTimeMillis());
        next.accept(store, action, null);
        m2End.set(System.currentTimeMillis());
    };


    @Test
    public void testCounter() {
        // Store
        int counter = 0;
        Store<Integer, String> store = Store.create(counter, reducer, m2, m1);

        // initial state
        assertThat(store.getState(), equalTo(0));

        // simple inc
        store.dispatch(INC);
        assertThat(store.getState(), equalTo(1));
        assertMiddlewareOrder();

        // multiple inc
        Stream.of(1, 2, 3).forEach(e -> store.dispatch(INC));
        assertThat(store.getState(), equalTo(4));
        assertMiddlewareOrder();

        // multiple dec
        Stream.of(1, 2, 3, 4, 5).forEach(e -> store.dispatch(DEC));
        assertThat(store.getState(), equalTo(-1));
        assertMiddlewareOrder();

        // consumers
        store.subscribe(consumer);
        Stream.of(1, 2, 3, 4, 5, 6, 7).forEach(e -> store.dispatch(INC));
        assertThat(store.getState(), equalTo(6));
        assertThat(consumerCount.intValue(), equalTo(7));
        assertMiddlewareOrder();

        store.unsubscribe(consumer);
        Stream.of(1, 2, 3).forEach(e -> store.dispatch(DEC));
        assertThat(store.getState(), equalTo(3));
        assertThat(consumerCount.intValue(), equalTo(7)); // not changed
        assertMiddlewareOrder();
    }

    private void assertMiddlewareOrder() {
        assertTrue(m1Start.longValue() < m2Start.longValue());
        assertTrue(m1End.longValue() > m2End.longValue());
    }

    private void sleep(long m) {
        Try.run(() -> Thread.sleep(m));
    }
}
