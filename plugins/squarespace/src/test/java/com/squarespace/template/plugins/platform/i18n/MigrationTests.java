package com.squarespace.template.plugins.platform.i18n;

import org.testng.annotations.Test;

import com.squarespace.template.TestSuiteRunner;
import com.squarespace.template.plugins.platform.PlatformUnitTestBase;

public class MigrationTests  extends PlatformUnitTestBase {

  private final TestSuiteRunner runner = new TestSuiteRunner(compiler(), DecimalFormatterTest.class);

  @Test
  public void testDecimal() {
    runner.run(
      "f-migrate-decimal-1.html"
    );
  }

  @Test
  public void testDatetime() {
    runner.run(
        "f-migrate-datetime-1.html"
    );
  }

  @Test
  public void testMoney() {
    runner.run(
        "f-migrate-money-1.html"
    );
  }

  @Test
  public void testProductPrice() {
    runner.run(
        "f-migrate-product-price-1.html"
    );
  }
}
