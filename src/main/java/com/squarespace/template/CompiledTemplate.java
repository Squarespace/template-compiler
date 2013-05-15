package com.squarespace.template;


public interface CompiledTemplate {

  public CodeMachine getMachine();
  
  public Instruction getCode();
  
}
