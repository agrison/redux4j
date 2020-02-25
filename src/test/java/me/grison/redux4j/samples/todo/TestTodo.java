package me.grison.redux4j.samples.todo;

import me.grison.redux4j.Reducer;
import me.grison.redux4j.Store;
import me.grison.redux4j.samples.todo.model.Todo;
import me.grison.redux4j.samples.todo.model.TodoAction;
import me.grison.redux4j.samples.todo.model.Todos;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import static me.grison.redux4j.samples.todo.model.TodoAction.*;

/**
 * @author Alexandre Grison (a.grison@gmail.com)
 */
public class TestTodo {

    final Reducer<TodoAction, Todos> reducer =
            (action, state) -> switch (action.type) {
                case ADD -> state.addTodo((String) action.payload);
                case REMOVE -> state.removeTodo((int) action.payload);
                case TOGGLE -> state.toggle((int) action.payload);
                case CHANGE_DISPLAY -> state.changeDisplay((String) action.payload);
                default -> throw new IllegalArgumentException("Invalid Action Type: " + action.type);
            };

    @Test
    public void testTodo() {
        var store = Store.create(new Todos(), reducer);
        store.dispatch(TodoAction.of(ADD, "foo"));
        Assert.assertThat(numTodos(store), CoreMatchers.equalTo(1));
        Assert.assertThat(todo(store, 0).todo, CoreMatchers.equalTo("foo"));

        store.dispatch(TodoAction.of(ADD, "bar"));
        Assert.assertThat(numTodos(store), CoreMatchers.equalTo(2));
        Assert.assertThat(todo(store, 1).todo, CoreMatchers.equalTo("bar"));

        store.dispatch(TodoAction.of(ADD, "bazz"));
        Assert.assertThat(numTodos(store), CoreMatchers.equalTo(3));
        Assert.assertThat(todo(store, 2).todo, CoreMatchers.equalTo("bazz"));

        Assert.assertThat(numDisplays(store), CoreMatchers.equalTo(3));

        store.dispatch(TodoAction.of(REMOVE, 1));
        Assert.assertThat(numTodos(store), CoreMatchers.equalTo(2));
        Assert.assertThat(numDisplays(store), CoreMatchers.equalTo(2));

        store.dispatch(TodoAction.of(TOGGLE, 0));
        Assert.assertThat(numTodos(store), CoreMatchers.equalTo(2));
        Assert.assertThat(numDisplays(store), CoreMatchers.equalTo(2));
        Assert.assertThat(todo(store, 0).done, CoreMatchers.equalTo(true));

        store.dispatch(TodoAction.of(CHANGE_DISPLAY, Todos.DISPLAY_COMPLETED));
        Assert.assertThat(numTodos(store), CoreMatchers.equalTo(2));
        Assert.assertThat(numDisplays(store), CoreMatchers.equalTo(1));
        Assert.assertThat(display(store, 0).todo, CoreMatchers.equalTo("foo"));
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

