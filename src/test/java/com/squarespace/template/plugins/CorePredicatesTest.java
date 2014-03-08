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

import static com.squarespace.template.Constants.EMPTY_ARGUMENTS;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.squarespace.template.Arguments;
import com.squarespace.template.CodeException;
import com.squarespace.template.Constants;
import com.squarespace.template.Context;
import com.squarespace.template.Predicate;
import com.squarespace.template.UnitTestBase;


public class CorePredicatesTest extends UnitTestBase {

  @Test
  public void testDebug() throws CodeException {
    String[] key = new String[] { "a", "b" };
    Context ctx = context("{\"debug\": true, \"a\": {\"b\": 1}}");
    ctx.pushSection(key);
    assertTrue(CorePredicates.DEBUG, ctx);

    for (String json : new String[] {
        "{\"a\": {\"b\": 1}}",
        "{\"debug\": 0, \"a\": {\"b\": 1}}" }) {
      ctx = context(json);
      ctx.pushSection(key);
      assertFalse(CorePredicates.DEBUG, ctx);  }
    }

  @Test
  public void testPlural() throws CodeException {
    assertFalse(CorePredicates.PLURAL, context("0"));
    assertFalse(CorePredicates.PLURAL, context("0.10"));
    assertFalse(CorePredicates.PLURAL, context("1"));
    assertFalse(CorePredicates.PLURAL, context("1.1"));
    assertFalse(CorePredicates.PLURAL, context("1.99"));
    // Integer part must be 2 or greater
    assertTrue(CorePredicates.PLURAL, context("2"));
    assertTrue(CorePredicates.PLURAL, context("3.14159"));
    assertTrue(CorePredicates.PLURAL, context("100000.101"));
  }

  @Test
  public void testSingular() throws CodeException {
    assertFalse(CorePredicates.SINGULAR, context("0"));
    assertFalse(CorePredicates.SINGULAR, context("0.9"));
    // Integer part must be == 1
    assertTrue(CorePredicates.SINGULAR, context("1"));
    assertTrue(CorePredicates.SINGULAR, context("1.1"));
  }

  private void assertTrue(Predicate predicate, Context ctx) throws CodeException {
    assertTrue(predicate, ctx, Constants.EMPTY_ARGUMENTS);
  }

  private void assertTrue(Predicate predicate, Context ctx, Arguments args) throws CodeException {
    Assert.assertTrue(predicate.apply(ctx, args));
  }

  private void assertFalse(Predicate predicate, Context ctx) throws CodeException {
    assertFalse(predicate, ctx, EMPTY_ARGUMENTS);
  }

  private void assertFalse(Predicate predicate, Context ctx, Arguments args) throws CodeException {
    Assert.assertFalse(predicate.apply(ctx, args));
  }
}
