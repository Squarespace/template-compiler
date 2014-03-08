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
 * Common code for Formatters and Predicates.
 */
public class Plugin {

  private final String identifier;

  private final boolean requiresArgs;

  public Plugin(String identifier, boolean requiresArgs) {
    this.identifier = identifier;
    this.requiresArgs = requiresArgs;
  }

  public String getIdentifier() {
    return identifier;
  }

  public boolean requiresArgs() {
    return requiresArgs;
  }

  /**
   * Perform all validation of arguments passed to the Plugin, and also
   * perform any necessary conversion. Store converted args as an opaque
   * object,
   *
   * @see Arguments#setOpaque(Object)
   */
  public void validateArgs(Arguments args) throws ArgumentsException {
    // NOOP
  }

  @Override
  public String toString() {
    return identifier;
  }

}
