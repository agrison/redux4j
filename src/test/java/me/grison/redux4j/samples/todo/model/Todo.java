package me.grison.redux4j.samples.todo.model;

/**
 * @author Alexandre Grison (a.grison@gmail.com)
 */
public class Todo {
	public String todo;
	public boolean done;

	public Todo(String todo) {
		this.todo = todo;
	}

	@Override public String toString() {
		return "Todo(" + todo + ", done=" + done + ")";
	}
}
