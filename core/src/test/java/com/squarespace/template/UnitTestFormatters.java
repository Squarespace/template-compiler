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

package com.squarespace.template;

import static com.squarespace.template.GeneralUtils.executeTemplate;



/**
 * Implementations to test the Formatter interface.
 */
public class UnitTestFormatters implements FormatterRegistry {

  @Override
  public void registerFormatters(SymbolTable<StringView, Formatter> table) {
    table.add(new DummyFormatter());
    table.add(new DummyTemplateFormatter());
    table.add(new ExecuteErrorFormatter());
    table.add(new InvalidArgsFormatter());
    table.add(new NpeFormatter());
    table.add(new RequiredArgsFormatter());
    table.add(new ReturnsMissingFormatter());
    table.add(new UnstableFormatter());
  }

  public static class DummyFormatter extends BaseFormatter {

    public DummyFormatter() {
      super("dummy", false);
    }

  }

  /**
   * Example formatter to demonstrate initialization-time compilation and
   * application of a partial template.
   */
  public static class DummyTemplateFormatter extends BaseFormatter {

    private Instruction instruction;

    public DummyTemplateFormatter() {
      super("dummy-template", false);
    }

    @Override
    public void initialize(Compiler compiler) throws CodeException {
      this.instruction = compiler.compile("<div>{bar}</div>").code();
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      var.set(executeTemplate(ctx, instruction, var.node().path("foo"), true));
    }
  }

  public static class ExecuteErrorFormatter extends BaseFormatter {

    public ExecuteErrorFormatter() {
      super("execute-error", false);
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      throw new CodeExecuteException(ctx.error(ExecuteErrorType.GENERAL_ERROR).name("ABCXYZ"));
    }

  }

  public static class InvalidArgsFormatter extends BaseFormatter {

    public InvalidArgsFormatter() {
      super("invalid-args", false);
    }

    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
      throw new ArgumentsException("Invalid arguments");
    }

  }

  public static class NpeFormatter extends BaseFormatter {

    public NpeFormatter() {
      super("npe", false);
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      throw new NullPointerException("fake NPE thrown by the test npe formatter.");
    }

  }

  public static class RequiredArgsFormatter extends BaseFormatter {

    public RequiredArgsFormatter() {
      super("required-args", true);
    }

  }

  public static class ReturnsMissingFormatter extends BaseFormatter {

    public ReturnsMissingFormatter() {
      super("returns-missing", false);
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      variables.first().setMissing();
    }

  }

  public static class UnstableFormatter extends BaseFormatter {

    public UnstableFormatter() {
      super("unstable", false);
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      throw new IllegalArgumentException("unexpected error!");
    }

  }

}
