package com.squarespace.template;

import java.util.ArrayList;
import java.util.List;


/**
 * Mapping of fixed instruction identifiers.  Instructions which have variant
 * forms (predicates, variable references) are handled separately.
 */
public class InstructionTable {
  
  private static final int HASHMAP_BUCKETS = 32;
  
  private static final StringViewMap<StringView, InstructionType> table = new StringViewMap<>(HASHMAP_BUCKETS);
  
  private static final List<String> symbolList = new ArrayList<>();
  
  static void add(String str, InstructionType type) {
    add(str, type, true);
  } 
  
  static void add(String str, InstructionType type, boolean isSymbol) {
    table.put(new StringView(str), type);
    if (isSymbol) {
      symbolList.add(str);
    }
  }
  
  static {
    add(".alternates", InstructionType.ALTERNATES_WITH, false);
    add(".end", InstructionType.END);
    add(".if", InstructionType.IF);
    add(".meta-left", InstructionType.META_LEFT);
    add(".meta-right", InstructionType.META_RIGHT);
    add(".newline", InstructionType.NEWLINE);
    add(".or", InstructionType.OR_PREDICATE);
    add(".repeated", InstructionType.REPEATED, false);
    add(".section", InstructionType.SECTION);
    add(".space", InstructionType.SPACE);
    add(".tab", InstructionType.TAB);
    
    // Special-case for instructions containing whitespace (yeah).
    symbolList.add(".alternates with");
    symbolList.add(".repeated section");
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
  
  public static String[] getSymbols() {
    return symbolList.toArray(Constants.EMPTY_ARRAY_OF_STRING);
  }

}
