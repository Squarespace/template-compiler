package com.squarespace.template;


/**
 * A Formatter is a function that examines a value and emits a string. Formatters
 * can have zero or more arguments.
 * 
 * NOTE: Formatter instances must be stateless and thread-safe, e.g. they
 * should only ever access read-only, thread-safe shared data.  The only exception
 * to this rule are the Context and Arguments that are passed in.
 */
public interface Formatter {

  public String getIdentifier();
  
  public boolean requiresArgs();
  
  /**
   * For Formatters that take arguments, they will be called with the raw StringView
   * covering their arguments during the tokenization phase. The Formatter must
   * raise a syntax error if the arguments are invalid.
   * 
   * For example, for a DATE formatter that takes a formatting string, an invalid
   * format escape should be caught in this method and an error thrown.
   */
  public void validateArgs(Arguments args) throws ArgumentsException;

  /**
   * During execution of the template, the Formatter will be called with a Context
   * instance and the arguments that were returned by convertArgs().  It is the
   * Formatter's responsibility to emit output using the context. If any 
   * runtime errors occur that are severe, the Formatter can throw an exception.
   */
  public void apply(Context ctx, Arguments args) throws CodeExecuteException;
  
}
