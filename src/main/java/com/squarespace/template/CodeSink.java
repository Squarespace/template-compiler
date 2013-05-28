package com.squarespace.template;


/**
 * Interface for classes which accept lists of instructions.
 */
public interface CodeSink {

  public void accept(Instruction ... instructions) throws CodeSyntaxException;
  
  /** 
   * Indicates end of instruction stream. All instructions have been fed, so the sink can now
   * perform post-processing.
   */
  public void complete();

}
