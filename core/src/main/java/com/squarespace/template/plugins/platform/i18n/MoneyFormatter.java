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
import com.squarespace.cldrengine.api.CurrencyType;
import com.squarespace.cldrengine.api.Decimal;
import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.BaseFormatter;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Context;
import com.squarespace.template.GeneralUtils;
import com.squarespace.template.OptionParsers;
import com.squarespace.template.Variable;
import com.squarespace.template.Variables;


/**
 * MONEY - Formats the special money type, that has the following form:
 *
 *      {"decimalValue": "1.25", "currencyCode": "USD"}
 */
public class MoneyFormatter extends BaseFormatter {

  public MoneyFormatter() {
    super("money", false);
  }

  @Override
  public void validateArgs(Arguments args) throws ArgumentsException {
    CurrencyFormatOptions opts = OptionParsers.currency(args);
    args.setOpaque(opts);
  }

  @Override
  public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
    Variable var = variables.first();
    JsonNode node = var.node();
    JsonNode decimalValue = node.path("decimalValue");
    JsonNode currencyNode = node.path("currencyCode");

    if (decimalValue.isMissingNode() || currencyNode.isMissingNode()) {
      // Fall back to the serialization paths for the new Money class.
      decimalValue = node.path("value");
      currencyNode = node.path("currency");
    }

    // No valid money node found.
    if (decimalValue.isMissingNode() || currencyNode.isMissingNode()) {
      var.setMissing();
      return;
    }

    CLDR cldr = ctx.cldr();
    String code = currencyNode.asText();
    Decimal decimal = GeneralUtils.nodeToDecimal(decimalValue);
    CurrencyType currency = CurrencyType.fromString(code);
    CurrencyFormatOptions opts = (CurrencyFormatOptions) args.getOpaque();
    String result = cldr.Numbers.formatCurrency(decimal, currency, opts);
    var.set(result);
  }

}
