package com.squarespace.template.plugins.platform.i18n;

import org.testng.annotations.Test;

import com.squarespace.template.TestSuiteRunner;
import com.squarespace.template.plugins.platform.PlatformUnitTestBase;

@Test(groups = { "unit" })
public class MultiLocaleTest extends PlatformUnitTestBase {

  private final TestSuiteRunner runner = new TestSuiteRunner(compiler(), MultiLocaleTest.class);

  @Test
  public void testMultiLocale() {
    runner.run("f-datetime-multi-locale-1.html", "f-datetime-multi-locale-2.html");
  }

}
