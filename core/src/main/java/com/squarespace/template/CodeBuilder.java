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

import java.util.List;

import com.squarespace.template.Instructions.AlternatesWithInst;
import com.squarespace.template.Instructions.BindVarInst;
import com.squarespace.template.Instructions.CommentInst;
import com.squarespace.template.Instructions.EndInst;
import com.squarespace.template.Instructions.EofInst;
import com.squarespace.template.Instructions.IfInst;
import com.squarespace.template.Instructions.IfPredicateInst;
import com.squarespace.template.Instructions.InjectInst;
import com.squarespace.template.Instructions.MacroInst;
import com.squarespace.template.Instructions.MetaInst;
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
 * CodeBuilder lets you chain method calls to create sequences of instructions,
 * feeding each instruction into a CodeMachine to produce a valid tree.
 */
public class CodeBuilder {

  /**
   * State machine to feed the instructions into. It converts a sequence of
   * instructions into a valid instruction tree, or throws an exception.
   */
  private final CodeMachine machine = new CodeMachine();

  /**
   * Builds the instruction instances.
   */
  private final CodeMaker maker = new CodeMaker();

  public CodeBuilder() {
  }

  /**
   * Finishes the sequence of instructions and returns the root instruction.
   */
  public RootInst build() throws CodeSyntaxException {
    machine.complete();
    return machine.getCode();
  }

  /**
   * Feeds multiple instructions to the machine.
   */
  public CodeBuilder accept(Instruction instruction) throws CodeSyntaxException {
    machine.accept(instruction);
    return this;
  }

  /**
   * Builds an {@link AlternatesWithInst} instruction and feeds it to the state machine.
   */
  public CodeBuilder alternatesWith() throws CodeSyntaxException {
    machine.accept(maker.alternates());
    return this;
  }

  /**
   * Builds a {@link BindVarInst} instruction and feeds it to the state machine.
   */
  public CodeBuilder bindvar(String name, String path) throws CodeSyntaxException {
    machine.accept(maker.bindvar(name, path));
    return this;
  }

  /**
   * Builds a {@link CommentInst} instruction and feeds it to the state machine.
   */
  public CodeBuilder comment(StringView view) throws CodeSyntaxException {
    machine.accept(maker.comment(view));
    return this;
  }

  /**
   * Builds a {@link CommentInst} instruction and feeds it to the state machine.
   */
  public CodeBuilder comment(String str) throws CodeSyntaxException {
    machine.accept(maker.comment(str));
    return this;
  }

  /**
   * Builds a {@link CommentInst} instruction and feeds it to the state machine.
   */
  public CodeBuilder comment(String str, int start, int end) throws CodeSyntaxException {
    machine.accept(maker.comment(str, start, end));
    return this;
  }

  /**
   * Builds a block {@link MacroInst} instruction and feeds it to the state machine.
   */
  public CodeBuilder macro(String name) throws CodeSyntaxException {
    machine.accept(maker.macro(name));
    return this;
  }

  /**
   * Builds a block {@link CommentInst} instruction and feeds it to the state machine.
   */
  public CodeBuilder mcomment(StringView view) throws CodeSyntaxException {
    machine.accept(maker.mcomment(view));
    return this;
  }

  /**
   * Builds a block {@link CommentInst} instruction and feeds it to the state machine.
   */
  public CodeBuilder mcomment(String str) throws CodeSyntaxException {
    machine.accept(maker.mcomment(str));
    return this;
  }

  /**
   * Builds a block {@link CommentInst} instruction and feeds it to the state machine.
   */
  public CodeBuilder mcomment(String str, int start, int end) throws CodeSyntaxException {
    machine.accept(maker.mcomment(str, start, end));
    return this;
  }

  /**
   * Builds an {@link EndInst} instruction and feeds it to the state machine.
   */
  public CodeBuilder end() throws CodeSyntaxException {
    machine.accept(maker.end());
    return this;
  }

  /**
   * Builds an {@link EofInst} instruction and feeds it to the state machine.
   */
  public CodeBuilder eof() throws CodeSyntaxException {
    machine.accept(maker.eof());
    return this;
  }

  /**
   * Builds an {@link IfInst} instruction and feeds it to the state machine.
   */
  public CodeBuilder ifexpn(List<String> variables, List<Operator> operators) throws CodeSyntaxException {
    machine.accept(maker.ifexpn(variables, operators));
    return this;
  }

  /**
   * Builds an {@link IfPredicateInst} instruction and feeds it to the state machine.
   */
  public CodeBuilder ifpred(Predicate predicate) throws CodeSyntaxException, ArgumentsException {
    machine.accept(maker.ifpred(predicate));
    return this;
  }

  /**
   * Builds an {@link IfPredicateInst} instruction with the given arguments, and
   * feeds it to the state machine.
   */
  public CodeBuilder ifpred(Predicate predicate, Arguments args) throws CodeSyntaxException, ArgumentsException {
    machine.accept(maker.ifpred(predicate, args));
    return this;
  }

  /**
   * Builds an {@link InjectInst} instruction with the given local variable name,
   * path and empty arguments, and feeds it to the state machine.
   */
  public CodeBuilder inject(String variable, String path) throws CodeSyntaxException {
    machine.accept(maker.inject(variable, path));
    return this;
  }

  /**
   * Builds an {@link InjectInst} instruction with the given local variable name,
   * path and optional arguments, and feeds it to the state machine.
   */
  public CodeBuilder inject(String variable, String path, Arguments args) throws CodeSyntaxException {
    machine.accept(maker.inject(variable, path, args));
    return this;
  }

  /**
   * Builds a {@link MetaInst} instruction for the left meta character and
   * feeds it to the state machine.
   */
  public CodeBuilder metaLeft() throws CodeSyntaxException {
    machine.accept(maker.metaLeft());
    return this;
  }

  /**
   * Builds a {@link MetaInst} instruction for the right meta character and
   * feeds it to the state machine.
   */
  public CodeBuilder metaRight() throws CodeSyntaxException {
    machine.accept(maker.metaRight());
    return this;
  }

  /**
   * Builds a {@link NewlineInst} instruction for the newline character and
   * feeds it to the state machine.
   */
  public CodeBuilder newline() throws CodeSyntaxException {
    machine.accept(maker.newline());
    return this;
  }

  /**
   * Builds an OR {@link PredicateInst} instruction and feeds it to the state machine.
   */
  public CodeBuilder or() throws CodeSyntaxException {
    machine.accept(maker.or());
    return this;
  }

  /**
   * Builds an OR {@link PredicateInst} instruction with the given {@link Predicate}
   * implementation and feeds it to the state machine.
   */
  public CodeBuilder or(Predicate impl) throws CodeSyntaxException {
    machine.accept(maker.or(impl));
    return this;
  }

  /**
   * Builds a {@link PredicateInst} instruction with the given {@link Predicate}
   * implementation and feeds it to the state machine.
   */
  public CodeBuilder predicate(Predicate impl) throws CodeSyntaxException {
    machine.accept(maker.predicate(impl));
    return this;
  }

  /**
   * Builds a {@link RepeatedInst} instruction and feeds it to the state machine.
   */
  public CodeBuilder repeated(String name) throws CodeSyntaxException {
    machine.accept(maker.repeated(name));
    return this;
  }

  /**
   * Builds a {@link SectionInst} instruction and feeds it to the state machine.
   */
  public CodeBuilder section(String name) throws CodeSyntaxException {
    machine.accept(maker.section(name));
    return this;
  }

  /**
   * Builds a {@link SpaceInst} instruction and feeds it to the state machine.
   */
  public CodeBuilder space() throws CodeSyntaxException {
    machine.accept(maker.space());
    return this;
  }

  /**
   * Builds a {@link TabInst} instruction and feeds it to the state machine.
   */
  public CodeBuilder tab() throws CodeSyntaxException {
    machine.accept(maker.tab());
    return this;
  }

  /**
   * Builds a {@link TextInst} instruction and feeds it to the state machine.
   */
  public CodeBuilder text(StringView view) throws CodeSyntaxException {
    machine.accept(maker.text(view));
    return this;
  }

  /**
   * Builds a {@link TextInst} instruction and feeds it to the state machine.
   */
  public CodeBuilder text(String str) throws CodeSyntaxException {
    machine.accept(maker.text(str));
    return this;
  }

  /**
   * Builds a {@link TextInst} instruction and feeds it to the state machine.
   */
  public CodeBuilder text(String str, int start, int end) throws CodeSyntaxException {
    machine.accept(maker.text(str, start, end));
    return this;
  }

  /**
   * Builds a {@link VariableInst} instruction with no formatters and feeds it to the state machine.
   */
  public CodeBuilder var(String name) throws CodeSyntaxException {
    machine.accept(maker.var(name));
    return this;
  }

  /**
   * Builds a {@link VariableInst} instruction with the given {@link Formatter}
   * implementations and feeds it to the state machine.
   */
  public CodeBuilder var(String name, Formatter... formatters) throws CodeSyntaxException {
    machine.accept(maker.var(name, formatters));
    return this;
  }

  /**
   * Builds a {@link VariableInst} instruction with the given {@link FormatterCall}
   * instance and feeds it to the state machine.
   */
  public CodeBuilder var(String name, FormatterCall formatter) throws CodeSyntaxException {
    machine.accept(maker.var(name, formatter));
    return this;
  }

  /**
   * Builds a {@link VariableInst} instruction with the given {@link FormatterCall}
   * instances and feeds it to the state machine.
   */
  public CodeBuilder var(String name, List<FormatterCall> formatters) throws CodeSyntaxException {
    machine.accept(maker.var(name, formatters));
    return this;
  }

}
