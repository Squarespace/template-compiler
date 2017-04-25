/**
 * Copyright (c) 2015 SQUARESPACE, Inc.
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

import com.fasterxml.jackson.databind.JsonNode;


class Frame {

  private final Frame parent;

  private final JsonNode node;

  private Map<String, JsonNode> variables;

  private Map<String, Instruction> macros;

  boolean stopResolution;

  int currentIndex;

  Frame(Frame parent, JsonNode node) {
    this.parent = parent;
    this.node = node;
    this.currentIndex = -1;
  }

  public Frame parent() {
    return parent;
  }

  public JsonNode node() {
    return node;
  }

  public void stopResolution(boolean flag) {
    this.stopResolution = flag;
  }

  public void setVar(String name, JsonNode node) {
    if (variables == null) {
      variables = new HashMap<>(4);
    }
    variables.put(name, node);
  }

  public JsonNode getVar(String name) {
    return (variables == null) ? null : variables.get(name);
  }

  public void setMacro(String name, Instruction inst) {
    if (macros == null) {
      macros = new HashMap<>(4);
    }
    macros.put(name, inst);
  }

  public Instruction getMacro(String name) {
    return (macros == null) ? null : macros.get(name);
  }

}