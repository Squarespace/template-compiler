package com.squarespace.template;


public abstract class BasePredicate implements Predicate {

  protected String identifier;
  
  protected boolean requiresArgs;
  
  public BasePredicate(String identifier, boolean requiresArgs) {
    this.identifier = identifier;
    this.requiresArgs = requiresArgs;
  }

  /**
   * Allows the Predicate implementation to parse and syntax check its arguments
   * using the raw StringView passed in by the Tokenizer.  No state is maintained,
   * this just performs the conversion. By default it returns an empty list.
   * 
   * NOTE: if arguments == null, it means the predicate was followed by zero or 
   * more whitespace characters, e.g. no arguments were provided.
   */
  public void validateArgs(Arguments args) throws ArgumentsException {
  }
  
  public String getIdentifier() {
    return identifier;
  }
  
  public boolean requiresArgs() {
    return requiresArgs;
  }
  
  @Override
  public String toString() {
    return identifier;
  }
  
  /**
   * Applies the Predicate to the context, using the given arguments which have
   * been validated and converted by convertArgs().
   */
  public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
    return true;
  }
  
}
