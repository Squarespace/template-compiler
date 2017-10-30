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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.squarespace.template.Instructions.AlternatesWithInst;
import com.squarespace.template.Instructions.BindVarInst;
import com.squarespace.template.Instructions.CommentInst;
import com.squarespace.template.Instructions.EndInst;
import com.squarespace.template.Instructions.EofInst;
import com.squarespace.template.Instructions.IfInst;
import com.squarespace.template.Instructions.IfPredicateInst;
import com.squarespace.template.Instructions.InjectInst;
import com.squarespace.template.Instructions.MetaInst;
import com.squarespace.template.Instructions.MacroInst;
import com.squarespace.template.Instructions.NewlineInst;
import com.squarespace.template.Instructions.PredicateInst;
import com.squarespace.template.Instructions.RepeatedInst;
import com.squarespace.template.Instructions.RootInst;
import com.squarespace.template.Instructions.SectionInst;
import com.squarespace.template.Instructions.SpaceInst;
import com.squarespace.template.Instructions.TabInst;
import com.squarespace.template.Instructions.TextInst;
import com.squarespace.template.Instructions.VariableInst;


/**
 * CodeMaker makes creating instances of instructions less verbose. Also useful
 * to shorten a lot of boilerplate in test cases.
 */
public class CodeMaker {

  public <T extends Instruction> T pre(T inst) {
    inst.setPreprocessScope();
    return inst;
  }

  public AlternatesWithInst alternates() {
    return new AlternatesWithInst();
  }

  public Arguments args() {
    return new Arguments();
  }

  public Arguments args(String args) {
    return new Arguments(view(args));
  }

  public BindVarInst bindvar(String name, String path) {
    return new BindVarInst(name, path);
  }

  public BindVarInst bindvar(String name, Variables variables) {
    return new BindVarInst(name, variables);
  }

  public BindVarInst bindvar(String name, Variables variables, List<FormatterCall> formatters) {
    BindVarInst inst = new BindVarInst(name, variables);
    if (!formatters.isEmpty()) {
      inst.setFormatters(formatters);
    }
    return inst;
  }

  public BindVarInst bindvar(String name, String path, List<FormatterCall> formatters) {
    BindVarInst inst = new BindVarInst(name, path);
    if (!formatters.isEmpty()) {
      inst.setFormatters(formatters);
    }
    return inst;
  }

  public StringBuilder buf() {
    return new StringBuilder();
  }

  public CommentInst comment(StringView view) {
    return new CommentInst(view);
  }

  public CommentInst comment(String str) {
    return new CommentInst(new StringView(str));
  }

  public CommentInst comment(String str, int start, int end) {
    return new CommentInst(new StringView(str, start, end));
  }

  public CommentInst mcomment(StringView view) {
    return new CommentInst(view, true);
  }

  public CommentInst mcomment(String str) {
    return new CommentInst(new StringView(str), true);
  }

  public CommentInst mcomment(String str, int start, int end) {
    return new CommentInst(new StringView(str, start, end), true);
  }

  public EndInst end() {
    return new EndInst();
  }

  public EofInst eof() {
    return new EofInst();
  }

  public FormatterCall fmt(Formatter formatter) throws ArgumentsException {
    return fmt(formatter, Constants.EMPTY_ARGUMENTS);
  }

  public FormatterCall fmt(Formatter formatter, Arguments args) throws ArgumentsException {
    FormatterCall res = new FormatterCall(formatter, args);
    formatter.validateArgs(args);
    return res;
  }

  public List<FormatterCall> formatters(Formatter... formatters) {
    List<FormatterCall> list = new ArrayList<>(formatters.length);
    for (Formatter impl : formatters) {
      list.add(new FormatterCall(impl, Constants.EMPTY_ARGUMENTS));
    }
    return list;
  }

  public List<FormatterCall> formatters(FormatterCall... formatters) {
    return Arrays.asList(formatters);
  }

  public IfInst ifexpn(List<String> vars, List<Operator> ops) {
    return new IfInst(vars, ops);
  }

  public IfPredicateInst ifpred(Predicate predicate) throws ArgumentsException {
    return ifpred(predicate, Constants.EMPTY_ARGUMENTS);
  }

  public IfPredicateInst ifpred(Predicate predicate, Arguments args) throws ArgumentsException {
    IfPredicateInst res = new IfPredicateInst(predicate, args);
    predicate.validateArgs(args);
    return res;
  }

  public InjectInst inject(String variable, String path) {
    return new InjectInst(variable, path, Constants.EMPTY_ARGUMENTS);
  }

  public InjectInst inject(String variable, String path, Arguments args) {
    return new InjectInst(variable, path, args);
  }

  public List<Integer> intlist(Integer... numbers) {
    return Arrays.<Integer>asList(numbers);
  }

  public MacroInst macro(String path) {
    return new MacroInst(path);
  }

  public MetaInst metaLeft() {
    return new MetaInst(true);
  }

  public MetaInst metaRight() {
    return new MetaInst(false);
  }

  public NewlineInst newline() {
    return new NewlineInst();
  }

  public List<Operator> oplist(Operator... ops) {
    return Arrays.<Operator>asList(ops);
  }

  public PredicateInst or() {
    PredicateInst inst = predicate(null);
    inst.setOr();
    return inst;
  }

  public PredicateInst or(Predicate impl) {
    return or(impl, Constants.EMPTY_ARGUMENTS);
  }

  public PredicateInst or(Predicate impl, Arguments args) {
    PredicateInst inst = predicate(impl, args);
    inst.setOr();
    return inst;
  }

  public PredicateInst predicate(Predicate impl) {
    return new PredicateInst(impl, Constants.EMPTY_ARGUMENTS);
  }

  public PredicateInst predicate(Predicate impl, Arguments args) {
    return new PredicateInst(impl, args);
  }

  public RepeatedInst repeated(String name) {
    return new RepeatedInst(name);
  }

  public RootInst root() {
    return new RootInst();
  }

  public SectionInst section(String name) {
    return new SectionInst(name);
  }

  public Instruction simple(InstructionType type) {
    switch (type) {
      case ALTERNATES_WITH:
        return alternates();
      case END:
        return end();
      case EOF:
        return eof();
      case META_LEFT:
        return metaLeft();
      case META_RIGHT:
        return metaRight();
      case NEWLINE:
        return newline();
      case ROOT:
        return root();
      case SPACE:
        return space();
      case TAB:
        return tab();
      default:
        throw new RuntimeException("attempt to construct a non-simple type '" + type + "'");
    }
  }

  public SpaceInst space() {
    return new SpaceInst();
  }

  public List<String> strlist(String... keys) {
    return Arrays.<String>asList(keys);
  }

  public String[] strarray(String... elements) {
    return elements;
  }

  public TabInst tab() {
    return new TabInst();
  }

  public TextInst text(StringView view) {
    return new TextInst(view);
  }

  public TextInst text(String str) {
    return new TextInst(new StringView(str));
  }

  public TextInst text(String str, int start, int end) {
    return new TextInst(new StringView(str, start, end));
  }

  public VariableInst var(String name) {
    return var(name, Collections.<FormatterCall>emptyList());
  }

  public VariableInst var(String name, Formatter... formatters) {
    return new VariableInst(name, formatters(formatters));
  }

  public VariableInst var(String name, FormatterCall... formatters) {
    return new VariableInst(name, Arrays.asList(formatters));
  }

  public VariableInst var(String name, List<FormatterCall> formatters) {
    return new VariableInst(name, formatters);
  }

  public VariableInst var(Variables variables) {
    return new VariableInst(variables, Collections.<FormatterCall>emptyList());
  }

  public VariableInst var(Variables variables, Formatter... formatters) {
    return new VariableInst(variables, formatters(formatters));
  }

  public VariableInst var(Variables name, FormatterCall... formatters) {
    return new VariableInst(name, Arrays.asList(formatters));
  }

  public Variables vars(String name, String... names) {
    Variables vars = new Variables(name);
    for (String n : names) {
      vars.add(n);
    }
    return vars;
  }

  public StringView view(String str) {
    return new StringView(str);
  }
}
