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
