package me.grison.redux4j.samples.todo;

import io.vavr.collection.HashMap;
import me.grison.redux4j.Reducer;
import me.grison.redux4j.Store;
import me.grison.redux4j.samples.todo.model.Todo;
import me.grison.redux4j.samples.todo.model.TodoAction;
import me.grison.redux4j.samples.todo.model.Todos;
import org.junit.Test;

import java.util.function.Supplier;

import static me.grison.redux4j.samples.todo.model.TodoAction.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Alexandre Grison (a.grison@gmail.com)
 */
public class TestTodo {

    final Reducer<TodoAction, Todos> reducer =
            (action, state) -> HashMap.<String, Supplier<Todos>>of(ADD, () -> state.addTodo((String) action.payload),
                    REMOVE, () -> state.removeTodo((int) action.payload),
                    TOGGLE, () -> state.toggle((int) action.payload),
                    CHANGE_DISPLAY, () -> state.changeDisplay((String) action.payload))
                    .getOrElse(action.type, () -> {
                        throw new IllegalArgumentException("Invalid Action Type: " + action.type);
                    }).get();

    @Test
    public void testTodo() {
        Store<Todos, TodoAction> store = Store.create(new Todos(), reducer);
        store.dispatch(TodoAction.of(ADD, "foo"));
        assertThat(numTodos(store), equalTo(1));
        assertThat(todo(store, 0).todo, equalTo("foo"));

        store.dispatch(TodoAction.of(ADD, "bar"));
        assertThat(numTodos(store), equalTo(2));
        assertThat(todo(store, 1).todo, equalTo("bar"));

        store.dispatch(TodoAction.of(ADD, "bazz"));
        assertThat(numTodos(store), equalTo(3));
        assertThat(todo(store, 2).todo, equalTo("bazz"));

        assertThat(numDisplays(store), equalTo(3));

        store.dispatch(TodoAction.of(REMOVE, 1));
        assertThat(numTodos(store), equalTo(2));
        assertThat(numDisplays(store), equalTo(2));

        store.dispatch(TodoAction.of(TOGGLE, 0));
        assertThat(numTodos(store), equalTo(2));
        assertThat(numDisplays(store), equalTo(2));
        assertThat(todo(store, 0).done, equalTo(true));

        store.dispatch(TodoAction.of(CHANGE_DISPLAY, Todos.DISPLAY_COMPLETED));
        assertThat(numTodos(store), equalTo(2));
        assertThat(numDisplays(store), equalTo(1));
        assertThat(display(store, 0).todo, equalTo("foo"));
    }

    private static int numTodos(Store<Todos, TodoAction> store) {
        return store.getState().todos.size();
    }

    private static int numDisplays(Store<Todos, TodoAction> store) {
        return store.getState().getDisplay().size();
    }

    private static Todo todo(Store<Todos, TodoAction> store, int index) {
        return store.getState().todos.get(index);
    }

    private static Todo display(Store<Todos, TodoAction> store, int index) {
        return store.getState().getDisplay().get(index);
    }
}
