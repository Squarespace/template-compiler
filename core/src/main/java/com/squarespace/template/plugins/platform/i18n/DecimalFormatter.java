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

import com.squarespace.cldrengine.CLDR;
import com.squarespace.cldrengine.api.Decimal;
import com.squarespace.cldrengine.api.DecimalFormatOptions;
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
 * DECIMAL - Formatter for decimal numbers.
 */
public class DecimalFormatter extends BaseFormatter {

  public DecimalFormatter() {
    super("decimal", false);
  }

  @Override
  public void validateArgs(Arguments args) throws ArgumentsException {
    DecimalFormatOptions opts = OptionParsers.decimal(args);
    args.setOpaque(opts);
  }

  @Override
  public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
    Variable var = variables.first();
    Decimal number = GeneralUtils.nodeToDecimal(var.node());
    if (number == null) {
      var.setMissing();
      return;
    }

    CLDR cldr = ctx.cldr();
    DecimalFormatOptions opts = (DecimalFormatOptions) args.getOpaque();
    String result = cldr.Numbers.formatDecimal(number, opts);
    var.set(result);
  }

}
