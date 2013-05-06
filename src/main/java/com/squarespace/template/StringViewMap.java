package com.squarespace.template;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class StringViewMap<K, V> extends HashMap<K, V> {

  public StringViewMap(int numBuckets) {
    super(numBuckets);
  }
  
  /**
   * Dump the structure of the map to debug and tune the layout. I'd prefer to switch to a
   * compiled DFA-based replacement, which will eliminate need for a HashMap and calling
   * hashCode() on StringViews -- it will match incrementally, char-by-char.  At this 
   * point I couldn't justify the additional dev, but may once work on the LESS compiler 
   * starts.
   */
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
