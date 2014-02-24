package com.squarespace.template.plugins;

import static com.squarespace.template.GeneralUtils.isTruthy;

import com.squarespace.template.Arguments;
import com.squarespace.template.BasePredicate;
import com.squarespace.template.BaseRegistry;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Context;
import com.squarespace.template.Predicate;


public class CorePredicates extends BaseRegistry<Predicate> {

  public static final Predicate DEBUG = new BasePredicate("debug?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return isTruthy(ctx.resolve("debug"));
    }
  };

  
  public static final Predicate PLURAL = new BasePredicate("plural?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return ctx.node().asLong() > 1;
    }
  };

  
  public static final Predicate SINGULAR = new BasePredicate("singular?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return ctx.node().asLong() == 1;
    }
  };

  
}
