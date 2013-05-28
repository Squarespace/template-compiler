package com.squarespace.template;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squarespace.v6.utils.JSONUtils;

/**
 * Provides a class to capture state about the error, prior to constructing the 
 * exception itself. Lets us pass this object around to various places if necessary
 * before wrapping it in a CodeSyntaxException.
 * 
 * The methods are named for the small set of keys used to do pattern substitution
 * in the error messages.  The methods allow more compact code, allow call
 * chaining, and to reduce typos when specifying key names, e.g. info.put("ofset", ...)
 */
public class ErrorInfo {

  private static final String CODE = "code";

  private static final String LINE = "line";

  private static final String OFFSET = "offset";

  private static final String TYPE = "type";
  
  private static final String DATA = "data";
  
  private static final String NAME = "name";
  
  private static final String LIMIT = "limit";

  private static final String REPR = "repr";
  
  private ErrorType type;
  
  private ErrorLevel level;
  
  private MapBuilder<String, Object> builder = new MapBuilder<>();
  
  public ErrorInfo(ErrorType type) {
    this(type, ErrorLevel.ERROR);
  }

  public ErrorInfo(ErrorType type, ErrorLevel level) {
    this.type = type;
    this.level = level;
  }
  
  public ErrorInfo code(Object code) {
    builder.put(CODE, code);
    return this;
  }

  public ErrorInfo line(int line) {
    builder.put(LINE, line);
    return this;
  }
  
  public ErrorInfo offset(int offset) {
    builder.put(OFFSET, offset);
    return this;
  }

  public ErrorInfo type(Object type) {
    builder.put(TYPE, type);
    return this;
  }
  
  public ErrorInfo data(Object data) {
    builder.put(DATA, data);
    return this;
  }

  public ErrorInfo name(Object name) {
    builder.put(NAME, name);
    return this;
  }
  
  public ErrorInfo limit(Object limit) {
    builder.put(LIMIT, limit);
    return this;
  }

  public ErrorInfo repr(String repr) {
    builder.put(REPR, repr);
    return this;
  }
  
  public MapBuilder<String, Object> getBuilder() {
    return builder;
  }
  
  public ErrorType getType() {
    return type;
  }
  
  public ErrorLevel getLevel() {
    return level;
  }

  public String getMessage() {
    Map<String, Object> params = builder.get();
    return type.prefix(params) + ": " + type.message(params);
  }
  
  public JsonNode toJson() {
    Map<String, Object> map = builder.get();
    ObjectNode obj = JSONUtils.createObjectNode();
    obj.put("level", level.toString());
    Integer line = (Integer)map.get(LINE);
    obj.put("line", (line == null) ? 0 : line);
    Integer offset = (Integer)map.get(OFFSET);
    obj.put("offset", (offset == null) ? 0 : offset);
    obj.put("type", type.toString());
    obj.put("prefix", type.prefix(map));
    obj.put("message", type.message(map));
    return obj;
  }
  
}
