/**
 * Copyright (c) 2015 SQUARESPACE, Inc.
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
package com.squarespace.template.plugins.platform.i18n;

import java.util.Currency;
import java.util.Locale;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.BaseFormatter;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Context;

class LegacyMoneyFormatter extends BaseFormatter {

  private static final String FORMATTER_NAME = "i18n-money-format";

  private static final Currency DEFAULT_CURRENCY = Currency.getInstance("USD");

  private static final Locale DEFAULT_LOCALE = Locale.US;

  private static final String CURRENCY_FIELD_NAME = "currencyCode";

  private static final String VALUE_FIELD_NAME = "decimalValue";

  LegacyMoneyFormatter() {
    super(FORMATTER_NAME, false);
  }

  @Override
  public void validateArgs(Arguments args) throws ArgumentsException {
    args.atMost(1);

    String localeStr = getLocaleArg(args);
    if (localeStr != null) {
      Locale locale;
      try {
        // LocaleUtils uses an underscore format (e.g. en_US), but the new Java standard
        // is a hyphenated format (e.g. en-US). This allows us to use LocaleUtils' validation.
        locale = LocaleUtils.toLocale(localeStr.replace('-', '_'));
      } catch (IllegalArgumentException e) {
        throw new ArgumentsException("Invalid locale: " + localeStr);
      }

      args.setOpaque(locale);
    } else {
      args.setOpaque(DEFAULT_LOCALE);
    }
  }

  @Override
  public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
    Locale locale = (Locale) args.getOpaque();
    Currency currency = getCurrency(node);
    double value = node.path(VALUE_FIELD_NAME).asDouble(0);
    return ctx.buildNode(LegacyMoneyFormatFactory.create(locale, currency).format(value));
  }

  private static String getLocaleArg(Arguments args) {
    if (args.count() == 0) {
      return null;
    }

    return StringUtils.trimToNull(args.first());
  }

  private static Currency getCurrency(JsonNode node) {
    String currencyStr = StringUtils.trimToNull(node.path(CURRENCY_FIELD_NAME).asText());
    if (currencyStr == null) {
      return DEFAULT_CURRENCY;
    }

    return Currency.getInstance(currencyStr);
  }
}
