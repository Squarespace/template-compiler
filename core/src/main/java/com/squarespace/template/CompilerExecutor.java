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

import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * Helper class for the compiler to execute a template.
 */
public class CompilerExecutor {

  private final Compiler compiler;
  private String template;
  private Instruction rootInstruction;
  private JsonNode rootNode;
  private ObjectNode partialsMap;
  private ObjectNode injectablesMap;
  private StringBuilder buffer;
  private Locale locale;
  private LoggingHook loggingHook;
  private CodeLimiter codeLimiter;
  private boolean safeExecution;
  private boolean preprocess;
  private int maxPartialDepth = Constants.DEFAULT_MAX_PARTIAL_DEPTH;

  CompilerExecutor(Compiler compiler) {
    this.compiler = compiler;
  }

  /**
   * Constructs a context, executes the instruction/template, and returns the
   * context that was used.
   */
  public Context execute() throws CodeException {
    Context ctx = new Context(rootNode, buffer, locale);
    Instruction instruction = rootInstruction;
    if (instruction == null) {
      template = template == null ? "" : template;
      CompiledTemplate compiled = compiler.compile(template, safeExecution, preprocess);
      for (ErrorInfo error : compiled.errors()) {
        ctx.addError(error);
      }
      instruction = compiled.code();
    }

    ctx.setCompiler(compiler);
    if (partialsMap != null) {
      ctx.setPartials(partialsMap);
    }
    if (injectablesMap != null) {
      ctx.setInjectables(injectablesMap);
    }
    if (loggingHook != null) {
      ctx.setLoggingHook(loggingHook);
    }
    if (safeExecution) {
      ctx.setSafeExecution();
    }
    if (codeLimiter != null) {
      ctx.setCodeLimiter(codeLimiter);
    }
    if (locale != null) {
      ctx.javaLocale(locale);
    }

    ctx.setMaxPartialDepth(maxPartialDepth);
    ctx.execute(instruction);
    return ctx;
  }

  /**
   * Template to compile and execute.  This is only used if an instruction
   * is not already set.
   */
  public CompilerExecutor template(String template) {
    this.template = template;
    return this;
  }

  /**
   * Sets the instruction to execute.
   */
  public CompilerExecutor code(Instruction instruction) {
    this.rootInstruction = instruction;
    return this;
  }

  /**
   * Sets the JSON node to execute the template against.
   */
  public CompilerExecutor json(JsonNode node) {
    this.rootNode = node;
    return this;
  }

  /**
   * Sets the JSON node to execute the template against by parsing
   * the given text as JSON.
   */
  public CompilerExecutor json(String jsonText) {
    this.rootNode = JsonUtils.decode(jsonText);
    return this;
  }

  /**
   * Sets the partials map, which is a map from name to template.
   */
  public CompilerExecutor partialsMap(ObjectNode node) {
    this.partialsMap = node;
    return this;
  }

  /**
   * Sets the partials map by parsing the given text as a JSON object.
   */
  public CompilerExecutor partialsMap(String jsonText) {
    this.partialsMap = (ObjectNode)JsonUtils.decode(jsonText);
    return this;
  }

  /**
   * Sets the injectables map, which is a map from name to JSON string.
   */
  public CompilerExecutor injectablesMap(ObjectNode node) {
    this.injectablesMap = node;
    return this;
  }

  /**
   * Sets the injectables map by parsing JSON text.
   */
  public CompilerExecutor injectablesMap(String jsonText) {
    this.injectablesMap = (ObjectNode)JsonUtils.decode(jsonText);
    return this;
  }

  /**
   * Buffer to which we append the rendered output.
   */
  public CompilerExecutor buffer(StringBuilder buffer) {
    this.buffer = buffer;
    return this;
  }

  /**
   * Locale to execute against, used by some formatters.
   */
  public CompilerExecutor locale(Locale locale) {
    this.locale = locale;
    return this;
  }

  /**
   * Hook for logging exceptions that occur during execution.
   */
  public CompilerExecutor loggingHook(LoggingHook loggingHook) {
    this.loggingHook = loggingHook;
    return this;
  }

  /**
   * Adds a code limiter to monitor execution and enforce an
   * instruction count limit.
   */
  public CompilerExecutor codeLimiter(CodeLimiter codeLimiter) {
    this.codeLimiter = codeLimiter;
    return this;
  }

  /**
   * Puts the compiler in safe execution mode.
   */
  public CompilerExecutor safeExecution(boolean safeExecution) {
    this.safeExecution = safeExecution;
    return this;
  }

  /**
   * Put the compiler into pre-process mode.
   */
  public CompilerExecutor preprocess(boolean preprocess) {
    this.preprocess = preprocess;
    return this;
  }

  /**
   * Sets the maximum partial nesting depth.
   */
  public CompilerExecutor maxPartialDepth(int depth) {
    this.maxPartialDepth = depth;
    return this;
  }

}