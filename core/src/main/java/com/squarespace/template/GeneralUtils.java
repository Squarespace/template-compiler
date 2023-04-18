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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.squarespace.cldrengine.api.Decimal;
import com.squarespace.template.Constants;


/**
 * Utility methods used by various parts of the framework.
 */
public class GeneralUtils {

  private static final JsonFactory JSON_FACTORY = new JsonFactory();
  private static final PrettyPrinter PRETTY_PRINTER = new JsonPrettyPrinter();

  private GeneralUtils() {
  }

  /**
   * Return true if the string argument is null or empty, false otherwise.
   */
  public static boolean isEmpty(String s) {
    return s == null || s.isEmpty();
  }

  /**
   * Quick string to integer conversion, clamping negative values to zero.
   */
  public static long toPositiveLong(CharSequence seq, int pos, int length) {
    long n = 0;
    int i = pos;
    while (i < length) {
      char c = seq.charAt(i);
      if (c >= '0' && c <= '9') {
        n *= 10;
        n += c - '0';
      } else {
        break;
      }
      i++;
    }
    return n;
  }

  /**
   * Convert an opaque JSON node to Decimal using the most correct
   * conversion method.
   */
  public static Decimal nodeToDecimal(JsonNode node) {
    JsonNodeType type = node.getNodeType();
    if (type == JsonNodeType.NUMBER) {
      return numericToDecimal((NumericNode)node);

    } else {
      try {
        return new Decimal(node.asText());
      } catch (ArithmeticException | NumberFormatException e) {
        // Fall through..
      }
    }
    return null;
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
   * Convert a numeric JSON node to Decimal using the most correct
   * conversion method.
   */
  private static Decimal numericToDecimal(NumericNode node) {
    switch (node.numberType()) {
      case INT:
      case LONG:
        return new Decimal(node.asLong());
      case FLOAT:
      case DOUBLE:
        return new Decimal(node.asDouble());
      case BIG_DECIMAL:
      case BIG_INTEGER:
      default:
        return new Decimal(node.decimalValue().toString());
    }
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
    return executeTemplate(ctx, inst, node, privateContext, null);
  }

  /**
   * Executes a compiled instruction using the given context and JSON node.
   * Optionally hides all context above the JSON node, treating it as a root.
   * This is a helper method for formatters which need to execute templates to
   * produce their output.
   * Optionally allows passing in an 'argvar' node which will be defined inside the
   * template as '@args'.
   */
  public static JsonNode executeTemplate(Context ctx, Instruction inst, JsonNode node, boolean privateContext,
        ObjectNode argvar)
      throws CodeExecuteException {

    // Temporarily swap the buffers to capture all output of the partial.
    StringBuilder buf = new StringBuilder();
    StringBuilder origBuf = ctx.swapBuffer(buf);
    try {
      // If we want to hide the parent context during execution, create a new
      // temporary sub-context.
      ctx.push(node);
      ctx.frame().stopResolution(privateContext);
      if (argvar != null) {
        ctx.setVar("@args", argvar);
      }
      ctx.execute(inst);

    } finally {
      ctx.swapBuffer(origBuf);
      ctx.pop();
    }
    return new TextNode(buf.toString());
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
      return streamToString(stream);
    } catch (IOException e) {
      throw new CodeExecuteException(resourceLoadError(path, e.toString()));
    }
  }

  public static String streamToString(InputStream stream) throws IOException {
    try (InputStreamReader reader = new InputStreamReader(stream, "UTF-8")) {
      StringBuilder buf = new StringBuilder();
      char[] buffer = new char[4096];
      int n = 0;
      while (-1 != (n = reader.read(buffer))) {
        buf.append(buffer, 0, n);
      }
      return buf.toString();
    }
  }

  public static boolean resourceExists(Class<?> cls, String fileName) {
    try {
      String name = resolveName(cls, fileName);
      Enumeration<URL> urls = cls.getClassLoader().getResources(name);
      return urls.hasMoreElements();
    } catch (IOException e) {
      throw new RuntimeException("Failed to list resources", e);
    }
  }

  public static List<Path> list(Class<?> cls, Predicate<Path> predicate) {
    try {
      Enumeration<URL> urls = cls.getClassLoader().getResources(resolveName(cls, "."));
      List<Path> result = new ArrayList<>();
      while (urls.hasMoreElements()) {
        URL url = urls.nextElement();
        if (!url.getProtocol().equals("file")) {
          continue;
        }
        File dir = Paths.get(url.toURI()).toFile();
        if (dir == null || !dir.isDirectory()) {
          continue;
        }
        for (File file : dir.listFiles()) {
          Path path = file.toPath();
          if (predicate.test(path)) {
            result.add(path.getFileName());
          }
        }
      }
      if (result.isEmpty()) {
        throw new RuntimeException("No files matched predicate");
      }
      Collections.sort(result);
      return result;
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException("Failed to list resources", e);
    }
  }

  private static ErrorInfo resourceLoadError(String path, String message) {
    ErrorInfo info = new ErrorInfo(ExecuteErrorType.RESOURCE_LOAD);
    info.name(path);
    info.data(message);
    return info;
  }

  private static String resolveName(Class<?> cls, String name) {
    if (name == null) {
      return name;
    }
    if (!name.startsWith("/")) {
      while (cls.isArray()) {
        cls = cls.getComponentType();
      }
      String baseName = cls.getName();
      int index = baseName.lastIndexOf('.');
      if (index != -1) {
        name = baseName.substring(0, index).replace('.', '/') + "/" + name;
      }
    } else {
      name = name.substring(1);
    }
    return name;
  }

  /**
   * Map.getOrDefault only available in JDK 8., <s>For now we support JDK 7.</s>
   * We now support JDK 8 but leaving this in place anyway.
   */
  public static <K, V> V getOrDefault(Map<K, V> map, K key, V defaultValue) {
    V value = map.get(key);
    return value == null ? defaultValue : value;
  }

  /**
   * Returns the translated string given the key in that localizedStrings node.
   * If key is not found, it returns the defaultValue.
   */
  public static String localizeOrDefault(JsonNode localizedStrings, String key, String defaultValue) {
    JsonNode node = localizedStrings.get(key);
    return node == null ? defaultValue : node.asText();
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
    gen.setPrettyPrinter(PRETTY_PRINTER);
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
      Object key = parts[i];
      if (allDigits(parts[i])) {
        try {
          key = Integer.parseInt(parts[i], 10);
        } catch (NumberFormatException e) {
          // Integer too large, treat as a string key.
          // fall through..
        }
      }
      keys[i] = key;
    }
    return keys;
  }

  /**
   * URL-encodes the string.
   */
  public static String urlEncode(String val) {
    try {
      return URLEncoder.encode(val, "UTF-8").replace("+", "%20");
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
      double n = node.asDouble();
      return !Double.isNaN(n) && n != 0;
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

  public static JsonNode getNodeAtPath(JsonNode node, Object[] path) {
    if (path == null) {
      return Constants.MISSING_NODE;
    }

    JsonNode tmp = node;

    for (Object key : path) {
      // Adapt the argument to the type of node we're accessing
      if (tmp instanceof ArrayNode && key instanceof Integer) {
        tmp = tmp.path((int)key);
      } else if (tmp instanceof ObjectNode) {
        if (key instanceof Integer) {
          key = ((Integer)key).toString();
        }
        tmp = tmp.path((String)key);
      } else {
        tmp = Constants.MISSING_NODE;
      }

      if (tmp.isMissingNode()) {
        break;
      }
    }

    return tmp;
  }
}
