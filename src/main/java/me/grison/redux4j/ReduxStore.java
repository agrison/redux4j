package me.grison.redux4j;

import java.util.UUID;

/**
 * This represents a Store.
 *
 * @author Alexandre Grison (a.grison@gmail.com)
 */
public interface ReduxStore<State, Action> {
	/**
	 * Get the current state.
	 * @return the current state.
	 */
	State getState();

	/**
	 * Dispatch an action.
	 *
	 * @param action the action to be dispatched.
	 */
	void dispatch(Action action);

	/**
	 * Un-subscribe a consumer given its id.
	 *
	 * @param subscriberId the subscriber id.
	 */
	void unsubscribe(UUID subscriberId);
}
