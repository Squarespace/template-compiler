/**
 * Copyright (c) 2016 SQUARESPACE, Inc.
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

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.cldr.CLDRLocale;
import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.CodeException;
import com.squarespace.template.CodeMaker;
import com.squarespace.template.Context;
import com.squarespace.template.Formatter;
import com.squarespace.template.JsonUtils;
import com.squarespace.template.StringView;
import com.squarespace.template.TestSuiteRunner;
import com.squarespace.template.plugins.platform.PlatformUnitTestBase;
import com.squarespace.template.plugins.platform.i18n.InternationalFormatters.DateTimeFieldFormatter;
import com.squarespace.template.plugins.platform.i18n.InternationalFormatters.DateTimeFormatter;


public class InternationalFormattersTest extends PlatformUnitTestBase {


  private static final String[] TESTED_CURRENCIES = {
      "AUD",
      "CAD",
      "EUR",
      "GBP",
      "USD",
      "JPY",
      "SEK"
  };

  private static final String[] TESTED_LOCALES = {
      "de_DE",
      "en_UK",
      "en_US",
      "es_US",
      "fr_FR",
      "sv_SE"
  };

  private static final DateTimeFormatter DATETIME = new DateTimeFormatter();

  private static final DateTimeFieldFormatter DATETIMEFIELD = new DateTimeFieldFormatter();

  private static final MoneyFormatter MONEY_FORMATTER = new MoneyFormatter();

  private static final CLDRLocale EN_US = new CLDRLocale("en", "US", "", "POSIX");

  private static final CLDRLocale FR = new CLDRLocale("fr", "", "", "");

  private final TestSuiteRunner runner = new TestSuiteRunner(compiler(), InternationalFormatters.class);

  @Test
  public void testDateTimeFormatter() throws Exception {
    // epoch timestamp for: "Thu, 02 Nov 2017 18:26:57 GMT"
    String json = "1509647217000";
    CodeMaker mk = maker();

    // ENGLISH

    assertEquals(format(EN_US, DATETIME, mk.args(" date-short"), json), "11/2/17");
    assertEquals(format(EN_US, DATETIME, mk.args(" time-medium"), json), "2:26:57 PM");
    assertEquals(format(EN_US, DATETIME, mk.args(" date-full time-short"), json),
        "Thursday, November 2, 2017 at 2:26 PM");

    assertEquals(format(EN_US, DATETIME, mk.args(" hm date-short"), json), "11/2/17, 2:26 PM");
    assertEquals(format(EN_US, DATETIME, mk.args(" yMMMd hm"), json), "Nov 2, 2017, 2:26 PM");

    assertEquals(format(EN_US, DATETIME, mk.args(" yMMMd time-medium wrap-short"), json),
        "Nov 2, 2017, 2:26:57 PM");
    assertEquals(format(EN_US, DATETIME, mk.args(" time-medium wrap-full yMMMd"), json),
        "Nov 2, 2017 at 2:26:57 PM");

    // FRENCH

    assertEquals(format(FR, DATETIME, mk.args(" date-short"), json), "02/11/2017");
    assertEquals(format(FR, DATETIME, mk.args(" time-medium"), json), "14:26:57");
    assertEquals(format(FR, DATETIME, mk.args(" date-full time-short"), json),
        "jeudi 2 novembre 2017 à 14:26");

    assertEquals(format(FR, DATETIME, mk.args(" hm date-short"), json), "02/11/2017 2:26 PM");
    assertEquals(format(FR, DATETIME, mk.args(" yMMMd hm"), json), "2 nov. 2017 2:26 PM");

    assertEquals(format(FR, DATETIME, mk.args(" yMMMd time-medium wrap-short"), json),
        "2 nov. 2017 14:26:57");
    assertEquals(format(FR, DATETIME, mk.args(" time-medium wrap-full yMMMd"), json),
        "2 nov. 2017 à 14:26:57");
  }

  @Test
  public void testDateTimeFieldFormatter() throws Exception {
    String json = "1509647217000";
    CodeMaker mk = maker();

    assertEquals(format(EN_US, DATETIMEFIELD, mk.args(" dd"), json), "02");
    assertEquals(format(EN_US, DATETIMEFIELD, mk.args(" MMMM"), json), "November");
    assertEquals(format(EN_US, DATETIMEFIELD, mk.args(" EEE"), json), "Thu");
    assertEquals(format(EN_US, DATETIMEFIELD, mk.args(" EEEE"), json), "Thursday");

    assertEquals(format(FR, DATETIMEFIELD, mk.args(" dd"), json), "02");
    assertEquals(format(FR, DATETIMEFIELD, mk.args(" MMMM"), json), "novembre");
    assertEquals(format(FR, DATETIMEFIELD, mk.args(" EEE"), json), "jeu.");
    assertEquals(format(FR, DATETIMEFIELD, mk.args(" EEEE"), json), "jeudi");
  }

  @Test
  public void testMoneyFormatter() throws Exception {
    List<String> paths = new ArrayList<>();
    for (String currency : TESTED_CURRENCIES) {
      for (String locale : TESTED_LOCALES) {
        paths.add("i18n-money-format-" + currency + "-" + locale + ".html");
      }
    }

    runner.run(paths.toArray(new String[paths.size()]));
  }

  @Test(expectedExceptions = ArgumentsException.class)
  public void testInvalidLocale() throws Exception {
    MONEY_FORMATTER.validateArgs(new Arguments(new StringView("qq-QQ")));
  }

  @Test(expectedExceptions = ArgumentsException.class)
  public void testExtraArgument() throws Exception {
    MONEY_FORMATTER.validateArgs(new Arguments(new StringView("en-US bad-arg")));
  }

  private String format(CLDRLocale locale, Formatter impl, Arguments args, String json) throws CodeException {
    Context ctx = new Context(JsonUtils.decode(json));
    ctx.cldrLocale(locale);
    impl.validateArgs(args);
    JsonNode node = impl.apply(ctx, args, ctx.node());
    return node.asText();
  }
}
