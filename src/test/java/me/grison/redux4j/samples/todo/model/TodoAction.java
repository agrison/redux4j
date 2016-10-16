package me.grison.redux4j.samples.todo.model;

/**
 * @author Alexandre Grison (a.grison@gmail.com)
 */
public class TodoAction {
	public static final String ADD = "ADD";
	public static final String TOGGLE = "TOGGLE";
	public static final String REMOVE = "REMOVE";
	public static final String CHANGE_DISPLAY = "DISPLAY";

	public String type;
	public Object payload;

	public TodoAction(String type, Object payload) {
		this.type = type;
		this.payload = payload;
	}

	public static TodoAction of(String type, Object payload) {
		return new TodoAction(type, payload);
	}
}
