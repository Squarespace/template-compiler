/**
 * Copyright (c) 2017 SQUARESPACE, Inc.
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

import java.util.Arrays;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.TextNode;


/**
 * Holds the name of a variable and its current value.
 */
public class Variable {

  private final Object[] name;
  private JsonNode node;

  public Variable(String name) {
    this(name, Constants.MISSING_NODE);
  }

  public Variable(String name, JsonNode value) {
    this.name = GeneralUtils.splitVariable(name);
    this.node = value;
  }

  public Object[] name() {
    return name;
  }

  public JsonNode node() {
    return node;
  }

  public void set(int value) {
    this.node = new IntNode(value);
  }

  public void set(long value) {
    this.node = new LongNode(value);
  }

  public void set(double value) {
    this.node = new DoubleNode(value);
  }

  public void set(String value) {
    this.node = new TextNode(value);
  }

  public void set(StringBuilder value) {
    this.node = new TextNode(value.toString());
  }

  public void set(CharSequence value) {
    this.node = new TextNode(value.toString());
  }

  public void setMissing() {
    this.node = Constants.MISSING_NODE;
  }

  public boolean missing() {
    return this.node.isMissingNode();
  }

  public void set(JsonNode node) {
    this.node = node;
  }

  public void resolve(Context ctx) {
    this.node = ctx.resolve(name);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Variable) {
      Variable other = (Variable) obj;
      return Arrays.equals(name, other.name) && Objects.equals(node, other.node);
    }
    return false;
  }

  @Override
  public int hashCode() {
    throw new UnsupportedOperationException("hashCode() not supported");
  }
}
