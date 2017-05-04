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

import static com.squarespace.cldr.CLDRCalendarUtils.skeletonType;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.cldr.CLDR;
import com.squarespace.cldr.CLDRLocale;
import com.squarespace.cldr.dates.CLDRCalendarFormatter;
import com.squarespace.cldr.dates.FormatType;
import com.squarespace.cldr.dates.SkeletonType;
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

  @Override
  public void registerFormatters(SymbolTable<StringView, Formatter> table) {
    table.add(new DateTimeFormatter());
    table.add(new DateTimeFieldFormatter());
    table.add(new MoneyFormatter());
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
   *
   */
  public static class DateTimeFormatter extends BaseFormatter {

    private static final ZoneId DEFAULT_ZONEID = ZoneId.of("America/New_York");

    public DateTimeFormatter() {
      super("datetime", true);
    }

    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
      args.atMost(3);
      args.setOpaque(new DateTimeFlags(args));
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      long instant = ctx.node().asLong();

      // TODO: obtain timezone via context or resolve via json.
      ZonedDateTime dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(instant), DEFAULT_ZONEID);

      CLDRLocale locale = ctx.cldrLocale();
      CLDRCalendarFormatter formatter = CLDR.get().getCalendarFormatter(locale);

      StringBuilder buf = new StringBuilder();

      DateTimeFlags flags = (DateTimeFlags) args.getOpaque();

      if (flags.wrapperType != null) {
        // If we're here, it means that we have both a date and a time, either named
        // or skeleton, and are using a localized wrapper.
        formatter.formatWrapped(flags.wrapperType, flags.dateType, flags.timeType,
            flags.dateSkeleton, flags.timeSkeleton, dateTime, buf);

      } else if (flags.dateType != null) {
        formatter.formatDate(flags.dateType, dateTime, buf);

      } else if (flags.timeType != null) {
        formatter.formatTime(flags.timeType, dateTime, buf);

      } else {
        String skeleton = flags.dateSkeleton != null ? flags.dateSkeleton : flags.timeSkeleton;
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
   * Repeating the format character changes the "field width":
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
      CLDRCalendarFormatter formatter = CLDR.get().getCalendarFormatter(locale);

      StringBuilder buf = new StringBuilder();
      formatter.formatField(args.first(), dateTime, buf);
      return ctx.buildNode(buf.toString());
    }
  }

  /**
   * You can format a date, a time, or a combination of either a date and time,
   * named (short, medium, etc) or a skeleton (yMMMd, hm, etc).
   */
  private static class DateTimeFlags {

    FormatType wrapperType;
    FormatType dateType;
    FormatType timeType;
    String dateSkeleton;
    String timeSkeleton;

    public DateTimeFlags(Arguments args) {
      // If you specify no arguments you'll still get something
      if (args.count() == 0) {
        dateType = FormatType.LONG;
        timeType = FormatType.LONG;
        wrapperType = FormatType.LONG;
        return;
      }

      // Note: Specifying at least one of the date-* or time-* flags will ignore
      // all other arguments.
      for (String arg : args.getArgs()) {
        switch (arg) {
          case "date":
          case "date-short":
            this.dateType = FormatType.SHORT;
            break;

          case "date-medium":
            this.dateType = FormatType.MEDIUM;
            break;

          case "date-long":
            this.dateType = FormatType.LONG;
            break;

          case "date-full":
            this.dateType = FormatType.FULL;
            break;

          case "time":
          case "time-short":
            this.timeType = FormatType.SHORT;
            break;

          case "time-medium":
            this.timeType = FormatType.MEDIUM;
            break;

          case "time-long":
            this.timeType = FormatType.LONG;
            break;

          case "time-full":
            this.timeType = FormatType.FULL;
            break;

          case "wrap":
          case "wrap-short":
            this.wrapperType = FormatType.SHORT;
            break;

          case "wrap-medium":
            this.wrapperType = FormatType.MEDIUM;
            break;

          case "wrap-long":
            this.wrapperType = FormatType.LONG;
            break;

          case "wrap-full":
            this.wrapperType = FormatType.FULL;
            break;

          default:
            if (skeletonType(arg) == SkeletonType.DATE) {
              dateSkeleton = arg;
            } else {
              timeSkeleton = arg;
            }
            break;
        }
      }

      boolean haveBoth = (dateType != null || dateSkeleton != null)
          && (timeType != null || timeSkeleton != null);

      // Based on which values exist, select an automatic wrapper type.
      if (wrapperType == null) {
        if (timeSkeleton != null || timeType != null) {
          wrapperType = (dateType != null) ? dateType : (dateSkeleton != null ? FormatType.SHORT : null);
        }
      } else if (!haveBoth) {
        // can't use the wrapper for just a date or time.
        wrapperType = null;
      }

    }
  }

}
