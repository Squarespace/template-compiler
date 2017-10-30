/**
 * Copyright (c) 2017 SQUARESPACE, Inc.
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

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;


public class TokenizerPreprocessTest extends UnitTestBase {

  private static final boolean VERBOSE = false;

  @Test
  public void testBasic() throws CodeException {
    CodeMaker mk = maker();
    assertResult("{^.section foo}{.section bar}{^foo}{bar}{^.end}{.end}",
        mk.pre(mk.section("foo")),
          mk.text("{.section bar}"),
        mk.pre(mk.var("foo")),
          mk.text("{bar}"),
        mk.pre(mk.end()),
          mk.text("{.end}"),
        mk.eof());
  }

  private void assertResult(String raw, Instruction... instructions) throws CodeSyntaxException {
    CodeList collector = collector();
    tokenizer(raw, collector, true).consume();
    List<Instruction> expected = Arrays.asList(instructions);
    if (VERBOSE) {
      System.out.println("assertResult: " + collector.getInstructions());
    }
    assertEquals(collector.getInstructions(), expected);
  }

}
