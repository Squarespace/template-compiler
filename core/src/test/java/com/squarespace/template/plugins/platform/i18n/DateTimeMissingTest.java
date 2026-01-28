/**
 * Copyright (c) 2017 SQUARESPACE, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.squarespace.template.plugins.platform.i18n;

import static org.testng.Assert.assertEquals;

import java.util.Locale;

import org.testng.annotations.Test;

import com.squarespace.template.CodeException;
import com.squarespace.template.Compiler;
import com.squarespace.template.Context;
import com.squarespace.template.compat.CompatLevel;
import com.squarespace.template.plugins.platform.PlatformUnitTestBase;

/**
 * A missing or null date input renders as missing at the fixed level.
 * The default level keeps the epoch 0 rendering. An explicit 0 is
 * present data and formats as the 1969 date at every level.
 */
public class DateTimeMissingTest extends PlatformUnitTestBase {

  private static final long NOW = 1502298000000L;

  private String run(String template, String json) throws CodeException {
    return run(template, json, null);
  }

  private String run(String template, String json, Long now) throws CodeException {
    Compiler compiler = compiler();
    Context ctx = compiler.newExecutor()
        .json(json)
        .template(template)
        .locale(Locale.forLanguageTag("en-US"))
        .now(now)
        .execute();
    assertEquals(ctx.getErrors().size(), 0, "execution produced errors");
    return ctx.buffer().toString();
  }

  private String runFixed(String template, String json, Long now) throws CodeException {
    Compiler compiler = compiler();
    Context ctx = compiler.newExecutor()
        .json(json)
        .template(template)
        .locale(Locale.forLanguageTag("en-US"))
        .now(now)
        .compat(CompatLevel.fixed())
        .execute();
    assertEquals(ctx.getErrors().size(), 0, "execution produced errors");
    return ctx.buffer().toString();
  }

  @Test
  public void testDatetimeMissing() throws CodeException {
    // Legacy, a missing or null value renders the epoch date.
    assertEquals(run("{d|datetime}", "{}"), "December 31, 1969");
    assertEquals(run("{d|datetime}", "{\"d\":null}"), "December 31, 1969");
    // Fixed, it renders missing.
    assertEquals(runFixed("{d|datetime}", "{}", null), "");
    assertEquals(runFixed("{d|datetime}", "{\"d\":null}", null), "");
  }

  @Test
  public void testDatetimePresentZero() throws CodeException {
    // An explicit 0 is present data and still renders the epoch date.
    assertEquals(run("{d|datetime}", "{\"d\":0}"), "December 31, 1969");
  }

  @Test
  public void testRelativeTimeMissing() throws CodeException {
    // Legacy, a missing or null value renders the age since epoch 0.
    assertEquals(run("{d|relative-time}", "{}", NOW), "48 years ago");
    assertEquals(run("{d|relative-time}", "{\"d\":null}", NOW), "48 years ago");
    // Fixed, it renders missing.
    assertEquals(runFixed("{d|relative-time}", "{}", NOW), "");
    assertEquals(runFixed("{d|relative-time}", "{\"d\":null}", NOW), "");
  }

  @Test
  public void testRelativeTimePresent() throws CodeException {
    // Matches the fixture: now=1502298000000, d=1502305200000.
    assertEquals(run("{d|relative-time}", "{\"d\":1502305200000}", NOW), "in 2 hours");
  }

  @Test
  public void testRelativeTimeSecondMissing() throws CodeException {
    // Legacy, a missing second operand renders the age since epoch 0.
    assertEquals(run("{s, e|relative-time}", "{\"s\":1502305200000}", NOW), "48 years ago");
    // Fixed, it renders missing.
    assertEquals(runFixed("{s, e|relative-time}", "{\"s\":1502305200000}", NOW), "");
  }

  @Test
  public void testIntervalMissing() throws CodeException {
    // Legacy, a missing operand reads as epoch 0.
    assertEquals(run("{s, e|datetime-interval}", "{}"), "7:00:00\u202FPM");
    assertEquals(run("{s, e|datetime-interval}", "{\"s\":1502298000000}"), "Aug 9, 2017\u2009\u2013\u2009Dec 31, 1969");
    assertEquals(run("{s, e|datetime-interval}", "{\"e\":1502305200000}"), "Dec 31, 1969\u2009\u2013\u2009Aug 9, 2017");
    // Fixed, the interval renders missing.
    assertEquals(runFixed("{s, e|datetime-interval}", "{}", null), "");
    assertEquals(runFixed("{s, e|datetime-interval}", "{\"s\":1502298000000}", null), "");
    assertEquals(runFixed("{s, e|datetime-interval}", "{\"e\":1502305200000}", null), "");
  }

  @Test
  public void testIntervalPresent() throws CodeException {
    // Same fixture as f-datetime-interval-1.html minus the repeated sections.
    String json = "{\"website\":{\"timeZone\":\"America/Los_Angeles\"},\"s\":1502298000000,\"e\":1502305200000}";
    String expected = "10:00\u202FAM\u2009\u2013\u200912:00\u202FPM";
    assertEquals(run("{s, e|datetime-interval}", json), expected);
  }
}
