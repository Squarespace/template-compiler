package com.squarespace.template;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.squarespace.template.plugins.CoreFormatters;
import com.squarespace.template.plugins.CorePredicates;


public class JsonTemplateEngineTest {

  private static final FormatterTable FORMATTERS = new FormatterTable();

  private static final PredicateTable PREDICATES = new PredicateTable();

  private static final JsonTemplateEngine COMPILER;
  
  static {
    FORMATTERS.register(new CoreFormatters());
    // Register your formatters here

    PREDICATES.register(new CorePredicates());
    // Register your predicates here

    COMPILER = new JsonTemplateEngine(FORMATTERS, PREDICATES);
  }

  @Test
  public void testCompile() throws CodeException {
    COMPILER.compile("{.section foo}{@}{.end}");
    
    try {
      COMPILER.compile("{.foo?}");
      Assert.fail("Expected CodeException");
    } catch (CodeException e) { }
  }
  
}
