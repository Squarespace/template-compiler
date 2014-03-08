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
 * A predicate is a function that returns either true of false.
 *
 * NOTE: Predicate instances must be stateless and thread-safe, e.g. they
 * should only ever access read-only, thread-safe shared data.  The only exception
 * to this rule are the Context and Arguments that are passed in.
 */
public interface Predicate {

  String getIdentifier();

  boolean requiresArgs();

  void validateArgs(Arguments args) throws ArgumentsException;

  boolean apply(Context ctx, Arguments args) throws CodeExecuteException;

}
