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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.NumericNode;


/**
 * Utility methods used by various parts of the framework.
 */
public class GeneralUtils {

  private static final JsonFactory JSON_FACTORY = new JsonFactory();

  private GeneralUtils() {
  }

  /**
   * Quick string to integer conversion.
   */
  public static long toLong(CharSequence seq, int pos, int length) {
    long n = 0;
    int i = pos;
    while (i < length) {
      char c = seq.charAt(i);
      if (c >= '0' && c <= '9') {
        n *= 10;
        n += (int)(c - '0');
      } else {
        break;
      }
      i++;
    }
    return n;
  }
  /**
   * Convert an opaque JSON node to BigDecimal using the most correct
   * conversion method.
   */
  public static BigDecimal nodeToBigDecimal(JsonNode node) {
    JsonNodeType type = node.getNodeType();
    if (type == JsonNodeType.NUMBER) {
      return numericToBigDecimal((NumericNode)node);

    } else {
      try {
        return new BigDecimal(node.asText());
      } catch (ArithmeticException | NumberFormatException e) {
        // Fall through..
      }
    }
    return null;
  }

  /**
   * Convert a numeric JSON node to BigDecimal using the most correct
   * conversion method.
   */
  private static BigDecimal numericToBigDecimal(NumericNode node) {
    switch (node.numberType()) {
      case INT:
      case LONG:
        return BigDecimal.valueOf(node.asLong());
      case FLOAT:
      case DOUBLE:
        return BigDecimal.valueOf(node.asDouble());
      case BIG_DECIMAL:
      case BIG_INTEGER:
      default:
        return node.decimalValue();
    }
  }


  /**
   * Executes a compiled instruction using the given context and JSON node.
   * Optionally hides all context above the JSON node, treating it as a root.
   * This is a helper method for formatters which need to execute templates to
   * produce their output.
   */
  public static JsonNode executeTemplate(Context ctx, Instruction inst, JsonNode node, boolean privateContext)
      throws CodeExecuteException {

    // Temporarily swap the buffers to capture all output of the partial.
    StringBuilder buf = new StringBuilder();
    StringBuilder origBuf = ctx.swapBuffer(buf);
    try {
      // If we want to hide the parent context during execution, create a new
      // temporary sub-context.
      ctx.push(node);
      ctx.frame().stopResolution(privateContext);
      ctx.execute(inst);

    } finally {
      ctx.swapBuffer(origBuf);
      ctx.pop();
    }
    return ctx.buildNode(buf.toString());
  }

  /**
   * Loads a resource from the Java package relative to {@code cls}, raising a
   * CodeException if it fails.
   */
  public static String loadResource(Class<?> cls, String path) throws CodeException {
    try (InputStream stream = cls.getResourceAsStream(path)) {
      if (stream == null) {
        throw new CodeExecuteException(resourceLoadError(path, "not found"));
      }
      return IOUtils.toString(stream, "UTF-8");
    } catch (IOException e) {
      throw new CodeExecuteException(resourceLoadError(path, e.toString()));
    }
  }

  private static ErrorInfo resourceLoadError(String path, String message) {
    ErrorInfo info = new ErrorInfo(ExecuteErrorType.RESOURCE_LOAD);
    info.name(path);
    info.data(message);
    return info;
  }


  /**
   * Map.getOrDefault only available in JDK 8, for now we support JDK 7.
   */
  public static <K, V> V getOrDefault(Map<K, V> map, K key, V defaultValue) {
    V value = map.get(key);
    return value == null ? defaultValue : value;
  }

  /**
   * Returns true if the first non-whitespace character is one of the
   * valid starting characters for a JSON value; else false.
   */
  public static boolean isJsonStart(String raw) {
    int size = raw.length();
    int index = 0;
    while (index < size) {
      char ch = raw.charAt(index);
      if (ch != ' ') {
        switch (ch) {
          case '"':
          case '-':
          case '0':
          case '1':
          case '2':
          case '3':
          case '4':
          case '5':
          case '6':
          case '7':
          case '8':
          case '9':
          case '[':
          case '{':
            return true;

          // Even when failSilently=true is passed, Jackson's decode method
          // throws an exception and immediately swallows it.

          // These string comparisons add more precision when trying to detect
          // a JSON value without attempting to parse it.  They are place-holders
          // to cut down on Jackson exceptions until a long-term fix is made.

          case 'f':
            return raw.startsWith("false");

          case 'n':
            return raw.startsWith("null");

          case 't':
            return raw.startsWith("true");

          default:
            return false;
        }
      }
      index++;
    }
    return false;
  }


  /**
   * Checks the {@code parent} node to see if it contains one of the keys, and
   * returns the first that matches. If none match it returns {@link Constants#MISSING_NODE}
   */
  public static JsonNode getFirstMatchingNode(JsonNode parent, String... keys) {
    for (String key : keys) {
      JsonNode node = parent.path(key);
      if (!node.isMissingNode()) {
        return node;
      }
    }
    return Constants.MISSING_NODE;
  }

  /**
   * Formats the {@code node} as a string using the pretty printer.
   */
  public static String jsonPretty(JsonNode node) throws IOException {
    StringBuilder buf = new StringBuilder();
    JsonGenerator gen = JSON_FACTORY.createGenerator(new StringBuilderWriter(buf));
    gen.useDefaultPrettyPrinter();
    gen.setCodec(JsonUtils.getMapper());
    gen.writeTree(node);
    return buf.toString();
  }

  /**
   * Splits a variable name into its parts.
   */
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

  /**
   * URL-encodes the string.
   */
  public static String urlEncode(String val) {
    try {
      return URLEncoder.encode(val, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return val;
    }
  }

  /**
   * Determines the boolean value of a node based on its type.
   */
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

  /**
   * Obtains the text representation of a node, converting {@code null} to
   * empty string.
   */
  public static String eatNull(JsonNode node) {
    return node.isNull() ? "" : node.asText();
  }

  /**
   * Indicates if the string consists of all digits.
   */
  private static boolean allDigits(String str) {
    for (int i = 0, len = str.length(); i < len; i++) {
      if (!Character.isDigit(str.charAt(i))) {
        return false;
      }
    }
    return true;
  }

}
