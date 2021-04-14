package me.grison.redux4j;

import io.vavr.Tuple;
import io.vavr.collection.HashMap;
import me.grison.redux4j.model.Foo;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.*;

/**
 * @author Alexandre Grison (a.grison@gmail.com)
 */
public class ReduxTest {
    Reducer<String, String> concatBar =
            (action, state) -> "CONCAT".equals(action) ? state + "bar" : state;

    Reducer<String, Integer> plus2 =
            (action, state) -> "PLUS".equals(action) ? state + 2 : state;

    Reducer<String, List<String>> addFoo =
            (action, state) -> {
                if ("ADD".equals(action))
                    state.add("foo");
                return state;
            };

    Reducer reducers = Redux.combineReducers(Tuple.of("s", concatBar), Tuple.of("i", plus2), Tuple.of("l", addFoo));

    @Test
    public void testCombineReducersMap() {
        io.vavr.collection.Map<String, Serializable> initialState = HashMap.of("s", "foo", "i", 1, "l", new ArrayList<String>());
        Store<Map<String, Object>, String> store = Redux.createStore(initialState.toJavaMap(), reducers);

        assertThat(store.getState().get("s"), equalTo("foo"));
        store.dispatch("CONCAT");
        assertThat(store.getState().get("s"), equalTo("foobar"));

        assertThat(store.getState().get("i"), equalTo(1));
        store.dispatch("PLUS");
        assertThat(store.getState().get("i"), equalTo(3));
        store.dispatch("PLUS");
        assertThat(store.getState().get("i"), equalTo(5));

        store.dispatch("CONCAT");
        assertThat(store.getState().get("s"), equalTo("foobarbar"));

        assertThat(store.getState().get("l"), equalTo(Arrays.asList()));
        store.dispatch("ADD");
        assertThat(store.getState().get("l"), equalTo(Arrays.asList("foo")));
        store.dispatch("ADD");
        assertThat(store.getState().get("l"), equalTo(Arrays.asList("foo", "foo")));
    }

    @Test
    public void testCombineReducersBean() {
        Store<Foo, String> store = Redux.createStore(new Foo(), reducers);

        assertThat(store.getState().getS(), equalTo("foo"));
        store.dispatch("CONCAT");
        assertThat(store.getState().getS(), equalTo("foobar"));

        assertThat(store.getState().getI(), equalTo(1));
        store.dispatch("PLUS");
        assertThat(store.getState().getI(), equalTo(3));
        store.dispatch("PLUS");
        assertThat(store.getState().getI(), equalTo(5));

        store.dispatch("CONCAT");
        assertThat(store.getState().getS(), equalTo("foobarbar"));

        assertThat(store.getState().getL(), equalTo(Arrays.asList()));
        store.dispatch("ADD");
        assertThat(store.getState().getL(), equalTo(Arrays.asList("foo")));
        store.dispatch("ADD");
        assertThat(store.getState().getL(), equalTo(Arrays.asList("foo", "foo")));
    }

    // Actions
    static final String INC = "INC";
    static final String DEC = "DEC";

    // this is our reducer which increments if INC, decrement if DEC
    // and does nothing otherwise
    final Reducer<String, Integer> reducer =
            (action, state) -> state + HashMap.of(INC, 1, DEC, -1).getOrElse(action, 0);

    final Middleware<Integer, String> middleware = (store, action, next) -> {
        System.out.println("Before " + store.getState());
        next.accept(store, action, null);
        System.out.println("After " + store.getState());
    };

    public void foo() {
        Store<Integer, String> store = Redux.createStore(0, reducer, middleware);

        store.dispatch(INC);
    }
}
