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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


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
    ObjectNode obj = JsonUtils.createObjectNode();
    obj.put("instructions", arrayJson(instructions));
    obj.put("predicates", arrayJson(predicates));
    obj.put("formatters", arrayJson(formatters));
    return obj;
  }
  
  private ArrayNode arrayJson(String[] array) {
    ArrayNode node = JsonUtils.createArrayNode();
    for (String elem : array) {
      node.add(elem);
    }
    return node;
  }
}
