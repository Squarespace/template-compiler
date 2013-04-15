package com.squarespace.template;


public class CompiledTemplate {

  private final CodeMachine machine;
  
  CompiledTemplate(CodeMachine machine) {
    this.machine = machine;
  }
  
  public CodeMachine getMachine() {
    return machine;
  }
  
  public Instruction getCode() {
    return machine.getCode();
  }
  
}
