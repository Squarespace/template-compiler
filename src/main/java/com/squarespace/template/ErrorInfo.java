package com.squarespace.template;

/**
 * Provides a class to capture state about the error, prior to constructing the 
 * exception itself. Lets us pass this object around to various places if necessary
 * before wrapping it in a CodeSyntaxException.
 */
public class ErrorInfo<T extends ErrorType> {

  private static final String CODE = "code";

  private static final String LINE = "line";

  private static final String OFFSET = "offset";

  private static final String TYPE = "type";
  
  private static final String DATA = "data";
  
  private static final String NAME = "name";
  
  private static final String LIMIT = "limit";

  private T errorType;
  
  private MapBuilder<String, Object> builder = new MapBuilder<>();
  
  public ErrorInfo(T type) {
    this.errorType = type;
  }

  public ErrorInfo<T> code(Object code) {
    builder.put(CODE, code);
    return this;
  }

  public ErrorInfo<T> line(int line) {
    builder.put(LINE, line);
    return this;
  }
  
  public ErrorInfo<T> offset(int offset) {
    builder.put(OFFSET, offset);
    return this;
  }

  public ErrorInfo<T> type(Object type) {
    builder.put(TYPE, type);
    return this;
  }
  
  public ErrorInfo<T> data(Object data) {
    builder.put(DATA, data);
    return this;
  }

  public ErrorInfo<T> name(Object name) {
    builder.put(NAME, name);
    return this;
  }
  
  public ErrorInfo<T> limit(Object limit) {
    builder.put(LIMIT, limit);
    return this;
  }

  public T getErrorType() {
    return errorType;
  }

  public String getMessage() {
    return errorType.format(builder.get());
  }
  
}
