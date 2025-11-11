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
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class JsonUtils {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private JsonUtils() {
  }

  public static ObjectNode createObjectNode() {
    return MAPPER.createObjectNode();
  }

  public static JsonNode decode(String input) {
    return decode(input, false);
  }

  /**
   * Attempt to decode the input as JSON. Returns the {@link JsonNode} if
   * the decode is successful.
   *
   * If the decode fails and the {@code failQuietly} flag is true, returns a
   * {@link MissingNode}. Otherwise an {@link IllegalArgumentException} will
   * be thrown.
   */
  public static JsonNode decode(String input, boolean failQuietly) {
    try {
      return MAPPER.readTree(input);
    } catch (IOException e) {
      if (failQuietly) {
        return MissingNode.getInstance();
      }
      throw new IllegalArgumentException("Unable to decode JSON", e);
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
    return compare(left, right, true);
  }

  /**
   * Compare two JsonNode values with the released behavior flag. Returns a
   * negative, zero, or positive int when left is less than, equal to, or
   * greater than right.
   *
   * When the flag is set the released order applies, which is not a total
   * order over mixed types. When clear a total order applies:
   * 1. number vs number: exact numeric order via BigDecimal, never
   *    truncated or rounded. NaN sorts before every finite number, and
   *    NaN equals NaN.
   * 2. text vs text: lexicographic code-unit order (no locale).
   * 3. boolean vs boolean: false < true.
   * 4. mixed types: fixed type rank, missing/null (0) < text (1) < number
   *    (2) < boolean (3) < composite (4). So "5" < 40, never lexicographic.
   * 5. same rank otherwise: null and missing both mean "nothing" and are
   *    equal; composites (objects, arrays) have no natural order and fall
   *    back to node equality.
   */
  public static int compare(JsonNode left, JsonNode right, boolean legacyOrder) {
    if (legacyOrder) {
      // Legacy, the exact code the release shipped.
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

    if (left.isNumber() && right.isNumber()) {
      return compareNumbers(left, right);
    }
    if (left.isTextual() && right.isTextual()) {
      return left.asText().compareTo(right.asText());
    }
    if (left.isBoolean() && right.isBoolean()) {
      return Boolean.compare(left.asBoolean(), right.asBoolean());
    }
    int leftRank = typeRank(left);
    int rightRank = typeRank(right);
    if (leftRank != rightRank) {
      return leftRank < rightRank ? -1 : 1;
    }
    if (leftRank == 0) {
      return 0;
    }
    return left.equals(right) ? 0 : -1;
  }

  /**
   * Compare two numbers. NaN is not a valid JSON value and BigDecimal
   * cannot hold it, so NaN orders before every finite number.
   */
  private static int compareNumbers(JsonNode left, JsonNode right) {
    boolean leftNaN = left.isFloatingPointNumber() && Double.isNaN(left.asDouble());
    boolean rightNaN = right.isFloatingPointNumber() && Double.isNaN(right.asDouble());
    if (leftNaN || rightNaN) {
      return leftNaN == rightNaN ? 0 : (leftNaN ? -1 : 1);
    }
    return left.decimalValue().compareTo(right.decimalValue());
  }

  /**
   * Fixed rank for a node's type, used to order mixed-type pairs.
   */
  private static int typeRank(JsonNode node) {
    if (node.isNull() || node.isMissingNode()) {
      return 0;
    }
    if (node.isTextual()) {
      return 1;
    }
    if (node.isNumber()) {
      return 2;
    }
    if (node.isBoolean()) {
      return 3;
    }
    return 4;
  }

}
