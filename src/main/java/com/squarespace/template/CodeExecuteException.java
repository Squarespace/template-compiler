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


/**
 * Represents any error that occurs during execution of a compiled template.
 */
public class CodeExecuteException extends CodeException {

  private final ErrorInfo errorInfo;
  
  public CodeExecuteException(ErrorInfo info) {
    super(info.getMessage());
    this.errorInfo = info;
  }
  
  public CodeExecuteException(ErrorInfo info, Throwable cause) {
    super(info.getMessage(), cause);
    this.errorInfo = info;
  }

  public ErrorInfo getErrorInfo() {
    return errorInfo;
  }
  
}
