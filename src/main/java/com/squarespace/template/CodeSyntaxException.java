package com.squarespace.template;



/**
 * Represents any error that occurs during compilation of a template.
 */
public class CodeSyntaxException extends CodeException {

  private final ErrorInfo errorInfo;
  
  public CodeSyntaxException(ErrorInfo info) {
    super(info.getMessage());
    this.errorInfo = info;
  }

  public ErrorInfo getErrorInfo() {
    return errorInfo;
  }
  
}
