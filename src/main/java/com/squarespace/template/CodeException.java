package com.squarespace.template;


/**
 * Base exception class with associated error info.
 */
public class CodeException extends Exception {

  public CodeException(String temp) {
    super(temp);
  }
  
}
