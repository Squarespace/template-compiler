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
package com.squarespace.template.plugins.platform.i18n;

import org.testng.annotations.Test;

import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.StringView;
import com.squarespace.template.TestSuiteRunner;
import com.squarespace.template.plugins.platform.PlatformUnitTestBase;

public class InternationalFormattersTest extends PlatformUnitTestBase {

  private static final MoneyFormatter MONEY_FORMATTER = new MoneyFormatter();

  private final TestSuiteRunner runner = new TestSuiteRunner(compiler(), InternationalFormatters.class);

  @Test
  public void testMoneyFormatter() throws Exception {
    runner.run(
        "f-money-1.html",
        "f-money-2.html",
        "f-money-3.html",
        "f-money-4.html",
        "f-money-5.html",
        "f-money-6.html",
        "f-money-7.html",
        "f-money-8.html",
        "f-money-9.html"
    );
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
