package com.squarespace.template;

import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;


/**
 * Base class for Instructions.
 */
public abstract class BaseInstruction implements Instruction {

  private int lineNumber;

  private int charOffset;
  
  public void setLineNumber(int number) {
    this.lineNumber = number;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public void setCharOffset(int offset) {
    this.charOffset = offset;
  }
  
  public int getCharOffset() {
    return charOffset;
  }

  /**
   * To facilitate clear error messages, the toString() for all instructions will
   * output the type and non-recursive representation.  Use repr() to get the
   * recursive representation alone.
   */
  public String toString() {
    StringBuilder buf = new StringBuilder(getType().toString());
    buf.append(" (").append(lineNumber).append(',').append(charOffset).append(')');
    String res = ReprEmitter.get(this, false);
    if (res.length() > 0) {
      buf.append(' ');
      buf.append(StringEscapeUtils.escapeJava(res));
    }
    return buf.toString();
  }
  
  public static String[] splitName(String name) {
    return name.equals("@") ? null : StringUtils.split(name, '.');
  }
  
  /**
   * 
   */
  public String repr() {
    StringBuilder buf = new StringBuilder();
    repr(buf, true);
    return buf.toString();
  }
  
  protected boolean equals(Object v1, Object v2) {
    return (v1 == null) ? (v2 == null) : v1.equals(v2);
  }
  
  protected void repr(List<Instruction> instructions, StringBuilder buf, boolean recurse) {
    for (Instruction inst : instructions) {
      inst.repr(buf, recurse);
    }
  }
  
  protected void tree(List<Instruction> instructions, StringBuilder buf, int depth) {
    for (Instruction inst : instructions) {
      inst.tree(buf, depth);
    }
  }

  /**
   * Generate a tree representation of this instruction and its children.
   */
  public void tree(StringBuilder buf, int depth) {
    throw new UnsupportedOperationException("tree() not yet implemented");
  }

  public void invoke(Context ctx) throws CodeExecuteException {
    // NOOP
  }

  /**
   * Generate a printable representation of this instruction and its children.
   */
  public abstract void repr(StringBuilder buf, boolean recurse);
  
  
}
