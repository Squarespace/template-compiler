package com.squarespace.template;

import java.util.ArrayList;
import java.util.List;


/**
 * A Block treats a sequence of instructions as a unit.  The underlying list
 * is initialized lazily on the first call to add().
 */
class Block {

  private List<Instruction> instructions;

  private int initialSize;
  
  public Block(int initialSize) {
    this.initialSize = initialSize;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof Block)) {
      return false;
    }
    List<Instruction> other = ((Block)obj).instructions;
    return (instructions == null) ? (other == null) : instructions.equals(other);
  }
  
  public void add(Instruction ... instrs) {
    if (instructions == null) {
      instructions = new ArrayList<>(initialSize);
    }
    for (Instruction inst : instrs) {
      instructions.add(inst);
    }
  }
  
  public List<Instruction> getInstructions() {
    return instructions;
  }
  
}
