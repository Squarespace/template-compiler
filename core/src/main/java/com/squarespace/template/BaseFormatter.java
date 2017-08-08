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

/**
 * Default base class for Formatters.
 */
public abstract class BaseFormatter extends Plugin implements Formatter {

  /**
   * Constructs a formatter with the given identifier and indicates whether
   * it requires arguments.
   */
  public BaseFormatter(String identifier, boolean requiresArgs) {
    super(identifier, requiresArgs);
  }

  @Override
  public void initialize(Compiler compiler) throws CodeException {

  }

  /**
   * Applies the Formatter to the context, using the given arguments which have
   * been validated and optionally converted by validateArgs(). Formatters append
   * output to the Context.
   */
  public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {

  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof BaseFormatter) {
      // Equality of identifiers works since they are unique within a given
      // compiler's symbol table.  We don't currently care about equality of
      // 2 different instances that happen to use the same identifier outside
      // of a symbol table.
      BaseFormatter other = (BaseFormatter)obj;
      return identifier().equals(other.identifier());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return identifier().hashCode();
  }

}
