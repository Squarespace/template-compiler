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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.squarespace.template.compat.CompatLevel;


/**
 * Main compiler API.
 */
public class Compiler {

  private final FormatterTable formatterTable;

  private final PredicateTable predicateTable;

  /**
   * Max entries in the cross-context partial cache. Bounded to avoid unbounded
   * memory growth; when full the whole cache is cleared (simple policy).
   */
  static final int MAX_PARTIAL_CACHE = 1024;

  /**
   * A compiled partial kept for reuse across contexts. Holds the source text
   * next to the instruction so a hash collision can never serve a stale
   * compile.
   */
  private static class PartialEntry {
    final String source;
    final Instruction inst;

    PartialEntry(String source, Instruction inst) {
      this.source = source;
      this.inst = inst;
    }
  }

  /**
   * Cross-context cache of compiled partials. Never the source of truth: a
   * context always compiles on a miss and stores in its own map; this cache
   * only skips a redundant recompile. Keyed by partial name, source hash, and
   * the safe/preprocess flags so differing compile modes never collide.
   *
   * The key omits the compat level because partials are always compiled at
   * the default level. If that changes, add the level to the key or a level
   * mismatch will serve a stale compile.
   */
  private final Map<String, PartialEntry> partialCache = new ConcurrentHashMap<>();

 /**
  * Since the FormatterTable and PredicateTable classes are extensible with custom
  * instances, this class accepts them as constructor arguments.  Just initialize an
  * instance of this once and use it across multiple threads in your application.
  */
  public Compiler(FormatterTable formatterTable, PredicateTable predicateTable) {
    this.formatterTable = formatterTable;
    this.predicateTable = predicateTable;
    formatterTable.initialize(this);
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
   * Returns a previously cached compile of this partial, or null if none is
   * usable. The key covers the partial name, the source hash and the compile
   * flags, and the stored source is compared on a hit, so a changed partial
   * of the same name always recompiles.
   */
  public Instruction getCachedPartial(String name, String source, boolean safeExecution, boolean preprocess) {
    PartialEntry entry = partialCache.get(key(name, source, safeExecution, preprocess));
    return entry != null && source.equals(entry.source) ? entry.inst : null;
  }

  /**
   * Caches a compiled partial for reuse by later contexts. Compile the partial
   * with the same flags the caller used; entries with different flags never
   * collide because the flags are part of the key. Only cache error-free
   * compiles so a cached instruction never hides syntax errors.
   */
  public void cachePartial(String name, String source, boolean safeExecution, boolean preprocess, Instruction inst) {
    if (partialCache.size() >= MAX_PARTIAL_CACHE) {
      partialCache.clear();
    }
    partialCache.put(key(name, source, safeExecution, preprocess), new PartialEntry(source, inst));
  }

  private static String key(String name, String source, boolean safeExecution, boolean preprocess) {
    return name + '\0' + source.hashCode() + '\0' + safeExecution + '\0' + preprocess;
  }

  /**
   * Compile the template and return a wrapper containing the instructions.
   * If the compile fails an exception will be thrown.
   */
  public CompiledTemplate compile(String template) throws CodeSyntaxException {
    return compile(template, false, false);
  }

  /**
   * Compile the template and return a wrapper containing the instructions. Useful if you want to
   * compile a template once and execute it multiple times. The {@code safeMode} flag
   * indicates whether errors cause an exception to be thrown.
   */
  public CompiledTemplate compile(String template, boolean safeMode, boolean preprocess) throws CodeSyntaxException {
    return compile(template, safeMode, preprocess, CompatLevel.defaultLevel());
  }

  /**
   * Compile the template at the given compatibility level. The level
   * selects which legacy behaviors stay active. Existing calls keep the
   * released behavior at level 0.
   */
  public CompiledTemplate compile(String template, boolean safeMode, boolean preprocess, CompatLevel compat)
      throws CodeSyntaxException {
    CodeMachine machine = new CodeMachine();
    if (safeMode) {
      machine.setValidate();
    }
    Tokenizer tokenizer = new Tokenizer(template, machine, preprocess, formatterTable, predicateTable);
    tokenizer.setCompat(compat);
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
