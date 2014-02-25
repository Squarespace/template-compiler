package com.squarespace.template;

import static com.squarespace.template.Constants.NULL_PLACEHOLDER;

import java.util.Map;


public enum ExecuteErrorType implements ErrorType {

  APPLY_PARTIAL_SYNTAX
  ("Applying partial '%(name)s' raised an error: %(data)s"),
  
  APPLY_PARTIAL_MISSING
  ("Attempt to apply partial '%(name)s' which could not be found."),

  COMPILE_PARTIAL_SYNTAX
  ("Compiling partial '%(name)s' raised errors:"),
  
  GENERAL_ERROR
  ("Default error %(name)s: %(data)s"),

  UNEXPECTED_ERROR
  ("Unexpected %(name)s when executing %(repr)s: %(data)s")
  
  ;
  
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
