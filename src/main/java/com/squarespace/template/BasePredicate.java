package com.squarespace.template;


/**
 * Default base class for Predicates.
 */
public abstract class BasePredicate extends Plugin implements Predicate {

  public BasePredicate(String identifier, boolean requiresArgs) {
    super(validateIdentifier(identifier), requiresArgs);
  }

  private static String validateIdentifier(String identifier) {
    if (!identifier.endsWith("?")) {
      throw new IllegalArgumentException("All predicates must end with '?'");
    }
    return identifier;
  }
  
  /**
   * Applies the Predicate to the context, using the given arguments which have
   * been validated and optionally converted by validateArgs(). Predicates do 
   * nothing but return a boolean value -- they do not append output to the Context.
   * 
   * TODO: actively prevent Predicates from appending output to Context?
   */
  public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
    return true;
  }

}
