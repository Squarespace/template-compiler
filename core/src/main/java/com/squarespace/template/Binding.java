/**
 * Copyright (c) 2019 SQUARESPACE, Inc.
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

public class Binding {

  private final String name;
  private final Object[] reference;

  public Binding(String name, Object[] reference) {
    this.name = name;
    this.reference = reference;
  }

  public String getName() {
    return name;
  }

  public Object[] getReference() {
    return reference;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Binding) {
      Binding other = (Binding) obj;
      return name.equals(other.name) && Arrays.equals(reference, other.reference);
    }
    return false;
  }

  @Override
  public int hashCode() {
    throw new UnsupportedOperationException("hashCode() not supported");
  }

}
