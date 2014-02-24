package com.squarespace.template;

/**
 * A predicate is a function that returns either true of false.
 *
 * NOTE: Predicate instances must be stateless and thread-safe, e.g. they
 * should only ever access read-only, thread-safe shared data.  The only exception
 * to this rule are the Context and Arguments that are passed in.
 */
public interface Predicate {

  public String getIdentifier();
  
  public boolean requiresArgs();
  
  public void validateArgs(Arguments args) throws ArgumentsException;
  
  public boolean apply(Context ctx, Arguments args) throws CodeExecuteException;

}
