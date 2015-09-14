package com.squarespace.template.plugins.platform;

import java.util.Arrays;
import java.util.List;

import com.squarespace.template.FormatterTable;
import com.squarespace.template.PredicateTable;
import com.squarespace.template.plugins.CoreFormatters;
import com.squarespace.template.plugins.CorePredicates;


public class DumpSymbols {

  public static void main(String[] args) {
    System.out.println("PREDICATES =======================");
    dumpPredicates();
    System.out.println();
    System.out.println("FORMATTERS =======================");
    dumpFormatters();
  }

  private static void dumpPredicates() {
    PredicateTable table = new PredicateTable();
    
    table.register(new CommercePredicates());
    table.register(new ContentPredicates());
    table.register(new CorePredicates());
    table.register(new SocialPredicates());
    table.register(new SlidePredicates());

    dump(table.getSymbols());
  }

  private static void dumpFormatters() {
    FormatterTable table = new FormatterTable();
    table.register(new CommerceFormatters());
    table.register(new ContentFormatters());
    table.register(new CoreFormatters());
    table.register(new SocialFormatters());
    dump(table.getSymbols());
  }
  
  private static void dump(String[] symbols) {
    List<String> syms = Arrays.asList(symbols);
    syms.sort(null);
    for (String sym : syms) {
      System.out.println(sym);
    }
  }
  
}
