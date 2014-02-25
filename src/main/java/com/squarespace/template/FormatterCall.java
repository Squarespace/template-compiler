package com.squarespace.template;


/**
 * Simple pair class to hold a Formatter and its arguments. Represents a call to a formatter.
 */
public class FormatterCall {
  
  private final Formatter impl;
  
  private final Arguments args;

  public FormatterCall(Formatter impl, Arguments args) {
    this.impl = impl;
    this.args = args;
  }
  
  public Formatter getFormatter() {
    return this.impl;
  }
  
  public Arguments getArguments() {
    return this.args;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof FormatterCall)) {
      return false;
    }
    FormatterCall other = (FormatterCall)obj;
    return impl.equals(other.impl) && args.equals(other.args);
  }

  @Override
  public int hashCode() {
    throw new UnsupportedOperationException("FormatterCall does not implement hashCode()");
  }
  
}
