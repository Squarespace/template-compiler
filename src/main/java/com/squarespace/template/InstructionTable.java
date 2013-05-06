package com.squarespace.template;


/**
 * Mapping of fixed instruction identifiers.  Instructions which have variant
 * forms (predicates, variable references) are handled separately.
 */
public class InstructionTable {
  
  private static final int HASHMAP_BUCKETS = 32;
  
  private static final StringViewMap<StringView, InstructionType> table = new StringViewMap<>(HASHMAP_BUCKETS);
  
  static void add(String str, InstructionType type) {
    table.put(new StringView(str), type);
  }
  
  static {
    add(".alternates", InstructionType.ALTERNATES_WITH);
    add(".end", InstructionType.END);
    add(".if", InstructionType.IF);
    add(".meta-left", InstructionType.META_LEFT);
    add(".meta-right", InstructionType.META_RIGHT);
    add(".newline", InstructionType.NEWLINE);
    add(".or", InstructionType.OR_PREDICATE);
    add(".repeated", InstructionType.REPEATED);
    add(".section", InstructionType.SECTION);
    add(".space", InstructionType.SPACE);
    add(".tab", InstructionType.TAB);
  }
  
  public static void dump() {
    try {
      table.dump();
    } catch (Exception e) {
      System.out.println("Error dumping buckets: " + e);
    }
  }
  public static StringViewMap<StringView, InstructionType> getTable() {
    return table;
  }
  
  public static InstructionType get(StringView symbol) {
    return table.get(symbol);
  }
  
}
