package me.grison.redux4j;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * This represents part of a store, regarding the subscribing/un-subscribing
 * using {@link java.util.function.Consumer} instead of {@link java.util.Observer}.
 *
 * @author Alexandre Grison (a.grison@gmail.com)
 */
public interface ConsumerSubscription<State> {
	/**
	 * Subscribe a consumer of the state.
	 *
	 * @param subscriber the subscriber.
	 * @return a UUID representing this consumer in this store.
	 */
	UUID subscribe(Consumer<State> subscriber);

	/**
	 * Un-subscribe a consumer from this store.
	 *
	 * @param subscriber the subscriber to be removed from the list of subscribers.
	 */
	void unsubscribe(Consumer<State> subscriber);
}
