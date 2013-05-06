package com.squarespace.template;


/**
 * Interface for classes which accept lists of instructions.
 */
public interface CodeSink {

  public void accept(Instruction ... instructions) throws CodeSyntaxException;
  
}
