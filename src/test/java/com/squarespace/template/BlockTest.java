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

package com.squarespace.template;

import org.testng.Assert;
import org.testng.annotations.Test;


@Test(groups = { "unit" })
public class BlockTest extends UnitTestBase {

  @Test
  public void testEquality() {
    CodeMaker maker = maker();
    Block b1 = new Block(2);
    b1.add(maker.text("foo"), maker.end(), maker.eof());
    Block b2 = new Block(2);
    b2.add(maker.text("foo"), maker.end(), maker.eof());
    Assert.assertEquals(b1, b2);

    Block b3 = new Block(2);
    b3.add(maker.text("foo"), maker.eof(), maker.end());
    Assert.assertNotEquals(b1, b3);

    Block b4 = new Block(2);
    b4.add(maker.text("foo"));
    Assert.assertNotEquals(b1, b4);
    Assert.assertNotEquals(b3, b4);

    Assert.assertFalse(b1.equals(null));
    Assert.assertFalse(b1.equals("foo"));
  }


}
