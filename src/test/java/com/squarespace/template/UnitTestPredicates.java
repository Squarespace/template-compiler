package com.squarespace.template;


/**
 * Implementations to test the full range of the Predicate interface.
 */
public class UnitTestPredicates implements Registry<StringView, Predicate> {

  public static Predicate PREDICATE_INVALID_ARGS = new BasePredicate("invalid-args?", false) {
    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
     throw new ArgumentsException("Invalid arguments");
    }
  };

  public static Predicate PREDICATE_REQUIRED_ARGS = new BasePredicate("required-args?", true) {
  };
  
  public static Predicate PREDICATE_EXECUTE_ERROR = new BasePredicate("execute-error", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      throw new CodeExecuteException(ctx.error(ExecuteErrorType.DEFAULT_ERROR).name("ABCXYZ"));
    }
  };

  @Override
  public void registerTo(SymbolTable<StringView, Predicate> symbolTable) {
  
  }
  
}
