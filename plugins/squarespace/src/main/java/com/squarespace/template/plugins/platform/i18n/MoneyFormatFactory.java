/**
 * Copyright (c) 2016 SQUARESPACE, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.squarespace.template.plugins.platform.i18n;

import static com.squarespace.template.plugins.platform.i18n.CurrencyPlacement.LEFT;
import static com.squarespace.template.plugins.platform.i18n.CurrencyPlacement.RIGHT;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This formatter follows the following set of rules when constructing a currency format.
 *
 * 1. If Squarespace has decided on a symbol for a currency, use it. Otherwise, fall back on Java's currency symbol
 * logic. 2. The currency placement is determined by the locale. Defaults to left if once is not specified. 3. If the
 * currency symbol starts with a letter and the placement is on the right, add a space between a value and the symbol.
 */
class MoneyFormatFactory {

  // Visible for testing
  static final Pattern STARTS_WITH_LETTER = Pattern.compile("^[A-Za-z].*?");

  // Visible for testing
  static final Pattern ENDS_WITH_LETTER = Pattern.compile("^.*?[A-Za-z]$");

  private static final String BASE_PATTERN = "#,##0.00";

  private static final String SYMBOL_FORMAT_CHAR = "¤";

  private static final String DELIMITER = ";";

  private static final Map<String, String> CURRENCY_SYMBOLS = new HashMap<String, String>() {
    {
      put("AUD", "A$");
      put("CAD", "C$");
      put("DKK", "kr");
      put("EUR", "€");
      put("GBP", "£");
      put("JPY", "¥");
      put("NOK", "kr");
      put("SEK", "kr");
      put("USD", "$");
    }
  };

  private static final Map<String, CurrencyPlacement> CURRENCY_PLACEMENTS = new HashMap<String, CurrencyPlacement>() {
    {
      put("da-DK", RIGHT);
      put("de-DE", RIGHT);
      put("en-AU", LEFT);
      put("en-CA", LEFT);
      put("en-IE", LEFT);
      put("en-UK", LEFT);
      put("en-US", LEFT);
      put("es-US", LEFT);
      put("fr-CA", LEFT);
      put("fr-FR", RIGHT);
      put("nl-NL", LEFT);
      put("no-NO", LEFT);
      put("sv-SE", RIGHT);
    }
  };

  private MoneyFormatFactory() {
  }

  static NumberFormat create(Locale locale, Currency currency) {
    // The order of these lines matter! For instance, swapping lines 3 and 4 causes 2 decimal places to always be shown.
    DecimalFormat formatter = new DecimalFormat();
    formatter.setCurrency(currency);
    formatter.applyLocalizedPattern(getLocalizedPattern(locale, currency));
    formatter.setMaximumFractionDigits(currency.getDefaultFractionDigits());
    formatter.setDecimalFormatSymbols(getDecimalFormatSymbols(locale, currency));
    return formatter;
  }

  private static String getLocalizedPattern(Locale locale, Currency currency) {
    CurrencyPlacement placement = getCurrencyPlacement(locale);
    String symbol = getCurrencySymbol(locale, currency);

    String positive;
    String negative;
    if (placement == RIGHT && STARTS_WITH_LETTER.matcher(symbol).matches()) {
      // If the symbol goes on the right and starts with a letter, add space between value and symbol.
      positive = BASE_PATTERN + " " + SYMBOL_FORMAT_CHAR;
      negative = "-" + positive;
    } else if (placement == RIGHT) {
      // Otherwise, no need to add space between value and symbol.
      positive = BASE_PATTERN + SYMBOL_FORMAT_CHAR;
      negative = "-" + positive;
    } else if (ENDS_WITH_LETTER.matcher(symbol).matches()) {
      // If the symbol goes on the left and ends with a letter, add space between symbol and value.
      positive = SYMBOL_FORMAT_CHAR + " " + BASE_PATTERN;
      // Note the placement of the negative sign.
      negative = SYMBOL_FORMAT_CHAR + " -" + BASE_PATTERN;
    } else {
      // Otherwise, no need to add space between symbol and value.
      positive = SYMBOL_FORMAT_CHAR + BASE_PATTERN;
      negative = "-" + positive;
    }

    return positive + DELIMITER + negative;
  }

  /**
   * Rely on Java for the formatting symbols (negative sign, grouping symbol, and decimal separator).
   *
   * @param locale locale
   * @param currency currency
   * @return the decimal format symbols
   */
  private static DecimalFormatSymbols getDecimalFormatSymbols(Locale locale, Currency currency) {
    DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
    symbols.setCurrency(currency);
    symbols.setCurrencySymbol(getCurrencySymbol(locale, currency));
    return symbols;
  }

  /**
   * Gets currency symbol that has been decided on for Squarespace money design standard. If a standard does not exist
   * for the currency, this falls back on Java's symbol formatting.
   *
   * @param locale locale
   * @param currency currency
   * @return the appropriate currency symbol
   */
  private static String getCurrencySymbol(Locale locale, Currency currency) {
    String currencySymbol = CURRENCY_SYMBOLS.get(currency.getCurrencyCode());
    if (currencySymbol == null) {
      currencySymbol = currency.getSymbol(locale);
    }

    return currencySymbol;
  }

  /**
   * Gets currency placement (left or right) based on locale, defaults to left if one is not specified.
   *
   * @param locale locale
   * @return the currency placement
   */
  private static CurrencyPlacement getCurrencyPlacement(Locale locale) {
    CurrencyPlacement placement = CURRENCY_PLACEMENTS.get(locale.toLanguageTag());
    if (placement == null) {
      placement = CurrencyPlacement.LEFT;
    }

    return placement;
  }
}
