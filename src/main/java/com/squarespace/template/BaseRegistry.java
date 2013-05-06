package com.squarespace.template;


public abstract class BaseRegistry<T> implements Registry<StringView, T> {

  @Override
  public void registerTo(SymbolTable<StringView, T> symbolTable) {
    // Only implemented to register dynamically-constructed instances.
  }
  
}
