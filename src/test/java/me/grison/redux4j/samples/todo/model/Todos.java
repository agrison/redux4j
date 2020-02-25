package me.grison.redux4j.samples.todo.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Alexandre Grison (a.grison@gmail.com)
 */
public class Todos {
	public static final String DISPLAY_ALL = "ALL";
	public static final String DISPLAY_COMPLETED = "COMPLETED";

	public final List<Todo> todos;
	public final String display;

	public Todos() {
		this(new ArrayList<>(), DISPLAY_ALL);
	}

	public Todos(List<Todo> todos, String display) {
		this.todos = todos;
		this.display = display;
	}

	public Todos addTodo(String todo) {
		var todos = copyTodos();
		todos.add(new Todo(todo));
		return new Todos(todos, display);
	}

	public Todos removeTodo(int index) {
		var todos = copyTodos();
		todos.remove(index);
		return new Todos(todos, display);
	}

	public Todos toggle(int index) {
		var todos = copyTodos();
		todos.get(index).done = !todos.get(index).done;
		return new Todos(todos, display);
	}

	public Todos changeDisplay(String display) {
		return new Todos(todos, display);
	}

	private List<Todo> copyTodos() {
		return new ArrayList<>(this.todos);
	}

	@Override public String toString() {
		return "Todos(display=" + display + ", todos=" + todos + ")";
	}

	public List<Todo> getDisplay() {
		return todos.stream().filter(e -> display.equals(DISPLAY_ALL) || e.done && display.equals(DISPLAY_COMPLETED))
				.collect(Collectors.toList());
	}
}
