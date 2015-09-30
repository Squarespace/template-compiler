/**
 * Copyright (c) 2015 SQUARESPACE, Inc.
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

package com.squarespace.template.plugins.platform;

import com.squarespace.template.Arguments;
import com.squarespace.template.BasePredicate;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Context;
import com.squarespace.template.Predicate;
import com.squarespace.template.PredicateRegistry;
import com.squarespace.template.StringView;
import com.squarespace.template.SymbolTable;
import com.squarespace.template.plugins.platform.enums.SliceType;


/**
 * Extracted from Commons library at commit ab4ba7a6f2b872a31cb6449ae9e96f5f5b30f471
 */
public class SlidePredicates implements PredicateRegistry {

  @Override
  public void registerPredicates(SymbolTable<StringView, Predicate> table) {
    table.add(CURRENT_TYPE);
  }

  public static final Predicate CURRENT_TYPE = new BasePredicate("current-type?", false) {

    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      int expected = ctx.node().path("currentType").asInt();
      SliceType type = SliceType.fromName(args.get(0));
      return type == null ? false : type.code() == expected;
    }
  };
}
