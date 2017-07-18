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

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.cldr.CLDR;
import com.squarespace.cldr.CLDRLocale;
import com.squarespace.cldr.DateTimeOptions;
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


public class InternationalFormatters implements FormatterRegistry {

  private static final ZoneId DEFAULT_ZONEID = ZoneId.of("America/New_York");

  @Override
  public void registerFormatters(SymbolTable<StringView, Formatter> table) {
    table.add(new DateTimeFormatter());
    table.add(new DateTimeFieldFormatter());
    table.add(new MessageFormatter());
    table.add(new MoneyFormatter());
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
   * DATETIME - Locale formatted dates according to Unicode CLDR rules. http://cldr.unicode.org/
   *
   * This formatter takes up to 3 arguments of the following types:
   *
   *     Named date format:   'date-short', 'date-medium', 'date-long', 'date-full'
   *     Named time format:   'time-short', 'time-medium', 'time-long', 'time-full'
   *  Skeleton date format:   'yMMMd', 'yQQQQ', 'yM', etc.
   *  Skeleton time format:   'Hmv', 'hms', etc.
   *        Wrapper format:   'wrap-short', 'wrap-medium', 'wrap-long', 'wrap-full'
   *
   * You can provide a date or time name/skeleton argument, and an optional
   * wrapper argument:
   *
   *     {t|datetime date-long time-short wrap-full}
   *     "November 2, 2017 at 2:26 PM"
   *
   *     {t|datetime date-short time-full wrap-short}
   *     "11/2/17, 2:26:57 PM"
   *
   * If no wrapper argument is specified, it is based on the date format. The
   * following will use "wrap-medium" since "date-medium" is present:
   *
   *     {t|datetime date-medium time-short}
   *     "Nov 2, 2017, 2:26 PM"
   *
   * You can also pass a date or time skeleton:
   *
   *     {t|datetime yMMMd hm wrap-long}
   *     "Nov 2, 2017 at 2:26 PM"
   *
   * Order of arguments doesn't matter, so this is equivalent to the above:
   *
   *     {t|datetime wrap-long hm yMMMd}
   *     "Nov 2, 2017 at 2:26 PM"
   *
   * If you provide no arguments you get the equivalent of "date-long time-long":
   *
   *     {t|datetime}
   *     "November 2, 2017 at 2:26:57 PM"
   *
   */
  public static class DateTimeFormatter extends BaseFormatter {

    public DateTimeFormatter() {
      super("datetime", false);
    }

    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
      args.atMost(3);
      DateTimeOptions options = new DateTimeOptions();
      options.set(args.getArgs());
      options.done();
      args.setOpaque(options);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      long instant = ctx.node().asLong();

      // TODO: obtain timezone via context or resolve via json.
      ZonedDateTime dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(instant), DEFAULT_ZONEID);

      CLDRLocale locale = ctx.cldrLocale();
      CalendarFormatter formatter = CLDR.get().getCalendarFormatter(locale);

      StringBuilder buf = new StringBuilder();

      DateTimeOptions options = (DateTimeOptions) args.getOpaque();

      if (options.wrapperType() != null) {
        // If we're here, it means that we have both a date and a time, either named
        // or skeleton, and are using a localized wrapper.
        formatter.formatWrapped(options.wrapperType(), options.dateType(), options.timeType(),
            options.dateSkeleton(), options.timeSkeleton(), dateTime, buf);

      } else if (options.dateType() != null) {
        formatter.formatDate(options.dateType(), dateTime, buf);

      } else if (options.timeType() != null) {
        formatter.formatTime(options.timeType(), dateTime, buf);

      } else {
        String skeleton = options.dateSkeleton() != null ? options.dateSkeleton() : options.timeSkeleton();
        formatter.formatSkeleton(skeleton, dateTime, buf);
      }

      return ctx.buildNode(buf.toString());
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
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      long instant = ctx.node().asLong();

      // TODO: obtain timezone via context or resolve via json.
      ZonedDateTime dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(instant), DEFAULT_ZONEID);

      CLDRLocale locale = ctx.cldrLocale();
      CalendarFormatter formatter = CLDR.get().getCalendarFormatter(locale);

      StringBuilder buf = new StringBuilder();
      formatter.formatField(args.first(), dateTime, buf);
      return ctx.buildNode(buf.toString());
    }
  }

}
