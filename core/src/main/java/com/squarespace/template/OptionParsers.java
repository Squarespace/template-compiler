/**
 * Copyright (c) 2020 SQUARESPACE, Inc.
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

package com.squarespace.template;

import static com.squarespace.cldrengine.utils.StringUtils.isEmpty;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.squarespace.cldrengine.api.ContextType;
import com.squarespace.cldrengine.api.CurrencyFormatOptions;
import com.squarespace.cldrengine.api.CurrencyFormatStyleType;
import com.squarespace.cldrengine.api.CurrencySymbolWidthType;
import com.squarespace.cldrengine.api.DateFieldWidthType;
import com.squarespace.cldrengine.api.DateFormatOptions;
import com.squarespace.cldrengine.api.DateIntervalFormatOptions;
import com.squarespace.cldrengine.api.DecimalFormatOptions;
import com.squarespace.cldrengine.api.DecimalFormatStyleType;
import com.squarespace.cldrengine.api.FormatWidthType;
import com.squarespace.cldrengine.api.NumberFormatOptions;
import com.squarespace.cldrengine.api.RelativeTimeFormatOptions;
import com.squarespace.cldrengine.api.RoundingModeType;
import com.squarespace.cldrengine.api.TimePeriodField;

public class OptionParsers {

  // Arbitrary value just to have an upper limit.
  private static final int CLAMP_MAX = 50;

  private OptionParsers() {

  }

  public static Options<DecimalFormatOptions> decimal(Arguments args) {
    return decimal(args.getArgs());
  }

  public static Options<DecimalFormatOptions> decimal(List<String> args) {
    Options<DecimalFormatOptions> options = new Options<>(null, new DecimalFormatOptions());
    parse(args, options, OptionParsers::decimalOption);
    return options;
  }

  public static Options<CurrencyFormatOptions> currency(Arguments args) {
    return currency(args.getArgs());
  }

  public static Options<CurrencyFormatOptions> currency(List<String> args) {
    Options<CurrencyFormatOptions> options = new Options<>(null, new CurrencyFormatOptions());
    parse(args, options, OptionParsers::currencyOption);
    return options;
  }

  public static Options<DateFormatOptions> datetime(Arguments args) {
    return datetime(args.getArgs());
  }

  public static Options<DateFormatOptions> datetime(List<String> args) {
    Options<DateFormatOptions> options = new Options<>(null, DateFormatOptions.build());
    parse(args, options, OptionParsers::datetimeOption);
    return options;
  }

  public static Options<DateIntervalFormatOptions> interval(Arguments args) {
    return interval(args.getArgs());
  }

  public static Options<DateIntervalFormatOptions> interval(List<String> args) {
    Options<DateIntervalFormatOptions> options = new Options<>(null, DateIntervalFormatOptions.build());
    parse(args, options, OptionParsers::intervalOption);
    return options;
  }

  public static Options<RelativeTimeFormatOptions> relativetime(Arguments args) {
    return relativetime(args.getArgs());
  }

  public static Options<RelativeTimeFormatOptions> relativetime(List<String> args) {
    Options<RelativeTimeFormatOptions> options = new Options<>(null, RelativeTimeFormatOptions.build());
    parse(args, options, OptionParsers::relativetimeOption);
    return options;
  }

  private static void decimalOption(String arg, String value, Options<DecimalFormatOptions> options) {
    DecimalFormatOptions inner = options.inner();
    switch (arg) {
      case "locale":
        options.localeName(value);
        break;

      case "style":
        inner.style(DecimalFormatStyleType.fromString(value));
        if (!inner.style.ok() && "standard".equals(value)) {
          inner.style(DecimalFormatStyleType.DECIMAL);
        }
        break;

      default:
        numberOption(arg, value, inner);
    }
  }

  private static void currencyOption(String arg, String value, Options<CurrencyFormatOptions> options) {
    CurrencyFormatOptions inner = options.inner();
    switch (arg) {
      case "locale":
        options.localeName(value);
        break;

      case "style":
        inner.style(CurrencyFormatStyleType.fromString(value));
        if (!inner.style.ok() && "standard".equals(value)) {
          inner.style(CurrencyFormatStyleType.SYMBOL);
        }
        break;

      case "symbol":
        inner.symbolWidth(CurrencySymbolWidthType.fromString(value));
        break;

      default:
        numberOption(arg, value, inner);
    }
  }

  private static void numberOption(String arg, String value, NumberFormatOptions opts) {
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

  private static void datetimeOption(String arg, String value, Options<DateFormatOptions> options) {
    DateFormatOptions inner = options.inner();
    if (StringUtils.isEmpty(value)) {
      switch (arg) {
        case "date":
          inner.date(FormatWidthType.SHORT);
          break;

        case "time":
          inner.time(FormatWidthType.SHORT);
          break;

        default:
          FormatWidthType format = FormatWidthType.fromString(arg);
          if (format != null) {
            inner.datetime(format);
          } else {
            // add skeleton fields
            String skel = inner.skeleton.get();
            inner.skeleton(skel == null ? arg : skel + arg);
          }
          break;
      }
      return;
    }

    switch (arg) {
      case "locale":
        options.localeName(value);
        break;

      case "context":
        inner.context(ContextType.fromString(value));
        break;

      case "date":
      case "time":
      case "datetime": {
        FormatWidthType format = FormatWidthType.fromString(value);
        if (format != null) {
          if (arg.equals("date")) {
            inner.date(format);
          } else if (arg.equals("time")) {
            inner.time(format);
          } else {
            inner.datetime(format);
          }
        } else {
          // add skeleton fields
          String skel = inner.skeleton.get();
          inner.skeleton(skel == null ? value : skel + value);
        }
        break;
      }

      case "skeleton":
        inner.skeleton(value);
        break;

      case "wrap":
      case "wrapper":
      case "wrapped": {
        inner.wrap(FormatWidthType.fromString(value));
        break;
      }

      default:
        break;
    }
  }

  private static void intervalOption(String arg, String value, Options<DateIntervalFormatOptions> options) {
    DateIntervalFormatOptions inner = options.inner();
    if (StringUtils.isEmpty(value)) {
      switch (arg) {
        case "context":
        case "skeleton":
        case "date":
        case "time":
          break;
        default:
          if (!StringUtils.isEmpty(arg)) {
            inner.skeleton(arg);
          }
          break;
      }
      return;
    }

    switch (arg) {
      case "locale":
        options.localeName(value);
        break;
      case "context":
        inner.context(ContextType.fromString(value));
        break;
      case "skeleton":
        inner.skeleton(value);
        break;
      case "date":
        inner.date(value);
        break;
      case "time":
        inner.time(value);
        break;
      default:
        break;
    }
  }

  private static void relativetimeOption(String arg, String value, Options<RelativeTimeFormatOptions> options) {
    RelativeTimeFormatOptions inner = options.inner();
    switch (arg) {
      case "locale":
        options.localeName(value);
        break;
      case "context":
        inner.context(ContextType.fromString(value));
        break;
      case "dayOfWeek":
        inner.dayOfWeek("true".equals(value));
        break;
      case "field":
        inner.field(TimePeriodField.fromString(value));
        break;
      case "numericOnly":
        inner.numericOnly("true".equals(value));
        break;
      case "alwaysNow":
        inner.alwaysNow("true".equals(value));
        break;
      case "width":
        inner.width(DateFieldWidthType.fromString(value));
        break;
      default:
        numberOption(arg, value, inner);
        break;
    }
  }

  interface Parser<T> {
    void apply(String arg, String value, T options);
  }

  private static <T> void parse(List<String> args, T options, Parser<T> parser) {
    if (args == null) {
      return;
    }
    int count = args.size();
    for (int i = 0; i < count; i++) {
      String arg = args.get(i);
      String value = "";
      int index = arg.indexOf(':');
      if (index != -1) {
        value = arg.substring(index + 1);
        arg = arg.substring(0, index);
      }
      parser.apply(arg, value, options);
    }
  }

  private static int toInt(String v) {
    return (int)GeneralUtils.toPositiveLong(v, 0, v.length());
  }

  private static int clamp(int value, int min, int max) {
    return value < min ? min : (value > max ? max : value);
  }

}
