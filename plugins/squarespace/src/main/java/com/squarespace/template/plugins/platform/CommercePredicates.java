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


public class CommercePredicates implements PredicateRegistry {

  @Override
  public void registerPredicates(SymbolTable<StringView, Predicate> table) {
    table.add(HAS_VARIANTS);
    table.add(ON_SALE);
    table.add(SOLD_OUT);
    table.add(VARIED_PRICES);
  }

  public static final Predicate HAS_VARIANTS = new BasePredicate("has-variants?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return CommerceUtils.hasVariants(ctx.node());
    }
  };

  public static final Predicate ON_SALE = new BasePredicate("on-sale?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return CommerceUtils.isOnSale(ctx.node());
    }
  };

  public static final Predicate SOLD_OUT = new BasePredicate("sold-out?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return CommerceUtils.isSoldOut(ctx.node());
    }
  };

  public static final Predicate VARIED_PRICES = new BasePredicate("varied-prices?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      return CommerceUtils.hasVariedPrices(ctx.node());
    }
  };

}
