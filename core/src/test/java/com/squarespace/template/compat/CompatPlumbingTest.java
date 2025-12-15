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

package com.squarespace.template.compat;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.squarespace.template.CodeException;
import com.squarespace.template.Compiler;
import com.squarespace.template.CompilerExecutor;
import com.squarespace.template.Context;
import com.squarespace.template.TestSuiteRunner;
import com.squarespace.template.TreeEmitter;
import com.squarespace.template.UnitTestBase;


/**
 * Checks the compat level plumbing and the released surface parity. While no
 * patch is wired to a code path, output and code shape must be identical at
 * every level. When a patch is wired, move its templates to the patch level
 * tests.
 */
public class CompatPlumbingTest extends UnitTestBase {

  private static final List<String> TEMPLATES = Arrays.asList(
      "plain text",
      "{a} {b.c} {d[0]}",
      "{a|str} {s|truncate 3} {n|mod 3}",
      "{.if a}{yes}{.end}",
      "{.section items}{i}{.end}",
      "{.include part} {missing}",
      "{.eval 2 * 3 + 40}",
      "{o|json}",
      "{.if nth? n 2}{y}{.end}");

  private static final String JSON =
      "{\"a\":true,\"s\":\"abcdef\",\"n\":7,\"d\":{\"c\":5,\"items\":[1,2]},\"o\":{\"k\":\"v\"}}";

  private final TestSuiteRunner runner = new TestSuiteRunner(compiler(), CompatPlumbingTest.class);

  @Test
  public void testFixtureLevelProperty() {
    runner.exec("compat-level-%N.html");
  }

  @Test
  public void testDefaultLevelIsReleased() throws Exception {
    Context ctx = compiler().newExecutor()
        .template("{a}")
        .json("{\"a\":1}")
        .execute();
    assertEquals(ctx.getCompat(), CompatLevel.defaultLevel());
  }

  @Test
  public void testExecutorPlumbing() throws Exception {
    Context ctx = compiler().newExecutor()
        .template("{a}")
        .json("{\"a\":1}")
        .compatLevel(2)
        .compatPatch(Patch.MOD_ZERO)
        .execute();
    assertEquals(ctx.getCompat(), CompatLevel.at(2).withPatch(Patch.MOD_ZERO));
    assertTrue(ctx.compatEnabled(Patch.MOD_ZERO));
    assertFalse(ctx.compatEnabled(Patch.JSON_START_KEYWORD));
  }

  @Test
  public void testParityAcrossLevels() throws Exception {
    Compiler compiler = compiler();
    for (String template : TEMPLATES) {
      String base = render(compiler, template, CompatLevel.defaultLevel());
      for (int level = 1; level <= Patch.maxThreshold(); level++) {
        String actual = render(compiler, template, CompatLevel.at(level));
        assertEquals(actual, base, template + " at level " + level);
      }
    }
  }

  @Test
  public void testCompileParityAcrossLevels() throws Exception {
    Compiler compiler = compiler();
    for (String template : TEMPLATES) {
      StringBuilder base = new StringBuilder();
      TreeEmitter.emit(compiler.compile(template, false, false, CompatLevel.defaultLevel()).code(), 0, base);
      for (int level = 1; level <= Patch.maxThreshold(); level++) {
        StringBuilder buf = new StringBuilder();
        TreeEmitter.emit(compiler.compile(template, false, false, CompatLevel.at(level)).code(), 0, buf);
        assertEquals(buf.toString(), base.toString(), template + " tree at level " + level);
      }
    }
  }

  private static String render(Compiler compiler, String template, CompatLevel compat) throws CodeException {
    CompilerExecutor exec = compiler.newExecutor()
        .template(template)
        .json(JSON)
        .safeExecution(true)
        .enableExpr(true)
        .enableInclude(true)
        .partialsMap("{\"part\":\"p\"}")
        .compat(compat);
    return exec.execute().buffer().toString();
  }
}
