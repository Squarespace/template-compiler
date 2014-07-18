package com.squarespace.template;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squarespace.template.Instructions.AlternatesWithInst;
import com.squarespace.template.Instructions.IfInst;
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

  public void extract(Instruction inst) {
    String name = null;
    refs.increment(inst);
    switch (inst.getType()) {

      case TEXT:
        refs.textBytes += ((TextInst)inst).getView().length();
        break;

      case ALTERNATES_WITH:
        AlternatesWithInst alternatesWith = (AlternatesWithInst)inst;
        extract(alternatesWith.getConsequent());
        extract(alternatesWith.getAlternative());
        break;

      case IF:
        IfInst ifInst = (IfInst)inst;
        for (Object[] var : ifInst.getVariables()) {
          name = ReprEmitter.get(var);
          refs.addVariable(name);
        }
        extract(ifInst.getConsequent());
        extract(ifInst.getAlternative());
        break;

      case OR_PREDICATE:
      case PREDICATE:
        PredicateInst predicateInst = (PredicateInst)inst;
        Predicate predicate = predicateInst.getPredicate();
        refs.increment(predicate);

        List<String> varRefs = predicate.getVariableNames(predicateInst.getArguments());
        for (String varRef : varRefs) {
          refs.addVariable(varRef);
        }

        extract(predicateInst.getConsequent());
        extract(predicateInst.getAlternative());
        break;

      case REPEATED:
        RepeatedInst repeated = (RepeatedInst)inst;
        name = ReprEmitter.get(repeated.getVariable());
        refs.pushSection(name);
        extract(repeated.getConsequent());
        extract(repeated.getAlternative());
        extract(repeated.getAlternatesWith());
        refs.popSection();
        break;

      case ROOT:
        extract(((RootInst)inst).getConsequent());
        break;

      case SECTION:
        SectionInst section = (SectionInst)inst;
        name = ReprEmitter.get(section.getVariable());
        refs.pushSection(name);
        extract(section.getConsequent());
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

  private void extract(Block block) {
    if (block != null) {
      for (Instruction inst : block.getInstructions()) {
        extract(inst);
      }
    }
  }

  public static class References {

    private Deque<ObjectNode> variables = new ArrayDeque<>();

    private ObjectNode currentNode = JsonUtils.createObjectNode();

    private Map<String, Integer> instructions = new HashMap<>();

    private Map<String, Integer> formatters = new HashMap<>();

    private Map<String, Integer> predicates = new HashMap<>();

    private int textBytes;

    public References() {
    }

    public ObjectNode report() {
      ObjectNode res = JsonUtils.createObjectNode();
      res.put("instructions", convert(instructions));
      res.put("formatters", convert(formatters));
      res.put("predicates", convert(predicates));
      res.put("variables", variables.getLast());
      res.put("textBytes", textBytes);
      return res;
    }

    private ObjectNode convert(Map<String, Integer> map) {
      ObjectNode res = JsonUtils.createObjectNode();
      for (Entry<String, Integer> entry : map.entrySet()) {
        res.put(entry.getKey(), entry.getValue());
      }
      return res;
    }

    private void increment(Instruction inst) {
      increment(instructions, inst.getType().name());
    }

    private void increment(Predicate predicate) {
      increment(predicates, predicate.getIdentifier());
    }

    private void increment(Formatter formatter) {
      increment(formatters, formatter.getIdentifier());
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
