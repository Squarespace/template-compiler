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

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.LongNode;
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
