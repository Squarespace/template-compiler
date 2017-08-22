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

import static com.squarespace.cldr.CLDR.Locale.de_DE;
import static com.squarespace.cldr.CLDR.Locale.en;
import static com.squarespace.cldr.CLDR.Locale.en_CA;
import static com.squarespace.cldr.CLDR.Locale.en_GB;
import static com.squarespace.cldr.CLDR.Locale.en_KY;
import static com.squarespace.cldr.CLDR.Locale.en_US;
import static com.squarespace.cldr.CLDR.Locale.fr_FR;
import static com.squarespace.cldr.CLDR.Locale.my_MM;
import static com.squarespace.cldr.CLDR.Locale.root;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.node.TextNode;
import com.squarespace.cldr.CLDR;
import com.squarespace.template.CodeException;
import com.squarespace.template.CodeMachine;
import com.squarespace.template.Context;
import com.squarespace.template.plugins.platform.PlatformUnitTestBase;


public class UnitsMetricPredicateTest extends PlatformUnitTestBase {

  @Test
  public void testLength() throws CodeException {
    String template = "{.units-metric? length}yes{.or}no{.end}";

    // METRIC
    run(root, template, "yes");
    run(en, template, "yes");
    run(en_CA, template, "yes");
    run(fr_FR, template, "yes");
    run(de_DE, template, "yes");

    // US measures with METRIC temperature
    run(my_MM, template, "no");

    // METRIC measures with US temperature
    run(en_KY, template, "yes");

    // NON-METRIC
    run(en_US, template, "no");
    run(en_GB, template, "no");
  }

  @Test
  public void testTemperature() throws CodeException {
    String template = "{.units-metric? temperature}yes{.or}no{.end}";

    // METRIC
    run(root, template, "yes");
    run(en, template, "yes");
    run(en_CA, template, "yes");
    run(fr_FR, template, "yes");
    run(de_DE, template, "yes");

    // US measures with METRIC temperature
    run(my_MM, template, "yes");

    // METRIC measures with US temperature
    run(en_KY, template, "no");

    // NON-METRIC
    run(en_US, template, "no");
    run(en_GB, template, "no");
  }

  private void run(CLDR.Locale locale, String template, String expected) throws CodeException {
    Context ctx = new Context(new TextNode(""));
    ctx.cldrLocale(locale);
    CodeMachine sink = machine();
    tokenizer(template, sink).consume();
    ctx.execute(sink.getCode());
    Assert.assertEquals(ctx.buffer().toString(), expected);
  }

}
