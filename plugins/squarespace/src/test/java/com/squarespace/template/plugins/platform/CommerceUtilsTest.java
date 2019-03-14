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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.squarespace.template.UnitTestBase;
import com.squarespace.template.plugins.platform.enums.ScarcityCalculationType;
import com.squarespace.template.plugins.platform.enums.ScarcityFlagType;

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

  @Test
  public void testIsScarcityEnabled() {
    Map<String, JsonNode> jsonMap = loadJson(getClass(), "test-is-scarcity-enabled.json");

    JsonNode websiteSettings = jsonMap.get("empty-websiteSettings");
    assertFalse(CommerceUtils.isScarcityEnabled(websiteSettings, ScarcityFlagType.PRODUCT_BLOCK));
    assertFalse(CommerceUtils.isScarcityEnabled(websiteSettings, ScarcityFlagType.PRODUCT_ITEMS));

    websiteSettings = jsonMap.get("all-disabled");
    assertFalse(CommerceUtils.isScarcityEnabled(websiteSettings, ScarcityFlagType.PRODUCT_BLOCK));
    assertFalse(CommerceUtils.isScarcityEnabled(websiteSettings, ScarcityFlagType.PRODUCT_ITEMS));

    websiteSettings = jsonMap.get("product-items-enabled");
    assertFalse(CommerceUtils.isScarcityEnabled(websiteSettings, ScarcityFlagType.PRODUCT_BLOCK));
    assertTrue(CommerceUtils.isScarcityEnabled(websiteSettings, ScarcityFlagType.PRODUCT_ITEMS));

    websiteSettings = jsonMap.get("product-blocks-enabled");
    assertTrue(CommerceUtils.isScarcityEnabled(websiteSettings, ScarcityFlagType.PRODUCT_BLOCK));
    assertFalse(CommerceUtils.isScarcityEnabled(websiteSettings, ScarcityFlagType.PRODUCT_ITEMS));

    websiteSettings = jsonMap.get("all-enabled");
    assertTrue(CommerceUtils.isScarcityEnabled(websiteSettings, ScarcityFlagType.PRODUCT_BLOCK));
    assertTrue(CommerceUtils.isScarcityEnabled(websiteSettings, ScarcityFlagType.PRODUCT_ITEMS));
  }

  @Test
  public void testCanDisplayScarcityMessage() {
    Map<String, JsonNode> jsonMap = loadJson(getClass(), "test-can-display-scarcity-message.json");
    for (String testKey : extractTests(jsonMap)) {
      JsonNode node = jsonMap.get(testKey);
      boolean result = CommerceUtils.canDisplayScarcityMessages(node.get("websiteSettings"), node.get("item"));
      if (testKey.endsWith("-true")) {
        assertTrue(result);
      } else {
        assertFalse(result);
      }
    }
  }

  @Test
  public void testGetScarceVariants() {
    Map<String, JsonNode> jsonMap = loadJson(getClass(), "test-get-scarce-variants.json");

    JsonNode testContext = jsonMap.get("total-stock-with-unlimited-variant");
    ArrayNode actual = CommerceUtils.getScarceVariants(testContext.get("websiteSettings"), testContext.get("item"),
        ScarcityCalculationType.TOTAL_STOCK);
    JsonNode expected = jsonMap.get("total-stock-with-unlimited-variant-expected");
    JsonAssert.assertJsonEquals(actual.toString(), expected.toString());

    testContext = jsonMap.get("per-variant-with-unlimited-variant");
    actual = CommerceUtils.getScarceVariants(testContext.get("websiteSettings"), testContext.get("item"),
        ScarcityCalculationType.PER_VARIANT);
    expected = jsonMap.get("per-variant-with-unlimited-variant-expected");
    JsonAssert.assertJsonEquals(actual.toString(), expected.toString());

    testContext = jsonMap.get("per-variant-with-over-threshold-variants");
    actual = CommerceUtils.getScarceVariants(testContext.get("websiteSettings"), testContext.get("item"),
        ScarcityCalculationType.PER_VARIANT);
    expected = jsonMap.get("per-variant-with-over-threshold-variants-expected");
    JsonAssert.assertJsonEquals(actual.toString(), expected.toString());
  }

}
