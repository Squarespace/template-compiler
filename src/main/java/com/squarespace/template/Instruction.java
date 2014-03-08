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


public interface Instruction {

  public InstructionType getType();

  public void setLineNumber(int line);

  public int getLineNumber();

  public void setCharOffset(int offset);

  public int getCharOffset();

  public void invoke(Context ctx) throws CodeExecuteException;

  public void repr(StringBuilder buf, boolean recurse);

  public void tree(StringBuilder buf, int depth);

}
