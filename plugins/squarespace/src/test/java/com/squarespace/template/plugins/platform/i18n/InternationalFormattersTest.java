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

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.StringView;
import com.squarespace.template.TestSuiteRunner;
import com.squarespace.template.plugins.platform.PlatformUnitTestBase;

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

  private static final MoneyFormatter MONEY_FORMATTER = new MoneyFormatter();

  private final TestSuiteRunner runner = new TestSuiteRunner(compiler(), InternationalFormatters.class);

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
}
