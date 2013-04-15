package com.squarespace.template;


/**
 * Represents any error that occurs during execution of a compiled template.
 */
public class CodeExecuteException extends CodeException {

  private ErrorInfo<ExecuteErrorType> errorInfo;
  
  public CodeExecuteException(ErrorInfo<ExecuteErrorType> info) {
    super(info.getMessage());
    this.errorInfo = info;
  }

  public ErrorInfo<ExecuteErrorType> getErrorInfo() {
    return errorInfo;
  }
  
}
