package com.squarespace.template;

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

  private ErrorType type;
  
  private String repr;
  
  private MapBuilder<String, Object> builder = new MapBuilder<>();
  
  public ErrorInfo(ErrorType type) {
    this.type = type;
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

  public MapBuilder<String, Object> getBuilder() {
    return builder;
  }
  
  public ErrorType getType() {
    return type;
  }

  public String getMessage() {
    if (repr == null) {
      repr = type.format(builder.get());
    }
    return repr;
  }
  
}
