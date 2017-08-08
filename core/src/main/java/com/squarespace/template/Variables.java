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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;


/**
 * Holds a list of Variable objects to pass to a formatter. Always holds
 * at least 1 variable, so calls to first() always work.
 */
public class Variables {

  private final List<Variable> variables;

  public Variables(String name) {
    this.variables = new ArrayList<>(1);
    this.variables.add(new Variable(name));
  }

  public Variables(String name, JsonNode value) {
    this.variables = new ArrayList<>(1);
    this.variables.add(new Variable(name, value));
  }

  public Variable first() {
    return variables.get(0);
  }

  public int count() {
    return variables.size();
  }

  public void add(String name) {
    this.variables.add(new Variable(name));
  }

  public Variable get(int index) {
    return index < variables.size() ? variables.get(index) : null;
  }

  public void resolve(Context ctx) {
    int count = variables.size();
    for (int i = 0; i < count; i++) {
      variables.get(i).resolve(ctx);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Variables) {
      Variables other = (Variables) obj;
      return Objects.equals(variables, other.variables);
    }
    return false;
  }

  @Override
  public int hashCode() {
    throw new UnsupportedOperationException("hashCode() not supported");
  }
}
