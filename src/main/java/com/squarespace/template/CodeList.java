package com.squarespace.template;

import java.util.ArrayList;
import java.util.List;


/**
 * A CodeSink that collects instructions in a list.
 */
public class CodeList implements CodeSink {

  private static final Instruction[] EMPTY_INSTRUCTION_ARRAY = new Instruction[0];
  
  private List<Instruction> instList = new ArrayList<>();
  
  public List<Instruction> getInstructions() {
    return instList;
  }

  public Instruction[] getInstructionArray() {
    return instList.toArray(EMPTY_INSTRUCTION_ARRAY);
  }
  
  public void accept(Instruction ... instructions) throws CodeSyntaxException {
    for (Instruction inst : instructions) {
      instList.add(inst);
    }
  }
  
}
