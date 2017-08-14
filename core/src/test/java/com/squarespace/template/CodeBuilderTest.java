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

import org.testng.annotations.Test;


public class CodeBuilderTest extends UnitTestBase {


  @Test
  public void testComment() throws CodeException {
    String actual = "    one two three    ";
    Instruction expected = parse("{# one two three }");

    assertEquals(builder().comment(actual, 3, actual.length() - 3).eof().build(), expected);
    assertEquals(builder().comment(new StringView(actual, 3, actual.length() - 3)).eof().build(), expected);
  }

  @Test
  public void testMultiLineComment() throws CodeException {
    String actual = "   \n one\n two\n three\n   ";
    Instruction expected = parse("{##\n one\n two\n three\n##}");

    assertEquals(builder().mcomment(actual, 3, actual.length() - 3).eof().build(), expected);
    assertEquals(builder().mcomment(new StringView(actual, 3, actual.length() - 3)).eof().build(), expected);
  }

  @Test
  public void testInject() throws CodeException {
    CodeMaker mk = new CodeMaker();
    Instruction expected = parse("{.inject @foo ./bar.json}");

    assertEquals(builder().inject("@foo", "./bar.json").eof().build(), expected);

    expected = parse("{.inject @foo ./bar.json arg1}");
    assertEquals(builder().inject("@foo", "./bar.json", mk.args(" arg1")).eof().build(), expected);
  }

  @Test
  public void testText() throws CodeException {
    Instruction expected = parse("foo{foo}bar");

    assertEquals(builder().text("foo").var("foo").text(new StringView("bar")).eof().build(), expected);
  }

  private Instruction parse(String template) throws CodeException {
    return compiler().compile(template).code();
  }

}
