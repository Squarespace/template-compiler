package com.squarespace.template.plugins.platform.i18n;

import com.squarespace.template.Formatter;
import com.squarespace.template.FormatterRegistry;
import com.squarespace.template.StringView;
import com.squarespace.template.SymbolTable;

public class InternationalFormatters implements FormatterRegistry {
  @Override
  public void registerFormatters(SymbolTable<StringView, Formatter> table) {
    table.add(new MoneyFormatter());
  }
}
