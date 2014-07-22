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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;


/**
 * JSON-Template compiler and execution engine.
 */
public class JsonTemplateEngine {

  private final FormatterTable formatterTable;

  private final PredicateTable predicateTable;

  private LoggingHook loggingHook;

  /**
  * Since the FormatterTable and PredicateTable classes are extensible with custom
  * instances, this class accepts them as constructor arguments.  Just initialize an
  * instance of this once and use it across multiple threads in your application.
  */
  public JsonTemplateEngine(FormatterTable formatterTable, PredicateTable predicateTable) {
    this.formatterTable = formatterTable;
    this.predicateTable = predicateTable;
    formatterTable.setInUse();
    predicateTable.setInUse();
  }

  public void setLoggingHook(LoggingHook hook) {
    this.loggingHook = hook;
  }

  /**
   * Execute the instruction against the JSON node.
   */
  public Context execute(Instruction instruction, JsonNode json) throws CodeExecuteException {
    return executeWithPartials(instruction, json, null, new StringBuilder());
  }

  /**
   * Execute the instruction against the JSON node, appending the output to buffer.
   */
  public Context execute(Instruction instruction, JsonNode json, StringBuilder buf) throws CodeExecuteException {
    return executeWithPartials(instruction, json, null, buf);
  }

  /**
   * Execute the instruction against the given JSON node, using the partial template map.
   */
  public Context execute(Instruction instruction, JsonNode json, JsonNode partials)
      throws CodeExecuteException {
    return executeWithPartials(instruction, json, partials, new StringBuilder());
  }

  public Context executeSafe(Instruction instruction, JsonNode json, JsonNode partials) throws CodeExecuteException {
    return executeWithPartialsSafe(instruction, json, partials, new StringBuilder());
  }

  /**
   * Execute the instruction against the JSON node, using the partial template map, and append
   * the output to buffer.
   */
  public Context executeWithPartials(Instruction instruction, JsonNode json, JsonNode partials, StringBuilder buf)
      throws CodeExecuteException {

    Context ctx = new Context(json, buf);
    ctx.setCompiler(this);
    ctx.setPartials(partials);
    ctx.setLoggingHook(loggingHook);
    instruction.invoke(ctx);
    return ctx;
  }

  public Context executeWithPartialsSafe(Instruction instruction, JsonNode json, JsonNode partials, StringBuilder buf)
      throws CodeExecuteException {

    Context ctx = new Context(json, buf);
    ctx.setCompiler(this);
    ctx.setPartials(partials);
    ctx.setLoggingHook(loggingHook);
    ctx.setSafeExecution();
    instruction.invoke(ctx);
    return ctx;
  }

  /**
   * Compile the template and return a wrapper containing the instructions. Useful if you want to
   * compile a template once and execute it multiple times.
   */
  public CompiledTemplate compile(String template) throws CodeSyntaxException {
    final CodeMachine machine = new CodeMachine();
    Tokenizer tokenizer = new Tokenizer(template, machine, formatterTable, predicateTable);
    tokenizer.consume();
    return new CompiledTemplate() {
      @Override
      public Instruction code() {
        return machine.getCode();
      }
      @Override
      public List<ErrorInfo> errors() {
        return Collections.emptyList();
      }
      @Override
      public CodeMachine machine() {
        return machine;
      }
    };
  }

  public CompiledTemplate compileSafe(String template) throws CodeSyntaxException {
    final CodeMachine machine = new CodeMachine();
    machine.setValidate();
    Tokenizer tokenizer = new Tokenizer(template, machine, formatterTable, predicateTable);
    tokenizer.setValidate();
    tokenizer.consume();
    final List<ErrorInfo> errors = joinErrors(tokenizer.getErrors(), machine.getErrors());

    return new CompiledTemplate() {
      @Override
      public Instruction code() {
        return machine.getCode();
      }
      @Override
      public List<ErrorInfo> errors() {
        return errors;
      }
      @Override
      public CodeMachine machine() {
        return machine;
      }
    };
  }

  /**
   * Compiles the template in validation mode, capturing all errors.
   */
  public ValidatedTemplate validate(String template) throws CodeSyntaxException {
    final CodeList sink = new CodeList();
    final CodeStats stats = new CodeStats();

    // Validate the template at the syntax level.
    Tokenizer tokenizer = new Tokenizer(template, sink, formatterTable, predicateTable);
    tokenizer.setValidate();
    tokenizer.consume();

    // Pass the parsed instructions to the CodeMachine for structural validation, and
    // collect some stats.
    CodeMachine machine = new CodeMachine();
    machine.setValidate();
    for (Instruction inst : sink.getInstructions()) {
      machine.accept(inst);
      stats.accept(inst);
    }

    final List<ErrorInfo> errors = joinErrors(tokenizer.getErrors(), machine.getErrors());

    // Return all of the validation objects for the template.
    return new ValidatedTemplate() {
      @Override
      public CodeList code() {
        return sink;
      }
      @Override
      public List<ErrorInfo> errors() {
        return errors;
      }
      @Override
      public CodeStats stats() {
        return stats;
      }
    };
  }

  private static List<ErrorInfo> joinErrors(List<ErrorInfo> parseErrors, List<ErrorInfo> compileErrors) {
    if (!parseErrors.isEmpty() || !compileErrors.isEmpty()) {
      ArrayList<ErrorInfo> result = new ArrayList<>(parseErrors.size() + compileErrors.size());
      result.addAll(parseErrors);
      result.addAll(compileErrors);
      return result;
    }
    return Collections.emptyList();
  }

}
