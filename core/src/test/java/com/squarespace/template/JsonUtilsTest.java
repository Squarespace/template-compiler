/**
 * Copyright (c) 2015 SQUARESPACE, Inc.
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;


public class JsonUtilsTest {

  @Test
  public void testJsonCompare() {
    assertEquals(JsonUtils.compare(json(1), json(1)), 0);
    assertEquals(JsonUtils.compare(json(1), json(2)), -1);
    assertEquals(JsonUtils.compare(json(2), json(1)), 1);

    assertEquals(JsonUtils.compare(json(1.0), json(1.0)), 0);
    assertEquals(JsonUtils.compare(json(1.0), json(2.0)), -1);
    assertEquals(JsonUtils.compare(json(2.0), json(1.0)), 1);

    assertEquals(JsonUtils.compare(json("a"), json("a")), 0);
    assertEquals(JsonUtils.compare(json("a"), json("b")), -1);
    assertEquals(JsonUtils.compare(json("b"), json("a")), 1);

    assertEquals(JsonUtils.compare(BooleanNode.TRUE, BooleanNode.TRUE), 0);
    assertEquals(JsonUtils.compare(BooleanNode.FALSE, BooleanNode.FALSE), 0);
    assertEquals(JsonUtils.compare(BooleanNode.TRUE, json("true")), 0);
    assertEquals(JsonUtils.compare(BooleanNode.FALSE, json("false")), 0);
    assertEquals(JsonUtils.compare(BooleanNode.TRUE, BooleanNode.FALSE), 1);
    assertEquals(JsonUtils.compare(BooleanNode.FALSE, BooleanNode.TRUE), -1);
  }

  /**
   * Enforces the released order on the 2-arg signature, the cases the total
   * order fix changes.
   */
  @Test
  public void testCompareLegacyOrder() {
    // A fractional right operand is truncated to long.
    assertEquals(JsonUtils.compare(IntNode.valueOf(2), DoubleNode.valueOf(2.5)), 0);
    assertEquals(JsonUtils.compare(IntNode.valueOf(2), DoubleNode.valueOf(2.001)), 0);

    // Text vs number orders by whichever side is left, lexicographic or
    // truncated, so the two directions disagree.
    assertEquals(JsonUtils.compare(TextNode.valueOf("5"), IntNode.valueOf(40)), 1);
    assertEquals(JsonUtils.compare(IntNode.valueOf(40), TextNode.valueOf("5")), 1);

    // Text vs null compares against the text null, a raw string diff.
    assertEquals(JsonUtils.compare(NullNode.getInstance(), TextNode.valueOf("a")), -1);
    assertEquals(JsonUtils.compare(TextNode.valueOf("a"), NullNode.getInstance()), "a".compareTo("null"));
  }

  @Test
  public void testCompareCrossTypes() {
    // Integers and doubles compare exactly, without truncation.
    assertEquals(JsonUtils.compare(IntNode.valueOf(2), DoubleNode.valueOf(2.5), false), -1);
    assertEquals(JsonUtils.compare(DoubleNode.valueOf(2.5), IntNode.valueOf(2), false), 1);
    assertEquals(JsonUtils.compare(IntNode.valueOf(2), DoubleNode.valueOf(2.0), false), 0);

    // Text ranks below numbers, so this is never lexicographic ("5" > "40").
    assertEquals(JsonUtils.compare(json("5"), json(40), false), -1);
    assertEquals(JsonUtils.compare(json(40), json("5"), false), 1);

    // Boolean ranks above text and numbers.
    assertEquals(JsonUtils.compare(BooleanNode.TRUE, json("true"), false), 1);
    assertEquals(JsonUtils.compare(json("true"), BooleanNode.TRUE, false), -1);
    assertEquals(JsonUtils.compare(BooleanNode.TRUE, json(1), false), 1);

    // Null and missing are both "nothing" and compare equal.
    assertEquals(JsonUtils.compare(NullNode.getInstance(), MissingNode.getInstance(), false), 0);
    assertEquals(JsonUtils.compare(MissingNode.getInstance(), NullNode.getInstance(), false), 0);

    // NaN is not a valid JSON value; it sorts before every finite number.
    assertEquals(JsonUtils.compare(DoubleNode.valueOf(Double.NaN), DoubleNode.valueOf(1.0), false), -1);
    assertEquals(JsonUtils.compare(DoubleNode.valueOf(1.0), DoubleNode.valueOf(Double.NaN), false), 1);
    assertEquals(JsonUtils.compare(DoubleNode.valueOf(Double.NaN), DoubleNode.valueOf(Double.NaN), false), 0);
  }

  /**
   * Property tests over a matrix of scalar node kinds: the fixed order must
   * be antisymmetric on every pair and transitive on every triple.
   */
  @Test
  public void testCompareTotalOrder() {
    JsonNode[] nodes = new JsonNode[] {
        MissingNode.getInstance(),
        NullNode.getInstance(),
        TextNode.valueOf("z"),
        IntNode.valueOf(1),
        LongNode.valueOf(2L),
        DoubleNode.valueOf(2.5),
        BooleanNode.FALSE,
        BooleanNode.TRUE,
    };
    for (JsonNode a : nodes) {
      for (JsonNode b : nodes) {
        if (a == b) {
          assertEquals(JsonUtils.compare(a, b, false), 0, "reflexive " + a.getNodeType());
        }
        // Antisymmetry: compare(a,b) is the exact negation of compare(b,a).
        int ab = JsonUtils.compare(a, b, false);
        assertEquals(ab, -JsonUtils.compare(b, a, false),
            "antisymmetry " + a.getNodeType() + " vs " + b.getNodeType());
        // Transitivity on every triple.
        for (JsonNode c : nodes) {
          if (ab < 0 && JsonUtils.compare(b, c, false) < 0) {
            assertTrue(JsonUtils.compare(a, c, false) < 0,
                "transitivity " + a.getNodeType() + " < " + b.getNodeType()
                    + " < " + c.getNodeType());
          }
        }
      }
    }
  }

  private JsonNode json(long value) {
    return new LongNode(value);
  }

  private JsonNode json(double value) {
    return new DoubleNode(value);
  }

  private JsonNode json(String value) {
    return new TextNode(value);
  }

}
