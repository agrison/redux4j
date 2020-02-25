package me.grison.redux4j;

import io.vavr.Tuple;
import me.grison.redux4j.model.Foo;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
        var initialState = io.vavr.collection.HashMap.of("s", "foo", "i", 1, "l", new ArrayList<String>());
        Store<Map<String, Object>, String> store = Redux.createStore(initialState.toJavaMap(), reducers);

        Assert.assertThat(store.getState().get("s"), CoreMatchers.equalTo("foo"));
        store.dispatch("CONCAT");
        Assert.assertThat(store.getState().get("s"), CoreMatchers.equalTo("foobar"));

        Assert.assertThat(store.getState().get("i"), CoreMatchers.equalTo(1));
        store.dispatch("PLUS");
        Assert.assertThat(store.getState().get("i"), CoreMatchers.equalTo(3));
        store.dispatch("PLUS");
        Assert.assertThat(store.getState().get("i"), CoreMatchers.equalTo(5));

        store.dispatch("CONCAT");
        Assert.assertThat(store.getState().get("s"), CoreMatchers.equalTo("foobarbar"));

        Assert.assertThat(store.getState().get("l"), CoreMatchers.equalTo(Arrays.asList()));
        store.dispatch("ADD");
        Assert.assertThat(store.getState().get("l"), CoreMatchers.equalTo(Arrays.asList("foo")));
        store.dispatch("ADD");
        Assert.assertThat(store.getState().get("l"), CoreMatchers.equalTo(Arrays.asList("foo", "foo")));
    }

    @Test
    public void testCombineReducersBean() {
        Store<Foo, String> store = Redux.createStore(new Foo(), reducers);

        Assert.assertThat(store.getState().getS(), CoreMatchers.equalTo("foo"));
        store.dispatch("CONCAT");
        Assert.assertThat(store.getState().getS(), CoreMatchers.equalTo("foobar"));

        Assert.assertThat(store.getState().getI(), CoreMatchers.equalTo(1));
        store.dispatch("PLUS");
        Assert.assertThat(store.getState().getI(), CoreMatchers.equalTo(3));
        store.dispatch("PLUS");
        Assert.assertThat(store.getState().getI(), CoreMatchers.equalTo(5));

        store.dispatch("CONCAT");
        Assert.assertThat(store.getState().getS(), CoreMatchers.equalTo("foobarbar"));

        Assert.assertThat(store.getState().getL(), CoreMatchers.equalTo(Arrays.asList()));
        store.dispatch("ADD");
        Assert.assertThat(store.getState().getL(), CoreMatchers.equalTo(Arrays.asList("foo")));
        store.dispatch("ADD");
        Assert.assertThat(store.getState().getL(), CoreMatchers.equalTo(Arrays.asList("foo", "foo")));
    }
}
