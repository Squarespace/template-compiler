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

import static com.squarespace.template.Constants.EMPTY_ARGUMENTS;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import com.squarespace.template.CodeException;
import com.squarespace.template.Context;
import com.squarespace.template.JsonUtils;
import com.squarespace.template.TestSuiteRunner;
import com.squarespace.template.compat.CompatLevel;

public class SlidePredicatesTest extends PlatformUnitTestBase {

  private final TestSuiteRunner runner = new TestSuiteRunner(compiler(), SlidePredicatesTest.class);

  @Test
  public void testCurrentType() {
    runner.run("p-current-type.html");
  }

  @Test
  public void testCurrentTypeAritySoft() throws CodeException {
    Context ctx = new Context(JsonUtils.decode("{\"currentType\": 5}"));

    // Legacy, a call with no arguments throws at render time.
    try {
      SlidePredicates.CURRENT_TYPE.apply(ctx, EMPTY_ARGUMENTS);
      fail("expected exception");
    } catch (RuntimeException e) {
      // Expected
    }

    // Fixed, it evaluates false, so the .or branch renders.
    ctx.setCompat(CompatLevel.fixed());
    assertEquals(SlidePredicates.CURRENT_TYPE.apply(ctx, EMPTY_ARGUMENTS), false);

    // Extra args are ignored, matching the JS engine: first arg wins.
    Context gallery = compiler().newExecutor()
        .template("{.current-type? gallery extra}a{.or}b{.end}")
        .json("{\"currentType\": 5}")
        .compat(CompatLevel.fixed())
        .execute();
    assertEquals(gallery.buffer().toString(), "a");
    Context blog = compiler().newExecutor()
        .template("{.current-type? blog extra}a{.or}b{.end}")
        .json("{\"currentType\": 5}")
        .compat(CompatLevel.fixed())
        .execute();
    assertEquals(blog.buffer().toString(), "b");
  }

}
