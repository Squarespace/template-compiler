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


import org.joda.time.DateTimeZone;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squarespace.template.Arguments;
import com.squarespace.template.CodeException;
import com.squarespace.template.Constants;
import com.squarespace.template.Context;
import com.squarespace.template.JsonUtils;
import com.squarespace.template.KnownDates;
import com.squarespace.template.Predicate;
import com.squarespace.template.TestSuiteRunner;


/**
 * Extracted from Commons library at commit ab4ba7a6f2b872a31cb6449ae9e96f5f5b30f471
 */
public class ContentPredicatesTest extends PlatformUnitTestBase {

  private final TestSuiteRunner runner = new TestSuiteRunner(compiler(), ContentPredicatesTest.class);

  @Test
  public void testCalendarView() {
    runner.run("p-calendar-view.html");
  }

  @Test
  public void testChildImages() {
    runner.run(
        "p-child-images-1.html",
        "p-child-images-2.html",
        "p-child-images-3.html",
        "p-child-images-4.html"
        );
  }

  @Test
  public void testClickable() {
    runner.run("p-clickable.html");
  }

  @Test
  public void testCollection() {
    runner.run("p-collection.html");
  }

  @Test
  public void testCollectionPage() {
    runner.run("p-collection-page.html");
  }

  @Test
  public void testCollectionTemplatePage() {
    runner.run("p-collection-template-page.html");
  }

  @Test
  public void testCollectionTypeNameEquals() {
    runner.run("p-collection-type-name-equals.html");
  }

  @Test
  public void testExcerpt() {
    runner.run("p-excerpt.html");
  }

  @Test
  public void testExternalLink() {
    runner.run("p-external-link.html");
  }

  @Test
  public void testFolder() {
    runner.run("p-folder.html");
  }

  @Test
  public void testGalleryMeta() {
    runner.run("p-gallery-meta.html");
  }

  @Test
  public void testGallerySelect() {
    runner.run("p-gallery-select.html");
  }

  @Test
  public void testGalleryBoolean() {
    runner.run("p-gallery-boolean.html");
  }

  @Test
  public void testHasMultiple() {
    runner.run("p-has-multiple.html");
  }

  @Test
  public void testIndex() {
    runner.run("p-index.html");
  }

  @Test
  public void testLocation() {
    runner.run("p-location.html");
  }

  @Test
  public void testMainImage() {
    runner.run("p-main-image.html");
  }

  @Test
  public void testRecordType() {
    runner.run("p-record-type.html");
  }
  
  @Test
  public void testBackgroundSource() {
    runner.run("p-background-source.html");
  }

  @Test
  public void testPassthrough() {
    runner.run("p-passthrough.html");
  }

  @Test
  public void testRedirect() {
    runner.run("p-redirect.html");
  }

  @Test
  public void testSameDay() throws CodeException {
    long date1 = KnownDates.NOV_15_2013_123030_UTC - (3600L * 1000);
    String json = getDateTestJson(date1, "America/New_York");
    ObjectNode node = (ObjectNode) JsonUtils.decode(json);
    ObjectNode dates = JsonUtils.createObjectNode();
    dates.put("startDate", date1);
    dates.put("endDate", KnownDates.NOV_15_2013_123030_UTC);
    node.put("dates", dates);

    String[] key = new String[] { "dates" };
    Context ctx = new Context(node);
    ctx.pushSection(key);
    assertTrue(ContentPredicates.SAME_DAY, ctx);

    // 2 days prior, shouldn't match
    dates.put("endDate", KnownDates.NOV_15_2013_123030_UTC - (86400L * 1000 * 2));
    ctx = new Context(node);
    ctx.pushSection(key);
    assertFalse(ContentPredicates.SAME_DAY, ctx);
  }

  @Test
  public void testServiceNameEmail() {
    runner.run("p-service-name-email.html");
  }

  @Test
  public void testShowPastEvents() {
    runner.run("p-show-past-events.html");
  }

  private void assertTrue(Predicate predicate, Context ctx) throws CodeException {
    assertTrue(predicate, ctx, Constants.EMPTY_ARGUMENTS);
  }

  private void assertTrue(Predicate predicate, Context ctx, Arguments args) throws CodeException {
    Assert.assertTrue(predicate.apply(ctx, args));
  }

  private void assertFalse(Predicate predicate, Context ctx) throws CodeException {
    assertFalse(predicate, ctx, Constants.EMPTY_ARGUMENTS);
  }

  private void assertFalse(Predicate predicate, Context ctx, Arguments args) throws CodeException {
    Assert.assertFalse(predicate.apply(ctx, args));
  }

  protected static String getDateTestJson(long timestamp, String tzId) {
    DateTimeZone timezone = DateTimeZone.forID(tzId);
    ObjectNode node = JsonUtils.createObjectNode();
    node.put("time", timestamp);
    ObjectNode website = JsonUtils.createObjectNode();
    website.put("timeZoneOffset", timezone.getOffset(timestamp));
    website.put("timeZone", timezone.getID());
    node.put("website", website);
    return node.toString();
  }

}
