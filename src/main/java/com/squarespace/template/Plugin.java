package com.squarespace.template;


/**
 * Common code for Formatters and Predicates.
 */
public class Plugin {

  private String identifier;
  
  private boolean requiresArgs;
  
  public Plugin(String identifier, boolean requiresArgs) {
    this.identifier = identifier;
    this.requiresArgs = requiresArgs;
  }
  
  public String getIdentifier() {
    return identifier;
  }
  
  public boolean requiresArgs() {
    return requiresArgs;
  }
  
  public void fail(ErrorInfo info) throws CodeExecuteException {
    throw new CodeExecuteException(info);
  }
  
  /**
   * Perform all validation of arguments passed to the Plugin, and also
   * perform any necessary conversion. Store converted args as an opaque
   * object, 
   * 
   * @see Arguments#setOpaque(Object)
   */
  public void validateArgs(Arguments args) throws ArgumentsException {
    // NOOP
  }
 
  @Override
  public String toString() {
    return identifier;
  }

}
