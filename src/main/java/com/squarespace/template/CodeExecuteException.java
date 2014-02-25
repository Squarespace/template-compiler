package com.squarespace.template;


/**
 * Represents any error that occurs during execution of a compiled template.
 */
public class CodeExecuteException extends CodeException {

  private final ErrorInfo errorInfo;
  
  public CodeExecuteException(ErrorInfo info) {
    super(info.getMessage());
    this.errorInfo = info;
  }
  
  public CodeExecuteException(ErrorInfo info, Throwable cause) {
    super(info.getMessage(), cause);
    this.errorInfo = info;
  }

  public ErrorInfo getErrorInfo() {
    return errorInfo;
  }
  
}
