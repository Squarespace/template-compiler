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
 * Generic symbol table to map string symbols to instances of a given
 * class. Uses reflection to discover and register instances by container class,
 * since they are numerous.
 */
public abstract class SymbolTable<K, V> {

  private final StringViewMap<K, V> table;

  private boolean inUse = false;

  public SymbolTable(int numBuckets) {
    table = new StringViewMap<>(numBuckets);
  }

  public void setInUse() {
    this.inUse = true;
  }

  public SymbolTable<K, V> register(Registry<K, V> source) {
    source.registerTo(this);
    return this;
  }

  public V get(K symbol) {
    return table.get(symbol);
  }

  public String dump() {
    try {
      return table.dump();
    } catch (Exception e) {
      throw new RuntimeException("Failed to dump buckets", e);
    }
  }

  protected void put(K key, V value) {
    if (inUse) {
      throw new IllegalStateException("Attempt to add a symbol after table in use.");
    }
    // Prevent registering duplicate symbols.
    if (table.get(key) != null) {
      throw new IllegalStateException("A symbol named '" + key + "' is already registered!");
    }
    table.put(key, value);
  }

  /**
   * Store the given type in the table.
   */
  public abstract void add(V impl);

}
