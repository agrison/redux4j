package me.grison.redux4j.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexandre Grison (a.grison@gmail.com)
 */
@Data
public class Foo {
    String s = "foo";
    int i = 1;
    List<String> l = new ArrayList<>();
}
