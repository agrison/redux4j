package me.grison.redux4j;

import javaslang.Tuple;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

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
		Map<String, Object> initialState = new HashMap<String, Object>() {{
			put("s", "foo");
			put("i", 1);
			put("l", new ArrayList<String>());
		}};
		Store<Map<String,Object>, String> store = Redux.createStore(initialState, reducers);

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
		Store<me.grison.redux4j.model.Foo, String> store = Redux.createStore(new me.grison.redux4j.model.Foo(), reducers);

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
