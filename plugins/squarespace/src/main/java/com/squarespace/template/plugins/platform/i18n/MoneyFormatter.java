/**
 * Copyright (c) 2017 SQUARESPACE, Inc.
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

import static com.squarespace.cldr.numbers.CurrencyFormatStyle.ACCOUNTING;
import static com.squarespace.cldr.numbers.CurrencyFormatStyle.CODE;
import static com.squarespace.cldr.numbers.CurrencyFormatStyle.NAME;
import static com.squarespace.cldr.numbers.CurrencyFormatStyle.SHORT;
import static com.squarespace.cldr.numbers.CurrencyFormatStyle.SYMBOL;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.cldr.CLDR;
import com.squarespace.cldr.numbers.CurrencyFormatOptions;
import com.squarespace.cldr.numbers.CurrencySymbolWidth;
import com.squarespace.cldr.numbers.NumberFormatter;
import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.BaseFormatter;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Context;
import com.squarespace.template.GeneralUtils;
import com.squarespace.template.Variable;
import com.squarespace.template.Variables;


/**
 * MONEY - Formats the special money type, that has the following form:
 *
 *      {"decimalValue": "1.25", "currencyCode": "USD"}
 *
 * Options:
 *
 *   style:<name>     - pattern style:  symbol, accounting, name, code, short
 *   symbol:<type>    - currency symbol width: default, narrow
 *   mode:<name>      - formatting mode: default, significant, significant-maxfrac
 *   round:<name>     - rounding mode: default, ceil, floor, truncate
 *   group            - if present this enables digit grouping
 *   no-group         - if present this disables digit grouping
 *
 * Used in default mode:
 *   minInt:<int>     - in default mode, sets the minimum number of integer digit (zero pads)
 *   minFrac:<int>    - sets the minimum number of fraction digits
 *
 * Used in default and significant-maxfrac modes:
 *   maxFrac:<int>    - sets the maximum number of fraction digits
 *
 * Used in significant and significant-maxfrac modes:
 *   maxSig:<int>     - sets the maximum number of significant digits
 *   minSig:<int>     - sets the minimum number of significant digits
 *
 */
public class MoneyFormatter extends BaseFormatter {

  public MoneyFormatter() {
    super("money", false);
  }

  @Override
  public void validateArgs(Arguments args) throws ArgumentsException {
    CurrencyFormatOptions opts = parseOptions(args);
    args.setOpaque(opts);
  }

  @Override
  public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
    Variable var = variables.first();
    JsonNode node = var.node();
    JsonNode decimal = node.path("decimalValue");
    JsonNode currency = node.path("currencyCode");
    if (decimal.isMissingNode() || currency.isMissingNode()) {
      var.setMissing();
      return;
    }
    BigDecimal decimalValue = GeneralUtils.nodeToBigDecimal(decimal);
    String currencyCode = currency.asText();

    CurrencyFormatOptions opts = (CurrencyFormatOptions) args.getOpaque();
    CLDR.Locale locale = ctx.cldrLocale();
    NumberFormatter fmt = CLDR.get().getNumberFormatter(locale);
    StringBuilder buf = new StringBuilder();
    fmt.formatCurrency(decimalValue, currencyCode, buf, opts);
    var.set(buf);
  }

  private CurrencyFormatOptions parseOptions(Arguments args) {
    CurrencyFormatOptions opts = new CurrencyFormatOptions();
    int count = args.count();
    String value = "";
    for (int i = 0; i < count; i++) {
      String arg = args.get(i);

      int index = arg.indexOf(':');
      if (index != -1) {
        value = arg.substring(index + 1);
        arg = arg.substring(0, index);
      }

      if (arg.equals("style")) {
        switch (value) {
          case "accounting":
            opts.setStyle(ACCOUNTING);
            break;

          case "code":
            opts.setStyle(CODE);
            break;

          case "name":
            opts.setStyle(NAME);
            break;

          case "short":
            opts.setStyle(SHORT);
            break;

          case "symbol":
          case "standard":
            opts.setStyle(SYMBOL);
            break;

          default:
            break;
        }
        continue;

      } else if (arg.equals("symbol")) {
        switch (value) {
          case "default":
            opts.setSymbolWidth(CurrencySymbolWidth.DEFAULT);
            break;

          case "narrow":
            opts.setSymbolWidth(CurrencySymbolWidth.NARROW);
            break;

          default:
            break;
        }
        continue;
      }

      DecimalFormatter.setNumberOption(arg, value, opts);
    }
    return opts;
  }

}
