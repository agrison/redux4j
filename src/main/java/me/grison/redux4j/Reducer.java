package me.grison.redux4j;

import javaslang.Function2;

/**
 * This represents a Reducer which is a function taking an action and a state and then return
 * the updated state.
 *
 * @author Alexandre Grison (a.grison@gmail.com)
 */
@FunctionalInterface
public interface Reducer<Action, State> extends Function2<Action, State, State> {
}
