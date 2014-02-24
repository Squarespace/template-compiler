package com.squarespace.template.plugins;

import static com.squarespace.template.GeneralUtils.isTruthy;

import org.joda.time.DateTimeZone;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.v6.utils.enums.CollectionType;


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
