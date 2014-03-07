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
 * A Block treats a sequence of instructions as a unit.  The underlying list
 * is initialized lazily on the first call to add().
 */
class Block {

  private final int initialSize;

  private List<Instruction> instructions;
  
  public Block(int initialSize) {
    this.initialSize = initialSize;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Block)) {
      return false;
    }
    Block other = (Block) obj;
    return (instructions == null) ? (other.instructions == null) : instructions.equals(other.instructions);
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
