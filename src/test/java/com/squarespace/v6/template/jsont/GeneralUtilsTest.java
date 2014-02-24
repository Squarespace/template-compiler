package com.squarespace.template;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;


@Test( groups={ "unit" })
public class GeneralUtilsTest {

  @Test
  public void testGetFirstMatchingNode() {
    ObjectNode o1 = JsonUtils.createObjectNode();
    o1.put("foo", "bar");
    ObjectNode o2 = JsonUtils.createObjectNode();
    o2.put("obj", o1);
    
    assertEquals(GeneralUtils.getFirstMatchingNode(o2, "bar", "obj", "foo"), o1);
    assertEquals(GeneralUtils.getFirstMatchingNode(o2, "x", "y"), Constants.MISSING_NODE);
  }
  
  @Test
  public void testIfString() {
    // Truth-y values
    JsonNode node = new IntNode(123);
    assertEquals(GeneralUtils.ifString(node, "000"), "123");
    node = new TextNode("456");
    assertEquals(GeneralUtils.ifString(node, "000"), "456");
    
    // False-y values
    node = new IntNode(0);
    assertEquals(GeneralUtils.ifString(node, "000"), "000");
    node = NullNode.getInstance();
    assertEquals(GeneralUtils.ifString(node, "000"), "000");
  }
}
