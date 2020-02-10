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

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.cldrengine.CLDR;
import com.squarespace.cldrengine.api.CurrencyFormatOptions;
import com.squarespace.cldrengine.api.CurrencyFormatStyleType;
import com.squarespace.cldrengine.api.CurrencySymbolWidthType;
import com.squarespace.cldrengine.api.CurrencyType;
import com.squarespace.cldrengine.api.Decimal;
import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.BaseFormatter;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Context;
import com.squarespace.template.GeneralUtils;
import com.squarespace.template.Variable;
import com.squarespace.template.Variables;
import com.squarespace.template.plugins.platform.CommerceUtils;


/**
 * MONEY - Formats the special money type, that has the following form:
 *
 *      {"decimalValue": "1.25", "currencyCode": "USD"}
 *
 * Options:
 *
 *   style:<name>     - pattern style:  symbol, accounting, name, code, short
 *   symbol:<type>    - currency symbol width: default, narrow
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
      // Check if we have a new money node and CLDR formatting is enabled.
      if (CommerceUtils.useCLDRMode(ctx)) {
        decimal = node.path("value");
        currency = node.path("currency");
      }

      // No valid money node found.
      if (decimal.isMissingNode() || currency.isMissingNode()) {
        var.setMissing();
        return;
      }
    }

    CLDR cldr = ctx.cldr();
    if (cldr == null) {
      var.setMissing();
      return;
    }

    String code = currency.asText();
    if (code == null) {
      var.setMissing();
      return;
    }

    Decimal decimalValue = GeneralUtils.nodeToDecimal(decimal);
    CurrencyType _currency = CurrencyType.fromString(code);

    CurrencyFormatOptions opts = (CurrencyFormatOptions) args.getOpaque();
    String result = cldr.Numbers.formatCurrency(decimalValue, _currency, opts);
    var.set(result);
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
        opts.style(CurrencyFormatStyleType.fromString(value));
        if (!opts.style.ok() && "standard".equals(value)) {
          opts.style(CurrencyFormatStyleType.SYMBOL);
        }

      } else if (arg.equals("symbol")) {
        opts.symbolWidth(CurrencySymbolWidthType.fromString(value));

      } else {
        DecimalFormatter.setNumberOption(arg, value, opts);
      }

    }
    return opts;
  }

}
