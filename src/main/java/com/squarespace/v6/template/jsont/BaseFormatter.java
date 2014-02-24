package com.squarespace.template;


/**
 * Default base class for Formatters.
 */
public abstract class BaseFormatter extends Plugin implements Formatter {

  public BaseFormatter(String identifier, boolean requiresArgs) {
    super(identifier, requiresArgs);
  }
  
  /**
   * Applies the Formatter to the context, using the given arguments which have
   * been validated and optionally converted by validateArgs(). Formatters append
   * output to the Context.
   */
  public void apply(Context ctx, Arguments args) throws CodeExecuteException {
    // NOOP
  }


}
