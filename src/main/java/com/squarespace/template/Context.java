package com.squarespace.template;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;


/**
 * Tracks all of the state needed for executing a template against a given JSON tree.
 * 
 * Compilation converts the raw text into an instruction tree. This instruction tree
 * is stateless and can be reused across multiple executions.
 * 
 * The Context is used to carry out a single execution of the template instruction tree.
 * Each execution of a template requires a fresh context object.
 */
public class Context {

  private static final String META_LEFT = "{";
  
  private static final String META_RIGHT = "}";
  
  private ArrayDeque<Frame> stack = new ArrayDeque<>();

  private Frame frame;
  
  /** 
   * Reference to the currently-executing instruction. All instruction execution
   * must pass control via the Context, for proper error handling.
   */
  private Instruction currentInstruction;

  private JsonNode rawPartials;
  
  private Map<String, Instruction> compiledPartials;

  private JsonTemplateEngine compiler;
  
  /* Holds the final output of the template execution */
  private StringBuilder buf;

  public Context(JsonNode node) {
    this(node, new StringBuilder());
  }

  public Context(JsonNode node, StringBuilder buf) {
    this.frame = new Frame(node);
    this.buf = buf;
  }
  
  public CharSequence getMetaLeft() {
    return META_LEFT;
  }
  
  public CharSequence getMetaRight() {
    return META_RIGHT;
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
  
  /**
   * Execute a single instruction.
   */
  public void execute(Instruction instruction) throws CodeExecuteException {
    if (instruction == null) {
      return;
    }
    currentInstruction = instruction;
    instruction.invoke(this);
  }
  
  /**
   * Execute a list of instructions.
   */
  public void execute(List<Instruction> instructions) throws CodeExecuteException {
    if (instructions == null) {
      return;
    }
    for (Instruction inst : instructions) {
      currentInstruction = inst;
      inst.invoke(this);
    }
  }
  
  public ErrorInfo<ExecuteErrorType> error(ExecuteErrorType code) {
    ErrorInfo<ExecuteErrorType> mk = new ErrorInfo<>(code);
    mk.code(code);
    mk.line(currentInstruction.getLineNumber());
    mk.offset(currentInstruction.getCharOffset());
    return mk;
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
   * context.
   */
  public Instruction getPartial(String name) throws CodeSyntaxException {
    if (rawPartials == null) {
      // Template wants to use a partial but none are defined.
      // TODO: need to figure out the correct behavior here -- should we emit an
      // end user error, or just silently omit the partial application.
      return null;
    }
    
    // See if we've previously compiled this exact partial.
    Instruction inst = compiledPartials.get(name);
    if (inst == null) {
      JsonNode partialNode = rawPartials.get(name);
      if (!partialNode.isTextual()) {
        // Should we bother worrying about this, or just cast the node to text?
        return null;
      }
      
      // Compile the partial.  This can throw a syntax exception, which the formatter
      // will catch and nest inside a runtime exception.
      CompiledTemplate template = compiler.compile(partialNode.asText());
      inst = template.getCode();
      
      // Cache the compiled template in case it is used more than once.
      compiledPartials.put(name, inst);
    }
    return inst;
  }
  
  public void append(CharSequence cs) {
    buf.append(cs);
  }
  
  public void append(CharSequence cs, int start, int end) {
    buf.append(cs, start, end);
  }
  
  public StringBuilder getBuffer() {
    return buf;
  }
  
  public JsonNode node() {
    return frame.node;
  }

  /**
   * Use this to find the index position in the current frame.
   */
  public int currentIndex() {
    return frame.currentIndex;
  }
  
  /**
   * Use this to find the index position of the parent frame.
   */
  public int parentIndex() {
    return frame.parentIndex;
  }
  
  public boolean hasNext() {
    return frame.currentIndex < frame.node.size();
  }
  
  /**
   * Increment the array element pointer for the current frame.
   */
  public void increment() {
    frame.currentIndex++;
  }

  /**
   * Push the node referenced by names onto the stack. If names == null do nothing.
   */
  public void push(String[] names) {
    if (names == null) {
      return;
    }
    
    // XXX: need to support searching up the stack until it resolves.
    // XXX: use stack.iterator() to resolve starting node

    JsonNode node = resolve(names, frame.node);
    stack.push(frame);
    frame = new Frame(node);
  }

  /**
   * Pushes the next element from the current array node onto the stack.
   */
  public void pushNext() {
    int parentIndex = frame.currentIndex;
    JsonNode node = frame.node.path(frame.currentIndex);
    frame.currentIndex++;
    stack.push(frame);
    frame = new Frame(node, parentIndex);
  }

  /**
   * Dig out the JSON node referenced by the list of names. 
   * If names == null, return the starting node.
   */
  public JsonNode resolve(String[] names, JsonNode node) {
    if (names == null) {
      return node;
    }
    for (String name : names) {
      node = node.path(name);
    }
    return node;
  }

  /**
   * Pop the stack unconditionally.
   */
  public void pop() {
    frame = stack.pop();
  }
  
  /**
   * Pop the stack if names != null.
   */
  public void pop(String[] names) {
    if (names == null) {
      return;
    }
    frame = stack.pop();
  }

  static class Frame {
    
    JsonNode node;
    
    int currentIndex;
    
    int parentIndex;
    
    public Frame(JsonNode node) {
      this(node, 0);
    }
    
    public Frame(JsonNode node, int parentIndex) {
      this.node = node;
      this.currentIndex = 0;
      this.parentIndex = parentIndex;
    }
    
  }
  
}
