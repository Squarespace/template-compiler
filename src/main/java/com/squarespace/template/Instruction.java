package com.squarespace.template;


interface Instruction {

  public InstructionType getType();

  public void setLineNumber(int line);
  
  public int getLineNumber();

  public void setCharOffset(int offset);
  
  public int getCharOffset();
  
  public void invoke(Context ctx) throws CodeExecuteException;

  public void repr(StringBuilder buf, boolean recurse);
  
  public void tree(StringBuilder buf, int depth);

}
