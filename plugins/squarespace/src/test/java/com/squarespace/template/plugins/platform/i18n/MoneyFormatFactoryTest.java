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

import static com.squarespace.template.plugins.platform.i18n.MoneyFormatFactory.ENDS_WITH_LETTER;
import static com.squarespace.template.plugins.platform.i18n.MoneyFormatFactory.STARTS_WITH_LETTER;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MoneyFormatFactoryTest {

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
}
