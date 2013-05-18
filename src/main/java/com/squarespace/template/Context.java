package com.squarespace.template;

import static com.squarespace.template.ExecuteErrorType.UNEXPECTED_ERROR;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.TextNode;


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

  private static final JsonNode DEFAULT_UNDEFINED = MissingNode.getInstance();
  
  private static final String META_LEFT = "{";
  
  private static final String META_RIGHT = "}";
  
  private ArrayDeque<Frame> stack = new ArrayDeque<>();

  private Frame currentFrame;

  private JsonNode undefined = DEFAULT_UNDEFINED;
  
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
    this.currentFrame = new Frame(node);
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
    try {
      instruction.invoke(this);
    } catch (CodeExecuteException e) {
      throw e;
    } catch (Exception e) {
      String repr = ReprEmitter.get(instruction, false);
      ErrorInfo info = error(UNEXPECTED_ERROR).name(e.getClass().getSimpleName()).data(e.getMessage()).repr(repr);
      throw new CodeExecuteException(info, e);
    }
  }
  
  /**
   * Execute a list of instructions.
   */
  public void execute(List<Instruction> instructions) throws CodeExecuteException {
    if (instructions == null) {
      return;
    }
    for (Instruction inst : instructions) {
      execute(inst);
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
  
  public StringBuilder buffer() {
    return buf;
  }
  
  public JsonNode node() {
    return currentFrame.node;
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
   * Increment the array element pointer for the current frame.
   */
  public void increment() {
    currentFrame.currentIndex++;
  }

  /**
   * Push the node referenced by names onto the stack.
   */
  public void push(String[] names) {
    push(resolve(names));
  }

  /**
   * SECTION/REPEATED scope does not look up the stack.  It only resolves
   * names against the current frame's node downward.
   */
  public void pushSection(String[] names) {
    JsonNode node;
    if (names == null) {
      node = currentFrame.node;
    } else {
      node = resolve(names[0], currentFrame);
      for (int i = 1; i < names.length; i++) {
        if (node.isMissingNode()) {
          break;
        }
        node = node.path(names[i]);
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
  
  public JsonNode resolve(String name) {
    return lookupStack(name);
  }

  /**
   * Lookup the JSON node referenced by the list of names. 
   */
  public JsonNode resolve(String[] names) {
    if (names == null) {
      return currentFrame.node;
    }

    // Find the starting point.
    JsonNode node = lookupStack(names[0]);
    for (int i = 1; i < names.length; i++) {
      if (node.isMissingNode()) {
        return undefined;
      }
      if (node.isNull()) {
        return new TextNode("[JSONT: Can't resolve '" + ReprEmitter.get(names) + "'.]");
      }
      node = node.path(names[i]);
    }
    return node;
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
  private JsonNode lookupStack(String name) {
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
  private JsonNode resolve(String name, Frame frame) {
    // Special internal variable @index points to the array index for a 
    // given stack frame.
    if (name.equals("@index")) {
      if (frame.currentIndex != -1) {
        // @index is 1-based
        return new IntNode(frame.currentIndex + 1);
      }
      return Constants.MISSING_NODE;
    }
    return frame.node.path(name);
  }

  /**
   * Pop a frame off the stack.
   */
  public void pop() {
    currentFrame = stack.pop();
  }
  
  static class Frame {
    
    JsonNode node;
    
    int currentIndex;
    
    public Frame(JsonNode node) {
      this.node = node;
      this.currentIndex = -1;
    }
    
  }
  
}
