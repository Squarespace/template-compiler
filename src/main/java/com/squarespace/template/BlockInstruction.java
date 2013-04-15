package com.squarespace.template;

import java.util.Arrays;
import java.util.List;


/**
 * A BlockInstruction is contains a Block of instructions and an alternative.
 * This groups together the common functionality for all instructions containing
 * blocks.
 * 
 * For a conditional block (PredicateInst, IfInst) the consequent is executed
 * if the conditional is true, and the alternative instruction is executed
 * if the conditional is false.
 * 
 * A RepeatedInst would execute its consequent multiple times.
 */
abstract class BlockInstruction extends BaseInstruction {

  protected Block consequent;
  
  protected Instruction alternative;
  
  public BlockInstruction(int consequentsLen) {
    consequent = new Block(consequentsLen);
  }
  
  public Block getConsequent() {
    return consequent;
  }
  
  /**
   * Set the instruction to execute instead of the consequents.
   * 
   * For non-conditional blocks, like section, the alternate is simply the END
   * instruction, indicating a successful and complete parse of the section.
   */
  public void setAlternative(Instruction inst) {
    alternative = inst;
  }
  
  public Instruction getAlternative() {
    return alternative;
  }
  
  protected boolean variableListEquals(List<String[]> t1, List<String[]> t2) {
    if (t1 == null) {
      return (t2 == null);
    }
    if (t2 != null) {
      int sz = t1.size();
      if (sz != t2.size()) {
        return false;
      }
      for (int i = 0; i < sz; i++) {
        if (!Arrays.equals(t1.get(i), t2.get(i))) {
          return false;
        }
      }
      return true;
    }
    return false;
  }
  
  protected boolean blockEquals(BlockInstruction other) {
    boolean res = (consequent == null) ? (other.consequent == null) : consequent.equals(other.consequent);
    if (!res) {
      return false;
    }
    return (alternative == null) ? (other.alternative == null) : alternative.equals(other.alternative);
  }
  
  /** Helper equals() method to simplify null checking. */
  protected static boolean equals(Instruction i1, Instruction i2) {
    return (i1 == null) ? (i2 == null) : i1.equals(i2);
  }

  /** Helper equals() method to simplify null checking. */
  protected static boolean blockEquals(Block b1, Block b2) {
    return (b1 == null) ? (b2 == null) : b1.equals(b2);
  }

}
