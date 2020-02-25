package me.grison.redux4j.samples.todo.model;

import lombok.Value;

/**
 * @author Alexandre Grison (a.grison@gmail.com)
 */
@Value
public class TodoAction {
	public static final String ADD = "ADD";
	public static final String TOGGLE = "TOGGLE";
	public static final String REMOVE = "REMOVE";
	public static final String CHANGE_DISPLAY = "DISPLAY";

	public final String type;
	public final Object payload;

	public static TodoAction of(String type, Object payload) {
		return new TodoAction(type, payload);
	}
}
