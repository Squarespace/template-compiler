package com.squarespace.template;

import static com.squarespace.template.SocialFormatters.ACTIVATE_TWITTER_LINKS;
import static com.squarespace.template.SocialFormatters.GOOGLE_CALENDAR_URL;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;


public class SocialFormattersTest extends UnitTestBase {
  
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
