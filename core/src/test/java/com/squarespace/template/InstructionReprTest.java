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

import static com.squarespace.template.Operator.LOGICAL_AND;
import static com.squarespace.template.Operator.LOGICAL_OR;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.template.Instructions.AlternatesWithInst;
import com.squarespace.template.Instructions.BindVarInst;
import com.squarespace.template.Instructions.CommentInst;
import com.squarespace.template.Instructions.IfInst;
import com.squarespace.template.Instructions.IfPredicateInst;
import com.squarespace.template.Instructions.InjectInst;
import com.squarespace.template.Instructions.MacroInst;
import com.squarespace.template.Instructions.PredicateInst;
import com.squarespace.template.Instructions.RepeatedInst;
import com.squarespace.template.Instructions.RootInst;
import com.squarespace.template.Instructions.SectionInst;
import com.squarespace.template.Instructions.TextInst;
import com.squarespace.template.Instructions.VariableInst;
import com.squarespace.template.plugins.CoreFormatters.JsonFormatter;
import com.squarespace.template.plugins.CoreFormatters.PluralizeFormatter;
import com.squarespace.template.plugins.CoreFormatters.TruncateFormatter;
import com.squarespace.template.plugins.CorePredicates;


/**
 * Validates the external representations of instructions.
 */
@Test(groups = { "unit" })
public class InstructionReprTest extends UnitTestBase {

  private static final Formatter JSON = new JsonFormatter();

  private static final Formatter PLURALIZE = new PluralizeFormatter();

  private static final Formatter TRUNCATE = new TruncateFormatter();

  @Test
  public void testAlternatesWithRepr() {
    CodeMaker mk = maker();
    AlternatesWithInst a1 = mk.alternates();
    assertEquals(a1.repr(), "{.alternates with}");
    assertNotEquals(a1.repr(), "{.alternatesWith}");
    assertNotEquals(a1.repr(), "{.alternateswith}");

    a1.getConsequent().add(mk.text("---"));
    assertEquals(a1.repr(), "{.alternates with}---");

    assertEquals(ReprEmitter.get(a1, false), "{.alternates with}");

    a1.setPreprocessScope();
    assertEquals(ReprEmitter.get(a1, false), "{^.alternates with}");
  }

  @Test
  public void testBindVarRepr() {
    CodeMaker mk = maker();
    BindVarInst s1 = mk.bindvar("@foo", "bar.baz");
    assertEquals(s1.repr(), "{.var @foo bar.baz}");

    s1 = mk.bindvar("@name", "names.0");
    assertEquals(s1.repr(), "{.var @name names.0}");
  }

  @Test
  public void testCommentRepr() {
    CommentInst c1 = maker().comment("foo");
    assertEquals(c1.repr(), "{#foo}");

    c1 = maker().mcomment("foo");
    assertEquals(c1.repr(), "{##foo##}");

    c1 = maker().mcomment("\nfoo\nbar\nbaz\n");
    assertEquals(c1.repr(), "{##\nfoo\nbar\nbaz\n##}");
  }

  @Test
  public void testIfRepr() {
    CodeMaker mk = maker();
    IfInst i1 = mk.ifexpn(mk.strlist("a"), null);
    assertEquals(i1.repr(), "{.if a}");
    IfInst i2 = mk.ifexpn(mk.strlist("a", "b"), mk.oplist(LOGICAL_OR));
    assertEquals(i2.repr(), "{.if a || b}");
    IfInst i3 = mk.ifexpn(mk.strlist("a", "b", "c"), mk.oplist(LOGICAL_AND, LOGICAL_AND));
    assertEquals(i3.repr(), "{.if a && b && c}");
    IfInst i4 = mk.ifexpn(mk.strlist("a.b", "c.d", "e.f"), mk.oplist(LOGICAL_OR, LOGICAL_AND));
    assertEquals(i4.repr(), "{.if a.b || c.d && e.f}");
  }

  @Test
  public void testIfPredicate() throws ArgumentsException {
    CodeMaker mk = maker();
    IfPredicateInst i1 = mk.ifpred(CorePredicates.PLURAL);
    assertEquals(i1.repr(), "{.if plural?}");
    i1 = mk.ifpred(UnitTestPredicates.REQUIRED_ARGS, mk.args(" 1 2 3"));
    assertEquals(i1.repr(), "{.if required-args? 1 2 3}");
    i1 = new IfPredicateInst(UnitTestPredicates.INVALID_ARGS, new Arguments(mk.view("/abc/def/ghi")));
    assertEquals(i1.repr(), "{.if invalid-args?/abc/def/ghi}");
  }

  @Test
  public void testInjectRepr() {
    CodeMaker mk = maker();
    InjectInst i1 = mk.inject("@foo", "foo.json");
    assertEquals(i1.repr(), "{.inject @foo foo.json}");
  }

  @Test
  public void testLiteralRepr() {
    CodeMaker mk = maker();
    assertEquals(mk.space().repr(), "{.space}");
    assertEquals(mk.tab().repr(), "{.tab}");
    assertEquals(mk.newline().repr(), "{.newline}");
  }

  @Test
  public void testMacroRepr() {
    CodeMaker mk = maker();
    MacroInst m1 = mk.macro("foo.1.bar.2");
    assertEquals(m1.repr(), "{.macro foo.1.bar.2}");

    MacroInst m2 = mk.macro("foo");
    m2.getConsequent().add(mk.text("text"));
    m2.getConsequent().add(mk.var("@bar"));
    assertEquals(m2.repr(), "{.macro foo}text{@bar}");
  }

  @Test
  public void testMetaRepr() {
    CodeMaker mk = maker();
    assertEquals(mk.metaLeft().repr(), "{.meta-left}");
    assertEquals(mk.metaRight().repr(), "{.meta-right}");
  }

  @Test
  public void testNames() {
    assertEquals(ReprEmitter.get(new String[] { "foo", "bar" }), "foo.bar");
    assertEquals(ReprEmitter.get((Object[])null), "@");
  }

  @Test
  public void testPredicateRepr() {
    CodeMaker mk = maker();
    PredicateInst p1 = mk.predicate(CorePredicates.PLURAL);
    assertEquals(p1.repr(), "{.plural?}");
    p1.getConsequent().add(mk.text("A"));
    assertEquals(p1.repr(), "{.plural?}A");
    p1.setAlternative(mk.end());
    assertEquals(p1.repr(), "{.plural?}A{.end}");

    PredicateInst p2 = mk.or();
    assertEquals(p2.repr(), "{.or}");
    p2.getConsequent().add(mk.text("B"));
    assertEquals(p2.repr(), "{.or}B");
    p2.setAlternative(mk.end());
    assertEquals(p2.repr(), "{.or}B{.end}");

    PredicateInst p3 = mk.or(CorePredicates.SINGULAR);
    assertEquals(p3.repr(), "{.or singular?}");
    p3.getConsequent().add(mk.text("C"));
    assertEquals(p3.repr(), "{.or singular?}C");
    p3.setAlternative(mk.end());
    assertEquals(p3.repr(), "{.or singular?}C{.end}");

    assertEquals(ReprEmitter.get(p3, false), "{.or singular?}");
  }

  @Test
  public void testRepeatedRepr() {
    CodeMaker mk = maker();
    RepeatedInst r1 = mk.repeated("a.b");
    assertEquals(r1.repr(), "{.repeated section a.b}");
    r1.getConsequent().add(mk.text("A"));
    assertEquals(r1.repr(), "{.repeated section a.b}A");

    AlternatesWithInst a1 = mk.alternates();
    r1.setAlternatesWith(a1);
    assertEquals(r1.repr(), "{.repeated section a.b}A{.alternates with}");
    a1.getConsequent().add(mk.text("-"));
    assertEquals(r1.repr(), "{.repeated section a.b}A{.alternates with}-");
    r1.setAlternative(mk.end());
    assertEquals(r1.repr(), "{.repeated section a.b}A{.alternates with}-{.end}");

    assertEquals(ReprEmitter.get(r1, false), "{.repeated section a.b}");
  }

  @Test
  public void testRootRepr() {
    CodeMaker mk = maker();
    RootInst r1 = mk.root();
    assertEquals(r1.repr(), "");
    r1.getConsequent().add(mk.text("A"));
    assertEquals(r1.repr(), "A");
    r1.getConsequent().add(mk.text("B"));
    r1.getConsequent().add(mk.text("C"));
    assertEquals(r1.repr(), "ABC");

    assertEquals(ReprEmitter.get(r1, false), "");
  }

  @Test
  public void testSectionRepr() {
    CodeMaker mk = maker();
    SectionInst s1 = mk.section("@");
    assertEquals(s1.repr(), "{.section @}");
    s1 = mk.section("a.b.c");
    assertEquals(s1.repr(), "{.section a.b.c}");
    s1.getConsequent().add(mk.text("A"));
    assertEquals(s1.repr(), "{.section a.b.c}A");

    PredicateInst p1 = mk.or();
    s1.setAlternative(p1);
    assertEquals(s1.repr(), "{.section a.b.c}A{.or}");
    p1.getConsequent().add(mk.text("B"));
    assertEquals(s1.repr(), "{.section a.b.c}A{.or}B");
    p1.setAlternative(mk.end());
    assertEquals(s1.repr(), "{.section a.b.c}A{.or}B{.end}");

    assertEquals(ReprEmitter.get(s1, false), "{.section a.b.c}");
  }

  @Test
  public void testTextRepr() {
    CodeMaker mk = maker();
    TextInst t1 = mk.text("foo bar");
    assertEquals(t1.repr(), "foo bar");
  }

  @Test
  public void testVariableRepr() throws ArgumentsException {
    CodeMaker mk = maker();
    assertEquals(mk.var("@").repr(), "{@}");
    assertEquals(mk.var("@index").repr(), "{@index}");
    assertEquals(mk.var("a.b.c").repr(), "{a.b.c}");

    VariableInst v1 = mk.var("a.b", JSON);
    assertEquals(v1.repr(), "{a.b|json}");
    v1 = mk.var("@", mk.fmt(PLURALIZE, mk.args(" a1 a2")));
    assertEquals(v1.repr(), "{@|pluralize a1 a2}");

    v1 = mk.var("@", mk.formatters(mk.fmt(PLURALIZE, mk.args(" a1 a2")), mk.fmt(TRUNCATE, mk.args(" 34"))));
    assertEquals(v1.repr(), "{@|pluralize a1 a2|truncate 34}");
  }


}
