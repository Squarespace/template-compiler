package com.squarespace.template;

import com.fasterxml.jackson.databind.JsonNode;


/**
 * Reusable class to compile and execute templates. 
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
   * Execute the instruction against the JSON node, appending the output to buf.
   */
  public Context execute(Instruction instruction, JsonNode json, StringBuilder buf) throws CodeExecuteException {
    return executeWithPartials(instruction, json, null, buf);
  }

  /**
   * Execute the instruction against the given JSON node, using the partial template map.
   */
  public Context executeWithPartials(Instruction instruction, JsonNode json, JsonNode partials)
      throws CodeExecuteException {
    return executeWithPartials(instruction, json, partials, new StringBuilder());
  }

  /**
   * Execute the instruction against the JSON node, using the partial template map, and append
   * the output to buf.
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
   * Compile the template and return the root instruction. Useful if you want to
   * compile a template once and execute it multiple times.
   */
  public CompiledTemplate compile(String template) throws CodeSyntaxException {
    CodeMachine machine = new CodeMachine();
    Tokenizer tokenizer = new Tokenizer(template, machine, formatterTable, predicateTable);
    tokenizer.consume();
    return new CompiledTemplate(machine);
  }

}
