/**
 * Copyright (c) 2014 SQUARESPACE, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.squarespace.template;

import java.util.Arrays;
import java.util.List;

import com.squarespace.template.Instructions.IfInst;
import com.squarespace.template.Instructions.PredicateInst;
import com.squarespace.template.Instructions.RepeatedInst;


/**
 * A BlockInstruction contains a primary list of nested instructions, called the consequent,
 * and a single alternative instruction.
 *
 * Conditional blocks execute the consequent when the condition is true, otherwise
 * they execute the alternative.  See {@link PredicateInst}, {@link IfInst}
 *
 * A {@link RepeatedInst} executes its consequent multiple times.
 */
abstract class BlockInstruction extends BaseInstruction {

  /**
   * Consequent block of instructions.
   */
  protected Block consequent;

  /**
   * Alternative instruction.
   */
  protected Instruction alternative;

  /**
   * Constructs a block instruction with the initial capacity for the consequent block.
   */
  BlockInstruction(int consequentsLen) {
    consequent = new Block(consequentsLen);
  }

  /**
   * Returns the consequent block.
   */
  public Block getConsequent() {
    return consequent;
  }

  /**
   * Set the instruction to execute instead of the consequents.
   *
   * For non-conditional blocks, like section, the alternate is simply the END
   * instruction, indicating a complete parse.
   */
  public void setAlternative(Instruction inst) {
    alternative = inst;
  }

  /**
   * Sets the alternative instruction.
   */
  public Instruction getAlternative() {
    return alternative;
  }

  /**
   * Indicates if the two lists of {@code Object[]} are equal.
   */
  protected boolean variableListEquals(List<Object[]> t1, List<Object[]> t2) {
    if (t1 == null) {
      return (t2 == null);
    }
    if (t2 != null) {
      int sz = t1.size();
      if (sz != t2.size()) {
        return false;
      }
      for (int i = 0; i < sz; i++) {
        if (!Arrays.deepEquals(t1.get(i), t2.get(i))) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  /**
   * Indicates if the current instruction's consequent and alternative are equal to that
   * of the {@code other) instruction.
   */
  protected boolean blockEquals(BlockInstruction other) {
    boolean res = (consequent == null) ? (other.consequent == null) : consequent.equals(other.consequent);
    if (!res) {
      return false;
    }
    return (alternative == null) ? (other.alternative == null) : alternative.equals(other.alternative);
  }

  /**
   * Helper equals() method to simplify null checking.
   */
  protected static boolean equals(Instruction i1, Instruction i2) {
    return (i1 == null) ? (i2 == null) : i1.equals(i2);
  }

  /**
   * Helper equals() method to simplify null checking.
   */
  protected static boolean blockEquals(Block b1, Block b2) {
    return (b1 == null) ? (b2 == null) : b1.equals(b2);
  }

}
