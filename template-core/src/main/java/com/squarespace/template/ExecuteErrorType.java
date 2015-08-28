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

import static com.squarespace.template.Constants.NULL_PLACEHOLDER;

import java.util.Map;


/**
 * Errors that can occur during execution of a template.
 */
public enum ExecuteErrorType implements ErrorType {

  APPLY_PARTIAL_SYNTAX
  ("Applying partial '%(name)s' raised an error: %(data)s"),

  APPLY_PARTIAL_MISSING
  ("Attempt to apply partial '%(name)s' which could not be found."),

  CODE_LIMIT_REACHED
  ("A %(name)s code limit was reached %(data)s"),

  COMPILE_PARTIAL_SYNTAX
  ("Compiling partial '%(name)s' raised errors:"),

  GENERAL_ERROR
  ("Default error %(name)s: %(data)s"),

  UNEXPECTED_ERROR
  ("Unexpected %(name)s when executing %(repr)s: %(data)s");

  private static final String PREFIX = "RuntimeError %(code)s at line %(line)s character %(offset)s";

  private final MapFormat prefixFormat;

  private final MapFormat messageFormat;

  private ExecuteErrorType(String messageFormat) {
    this.prefixFormat = new MapFormat(PREFIX, NULL_PLACEHOLDER);
    this.messageFormat = new MapFormat(messageFormat, NULL_PLACEHOLDER);
  }

  public String prefix(Map<String, Object> params) {
    return this.prefixFormat.apply(params);
  }

  public String message(Map<String, Object> params) {
    return messageFormat.apply(params);
  }

}
