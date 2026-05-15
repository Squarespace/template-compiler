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
   * memory growth; when full a single entry is evicted before inserting.
   */
  static int MAX_PARTIAL_CACHE = 1024;

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
   * only skips a redundant recompile. Keyed by partial name, source hash, the
   * safe/preprocess flags, and the compat level, so a compile is only reused
   * by contexts at the same level.
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
   * usable. The key covers the partial name, the source hash, the compile
   * flags and the compat level, and the stored source is compared on a hit,
   * so a changed partial of the same name always recompiles.
   */
  public Instruction getCachedPartial(String name, String source, boolean safeExecution, boolean preprocess,
      CompatLevel compat) {
    PartialEntry entry = partialCache.get(key(name, source, safeExecution, preprocess, compat));
    return entry != null && source.equals(entry.source) ? entry.inst : null;
  }

  /**
   * Caches a compiled partial for reuse by later contexts. Compile the partial
   * with the same flags and compat level the caller used; entries with
   * different modes never collide because they are part of the key. Only cache
   * error-free compiles so a cached instruction never hides syntax errors.
   */
  public void cachePartial(String name, String source, boolean safeExecution, boolean preprocess, Instruction inst,
      CompatLevel compat) {
    if (partialCache.size() >= MAX_PARTIAL_CACHE) {
      // Evict one entry rather than clearing; a miss just recompiles, so
      // concurrent evictions are harmless and the rest of the cache survives.
      for (String existing : partialCache.keySet()) {
        partialCache.remove(existing);
        break;
      }
    }
    partialCache.put(key(name, source, safeExecution, preprocess, compat), new PartialEntry(source, inst));
  }

  /**
   * Number of entries currently held in the cross-context partial cache.
   */
  int partialCacheSize() {
    return partialCache.size();
  }

  private static String key(String name, String source, boolean safeExecution, boolean preprocess, CompatLevel compat) {
    // CompatLevel has value equals/hashCode, and overrides (not just the
    // level number) affect parse-time validation, so key on the whole value.
    return name + '\0' + source.hashCode() + '\0' + safeExecution + '\0' + preprocess + '\0' + compat.hashCode();
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
