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

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.squarespace.cldr.CLDR;
import com.squarespace.cldr.CLDRLocale;
import com.squarespace.cldr.numbers.DecimalFormatOptions;
import com.squarespace.cldr.numbers.NumberFormatMode;
import com.squarespace.cldr.numbers.NumberFormatOptions;
import com.squarespace.cldr.numbers.DecimalFormatStyle;
import com.squarespace.cldr.numbers.NumberFormatter;
import com.squarespace.cldr.numbers.NumberRoundMode;
import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.BaseFormatter;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Context;
import com.squarespace.template.GeneralUtils;


/**
 * DECIMAL - Formatter for decimal numbers.
 *
 * Options:
 *
 *   style:<name>     - pattern style:  decimal, percent, permille, short, long.
 *   mode:<name>      - formatting mode: default, significant, significant-maxfrac
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
  public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
    BigDecimal number = GeneralUtils.nodeToBigDecimal(node);
    if (number == null) {
      return MissingNode.getInstance();
    }

    DecimalFormatOptions opts = (DecimalFormatOptions) args.getOpaque();
    CLDRLocale locale = ctx.cldrLocale();
    NumberFormatter fmt = CLDR.get().getNumberFormatter(locale);
    StringBuilder buf = new StringBuilder();
    fmt.formatDecimal(number, buf, opts);
    return ctx.buildNode(buf.toString());
  }

  private DecimalFormatOptions parseOptions(Arguments args) {
    DecimalFormatOptions opts = new DecimalFormatOptions();
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
          case "percent":
            opts.setStyle(DecimalFormatStyle.PERCENT);
            break;

          case "permille":
            opts.setStyle(DecimalFormatStyle.PERMILLE);
            break;

          case "short":
            opts.setStyle(DecimalFormatStyle.SHORT);
            break;

          case "long":
            opts.setStyle(DecimalFormatStyle.LONG);
            break;

          case "standard":
          case "decimal":
            opts.setStyle(DecimalFormatStyle.DECIMAL);
            break;

          default:
            break;
        }
        continue;
      }

      setNumberOption(arg, value, opts);
    }
    return opts;
  }

  /**
   * Interprets common options for DecimalFormatter and CurrencyFormatter.
   */
  protected static void setNumberOption(String arg, String value, NumberFormatOptions<?> opts) {
    switch (arg) {
      case "group":
      case "grouping":
        opts.setGrouping(true);
        break;

      case "no-group":
      case "no-grouping":
        opts.setGrouping(false);
        break;

      case "mode":
        switch (value) {
          case "significant":
            opts.setFormatMode(NumberFormatMode.SIGNIFICANT);
            break;

          case "significant-maxfrac":
          case "significant-maxFrac":
            opts.setFormatMode(NumberFormatMode.SIGNIFICANT_MAXFRAC);
            break;

          case "default":
          case "fractions":
            opts.setFormatMode(NumberFormatMode.DEFAULT);
            break;

          default:
            break;
        }
        break;

      case "round":
      case "rounding":
        switch (value) {
          case "ceil":
            opts.setRoundMode(NumberRoundMode.CEIL);
            break;

          case "truncate": opts.setRoundMode(NumberRoundMode.TRUNCATE); break;

          case "floor":
            opts.setRoundMode(NumberRoundMode.FLOOR);
            break;

          case "round":
          case "default":
          case "half-even":
            opts.setRoundMode(NumberRoundMode.ROUND);

          default:
            break;
        }
        break;

      case "minint":
      case "minInt":
        opts.setMinimumIntegerDigits(clamp(toInt(value), 0, CLAMP_MAX));
        break;

      case "maxfrac":
      case "maxFrac":
        opts.setMaximumFractionDigits(clamp(toInt(value), 0, CLAMP_MAX));
        break;

      case "minfrac":
      case "minFrac":
        opts.setMinimumFractionDigits(clamp(toInt(value), 0, CLAMP_MAX));
        break;

      case "maxsig":
      case "maxSig":
        opts.setMaximumSignificantDigits(clamp(toInt(value), 1, CLAMP_MAX));
        break;

      case "minsig":
      case "minSig":
        opts.setMinimumSignificantDigits(clamp(toInt(value), 1, CLAMP_MAX));
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
