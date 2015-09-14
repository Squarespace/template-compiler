/**
 * Copyright (c) 2015 SQUARESPACE, Inc.
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


/**
 * Main compiler API.
 */
public class Compiler {

  private final FormatterTable formatterTable;

  private final PredicateTable predicateTable;

 /**
  * Since the FormatterTable and PredicateTable classes are extensible with custom
  * instances, this class accepts them as constructor arguments.  Just initialize an
  * instance of this once and use it across multiple threads in your application.
  */
  public Compiler(FormatterTable formatterTable, PredicateTable predicateTable) {
    this.formatterTable = formatterTable;
    this.predicateTable = predicateTable;
    formatterTable.setInUse();
    predicateTable.setInUse();
  }

  public FormatterTable formatterTable() {
    return formatterTable;
  }

  public PredicateTable predicateTable() {
    return predicateTable;
  }

  public CompilerExecutor newExecutor() {
    return new CompilerExecutor(this);
  }

  /**
   * Compile the template and return a wrapper containing the instructions.
   * If the compile fails an exception will be thrown.
   */
  public CompiledTemplate compile(String template) throws CodeSyntaxException {
    return compile(template, false);
  }

  /**
   * Compile the template and return a wrapper containing the instructions. Useful if you want to
   * compile a template once and execute it multiple times. The {@code safeMode} flag
   * indicates whether errors cause an exception to be thrown.
   */
  public CompiledTemplate compile(String template, boolean safeMode) throws CodeSyntaxException {
    CodeMachine machine = new CodeMachine();
    if (safeMode) {
      machine.setValidate();
    }
    Tokenizer tokenizer = new Tokenizer(template, machine, formatterTable, predicateTable);
    if (safeMode) {
      tokenizer.setValidate();
    }
    tokenizer.consume();
    List<ErrorInfo> errors = joinErrors(tokenizer.getErrors(), machine.getErrors());
    return new CompiledTemplate(machine.getCode(), errors);
  }

  /**
   * Compiles the template in validation mode, capturing all errors.
   */
  public ValidatedTemplate validate(String template) throws CodeSyntaxException {
    CodeList sink = new CodeList();
    CodeStats stats = new CodeStats();

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
    machine.complete();
    stats.complete();

    List<ErrorInfo> errors = joinErrors(tokenizer.getErrors(), machine.getErrors());
    return new ValidatedTemplate(sink, stats, errors);
  }

  private static List<ErrorInfo> joinErrors(List<ErrorInfo> parseErrors, List<ErrorInfo> compileErrors) {
    if (parseErrors.isEmpty() && compileErrors.isEmpty()) {
      return Collections.emptyList();
    }
    List<ErrorInfo> result = new ArrayList<>(parseErrors.size() + compileErrors.size());
    result.addAll(parseErrors);
    result.addAll(compileErrors);
    return result;
  }

}
