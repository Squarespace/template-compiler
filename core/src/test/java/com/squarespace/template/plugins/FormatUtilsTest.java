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

package com.squarespace.template.plugins;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.squarespace.template.plugins.FormatUtils.FormatArg;

@Test(groups = { "unit" })
public class FormatUtilsTest {

  private static String format(String pattern, String... values) {
    return format(pattern, false, values);
  }

  private static String format(String pattern, boolean legacy, String... values) {
    FormatArg[] args = new FormatArg[values.length];
    for (int i = 0; i < values.length; i++) {
      args[i] = new FormatArg(new Object[0]);
      args[i].value = values[i];
    }
    StringBuilder buf = new StringBuilder();
    FormatUtils.format(pattern, args, buf, legacy);
    return buf.toString();
  }

  @Test
  public void testSubstitution() {
    assertEquals(format("one {0} two {1}", "X", "Y"), "one X two Y");
    assertEquals(format("one {0} two {1}", true, "X", "Y"), "one X two Y");
    // multi-digit slot, needs at least 11 args
    assertEquals(format("ten {10}", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"), "ten k");
    assertEquals(format("ten {10}", true, "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"), "ten k");
  }

  @Test
  public void testIgnoringTagDoesNotLeakArgs() {
    // Legacy, digits inside a bad tag keep counting, which re-arms a
    // slot and leaks its value.
    assertEquals(format("cost {x 2} dollars", true, "ARG0"), "cost ARG0 dollars");
    // Legacy, a bad tag without digits is still swallowed.
    assertEquals(format("full {x} text", true, "ARG0"), "full  text");

    // Fixed, the bad tag is ignored until the closing brace.
    assertEquals(format("cost {x 2} dollars", "ARG0"), "cost  dollars");
    assertEquals(format("full {x} text", "ARG0"), "full  text");
  }

  @Test
  public void testBraceEscape() {
    // Legacy, braces have no escape: the tag is swallowed and a stray
    // "}" can survive.
    assertEquals(format("literal {{ brace }}", true, "ARG0"), "literal }");
    assertEquals(format("a {{0}} b", true, "ARG0"), "a } b");
    assertEquals(format("{{ }}", true, "ARG0"), "}");

    // Fixed, "{{" and "}}" are literal braces.
    assertEquals(format("literal {{ brace }}", "ARG0"), "literal { brace }");
    assertEquals(format("a {{0}} b", "ARG0"), "a {0} b");
    assertEquals(format("{{ }}", "ARG0"), "{ }");
  }

}
