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

package com.squarespace.template.plugins.platform;

import org.testng.annotations.Test;

import com.squarespace.template.CodeException;
import com.squarespace.template.Formatter;
import com.squarespace.template.plugins.platform.CommerceFormatters.MoneyDashFormatter;


/**
 * Extracted from Commons library at commit ab4ba7a6f2b872a31cb6449ae9e96f5f5b30f471
 */
public class CommerceFormattersTest extends TemplateUnitTestBase {

  private static final Formatter MONEY_DASH_FORMAT = new MoneyDashFormatter();

  @Test
  public void testMoneyFormat() throws CodeException {
    assertFormatter(MONEY_DASH_FORMAT, "1", "0.01");
    assertFormatter(MONEY_DASH_FORMAT, "10", "0.10");
    assertFormatter(MONEY_DASH_FORMAT, "1201", "12.01");
    assertFormatter(MONEY_DASH_FORMAT, "350", "3.50");
    assertFormatter(MONEY_DASH_FORMAT, "100", "1.00");
    assertFormatter(MONEY_DASH_FORMAT, "100000", "1,000.00");
    assertFormatter(MONEY_DASH_FORMAT, "100003", "1,000.03");
    assertFormatter(MONEY_DASH_FORMAT, "1241313", "12,413.13");
  }

}
