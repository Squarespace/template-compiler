package com.squarespace.template;


public class UnitTestFormatters extends BaseRegistry<Formatter> {

  public static Formatter INVALID_ARGS = new BaseFormatter("invalid-args", false) {
    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
      throw new ArgumentsException("Invalid arguments");
    }
  };
  
  public static Formatter REQUIRED_ARGS = new BaseFormatter("required-args", true) {
  };

  public static Formatter EXECUTE_ERROR = new BaseFormatter("execute-error", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      throw new CodeExecuteException(ctx.error(ExecuteErrorType.DEFAULT_ERROR).name("ABCXYZ"));
    }
  };
  
}
