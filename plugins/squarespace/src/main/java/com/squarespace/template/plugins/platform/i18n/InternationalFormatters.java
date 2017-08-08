/**
 * Copyright (c) 2015 SQUARESPACE, Inc.
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
import com.squarespace.cldr.CLDRLocale;
import com.squarespace.cldr.dates.CalendarFormatter;
import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.BaseFormatter;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Context;
import com.squarespace.template.Formatter;
import com.squarespace.template.FormatterRegistry;
import com.squarespace.template.StringView;
import com.squarespace.template.SymbolTable;
import com.squarespace.template.Variable;
import com.squarespace.template.Variables;


public class InternationalFormatters implements FormatterRegistry {

  @Override
  public void registerFormatters(SymbolTable<StringView, Formatter> table) {
    table.add(new DateTimeFormatter());
    table.add(new DateTimeFieldFormatter());
    table.add(new DateTimeIntervalFormatter());
    table.add(new DecimalFormatter());
    table.add(new MessageFormatter());
    table.add(new MoneyFormatter());
    table.add(new LegacyMoneyFormatter());
    table.add(new PluralFormatter());
  }

  /**
   * DEPRECATE in favor of using MessageFormatter.
   */
  private static class PluralFormatter extends MessageFormatter {

    PluralFormatter() {
      super("plural");
    }
  }

  /**
   * DATETIMEFIELD - Format a single localized date or time field.
   *
   * This formatter takes 1 argument that represents a single date-time field of a
   * given width. This can be used if you simply want to print the year, name of
   * the current month or weekday, etc.
   *
   * Date fields are represented by a single character. The fields are defined in
   * the CLDR specification (not all fields are currently supported):
   *
   *  http://www.unicode.org/reports/tr35/tr35-dates.html#Date_Field_Symbol_Table
   *
   * Repeating the character increases the "field width":
   *
   *     {t|datetimefield M}
   *     11
   *
   *     {t|datetimefield MMM}
   *     "Nov"
   *
   *     {t|datetimefield MMMM}
   *     "November"
   *
   *     {t|datetimefield EEE}
   *     "Thu"
   *
   *     {t|datetimefield EEEE}
   *     "Thursday"
   *
   */
  public static class DateTimeFieldFormatter extends BaseFormatter {

    private static final ZoneId DEFAULT_ZONEID = ZoneId.of("America/New_York");

    public DateTimeFieldFormatter() {
      super("datetimefield", true);
    }

    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
      args.exactly(1);
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      long instant = var.node().asLong();

      // TODO: obtain timezone via context or resolve via json.
      ZonedDateTime dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(instant), DEFAULT_ZONEID);

      CLDRLocale locale = ctx.cldrLocale();
      CalendarFormatter formatter = CLDR.get().getCalendarFormatter(locale);

      StringBuilder buf = new StringBuilder();
      formatter.formatField(dateTime, args.first(), buf);
      var.set(buf);
    }
  }

}
