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
   * hashCode() on StringViews -- it will match incrementally, char-by-char.
   */
  public String dump() throws Exception {
    StringBuilder buf = new StringBuilder();

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

    for (int i = 0, len = table.length; i < len; i++) {
      buf.append("Bucket ").append(i).append(":\n");
      Map.Entry<K, V> entry = table[i];

      int num = 0;
      while (entry != null) {
        num++;
        buf.append(entry.getKey()).append(' ');
        entry = (Map.Entry<K, V>)nextField.get(entry);
      }
      if (num > 0) {
        buf.append('(').append(num).append(')');
      }
      buf.append('\n');
    }
    return buf.toString();
  }

}
