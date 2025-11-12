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

import static com.squarespace.template.ExecuteErrorType.UNEXPECTED_ERROR;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.squarespace.template.CodeException;
import com.squarespace.template.Context;
import com.squarespace.template.TestSuiteRunner;
import com.squarespace.template.compat.CompatLevel;


public class CommercePredicatesTest extends PlatformUnitTestBase {

  private final TestSuiteRunner runner = new TestSuiteRunner(compiler(), CommercePredicatesTest.class);

  @Test
  public void testHasVariants() {
    runner.run("p-has-variants.html");
  }

  @Test
  public void testOnSale() {
    runner.run("p-on-sale.html");
  }

  @Test
  public void testSoldOut() {
    runner.run("p-sold-out.html");
  }

  @Test
  public void testVariedPrices() throws CodeException {
    runner.run(
        "p-varied-prices.html",
        "p-varied-prices-2.html",
        "p-varied-prices-3.html",
        "p-varied-prices-4.html"
        );

    String malformed = "{\"productType\":1,\"structuredContent\":{\"productType\":1,\"variants\":{\"a\":1,\"b\":2}}}";

    // Legacy, an object variants node with two or more fields throws at
    // the default level and safe mode collects the throw.
    Context legacy = compiler().newExecutor()
        .template("{.varied-prices?}y{.or}n{.end}")
        .json(malformed)
        .safeExecution(true)
        .execute();
    assertContext(legacy, "");
    assertEquals(legacy.getErrors().size(), 1);
    assertEquals(legacy.getErrors().get(0).getType(), UNEXPECTED_ERROR);
    assertTrue(legacy.getErrors().get(0).getMessage().contains("NullPointerException"));

    // Fixed, the same input renders the false branch without error.
    Context fixed = compiler().newExecutor()
        .template("{.varied-prices?}y{.or}n{.end}")
        .json(malformed)
        .safeExecution(true)
        .compat(CompatLevel.fixed())
        .execute();
    assertContext(fixed, "n");
    assertEquals(fixed.getErrors().size(), 0);
  }

}
