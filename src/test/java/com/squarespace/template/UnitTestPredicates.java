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

import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.BasePredicate;
import com.squarespace.template.BaseRegistry;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Context;
import com.squarespace.template.ExecuteErrorType;
import com.squarespace.template.Predicate;



/**
 * Implementations to test the Predicate interface.
 */
public class UnitTestPredicates extends BaseRegistry<Predicate> {

  public static Predicate EXECUTE_ERROR = new BasePredicate("execute-error?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      throw new CodeExecuteException(ctx.error(ExecuteErrorType.GENERAL_ERROR).name("ABCXYZ"));
    }
  };
  
  public static Predicate INVALID_ARGS = new BasePredicate("invalid-args?", false) {
    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
     throw new ArgumentsException("Invalid arguments");
    }
  };

  public static Predicate REQUIRED_ARGS = new BasePredicate("required-args?", true) {
  };
  
  public static Predicate UNSTABLE = new BasePredicate("unstable?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      throw new IllegalArgumentException("unexpected error!");
    }
  };
  
}
