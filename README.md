## Redux in Java

Using Java 8 functional stuff and javaslang.

## Features

* Store
* Reducer
* CombineReducers
* Middlewares

## Counter example

```java
public class Counter {
    // Actions
    static final String INC = "INC";
    static final String DEC = "DEC";

    // this is our reducer which increments if INC, decrement if DEC
    // and does nothing otherwise
    final Reducer<String, Integer> reducer = (action, state) -> {
        return state + Match(action).of(
            Case($(INC), 1),
            Case($(DEC), -1),
            Case($(), 0)
        );
    };

    public void foo() {
        // This is our store with its initial state of zero and the reducer seen above
        Store<Integer, String> store = Redux.createStore(0, reducer);

        // dispatch an INC action
        store.dispatch(INC);
        store.getState(); // 1

        // dispatch an DEC action
        store.dispatch(DEC);
        store.getState(); // 0
    }
}
```

## Middlewares

```java
public class Counter {

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
```

Outputs:

    Before 0
    After 1

## Combine Reducers

```java
pubic class Foo {
	Reducer<String, String> concatBar =
			(action, state) -> "CONCAT".equals(action) ? state + "bar" : state;

	Reducer<String, Integer> plus2 =
			(action, state) -> "PLUS".equals(action) ? state + 2 : state;

	Reducer<String, List<String>> addFoo = (action, state) -> {
					if ("ADD".equals(action))
						state.add("foo");
					return state;
				};

	Reducer reducers = Redux.combineReducers(Tuple.of("str", concatBar), Tuple.of("int", plus2), Tuple.of("list", addFoo));

	@Test
	public void testCombineReducersMap() {
		Map<String, Object> initialState = new HashMap<String, Object>() {{
			put("str", "foo");
			put("int", 1);
			put("list", new ArrayList<String>());
		}};
		Store<Map<String,Object>, String> store = Redux.createStore(initialState, reducers);

		assertThat(store.getState().get("str"), equalTo("foo"));
		store.dispatch("CONCAT");
		assertThat(store.getState().get("str"), equalTo("foobar"));

		assertThat(store.getState().get("int"), equalTo(1));
		store.dispatch("PLUS");
		assertThat(store.getState().get("int"), equalTo(3));
		store.dispatch("PLUS");
		assertThat(store.getState().get("int"), equalTo(5));

		store.dispatch("CONCAT");
		assertThat(store.getState().get("str"), equalTo("foobarbar"));

		assertThat(store.getState().get("list"), equalTo(Arrays.asList()));
		store.dispatch("ADD");
		assertThat(store.getState().get("list"), equalTo(Arrays.asList("foo")));
		store.dispatch("ADD");
		assertThat(store.getState().get("list"), equalTo(Arrays.asList("foo", "foo")));
	}

	// same with a javabean with getters/setters
}
```