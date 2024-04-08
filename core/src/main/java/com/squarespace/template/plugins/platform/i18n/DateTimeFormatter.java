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
import com.squarespace.cldrengine.api.DateFormatOptions;
import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.BaseFormatter;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Context;
import com.squarespace.template.OptionParsers;
import com.squarespace.template.Options;
import com.squarespace.template.Variable;
import com.squarespace.template.Variables;
import com.squarespace.template.plugins.PluginDateUtils;


/**
 * DATETIME - Locale formatted dates according to Unicode CLDR rules. http://cldr.unicode.org/
 */
public class DateTimeFormatter extends BaseFormatter {

  public DateTimeFormatter() {
    super("datetime", false);
  }

  @Override
  public void validateArgs(Arguments args) throws ArgumentsException {
    Options<DateFormatOptions> options = OptionParsers.datetime(args);
    args.setOpaque(options);
  }

  @Override
  public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
    Variable var = variables.first();
    long epoch = var.node().asLong();
    String zoneId = PluginDateUtils.getTimeZoneNameFromContext(ctx);
    @SuppressWarnings("unchecked")
    Options<DateFormatOptions> options = (Options<DateFormatOptions>) args.getOpaque();
    CLDR cldr = ctx.localeManager().get(options.localeName()).cldr();
    CalendarDate date = cldr.Calendars.toGregorianDate(epoch, zoneId);
    String result = cldr.Calendars.formatDate(date, options.inner());
    var.set(result);
  }

}
