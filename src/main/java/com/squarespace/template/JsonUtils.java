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

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class JsonUtils {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private JsonUtils() {
  }

  public static ObjectNode createObjectNode() {
    return MAPPER.createObjectNode();
  }

  public static JsonNode decode(String input) {
    try {
      return MAPPER.readTree(input);
    } catch (IOException e) {
      throw new IllegalArgumentException("Unabled to decode JSON", e);
    }
  }

  public static ArrayNode createArrayNode() {
    return MAPPER.createArrayNode();
  }

  public static ObjectMapper getMapper() {
    return MAPPER;
  }

  /**
   * Compare two JsonNode objects and return an integer.
   *
   * @return  a negative integer, zero, or a positive integer as this object
   *          is less than, equal to, or greater than the specified object.
   */
  public static int compare(JsonNode left, JsonNode right) {
    if (left.isLong() || left.isInt()) {
      return Long.compare(left.asLong(), right.asLong());

    } else if (left.isDouble() || left.isFloat()) {
      return Double.compare(left.asDouble(), right.asDouble());

    } else if (left.isTextual()) {
      return left.asText().compareTo(right.asText());

    } else if (left.isBoolean()) {
      return Boolean.compare(left.asBoolean(), right.asBoolean());
    }

    // Not comparable in a relative sense, default to equals.
    return left.equals(right) ? 0 : -1;
  }

}
