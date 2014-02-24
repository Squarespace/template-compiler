package com.squarespace.template;

import java.util.List;

import com.squarespace.template.Instructions.RootInst;


/**
 * CodeBuilder lets you chain method calls to create sequences of instructions, 
 * feeding each instruction into a CodeMachine to produce a valid tree.
 */
public class CodeBuilder {

  private CodeMachine machine = new CodeMachine();
  
  private CodeMaker maker = new CodeMaker();
  
  public CodeBuilder() {
  }
  
  public RootInst build() throws CodeSyntaxException {
    machine.complete();
    return machine.getCode();
  }
  
  public CodeBuilder accept(Instruction ... instructions) throws CodeSyntaxException {
    machine.accept(instructions);
    return this;
  }
  
  public CodeBuilder alternatesWith() throws CodeSyntaxException {
    machine.accept(maker.alternates());
    return this;
  }
  
  public CodeBuilder comment(StringView view) throws CodeSyntaxException {
    machine.accept(maker.comment(view));
    return this;
  }
  
  public CodeBuilder comment(String str) throws CodeSyntaxException {
    machine.accept(maker.comment(str));
    return this;
  }
  
  public CodeBuilder comment(String str, int start, int end) throws CodeSyntaxException {
    machine.accept(maker.comment(str, start, end));
    return this;
  }
  
  public CodeBuilder mcomment(StringView view) throws CodeSyntaxException {
    machine.accept(maker.mcomment(view));
    return this;
  }
  
  public CodeBuilder mcomment(String str) throws CodeSyntaxException {
    machine.accept(maker.mcomment(str));
    return this;
  }
  
  public CodeBuilder mcomment(String str, int start, int end) throws CodeSyntaxException {
    machine.accept(maker.mcomment(str, start, end));
    return this;
  }
  
  public CodeBuilder end() throws CodeSyntaxException {
    machine.accept(maker.end());
    return this;
  }

  public CodeBuilder eof() throws CodeSyntaxException {
    machine.accept(maker.eof());
    return this;
  }
  
  public CodeBuilder ifexpn(List<String> variables, List<Operator> operators) throws CodeSyntaxException {
    machine.accept(maker.ifexpn(variables, operators));
    return this;
  }
  
  public CodeBuilder ifpred(Predicate predicate) throws CodeSyntaxException {
    machine.accept(maker.ifpred(predicate));
    return this;
  }
  
  public CodeBuilder ifpred(Predicate predicate, Arguments args) throws CodeSyntaxException {
    machine.accept(maker.ifpred(predicate, args));
    return this;
  }
  
  public CodeBuilder metaLeft() throws CodeSyntaxException {
    machine.accept(maker.metaLeft());
    return this;
  }
  
  public CodeBuilder metaRight() throws CodeSyntaxException {
    machine.accept(maker.metaRight());
    return this;
  }
  
  public CodeBuilder newline() throws CodeSyntaxException {
    machine.accept(maker.newline());
    return this;
  }

  public CodeBuilder or() throws CodeSyntaxException {
    machine.accept(maker.or());
    return this;
  }

  public CodeBuilder or(Predicate impl) throws CodeSyntaxException {
    machine.accept(maker.or(impl));
    return this;
  }

  public CodeBuilder predicate(Predicate impl) throws CodeSyntaxException {
    machine.accept(maker.predicate(impl));
    return this;
  }
  
  public CodeBuilder repeated(String name) throws CodeSyntaxException {
    machine.accept(maker.repeated(name));
    return this;
  }

  public CodeBuilder section(String name) throws CodeSyntaxException {
    machine.accept(maker.section(name));
    return this;
  }
  
  public CodeBuilder space() throws CodeSyntaxException {
    machine.accept(maker.space());
    return this;
  }
  
  public CodeBuilder tab() throws CodeSyntaxException {
    machine.accept(maker.tab());
    return this;
  }
  
  public CodeBuilder text(StringView view) throws CodeSyntaxException {
    machine.accept(maker.text(view));
    return this;
  }

  public CodeBuilder text(String str) throws CodeSyntaxException {
    machine.accept(maker.text(str));
    return this;
  }
  
  public CodeBuilder text(String str, int start, int end) throws CodeSyntaxException {
    machine.accept(maker.text(str, start, end));
    return this;
  }
  
  public CodeBuilder var(String name) throws CodeSyntaxException {
    machine.accept(maker.var(name));
    return this;
  }
  
  public CodeBuilder var(String name, Formatter ... formatters) throws CodeSyntaxException {
    machine.accept(maker.var(name, formatters));
    return this;
  }
  
  public CodeBuilder var(String name, FormatterCall formatter) throws CodeSyntaxException {
    machine.accept(maker.var(name, formatter));
    return this;
  }
  
  public CodeBuilder var(String name, List<FormatterCall> formatters) throws CodeSyntaxException {
    machine.accept(maker.var(name, formatters));
    return this;
  }
  

}
