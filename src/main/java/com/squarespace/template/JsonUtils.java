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

}
