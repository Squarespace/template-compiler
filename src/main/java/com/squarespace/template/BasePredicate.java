package com.squarespace.template;


public abstract class BasePredicate implements Predicate {

  protected String identifier;
  
  protected boolean requiresArgs;
  
  public BasePredicate(String identifier, boolean requiresArgs) {
    this.identifier = identifier;
    this.requiresArgs = requiresArgs;
  }

  public String getIdentifier() {
    return identifier;
  }
  
  public boolean requiresArgs() {
    return requiresArgs;
  }
  
  public void validateArgs(Arguments args) throws ArgumentsException {
    // NOOP
  }
  
  /**
   * Applies the Predicate to the context, using the given arguments which have
   * been validated and converted by convertArgs().
   */
  public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
    return true;
  }

  @Override
  public String toString() {
    return identifier;
  }

}
