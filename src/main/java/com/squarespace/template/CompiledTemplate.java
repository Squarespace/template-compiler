package com.squarespace.template;

import java.util.List;


public interface CompiledTemplate {

  public CodeMachine machine();
  
  public List<ErrorInfo> errors();
  
  public Instruction code();
  
}
