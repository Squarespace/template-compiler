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

package com.squarespace.template.plugins;

import static com.squarespace.template.KnownDates.AUG_24_2015_172345_UTC;
import static com.squarespace.template.KnownDates.JAN_01_1970_071510_UTC;
import static com.squarespace.template.KnownDates.MAY_13_2013_010000_UTC;
import static com.squarespace.template.KnownDates.NOV_15_2013_123030_UTC;
import static com.squarespace.template.plugins.PluginDateUtils.sameDay;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Locale;

import org.testng.annotations.Test;

import com.squarespace.template.CodeException;


@Test(groups = { "unit" })
public class PluginDateUtilsTest {

  private static final String TZ_UTC = "UTC";

  private static final String TZ_NY = "America/New_York";

  private static final String TZ_LA = "America/Los_Angeles";

  private static final Locale MEXICO = new Locale("es", "MX");

  private static final long ONE_MINUTE_MS = 60 * 1000;

  private static final long ONE_HOUR_MS = 60 * ONE_MINUTE_MS;

  private static final long ONE_DAY_MS = 24 * ONE_HOUR_MS;

  private static final long ONE_MONTH_MS = (long)(30.41 * ONE_DAY_MS);

  private static final long ONE_YEAR_MS = 365 * ONE_DAY_MS;

  @Test
  public void testHumanizeDate() {
    long baseMs = MAY_13_2013_010000_UTC + 30000;
    assertEquals(humanizeDate(MAY_13_2013_010000_UTC, baseMs, true), "about 30 seconds ago");
    assertEquals(humanizeDate(MAY_13_2013_010000_UTC, baseMs, false), "less than a minute ago");

    baseMs += ONE_MINUTE_MS;
    assertEquals(humanizeDate(MAY_13_2013_010000_UTC, baseMs, true), "about a minute ago");

    baseMs += ONE_MINUTE_MS;
    assertEquals(humanizeDate(MAY_13_2013_010000_UTC, baseMs, true), "about 2 minutes ago");

    baseMs += (5 * ONE_MINUTE_MS);
    assertEquals(humanizeDate(MAY_13_2013_010000_UTC, baseMs, true), "about 7 minutes ago");

    baseMs += (60 * ONE_MINUTE_MS);
    assertEquals(humanizeDate(MAY_13_2013_010000_UTC, baseMs, true), "about an hour ago");

    baseMs += (60 * ONE_MINUTE_MS);
    assertEquals(humanizeDate(MAY_13_2013_010000_UTC, baseMs, true), "about 2 hours ago");

    baseMs = MAY_13_2013_010000_UTC + (long)(83600000 * 2.5);
    assertEquals(humanizeDate(MAY_13_2013_010000_UTC, baseMs, true), "about 2 days ago");

    baseMs += (ONE_DAY_MS * 5);
    assertEquals(humanizeDate(MAY_13_2013_010000_UTC, baseMs, true), "about a week ago");

    baseMs += (ONE_DAY_MS * 10);
    assertEquals(humanizeDate(MAY_13_2013_010000_UTC, baseMs, true), "about 2 weeks ago");

    assertEquals(humanizeDate(MAY_13_2013_010000_UTC, NOV_15_2013_123030_UTC, true), "about 6 months ago");
    assertEquals(humanizeDate(MAY_13_2013_010000_UTC, AUG_24_2015_172345_UTC, true), "about 2 years ago");
  }

  @Test
  public void testSameDay() {
    long baseMs = MAY_13_2013_010000_UTC;

    // 5 minutes after
    assertTrue(sameDay(baseMs, baseMs + (ONE_MINUTE_MS * 5), TZ_UTC));

    // 2 days after
    assertFalse(sameDay(baseMs, baseMs + (ONE_DAY_MS * 2), TZ_UTC));

    // 2 months after
    assertFalse(sameDay(baseMs, baseMs + (ONE_MONTH_MS * 2), TZ_UTC));

    // 2 years after
    assertFalse(sameDay(baseMs, baseMs + (ONE_YEAR_MS * 2), TZ_UTC));
  }

  @Test
  public void testBasics() throws CodeException {
    String format = "%Y-%m-%d %H:%M:%S %Z";
    assertEquals(formatDate(format, MAY_13_2013_010000_UTC, TZ_UTC), "2013-05-13 01:00:00 UTC");
    assertEquals(formatDate(format, MAY_13_2013_010000_UTC, TZ_NY), "2013-05-12 21:00:00 EDT");
    assertEquals(formatDate(format, MAY_13_2013_010000_UTC, TZ_LA), "2013-05-12 18:00:00 PDT");
    assertEquals(formatDate(format, NOV_15_2013_123030_UTC, TZ_NY), "2013-11-15 07:30:30 EST");
    assertEquals(formatDate(format, NOV_15_2013_123030_UTC, TZ_LA), "2013-11-15 04:30:30 PST");
  }

  @Test
  public void testSingleFields() throws CodeException {
    // Day of week, long
    assertEquals(formatDate("%A", JAN_01_1970_071510_UTC, TZ_UTC), "Thursday");
    assertEquals(formatDate("%A", MAY_13_2013_010000_UTC, TZ_UTC), "Monday");
    assertEquals(formatDate("%A", NOV_15_2013_123030_UTC, TZ_UTC), "Friday");

    // Day of week, long, locale-adjusted
    assertEquals(formatDate("%A", JAN_01_1970_071510_UTC, TZ_UTC, MEXICO), "jueves");
    assertEquals(formatDate("%A", MAY_13_2013_010000_UTC, TZ_UTC, MEXICO), "lunes");
    assertEquals(formatDate("%A", NOV_15_2013_123030_UTC, TZ_UTC, MEXICO), "viernes");

    // Day of week, short
    assertEquals(formatDate("%a", JAN_01_1970_071510_UTC, TZ_UTC), "Thu");
    assertEquals(formatDate("%a", MAY_13_2013_010000_UTC, TZ_UTC), "Mon");
    assertEquals(formatDate("%a", NOV_15_2013_123030_UTC, TZ_UTC), "Fri");

    // Month of year, long
    assertEquals(formatDate("%B", JAN_01_1970_071510_UTC, TZ_UTC), "January");
    assertEquals(formatDate("%B", MAY_13_2013_010000_UTC, TZ_UTC), "May");
    assertEquals(formatDate("%B", NOV_15_2013_123030_UTC, TZ_UTC), "November");

    // Month of year, long, locale-adjusted
    assertEquals(formatDate("%B", JAN_01_1970_071510_UTC, TZ_UTC, MEXICO), "enero");
    assertEquals(formatDate("%B", MAY_13_2013_010000_UTC, TZ_UTC, MEXICO), "mayo");
    assertEquals(formatDate("%B", NOV_15_2013_123030_UTC, TZ_UTC, MEXICO), "noviembre");

    // Month of year, short
    assertEquals(formatDate("%b", JAN_01_1970_071510_UTC, TZ_UTC), "Jan");
    assertEquals(formatDate("%b", MAY_13_2013_010000_UTC, TZ_UTC), "May");
    assertEquals(formatDate("%b", NOV_15_2013_123030_UTC, TZ_UTC), "Nov");

    // Century
    assertEquals(formatDate("%C", JAN_01_1970_071510_UTC, TZ_UTC), "19");
    assertEquals(formatDate("%C", MAY_13_2013_010000_UTC, TZ_UTC), "20");

    // Day of month, zero-padded
    assertEquals(formatDate("%d", JAN_01_1970_071510_UTC, TZ_UTC), "01");
    assertEquals(formatDate("%d", MAY_13_2013_010000_UTC, TZ_UTC), "13");

    // Day of month, space-padded
    assertEquals(formatDate("%e", JAN_01_1970_071510_UTC, TZ_UTC), " 1");
    assertEquals(formatDate("%d", MAY_13_2013_010000_UTC, TZ_UTC), "13");

    // Year
    assertEquals(formatDate("%G", JAN_01_1970_071510_UTC, TZ_UTC), "1970");
    assertEquals(formatDate("%G", MAY_13_2013_010000_UTC, TZ_UTC), "2013");

    // Year of century
    assertEquals(formatDate("%g", JAN_01_1970_071510_UTC, TZ_UTC), "70");
    assertEquals(formatDate("%g", MAY_13_2013_010000_UTC, TZ_UTC), "13");

    // Hour of day, 24-hour
    assertEquals(formatDate("%H", JAN_01_1970_071510_UTC, TZ_NY), "02");
    assertEquals(formatDate("%H", MAY_13_2013_010000_UTC, TZ_NY), "21");
    assertEquals(formatDate("%H", NOV_15_2013_123030_UTC, TZ_NY), "07");

    // Month of year, short, locale-adjusted
    assertEquals(formatDate("%h", MAY_13_2013_010000_UTC, TZ_UTC, Locale.GERMANY), "Mai");
    assertEquals(formatDate("%h", MAY_13_2013_010000_UTC, TZ_UTC, MEXICO), "may");

    // Hour of day, 12-hour
    assertEquals(formatDate("%I", JAN_01_1970_071510_UTC, TZ_NY), "02");
    assertEquals(formatDate("%I", MAY_13_2013_010000_UTC, TZ_NY), "09");
    assertEquals(formatDate("%I", NOV_15_2013_123030_UTC, TZ_NY), "07");

    // Day of year, zero-padded
    assertEquals(formatDate("%j", JAN_01_1970_071510_UTC, TZ_NY), "001");
    assertEquals(formatDate("%j", MAY_13_2013_010000_UTC, TZ_NY), "132");
    assertEquals(formatDate("%j", NOV_15_2013_123030_UTC, TZ_NY), "319");

    // Hour of day, 24-hour, space-padded
    assertEquals(formatDate("%k", JAN_01_1970_071510_UTC, TZ_NY), " 2");
    assertEquals(formatDate("%k", MAY_13_2013_010000_UTC, TZ_NY), "21");
    assertEquals(formatDate("%k", NOV_15_2013_123030_UTC, TZ_NY), " 7");

    // Hour of day, 12-hour, space-padded
    assertEquals(formatDate("%l", JAN_01_1970_071510_UTC, TZ_NY), " 2");
    assertEquals(formatDate("%l", MAY_13_2013_010000_UTC, TZ_NY), " 9");
    assertEquals(formatDate("%l", NOV_15_2013_123030_UTC, TZ_NY), " 7");

    // Minute of hour, zero-padded
    assertEquals(formatDate("%M", JAN_01_1970_071510_UTC, TZ_NY), "15");
    assertEquals(formatDate("%M", MAY_13_2013_010000_UTC, TZ_NY), "00");
    assertEquals(formatDate("%M", NOV_15_2013_123030_UTC, TZ_NY), "30");

    // Month of year, zero-padded
    assertEquals(formatDate("%m", JAN_01_1970_071510_UTC, TZ_NY), "01");
    assertEquals(formatDate("%m", MAY_13_2013_010000_UTC, TZ_NY), "05");
    assertEquals(formatDate("%m", NOV_15_2013_123030_UTC, TZ_NY), "11");

    // Newline character
    assertEquals(formatDate("%n", JAN_01_1970_071510_UTC, TZ_UTC), "\n");

    // am / pm
    assertEquals(formatDate("%P", JAN_01_1970_071510_UTC, TZ_UTC), "am");
    assertEquals(formatDate("%P", MAY_13_2013_010000_UTC, TZ_UTC), "am");
    assertEquals(formatDate("%P", NOV_15_2013_123030_UTC, TZ_UTC), "pm");
    assertEquals(formatDate("%P", JAN_01_1970_071510_UTC, TZ_NY), "am");
    assertEquals(formatDate("%P", MAY_13_2013_010000_UTC, TZ_NY), "pm");
    assertEquals(formatDate("%P", NOV_15_2013_123030_UTC, TZ_NY), "am");

    // AM / PM
    assertEquals(formatDate("%p", JAN_01_1970_071510_UTC, TZ_UTC), "AM");
    assertEquals(formatDate("%p", MAY_13_2013_010000_UTC, TZ_UTC), "AM");
    assertEquals(formatDate("%p", NOV_15_2013_123030_UTC, TZ_UTC), "PM");
    assertEquals(formatDate("%p", JAN_01_1970_071510_UTC, TZ_NY), "AM");
    assertEquals(formatDate("%p", MAY_13_2013_010000_UTC, TZ_NY), "PM");
    assertEquals(formatDate("%p", NOV_15_2013_123030_UTC, TZ_NY), "AM");

    // Second of minute
    assertEquals(formatDate("%S", JAN_01_1970_071510_UTC, TZ_UTC), "10");
    assertEquals(formatDate("%S", MAY_13_2013_010000_UTC, TZ_UTC), "00");
    assertEquals(formatDate("%S", NOV_15_2013_123030_UTC, TZ_UTC), "30");

    // Seconds
    assertEquals(formatDate("%s", JAN_01_1970_071510_UTC, TZ_UTC), "26110");
    assertEquals(formatDate("%s", MAY_13_2013_010000_UTC, TZ_UTC), "1368406800");
    assertEquals(formatDate("%s", NOV_15_2013_123030_UTC, TZ_UTC), "1384518630");

    // Tab character
    assertEquals(formatDate("%t", JAN_01_1970_071510_UTC, TZ_UTC), "\t");

    // Year
    assertEquals(formatDate("%Y", JAN_01_1970_071510_UTC, TZ_UTC), "1970");
    assertEquals(formatDate("%Y", MAY_13_2013_010000_UTC, TZ_UTC), "2013");
    assertEquals(formatDate("%Y", NOV_15_2013_123030_UTC, TZ_UTC), "2013");

    // Year of century
    assertEquals(formatDate("%y", JAN_01_1970_071510_UTC, TZ_UTC), "70");
    assertEquals(formatDate("%y", MAY_13_2013_010000_UTC, TZ_UTC), "13");
    assertEquals(formatDate("%y", NOV_15_2013_123030_UTC, TZ_UTC), "13");

    // Timezone, short name
    assertEquals(formatDate("%Z", JAN_01_1970_071510_UTC, TZ_UTC), "UTC");
    assertEquals(formatDate("%Z", MAY_13_2013_010000_UTC, TZ_NY), "EDT");
    assertEquals(formatDate("%Z", NOV_15_2013_123030_UTC, TZ_LA), "PST");

    // Timezone, offset
    assertEquals(formatDate("%z", JAN_01_1970_071510_UTC, TZ_UTC), "0000");
    assertEquals(formatDate("%z", MAY_13_2013_010000_UTC, TZ_NY), "-0400");
    assertEquals(formatDate("%z", NOV_15_2013_123030_UTC, TZ_LA), "-0800");

    // Literals
    assertEquals(formatDate("%%", JAN_01_1970_071510_UTC, TZ_UTC), "%%");
    assertEquals(formatDate("%,", MAY_13_2013_010000_UTC, TZ_NY), "%,");
  }

  @Test
  public void testAggregateFields() throws CodeException {
    String r1 = formatDate("%c", NOV_15_2013_123030_UTC, TZ_NY);
    String r2 = formatDate("%a, %b %d, %Y %I:%M:%S %p %Z", NOV_15_2013_123030_UTC, TZ_NY);
    assertEquals(r1, r2);

    r1 = formatDate("%c", MAY_13_2013_010000_UTC, TZ_NY);
    r2 = formatDate("%a, %b %d, %Y %I:%M:%S %p %Z", MAY_13_2013_010000_UTC, TZ_NY);
    assertEquals(r1, r2);

    long base = MAY_13_2013_010000_UTC - (86400000 * 5);
    r1 = formatDate("%c", base, TZ_NY);
    assertEquals(r1, "Tue, May 7, 2013 09:00:00 PM EDT");

    r1 = formatDate("%F", NOV_15_2013_123030_UTC, TZ_LA);
    r2 = formatDate("%Y-%m-%d", NOV_15_2013_123030_UTC, TZ_LA);
    assertEquals(r1, r2);

    r1 = formatDate("%D", NOV_15_2013_123030_UTC, TZ_NY);
    r2 = formatDate("%m/%d/%y", NOV_15_2013_123030_UTC, TZ_NY);
    assertEquals(r1, r2);

    r1 = formatDate("%x", NOV_15_2013_123030_UTC, TZ_NY);
    r2 = formatDate("%m/%d/%Y", NOV_15_2013_123030_UTC, TZ_NY);
    assertEquals(r1, r2);

    r1 = formatDate("%R", NOV_15_2013_123030_UTC, TZ_NY);
    r2 = formatDate("%H:%M", NOV_15_2013_123030_UTC, TZ_NY);
    assertEquals(r1, r2);

    r1 = formatDate("%T", MAY_13_2013_010000_UTC, TZ_NY);
    r2 = formatDate("%H:%M:%S", MAY_13_2013_010000_UTC, TZ_NY);
    assertEquals(r1, r2);

    r1 = formatDate("%X", NOV_15_2013_123030_UTC, TZ_NY);
    r2 = formatDate("%I:%M:%S %p", NOV_15_2013_123030_UTC, TZ_NY);
    assertEquals(r1, r2);

    r1 = formatDate("%X", MAY_13_2013_010000_UTC, TZ_NY);
    r2 = formatDate("%I:%M:%S %p", MAY_13_2013_010000_UTC, TZ_NY);
    assertEquals(r1, r2);

    r1 = formatDate("%v", MAY_13_2013_010000_UTC, TZ_NY);
    r2 = formatDate("%e-%b-%Y", MAY_13_2013_010000_UTC, TZ_NY);
    assertEquals(r1, r2);
  }

  @Test
  public void testAmPm() {
    // am/pm and 12-hour hour
    String format = "%p %P %l";
    assertEquals(formatDate(format, MAY_13_2013_010000_UTC, TZ_NY), "PM pm  9");
    assertEquals(formatDate(format, NOV_15_2013_123030_UTC, TZ_NY), "AM am  7");
  }

  @Test
  public void testTimezoneOffsets() {
    // Timezone short names and offsets
    String format = "%Z %z";
    assertEquals(formatDate(format, MAY_13_2013_010000_UTC, TZ_NY), "EDT -0400");
    assertEquals(formatDate(format, MAY_13_2013_010000_UTC, TZ_LA), "PDT -0700");
    assertEquals(formatDate(format, NOV_15_2013_123030_UTC, TZ_NY), "EST -0500");
    assertEquals(formatDate(format, NOV_15_2013_123030_UTC, TZ_LA), "PST -0800");
  }

  @Test
  public void testWeekOfYear() {
    // TODO: Week of Year (Joda doesn't support the full range of week-of-year calculations)
//    format = "%U %V %W";
//    assertEquals(formatDate(format, MAY_13_2013_010000_UTC, TZ_NY), "20");
  }

  private String humanizeDate(long instantMs, long baseMs, boolean showSeconds) {
    StringBuilder buf = new StringBuilder();
    PluginDateUtils.humanizeDate(instantMs, baseMs, TZ_UTC, showSeconds, buf);
    return buf.toString();
  }

  private String formatDate(String format, long timestamp, String tzId) {
    return formatDate(format, timestamp, tzId, Locale.US);
  }

  private String formatDate(String format, long timestamp, String tzId, Locale locale) {
    StringBuilder buf = new StringBuilder();
    PluginDateUtils.formatDate(locale, format, timestamp, tzId, buf);
    return buf.toString();
  }

}
