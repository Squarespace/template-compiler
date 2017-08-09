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
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Context;
import com.squarespace.template.GeneralUtils;
import com.squarespace.template.JsonUtils;
import com.squarespace.template.Patterns;
import com.squarespace.template.Predicate;
import com.squarespace.template.PredicateRegistry;
import com.squarespace.template.ReferenceScanner.References;
import com.squarespace.template.ReprEmitter;
import com.squarespace.template.StringView;
import com.squarespace.template.SymbolTable;
import com.squarespace.template.VariableRef;


public class CorePredicates implements PredicateRegistry {

  /**
   * Registers the active predicates in this registry.
   */
  @Override
  public void registerPredicates(SymbolTable<StringView, Predicate> table) {
    table.add(DEBUG);
    table.add(EQUAL);
    table.add(EVEN);
    table.add(GREATER_THAN);
    table.add(GREATER_THAN_OR_EQUAL);
    table.add(LESS_THAN);
    table.add(LESS_THAN_OR_EQUAL);
    table.add(NOT_EQUAL);
    table.add(NTH);
    table.add(ODD);
    table.add(PLURAL);
    table.add(SINGULAR);
  };

  public static final Predicate DEBUG = new BasePredicate("debug?", false) {

    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return isTruthy(ctx.resolve("debug"));
    }

  };

  /**
   * Class of predicates that take 1 argument which is either (a) a JSON value or
   * (b) a variable reference.
   */
  private static abstract class JsonPredicate extends BasePredicate {

    JsonPredicate(String identifier) {
      super(identifier, true);
    }

    JsonPredicate(String identifier, boolean requiresArgs) {
      super(identifier, requiresArgs);
    }

    public abstract void limitArgs(Arguments args) throws ArgumentsException;

    @Override
    public void addReferences(Arguments args, References refs) {
      addVariableNames(args, refs);
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
      // Peek at content to see if its JSON-like. This will cut down on the
      // number of failed JSON parse attempts.
      if (GeneralUtils.isJsonStart(raw)) {
        JsonNode result = JsonUtils.decode(raw, true);
        if (!result.isMissingNode()) {
          return result;
        }
      }

      // Attempt to parse variable name.
      int length = raw.length();
      if (Patterns.VARIABLE_REF_DOTTED.match(raw, 0, length) != -1) {
        return new VariableRef(raw);
      }

      throw new ArgumentsException("Argument " + raw + " must be a valid JSON value or variable reference.");
    }

    @SuppressWarnings("unchecked")
    protected JsonNode resolve(Context ctx, Arguments args, int index) {
      List<Object> parsed = (List<Object>) args.getOpaque();
      Object arg = parsed.get(index);
      if (arg instanceof VariableRef) {
        VariableRef ref = (VariableRef)arg;
        return ctx.resolve(ref.reference());
      }
      return (JsonNode)arg;
    }

    @SuppressWarnings("unchecked")
    private void addVariableNames(Arguments args, References refs) {
      List<Object> parsed = (List<Object>) args.getOpaque();
      for (Object arg : parsed) {
        if (arg instanceof VariableRef) {
          String name = ReprEmitter.get(((VariableRef)arg).reference());
          refs.addVariable(name);
        }
      }
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


  public static final Predicate NOT_EQUAL = new JsonPredicate("notEqual?") {

    @Override
    public void limitArgs(Arguments args) throws ArgumentsException {
      args.between(1, 2);
    }

    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode arg0 = resolve(ctx, args, 0);
      if (args.count() == 1) {
        return !ctx.node().equals(arg0);
      } else {
        return !arg0.equals(resolve(ctx, args, 1));
      }
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
    }

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
