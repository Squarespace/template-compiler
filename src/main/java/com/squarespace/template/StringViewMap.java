package com.squarespace.template;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class StringViewMap<K, V> extends HashMap<K, V> {

  public StringViewMap(int numBuckets) {
    super(numBuckets);
  }
  
  @SuppressWarnings("unchecked")
  public void dump() throws Exception {

    Field field = HashMap.class.getDeclaredField("table");
    field.setAccessible(true);

    Map.Entry<K, V>[] table = (Map.Entry<K, V>[])field.get(this);

    Class<?> hashMapEntryClass = null;
    for (Class<?> c : HashMap.class.getDeclaredClasses()) {
      if ("java.util.HashMap.Entry".equals(c.getCanonicalName())) {
        hashMapEntryClass = c;
      }
    }

    Field nextField = hashMapEntryClass.getDeclaredField("next");
    nextField.setAccessible(true);

    for (int i = 0; i < table.length; i++) {
      System.out.print("Bucket " + i + ": ");
      Map.Entry<K, V> entry = table[i];

      int num = 0;
      while (entry != null) {
        num++;
        System.out.print(entry.getKey() + " ");
        entry = (Map.Entry<K, V>)nextField.get(entry);
      }
      if (num > 0) {
        System.out.print("(" + num + ")");
      }
      System.out.println();
    }
  }

}
