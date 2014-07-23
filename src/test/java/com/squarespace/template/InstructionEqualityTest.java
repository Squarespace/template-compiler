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
import static com.squarespace.template.plugins.CorePredicates.PLURAL;
import static com.squarespace.template.plugins.CorePredicates.SINGULAR;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.template.Instructions.AlternatesWithInst;
import com.squarespace.template.Instructions.CommentInst;
import com.squarespace.template.Instructions.EndInst;
import com.squarespace.template.Instructions.EofInst;
import com.squarespace.template.Instructions.IfInst;
import com.squarespace.template.Instructions.IfPredicateInst;
import com.squarespace.template.Instructions.LiteralInst;
import com.squarespace.template.Instructions.MetaInst;
import com.squarespace.template.Instructions.PredicateInst;
import com.squarespace.template.Instructions.RepeatedInst;
import com.squarespace.template.Instructions.RootInst;
import com.squarespace.template.Instructions.SectionInst;
import com.squarespace.template.Instructions.SpaceInst;
import com.squarespace.template.Instructions.TextInst;
import com.squarespace.template.Instructions.VariableInst;
import com.squarespace.template.plugins.CoreFormatters;


@Test(groups = { "unit" })
public class InstructionEqualityTest extends UnitTestBase {

  @Test
  public void testAlternatesWithEquals() throws CodeSyntaxException {
    CodeMaker mk = maker();
    AlternatesWithInst a1 = mk.alternates();
    AlternatesWithInst a2 = mk.alternates();
    assertEquals(a1, a2);
    assertFalse(a1.equals(null));

    a1.getConsequent().add(mk.text("foo"));
    assertNotEquals(a1, a2);
    assertNotEquals(a1, mk.section("foo"));
    assertNotEquals(a1, mk.end());

    a2.getConsequent().add(mk.text("foo"));
    assertEquals(a1, a2);

    a1.setAlternative(mk.text("bar"));
    assertNotEquals(a1, a2);
    assertNotEquals(a1, mk.section("foo"));
    assertNotEquals(a1, mk.end());

    a2.setAlternative(mk.text("bar"));
    assertEquals(a1, a2);
  }

  @Test
  public void testCommentEquals() throws CodeSyntaxException {
    CodeMaker mk = maker();
    CommentInst c1 = mk.comment("foo");
    assertEquals(c1, mk.comment("foo"));

    assertNotEquals(c1, mk.comment("  foo  "));
    assertNotEquals(c1, mk.text("foo"));

    assertNotEquals(c1, mk.end());
    assertNotEquals(c1, mk.eof());
  }

  @Test
  public void testEndEquals() throws CodeSyntaxException {
    CodeMaker mk = maker();
    EndInst e1 = mk.end();
    assertEquals(e1, mk.end());

    assertNotEquals(e1, mk.comment("foo"));
    assertNotEquals(e1, mk.eof());
  }

  @Test
  public void testEofEquals() throws CodeSyntaxException {
    CodeMaker mk = maker();
    EofInst e1 = mk.eof();
    assertEquals(e1, mk.eof());

    assertNotEquals(e1, mk.space());
    assertNotEquals(e1, mk.end());
  }

  @Test
  public void testIfEquals() throws CodeSyntaxException {
    CodeMaker mk = maker();
    IfInst i1 = mk.ifexpn(mk.strlist("foo", "bar"), mk.oplist(LOGICAL_OR));
    IfInst i2 = mk.ifexpn(mk.strlist("foo", "bar"), mk.oplist(LOGICAL_OR));
    assertEquals(i1, i2);
    assertFalse(i1.equals(null));
    assertNotEquals(i1, mk.ifexpn(mk.strlist("@"), mk.oplist()));
    assertNotEquals(i1, mk.ifexpn(mk.strlist("bar", "foo"), mk.oplist(LOGICAL_OR)));
    assertNotEquals(i1, mk.ifexpn(mk.strlist("foo", "bar"), mk.oplist(LOGICAL_AND)));

    testBlockEquals(i1, i2);
  }

  @Test
  public void testIfPredicateEquals() throws CodeSyntaxException, ArgumentsException {
    CodeMaker mk = maker();
    IfPredicateInst i1 = mk.ifpred(PLURAL);
    IfPredicateInst i2 = mk.ifpred(PLURAL);
    assertEquals(i1, i2);
    IfPredicateInst i3 = mk.ifpred(SINGULAR);
    assertFalse(i1.equals(null));
    assertNotEquals(i1, i3);
    assertNotEquals(i1, mk.ifexpn(mk.strlist("@"), mk.oplist()));
  }

  @Test
  public void testLiteralEquals() throws CodeSyntaxException {
    CodeMaker mk = maker();
    SpaceInst s1 = mk.space();
    assertEquals(s1, mk.space());
    assertFalse(s1.equals(null));
    assertNotEquals(s1, mk.tab());
    assertNotEquals(s1, mk.end());

    LiteralInst fake = new LiteralInst("space", ".") {
      public InstructionType getType() {
        return InstructionType.SPACE;
      }
    };
    assertNotEquals(s1, fake);
  }

  @Test
  public void testMetaEquals() throws CodeSyntaxException {
    CodeMaker mk = maker();
    MetaInst m1 = mk.metaLeft();
    assertEquals(m1, mk.metaLeft());
    assertFalse(m1.equals(null));
    assertNotEquals(m1, mk.metaRight());
    assertNotEquals(m1, mk.end());

    m1 = mk.metaRight();
    assertEquals(m1, mk.metaRight());
    assertNotEquals(m1, mk.metaLeft());
    assertNotEquals(m1, mk.end());
  }

  @Test
  public void testPredicateEquals() throws CodeSyntaxException {
    CodeMaker mk = maker();
    PredicateInst p1 = mk.predicate(PLURAL);
    PredicateInst p2 = mk.predicate(PLURAL);
    assertNotEquals(p1, mk.predicate(SINGULAR));
    assertEquals(p1, p2);
    assertFalse(p1.equals(null));
    assertNotEquals(p1, mk.predicate(PLURAL, mk.args(" foo")));
    testBlockEquals(p1, p2);

    PredicateInst p3 = mk.predicate(PLURAL, mk.args(" a b"));
    PredicateInst p4 = mk.predicate(PLURAL, mk.args(" a b"));
    assertEquals(p3, p4);
  }

  @Test
  public void testRepeatEquals() throws CodeSyntaxException {
    CodeMaker mk = maker();
    RepeatedInst r1 = mk.repeated("foo.bar");
    RepeatedInst r2 = mk.repeated("foo.bar");
    assertEquals(r1, r2);
    assertFalse(r1.equals(null));
    assertNotEquals(r1, mk.repeated("bar.foo"));
    assertNotEquals(r1, mk.repeated("@"));

    AlternatesWithInst a1 = mk.alternates();
    AlternatesWithInst a2 = mk.alternates();
    r1.setAlternatesWith(a1);
    assertNotEquals(r1, r2);
    r2.setAlternatesWith(a2);
    assertEquals(r1, r2);
    testBlockEquals(r1, r2);
  }

  @Test
  public void testRootEquals() throws CodeSyntaxException {
    CodeMaker mk = maker();
    RootInst r1 = mk.root();
    RootInst r2 = mk.root();
    assertEquals(r1, r2);
    assertFalse(r1.equals(null));
    assertNotEquals(r1, mk.end());
    assertNotEquals(r1, mk.section("foo"));

    r1.getConsequent().add(mk.text("foo"));
    assertNotEquals(r1, r2);
    assertNotEquals(r1, mk.end());
    r2.getConsequent().add(mk.text("foo"));
    assertEquals(r1, r2);
    r2.getConsequent().add(mk.end());
    assertNotEquals(r1, r2);
  }

  @Test
  public void testSectionEquals() throws CodeSyntaxException {
    CodeMaker mk = maker();
    SectionInst s1 = mk.section("foo.bar");
    SectionInst s2 = mk.section("foo.bar");
    assertEquals(s1, s2);
    assertFalse(s1.equals(null));
    assertNotEquals(s1, mk.section("bar.foo"));
    assertNotEquals(s1, mk.section("@"));
    assertNotEquals(s1, mk.end());

    testBlockEquals(s1, s2);
  }

  @Test
  public void testTextEquals() throws CodeSyntaxException {
    CodeMaker mk = maker();
    TextInst t1 = mk.text("foo bar");
    assertEquals(t1, mk.text("foo bar"));
    assertFalse(t1.equals(null));
    assertNotEquals(t1, mk.text("foo-bar"));
    assertNotEquals(t1, mk.comment("foo bar"));
    assertNotEquals(t1, mk.end());
  }

  @Test
  public void testVariableEquals() throws CodeSyntaxException, ArgumentsException {
    CodeMaker mk = maker();
    VariableInst v1 = mk.var("foo.bar");
    assertEquals(v1, mk.var("foo.bar"));
    assertFalse(v1.equals(null));
    assertNotEquals(v1, mk.var("foo"));
    assertNotEquals(v1, mk.var("@"));
    assertNotEquals(v1, mk.var("bar.foo"));
    assertNotEquals(v1, mk.comment("foo.bar"));
    assertNotEquals(v1, mk.end());

    v1 = mk.var("foo.bar", mk.formatters(CoreFormatters.JSON));
    assertEquals(v1, mk.var("foo.bar", mk.formatters(CoreFormatters.JSON)));
    assertFalse(v1.equals(null));

    assertNotEquals(v1, mk.var("@", CoreFormatters.JSON));
    VariableInst x =  mk.var("foo.bar", CoreFormatters.PLURALIZE);
    assertNotEquals(v1, x);
    assertNotEquals(v1, mk.var("bar.foo", CoreFormatters.JSON));
    assertNotEquals(v1, mk.var("foo.bar"));
    assertNotEquals(v1, mk.end());

    // With arguments
    Arguments args1 = mk.args(" a1 a2");
    Arguments args2 = mk.args(" a2 a1");
    VariableInst v2 = mk.var("foo.bar", mk.formatters(mk.fmt(CoreFormatters.PLURALIZE, args1)));
    assertNotEquals(v1, v2);
    assertEquals(v2, mk.var("foo.bar", mk.fmt(CoreFormatters.PLURALIZE, args1)));
    assertNotEquals(v2, mk.var("foo.bar", mk.fmt(CoreFormatters.PLURALIZE, args2)));

    VariableInst v3 = mk.var("a.b.c", mk.fmt(CoreFormatters.SLUGIFY, args1));
    assertEquals(v3, mk.var("a.b.c", mk.fmt(CoreFormatters.SLUGIFY, args1)));

  }

  /**
   * Warning: this modifies the arguments, and assumes the block instructions
   * are initially equivalent to one another.
   */
  private void testBlockEquals(BlockInstruction b1, BlockInstruction b2) {
    CodeMaker mk = maker();
    b1.getConsequent().add(mk.text("---"));
    assertNotEquals(b1, b2);
    b2.getConsequent().add(mk.text("---"));
    assertEquals(b1, b2);

    b1.setAlternative(mk.end());
    assertNotEquals(b1, b2);
    b2.setAlternative(mk.end());
    assertEquals(b1, b2);

    b2.setAlternative(mk.text("oops"));
    assertNotEquals(b1, b2);
    b2.setAlternative(mk.end());
    b2.getConsequent().add(mk.text("oops"));
    assertNotEquals(b1, b2);
  }

}