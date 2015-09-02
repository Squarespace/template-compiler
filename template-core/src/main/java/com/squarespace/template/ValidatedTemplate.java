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

import java.util.Collections;
import java.util.List;


public class ValidatedTemplate {

  private final CodeList codeList;

  private final CodeStats codeStats;

  private final List<ErrorInfo> errors;

  public ValidatedTemplate(CodeList codeList, CodeStats codeStats, List<ErrorInfo> errors) {
    this.codeList = codeList;
    this.codeStats = codeStats;
    this.errors = errors == null ? Collections.<ErrorInfo>emptyList() : errors;
  }

  public CodeList code() {
    return codeList;
  }

  public CodeStats stats() {
    return codeStats;
  }

  public List<ErrorInfo> errors() {
    return errors;
  }

}
