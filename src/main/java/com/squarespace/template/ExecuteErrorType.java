package com.squarespace.template;

import java.util.Map;


public enum ExecuteErrorType implements ErrorType {

  APPLY_PARTIAL_SYNTAX
  ("Applying partial '%(name)s' raised an error: %(data)s"),
  
  APPLY_PARTIAL_MISSING
  ("Attempt to apply partial '%(name)s' which could not be found."),
  
  GENERAL_ERROR
  ("Default error %(name)s: %(data)s"),

  UNEXPECTED_ERROR
  ("Unexpected %(name)s when executing %(repr)s: %(data)s")
  
  ;
  
  private static final String PREFIX = "RuntimeError %(code)s at line %(line)s character %(offset)s: ";
  
  private MapFormat mapFormat;
  
  private ExecuteErrorType(String rawFormat) {
    this.mapFormat = new MapFormat(PREFIX + rawFormat, Constants.NULL_PLACEHOLDER);
  }
  
  public String format(Map<String, Object> params) {
    return mapFormat.apply(params);
  }

}
