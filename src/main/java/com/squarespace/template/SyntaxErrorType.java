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
 * Definitions of all syntax errors that can be raised during template parsing,
 * assembling.
 */
public enum SyntaxErrorType implements ErrorType {

  // NOTE: The constructor placement below is odd but is a little easier to read.
  
  DEAD_CODE_BLOCK
  ("This %(type)s block will never execute."),
  
  EOF_IN_BLOCK
  ("Reached EOF in the middle of %(data)s"),
  
  EOF_IN_COMMENT
  ("Reached EOF in the middle of a multi-line comment"),
  
  EXTRA_CHARS
  ("Extra characters found after %(type)s instruction: '%(data)s'"),

  FORMATTER_INVALID
  ("Invalid formatter name '%(name)s' found."),

  FORMATTER_NEEDS_ARGS
  ("Formatter '%(data)s' needs arguments but none were provided."),
  
  FORMATTER_UNKNOWN
  ("Formatter '%(name)s' is unknown."),
  
  FORMATTER_ARGS_INVALID
  ("Formatter '%(name)s' arguments are invalid: '%(data)s'"),

  IF_EMPTY
  ("IF instruction requires at least one variable to test."),

  IF_EXPECTED_VAROP
  ("Expected an operator or a variable, found '%(data)s'"),

  IF_TOO_MANY_OPERATORS
  ("Too many operators in IF instruction."),
  
  IF_TOO_MANY_VARS
  ("Too many variables in IF instruction. Limit is %(limit)s."),

  INVALID_INSTRUCTION
  ("Invalid instruction '%(data)s'"),
    
  MISMATCHED_END
  ("Mismatched END found at ROOT."),
  
  MISSING_WITH_KEYWORD
  ("Missing 'with' keyword, found '%(data)s'"),

  MISSING_SECTION_KEYWORD
  ("Missing 'section' keyword, found '%(data)s'"),

  NOT_ALLOWED_AT_ROOT
  ("%(type)s instruction is not allowed at the template root."),
  
  NOT_ALLOWED_IN_BLOCK
  ("%(type)s instruction is not allowed inside %(data)s block."),

  OR_EXPECTED_PREDICATE
  ("Expected a predicate to follow %(type)s, found '%(data)s'"),
  
  PREDICATE_ARGS_INVALID
  ("Predicate %(name)s arguments invalid: '%(data)s'"),
  
  PREDICATE_NEEDS_ARGS
  ("Predicate '.%(data)s' requires arguments but none were provided."),
  
  PREDICATE_UNKNOWN
  ("Predicate '%(data)s' is unknown."),

  VARIABLE_EXPECTED
  ("Variable expected, found '%(data)s'"),

  WHITESPACE_EXPECTED
  ("Whitespace expected, found '%(data)s'"),

  ;
  
  private static final String PREFIX = "SyntaxError %(code)s at line %(line)s character %(offset)s";

  private MapFormat prefixFormat;
  
  private MapFormat messageFormat;
  
  private SyntaxErrorType(String rawFormat) {
    this.prefixFormat = new MapFormat(PREFIX, NULL_PLACEHOLDER);
    this.messageFormat = new MapFormat(rawFormat, NULL_PLACEHOLDER);
  }
  
  public String prefix(Map<String, Object> params) {
    return prefixFormat.apply(params);
  }

  public String message(Map<String, Object> params) {
    return messageFormat.apply(params);
  }
 
}
