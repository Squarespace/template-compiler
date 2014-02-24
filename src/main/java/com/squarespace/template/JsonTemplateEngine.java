package com.squarespace.template;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;


/**
 * JSON-Template compiler and execution engine.
 */
public class JsonTemplateEngine {

  private final FormatterTable formatterTable;
  
  private final PredicateTable predicateTable;

  private final MetaData metaData;

  private LoggingHook loggingHook;
  
  /**
  * Since the FormatterTable and PredicateTable classes are extensible with custom
  * instances, this class accepts them as constructor arguments.  Just initialize an
  * instance of this once and use it across multiple threads in your application.
  */
  public JsonTemplateEngine(FormatterTable formatterTable, PredicateTable predicateTable) {
    this.formatterTable = formatterTable;
    this.predicateTable = predicateTable;
    formatterTable.setInUse();
    predicateTable.setInUse();
    this.metaData = new MetaData(formatterTable, predicateTable);
  }
  
  public void setLoggingHook(LoggingHook hook) {
    this.loggingHook = hook;
  }
  
  public MetaData getMetaData() {
    return this.metaData;
  }
  
  /**
   * Execute the instruction against the JSON node.
   */
  public Context execute(Instruction instruction, JsonNode json) throws CodeExecuteException {
    return executeWithPartials(instruction, json, null, new StringBuilder());
  }

  /**
   * Execute the instruction against the JSON node, appending the output to buffer.
   */
  public Context execute(Instruction instruction, JsonNode json, StringBuilder buf) throws CodeExecuteException {
    return executeWithPartials(instruction, json, null, buf);
  }

  /**
   * Execute the instruction against the given JSON node, using the partial template map.
   */
  public Context execute(Instruction instruction, JsonNode json, JsonNode partials)
      throws CodeExecuteException {
    return executeWithPartials(instruction, json, partials, new StringBuilder());
  }

  public Context executeSafe(Instruction instruction, JsonNode json, JsonNode partials) throws CodeExecuteException {
    return executeWithPartialsSafe(instruction, json, partials, new StringBuilder());
  }
  
  /**
   * Execute the instruction against the JSON node, using the partial template map, and append
   * the output to buffer.
   */
  public Context executeWithPartials(Instruction instruction, JsonNode json, JsonNode partials, StringBuilder buf)
      throws CodeExecuteException {

    Context ctx = new Context(json, buf);
    ctx.setCompiler(this);
    ctx.setPartials(partials);
    ctx.setLoggingHook(loggingHook);
    instruction.invoke(ctx);
    return ctx;
  }

  public Context executeWithPartialsSafe(Instruction instruction, JsonNode json, JsonNode partials, StringBuilder buf) 
      throws CodeExecuteException {

    Context ctx = new Context(json, buf);
    ctx.setCompiler(this);
    ctx.setPartials(partials);
    ctx.setLoggingHook(loggingHook);
    ctx.setSafeExecution();
    instruction.invoke(ctx);
    return ctx;
  }
  
  /**
   * Compile the template and return a wrapper containing the instructions. Useful if you want to
   * compile a template once and execute it multiple times.
   */
  public CompiledTemplate compile(String template) throws CodeSyntaxException {
    final CodeMachine machine = new CodeMachine();
    Tokenizer tokenizer = new Tokenizer(template, machine, formatterTable, predicateTable);
    tokenizer.consume();
    return new CompiledTemplate() {
      @Override
      public Instruction code() {
        return machine.getCode();
      }
      @Override
      public List<ErrorInfo> errors() {
        return Collections.emptyList();
      }
      @Override
      public CodeMachine machine() {
        return machine;
      }
    };
  }
  
  public CompiledTemplate compileSafe(String template) throws CodeSyntaxException {
    final CodeMachine machine = new CodeMachine();
    machine.setValidate();
    Tokenizer tokenizer = new Tokenizer(template, machine, formatterTable, predicateTable);
    tokenizer.setValidate();
    tokenizer.consume();
    final List<ErrorInfo> errors = tokenizer.getErrors();
    errors.addAll(machine.getErrors());
    
    return new CompiledTemplate() {
      @Override
      public Instruction code() {
        return machine.getCode();
      }
      @Override
      public List<ErrorInfo> errors() {
        return errors;
      }
      @Override
      public CodeMachine machine() {
        return machine;
      }
    };
  }
  
  /**
   * Compiles the template in validation mode, capturing all errors.
   */
  public ValidatedTemplate validate(String template) throws CodeSyntaxException {
    final CodeList sink = new CodeList();
    final CodeStats stats = new CodeStats();

    // Validate the template at the syntax level.
    Tokenizer tokenizer = new Tokenizer(template, sink, formatterTable, predicateTable);
    tokenizer.setValidate();
    tokenizer.consume();
    final List<ErrorInfo> errors = tokenizer.getErrors();

    // Pass the parsed instructions to the CodeMachine for structural validation, and
    // collect some stats.
    CodeMachine machine = new CodeMachine();
    machine.setValidate();
    for (Instruction inst : sink.getInstructions()) {
      machine.accept(inst);
      stats.accept(inst);
    }
    errors.addAll(machine.getErrors());
    
    // Return all of the validation objects for the template.
    return new ValidatedTemplate() {
      @Override
      public CodeList code() {
        return sink;
      }
      @Override
      public List<ErrorInfo> errors() {
        return errors;
      }
      @Override
      public CodeStats stats() {
        return stats;
      }
    };
  }

}