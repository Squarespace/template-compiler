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

import static org.testng.Assert.assertEquals;

import java.util.Locale;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.CodeException;
import com.squarespace.template.CodeMaker;
import com.squarespace.template.Context;
import com.squarespace.template.Formatter;
import com.squarespace.template.JsonUtils;
import com.squarespace.template.StringView;
import com.squarespace.template.TestSuiteRunner;
import com.squarespace.template.Variables;
import com.squarespace.template.plugins.platform.PlatformUnitTestBase;


public class InternationalFormattersTest extends PlatformUnitTestBase {

  private static final String en_US = "en-US";
  private static final String fr = "fr";

//  private static final String[] TESTED_CURRENCIES = {
//      "AUD",
//      "CAD",
//      "EUR",
//      "GBP",
//      "USD",
//      "JPY",
//      "SEK"
//  };
//
//  private static final String[] TESTED_LOCALES = {
//      "de_DE",
//      "en_UK",
//      "en_US",
//      "es_US",
//      "fr_FR",
//      "sv_SE"
//  };

  private static final DateTimeFormatter DATETIME = new DateTimeFormatter();
//  private static final DateTimeFieldFormatter DATETIMEFIELD = new DateTimeFieldFormatter();
  private static final LegacyMoneyFormatter MONEY_FORMATTER = new LegacyMoneyFormatter();

  private final TestSuiteRunner runner = new TestSuiteRunner(compiler(), InternationalFormatters.class);

  @Test
  public void testDateTimeFormatter() throws Exception {
    // epoch timestamp for: "Thu, 02 Nov 2017 18:26:57 GMT"
    String json = "1509647217000";
    CodeMaker mk = maker();

    // DEFAULTING

    assertEquals(format(en_US, DATETIME, mk.args(" xyz foo"), json), "2017, 14:26 EDT");

    // ENGLISH

    assertEquals(format(en_US, DATETIME, mk.args(""), json), "November 2, 2017");
    assertEquals(format(en_US, DATETIME, mk.args(" short"), json), "11/2/17, 2:26 PM");
    assertEquals(format(en_US, DATETIME, mk.args(" long"), json), "November 2, 2017 at 2:26:57 PM EDT");

    assertEquals(format(en_US, DATETIME, mk.args(" date"), json), "11/2/17");
    assertEquals(format(en_US, DATETIME, mk.args(" date:short"), json), "11/2/17");
    assertEquals(format(en_US, DATETIME, mk.args(" time"), json), "2:26 PM");
    assertEquals(format(en_US, DATETIME, mk.args(" time:medium"), json), "2:26:57 PM");
    assertEquals(format(en_US, DATETIME, mk.args(" date:full time:short"), json),
        "Thursday, November 2, 2017 at 2:26 PM");

    assertEquals(format(en_US, DATETIME, mk.args(" time:hm date:short"), json), "11/2/17, 2:26 PM");
    assertEquals(format(en_US, DATETIME, mk.args(" date:yMMMd time:hm"), json), "Nov 2, 2017, 2:26 PM");

    assertEquals(format(en_US, DATETIME, mk.args(" date:yMMMd time:medium wrap:short"), json),
        "Nov 2, 2017, 2:26:57 PM");
    assertEquals(format(en_US, DATETIME, mk.args(" time:medium wrap:full date:yMMMd"), json),
        "Nov 2, 2017 at 2:26:57 PM");

    // Bare skeletons

    assertEquals(format(en_US, DATETIME, mk.args(" date:short hm"), json), "11/2/17, 2:26 PM");
    assertEquals(format(en_US, DATETIME, mk.args(" yMMMd hm"), json), "Nov 2, 2017, 2:26 PM");


    // FRENCH

    assertEquals(format(fr, DATETIME, mk.args(" date:short"), json), "02/11/2017");
    assertEquals(format(fr, DATETIME, mk.args(" time:medium"), json), "14:26:57");
    assertEquals(format(fr, DATETIME, mk.args(" date:full time:short"), json),
        "jeudi 2 novembre 2017 à 14:26");

    assertEquals(format(fr, DATETIME, mk.args(" time:hm date:short"), json), "02/11/2017 2:26 PM");
    assertEquals(format(fr, DATETIME, mk.args(" date:yMMMd time:hm"), json), "2 nov. 2017 à 2:26 PM");

    assertEquals(format(fr, DATETIME, mk.args(" date:yMMMd time:medium wrap:short"), json),
        "2 nov. 2017 14:26:57");
    assertEquals(format(fr, DATETIME, mk.args(" time:medium wrap:full date:yMMMd"), json),
        "2 nov. 2017 à 14:26:57");
  }

  @Test
  public void testDateTimeFieldFormatter() throws Exception {
    // DEPRECATED
//    String json = "1509647217000";
//    CodeMaker mk = maker();
//
//    assertEquals(format(en_US, DATETIMEFIELD, mk.args(" dd"), json), "02");
//    assertEquals(format(en_US, DATETIMEFIELD, mk.args(" MMMM"), json), "November");
//    assertEquals(format(en_US, DATETIMEFIELD, mk.args(" EEE"), json), "Thu");
//    assertEquals(format(en_US, DATETIMEFIELD, mk.args(" EEEE"), json), "Thursday");
//
//    assertEquals(format(fr, DATETIMEFIELD, mk.args(" dd"), json), "02");
//    assertEquals(format(fr, DATETIMEFIELD, mk.args(" MMMM"), json), "novembre");
//    assertEquals(format(fr, DATETIMEFIELD, mk.args(" EEE"), json), "jeu.");
//    assertEquals(format(fr, DATETIMEFIELD, mk.args(" EEEE"), json), "jeudi");
  }

  @Test
  public void testMoneyFormatter() {
    runner.exec("i18n-money-format-%N.html");
  }

  public void testExtraArgument() throws Exception {
    try {
      MONEY_FORMATTER.validateArgs(new Arguments(new StringView(" en-US bad-arg")));
      Assert.fail("expected ArgumentsException, too many arguments passed");
    } catch (ArgumentsException e) {
      // fall through
    }
  }

  private String format(String locale, Formatter impl, Arguments args, String json) throws CodeException {
    Context ctx = new Context(JsonUtils.decode(json), Locale.forLanguageTag(locale));
    impl.validateArgs(args);
    Variables variables = new Variables("@", ctx.node());
    impl.apply(ctx, args, variables);
    return variables.first().node().asText();
  }

}
