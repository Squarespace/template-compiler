package com.squarespace.template;

import java.util.Map;


public enum TemplateType {

  BLOCK
  ("template: block type %(block)s"),

  GENERIC
  ("template: %(data)s"),
  
  NONE
  ("template: no context provided."),

  PAGE
  ("template: page %(file)s")


  ;

  private MapFormat format;
  
  private TemplateType(String rawFormat) {
    this.format = new MapFormat(rawFormat);
  }
  
  public String message(Map<String, Object> params) {
    return format.apply(params);
  }

}
