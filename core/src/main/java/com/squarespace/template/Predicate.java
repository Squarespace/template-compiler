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

import com.squarespace.template.ReferenceScanner.References;


/**
 * A predicate is a function that returns either true of false.
 *
 * NOTE: Predicate instances must be stateless and thread-safe, e.g. they
 * should only ever access read-only, thread-safe shared data.  The only exception
 * to this rule are the Context and Arguments that are passed in.
 */
public interface Predicate {

  /**
   * Name used to uniquely identify this predicate. This determines
   * the syntax used to invoke the predicate.
   */
  String identifier();

  /**
   * Indicates whether the predicate requires arguments.
   */
  boolean requiresArgs();

  /**
   * Allows the predicate to declare anything (variables, etc) it references.
   * TODO: once there is a more general way of accessing parsed arguments
   * this should be removed.
   */
  void addReferences(Arguments args, References refs);

  void validateArgs(Arguments args) throws ArgumentsException;

  boolean apply(Context ctx, Arguments args) throws CodeExecuteException;

}
