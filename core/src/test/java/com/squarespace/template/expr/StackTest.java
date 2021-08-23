package com.squarespace.template.expr;

import static org.testng.Assert.assertEquals;

import java.util.ArrayDeque;

import org.testng.annotations.Test;

public class StackTest {

  @Test
  public void testBasic() {
    Stack<Token> s = new Stack<>();
    s.push(new NumberToken(1.0));
    s.push(new NumberToken(2.0));
    s.push(new NumberToken(3.0));
    assertEquals(s.length(), 3);
    assertEquals(s.toString(), "[NUMBER[1.0], NUMBER[2.0], NUMBER[3.0]]");

    assertEquals(s.pop(), new NumberToken(3.0));
    assertEquals(s.length(), 2);
    assertEquals(s.elems(), deque(2.0, 1.0));

    assertEquals(s.top(), new NumberToken(2.0));
    assertEquals(s.pop(), new NumberToken(2.0));
    assertEquals(s.length(), 1);
    assertEquals(s.elems(), deque(1.0));

    s.top(null);
    s.top(new NumberToken(1.0));
    assertEquals(s.toString(), "[NUMBER[1.0]]");

    s.top(new NumberToken(4.0));
    assertEquals(s.toString(), "[NUMBER[4.0]]");

    assertEquals(s.top(), new NumberToken(4.0));
    assertEquals(s.pop(), new NumberToken(4.0));
    assertEquals(s.length(), 0);

    assertEquals(s.top(), null);
    assertEquals(s.pop(), null);
    assertEquals(s.length(), 0);

    s.top(new NumberToken(5.0));
    assertEquals(s.toString(), "[]");
    assertEquals(s.top(), null);
    assertEquals(s.pop(), null);
    assertEquals(s.length(), 0);
  }

  private static ArrayDeque<Token> deque(double ...nums) {
    ArrayDeque<Token> r = new ArrayDeque<>();
    for (double n : nums) {
      r.push(new NumberToken(n));
    }
    return r;
  }

}
