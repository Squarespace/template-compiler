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

import java.math.BigDecimal;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.node.DecimalNode;
import com.squarespace.template.Instructions.RootInst;
import com.squarespace.template.plugins.CorePredicates;


/**
 * Executing pieces of code and verifying output.
 */
@Test(groups = { "unit" })
public class CodeExecuteTest extends UnitTestBase {

  private static final String ALPHAS = "abcdefghijklmnopqrstuvwxyz";

  @Test
  public void testBindVar() throws CodeException {
    RootInst root = builder().bindvar("@name", "foo").var("@name").eof().build();
    assertEquals(repr(root), "{.var @name foo}{@name}");
    assertContext(execute("{\"foo\": 123}", root), "123");

    // Bind variable to element inside array
    root = builder().bindvar("@name", "foo.1").var("@name").eof().build();
    assertEquals(repr(root), "{.var @name foo.1}{@name}");
    assertContext(execute("{\"foo\": [1,2,3]}", root), "2");

    // Resolve variable in outer scope
    root = builder().bindvar("@val", "foo").section("bar").var("@val").end().eof().build();
    assertEquals(repr(root), "{.var @val foo}{.section bar}{@val}{.end}");
    assertContext(execute("{\"foo\": 1, \"bar\": 2}", root), "1");

    // Full example with nesting
    String json = "{\"managers\":["
        + "{\"name\": \"Bill\", \"employees\": [{\"name\": \"Peter\"}, {\"name\": \"Michael\"}]},"
        + "{\"name\": \"Bob\", \"employees\": [{\"name\": \"Samir\"}]}"
        + "]}";
    CodeBuilder cb = builder();
    cb.repeated("managers").bindvar("@boss", "name").bindvar("@boss-idx", "@index");
    cb.repeated("employees").var("@boss-idx").text(".").var("@index").text(" ");
    cb.var("name").text(" is managed by ").var("@boss").text("\n").end();
    root = cb.alternatesWith().text("---\n").end().eof().build();
    assertContext(execute(json, root),
        "1.1 Peter is managed by Bill\n"
        + "1.2 Michael is managed by Bill\n"
        + "---\n"
        + "2.1 Samir is managed by Bob\n");

    // Example with dotted access on @var
    json = "{\"person\": {\"name\": \"Larry\", \"age\": \"21\"}}";
    root = builder().bindvar("@person", "person").var("@person.name").text(" is ").var("@person.age").eof().build();
    assertContext(execute(json, root), "Larry is 21");

    // Examples of binding with brackets
    json = "{\"strings\": {\"en\": [\"Hello\", \"world\"], \"fr\": [\"Bonjour\", \"le\", \"monde\"]}, "
        + "\"languages\": [\"en\", \"fr\"]}";
    cb = builder().bindvar("@english", "strings[languages.0]");
    cb.repeated("@english").var("@").alternatesWith().text(" ").end().eof();
    assertContext(execute(json, cb.build()), "Hello world");

    cb = builder().bindvar("@strings", "strings");
    cb.repeated("languages").bindvar("@string", "@strings[@]");
    cb.repeated("@string").var("@").alternatesWith().text(" ").end();
    cb.alternatesWith().text("\n").end().eof();
    assertContext(execute(json, cb.build()),
        "Hello world\n"
        + "Bonjour le monde");
  }

  @Test
  public void testLiterals() throws CodeException {
    RootInst root = builder().metaLeft().space().tab().newline().metaRight().eof().build();
    assertContext(execute("{}", root), "{ \t\n}");
  }

  @Test
  public void testPredicates() throws CodeException {
    CodeBuilder builder = builder();
    builder.predicate(CorePredicates.PLURAL).text("A");
    builder.or(CorePredicates.SINGULAR).text("B");
    builder.or().text("C").end();

    Instruction root = builder.eof().build();
    assertEquals(repr(root), "{.plural?}A{.or singular?}B{.or}C{.end}");
    assertContext(execute("5", root), "A");
    assertContext(execute("1", root), "B");
    assertContext(execute("0", root), "C");
    assertContext(execute("-3.1415926", root), "C");
  }

  @Test
  public void testSection() throws CodeException {
    RootInst root = builder().section("foo.bar").var("baz").end().eof().build();

    String json = "{\"foo\": {\"bar\": {\"baz\": 123}}}";
    assertEquals(repr(root), "{.section foo.bar}{baz}{.end}");
    assertContext(execute(json, root), "123");

    root = builder().section("foo.1").var("@").end().eof().build();
    json = "{\"foo\": [\"a\", \"b\"]}";
    assertEquals(repr(root), "{.section foo.1}{@}{.end}");
    assertContext(execute(json, root), "b");

    root = builder().section("foo[bar]").var("@").end().eof().build();
    json = "{\"foo\": [\"a\", \"b\"], \"bar\": 1}";
    assertEquals(repr(root), "{.section foo[bar]}{@}{.end}");
    assertContext(execute(json, root), "b");
  }

  @Test
  public void testSectionMissing() throws CodeException {
    CodeBuilder builder = builder();
    builder.section("foo").text("A").or().text("B").end();
    RootInst root = builder.eof().build();
    assertContext(execute("{\"foo\": 123}", root), "A");
    assertContext(execute("{}", root), "B");
    assertContext(execute("{\"foo\": null}", root), "B");
    assertContext(execute("{\"foo\": []}", root), "B");
  }

  @Test
  public void testText() throws CodeException {
    String expected = "defjkl";
    RootInst root = builder().text(ALPHAS, 3, 6).text(ALPHAS, 9, 12).eof().build();
    assertContext(execute("{}", root), expected);
  }

  @Test
  public void testRepeat() throws CodeException {
    String expected = "Hi, Joe! Hi, Bob! ";
    RootInst root = builder().repeated("@").text("Hi, ").var("foo").text("! ").end().eof().build();
    assertContext(execute("[{\"foo\": \"Joe\"},{\"foo\": \"Bob\"}]", root), expected);
  }

  @Test
  public void testRepeatOr() throws CodeException {
    RootInst root = builder().repeated("foo").text("A").var("@").or().text("B").end().eof().build();
    assertEquals(repr(root), "{.repeated section foo}A{@}{.or}B{.end}");
    assertContext(execute("{\"foo\": [1, 2, 3]}", root), "A1A2A3");
    assertContext(execute("{}", root), "B");
  }

  @Test
  public void testVariable() throws CodeException {
    RootInst root = builder().var("foo.bar").eof().build();
    assertContext(execute("{\"foo\": {\"bar\": 123}}", root), "123");

    root = builder().var("@").eof().build();
    assertContext(execute("3.14159", root), "3.14159");
    assertContext(execute("123.000", root), "123.0");
    assertContext(execute("null", root), "");

    root = builder().var("foo.2.bar").eof().build();
    assertContext(execute("{\"foo\": [0, 0, {\"bar\": \"hi\"}]}", root), "hi");

    root = builder().var("foo[2].bar").eof().build();
    assertContext(execute("{\"foo\": [0, 0, {\"bar\": \"hi\"}]}", root), "hi");

    root = builder().var("foo[baz].bar").eof().build();
    assertContext(execute("{\"foo\": [0, 0, {\"bar\": \"hi\"}], \"baz\": 2}", root), "hi");

    root = builder().var("foo[2][baz]").eof().build();
    assertContext(execute("{\"foo\": [0, 0, {\"bar\": \"hi\"}], \"baz\": \"bar\"}", root), "hi");

    root = builder().var("foo").eof().build();
    assertContext(execute("{\"foo\": [\"a\", \"b\", 123]}", root), "a,b,123");
  }

  @Test
  public void testVariableScope() throws CodeException {
    RootInst root = builder().repeated("names")
        .bindvar("@curr", "name").alternatesWith().var("@curr").end().eof().build();
    String json = "{\"names\": [{\"name\": \"bob\"}, {\"name\": \"larry\"}]}";
    assertContext(execute(json, root), "bob");
  }

  @Test
  public void testVariableTypes() throws CodeException {
    RootInst root = builder().var("@").eof().build();
    String value = "12345678900000000.1234567890000000";
    DecimalNode node = new DecimalNode(new BigDecimal(value));
    assertContext(execute(node, root), value);

    value = "123.0";
    node = new DecimalNode(new BigDecimal(value));
    assertContext(execute(node, root), value);
  }

}
