package com.squarespace.template.plugins.platform.i18n;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

class InternationalFormatterUtils {

  private InternationalFormatterUtils() {
  }

  static String formatMoney(Locale locale, Currency currency, double value) {
    NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
    formatter.setCurrency(currency);
    formatter.setMaximumFractionDigits(currency.getDefaultFractionDigits());
    return formatter.format(value);
  }
}
