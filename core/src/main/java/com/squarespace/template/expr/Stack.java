package com.squarespace.template.expr;

import java.util.ArrayDeque;
import java.util.Collection;

/**
 * Basic stack. Implements the same interface as that in the TypeScript version
 * of the compiler, with the push() and pop() methods corresponding to the
 * JavaScript Array type, and top() pointing to the last element of the array.
 */
public class Stack<T extends Token> {

  private final ArrayDeque<T> elems = new ArrayDeque<>();
  private int i;

  public int length() {
    return elems.size();
  }

  public T top() {
    return this.elems.peekLast();
  }

  public void top(T t) {
    if (t != null && !this.elems.isEmpty()) {
      this.elems.removeLast();
      this.elems.addLast(t);
    }
  }

  public Collection<T> elems() {
    return this.elems;
  }

  public void push(T t) {
    this.elems.addLast(t);
    this.i++;
  }

  public T pop() {
    return this.elems.isEmpty() ? null : this.elems.removeLast();
  }

  @Override
  public String toString() {
    return this.elems.toString();
  }
}
