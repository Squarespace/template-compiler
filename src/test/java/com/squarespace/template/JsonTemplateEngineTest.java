package com.squarespace.template;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.squarespace.template.CodeException;
import com.squarespace.template.FormatterTable;
import com.squarespace.template.JsonTemplateEngine;
import com.squarespace.template.PredicateTable;
import com.squarespace.template.plugins.CoreFormatters;
import com.squarespace.template.plugins.CorePredicates;


public class JsonTemplateEngineTest {

  private static final FormatterTable FORMATTERS = new FormatterTable();

  private static final PredicateTable PREDICATES = new PredicateTable();

  private static final JsonTemplateEngine COMPILER;
  
  static {

    // Configure static plugins to replace defaults with your values.
    CoreFormatters.DATE.setTimezoneKey(Constants.TIMEZONE_KEY);
    
    FORMATTERS.register(new CoreFormatters());
    // Register additional formatters

    PREDICATES.register(new CorePredicates());
    // Register additional predicates

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
