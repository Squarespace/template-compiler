package com.squarespace.template;


public interface Predicate {

  public String getIdentifier();
  
  public boolean requiresArgs();
  
  public void validateArgs(Arguments args) throws ArgumentsException;
  
  public boolean apply(Context ctx, Arguments args) throws CodeExecuteException;

}
