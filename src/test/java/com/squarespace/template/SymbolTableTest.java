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

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;


@Test( groups={ "unit" })
public class SymbolTableTest {
  
  @Test
  public void testSymbolTable() {
    NameTable table = new NameTable();
    table.register(new NameRegistry());
    Assert.assertEquals(table.get("ignored"), null);
    Assert.assertEquals(table.get("static"), new Name("static"));
    Assert.assertEquals(table.get("dynamic"), new Name("dynamic"));
  }
  
  @Test
  public void testFormatterTable() {
    FormatterTable table = new FormatterTable(8);
    table.register(new UnitTestFormatters());
    String[] expected = new String[] { "execute-error", "invalid-args", "npe", "required-args", "unstable" };
    String[] symbols = table.getSymbols();
    Arrays.sort(symbols);
    Assert.assertEquals(symbols, expected);
  }

  @Test
  public void testPredicateTable() {
    PredicateTable table = new PredicateTable(8);
    table.register(new UnitTestPredicates());
    String[] expected = new String[] { "execute-error?", "invalid-args?", "required-args?", "unstable?" };
    String[] symbols = table.getSymbols();
    Arrays.sort(symbols);
    Assert.assertEquals(symbols, expected);
  }
  
  @Test
  public void testDuplicateSymbols() {
    PredicateTable table = new PredicateTable(8);
    table.register(new UnitTestPredicates());
    try {
      table.register(new UnitTestPredicates());
      Assert.fail("Expected error on registering duplicate symbol");
    } catch (RuntimeException e) {
      // Expected.
    }
  }
  
  static class Name {
    
    private String name;
    
    public Name(String n) {
      this.name = n;
    }
    
    public String getName() {
      return name;
    }
    
    @Override
    public boolean equals(Object obj) {
      return (obj instanceof Name) ? name.equals(((Name)obj).name) : false;
    }
  }
  
  static class NameTable extends SymbolTable<String, Name> {
    
    private static final TypeRef<Name> TYPE_REF = new TypeRef<Name>() { };

    public NameTable() {
      super(TYPE_REF, 8);
    }
    
    @Override
    public void registerSymbol(Object impl) {
      Name name = (Name) impl;
      put(name.getName(), name);
    }
  }
  
  static class NameRegistry implements Registry<String, Name> {
    
    public Name IGNORED = new Name("ignored");
    
    public static final Name STATIC = new Name("static");
    
    @Override
    public void registerTo(SymbolTable<String, Name> symbolTable) {
      symbolTable.registerSymbol(new Name("dynamic"));
    }
    
  }
}
