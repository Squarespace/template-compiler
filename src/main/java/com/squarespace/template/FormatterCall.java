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
 * Simple pair class to hold a Formatter and its arguments. Represents a call to a formatter.
 */
public class FormatterCall {

  /**
   * The formatter implementation to call.
   */
  private final Formatter impl;

  /**
   * The arguments to be passed to the formatter.
   */
  private final Arguments args;

  /**
   * Constructs a call to the given {@link Formatter} with the given {@link Arguments}.
   */
  public FormatterCall(Formatter impl, Arguments args) {
    this.impl = impl;
    this.args = args;
  }

  /**
   * Returns the formatter to be called.
   */
  public Formatter getFormatter() {
    return this.impl;
  }

  /**
   * Returns the arguments to the formatter call.
   */
  public Arguments getArguments() {
    return this.args;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof FormatterCall)) {
      return false;
    }
    FormatterCall other = (FormatterCall)obj;
    return impl.equals(other.impl) && args.equals(other.args);
  }

  @Override
  public int hashCode() {
    throw new UnsupportedOperationException("FormatterCall does not implement hashCode()");
  }

}
