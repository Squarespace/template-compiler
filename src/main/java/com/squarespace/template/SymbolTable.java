package com.squarespace.template;

import java.lang.reflect.Field;

import com.fasterxml.jackson.core.type.TypeReference;


/**
 * Generic symbol table to map string symbols to instances of a given
 * class. Uses reflection to discover and instances by container class,
 * since they are numerous.
 */
public abstract class SymbolTable<K, V> {

  private StringViewMap<K, V> table;

  private TypeReference<V> ref;
  
  private boolean inUse = false;
  
  public SymbolTable(TypeReference<V> ref, int numBuckets) {
    table = new StringViewMap<>(numBuckets);
    this.ref = ref;
  }

  public void setInUse() {
    this.inUse = true;
  }
  
  public void register(Registry<K, V> source) {
    registerClass(source);
  }
  
  public V get(K symbol) {
    return table.get(symbol);
  }
  
  public void dump() {
    try {
      table.dump();
    } catch (Exception e) {
      System.out.println("Error dumping buckets: " + e);
    }
  }

  protected void put(K key, V value) {
    if (inUse) {
      throw new RuntimeException("Attempt to add a symbol after table in use.");
    }
    // Prevent registering duplicate symbols.
    if (table.get(key) != null) {
      throw new RuntimeException("A symbol named '" + key + "' is already registered!");
    }
    table.put(key, value);
  }

  /**
   * Callback to cast the object, retrieve the key and store it in the table.
   */
  public abstract void registerSymbol(Object impl);

  /**
   * Use reflection to iterate over all static fields on the class and
   * register each that matches our type reference.
   */
  private void registerClass(Registry<K, V> source) {
    
    // Scan for static instances
    Field[] fields = source.getClass().getDeclaredFields();
    for (Field field : fields) {
      
      if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
        continue;
      }
      
      Class<?> type = field.getType();
      if (type.equals(ref.getType())) {
        field.setAccessible(true);
        try {
          registerSymbol(field.get(source));
        } catch (IllegalAccessException e) {
          throw new RuntimeException("failed to register " + source, e);
        }
      }
    }
    
    // Register dynamic instances
    source.registerTo(this);
  }
  
}
