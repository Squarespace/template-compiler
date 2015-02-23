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

import java.util.ArrayList;
import java.util.List;


/**
 * Symbol table holding all active predicates.  Uses reflection to discover and
 * statically register predicate instances by container class, since they are numerous.
 */
public class PredicateTable extends SymbolTable<StringView, Predicate> {

  private static final int NUM_BUCKETS = 128;

  private final List<String> symbolList = new ArrayList<>();

  public PredicateTable() {
    super(NUM_BUCKETS);
  }

  public PredicateTable(int numBuckets) {
    super(numBuckets);
  }

  public String[] getSymbols() {
    return symbolList.toArray(Constants.EMPTY_ARRAY_OF_STRING);
  }

  /**
   * Store the Predicate in the symbol table.
   */
  @Override
  public void add(Predicate predicate) {
    put(new StringView(predicate.getIdentifier()), predicate);
    symbolList.add(predicate.getIdentifier());
  }

}
