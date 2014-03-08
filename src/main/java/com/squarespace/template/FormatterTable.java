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
 * Symbol table holding all active formatters.
 */
public class FormatterTable extends SymbolTable<StringView, Formatter> {

  private static final int NUM_BUCKETS = 64;

  private static final TypeRef<Formatter> TYPE_REF = new TypeRef<Formatter>() { };

  private final List<String> symbolList = new ArrayList<>();

  public FormatterTable() {
    super(TYPE_REF, NUM_BUCKETS);
  }

  public FormatterTable(int numBuckets) {
    super(TYPE_REF, numBuckets);
  }

  public String[] getSymbols() {
    return symbolList.toArray(Constants.EMPTY_ARRAY_OF_STRING);
  }

  /**
   * Callback to cast the Formatter and store it in the table.
   */
  @Override
  public void registerSymbol(Object impl) {
    Formatter formatter = (Formatter) impl;
    put(new StringView(formatter.getIdentifier()), formatter);
    symbolList.add(formatter.getIdentifier());
  }

}
