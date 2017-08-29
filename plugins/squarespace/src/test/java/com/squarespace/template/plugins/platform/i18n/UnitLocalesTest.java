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

import static com.squarespace.cldr.CLDR.Locale.en_US;
import static com.squarespace.cldr.CLDR.Locale.fr_FR;

import java.util.regex.Pattern;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.cldr.CLDR;
import com.squarespace.compiler.parse.Pair;
import com.squarespace.template.CompiledTemplate;
import com.squarespace.template.Context;
import com.squarespace.template.GeneralUtils;
import com.squarespace.template.JsonUtils;
import com.squarespace.template.TestCaseParser;
import com.squarespace.template.plugins.platform.PlatformUnitTestBase;


public class UnitLocalesTest extends PlatformUnitTestBase {

  private static final Pattern RE_LINES = Pattern.compile("\n+");

  @Test
  public void testAcceleration() throws Exception {
    String tmpl = "units/acceleration-in.html";
    test(en_US, tmpl, "units/acceleration-out-en-US.html");
    test(fr_FR, tmpl, "units/acceleration-out-fr-FR.html");
  }

  @Test
  public void testAngle() throws Exception {
    String tmpl = "units/angle-in.html";
    test(en_US, tmpl, "units/angle-out-en-US.html");
    test(fr_FR, tmpl, "units/angle-out-fr-FR.html");
  }

  @Test
  public void testArea() throws Exception {
    String tmpl = "units/area-in.html";
    test(en_US, tmpl, "units/area-out-en-US.html");
    test(fr_FR, tmpl, "units/area-out-fr-FR.html");
  }

  @Test
  public void testConsumption() throws Exception {
    String tmpl = "units/consumption-in.html";
    test(en_US, tmpl, "units/consumption-out-en-US.html");
    test(fr_FR, tmpl, "units/consumption-out-fr-FR.html");
  }

  @Test
  public void testDigital() throws Exception {
    String tmpl = "units/digital-in.html";
    test(en_US, tmpl, "units/digital-out-en-US.html");
    test(fr_FR, tmpl, "units/digital-out-fr-FR.html");
  }

  @Test
  public void testDuration() throws Exception {
    String tmpl = "units/duration-in.html";
    test(en_US, tmpl, "units/duration-out-en-US.html");
    test(fr_FR, tmpl, "units/duration-out-fr-FR.html");
  }

  @Test
  public void testElectric() throws Exception {
    String tmpl = "units/electric-in.html";
    test(en_US, tmpl, "units/electric-out-en-US.html");
    test(fr_FR, tmpl, "units/electric-out-fr-FR.html");
  }

  @Test
  public void testFrequency() throws Exception {
    String tmpl = "units/frequency-in.html";
    test(en_US, tmpl, "units/frequency-out-en-US.html");
    test(fr_FR, tmpl, "units/frequency-out-fr-FR.html");
  }

  @Test
  public void testLength() throws Exception {
    String tmpl = "units/length-in.html";
    test(en_US, tmpl, "units/length-out-en-US.html");
    test(fr_FR, tmpl, "units/length-out-fr-FR.html");
  }

  @Test
  public void testMass() throws Exception {
    String tmpl = "units/mass-in.html";
    test(en_US, tmpl, "units/mass-out-en-US.html");
    test(fr_FR, tmpl, "units/mass-out-fr-FR.html");
  }

  @Test
  public void testPower() throws Exception {
    String tmpl = "units/power-in.html";
    test(en_US, tmpl, "units/power-out-en-US.html");
    test(fr_FR, tmpl, "units/power-out-fr-FR.html");
  }

  @Test
  public void testSpeed() throws Exception {
    String tmpl = "units/speed-in.html";
    test(en_US, tmpl, "units/speed-out-en-US.html");
    test(fr_FR, tmpl, "units/speed-out-fr-FR.html");
  }

  @Test
  public void testTemperature() throws Exception {
    String tmpl = "units/temperature-in.html";
    test(en_US, tmpl, "units/temperature-out-en-US.html");
    test(fr_FR, tmpl, "units/temperature-out-fr-FR.html");
  }

  @Test
  public void testVolume() throws Exception {
    String tmpl = "units/volume-in.html";
    test(en_US, tmpl, "units/volume-out-en-US.html");
    test(fr_FR, tmpl, "units/volume-out-fr-FR.html");
  }

  private void test(CLDR.Locale locale, String templatePath, String expectedPath) throws Exception {
    Pair<CompiledTemplate, JsonNode> compiled = load(templatePath, "units/_numbers.json");
    Context ctx = new Context(compiled._2);
    ctx.cldrLocale(locale);
    ctx.execute(compiled._1.code());
    String actual = reformat(ctx.buffer().toString());
    String expected = reformat(GeneralUtils.loadResource(getClass(), expectedPath));
    if (!actual.trim().equals(expected.trim())) {
      String diff = TestCaseParser.diff(expected, actual);
      throw new AssertionError("File '" + expectedPath + "' output does not match:\n" + diff);
    }
  }

  private static String reformat(String raw) {
    String[] lines = RE_LINES.split(raw);
    StringBuilder buf = new StringBuilder();
    for (String line : lines) {
      line = line.trim();
      if (!line.isEmpty()) {
        buf.append(line).append('\n');
      }
    }
    return buf.toString();
  }

  private Pair<CompiledTemplate, JsonNode> load(String templatePath, String jsonPath) throws Exception {
    String template = GeneralUtils.loadResource(getClass(), templatePath);
    String json = GeneralUtils.loadResource(getClass(), jsonPath);
    CompiledTemplate code = compiler().compile(template);
    JsonNode node = JsonUtils.decode(json);
    return Pair.pair(code, node);
  }

}
