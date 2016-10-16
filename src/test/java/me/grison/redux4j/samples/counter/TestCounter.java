package me.grison.redux4j.samples.counter;

import javaslang.collection.Stream;
import me.grison.redux4j.*;
import org.hamcrest.CoreMatchers;
import org.junit.*;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static javaslang.API.*;

/**
 * @author Alexandre Grison (a.grison@gmail.com)
 */
public class TestCounter {
	static final String INC = "INCREMENT";
	static final String DEC = "DECREMENT";

	final Reducer<String, Integer> reducer = (action, state) -> {
		return state + Match(action).of(
			Case($(INC), 1),
			Case($(DEC), -1),
			Case($(), 0)
		);
	};

	final AtomicInteger consumerCount = new AtomicInteger(0);
	final Consumer<Integer> consumer = (state) -> consumerCount.incrementAndGet();
	final Middleware<Integer, String> m1 = (store, action, next) -> {
		System.out.println("START MIDDLEWARE 1");
		next.accept(store, action, null);
		System.out.println("END MIDDLEWARE 1");
	};
	final Middleware<Integer, String> m2 = (store, action, next) -> {
		System.out.println("START MIDDLEWARE 2");
		next.accept(store, action, null);
		System.out.println("END MIDDLEWARE 2");
	};


	@Test
	public void testCounter() {
		// Store
		Integer counter = 0;
		Store<Integer, String> store = Store.create(counter, reducer);//, m1, m2);

		// initial state
		Assert.assertThat(store.getState(), CoreMatchers.equalTo(0));

		// simple inc
		store.dispatch(INC);
		Assert.assertThat(store.getState(), CoreMatchers.equalTo(1));

		// multiple inc
		Stream.of(1, 2, 3).forEach(e -> store.dispatch(INC));
		Assert.assertThat(store.getState(), CoreMatchers.equalTo(4));

		// multiple dec
		Stream.of(1, 2, 3, 4, 5).forEach(e -> store.dispatch(DEC));
		Assert.assertThat(store.getState(), CoreMatchers.equalTo(-1));

		// consumers
		store.subscribe(consumer);
		Stream.of(1, 2, 3, 4, 5, 6, 7).forEach(e -> store.dispatch(INC));
		Assert.assertThat(store.getState(), CoreMatchers.equalTo(6));
		Assert.assertThat(consumerCount.intValue(), CoreMatchers.equalTo(7));

		store.unsubscribe(consumer);
		Stream.of(1, 2, 3).forEach(e -> store.dispatch(DEC));
		Assert.assertThat(store.getState(), CoreMatchers.equalTo(3));
		Assert.assertThat(consumerCount.intValue(), CoreMatchers.equalTo(7)); // not changed
	}
}
