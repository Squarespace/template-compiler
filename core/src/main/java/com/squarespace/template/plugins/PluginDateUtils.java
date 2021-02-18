/**
 * Copyright (c) 2014 SQUARESPACE, Inc.
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

package com.squarespace.template.plugins;

import static com.squarespace.template.plugins.PluginUtils.leftPad;

import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.cldrengine.CLDR;
import com.squarespace.cldrengine.api.Bundle;
import com.squarespace.cldrengine.api.GregorianDate;
import com.squarespace.cldrengine.internal.CalendarFields;
import com.squarespace.template.Constants;
import com.squarespace.template.Context;


public class PluginDateUtils {

  private static final String DEFAULT_TIMEZONEID = "America/New_York";

  private static final String[] SHORT_DAYS = new String[] {
    "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
  };
  private static final String[] LONG_DAYS = new String[] {
    "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
  };
  private static final String[] SHORT_MONTHS = new String[] {
    "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
  };
  private static final String[] LONG_MONTHS = new String[] {
      "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"
  };

  private PluginDateUtils() {
  }

  enum DatePartType {
    YEAR("year"),
    MONTH("month"),
    WEEK("week"),
    DAY("day"),
    HOUR("hour"),
    MINUTE("minute"),
    SECOND("second");

    private String name;

    DatePartType(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  private static void humanizeDatePlural(int value, DatePartType type, StringBuilder buf) {
    buf.append("about ");
    switch (type) {
      case HOUR:
        if (value == 1) {
          buf.append("an hour");
        } else {
          buf.append(value).append(' ').append(type).append('s');
        }
        break;

      default:
        if (value == 1) {
          buf.append("a ").append(type);
        } else {
          buf.append(value).append(' ').append(type).append('s');
        }
    }
    buf.append(" ago");
  }

  public static void humanizeDate(long instantMs, long now, boolean showSeconds, StringBuilder buf) {
    humanizeDate(instantMs, now, TimeZone.getDefault().getID(), showSeconds, buf);
  }

  public static void humanizeDate(long instantMs, long baseMs, String tzId, boolean showSeconds, StringBuilder buf) {
    DateTimeZone timeZone = DateTimeZone.forID(tzId);
    int offset = timeZone.getOffset(instantMs);
    Duration delta = new Duration(baseMs - instantMs + offset);

    int days = (int)delta.getStandardDays();
    int years = (int)Math.floor(days / 365.0);
    if (years > 0) {
      humanizeDatePlural(years, DatePartType.YEAR, buf);
      return;
    }
    int months = (int)Math.floor(days / 30.0);
    if (months > 0) {
      humanizeDatePlural(months, DatePartType.MONTH, buf);
      return;
    }
    int weeks = (int)Math.floor(days / 7.0);
    if (weeks > 0) {
      humanizeDatePlural(weeks, DatePartType.WEEK, buf);
      return;
    }
    if (days > 0) {
      humanizeDatePlural(days, DatePartType.DAY, buf);
      return;
    }
    int hours = (int)delta.getStandardHours();
    if (hours > 0) {
      humanizeDatePlural(hours, DatePartType.HOUR, buf);
      return;
    }
    int mins = (int)delta.getStandardMinutes();
    if (mins > 0) {
      humanizeDatePlural(mins, DatePartType.MINUTE, buf);
      return;
    }
    int secs = (int)delta.getStandardSeconds();
    if (showSeconds) {
      humanizeDatePlural(secs, DatePartType.SECOND, buf);
      return;
    }
    buf.append("less than a minute ago");
  }

  enum DateTimeAggregate {
    FULL,
    H12,
    H240_M0,
    HHMMSSP,
    MMDDYY,
    MMDDYYYY,
    YYYYMMDD
  }

  public static boolean sameDay(long instant1, long instant2, String tzName) {
    DateTimeZone zone = null;
    try {
      zone = DateTimeZone.forID(tzName);
    } catch (IllegalArgumentException e) {
      zone = DateTimeZone.getDefault();
    }
    DateTime date1 = new DateTime(instant1, zone);
    DateTime date2 = new DateTime(instant2, zone);
    return (date1.year().get() == date2.year().get())
        && (date1.monthOfYear().get() == date2.monthOfYear().get())
        && (date1.dayOfMonth().get() == date2.dayOfMonth().get());
  }

  /**
   * Takes a strftime()-compatible format string and outputs the properly formatted date.
   */
  public static void formatDate(CLDR cldr, String fmt, long instant, String tzName, StringBuilder buf) {
    GregorianDate d = GregorianDate.fromUnixEpoch(instant, tzName, 0, 0);
    CalendarFields fields = null;
    Bundle bundle = null;
    if (cldr != null) {
      fields = cldr.Schema.Gregorian.standAlone;
      bundle = cldr.General.bundle();
    }
    _formatDate(bundle, fields, fmt, d, buf);
  }

  private static void _formatDate(Bundle bundle, CalendarFields fields, String fmt, GregorianDate d, StringBuilder buf) {
    int index = 0;
    int len = fmt.length();
    while (index < len) {
      char c1 = fmt.charAt(index);
      index++;
      if (c1 != '%' || index == len) {
        buf.append(c1);
        continue;
      }
      char c2 = fmt.charAt(index);
      switch (c2) {
        // %a     locale's abbreviated weekday name (e.g., Sun)
        case 'a': {
          if (bundle != null) {
            String name = fields.weekdays.get(bundle, "abbreviated", Long.toString(d.dayOfWeek()));
            buf.append(name);
          } else {
            buf.append(SHORT_DAYS[(int)d.dayOfWeek() - 1]);
          }
          break;
        }

        // %A     locale's full weekday name (e.g., Sunday)
        case 'A': {
          if (bundle != null) {
            String name = fields.weekdays.get(bundle, "wide", Long.toString(d.dayOfWeek()));
            buf.append(name);
          } else {
            buf.append(LONG_DAYS[(int)d.dayOfWeek() - 1]);
          }
          break;
        }

        // %b     locale's abbreviated month name (e.g., Jan)
        case 'b': {
          if (bundle != null) {
            String name = fields.months.get(bundle, "abbreviated", Long.toString(d.month()));
            buf.append(name);
          } else {
            buf.append(SHORT_MONTHS[(int)d.month() - 1]);
          }
          break;
        }

        // %B     locale's full month name (e.g., January)
        case 'B': {
          if (bundle != null) {
            String name = fields.months.get(bundle, "wide", Long.toString(d.month()));
            buf.append(name);
          } else {
            buf.append(LONG_MONTHS[(int)d.month() - 1]);
          }
          break;
        }

        // %c     locale's date and time (e.g., Thu Mar  3 23:05:25 2005)
        case 'c':
          _formatDate(bundle, fields, "%a, %b " + d.dayOfMonth() + ", %Y %i:%M:%S %p %Z", d, buf);
          break;

        // %C     century; like %Y, except omit last two digits (e.g., 20)
        case 'C': leftPad(d.year() / 100, '0', 2, buf); break;

        // %d     day of month (e.g., 01)
        case 'd': leftPad(d.dayOfMonth(), '0', 2, buf); break;

        // %D     date; same as %m/%d/%y
        case 'D': _formatDate(bundle, fields, "%m/%d/%y", d, buf); break;

        // %e     day of month, space padded; same as %_d
        case 'e': leftPad(d.dayOfMonth(), ' ', 2, buf); break;

        // %F     full date; same as %Y-%m-%d
        case 'F': _formatDate(bundle, fields, "%Y-%m-%d", d, buf); break;

        // %g     last two digits of year of ISO week number (see %G)
        case 'g': leftPad(d.yearOfWeekOfYearISO() % 100, '0', 2, buf); break;

        // %G     year of ISO week number (see %V); normally useful only with %V
        case 'G': leftPad(d.yearOfWeekOfYearISO(), '0', 4, buf); break;

        // %h     same as %b
        case 'h': {
          if (bundle != null) {
            String name = fields.months.get(bundle, "abbreviated", Long.toString(d.month()));
            buf.append(name);
          } else {
            buf.append(SHORT_MONTHS[(int)d.month() - 1]);
          }
          break;
        }

        // %H     hour (00..23)
        case 'H': leftPad(d.hourOfDay(), '0', 2, buf); break;

        // %i     hour (1..12), unpadded
        case 'i': {
          long h = d.hour();
          buf.append(h == 0 ? 12 : h);
          break;
        }

        // %I     hour (01..12), zero-padded
        case 'I': {
          long h = d.hour();
          leftPad(h == 0 ? 12 : h, '0', 2, buf);
          break;
        }

        // %j     day of year (001..366)
        case 'j': leftPad(d.dayOfYear(), '0', 3, buf); break;

        // %k     hour, space padded ( 0..23); same as %H
        case 'k': leftPad(d.hourOfDay(), ' ', 2, buf); break;

        // %l     hour, space padded ( 1..12); same as %I
        case 'l': {
          int h = (int)d.hour();
          leftPad(h == 0 ? 12 : h, ' ', 2, buf);
          break;
        }

        // %m     month (01..12)
        case 'm': leftPad(d.month(), '0', 2, buf); break;

        // %M     minute (00..59)
        case 'M': leftPad(d.minute(), '0', 2, buf); break;

        // %n     a newline
        case 'n': buf.append('\n'); break;

        // %N     nanoseconds (000000000..999999999)
        case 'N': leftPad(d.milliseconds() * 1000000, '0', 9, buf); break;

        // %p     locale's equivalent of either AM or PM; blank if not known
        case 'p': buf.append(d.isAM() ? "AM" : "PM"); break;

        // %P     like %p, but lower case
        case 'P': buf.append(d.isAM() ? "am" : "pm"); break;

        // %q     quarter of year (1..4)
        case 'q': {
          long q = ((d.month() - 1) / 3) + 1;
          buf.append(q);
          break;
        }

        // %r     locale's 12-hour clock time (e.g., 11:11:04 PM)
        case 'r': {
          int h = (int)d.hour();
          buf.append(h == 0 ? 12 : h);
          _formatDate(bundle, fields, ":%M:%S %p", d, buf);
          break;
        }

        // %R     24-hour hour and minute; same as %H:%M
        case 'R': _formatDate(bundle, fields, "%H:%M", d, buf); break;

        // %s     seconds since 1970-01-01 00:00:00 UTC
        case 's': buf.append(d.unixEpoch() / 1000); break;

        // %S     second (00..60)
        case 'S': leftPad(d.second(), '0', 2, buf); break;

        // %t     a tab
        case 't': buf.append('\t'); break;

        // %T     time; same as %H:%M:%S
        case 'T': _formatDate(bundle, fields, "%H:%M:%S", d, buf); break;

        // %u     day of week (1..7); 1 is Monday
        case 'u': {
          int wk = (int)d.dayOfWeek() - 1;
          buf.append(wk == 0 ? 7 : wk);
          break;
        }

        // %U     week number of year, with Sunday as first day of week (00..53)
        case 'U': leftPad(d.weekOfYear(), '0', 2, buf); break;

        // Undocumented
        case 'v': _formatDate(bundle, fields, "%e-%b-%Y", d, buf); break;

        // %V     ISO week number, with Monday as first day of week (01..53)
        case 'V': leftPad(d.weekOfYearISO(), '0', 2, buf); break;

        // %w     day of week (0..6); 0 is Sunday
        case 'w': buf.append(d.dayOfWeek() - 1); break;

        // %W     week number of year, with Monday as first day of week (00..53)
        case 'W': leftPad(d.weekOfYear(), '0', 2, buf); break;

        // %x     locale's date representation (e.g., 12/31/1999)
        case 'x': _formatDate(bundle, fields, "%m/%d/%Y", d, buf); break;

        // %X     locale's time representation (e.g., 23:13:48)
        case 'X': _formatDate(bundle, fields, "%I:%M:%S %p", d, buf); break;

        // %y     last two digits of year (00..99)
        case 'y':
          leftPad(d.year() % 100, '0', 2, buf); break;

        // %Y     year
        case 'Y': buf.append(d.year()); break;

        // %z     +hhmm numeric time zone (e.g., -0400)
        case 'z': {
          TZC t = new TZC(d.timeZoneOffset());
          buf.append(t.negative ? '-' : '+');
          leftPad(t.hours, '0', 2, buf);
          buf.append(':');
          leftPad(t.minutes, '0', 2, buf);
          break;
        }

        // %Z     alphabetic time zone abbreviation (e.g., EDT)
        case 'Z': buf.append(d.timeZoneAbbr()); break;

        default:
          // no match, emit literals.
          buf.append(c1).append(c2);
      }
      index++;
    }
  }

  private static class TZC {
    private final boolean negative;
    private final int hours;
    private final int minutes;

    TZC(int offset) {
      this.negative = offset < 0;
      if (negative) {
        offset *= -1;
      }
      offset /= 60000;
      this.hours = offset / 60;
      this.minutes = offset % 60;
    }
  }

  public static String getTimeZoneNameFromContext(Context ctx) {
    JsonNode tzNode = ctx.resolve(Constants.TIMEZONE_KEY);

    String tzName = "UTC";
    if (tzNode.isMissingNode()) {
      tzName = DEFAULT_TIMEZONEID;
    } else {
      tzName = tzNode.asText();
    }
    return tzName;
  }

}
