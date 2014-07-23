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

import static org.testng.Assert.assertTrue;

import java.util.regex.Pattern;

import org.testng.annotations.Test;


public class TreeEmitterTest extends UnitTestBase {

  private static final Pattern SPACES = Pattern.compile("\\s+");

  @Test
  public void testTreeEmitter() throws CodeException {
    String raw = "{.repeated section a}a{.or even? b}b{.end}";
    Instruction inst = compiler().compile(raw).code();
    String result = SPACES.matcher(TreeEmitter.get(inst)).replaceAll(" ");
    assertTrue(result.contains("REPEATED true: TEXT alternates: null false: OR_PREDICATE"));
  }

}
