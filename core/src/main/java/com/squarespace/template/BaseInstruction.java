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

import org.apache.commons.lang3.StringEscapeUtils;


/**
 * Base class for Instructions.
 */
public abstract class BaseInstruction implements Instruction {

  private boolean preprocessScope;
  private int lineNumber;
  private int charOffset;

  public void setPreprocessScope() {
    this.preprocessScope = true;
  }

  public boolean inPreprocessScope() {
    return this.preprocessScope;
  }

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

  @Override
  public boolean equals(Object obj) {
    throw new UnsupportedOperationException("equals() not supported");
  }

  @Override
  public int hashCode() {
    throw new UnsupportedOperationException("hashCode() not supported");
  }

  /**
   * To facilitate clear error messages, the toString() for all instructions will
   * output the type and non-recursive representation.  Use repr() to get the
   * recursive representation alone.
   */
  @Override
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

  public String repr() {
    StringBuilder buf = new StringBuilder();
    repr(buf, true);
    return buf.toString();
  }

  /**
   * Generate a tree representation of this instruction and its children.
   */
  public void tree(StringBuilder buf, int depth) {
    TreeEmitter.emit(this, depth, buf);
  }

  /**
   * Invokes the instruction. Default is to do nothing.
   */
  public void invoke(Context ctx) throws CodeExecuteException {
    // NOOP
  }

  /**
   * Generate a printable representation of this instruction and its children.
   */
  public abstract void repr(StringBuilder buf, boolean recurse);

}
