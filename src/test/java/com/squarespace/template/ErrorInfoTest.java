package com.squarespace.template;

import static org.testng.Assert.assertEquals;

import java.util.Map;

import org.testng.annotations.Test;


public class ErrorInfoTest {

  static class DummyType implements ErrorType {
    
    private MapFormat format;
    
    public DummyType(String format) {
      this.format = new MapFormat(format, "?");
    }
      
    @Override
    public String format(Map<String, Object> params) {
      return format.apply(params);
    }
  }
  
  @Test
  public void testRepr() {
    DummyType type = new DummyType("%(name)s %(data)s %(repr)s");
    ErrorInfo info = new ErrorInfo(type);
    info.name("foo");
    MapBuilder<String, Object> builder = info.getBuilder();
    assertEquals(builder.get().get("name"), "foo");
    assertEquals(info.getMessage(), "foo ? ?");
    
    info.repr("bar");
    assertEquals(info.getMessage(), "foo ? bar");
    
  }
}
