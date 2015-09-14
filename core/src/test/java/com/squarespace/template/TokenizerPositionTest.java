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

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;


@Test(groups = { "unit" })
public class TokenizerPositionTest extends UnitTestBase {

  private static final boolean VERBOSE = false;

  /**
   * Sanity-checking all the line and column offsets. All line and character offsets
   * are 1-based to match the positions a text editor would report.
   */
  @Test
  public void testPositions() throws CodeSyntaxException {
    CodeMaker mk = maker();

    // VARIABLE, EOF
    String str = "{@}";
    assertLines(parse(str), mk.intlist(1, 1));
    assertOffsets(parse(str), mk.intlist(1, 4));

    // TEXT, VARIABLE, EOF
    str = "\n\n\n{@}";
    assertLines(parse(str), mk.intlist(1, 4, 4));
    assertOffsets(parse(str), mk.intlist(1, 1, 4));

    // TEXT, VARIABLE, TEXT, EOF
    str = "foo\n{@}\nbar";
    assertLines(parse(str), mk.intlist(1, 2, 2, 3));
    assertOffsets(parse(str), mk.intlist(1, 1, 4, 4));

    // TEXT, VARIABLE, TEXT, VARIABLE, TEXT, END, TEXT, EOF
    str = "\n{@}A\n\n{@}B\n\n{.end}C\n";
    assertLines(parse(str), mk.intlist(1, 2, 2, 4, 4, 6, 6, 7));
    assertOffsets(parse(str), mk.intlist(1, 1, 4, 1, 4, 1, 7, 1));

    // TEXT, VARIABLE, TEXT, VARIABLE, EOF
    str = "  {@}\n{@}";
    assertLines(parse(str), mk.intlist(1, 1, 1, 2, 2));
    assertOffsets(parse(str), mk.intlist(1, 3, 6, 1, 4));

    // TEXT, VARIABLE, TEXT, EOF
    str = "1{@}5";
    assertLines(parse(str), mk.intlist(1, 1, 1, 1));
    assertOffsets(parse(str), mk.intlist(1, 2, 5, 6));

    // TEXT, VARIABLE, TEXT, EOF
    str = "123{@}789";
    assertLines(parse(str), mk.intlist(1, 1, 1, 1));
    assertOffsets(parse(str), mk.intlist(1, 4, 7, 10));

    // TEXT, VARIABLE, TEXT, EOF
    str = "\n {@} \n";
    assertLines(parse(str), mk.intlist(1, 2, 2, 3));
    assertOffsets(parse(str), mk.intlist(1, 2, 5, 1));

    // TEXT, VARIABLE, TEXT, VARIABLE, TEXT, EOF
    str = "\n{@}\n{@}\n";
    assertLines(parse(str), mk.intlist(1, 2, 2, 3, 3, 4));
    assertOffsets(parse(str), mk.intlist(1, 1, 4, 1, 4, 1));

    // SECTION, VARIABLE, END, TEXT, REPEATED, VARIABLE, END, EOF
    str = "{.section a}{b}{.end}\n{.repeated section b}{@}{.end}";
    assertLines(parse(str), mk.intlist(1, 1, 1, 1, 2, 2, 2, 2));
    assertOffsets(parse(str), mk.intlist(1, 13, 16, 22, 1, 22, 25, 31));

    // TEXT SECTION END EOF
    str = " {.section a}{.end}";
    assertLines(parse(str), mk.intlist(1, 1, 1, 1));
    assertOffsets(parse(str), mk.intlist(1, 2, 14, 20));
  }

  @Test
  public void testMultiLineComments() throws CodeSyntaxException {
    CodeMaker mk = maker();

    // TEXT, COMMENT, VARIABLE, EOF
    String str = "\n\n{##\n\n##}{@}";
    assertLines(parse(str), mk.intlist(1, 3, 5, 5));
    assertOffsets(parse(str), mk.intlist(1, 1, 4, 7));
  }

  private List<Instruction> parse(String raw) throws CodeSyntaxException {
    CodeList collector = collector();
    tokenizer(raw, collector).consume();
    return collector.getInstructions();
  }

  private void assertLines(List<Instruction> instructions, List<Integer> expected) {
    List<Integer> actual = new ArrayList<>();
    for (int i = 0; i < instructions.size(); i++) {
      actual.add(instructions.get(i).getLineNumber());
    }
    if (VERBOSE) {
      System.out.println("assertLines: " + instructions);
      System.out.println("assertLines: " + actual);
      System.out.println("--------------------------");
    }
    assertEquals(actual.size(), expected.size());
    assertEquals(actual, expected);
  }

  private void assertOffsets(List<Instruction> instructions, List<Integer> expected) {
    List<Integer> actual = new ArrayList<>();
    for (int i = 0; i < instructions.size(); i++) {
      actual.add(instructions.get(i).getCharOffset());
    }
    if (VERBOSE) {
      System.out.println("assertOffsets: " + instructions);
      System.out.println("assertOffsets: " + actual);
      System.out.println("--------------------------");
    }
    assertEquals(actual.size(), expected.size());
    assertEquals(actual, expected);
  }

}
