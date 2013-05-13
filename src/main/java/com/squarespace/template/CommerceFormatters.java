package com.squarespace.template;

import java.util.Locale;


public class CommerceFormatters extends BaseRegistry<Formatter> {

  static class MoneyFormatter extends BaseFormatter {
    public MoneyFormatter(String identifier) {
      super(identifier, false);
    }
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      double value = ctx.node().asDouble();
      ctx.append(FormatterUtils.formatMoney(value, Locale.US));
    }
  }
  
  public static final Formatter MONEY_FORMAT = new MoneyFormatter("money-format");
  
  public static final Formatter MONEYFORMAT = new MoneyFormatter("moneyFormat");

}
