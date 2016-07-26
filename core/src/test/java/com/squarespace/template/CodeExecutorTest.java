/**
 * Copyright (c) 2016 SQUARESPACE, Inc.
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

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class CodeExecutorTest extends UnitTestBase {

  @Test
  public void testInjectInstruction() throws CodeException {
    ObjectNode map = JsonUtils.createObjectNode();
    map.put("foo.json", "{\"name\": \"albert\"}");
    map.put("bar.json", "123");
    Context ctx = compiler().newExecutor()
        .template("{.inject @foo foo.json}{.inject @bar bar.json}{@foo.name}-{@bar}")
        .json("{}")
        .injectablesMap(map)
        .execute();
    assertEquals(ctx.buffer().toString(), "albert-123");
  }

}
