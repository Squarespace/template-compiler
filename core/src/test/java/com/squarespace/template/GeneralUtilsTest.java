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
import static com.squarespace.template.GeneralUtils.toLong;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
  public void testToLong() {
    assertEquals(toLong("12345", 0, 5), 12345);
    assertEquals(toLong("12345xyz", 0, 8), 12345);
    assertEquals(toLong("xyz", 0, 3), 0);
    assertEquals(toLong("   ", 0, 3), 0);
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
}
