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

import java.util.ArrayList;
import java.util.List;

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
import com.squarespace.template.ReprEmitter;


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

    public JsonPredicate(String identifier, boolean requiresArgs) {
      super(identifier, requiresArgs);
    }

    public abstract void limitArgs(Arguments args) throws ArgumentsException;

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getVariableNames(Arguments args) {
      List<String> names = new ArrayList<>();
      List<Object> parsed = (List<Object>) args.getOpaque();
      for (Object arg : parsed) {
        if (arg instanceof VarRef) {
          String name = ReprEmitter.get(((VarRef)arg).reference());
          names.add(name);
        }
      }
      return names;
    }

    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
      limitArgs(args);
      List<Object> parsed = new ArrayList<>();
      for (int i = 0; i < args.count(); i++) {
        parsed.add(parse(args, i));
      }
      args.setOpaque(parsed);
    };

    private Object parse(Arguments args, int index) throws ArgumentsException {
      String raw = args.get(index);
      // Attempt to decode as JSON
      try {
        return JsonUtils.decode(raw);
      } catch (IllegalArgumentException e) {
        // Fall through..
      }

      // Attempt to parse variable name.
      if (Patterns.VARIABLE.matcher(raw).matches()) {
        return new VarRef(GeneralUtils.splitVariable(raw));
      }

      throw new ArgumentsException("Argument " + raw + " must be a valid JSON value or variable reference.");
    }

    @SuppressWarnings("unchecked")
    protected JsonNode resolve(Context ctx, Arguments args, int index) {
      List<Object> parsed = (List<Object>) args.getOpaque();
      Object arg = parsed.get(index);
      if (arg instanceof VarRef) {
        VarRef ref = (VarRef)arg;
        return ctx.resolve(ref.reference());
      }
      return (JsonNode)arg;
    }

  }


  public static final Predicate EQUAL = new JsonPredicate("equal?") {

    @Override
    public void limitArgs(Arguments args) throws ArgumentsException {
      args.between(1, 2);
    }

    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode arg0 = resolve(ctx, args, 0);
      if (args.count() == 1) {
        return ctx.node().equals(arg0);
      } else {
        return arg0.equals(resolve(ctx, args, 1));
      }
    }

  };


  public static final Predicate EVEN = new JsonPredicate("even?", false) {

    @Override
    public void limitArgs(Arguments args) throws ArgumentsException {
      args.atMost(1);
    }

    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode node = ctx.node();
      if (args.count() == 1) {
        node = resolve(ctx, args, 0);
      }
      if (node.isIntegralNumber()) {
        return (node.asLong() % 2) == 0;
      }
      return false;
    }

  };


  public static final Predicate GREATER_THAN = new JsonPredicate("greaterThan?") {

    @Override
    public void limitArgs(Arguments args) throws ArgumentsException {
      args.between(1, 2);
    }

    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode arg0 = resolve(ctx, args, 0);
      if (args.count() == 1) {
        return JsonUtils.compare(ctx.node(), arg0) > 0;
      }
      return JsonUtils.compare(arg0, resolve(ctx, args, 1)) > 0;
    }

  };


  public static final Predicate GREATER_THAN_OR_EQUAL = new JsonPredicate("greaterThanOrEqual?") {

    @Override
    public void limitArgs(Arguments args) throws ArgumentsException {
      args.between(1, 2);
    }

    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode arg0 = resolve(ctx, args, 0);
      if (args.count() == 1) {
        return JsonUtils.compare(ctx.node(), arg0) >= 0;
      }
      return JsonUtils.compare(arg0, resolve(ctx, args, 1)) >= 0;
    }

  };


  public static final Predicate LESS_THAN = new JsonPredicate("lessThan?") {

    @Override
    public void limitArgs(Arguments args) throws ArgumentsException {
      args.between(1, 2);
    }

    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode arg0 = resolve(ctx, args, 0);
      if (args.count() == 1) {
        return JsonUtils.compare(ctx.node(), arg0) < 0;
      }
      return JsonUtils.compare(arg0, resolve(ctx, args, 1)) < 0;
    }

  };


  public static final Predicate LESS_THAN_OR_EQUAL = new JsonPredicate("lessThanOrEqual?") {

    @Override
    public void limitArgs(Arguments args) throws ArgumentsException {
      args.between(1, 2);
    }

    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode arg0 = resolve(ctx, args, 0);
      if (args.count() == 1) {
        return JsonUtils.compare(ctx.node(), arg0) <= 0;
      }
      return JsonUtils.compare(arg0, resolve(ctx, args, 1)) <= 0;
    }

  };


  public static final Predicate NTH = new JsonPredicate("nth?", false) {

    @Override
    public void limitArgs(Arguments args) throws ArgumentsException {
      args.between(1, 2);
    }

    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode node = ctx.node();
      JsonNode modulus = resolve(ctx, args, 0);
      if (args.count() == 2) {
        node = modulus;
        modulus = resolve(ctx, args, 1);
      }
      if (node.isIntegralNumber() && modulus.isIntegralNumber()) {
        return node.asLong() % modulus.asLong() == 0;
      }
      return false;
    };

  };


  public static final Predicate ODD = new JsonPredicate("odd?", false) {

    @Override
    public void limitArgs(Arguments args) throws ArgumentsException {
      args.atMost(1);
    }

    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode node = ctx.node();
      if (args.count() == 1) {
        node = resolve(ctx, args, 0);
      }
      if (node.isIntegralNumber()) {
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
