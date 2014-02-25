package com.squarespace.template;

import java.util.ArrayList;
import java.util.List;


/**
 * A CodeSink that collects instructions in a list.
 */
public class CodeList implements CodeSink {

  private List<Instruction> instList = new ArrayList<>();
  
  public List<Instruction> getInstructions() {
    return instList;
  }

  public void accept(Instruction ... instructions) throws CodeSyntaxException {
    for (Instruction inst : instructions) {
      instList.add(inst);
    }
  }
  
  @Override
  public void complete() {
    // NOOP
  }
  
}
