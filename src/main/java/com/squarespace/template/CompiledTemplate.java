package com.squarespace.template;

import java.util.List;


public interface CompiledTemplate {

  public CodeMachine getMachine();
  
  public List<ErrorInfo> getErrors();
  
  public Instruction getCode();
  
}
