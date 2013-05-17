package com.squarespace.template;

import java.util.HashMap;
import java.util.Map;

import com.squarespace.template.Instructions.FormatterInst;
import com.squarespace.template.Instructions.IfPredicateInst;
import com.squarespace.template.Instructions.PredicateInst;


/**
 * Examines sequences of instructions and gathers some statistics.
 */
public class CodeStats implements CodeSink {

  private int totalInstructions;
  
  private Counter<InstructionType> instructionCounter;

  private Counter<String> formatterCounter;
  
  private Counter<String> predicateCounter;

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
          
        case FORMATTER:
          formatterCounter.increment(((FormatterInst)inst).getFormatter().getIdentifier());
          break;
          
        default:
          break;
      }
    }
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
