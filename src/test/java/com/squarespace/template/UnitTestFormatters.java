package com.squarespace.template;

import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.BaseFormatter;
import com.squarespace.template.BaseRegistry;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Context;
import com.squarespace.template.ExecuteErrorType;
import com.squarespace.template.Formatter;



/**
 * Implementations to test the Formatter interface.
 */
public class UnitTestFormatters extends BaseRegistry<Formatter> {

  public static Formatter EXECUTE_ERROR = new BaseFormatter("execute-error", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      throw new CodeExecuteException(ctx.error(ExecuteErrorType.GENERAL_ERROR).name("ABCXYZ"));
    }
  };
  
  public static Formatter INVALID_ARGS = new BaseFormatter("invalid-args", false) {
    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
      throw new ArgumentsException("Invalid arguments");
    }
  };
  
  public static Formatter NPE = new BaseFormatter("npe", false) {

    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      throw new NullPointerException("fake NPE thrown by the test npe formatter.");
    }
  };
  
  public static Formatter REQUIRED_ARGS = new BaseFormatter("required-args", true) {
  };

  public static Formatter UNSTABLE = new BaseFormatter("unstable", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      throw new IllegalArgumentException("unexpected error!");
    }
  };
  
}
