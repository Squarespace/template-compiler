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
