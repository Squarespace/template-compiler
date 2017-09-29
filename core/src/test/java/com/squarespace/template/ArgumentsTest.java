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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;


@Test(groups = { "unit" })
public class ArgumentsTest extends UnitTestBase {

  @SuppressWarnings("unlikely-arg-type")
  @Test
  public void testBasics() {
    CodeMaker mk = maker();
    Arguments args = new Arguments(mk.view(" a b c"));
    assertEquals(args.first(), "a");
    assertEquals(args.get(0), "a");
    assertEquals(args.get(1), "b");
    assertEquals(args.get(2), "c");
    assertEquals(args.count(), 3);
    Assert.assertFalse(args.isEmpty());
    assertEquals(args.getDelimiter(), ' ');
    assertEquals(args.getArgs(), Arrays.asList("a", "b", "c"));
    assertEquals(args.join(), "a b c");

    Arguments args2 = new Arguments(mk.view(" a b c"));
    assertEquals(args, args2);

    assertFalse(args.equals(null));
    assertFalse(args.equals(" a b c"));
    assertFalse(args.equals("a b c"));

    Arguments args3 = mk.args(":a:b:c");
    assertFalse(args.equals(args3));

    Arguments args4 = new Arguments(null);
    assertFalse(args.equals(args4));
    assertEquals(args4, Constants.EMPTY_ARGUMENTS);
  }

  @Test
  public void testAssertions() throws ArgumentsException {
    CodeMaker mk = maker();
    Arguments args = new Arguments(mk.view("/1/2/3"));
    for (int i = 0; i < 3; i++) {
      args.atLeast(i);
    }
    args.exactly(3);
    args.atMost(3);
    args.between(0, 3);
  }

  @Test
  public void testFailedAssertions() {
    CodeMaker mk = maker();
    Arguments args = mk.args(":1:2:3");
    try {
      args.atLeast(5);
      fail();
    } catch (ArgumentsException e) {
    }

    try {
      args.atMost(1);
      fail();
    } catch (ArgumentsException e) {
    }

    try {
      args.exactly(2);
      fail();
    } catch (ArgumentsException e) {
    }
  }

  @Test
  public void testOpaque() {
    CodeMaker mk = maker();
    Integer num = new Integer(3);
    Arguments args = mk.args(" a b");
    args.setOpaque(num);
    assertEquals(args.getOpaque(), num);
  }
}
