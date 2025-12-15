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


import static com.squarespace.template.ExecuteErrorType.UNEXPECTED_ERROR;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import com.squarespace.template.CodeException;
import com.squarespace.template.Constants;
import com.squarespace.template.Context;
import com.squarespace.template.Formatter;
import com.squarespace.template.JsonUtils;
import com.squarespace.template.KnownDates;
import com.squarespace.template.TestSuiteRunner;
import com.squarespace.template.Variables;
import com.squarespace.template.compat.CompatLevel;
import com.squarespace.template.plugins.platform.SocialFormatters.ActivateTwitterLinksFormatter;
import com.squarespace.template.plugins.platform.SocialFormatters.GoogleCalendarUrlFormatter;
import com.squarespace.template.plugins.platform.SocialFormatters.TwitterFollowButtonFormatter;


/**
 * Extracted from Commons library at commit ab4ba7a6f2b872a31cb6449ae9e96f5f5b30f471
 */
public class SocialFormattersTest extends PlatformUnitTestBase {

  private static final Formatter ACTIVATE_TWITTER_LINKS = new ActivateTwitterLinksFormatter();

  private static final Formatter GOOGLE_CALENDAR_URL = new GoogleCalendarUrlFormatter();

  private static final Formatter TWITTER_FOLLOW_BUTTON = new TwitterFollowButtonFormatter();

  private static final long ONE_DAY = 86400 * 1000;

  private final TestSuiteRunner runner = new TestSuiteRunner(compiler(), SocialFormattersTest.class);

  @Test
  public void testActivateTwitterLinks() throws CodeException {
    String json = "\"#foo and #bar\"";
    String result = format(ACTIVATE_TWITTER_LINKS, json);
    assertTrue(result.contains("twitter.com/hashtag/foo"));
    assertTrue(result.contains("twitter.com/hashtag/bar"));

    runner.run(
       "f-activate-twitter-links-1.html"
    );
   }

  @Test
  public void testCommentLink() {
    runner.run(
        "f-comment-link-1.html",
        "f-comment-link-2.html",
        "f-comment-link-3.html"
        );
  }

  @Test
  public void testComments() {
    runner.run(
        "f-comments-1.html",
        "f-comments-2.html",
        "f-comments-3.html"
        );
  }

  @Test
  public void testCommentCount() {
    runner.run(
        "f-comment-count-1.html",
        "f-comment-count-2.html",
        "f-comment-count-3.html"
        );
  }

  @Test
  public void testLikeButton() {
    runner.run(
        "f-like-button-1.html",
        "f-like-button-2.html"
        );
  }

  @Test
  public void testGoogleCalendarUrl() throws CodeException {
    long end = KnownDates.NOV_15_2013_123030_UTC;
    long start = end - ONE_DAY;
    String json = "{\"startDate\": " + start + ", \"endDate\": " + end + ", \"title\": \"foo\"}";
    String result = format(GOOGLE_CALENDAR_URL, json);
    assertTrue(result.contains("dates=20131114T123030Z/20131115T123030Z"));

    runner.run(
        "f-google-calendar-url-1.html"
        );
  }

  @Test
  public void testSocialButton() {
    runner.run(
        "f-social-button-1.html",
        "f-social-button-2.html",
        "f-social-button-3.html",
        "f-social-button-4.html",
        "f-social-button-5.html"
        );
  }

  @Test
  public void testSocialButtonInline() {
    runner.run(
        "f-social-button-inline-1.html",
        "f-social-button-inline-2.html"
        );
  }

  @Test
  public void testTwitterFollowButton() throws CodeException {
    runner.run(
        "f-twitter-follow-button-1.html",
        "f-twitter-follow-button-2.html",
        "f-twitter-follow-button-3.html",
        "f-twitter-follow-button-4.html",
        "f-twitter-follow-button-5.html",
        "f-twitter-follow-button-6.html"
        );

    String empty = "{\"userName\": \"\", \"profileUrl\": \"\"}";

    // Legacy, an empty username and profileUrl throws at the default level.
    try {
      format(TWITTER_FOLLOW_BUTTON, empty);
      fail("expected ArrayIndexOutOfBoundsException");
    } catch (ArrayIndexOutOfBoundsException e) {
      // Expected
    }

    // Legacy, safe mode at the default level collects the throw.
    Context legacy = compiler().newExecutor()
        .template("x {@|twitter-follow-button} x")
        .json(empty)
        .safeExecution(true)
        .execute();
    assertContext(legacy, "x  x");
    assertEquals(legacy.getErrors().size(), 1);
    assertEquals(legacy.getErrors().get(0).getType(), UNEXPECTED_ERROR);
    assertTrue(legacy.getErrors().get(0).getMessage().contains("ArrayIndexOutOfBoundsException"));

    // Fixed, an empty username and profileUrl renders nothing without error.
    Context fixed = compiler().newExecutor()
        .template("x {@|twitter-follow-button} x")
        .json(empty)
        .safeExecution(true)
        .compat(CompatLevel.fixed())
        .execute();
    assertContext(fixed, "x  x");
    assertEquals(fixed.getErrors().size(), 0);

    // Fixed, the username is escaped before it goes into the attribute.
    Context ctx = new Context(JsonUtils.decode("{\"userName\": \"a\\\"b\"}"));
    ctx.setCompat(CompatLevel.fixed());
    Variables vars = new Variables("var", ctx.node());
    TWITTER_FOLLOW_BUTTON.apply(ctx, Constants.EMPTY_ARGUMENTS, vars);
    assertTrue(vars.first().node().asText().contains("data-username=\"a&quot;b\""));
  }
}
