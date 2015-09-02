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
 * Default base class for Predicates.
 */
public abstract class BasePredicate extends Plugin implements Predicate {

  public BasePredicate(String identifier, boolean requiresArgs) {
    super(validateIdentifier(identifier), requiresArgs);
  }

  private static String validateIdentifier(String identifier) {
    if (!identifier.endsWith("?")) {
      throw new IllegalArgumentException("All predicates must end with '?'");
    }
    return identifier;
  }

  /**
   * Applies the Predicate to the context, using the given arguments which have
   * been validated and optionally converted by validateArgs(). Predicates do
   * nothing but return a boolean value -- they do not append output to the Context.
   *
   * TODO: actively prevent Predicates from appending output to Context?
   */
  public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
    return true;
  }

}
