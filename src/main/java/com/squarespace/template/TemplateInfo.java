package com.squarespace.template;

import java.util.Map;


public class TemplateInfo {

  private static final String BLOCK = "block";
  
  private static final String DATA = "data";
  
  private static final String FILE = "file";
  
  private static final String SOURCE = "source";
  
  private TemplateType type;
  
  private MapBuilder<String, Object> builder = new MapBuilder<>();
  
  public TemplateInfo(TemplateType type) {
    this.type = type;
  }
  
  public TemplateInfo block(Object block) {
    builder.put(BLOCK, block);
    return this;
  }
  
  public TemplateInfo data(Object data) {
    builder.put(DATA, data);
    return this;
  }
  
  public TemplateInfo file(Object file) {
    builder.put(FILE, file);
    return this;
  }
  
  public TemplateInfo source(Object source) {
    builder.put(SOURCE, source);
    return this;
  }
 
  public String getMessage() {
    Map<String, Object> params = builder.get();
    return type.message(params);
  }
  
}
