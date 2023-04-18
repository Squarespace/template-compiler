/**
 * Copyright (c) 2014 SQUARESPACE, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.squarespace.template;

import static com.squarespace.template.GeneralUtils.isJsonStart;
import static com.squarespace.template.GeneralUtils.splitVariable;
import static com.squarespace.template.GeneralUtils.toPositiveLong;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;


@Test(groups = { "unit" })
public class GeneralUtilsTest {

  @Test
  public void testLoadResourceTest() {
    try {
      GeneralUtils.loadResource(getClass(), "this/resource/is/missing.txt");
      Assert.fail("GeneralUtils.loadResource() should throw on missing resource");
    } catch (CodeException e) {
      // expected
    }
  }

  @Test
  public void testSplitVariable() {
    assertEquals(splitVariable("@"), null);
    assertEquals(splitVariable("@index"), new Object[] { "@index" });
    assertEquals(splitVariable("@index0"), new Object[] { "@index0" });
    assertEquals(splitVariable("a"), new Object[] { "a" });
    assertEquals(splitVariable("a.b.c"), new Object[] { "a", "b", "c" });
    assertEquals(splitVariable("a.1.b.2"), new Object[] { "a", 1, "b", 2 });
    assertEquals(splitVariable("0.1.2"), new Object[] { 0, 1, 2 });

    // too large an integer
    String large = "333333333333333333333333333333";
    assertEquals(splitVariable(large), new Object[] { large });
  }

  @Test
  public void testToLong() {
    assertEquals(toPositiveLong("12345", 0, 5), 12345);
    assertEquals(toPositiveLong("12345xyz", 0, 8), 12345);
    assertEquals(toPositiveLong("xyz", 0, 3), 0);
    assertEquals(toPositiveLong("   ", 0, 3), 0);
  }

  @Test
  public void testIsJsonStart() {
    assertTrue(isJsonStart("123"));
    assertTrue(isJsonStart("  123"));
    assertTrue(isJsonStart("  {\"key\": \"val\"}"));
    assertTrue(isJsonStart("  [1, 2, 3]"));
    assertFalse(isJsonStart(":123"));
    assertFalse(isJsonStart("'foo'"));
    assertFalse(isJsonStart("   "));
  }

  @Test
  public void testGetFirstMatchingNode() {
    ObjectNode o1 = JsonUtils.createObjectNode();
    o1.put("foo", "bar");
    ObjectNode o2 = JsonUtils.createObjectNode();
    o2.set("obj", o1);

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

  @Test
  public void testGetNodeAtPath() {
    ObjectNode arrItem = JsonUtils.createObjectNode();
    arrItem.put("id", "node-1");

    ArrayNode arr = JsonUtils.createArrayNode();
    arr.add(arrItem);

    ObjectNode container = JsonUtils.createObjectNode();
    container.set("arr", arr);

    assertEquals(GeneralUtils.getNodeAtPath(container, null), Constants.MISSING_NODE);

    Object[] path = splitVariable("some.0.nonexistent.field");
    assertEquals(GeneralUtils.getNodeAtPath(container, path), Constants.MISSING_NODE);

    path = new Object[0];
    assertEquals(GeneralUtils.getNodeAtPath(container, path), container);

    path = splitVariable("arr");
    assertEquals(GeneralUtils.getNodeAtPath(container, path), arr);

    path = splitVariable("arr.0.id");
    assertEquals(GeneralUtils.getNodeAtPath(container, path).asText(), "node-1");
  }
}
