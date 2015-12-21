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

import java.util.ArrayList;
import java.util.List;


/**
 * A Block treats a sequence of instructions as a group.  The underlying list
 * is initialized lazily on the first call to add().
 */
class Block {

  /**
   * Initial size of the instruction list.
   */
  private final int initialSize;

  /**
   * List of instructions.
   */
  private List<Instruction> instructions;

  /**
   * Constructs a block having the given initial size.
   */
  Block(int initialSize) {
    this.initialSize = initialSize;
  }

  /**
   * Adds an instruction to the list.
   */
  public void add(Instruction inst) {
    if (instructions == null) {
      instructions = new ArrayList<>(initialSize);
    }
    instructions.add(inst);
  }

  /**
   * Returns the instruction list.
   */
  public List<Instruction> getInstructions() {
    return instructions;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Block)) {
      return false;
    }
    Block other = (Block) obj;
    return (instructions == null) ? (other.instructions == null) : instructions.equals(other.instructions);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
