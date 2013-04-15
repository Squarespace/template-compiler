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
    table = new StringViewMap<K, V>(numBuckets);
    this.ref = ref;
  }

  public void setInUse() {
    this.inUse = true;
  }
  
  /**
   * Register all T fields found on each of the source classes.
   */
  public void register(Class<?> ... sources) {
    for (Class<?> source : sources) {
      registerClass(source);
    }
  }
  
  /**
   * Retrieve the T instance by symbol.
   */
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

  void put(K key, V value) {
    if (inUse) {
      throw new RuntimeException("Attempt to add a symbol after table in use.");
    }
    table.put(key, value);
  }

  /**
   * Callback to cast the object, retrieve the key and store it in the table.
   */
  abstract void registerSymbol(Object impl);

  /**
   * Use reflection to iterate over all static T fields on the class and
   * register each using a callback.
   */
  private void registerClass(Class<?> source) {
    Field[] fields = source.getDeclaredFields();
    for (Field field : fields) {
      
      if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
        continue;
      }
      
      Class<?> type = field.getType();
      if (type.equals(ref.getType())) {
        try {
          registerSymbol(field.get(source));
        } catch (IllegalAccessException e) {
          throw new RuntimeException("failed to register " + source, e);
        }
      }
    }
  }
  
}
