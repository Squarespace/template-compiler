package com.squarespace.template;


public class Options<T> {

  private String localeName;
  private T inner;

  public Options(String localeName, T inner) {
    this.localeName = localeName;
    this.inner = inner;
  }

  public String localeName() {
    return this.localeName;
  }

  public void localeName(String name) {
    this.localeName = name;
  }

  public T inner() {
    return this.inner;
  }

  public void inner(T inner) {
    this.inner = inner;
  }
}
