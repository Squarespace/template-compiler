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
 * A class implementing this interface is used as a way of bundling together related
 * implementations of a particular type of symbol.  Any static fields of type V defined
 * on the instance's class will be automatically registered.  The registerTo() method
 * will be called to allow registering dynamically-constructed instances.
 */
public interface Registry<K, V> {

  /**
   * Provides a way to register dynamically-constructed instances, e.g. when you need
   * to create N symbols that are nearly identical you'd rather not create them as
   * static fields but perhaps build them in a loop.
   */
  public void registerTo(SymbolTable<K, V> symbolTable);

}
