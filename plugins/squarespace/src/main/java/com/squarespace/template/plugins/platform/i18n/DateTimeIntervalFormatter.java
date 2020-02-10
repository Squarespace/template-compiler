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
import com.squarespace.cldrengine.api.CalendarDate;
import com.squarespace.cldrengine.api.ContextType;
import com.squarespace.cldrengine.api.DateIntervalFormatOptions;
import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.BaseFormatter;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Context;
import com.squarespace.template.Variable;
import com.squarespace.template.Variables;
import com.squarespace.template.plugins.PluginDateUtils;


/**
 * Formats date time intervals using Unicode CLDR.
 */
public class DateTimeIntervalFormatter extends BaseFormatter {

  public DateTimeIntervalFormatter() {
    super("datetime-interval", false);
  }

  @Override
  public void validateArgs(Arguments args) throws ArgumentsException {
    DateIntervalFormatOptions options = DateIntervalFormatOptions.build();
    setDateIntervalFormatOptions(options, args);
    args.setOpaque(options);
  }

  @Override
  public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
    int count = variables.count();
    if (count < 2) {
      return;
    }

    Variable v1 = variables.get(0);
    Variable v2 = variables.get(1);

    CLDR cldr = ctx.cldr();
    if (cldr == null) {
      v1.set("");
      return;
    }

    String zoneId = PluginDateUtils.getTimeZoneNameFromContext(ctx);
    CalendarDate start = cldr.Calendars.toGregorianDate(v1.node().asLong(0), zoneId);
    CalendarDate end = cldr.Calendars.toGregorianDate(v2.node().asLong(0), zoneId);
    DateIntervalFormatOptions options = (DateIntervalFormatOptions) args.getOpaque();
    String result = cldr.Calendars.formatDateInterval(start, end, options);
    v1.set(result);
  }

  private static void setDateIntervalFormatOptions(DateIntervalFormatOptions options, Arguments args) {
    for (String arg : args.getArgs()) {
      int i = arg.indexOf(':');
      if (i == -1) {
        switch (arg) {
          case "context":
          case "skeleton":
          case "date":
          case "time":
            break;
          default:
            options.skeleton(arg);
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
        case "skeleton":
          options.skeleton(val);
          break;
        case "date":
          options.date(val);
          break;
        case "time":
          options.time(val);
          break;
      }
    }
  }
}
