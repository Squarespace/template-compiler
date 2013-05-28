package com.squarespace.template;

import java.util.Map;


public interface ErrorType {

  public String prefix(Map<String, Object> params);
  
  public String message(Map<String, Object> params);

}
