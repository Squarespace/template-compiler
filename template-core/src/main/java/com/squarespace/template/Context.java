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

import static com.squarespace.template.ExecuteErrorType.UNEXPECTED_ERROR;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.TextNode;


/**
 * Tracks all of the state needed for executing a template against a given JSON tree.
 *
 * Compilation converts the raw text into an instruction tree. The instruction tree
 * is stateless and can be reused across multiple executions.
 *
 * The Context is used to carry out a single execution of the template instruction tree.
 * Each execution of a template requires a fresh context object.
 */
public class Context {

  private static final JsonNode DEFAULT_UNDEFINED = MissingNode.getInstance();

  private static final String META_LEFT = "{";

  private static final String META_RIGHT = "}";

  private final ArrayDeque<Frame> stack = new ArrayDeque<>();

  private final Locale locale;

  private Frame currentFrame;

  private JsonNode undefined = DEFAULT_UNDEFINED;

  private boolean safeExecution = false;

  private List<ErrorInfo> errors;

  /**
   * Reference to the currently-executing instruction. All instruction execution
   * must pass control via the Context, for proper error handling.
   */
  private Instruction currentInstruction;

  private JsonNode rawPartials;

  private Map<String, Instruction> compiledPartials;

  private JsonTemplateEngine compiler;

  private LoggingHook loggingHook;

  private CodeLimiter codeLimiter = new NoopCodeLimiter();

  /* Holds the final output of the template execution */
  private StringBuilder buf;

  public Context(JsonNode node) {
    this(node, new StringBuilder(), Locale.getDefault());
  }

  public Context(JsonNode node, StringBuilder buf) {
    this(node, buf, Locale.getDefault());
  }

  public Context(JsonNode node, Locale locale) {
    this(node, new StringBuilder(), locale);
  }

  public Context(JsonNode node, StringBuilder buf, Locale locale) {
    this.currentFrame = new Frame(node);
    this.buf = buf;
    this.locale = locale;
  }

  public static Context subContext(Context ctx, StringBuilder buf) {
    return new SubContext(ctx, buf);
  }

  public boolean safeExecutionEnabled() {
    return safeExecution;
  }

  public List<ErrorInfo> getErrors() {
    return (errors == null) ? Collections.<ErrorInfo>emptyList() : errors;
  }

  public Locale locale() {
    return locale;
  }

  /**
   * Set mode where no exceptions will be thrown; instead
   */
  public void setSafeExecution() {
    this.safeExecution = true;
  }

  public CharSequence getMetaLeft() {
    return META_LEFT;
  }

  public CharSequence getMetaRight() {
    return META_RIGHT;
  }

  /**
   * Swap the buffer for the current formatter.
   */
  public StringBuilder swapBuffer(StringBuilder newBuffer) {
    StringBuilder tmp = buf;
    buf = newBuffer;
    return tmp;
  }

  /**
   * Sets a compiler to be used for compiling partials. If no compiler is set,
   * partials cannot be compiled and will raise errors.
   */
  public void setCompiler(JsonTemplateEngine compiler) {
    this.compiler = compiler;
  }

  public JsonTemplateEngine getCompiler() {
    return compiler;
  }

  public void setLoggingHook(LoggingHook hook) {
    this.loggingHook = hook;
  }

  public CodeLimiter getCodeLimiter() {
    return codeLimiter;
  }

  public void setCodeLimiter(CodeLimiter limiter) {
    this.codeLimiter = limiter;
  }

  /**
   * Execute a single instruction.
   */
  public void execute(Instruction instruction) throws CodeExecuteException {
    if (instruction == null) {
      return;
    }
    currentInstruction = instruction;
    try {
      codeLimiter.check();
      instruction.invoke(this);

    } catch (CodeExecuteException e) {
      // This is thrown explicitly when an instruction / plugin needs to
      // abort execution. Instructions and plugins must first check if
      // safe execution mode is enabled before throwing. This gives us
      // the flexibility to abort execution even when safe mode is enabled
      // for severe errors, or when a hard resource limit is reached.
      throw e;

    } catch (Exception e) {
      String repr = ReprEmitter.get(instruction, false);
      ErrorInfo error = error(UNEXPECTED_ERROR)
          .name(e.getClass().getSimpleName())
          .data(e.getMessage())
          .repr(repr);

      // In safe mode we don't raise exceptions; just append the error.
      if (safeExecution) {
        addError(error);
      } else {
        throw new CodeExecuteException(error, e);
      }

      // If a logging hook exists, always log the unexpected exception.
      log(e);
    }
  }

  /**
   * Execute a list of instructions.
   */
  public void execute(List<Instruction> instructions) throws CodeExecuteException {
    if (instructions != null) {
      for (Instruction inst : instructions) {
        execute(inst);
      }
    }
  }

  public ErrorInfo error(ExecuteErrorType code) {
    ErrorInfo info = new ErrorInfo(code);
    info.code(code);
    info.line(currentInstruction.getLineNumber());
    info.offset(currentInstruction.getCharOffset());
    return info;
  }

  /**
   * Lazily allocate the compiled partials cache.
   */
  public void setPartials(JsonNode node) {
    this.rawPartials = node;
    this.compiledPartials = new HashMap<>();
  }

  /**
   * Returns the root instruction for a compiled partial, assuming the partial exists
   * in the partials map. Compiled partials are cached for reuse within the same
   * context, since a partial may be applied multiple times within a template, or
   * inside a loop.
   */
  public Instruction getPartial(String name) throws CodeSyntaxException {
    if (rawPartials == null) {
      // Template wants to use a partial but none are defined.
      return null;
    }

    // See if we've previously compiled this exact partial.
    Instruction inst = compiledPartials.get(name);
    if (inst == null) {
      JsonNode partialNode = rawPartials.get(name);
      if (partialNode == null) {
        // Indicate partial is missing.
        return null;
      }
      if (!partialNode.isTextual()) {
        // Should we bother worrying about this, or just cast the node to text?
        return null;
      }

      // Compile the partial.  This can throw a syntax exception, which the formatter
      // will catch and nest inside a runtime exception.
      String source = partialNode.asText();
      CompiledTemplate template = null;
      if (safeExecution) {
        template = compiler.compileSafe(source);
        List<ErrorInfo> errors = template.errors();
        if (!errors.isEmpty()) {
          ErrorInfo parent = error(ExecuteErrorType.COMPILE_PARTIAL_SYNTAX).name(name);
          parent.child(errors);
          addError(parent);
        }

      } else {
        template = compiler.compile(source);
      }

      inst = template.code();

      // Cache the compiled template in case it is used more than once.
      compiledPartials.put(name, inst);
    }
    return inst;
  }

  public StringBuilder buffer() {
    return buf;
  }

  public JsonNode node() {
    return currentFrame.node;
  }

  /**
   * Replace the node's value for the current stack frame.  This enables formatters to
   * chain their output without requiring a change to their interface, e.g. return
   * value, extra method args, etc.
   */
  public void setNode(JsonNode node) {
    this.currentFrame.node = node;
  }

  public void setNode(String value) {
    setNode(buildNode(value));
  }

  public void setNode(int value) {
    setNode(buildNode(value));
  }

  public void setNode(long value) {
    setNode(buildNode(value));
  }

  public void setNode(double value) {
    setNode(buildNode(value));
  }

  public JsonNode buildNode(String value) {
    return new TextNode(value);
  }

  public JsonNode buildNode(int value) {
    return new IntNode(value);
  }

  public JsonNode buildNode(long value) {
    return new LongNode(value);
  }

  public JsonNode buildNode(double value) {
    return new DoubleNode(value);
  }

  public boolean initIteration() {
    JsonNode node = node();
    if (!node.isArray()) {
      return false;
    }
    currentFrame.currentIndex = 0;
    return true;
  }

  /**
   * Use this to find the index position in the current frame.
   */
  public int currentIndex() {
    return currentFrame.currentIndex;
  }

  public boolean hasNext() {
    return currentFrame.currentIndex < currentFrame.node.size();
  }

  /**
   * Return the current frame's array size.
   */
  public int arraySize() {
    return currentFrame.node.size();
  }

  /**
   * Increment the array element pointer for the current frame.
   */
  public void increment() {
    currentFrame.currentIndex++;
  }

  /**
   * Push the node referenced by names onto the stack.
   */
  public void push(Object[] names) {
    push(resolve(names));
  }

  /**
   * SECTION/REPEATED scope does not look up the stack.  It only resolves
   * names against the current frame's node downward.
   */
  public void pushSection(Object[] names) {
    JsonNode node;
    if (names == null) {
      node = currentFrame.node;
    } else {
      node = resolve(names[0], currentFrame);
      for (int i = 1, len = names.length; i < len; i++) {
        if (node.isMissingNode()) {
          break;
        }
        node = nodePath(node, names[i]);
      }
    }
    push(node);
  }

  /**
   * Pushes the next element from the current array node onto the stack.
   */
  public void pushNext() {
    JsonNode node = currentFrame.node.path(currentFrame.currentIndex);
    if (node.isNull()) {
      node = undefined;
    }
    push(node);
  }

  public void setVar(String name, JsonNode node) {
    currentFrame.setVar(name, node);
  }

  public JsonNode resolve(Object name) {
    return lookupStack(name);
  }

  /**
   * Lookup the JSON node referenced by the list of names.
   */
  public JsonNode resolve(Object[] names) {
    if (names == null) {
      return currentFrame.node;
    }

    // Find the starting point.
    JsonNode node = lookupStack(names[0]);
    for (int i = 1, len = names.length; i < len; i++) {
      if (node.isMissingNode()) {
        return undefined;
      }
      if (node.isNull()) {
        return new TextNode("[JSONT: Can't resolve '" + ReprEmitter.get(names) + "'.]");
      }
      node = nodePath(node, names[i]);
    }
    return node;
  }

  private void log(Exception exc) {
    if (loggingHook != null) {
      loggingHook.log(exc);
    }
  }

  private void push(JsonNode node) {
    stack.push(currentFrame);
    currentFrame = new Frame(node);
  }

  /**
   * Starting at the current frame, walk up the stack looking for the first
   * object node which contains 'name' and return that. If none match, return
   * undefined.
   */
  private JsonNode lookupStack(Object name) {
    JsonNode node = resolve(name, currentFrame);
    if (!node.isMissingNode()) {
      return node;
    }
    Iterator<Frame> iter = stack.iterator();
    while (iter.hasNext()) {
      node = resolve(name, iter.next());
      if (!node.isMissingNode()) {
        return node;
      }
    }
    return undefined;
  }

  /**
   * Obtain the value for 'name' from the given stack frame's node.
   */
  private JsonNode resolve(Object name, Frame frame) {
    // Special internal variable @index points to the array index for a
    // given stack frame.
    if (name instanceof String) {
      String strName = (String)name;

      if (strName.startsWith("@")) {
        if (name.equals("@index")) {
          if (frame.currentIndex != -1) {
            // @index is 1-based
            return new IntNode(frame.currentIndex + 1);
          }
          return Constants.MISSING_NODE;
        }
        JsonNode node = frame.getVar(strName);
        return (node == null) ? Constants.MISSING_NODE : node;
      }

      // Fall through
    }
    return nodePath(frame.node, name);
  }

  private JsonNode nodePath(JsonNode node, Object key) {
    if (key instanceof Integer) {
      return node.path((int) key);
    }
    return node.path((String) key);
  }

  public void addError(ErrorInfo error) {
    if (errors == null) {
      errors = new ArrayList<>();
    }
    errors.add(error);
  }

  /**
   * Pop a frame off the stack.
   */
  public void pop() {
    currentFrame = stack.pop();
  }

  static class Frame {

    private Map<String, JsonNode> variables;

    private JsonNode node;

    int currentIndex;

    public Frame(JsonNode node) {
      this.node = node;
      this.currentIndex = -1;
    }

    public void setVar(String name, JsonNode node) {
      if (variables == null) {
        variables = new HashMap<>(4);
      }
      variables.put(name, node);
    }

    public JsonNode getVar(String name) {
      return (variables == null) ? null : variables.get(name);
    }

  }

  static class SubContext extends Context {

    private final Context parent;

    public SubContext(Context parent, StringBuilder buf) {
      super(parent.node(), buf, parent.locale());
      this.parent = parent;
      this.setLoggingHook(parent.loggingHook);
      this.setCodeLimiter(parent.codeLimiter);
      if (parent.safeExecutionEnabled()) {
        this.setSafeExecution();
      }
    }

    @Override
    public void addError(ErrorInfo error) {
      parent.addError(error);
    }

  }

}
