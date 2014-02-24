package com.squarespace.template;

import java.util.List;


public interface ValidatedTemplate {

  public List<ErrorInfo> getErrors();
  
  public CodeList getCode();
  
  public CodeStats getStats();
  
}
