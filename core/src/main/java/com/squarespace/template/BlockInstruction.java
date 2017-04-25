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
public interface BlockInstruction extends Instruction {

  /**
   * Returns the consequent block.
   */
  Block getConsequent();

  /**
   * Set the instruction to execute instead of the consequents.
   *
   * For non-conditional blocks, like section, the alternate is simply the END
   * instruction, indicating a complete parse.
   */
  void setAlternative(Instruction inst);

  /**
   * Sets the alternative instruction.
   */
  Instruction getAlternative();

}
