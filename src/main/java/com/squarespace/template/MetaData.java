package com.squarespace.template;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squarespace.v6.utils.JSONUtils;


/**
 * Convenience method to store all symbols currently enabled in the given tables.
 */
public class MetaData {

  private final String[] instructions;
  
  private final String[] formatters;
  
  private final String[] predicates;

  public MetaData(FormatterTable formatterTable, PredicateTable predicateTable) {
    this.formatters = formatterTable.getSymbols();
    this.predicates = predicateTable.getSymbols();
    this.instructions = InstructionTable.getSymbols();
  }
  
  public JsonNode getJson() {
    ObjectNode obj = JSONUtils.createObjectNode();
    obj.put("instructions", arrayJson(instructions));
    obj.put("predicates", arrayJson(predicates));
    obj.put("formatters", arrayJson(formatters));
    return obj;
  }
  
  private ArrayNode arrayJson(String[] array) {
    ArrayNode node = JSONUtils.createArrayNode();
    for (String elem : array) {
      node.add(elem);
    }
    return node;
  }
}
