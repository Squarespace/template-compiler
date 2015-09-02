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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squarespace.template.Instructions.AlternatesWithInst;
import com.squarespace.template.Instructions.IfInst;
import com.squarespace.template.Instructions.IfPredicateInst;
import com.squarespace.template.Instructions.PredicateInst;
import com.squarespace.template.Instructions.RepeatedInst;
import com.squarespace.template.Instructions.RootInst;
import com.squarespace.template.Instructions.SectionInst;
import com.squarespace.template.Instructions.TextInst;
import com.squarespace.template.Instructions.VariableInst;


/**
 * Scans an instruction tree recursively, collecting counts of the number of
 * instructions, predicates and formatters, as well as the tree of variable
 * references.
 */
public class ReferenceScanner {

  private final References refs = new References();

  public References references() {
    return refs;
  }

  /**
   * Extracts reference metrics from a single instruction.
   */
  public void extract(Instruction inst) {
    String name = null;
    if (inst == null) {
      return;
    }
    if (!(inst instanceof RootInst)) {
      refs.increment(inst);
    }
    switch (inst.getType()) {

      case TEXT:
        refs.textBytes += ((TextInst)inst).getView().length();
        break;

      case ALTERNATES_WITH:
        AlternatesWithInst alternatesWith = (AlternatesWithInst)inst;
        extractBlock(alternatesWith.getConsequent());
        extract(alternatesWith.getAlternative());
        break;

      case IF:
      {
        BlockInstruction blockInst = (BlockInstruction)inst;
        refs.addIfInstruction(blockInst);
        if (inst instanceof IfInst) {
          IfInst ifInst = (IfInst)inst;
          for (Object[] var : ifInst.getVariables()) {
            name = ReprEmitter.get(var);
            refs.addVariable(name);
          }
        } else {
          IfPredicateInst ifInst = (IfPredicateInst)inst;
          refs.increment(ifInst.getPredicate());
        }
        extractBlock(blockInst.getConsequent());
        extract(blockInst.getAlternative());
        break;
      }

      case OR_PREDICATE:
      case PREDICATE:
        PredicateInst predicateInst = (PredicateInst)inst;
        Predicate predicate = predicateInst.getPredicate();
        if (predicate != null) {
          refs.increment(predicate);

          List<String> varRefs = getVariableNames(predicateInst.getArguments());
          for (String varRef : varRefs) {
            refs.addVariable(varRef);
          }
        }

        extractBlock(predicateInst.getConsequent());
        extract(predicateInst.getAlternative());
        break;

      case REPEATED:
        RepeatedInst repeated = (RepeatedInst)inst;
        name = ReprEmitter.get(repeated.getVariable());
        refs.pushSection(name);
        extractBlock(repeated.getConsequent());
        extract(repeated.getAlternative());
        extract(repeated.getAlternatesWith());
        refs.popSection();
        break;

      case ROOT:
        extractBlock(((RootInst)inst).getConsequent());
        break;

      case SECTION:
        SectionInst section = (SectionInst)inst;
        name = ReprEmitter.get(section.getVariable());
        refs.pushSection(name);
        extractBlock(section.getConsequent());
        extract(section.getAlternative());
        refs.popSection();
        break;

      case VARIABLE:
        VariableInst variable = (VariableInst)inst;
        name = ReprEmitter.get(variable.getVariable());
        refs.addVariable(name);

        for (FormatterCall call : variable.getFormatters()) {
          refs.increment(call.getFormatter());
        }
        break;

      default:
        break;

    }
  }

  @SuppressWarnings("unchecked")
  private List<String> getVariableNames(Arguments args) {
    List<String> names = new ArrayList<>();
    List<Object> parsed = (List<Object>) args.getOpaque();
    for (Object arg : parsed) {
      if (arg instanceof VariableRef) {
        String name = ReprEmitter.get(((VariableRef)arg).reference());
        names.add(name);
      }
    }
    return names;
  }

  /**
   * Iterates over all instructions in the block, extracting metrics from each.
   */
  private void extractBlock(Block block) {
    if (block != null) {
      List<Instruction> instructions = block.getInstructions();
      if (instructions != null) {
        for (Instruction inst : instructions) {
          extract(inst);
        }
      }
    }
  }

  /**
   * Holds a set of metrics on all instructions referenced in a template.
   */
  public static class References {

    private Deque<ObjectNode> variables = new ArrayDeque<>();

    private ObjectNode currentNode = JsonUtils.createObjectNode();

    private Map<String, Integer> instructions = new HashMap<>();

    private Map<String, Integer> formatters = new HashMap<>();

    private Map<String, Integer> predicates = new HashMap<>();

    private List<String> ifVariants = new ArrayList<>();

    private int textBytes;

    /**
     * Renders a JSON report containing the collected metrics.
     */
    public ObjectNode report() {
      ObjectNode res = JsonUtils.createObjectNode();
      res.put("instructions", convert(instructions));
      res.put("formatters", convert(formatters));
      res.put("predicates", convert(predicates));
      res.put("variables", currentNode);
      res.put("textBytes", textBytes);
      // NOTE: this is temporary - phensley
      res.put("ifInstructions", convert(ifVariants));
      return res;
    }

    private ObjectNode convert(Map<String, Integer> map) {
      ObjectNode res = JsonUtils.createObjectNode();
      for (Entry<String, Integer> entry : map.entrySet()) {
        res.put(entry.getKey(), entry.getValue());
      }
      return res;
    }

    private ArrayNode convert(List<String> list) {
      ArrayNode res = JsonUtils.createArrayNode();
      for (String elem : list) {
        res.add(elem);
      }
      return res;
    }

    private void increment(Instruction inst) {
      if (inst != null) {
        increment(instructions, inst.getType().name());
      }
    }

    private void increment(Predicate predicate) {
      if (predicate != null) {
        increment(predicates, predicate.identifier());
      }
    }

    private void increment(Formatter formatter) {
      if (formatter != null) {
        increment(formatters, formatter.identifier());
      }
    }

    private void increment(Map<String, Integer> counter, String key) {
      Integer value = counter.get(key);
      if (value == null) {
        value = 0;
      }
      counter.put(key, value + 1);
    }

    /**
     * Adds a variable to the current scope.
     */
    private void addVariable(String name) {
      JsonNode node = currentNode.path(name);
      if (node.isMissingNode()) {
        currentNode.put(name, NullNode.getInstance());
      }
    }

    // NOTE: This is temporary, to assess the .if instruction variants out there and
    // assess the impact of migration. Will remove this once data is gathered. - phensley
    private void addIfInstruction(BlockInstruction inst) {
      StringBuilder buf = new StringBuilder();
      if (inst instanceof IfInst) {
        ReprEmitter.emit((IfInst)inst, buf, false);
      } else {
        ReprEmitter.emit((IfPredicateInst)inst, buf, false);
      }
      ifVariants.add(buf.toString());
    }

    /**
     * Pushes one variable scope level.
     */
    private void pushSection(String name) {
      JsonNode node = currentNode.path(name);
      ObjectNode obj = null;
      if (node.isObject()) {
        obj = (ObjectNode)node;
      } else {
        obj = JsonUtils.createObjectNode();
        currentNode.put(name, obj);
      }
      variables.push(currentNode);
      currentNode = obj;
    }

    /**
     * Pops one variable scope level.
     */
    private void popSection() {
      currentNode = variables.pop();
    }

  }

}
