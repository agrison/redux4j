package me.grison.redux4j;

/**
 * This interface defines a Middleware, which is a consumer of three arguments:
 * 1. the store itself
 * 2. the action which is to be dispatched
 * 3. the next middleware in the chain of middlewares.
 *
 * @author Alexandre Grison (a.grison@gmail.com)
 */
@FunctionalInterface
public interface Middleware<State, Action>
		extends TriConsumer<ReduxStore<State, Action>, Action, Middleware<State,Action>> {
}
