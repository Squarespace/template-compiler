package com.squarespace.template;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class ReferenceScannerTest extends UnitTestBase {

  private static final boolean VERBOSE = true;

  @Test
  public void testBasic() throws CodeException, JsonProcessingException {
    ObjectNode result = scan("{.section nums}{.even? @}#{@|json}{.end}{.end}");
    render(result);

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

  @Test
  public void testSectionNesting() throws CodeException {
    ObjectNode result = scan("{.section a}{.section b}{.section c}{d}{.end}{.end}{.end}");
    render(result);

    ObjectNode vars = (ObjectNode)result.get("variables");
    assertTrue(vars.get("a").get("b").get("c").get("d").isNull());
  }

  @Test
  public void testAlternates() throws CodeException {
    ObjectNode result = scan("{.odd? a}{a}{.or}{b}{.end}{.repeated section c}{.alternates with}d{.end}");
    render(result);

  }

  private ObjectNode scan(String source) throws CodeException {
    ReferenceScanner scanner = new ReferenceScanner();
    CompiledTemplate template = compiler().compile(source);
    scanner.extract(template.code());
    return scanner.references().report();
  }

  private void render(ObjectNode result) {
    try {
      String json = JsonUtils.getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(result);
      if (VERBOSE) {
        System.out.println(json);
      }
    } catch (JsonProcessingException e) {
      Assert.fail(e.getMessage());
    }
  }

}
