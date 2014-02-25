package com.squarespace.template;


/**
 * Common code for Formatters and Predicates.
 */
public class Plugin {

  private final String identifier;
  
  private final boolean requiresArgs;
  
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
