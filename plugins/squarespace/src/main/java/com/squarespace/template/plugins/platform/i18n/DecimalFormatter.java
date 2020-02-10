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

import static com.squarespace.cldrengine.utils.StringUtils.isEmpty;

import com.squarespace.cldrengine.CLDR;
import com.squarespace.cldrengine.api.Decimal;
import com.squarespace.cldrengine.api.DecimalFormatOptions;
import com.squarespace.cldrengine.api.DecimalFormatStyleType;
import com.squarespace.cldrengine.api.NumberFormatOptions;
import com.squarespace.cldrengine.api.RoundingModeType;
import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.BaseFormatter;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Context;
import com.squarespace.template.GeneralUtils;
import com.squarespace.template.Variable;
import com.squarespace.template.Variables;


/**
 * DECIMAL - Formatter for decimal numbers.
 *
 * Options:
 *
 *   style:<name>     - pattern style:  decimal, short, long, percent, permille
 *                                      percent-scaled, permille-scaled
 *   round:<name>     - rounding mode: default, ceil, floor, truncate
 *   group            - if present this enables digit grouping
 *
 * Used in default mode:
 *   minint:<int>     - in default mode, sets the minimum number of integer digit (zero pads)
 *   minfrac:<int>    - sets the minimum number of fraction digits
 *
 * Used in default and significant-maxfrac modes:
 *   maxfrac:<int>    - sets the maximum number of fraction digits
 *
 * Used in significant and significant-maxfrac modes:
 *   maxsig:<int>     - sets the maximum number of significant digits
 *   minsig:<int>     - sets the minimum number of significant digits
 */
public class DecimalFormatter extends BaseFormatter {

  // Arbitrary value just to have an upper limit.
  private static final int CLAMP_MAX = 50;

  public DecimalFormatter() {
    super("decimal", false);
  }

  @Override
  public void validateArgs(Arguments args) throws ArgumentsException {
    DecimalFormatOptions opts = parseOptions(args);
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
    if (cldr == null) {
      var.set("");
      return;
    }

    DecimalFormatOptions opts = (DecimalFormatOptions) args.getOpaque();
    String result = cldr.Numbers.formatDecimal(number, opts);
    var.set(result);
  }

  private DecimalFormatOptions parseOptions(Arguments args) {
    DecimalFormatOptions opts = new DecimalFormatOptions();
    int count = args.count();
    for (int i = 0; i < count; i++) {
      String arg = args.get(i);
      String value = "";
      int index = arg.indexOf(':');
      if (index != -1) {
        value = arg.substring(index + 1);
        arg = arg.substring(0, index);
      }

      if (arg.equals("style")) {
        opts.style(DecimalFormatStyleType.fromString(value));
      } else {
        setNumberOption(arg, value, opts);
      }
    }
    return opts;
  }

  /**
   * Interprets common options for DecimalFormatter and CurrencyFormatter.
   */
  protected static void setNumberOption(String arg, String value, NumberFormatOptions opts) {
    switch (arg) {
      case "group":
      case "grouping":
        opts.group(isEmpty(value) || value.equals("true"));
        break;

      case "no-group":
      case "no-grouping":
        opts.group(false);
        break;

      case "round":
      case "rounding":
        RoundingModeType mode = null;
        switch (value) {
          case "ceil":
            mode = RoundingModeType.CEILING;
            break;
          case "truncate":
            mode = RoundingModeType.DOWN;
            break;
          default:
            mode = RoundingModeType.fromString(value);
            break;
        }
        opts.round(mode);
        break;

      case "minint":
      case "minInt":
      case "minimumIntegerDigits":
        opts.minimumIntegerDigits(clamp(toInt(value), 0, CLAMP_MAX));
        break;

      case "maxfrac":
      case "maxFrac":
      case "maximumFractionDigits":
        opts.maximumFractionDigits(clamp(toInt(value), 0, CLAMP_MAX));
        break;

      case "minfrac":
      case "minFrac":
      case "minimumFractionDigits":
        opts.minimumFractionDigits(clamp(toInt(value), 0, CLAMP_MAX));
        break;

      case "maxsig":
      case "maxSig":
      case "maximumSignificantDigits":
        opts.maximumSignificantDigits(clamp(toInt(value), 0, CLAMP_MAX));
        break;

      case "minsig":
      case "minSig":
      case "minimumSignificantDigits":
        opts.minimumSignificantDigits(clamp(toInt(value), 0, CLAMP_MAX));
        break;

      default:
        break;
    }
  }

  private static int toInt(String v) {
    return (int) GeneralUtils.toLong(v, 0, v.length());
  }

  private static int clamp(int value, int min, int max) {
    return value < min ? min : (value > max ? max : value);
  }

}
