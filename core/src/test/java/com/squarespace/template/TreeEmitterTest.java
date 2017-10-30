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

package com.squarespace.template;

import java.util.Map;

import org.testng.annotations.Test;


public class TreeEmitterTest extends UnitTestBase {

  @Test
  public void testTreeEmitter() throws CodeException {
    String raw = GeneralUtils.loadResource(TreeEmitterTest.class, "tree-emitter.txt");
    Map<String, String> sections = TestCaseParser.parseSections(raw);

    String input = sections.get("INPUT").trim();
    String expected = sections.get("EXPECTED").trim();
    Instruction inst = compiler().compile(input, false, false).code();
    String actual = TreeEmitter.get(inst).trim();
    if (!actual.equals(expected)) {
      throw new AssertionError("Output does not match:\n" + TestCaseParser.diff(expected, actual));
    }
  }

}
