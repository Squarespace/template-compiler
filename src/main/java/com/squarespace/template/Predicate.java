package com.squarespace.template;


public interface Predicate {

  public String getIdentifier();
  
  public boolean requiresArgs();
  
  public void validateArgs(Arguments args) throws ArgumentsException;
  
//  public List<String> convertArgs(StringView arguments) throws CodeSyntaxException;
  
  public boolean apply(Context ctx, Arguments args) throws CodeExecuteException;

}
