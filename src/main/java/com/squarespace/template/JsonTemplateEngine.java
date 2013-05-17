package com.squarespace.template;

import com.fasterxml.jackson.databind.JsonNode;


/**
 * JSON-Template compiler and execution engine.
 */
public class JsonTemplateEngine {

  private final FormatterTable formatterTable;
  
  private final PredicateTable predicateTable;

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

  /**
   * Execute the instruction against the JSON node, using the partial template map, and append
   * the output to buffer.
   */
  public Context executeWithPartials(Instruction instruction, JsonNode json, JsonNode partials, StringBuilder buf)
      throws CodeExecuteException {

    Context ctx = new Context(json, buf);
    ctx.setCompiler(this);
    ctx.setPartials(partials);
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
      public Instruction getCode() {
        return machine.getCode();
      }
      @Override
      public CodeMachine getMachine() {
        return machine;
      }
    };
  }
  
  /**
   * Tokenize the template and return the list of instructions.  The list is not built into a
   * tree -- its simply an ordered list of parsed instruction instances.  If you render these in order
   * you'll get back the original template source.
   */
  public CodeList tokenize(String template) throws CodeSyntaxException {
    CodeList sink = new CodeList();
    Tokenizer tokenizer = new Tokenizer(template, sink, formatterTable, predicateTable);
    tokenizer.consume();
    return sink;
  }

  /**
   * Tokenize the template and feed all parsed instructions to the sink.
   */
  public void tokenize(String template, CodeSink sink) throws CodeSyntaxException {
    Tokenizer tokenizer = new Tokenizer(template, sink, formatterTable, predicateTable);
    tokenizer.consume();
  }
}
