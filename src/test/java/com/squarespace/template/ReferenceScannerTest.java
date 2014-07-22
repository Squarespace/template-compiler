package com.squarespace.template;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class ReferenceScannerTest extends UnitTestBase {

  private static final boolean DEBUG = false;

  @Test
  public void testBasic() throws CodeException, JsonProcessingException {
    ReferenceScanner scanner = new ReferenceScanner();
    CompiledTemplate template = compiler().compile("{.section nums}{.even? @}#{@|json}{.end}{.end}");
    scanner.extract(template.code());
    ObjectNode result = scanner.references().report();

    for (String type : new String[] { "VARIABLE", "TEXT", "SECTION", "PREDICATE" }) {
      assertEquals(result.get("instructions").get(type).asInt(), 1);
    }
    assertEquals(result.get("instructions").get("END").asInt(), 2);
    assertEquals(result.get("predicates").get("even?").asInt(), 1);
    assertEquals(result.get("formatters").get("json").asInt(), 1);
    assertTrue(result.get("variables").get("nums").isObject());
    assertEquals(result.get("textBytes").asInt(), 1);
    if (DEBUG) {
      String json = JsonUtils.getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(result);
      System.out.println(json);
    }
  }

}
