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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Locale;

import org.testng.annotations.Test;


/**
 * Tests for PlatformUtils.
 */
public class PlatformUtilsTest {

  @Test
  public void testFormatBookkeeperMoneyNegativeUs() {
    // Pin the exact output: locale minus sign, no accounting parentheses.
    String actual = PlatformUtils.formatBookkeeperMoney(-123.45, Locale.US);
    assertEquals(actual, "-$1.23");
    assertTrue(actual.startsWith("-"));
    assertFalse(actual.contains("("));
  }
}
