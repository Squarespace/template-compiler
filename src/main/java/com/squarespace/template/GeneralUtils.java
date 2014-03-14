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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;


public class GeneralUtils {

  private static final JsonFactory JSON_FACTORY = new JsonFactory();

  private GeneralUtils() {
  }

  public static JsonNode getFirstMatchingNode(JsonNode parent, String ... keys) {
    for (String key : keys) {
      JsonNode node = parent.path(key);
      if (!node.isMissingNode()) {
        return node;
      }
    }
    return Constants.MISSING_NODE;
  }

  public static String jsonPretty(JsonNode node) throws IOException {
    StringBuilder buf = new StringBuilder();
    JsonGenerator gen = JSON_FACTORY.createGenerator(new StringBuilderWriter(buf));
    gen.useDefaultPrettyPrinter();
    gen.setCodec(JsonUtils.getMapper());
    gen.writeTree(node);
    return buf.toString();
  }

  public static Object[] splitVariable(String name) {
    String[] parts = name.equals("@") ? null : StringUtils.split(name, '.');
    if (parts == null) {
      return null;
    }

    // Each segment of the key path can be either a String or an Integer.
    Object[] keys = new Object[parts.length];
    for (int i = 0, len = parts.length; i < len; i++) {
      keys[i] = allDigits(parts[i]) ? Integer.parseInt(parts[i], 10) : parts[i];
    }
    return keys;
  }

  public static String urlEncode(String val) {
    try {
      return URLEncoder.encode(val, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return val;
    }
  }

  public static boolean isTruthy(JsonNode node) {
    if (node.isTextual()) {
      return !node.asText().equals("");
    }
    if (node.isNumber() || node.isBoolean()) {
      return node.asLong() != 0;
    }
    if (node.isMissingNode() || node.isNull()) {
      return false;
    }
    return node.size() != 0;
  }

  public static String ifString(JsonNode node, String defaultString) {
    return isTruthy(node) ? node.asText() : defaultString;
  }

  public static double ifDouble(JsonNode node, double defaultValue) {
    return isTruthy(node) ? node.asDouble() : defaultValue;
  }

  public static String eatNull(JsonNode node) {
    return node.isNull() ? "" : node.asText();
  }

  private static boolean allDigits(String str) {
    for (int i = 0, len = str.length(); i < len; i++) {
      if (!Character.isDigit(str.charAt(i))) {
        return false;
      }
    }
    return true;
  }

}
