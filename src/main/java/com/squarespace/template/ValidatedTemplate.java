package com.squarespace.template;

import java.util.List;


public interface ValidatedTemplate {

  public List<ErrorInfo> errors();
  
  public CodeList code();
  
  public CodeStats stats();
  
}
