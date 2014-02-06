package com.squarespace.template;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.type.TypeReference;


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
    
    private static final TypeReference<Name> TYPE_REF = new TypeReference<Name>() { };

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
