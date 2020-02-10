/**
 * Copyright (c) 2017 SQUARESPACE, Inc.
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

import com.squarespace.cldrengine.CLDR;
import com.squarespace.cldrengine.api.CalendarDate;
import com.squarespace.cldrengine.api.ContextType;
import com.squarespace.cldrengine.api.DateFormatOptions;
import com.squarespace.cldrengine.api.FormatWidthType;
import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.BaseFormatter;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Context;
import com.squarespace.template.Variable;
import com.squarespace.template.Variables;
import com.squarespace.template.plugins.PluginDateUtils;


/**
 * DATETIME - Locale formatted dates according to Unicode CLDR rules. http://cldr.unicode.org/
 *
 * TODO: revise docs below
 *
 * This formatter takes up to 3 arguments of the following types:
 *
 * Named date format: 'date:short', 'date:medium', 'date:long', 'date:full' Named time format: 'time:short',
 * 'time:medium', 'time:long', 'time:full' Skeleton date format: 'date:yMMMd', 'date:yQQQQ', 'date:yM', etc. Skeleton
 * time format: 'time:Hmv', 'time:hms', etc. Wrapper format: 'wrap:short', 'wrap:medium', 'wrap:long', 'wrap:full'
 *
 * You can provide a date or time name/skeleton argument, and an optional wrapper argument:
 *
 * {t|datetime date:long time:short wrap:full} "November 2, 2017 at 2:26 PM"
 *
 * {t|datetime date:short time:full wrap:short} "11/2/17, 2:26:57 PM"
 *
 * If no wrapper argument is specified, it is based on the date format. The following will use "wrap:medium" since
 * "date:medium" is present:
 *
 * {t|datetime date:medium time:short} "Nov 2, 2017, 2:26 PM"
 *
 * You can also pass a date or time skeleton:
 *
 * {t|datetime date:yMMMd time:hm wrap:long} "Nov 2, 2017 at 2:26 PM"
 *
 * Order of arguments doesn't matter, so this is equivalent to the above:
 *
 * {t|datetime wrap:long time:hm date:yMMMd} "Nov 2, 2017 at 2:26 PM"
 *
 * If you provide no arguments you get the equivalent of "date:yMd":
 *
 * {t|datetime} "11/2/2017"
 *
 */
public class DateTimeFormatter extends BaseFormatter {

  public DateTimeFormatter() {
    super("datetime", false);
  }

  @Override
  public void validateArgs(Arguments args) throws ArgumentsException {
    DateFormatOptions options = new DateFormatOptions();
    setDateFormatOptions(options, args);
    args.setOpaque(options);
  }

  @Override
  public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
    Variable var = variables.first();
    long epoch = var.node().asLong();
    String zoneId = PluginDateUtils.getTimeZoneNameFromContext(ctx);
    CLDR cldr = ctx.cldr();
    if (cldr == null) {
      var.set("");
      return;
    }
    DateFormatOptions options = (DateFormatOptions) args.getOpaque();
    CalendarDate date = cldr.Calendars.toGregorianDate(epoch, zoneId);
    String result = cldr.Calendars.formatDate(date, options);
    var.set(result);
  }

  private static void setDateFormatOptions(DateFormatOptions options, Arguments args) {
    for (String arg : args.getArgs()) {
      int i = arg.indexOf(':');
      if (i == -1) {
        switch (arg) {
          case "date":
            options.date(FormatWidthType.SHORT);
            break;

          case "time":
            options.time(FormatWidthType.SHORT);
            break;

          default:
            FormatWidthType format = FormatWidthType.fromString(arg);
            if (format != null) {
              options.datetime(format);
            } else {
              // add skeleton fields
              String skel = options.skeleton.get();
              options.skeleton(skel == null ? arg : skel + arg);
            }
            break;
        }
        continue;
      }

      String key = arg.substring(0, i);
      String val = arg.substring(i + 1);

      switch (key) {
        case "context":
          options.context(ContextType.fromString(val));
          break;

        case "date":
        case "time":
        case "datetime": {
          FormatWidthType format = FormatWidthType.fromString(val);
          if (format != null) {
            if (key.equals("date")) {
              options.date(format);
            } else if (key.equals("time")) {
              options.time(format);
            } else {
              options.datetime(format);
            }
          } else {
            copySkel(options, val);
          }
          break;
        }

        case "skeleton":
          copySkel(options, val);
          break;

        case "wrap":
        case "wrapper":
        case "wrapped": {
          options.wrap(FormatWidthType.fromString(val));
          break;
        }

        default:
          break;
      }
    }
  }

  /**
   * If a skeleton was given, append to the existing skeleton. Then check
   * if any date, time, or datetime options were already set. If so,
   * convert them into skeletons and append.
   */
  private static final void copySkel(DateFormatOptions opts, String val) {
    String skel = opts.skeleton.get();
    skel = skel == null ? val : skel + val;
    if (opts.date.ok()) {
      skel += skelFromDate(opts.date.get());
    }
    if (opts.time.ok()) {
      skel += skelFromTime(opts.time.get());
    }
    if (opts.datetime.ok()) {
      FormatWidthType type = opts.datetime.get();
      skel += skelFromDate(type) + skelFromTime(type);
    }
    opts.skeleton(skel);
    opts.date.clear();
    opts.time.clear();
    opts.datetime.clear();
  }

  private static final String skelFromDate(FormatWidthType type) {
    if (type != null) {
      switch (type) {
        case FULL:
          return "EEEEyMMMMd";
        case LONG:
          return "yMMMMd";
        case MEDIUM:
          return "yMMMd";
        case SHORT:
          return "yyMd";
      }
    }
    return "";
  }

  private static final String skelFromTime(FormatWidthType type) {
    if (type != null) {
      switch (type) {
        case FULL:
          return "hmmssazzzz";
        case LONG:
          return "hmmssaz";
        case MEDIUM:
          return "hmmssa";
        case SHORT:
          return "hmma";
      }
    }
    return "";
  }

}
