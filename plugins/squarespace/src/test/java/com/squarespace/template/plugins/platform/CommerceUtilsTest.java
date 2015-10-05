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

import static com.squarespace.template.JsonLoader.loadJson;

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
    Map<String, JsonNode> jsonMap = loadJson(CommerceUtilsTest.class, "commerce-utils-item-variant.json");
    for (String testKey : extractTests(jsonMap)) {
      JsonNode item = jsonMap.get(testKey);
      JsonNode actual = CommerceUtils.getItemVariantOptions(item);
      JsonNode expected = jsonMap.get(testKey + "-expected");
      JsonAssert.assertJsonEquals(actual.toString(), expected.toString());
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
