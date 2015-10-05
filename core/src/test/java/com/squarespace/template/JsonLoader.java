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

package com.squarespace.template;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;


public class JsonLoader {

  public static Map<String, JsonNode> loadJson(Class<?> relativeCls, String path) {
    try {
      String source = GeneralUtils.loadResource(relativeCls, path);
      Map<String, String> sections = TestCaseParser.parseSections(source);
      Map<String, JsonNode> result = new HashMap<>();
      for (Map.Entry<String, String> entry : sections.entrySet()) {
        result.put(entry.getKey(), JsonUtils.decode(entry.getValue()));
      }
      return result;
    } catch (CodeException e) {
      throw new AssertionError("Failed to load JSON from resource '" + path + "'", e);
    }
  }

}
