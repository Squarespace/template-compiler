/**
 * Copyright (c) 2014 SQUARESPACE, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.squarespace.template.plugins;

import static com.squarespace.template.GeneralUtils.isTruthy;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.BasePredicate;
import com.squarespace.template.BaseRegistry;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Context;
import com.squarespace.template.GeneralUtils;
import com.squarespace.template.JsonUtils;
import com.squarespace.template.Patterns;
import com.squarespace.template.Predicate;


public class CorePredicates extends BaseRegistry<Predicate> {

  public static final Predicate DEBUG = new BasePredicate("debug?", false) {

    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return isTruthy(ctx.resolve("debug"));
    }

  };

  private static class VarRef {

    private final Object[] reference;

    public VarRef(Object[] reference) {
      this.reference = reference;
    }

    public Object[] reference() {
      return reference;
    }

  }

  /**
   * Class of predicates that take 1 argument which is either (a) a JSON value or
   * (b) a variable reference.
   */
  private static abstract class JsonPredicate extends BasePredicate {

    public JsonPredicate(String identifier) {
      super(identifier, true);
    }

    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
      args.exactly(1);
      String raw = args.get(0);

      // Attempt to decode as JSON
      try {
        args.setOpaque(JsonUtils.decode(raw));
        return;
      } catch (IllegalArgumentException e) {
        // Fall through..
      }

      // Attempt to parse variable name.
      if (Patterns.VARIABLE.matcher(raw).matches()) {
        args.setOpaque(new VarRef(GeneralUtils.splitVariable(raw)));
        return;
      }

      throw new ArgumentsException("Argument must be a valid JSON value or variable reference.");
    };

    protected JsonNode resolve(Context ctx, Arguments args) {
      Object arg = args.getOpaque();
      if (arg instanceof VarRef) {
        VarRef ref = (VarRef)arg;
        return ctx.resolve(ref.reference());
      }
      return (JsonNode)arg;
    }

  }

  public static final Predicate EQUALS = new JsonPredicate("equals?") {

    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode expected = resolve(ctx, args);
      return ctx.node().equals(expected);
    }

  };


  public static final Predicate EVEN = new BasePredicate("even?", false) {

    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode node = ctx.node();
      if (node.isInt() || node.isLong()) {
        return (node.asLong() % 2) == 0;
      }
      return false;
    }

  };


  public static final Predicate GREATER_THAN = new JsonPredicate("greaterThan?") {

    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode expected = resolve(ctx, args);
      return JsonUtils.compare(ctx.node(), expected) > 0;
    }

  };


  public static final Predicate GREATER_THAN_OR_EQUAL = new JsonPredicate("greaterThanOrEqual?") {

    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode expected = resolve(ctx, args);
      return JsonUtils.compare(ctx.node(), expected) >= 0;
    }

  };


  public static final Predicate LESS_THAN = new JsonPredicate("lessThan?") {

    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode expected = resolve(ctx, args);
      return JsonUtils.compare(ctx.node(), expected) < 0;
    }

  };


  public static final Predicate LESS_THAN_OR_EQUAL = new JsonPredicate("lessThanOrEqual?") {

    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode expected = resolve(ctx, args);
      return JsonUtils.compare(ctx.node(), expected) <= 0;
    }

  };


  public static final Predicate ODD = new BasePredicate("odd?", false) {

    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode node = ctx.node();
      if (node.isInt() || node.isLong()) {
        return (node.asLong() % 2) != 0;
      }
      return false;
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
