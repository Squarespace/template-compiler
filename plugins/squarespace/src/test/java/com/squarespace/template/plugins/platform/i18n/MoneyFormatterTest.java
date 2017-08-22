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
import static org.testng.Assert.assertEquals;

import java.math.BigDecimal;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squarespace.cldr.CLDR;
import com.squarespace.template.Arguments;
import com.squarespace.template.CodeException;
import com.squarespace.template.CodeMaker;
import com.squarespace.template.Compiler;
import com.squarespace.template.Context;
import com.squarespace.template.JsonUtils;
import com.squarespace.template.TestSuiteRunner;
import com.squarespace.template.Variables;
import com.squarespace.template.plugins.platform.PlatformUnitTestBase;


/**
 * Tests for the money formatter.
 *
 * NOTE: incomplete
 */
public class MoneyFormatterTest extends PlatformUnitTestBase {

  private static final MoneyFormatter MONEY = new MoneyFormatter();
  private final TestSuiteRunner runner = new TestSuiteRunner(compiler(), InternationalFormatters.class);
  private final CodeMaker mk = maker();

  @Test
  public void testTemplates() {
    runner.run(
        "f-decimal-1.html",
        "f-decimal-2.html"
    );
  }

  @Test
  public void testExecutor() throws CodeException {
    String json = "{\"decimalValue\": \"-15789.12\", \"currencyCode\": \"USD\"}";
    String template = "{@|money style:short mode:significant}";

    assertEquals(executeLocale(template, json, CLDR.Locale.fr), "-15,8 k $US");
    assertEquals(executeLocale(template, json, CLDR.Locale.en_US), "-$15.8K");
  }

  private String executeLocale(String template, String json, CLDR.Locale locale) throws CodeException {
    Compiler compiler = compiler();
    Context ctx = compiler.newExecutor()
        .json(json)
        .template(template)
        .cldrLocale(locale)
        .execute();
    return ctx.buffer().toString();
  }

  @Test
  public void testBasics() {
    String args = " style:accounting group";
    run(en_US, usd("23.98"), args, "$23.98");
    run(en_US, usd("-1551.75"), args, "($1,551.75)");

    args = " style:name mode:significant-maxfrac minSig:1";
    run(en_US, usd("1"), args, "1 US dollar");
    run(en_US, usd("-1551.75"), args, "-1,551.75 US dollars");
    run(en_US, usd(big("-1551.75")), args, "-1,551.75 US dollars");

    run(en_US, eur("1"), args, "1 euro");
    run(en_US, eur("-1551.75"), args, "-1,551.75 euros");
    run(en_US, eur(big("-1551.75")), args, "-1,551.75 euros");
  }

  @Test
  public void testLong() {
    String args = " style:name mode:significant group minSig:1";
    run(en_US, usd("0.00"), args, "0 US dollars");
    run(en_US, usd("1.00"), args, "1 US dollar");
    run(en_US, usd("3.59"), args, "3.59 US dollars");
    run(en_US, usd("1200"), args, "1,200 US dollars");
    run(en_US, usd("-15789.12"), args, "-15,789.12 US dollars");
    run(en_US, usd("99999.00"), args, "99,999 US dollars");
    run(en_US, usd("-100200300.40"), args, "-100,200,300.4 US dollars");
    run(en_US, usd("10000000001.00"), args, "10,000,000,001 US dollars");
  }

  @Test
  public void testSignificantDigits() {
    String args = " style:short";
    run(en_US, usd("-1551.75"), args, "-$1.6K");

    args = " style:short mode:significant-maxfrac maxfrac:0";
    run(en_US, usd("-1551.75"), args, "-$2K");

    args = " style:short mode:significant maxSig:3";
    run(en_US, usd("-1551.75"), args, "-$1.55K");

    args = " style:short mode:significant maxSig:4";
    run(en_US, usd("-1551.75"), args, "-$1.552K");
  }

  @Test
  public void testFractionDigits() {
    String args = " style:short mode:fractions minFrac:2";
    run(en_US, usd("-1551.75"), args, "-$1.55K");
    run(en_US, eur("-1551.75"), args, "-€1.55K");

    args = " style:short mode:fractions minFrac:3";
    run(en_US, usd("-1551.75"), args, "-$1.552K");
    run(en_US, eur("-1551.75"), args, "-€1.552K");
  }

  @Test
  public void testIntegerDigits() {
    String args = " minInt:4";
    run(en_US, usd("1.57"), args, "$0,001.57");
    run(en_US, usd("1.57"), args, "$0,001.57");

    args = " style:code minInt:4";
    run(en_US, usd("1.57"), args, "0,001.57 USD");
  }

  private static BigDecimal big(String n) {
    return new BigDecimal(n);
  }

  private static String usd(String n) {
    return money(n, CLDR.Currency.USD);
  }

  private static String eur(String n) {
    return money(n, CLDR.Currency.EUR);
  }

  private static String usd(BigDecimal n) {
    return money(n, CLDR.Currency.USD);
  }

  private static String eur(BigDecimal n) {
    return money(n, CLDR.Currency.EUR);
  }

  private static String money(BigDecimal n, CLDR.Currency code) {
    ObjectNode m = moneyBase(code.name());
    m.put("decimalValue", n);
    return m.toString();
  }

  private static String money(String n, CLDR.Currency code) {
    ObjectNode m = moneyBase(code.name());
    m.put("decimalValue", n);
    return m.toString();
  }

  private static ObjectNode moneyBase(String currencyCode) {
    ObjectNode obj = JsonUtils.createObjectNode();
    obj.put("currencyCode", currencyCode);
    return obj;
  }

  private void run(CLDR.Locale locale, String json, String args, String expected) {
    try {
      String actual = format(locale, mk.args(args), json);
      Assert.assertEquals(actual, expected);
    } catch (CodeException e) {
      Assert.fail("formatter raised an error", e);
    }
  }

  private static String format(CLDR.Locale locale,  Arguments args, String json) throws CodeException {
    Context ctx = new Context(JsonUtils.decode(json));
    ctx.cldrLocale(locale);
    MONEY.validateArgs(args);
    Variables variables = new Variables("@", ctx.node());
    MONEY.apply(ctx, args, variables);
    return variables.first().node().asText();
  }


}
