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

import static com.squarespace.template.plugins.platform.i18n.LegacyMoneyFormatFactory.ENDS_WITH_LETTER;
import static com.squarespace.template.plugins.platform.i18n.LegacyMoneyFormatFactory.STARTS_WITH_LETTER;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.util.Currency;
import java.util.Locale;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squarespace.template.Arguments;
import com.squarespace.template.CodeException;
import com.squarespace.template.CodeMaker;
import com.squarespace.template.Context;
import com.squarespace.template.ExecuteErrorType;
import com.squarespace.template.Formatter;
import com.squarespace.template.JsonUtils;
import com.squarespace.template.TestSuiteRunner;
import com.squarespace.template.Variables;
import com.squarespace.template.compat.CompatLevel;
import com.squarespace.template.plugins.platform.PlatformUnitTestBase;


public class LegacyMoneyFormatFactoryTest extends PlatformUnitTestBase {

  private static final Formatter CLDR_MONEY = new MoneyFormatter();
  private static final Formatter LEGACY_MONEY = new LegacyMoneyFormatter();
  private final CodeMaker mk = maker();

//  private static final Map<String, CLDR.Locale> LOCALE_MAP = new HashMap<String, CLDR.Locale>() {{
//    put("de-DE", CLDR.Locale.de_DE);
//    put("en-UK", CLDR.Locale.en_GB);
//    put("en-US", CLDR.Locale.en_US);
//    put("es-US", CLDR.Locale.es_US);
//    put("fr-FR", CLDR.Locale.fr_FR);
//    put("sv-SE", CLDR.Locale.sv_SE);
//  }};

  private static final String[] LOCALES = new String[] {
      "de-DE", "en-UK", "en-US", "es-US", "fr-FR", "sv-SE"
  };

  private static final String[] CURRENCIES = new String[] {
      "AUD",
      "CAD",
      "EUR",
      "GBP",
      "USD",
      "JPY",
      "SEK"
  };

  private static final String[] NUMBERS = new String[] {
      "1234.56", "-1234.56"
  };

  private final TestSuiteRunner runner = new TestSuiteRunner(compiler(), LegacyMoneyFormatFactoryTest.class);

  @Test
  public void testLargeValuePrecision() throws Exception {
    String large = "123456789012345678.90";
    Arguments args = mk.args(" en-US");
    LEGACY_MONEY.validateArgs(args);

    // Level 0, the released double round-trip loses precision.
    Context ctx = new Context(moneyJson(large, "USD"));
    Variables variables = new Variables("@", ctx.node());
    LEGACY_MONEY.apply(ctx, args, variables);
    Assert.assertEquals(variables.first().node().asText(), "$123,456,789,012,345,680.00");

    // Fixed, the exact decimal value is preserved.
    ctx = new Context(moneyJson(large, "USD"));
    ctx.setCompat(CompatLevel.fixed());
    variables = new Variables("@", ctx.node());
    LEGACY_MONEY.apply(ctx, args, variables);
    Assert.assertEquals(variables.first().node().asText(), "$123,456,789,012,345,678.90");

    // Fixed, a missing decimalValue renders zero, like the released asDouble(0).
    ObjectNode noValue = JsonUtils.createObjectNode();
    noValue.put("currencyCode", "USD");
    ctx = new Context(noValue);
    ctx.setCompat(CompatLevel.fixed());
    variables = new Variables("@", ctx.node());
    LEGACY_MONEY.apply(ctx, args, variables);
    Assert.assertEquals(variables.first().node().asText(), "$0.00");
  }

  @Test
  public void testLargeValueFixtures() {
    runner.run("i18n-money-format-9.html", "i18n-money-format-10.html");
  }

  /**
   * Check compatibility between the legacy money formatter and the CLDR-based one.
   */
  @Test
  public void testCompatibility() throws Exception {
    System.out.printf("%-8s %-10s %15s %20s %20s\n", "LOCALE", "CURRENCY", "LEGACY", "CLDR (narrow)", "CLDR (std)");
    System.out.println("------------------------------------------------------------------------------");
    for (String locale : LOCALES) {
      for (String currency : CURRENCIES) {
        for (String n : NUMBERS) {
          String legacy = legacy(locale, currency, n);
          String narrow = cldr(locale, currency, n, true);
          String standard = cldr(locale, currency, n, false);
          System.out.printf("%-8s %-10s %15s %20s %20s\n", locale, currency, legacy, narrow, standard);
        }
        System.out.println();
      }
    }

    // The legacy formatter uses Squarespace symbols and its own placement
    // rules, so it differs from CLDR in expected ways.
    // en-US USD agrees with CLDR.
    Assert.assertEquals(legacy("en-US", "USD", "1234.56"), "$1,234.56");
    Assert.assertEquals(legacy("en-US", "USD", "1234.56"), cldr("en-US", "USD", "1234.56", true));

    // de-DE USD differs only in the no-break space before the right-hand symbol.
    Assert.assertEquals(legacy("de-DE", "USD", "1234.56"), "1.234,56$");
    Assert.assertEquals(cldr("de-DE", "USD", "1234.56", true), "1.234,56\u00a0$");

    // en-UK AUD differs only in the symbol, narrow CLDR drops the letter.
    Assert.assertEquals(legacy("en-UK", "AUD", "1234.56"), "A$1,234.56");
    Assert.assertEquals(cldr("en-UK", "AUD", "1234.56", true), "$1,234.56");
    Assert.assertEquals(cldr("en-UK", "AUD", "1234.56", false), "A$1,234.56");
  }

  @Test
  public void testStartsWithLetter() throws Exception {
    Assert.assertFalse(STARTS_WITH_LETTER.matcher("").matches());
    Assert.assertFalse(STARTS_WITH_LETTER.matcher("$").matches());
    Assert.assertFalse(STARTS_WITH_LETTER.matcher("£").matches());

    Assert.assertTrue(STARTS_WITH_LETTER.matcher("A$").matches());
    Assert.assertTrue(STARTS_WITH_LETTER.matcher("Ch$").matches());
    Assert.assertTrue(STARTS_WITH_LETTER.matcher("kr").matches());
  }

  @Test
  public void testEndsWithLetter() throws Exception {
    Assert.assertFalse(ENDS_WITH_LETTER.matcher("").matches());
    Assert.assertFalse(ENDS_WITH_LETTER.matcher("$").matches());
    Assert.assertFalse(ENDS_WITH_LETTER.matcher("£").matches());
    Assert.assertFalse(ENDS_WITH_LETTER.matcher("A$").matches());
    Assert.assertFalse(ENDS_WITH_LETTER.matcher("Ch$").matches());

    Assert.assertTrue(ENDS_WITH_LETTER.matcher("kr").matches());
  }

  @Test
  public void testJvmDefaultLocaleSymbols() throws Exception {
    Locale original = Locale.getDefault();
    Locale.setDefault(new Locale("fr", "FR"));
    try {
      // Legacy, a fr-FR JVM default locale throws for every target.
      for (String target : new String[] { "en-US", "fr-FR" }) {
        try {
          legacy(target, "USD", "1234.56");
          Assert.fail("expected IllegalArgumentException for " + target);
        } catch (IllegalArgumentException e) {
          Assert.assertTrue(e.getMessage().contains("Malformed pattern"), e.getMessage());
        }
      }

      // Legacy, safe mode at the default level collects the throw.
      Context legacyCtx = compiler().newExecutor()
          .template("[ {@|i18n-money-format en-US} ]")
          .json(moneyJson("1234.56", "USD").toString())
          .safeExecution(true)
          .execute();
      Assert.assertEquals(legacyCtx.getErrors().size(), 1);
      Assert.assertEquals(legacyCtx.getErrors().get(0).getType(), ExecuteErrorType.UNEXPECTED_ERROR);
      Assert.assertTrue(legacyCtx.getErrors().get(0).getMessage().contains("IllegalArgumentException"));

      // Fixed, the pattern parse does not read the JVM default locale.
      Context ctx = new Context(moneyJson("1234.56", "USD"));
      ctx.setCompat(CompatLevel.fixed());
      Arguments args = mk.args(" en-US");
      LEGACY_MONEY.validateArgs(args);
      Variables variables = new Variables("@", ctx.node());
      LEGACY_MONEY.apply(ctx, args, variables);
      Assert.assertEquals(variables.first().node().asText(), "$1,234.56");

      // Fixed, a comma decimal target formats correctly too.
      ctx = new Context(moneyJson("1234.56", "EUR"));
      ctx.setCompat(CompatLevel.fixed());
      args = mk.args(" fr-FR");
      LEGACY_MONEY.validateArgs(args);
      variables = new Variables("@", ctx.node());
      LEGACY_MONEY.apply(ctx, args, variables);
      char grouping = new DecimalFormatSymbols(new Locale("fr", "FR")).getGroupingSeparator();
      Assert.assertEquals(variables.first().node().asText(), "1" + grouping + "234,56\u20ac");

      // Fixed, safe mode at the fixed level renders without error.
      Context fixedCtx = compiler().newExecutor()
          .template("[ {@|i18n-money-format en-US} ]")
          .json(moneyJson("1234.56", "USD").toString())
          .safeExecution(true)
          .compat(CompatLevel.fixed())
          .execute();
      Assert.assertEquals(fixedCtx.getErrors().size(), 0);
      Assert.assertEquals(fixedCtx.buffer().toString(), "[ $1,234.56 ]");
    } finally {
      Locale.setDefault(original);
    }
  }

  @Test
  public void testFixedMatchesReleasedOnDotDecimalJvm() throws Exception {
    if (new DecimalFormatSymbols(Locale.getDefault()).getDecimalSeparator() != '.') {
      // The legacy path throws off a dot decimal JVM, see the test above.
      return;
    }
    for (String locale : LOCALES) {
      for (String currency : CURRENCIES) {
        for (String n : NUMBERS) {
          Locale tag = Locale.forLanguageTag(locale);
          Currency cur = Currency.getInstance(currency);
          double value = Double.parseDouble(n);
          String released = LegacyMoneyFormatFactory.create(tag, cur).format(value);
          String fixed = LegacyMoneyFormatFactory.create(tag, cur, false).format(value);
          Assert.assertEquals(fixed, released, locale + " " + currency + " " + n);
        }
      }
    }
  }

  private String legacy(String locale, String currency, String number) throws CodeException {
    Arguments args = mk.args(" " + locale);
    LEGACY_MONEY.validateArgs(args);
    Context ctx = new Context(moneyJson(number, currency));
    Variables variables = new Variables("@", ctx.node());
    LEGACY_MONEY.apply(ctx, args, variables);
    return variables.first().node().asText();
  }

  private String cldr(String tag, String currency, String number, boolean narrow) throws CodeException {
    Arguments args = mk.args(narrow ? " symbol:narrow" : " ");
    CLDR_MONEY.validateArgs(args);
    Locale locale = Locale.forLanguageTag(tag);
    Context ctx = new Context(moneyJson(number, currency), new StringBuilder(), locale);
    Variables variables = new Variables("@", ctx.node());
    CLDR_MONEY.apply(ctx, args, variables);
    return variables.first().node().asText();
  }

  private static ObjectNode moneyJson(String number, String currency) {
    ObjectNode r = JsonUtils.createObjectNode();
    r.put("decimalValue", new BigDecimal(number));
    r.put("currencyCode", currency);
    return r;
  }

}
