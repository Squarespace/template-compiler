package com.squarespace.template;

import java.util.Arrays;

import com.fasterxml.jackson.core.type.TypeReference;


/**
 * Test the speedup factor of StringView over String, with hashing considered.
 */
public class StringViewPerfTest {

  static class StringNum {
    private String str;
    private Integer num;
    public StringNum(String s, Integer n) {
      this.str = s;
      this.num = n;
    }
    public String getString() {
      return this.str;
    }
    public Integer getNum() {
      return this.num;
    }
  }

  static class StringViewNum {
    private StringView view;
    private Integer num;
    public StringViewNum(StringView v, Integer n) {
      this.view = v;
      this.num = n;
    }
    public StringView getStringView() {
      return view;
    }
    public Integer getNum() {
      return num;
    }
  }
  
  static class StringTable extends SymbolTable<String, Integer> {
    public StringTable() {
      super(new TypeReference<Integer>(){}, 64);
    }
    @Override
    void registerSymbol(Object impl) {
      StringNum num = (StringNum) impl;
      put(num.getString(), num.getNum());
    }
  }

  static class StringViewTable extends SymbolTable<StringView, Integer> {
    public StringViewTable() {
      super(new TypeReference<Integer>(){}, 64);
    }
    @Override
    void registerSymbol(Object impl) {
      StringViewNum num = (StringViewNum) impl;
      put(num.getStringView(), num.getNum());
    }
  }
  
  private String expand(String data, int times) {
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < times; i++) {
      buf.append(data);
    }
    return buf.toString();
  }

// DISABLED: used for development only
//  @Test
  public void testFaster() throws Exception {
    String data = "abcdefghijklmnopqrstuvwxyz";

    int iters = 10_000;
    for (int i = 1; i < 4; i++) {
      String raw = expand(data, i * 1024);
      System.out.printf("length: %d\n", raw.length());
      long strTime = runString(raw, iters);
      long viewTime = runView(raw, iters);
      System.out.printf("   str: %d\n", strTime);
      System.out.printf("  view: %d\n", viewTime);
      System.out.printf("factor: %.2f\n\n", (strTime / (double)viewTime));
    }
  }
  
  private long runString(String data, int iters) {
    int len = data.length();
    StringTable strTable = new StringTable();
    strTable.registerSymbol(new StringNum(data, 1));
    long[] times = new long[5];

    for (int p = 0; p < 5; p++) {
      long start = System.currentTimeMillis();
      for (int i = 0; i < iters; i++) {
        String s = data.substring(2 + (i % 15), len - (i % 50));
        if (strTable.get(s) != null) {
          throw new RuntimeException("bug!");
        }
      }
      times[p] = System.currentTimeMillis() - start;
    }
    Arrays.sort(times);
    long elapsed = 0;
    for (int p = 1; p < 4; p++) {
      elapsed += times[p];
    }
    return elapsed / 3;
  }
  
  private long runView(String data, int iters) {
    int len = data.length();
    StringView view = new StringView(data);
    StringViewTable viewTable = new StringViewTable();
    viewTable.registerSymbol(new StringViewNum(view, 1));

    long[] times = new long[5];
    
    for (int p = 0; p < 5; p++) {
      long start = System.currentTimeMillis();
      for (int i = 0; i < iters; i++) {
        StringView s = new StringView(data, 2 + (i % 15), len - (i % 50));
        if (viewTable.get(s) != null) {
          throw new RuntimeException("bug!");
        }
      }
      times[p] = System.currentTimeMillis() - start;
    }
    Arrays.sort(times);
    long elapsed = 0;
    for (int p = 1; p < 4; p++) {
      elapsed += times[p];
    }
    return elapsed / 3;
  }
  
}
