package com.squarespace.template;



/**
 * Represents any error that occurs during compilation of a raw template string.
 */
public class CodeSyntaxException extends CodeException {

  private ErrorInfo errorInfo;
  
  public CodeSyntaxException(ErrorInfo info) {
    super(info.getMessage());
    this.errorInfo = info;
  }

  public ErrorInfo getError() {
    return errorInfo;
  }
  
}
