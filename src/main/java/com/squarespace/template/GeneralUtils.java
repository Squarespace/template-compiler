package com.squarespace.template;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.io.output.StringBuilderWriter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.v6.utils.JSONUtils;


public class GeneralUtils {

  public static final JsonNode EMPTY_OBJECT = JSONUtils.createObjectNode();

  public static final JsonNode MISSING_NODE = EMPTY_OBJECT.path("");

  private static final JsonFactory JSON_FACTORY = new JsonFactory();
  
  public static JsonNode getFirstMatchingNode(JsonNode parent, String ... keys) {
    for (String key : keys) {
      JsonNode node = parent.path(key);
      if (!node.isMissingNode()) {
        return node;
      }
    }
    return MISSING_NODE;
  }
  
  public static String jsonPretty(JsonNode node) throws IOException {
    StringBuilder buf = new StringBuilder();
    JsonGenerator gen = JSON_FACTORY.createJsonGenerator(new StringBuilderWriter(buf));
    gen.useDefaultPrettyPrinter();
    gen.setCodec(JSONUtils.MAPPER);
    gen.writeTree(node);
    return buf.toString();
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
  
}
