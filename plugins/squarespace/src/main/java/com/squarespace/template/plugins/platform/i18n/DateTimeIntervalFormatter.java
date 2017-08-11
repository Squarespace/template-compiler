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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.squarespace.cldr.CLDR;
import com.squarespace.cldr.dates.CalendarFormatter;
import com.squarespace.cldr.dates.DateTimeIntervalSkeleton;
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
  }

  @Override
  public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
    int count = variables.count();
    if (count < 2) {
      return;
    }

    String tzName = PluginDateUtils.getTimeZoneNameFromContext(ctx);
    ZoneId zoneId = ZoneId.of(tzName);

    Variable first = variables.first();
    if (first.node().asLong() == 0) {
      first.setMissing();
      return;
    }

    ZonedDateTime start = parse(first, zoneId);
    ZonedDateTime end = parse(variables.get(1), zoneId);

    DateTimeIntervalSkeleton skeleton = null;
    if (count >= 3) {
      String skel = variables.get(2).node().asText();
      skeleton = DateTimeIntervalSkeleton.fromString(skel);
    }

    CLDR.Locale locale = ctx.cldrLocale();
    CalendarFormatter formatter = CLDR.get().getCalendarFormatter(locale);
    StringBuilder buffer = new StringBuilder();
    formatter.format(start, end, skeleton, buffer);
    first.set(buffer);
  }

  private ZonedDateTime parse(Variable var, ZoneId zoneId) {
    long instant = var.node().asLong();
    return ZonedDateTime.ofInstant(Instant.ofEpochMilli(instant), zoneId);
  }

}
