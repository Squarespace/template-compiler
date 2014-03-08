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

  public static Object[] splitVariable(String name) {
    String[] parts = name.equals("@") ? null : StringUtils.split(name, '.');
    if (parts == null) {
      return null;
    }

    // Each segment of the key path can be either a String or an Integer.
    Object[] keys = new Object[parts.length];
    for (int i = 0, len = parts.length; i < len; i++) {
      keys[i] = allDigits(parts[i]) ? Integer.parseInt(parts[i], 10) : parts[i];
    }
    return keys;
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

  public void invoke(Context ctx) throws CodeExecuteException {
    // NOOP
  }

  /**
   * Generate a printable representation of this instruction and its children.
   */
  public abstract void repr(StringBuilder buf, boolean recurse);

  private static boolean allDigits(String str) {
    for (int i = 0, len = str.length(); i < len; i++) {
      if (!Character.isDigit(str.charAt(i))) {
        return false;
      }
    }
    return true;
  }
}
