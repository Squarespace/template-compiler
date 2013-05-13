package com.squarespace.template;


/**
 * Implementations to test the Predicate interface.
 */
public class UnitTestPredicates extends BaseRegistry<Predicate> {

  public static Predicate INVALID_ARGS = new BasePredicate("invalid-args?", false) {
    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
     throw new ArgumentsException("Invalid arguments");
    }
  };

  public static Predicate REQUIRED_ARGS = new BasePredicate("required-args?", true) {
  };
  
  public static Predicate EXECUTE_ERROR = new BasePredicate("execute-error", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      throw new CodeExecuteException(ctx.error(ExecuteErrorType.GENERAL_ERROR).name("ABCXYZ"));
    }
  };
  
}
