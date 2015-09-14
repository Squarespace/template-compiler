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

package com.squarespace.template.plugins.platform;


import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.squarespace.template.CodeException;
import com.squarespace.template.Formatter;
import com.squarespace.template.KnownDates;
import com.squarespace.template.plugins.platform.SocialFormatters.ActivateTwitterLinksFormatter;
import com.squarespace.template.plugins.platform.SocialFormatters.GoogleCalendarUrlFormatter;


public class SocialFormattersTest extends TemplateUnitTestBase {

  private static final Formatter ACTIVATE_TWITTER_LINKS = new ActivateTwitterLinksFormatter();

  private static final Formatter GOOGLE_CALENDAR_URL = new GoogleCalendarUrlFormatter();

  private static final long ONE_DAY = 86400 * 1000;

  @Test
  public void testActivateTwitterLinks() throws CodeException {
    String json = "\"#foo and #bar\"";
    String result = format(ACTIVATE_TWITTER_LINKS, json);
    assertTrue(result.contains("twitter.com/search/foo"));
    assertTrue(result.contains("twitter.com/search/bar"));
  }

  @Test
  public void testGoogleCalendarUrl() throws CodeException {
    long end = KnownDates.NOV_15_2013_123030_UTC;
    long start = end - ONE_DAY;
    String json = "{\"startDate\": " + start + ", \"endDate\": " + end + ", \"title\": \"foo\"}";
    String result = format(GOOGLE_CALENDAR_URL, json);
    assertTrue(result.contains("dates=20131114T123030Z/20131115T123030Z"));
  }

}
