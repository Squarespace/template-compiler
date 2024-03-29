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

import java.math.BigDecimal;
import java.util.Locale;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squarespace.cldrengine.api.CurrencyType;
import com.squarespace.template.Arguments;
import com.squarespace.template.CodeException;
import com.squarespace.template.CodeMaker;
import com.squarespace.template.Compiler;
import com.squarespace.template.Context;
import com.squarespace.template.JsonUtils;
import com.squarespace.template.Variables;
import com.squarespace.template.plugins.platform.PlatformUnitTestBase;


/**
 * Tests for the money formatter using the new money JSON structure.
 */
public class MoneyFormatterTest extends PlatformUnitTestBase {

  private static final String en_US = "en-US";

  private static final MoneyFormatter MONEY = new MoneyFormatter();
  private final CodeMaker mk = maker();

  @Test
  public void testExecutor() throws CodeException {
    String json = "{\"amt\": {\"value\": \"-15789.12\", \"currency\": \"USD\"}, "
         + "\"featureFlags\": {\"useCLDRMoneyFormat\": true}}";
    String template = "{amt|money style:short mode:significant}";

    assertEquals(executeLocale(template, json, "fr"), "-16 k $US");
    assertEquals(executeLocale(template, json, "en-US"), "-$16K");
  }

  private String executeLocale(String template, String json, String locale) throws CodeException {
    Compiler compiler = compiler();
    Context ctx = compiler.newExecutor()
        .json(json)
        .template(template)
        .locale(Locale.forLanguageTag(locale))
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
  public void testBadMoney() {
    // Mixing up the serialized money
    ObjectNode m = JsonUtils.createObjectNode();
    m.put("currencyCode", "USD");
    m.put("value", "123.456");
    run(en_US, m.toString(), "", "");
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

  @Test
  public void testNoCLDR() {
    String args = " minInt:4";
    run(en_US, usd("1.57"), args, "$0,001.57");
  }

  @Test
  public void testLegacy() {
    run(en_US, usd("0.50"), "", "$0.50");
  }

  private static BigDecimal big(String n) {
    return new BigDecimal(n);
  }

  private static String usd(String n) {
    return money(n, CurrencyType.USD);
  }

  private static String eur(String n) {
    return money(n, CurrencyType.EUR);
  }

  private static String usd(BigDecimal n) {
    return money(n.toPlainString(), CurrencyType.USD);
  }

  private static String eur(BigDecimal n) {
    return money(n.toPlainString(), CurrencyType.EUR);
  }

  private static String legacy(String value, String currency) {
    ObjectNode m = JsonUtils.createObjectNode();
    m.put("currencyCode", currency);
    m.put("decimalValue", value);
    return m.toString();
  }

  private static String money(String value, CurrencyType code) {
    ObjectNode m = JsonUtils.createObjectNode();
    m.put("currency", code.value());
    m.put("value", value);
    return m.toString();
  }

  private void run(String locale, String json, String args, String expected) {
    try {
      String actual = format(locale, mk.args(args), json);
      Assert.assertEquals(actual, expected);
    } catch (CodeException e) {
      Assert.fail("formatter raised an error", e);
    }
  }

  private static String format(String locale,  Arguments args, String json) throws CodeException {
    Context ctx = new Context(JsonUtils.decode(json));
    ctx.javaLocale(Locale.forLanguageTag(locale));
    MONEY.validateArgs(args);
    Variables variables = new Variables("@", ctx.node());
    MONEY.apply(ctx, args, variables);
    return variables.first().node().asText();
  }


}
