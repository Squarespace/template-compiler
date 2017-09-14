/**
 * Copyright (c) 2017 SQUARESPACE, Inc.
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

import static com.squarespace.cldr.CLDR.Locale.de;
import static com.squarespace.cldr.CLDR.Locale.en;
import static com.squarespace.cldr.CLDR.Locale.en_CA;
import static com.squarespace.cldr.CLDR.Locale.en_US;
import static com.squarespace.cldr.CLDR.Locale.fr;
import static com.squarespace.cldr.CLDR.Locale.ja;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.squarespace.cldr.CLDR;
import com.squarespace.template.Arguments;
import com.squarespace.template.CodeException;
import com.squarespace.template.CodeMaker;
import com.squarespace.template.Context;
import com.squarespace.template.Formatter;
import com.squarespace.template.JsonUtils;
import com.squarespace.template.TestSuiteRunner;
import com.squarespace.template.Variables;
import com.squarespace.template.plugins.platform.PlatformUnitTestBase;


public class UnitFormattersTest extends PlatformUnitTestBase {

  private static final Formatter UNIT = new UnitFormatter();
  private final CodeMaker mk = maker();
  private final TestSuiteRunner runner = new TestSuiteRunner(compiler(), UnitFormattersTest.class);

  @Test
  public void testRunner() {
    runner.run(
        "f-unit-angle-en-US.html",
        "f-unit-area-en-US.html",
        "f-unit-digital-en-US.html",
        "f-unit-duration-en-US.html",
        "f-unit-frequency-en-US.html",
        "f-unit-length-en-US.html",
        "f-unit-temperature-en-US.html"
    );
  }

  @Test
  public void testInvalid() {
    String json = "\"xyz\"";
    run(en_US, UNIT, json, "  in:second out:hour,minute,second format:long", "");
  }

  @Test
  public void testDefaulting() {
    String json = "1234567";

    // No arguments
    run(en_US, UNIT, json, "", "1234567");

    // No unit-related arguments
    run(en_US, UNIT, json, " group", "1,234,567");

    // No unit, nothing to do.
    run(en_US, UNIT, json, " group:true", "1,234,567");

    // If only compact form is specified, we don't infer the units since it is
    // too fuzzy and locale-dependent. For example, compact form "temperature"
    // would be CELSIUS in a metric locale or FAHRENHEIT in non-metric.
    run(en_US, UNIT, json, " compact:byte", "1234567");
    run(en_US, UNIT, json, " compact:temperature", "1234567");

    // Output compact form by default
    run(en_US, UNIT, json, " in:byte", "1.2MB");
    run(en_US, UNIT, json, " in:inch", "19.5mi");

    // No input unit specified, infer bytes from output unit.
    run(en_US, UNIT, json, " out:kilobyte group:true", "1,205.6kB");
  }

  @Test
  public void testDigital() {
    run(en, UNIT, "123", " in:byte", "123byte");
    run(en, UNIT, "123456", " in:byte compact:byte", "120.6kB");
    run(en, UNIT, "1533", " in:megabyte compact:byte", "1.5GB");
    run(en, UNIT, "1024", " in:byte compact:byte", "1kB");

    String json = "1351531335";
    String args = " format:long in:byte out:megabyte group:true";
    run(en, UNIT, json, args, "1,288.9 megabytes");
    run(fr, UNIT, json, args, "1 288,9 mégaoctets");
    run(de, UNIT, json, args, "1.288,9 Megabytes");
    run(ja, UNIT, json, args, "1,288.9 メガバイト");
  }

  @Test
  public void testDurationSequence() {
    String json = "2.35";
    run(en, UNIT, json, " in:days sequence:minute,day,hour", "2d 8h 24m");
    run(fr, UNIT, json, " in:days sequence:minute,day,hour format:long", "2 jours 8 heures 24 minutes");
  }

  @Test
  public void testLengthSequence() {
    String json = "7890.123";

    run(en_CA, UNIT, json, " in:foot sequence:mile,foot,inch", "1mi 2610′ 1.5″");
    run(en_US, UNIT, json, " in:foot sequence:mile,foot,inch", "1mi 2610′ 1.5″");

    // Incompatible units reverts to non-sequence formatting.
    run(en, UNIT, json, " in:foot sequence:mile,foot,hertz", "2.4km");
    run(en_US, UNIT, json, " in:foot sequence:mile,foot,hertz", "1.5mi");
  }

  @Test
  public void testTemperature() {
    String json = "35.4";

    run(en_US, UNIT, json, " in:celsius out:fahrenheit", "95.7°");
  }

  private void run(CLDR.Locale locale, Formatter formatter, String json, String args, String expected) {
    try {
      String actual = format(locale, formatter, mk.args(args), json);
      Assert.assertEquals(actual, expected);
    } catch (CodeException e) {
      Assert.fail("formatter raised an error", e);
    }
  }

  private static String format(CLDR.Locale locale, Formatter formatter, Arguments args, String json)
      throws CodeException {

    Context ctx = new Context(JsonUtils.decode(json));
    ctx.cldrLocale(locale);
    formatter.validateArgs(args);
    Variables variables = new Variables("@", ctx.node());
    formatter.apply(ctx, args, variables);
    return variables.first().node().asText();
  }

}
