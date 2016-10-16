package me.grison.redux4j.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexandre Grison (a.grison@gmail.com)
 */
public class Foo {
	String s = "foo";
	int i = 1;
	List<String> l = new ArrayList<>();

	public String getS() {
		return s;
	}

	public void setS(String s) {
		this.s = s;
	}

	public int getI() {
		return i;
	}

	public void setI(int i) {
		this.i = i;
	}

	public List<String> getL() {
		return l;
	}

	public void setL(List<String> l) {
		this.l = l;
	}

	@Override
	public String toString() { return "Foo(" + s + ", " + i + ", " + l +")"; }
}
