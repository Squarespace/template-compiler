package com.squarespace.template;


/**
 * Represents any error that occurs during compilation of a raw template string.
 */
public class CodeSyntaxException extends CodeException {

  private ErrorInfo<SyntaxErrorType> errorInfo;
  
  public CodeSyntaxException(ErrorInfo<SyntaxErrorType> info) {
    super(info.getMessage());
    errorInfo = info;
  }
  
  public ErrorInfo<SyntaxErrorType> getErrorInfo() {
    return errorInfo;
  }
  
}
