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


public class ContentPredicatesTest extends TemplateUnitTestBase {

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
