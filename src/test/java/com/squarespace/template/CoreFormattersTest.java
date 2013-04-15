package com.squarespace.template;

import static com.squarespace.template.Constants.EMPTY_ARGUMENTS;
import static com.squarespace.template.CoreFormatters.PLURALIZE;
import static com.squarespace.template.CoreFormatters.SLUGIFY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import com.squarespace.v6.utils.JSONUtils;


public class CoreFormattersTest extends UnitTestBase {

  @Test
  public void testPluralize() throws CodeException, ArgumentsException {
    CodeMaker mk = maker();
    
    Arguments args = mk.args("");
    assertFormatter(PLURALIZE, "0", "");
    assertFormatter(PLURALIZE, "1", "");
    assertFormatter(PLURALIZE, "2", "s");
    assertFormatter(PLURALIZE, "3.1415", "s");

    args = mk.args(" A");
    assertFormatter(PLURALIZE, "0", "", args);
    assertFormatter(PLURALIZE, "1", "", args);
    assertFormatter(PLURALIZE, "2", "A", args);
    assertFormatter(PLURALIZE, "100", "A", args);
    
    args = mk.args("/A/B");
    assertFormatter(PLURALIZE, "0", "A", args);
    assertFormatter(PLURALIZE, "1", "A", args);
    assertFormatter(PLURALIZE, "2", "B", args);
    assertFormatter(PLURALIZE, "100", "B", args);
    
    args = mk.args(":1:2:3:4");
    assertInvalidArgs(PLURALIZE, args);
  }
  
  @Test
  public void testSlugify() throws CodeException {
    String data = "\"Next Total Eclipse on 20th of March 2015\"";
    assertFormatter(SLUGIFY, data, "next-total-eclipse-on-20th-of-march-2015");
    data = "\"Value of PI is approx. 3.14159\"";
    assertFormatter(SLUGIFY, data, "value-of-pi-is-approx-314159");
    data = "\"1.2.3.4.5-()*&-foo.bar-baz\"";
    assertFormatter(SLUGIFY, data, "12345--foobar-baz");
  }
  
  private void assertFormatter(Formatter impl, String json, String expected) throws CodeException {
    assertFormatter(impl, json, expected, EMPTY_ARGUMENTS);
  }
  
  private void assertFormatter(Formatter impl, String json, String expected, Arguments args) throws CodeException {
    Context ctx = new Context(JSONUtils.decode(json));
    impl.apply(ctx, args);
    assertEquals(eval(ctx), expected);
  }

  private void assertInvalidArgs(Formatter impl, Arguments args) {
    try {
      impl.validateArgs(args);
      fail("Expected " + args + " to raise exception");
    } catch (ArgumentsException e) {
      // Expected
    }
  }
}
