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
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squarespace.cldr.CLDR;
import com.squarespace.cldr.CLDRLocale;
import com.squarespace.template.Arguments;
import com.squarespace.template.CodeException;
import com.squarespace.template.CodeMaker;
import com.squarespace.template.Context;
import com.squarespace.template.Formatter;
import com.squarespace.template.JsonUtils;
import com.squarespace.template.Variables;
import com.squarespace.template.plugins.platform.PlatformUnitTestBase;


public class LegacyMoneyFormatFactoryTest extends PlatformUnitTestBase {

  private static final Formatter CLDR_MONEY = new MoneyFormatter();
  private static final Formatter LEGACY_MONEY = new LegacyMoneyFormatter();
  private final CodeMaker mk = maker();

  private static final Map<String, CLDRLocale> LOCALE_MAP = new HashMap<String, CLDRLocale>() {{
    put("de-DE", CLDR.DE_DE);
    put("en-UK", CLDR.EN_GB);
    put("en-US", CLDR.EN_US);
    put("es-US", CLDR.ES_US);
    put("fr-FR", CLDR.FR_FR);
    put("sv-SE", CLDR.SV_SE);
  }};

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

  /**
   * Check compatibility between the legacy money formatter and the CLDR-based one.
   */
  @Test
  public void testCompatibility() throws Exception {
    System.out.printf("%-8s %-10s %15s %20s %20s\n", "LOCALE", "CURRENCY", "LEGACY", "CLDR (narrow)", "CLDR (std)");
    System.out.println("------------------------------------------------------------------------------");
    for (String locale : LOCALE_MAP.keySet()) {
      for (String currency : CURRENCIES) {
        for (String n : NUMBERS) {
          String legacy = legacy(locale, currency, n);
          String narrow = cldr(LOCALE_MAP.get(locale), currency, n, true);
          String standard = cldr(LOCALE_MAP.get(locale), currency, n, false);
          System.out.printf("%-8s %-10s %15s %20s %20s\n", locale, currency, legacy, narrow, standard);
        }
        System.out.println();
      }
    }
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

  private String legacy(String locale, String currency, String number) throws CodeException {
    Arguments args = mk.args(" " + locale);
    LEGACY_MONEY.validateArgs(args);
    Context ctx = new Context(moneyJson(number, currency));
    Variables variables = new Variables("@", ctx.node());
    LEGACY_MONEY.apply(ctx, args, variables);
    return variables.first().node().asText();
  }

  private String cldr(CLDRLocale locale, String currency, String number, boolean narrow) throws CodeException {
    Arguments args = mk.args(narrow ? " symbol:narrow" : " ");
    CLDR_MONEY.validateArgs(args);
    Context ctx = new Context(moneyJson(number, currency));
    ctx.cldrLocale(locale);
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
