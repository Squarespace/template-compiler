/**
 * Copyright (c) 2014 SQUARESPACE, Inc.
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

package com.squarespace.template.plugins;
import static org.testng.Assert.assertEquals;

import java.util.Locale;

import org.testng.annotations.Test;

import com.squarespace.cldrengine.CLDR;
import com.squarespace.cldrengine.api.Decimal;


@Test(groups = { "unit" })
public class PluginUtilsTest {

  private static final char[] HEX_DIGIT = "0123456789abcdef".toCharArray();

  @Test
  public void testHexDigitToInt() {
    for (int i = 0; i < HEX_DIGIT.length; i++) {
      assertEquals(PluginUtils.hexDigitToInt(HEX_DIGIT[i]), i);
      assertEquals(PluginUtils.hexDigitToInt(Character.toUpperCase(HEX_DIGIT[i])), i);
    }
    assertEquals(PluginUtils.hexDigitToInt('x'), -1);
    assertEquals(PluginUtils.hexDigitToInt('Y'), -1);
    assertEquals(PluginUtils.hexDigitToInt('-'), -1);
  }

  @Test
  public void testFormatMoney() {
    assertEquals(PluginUtils.formatMoney(new Decimal(100), Locale.US), "1.00");
    assertEquals(PluginUtils.formatMoney(new Decimal(12345), Locale.US), "123.45");
    assertEquals(PluginUtils.formatMoney(new Decimal(12345), Locale.GERMAN), "123,45");
  }

  @Test
  public void testFormatMoneyCLDR() {
    CLDR en = CLDR.get("en-US");
    assertEquals(PluginUtils.formatMoney(new Decimal(1), "USD", en), "$1.00");
    assertEquals(PluginUtils.formatMoney(new Decimal(123.45), "USD", en), "$123.45");

    CLDR de = CLDR.get("de");
    assertEquals(PluginUtils.formatMoney(new Decimal(123.45), "USD", de), "123,45 $");
    assertEquals(PluginUtils.formatMoney(new Decimal(123.45), "EUR", de), "123,45 €");
  }

  @Test
  public void testRemoveTags() {
    assertEquals(PluginUtils.removeTags("hi,<\nhello < >world"), "hi, world");
  }

}
