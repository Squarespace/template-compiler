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

import static com.squarespace.template.plugins.platform.CommerceUtils.hasVariants;

import static com.squarespace.template.JsonLoader.loadJson;
import static com.squarespace.template.plugins.platform.CommerceUtils.getTotalStockRemaining;
import static com.squarespace.template.plugins.platform.CommerceUtils.hasVariedPrices;
import static com.squarespace.template.plugins.platform.CommerceUtils.isMultipleQuantityAllowedForServices;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.template.UnitTestBase;

import net.javacrumbs.jsonunit.JsonAssert;


public class CommerceUtilsTest extends UnitTestBase {

  @Test
  public void testGetItemVariantOptions() {
    Map<String, JsonNode> jsonMap = loadJson(getClass(), "get-item-variant-options.json");
    for (String testKey : extractTests(jsonMap)) {
      JsonNode item = jsonMap.get(testKey);
      JsonNode actual = CommerceUtils.getItemVariantOptions(item);
      JsonNode expected = jsonMap.get(testKey + "-expected");
      JsonAssert.assertJsonEquals(actual.toString(), expected.toString());
    }
  }

  @Test
  public void testGetTotalStockRemaining() {
    Map<String, JsonNode> jsonMap = loadJson(getClass(), "get-total-stock-remaining.json");
    JsonNode item = jsonMap.get("getTotalStock-unlimited-physical");
    assertEquals(getTotalStockRemaining(item), Double.POSITIVE_INFINITY);

    item = jsonMap.get("getTotalStock-digital");
    assertEquals(getTotalStockRemaining(item), Double.POSITIVE_INFINITY);

    item = jsonMap.get("getTotalStock-six-service");
    assertEquals(getTotalStockRemaining(item), 6.0);

    item = jsonMap.get("getTotalStock-0-physical");
    assertEquals(getTotalStockRemaining(item), 0.0);

    item = jsonMap.get("getTotalStock-0-physical-2");
    assertEquals(getTotalStockRemaining(item), 0.0);

    item = jsonMap.get("getTotalStock-unknown");
    assertEquals(getTotalStockRemaining(item), 0.0);

    // Sum > Integer.MAX_VALUE must not wrap negative (accumulate in long).
    item = jsonMap.get("getTotalStock-overflow");
    assertEquals(getTotalStockRemaining(item), 4294967296.0);

    // Legacy, the int accumulator wraps 2^32 back to 0.
    assertEquals(getTotalStockRemaining(item, true), 0.0);
  }

  @Test
  public void testHasVariants() {
    Map<String, JsonNode> jsonMap = loadJson(getClass(), "has-variants.json");
    for (Map.Entry<String, JsonNode> entry : jsonMap.entrySet()) {
      String key = entry.getKey();
      if (key.endsWith("-true")) {
        assertTrue(hasVariants(entry.getValue()), key);
      } else {
        assertFalse(hasVariants(entry.getValue()), key);
      }
    }
  }

  @Test
  public void testHasVariedPrices() {
    Map<String, JsonNode> jsonMap = loadJson(getClass(), "has-varied-prices.json");
    for (Map.Entry<String, JsonNode> entry : jsonMap.entrySet()) {
      String key = entry.getKey();
      if (key.endsWith("-true")) {
        assertTrue(hasVariedPrices(entry.getValue()), key);
      } else {
        assertFalse(hasVariedPrices(entry.getValue()), key);
      }
    }
  }

  @Test
  public void testHasVariedPricesNonArrayVariants() {
    JsonNode item = json("{\"productType\":1,\"structuredContent\":{\"productType\":1,\"variants\":{\"a\":1,\"b\":2}}}");

    // Legacy, the released signature throws on an object variants node
    // with two or more fields.
    try {
      hasVariedPrices(item);
      fail("expected NullPointerException");
    } catch (NullPointerException e) {
      // Expected
    }

    // Fixed, a non-array variants node is treated like missing or empty.
    assertFalse(hasVariedPrices(item, false));

    // Fixed, well-formed arrays keep the released verdicts.
    JsonNode varied = json("{\"structuredContent\":{\"productType\":1,\"variants\":[{\"price\":100},{\"price\":200}]}}");
    assertTrue(hasVariedPrices(varied, false));
    JsonNode same = json("{\"structuredContent\":{\"productType\":1,\"variants\":[{\"price\":100},{\"price\":100}]}}");
    assertFalse(hasVariedPrices(same, false));
  }

  @Test
  public void testIsMultipleQuantityAllowedForServices() {
    Map<String, JsonNode> jsonMap = loadJson(getClass(), "is-multi-quantity-allowed-for-services.json");
    for (Map.Entry<String, JsonNode> entry : jsonMap.entrySet()) {
      String key = entry.getKey();
      assertEquals(isMultipleQuantityAllowedForServices(entry.getValue()), key.endsWith("-true"), key);
    }
  }

  private static List<String> extractTests(Map<String, JsonNode> jsonMap) {
    List<String> tests = new ArrayList<>();
    for (String key : jsonMap.keySet()) {
      if (!key.endsWith("-expected")) {
        tests.add(key);
      }
    }
    return tests;
  }

}
