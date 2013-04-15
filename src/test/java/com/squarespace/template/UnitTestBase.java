package com.squarespace.template;

import static org.testng.Assert.assertEquals;

import com.ibm.icu.text.NumberFormat;
import com.squarespace.v6.utils.JSONUtils;

/**
 * Methods to simplify writing tests against the JSON template package.
 */
class UnitTestBase {

  private static final boolean DEBUG = false;
  
  private static final PredicateTable predicateTable = new PredicateTable();
  
  private static final FormatterTable formatterTable = new FormatterTable();
  
  static {
    predicateTable.register(CorePredicates.class);
    predicateTable.register(UnitTestPlugins.class);

    formatterTable.register(CoreFormatters.class);
    formatterTable.register(UnitTestPlugins.class);

    // Used to tune the symbol table sizes.
    if (DEBUG) {
      System.out.println("\nINSTRUCTION TABLE:");
      InstructionTable.dump();
      System.out.println("\nPREDICATE TABLE:");
      predicateTable.dump();
      System.out.println("============================\n");
      System.out.println("\nFORMATTER TABLE:");
      formatterTable.dump();
      System.out.println("============================\n");
    }
  }
  
  /**
   * This instance is stateless so it can be reused across tests.
   */
  private CodeMaker maker = new CodeMaker();
  
  public JsonTemplateEngine compiler() {
    return new JsonTemplateEngine(formatterTable, predicateTable);
  }
  
  public CodeBuilder builder() {
    return new CodeBuilder();
  }
  
  public CodeMachine machine() {
    return new CodeMachine();
  }
  
  public CodeMaker maker() {
    return maker;
  }

  public CodeList collector() {
    return new CodeList();
  }
  
  public Tokenizer tokenizer(String data) {
    return tokenizer(data, collector());
  }
  
  public Tokenizer tokenizer(String data, CodeSink sink) {
    return new Tokenizer(data, sink, formatterTable, predicateTable);
  }
  
  public String repr(Instruction inst) {
    return ReprEmitter.get(inst, true);
  }

  public String eval(Context ctx) {
    return ctx.getBuffer().toString();
  }
  
  public String commas(long num) {
    return NumberFormat.getInstance().format(num);
  }
  
  public String commas(double num) {
    return NumberFormat.getInstance().format(num);
  }
  
  /**
   * Execute the instruction on the given JSON data.  Return the execution context.
   */
  public Context execute(String jsonData, Instruction ... instructions) throws CodeExecuteException {
    Context ctx = new Context(JSONUtils.decode(jsonData));
    for (Instruction inst : instructions) {
      inst.invoke(ctx);
    }
    return ctx;
  }

  /**
   * Grab the buffer from the context and assert that it equals the expected value.
   */
  public void assertContext(Context ctx, String expected) {
    String result = ctx.getBuffer().toString();
    assertEquals(result, expected);
  }
}
