package com.squarespace.template;


public interface CodeSink {

  public void accept(Instruction ... instructions) throws CodeSyntaxException;
  
}
