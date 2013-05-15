package com.squarespace.template;


/**
 * Base exception class.
 */
public class CodeException extends Exception {

  public CodeException(String message) {
    super(message);
  }
  
  public CodeException(String message, Throwable cause) {
    super(message, cause);
  }
  
}
