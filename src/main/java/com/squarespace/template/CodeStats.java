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

import java.util.HashMap;
import java.util.Map;

import com.squarespace.template.Instructions.IfPredicateInst;
import com.squarespace.template.Instructions.PredicateInst;
import com.squarespace.template.Instructions.VariableInst;


/**
 * Examines sequences of instructions and gathers some statistics.
 */
public class CodeStats implements CodeSink {

  private Counter<InstructionType> instructionCounter;

  private Counter<String> formatterCounter;

  private Counter<String> predicateCounter;

  private int totalInstructions;

  public CodeStats() {
    this.instructionCounter = new Counter<>();
    this.formatterCounter = new Counter<>();
    this.predicateCounter = new Counter<>();
  }

  @Override
  public void accept(Instruction... instructions) throws CodeSyntaxException {
    for (Instruction inst : instructions) {
      totalInstructions++;
      InstructionType type = inst.getType();
      instructionCounter.increment(type);
      switch (type) {

        case PREDICATE:
        case OR_PREDICATE:
          Predicate predicate = ((PredicateInst)inst).getPredicate();
          if (predicate != null) {
            predicateCounter.increment(predicate.getIdentifier());
          }
          break;

        case IF:
          if (inst instanceof IfPredicateInst) {
            predicateCounter.increment(((IfPredicateInst)inst).getPredicate().getIdentifier());
          }
          break;

        case VARIABLE:
          for (FormatterCall formatter : ((VariableInst)inst).getFormatters()) {
            formatterCounter.increment(formatter.getFormatter().getIdentifier());
          }
          break;

        default:
          break;
      }
    }
  }

  @Override
  public void complete() {

  }

  public int getTotalInstructions() {
    return totalInstructions;
  }

  public Map<InstructionType, Integer> getInstructionCounts() {
    return instructionCounter.getMap();
  }

  public Map<String, Integer> getFormatterCounts() {
    return formatterCounter.getMap();
  }

  public Map<String, Integer> getPredicateCounts() {
    return predicateCounter.getMap();
  }


  private static class Counter<K> {

    private Map<K, Integer> map = new HashMap<>();

    public void increment(K key) {
      Integer val = map.get(key);
      if (val == null) {
        val = new Integer(1);
        map.put(key, val);
      } else {
        map.put(key, val + 1);
      }
    }

    public Map<K, Integer> getMap() {
      return map;
    }
  }
}
