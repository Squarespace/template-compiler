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

import static com.squarespace.template.Constants.EMPTY_ARGUMENTS;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.template.plugins.CoreFormatters;
import com.squarespace.template.plugins.CorePredicates;

/**
 * Methods to simplify writing tests against the JSON template package.
 */
public class UnitTestBase {

  // Set this flag to emit extra info for debugging
  protected static final boolean DEBUG = false;

  /**
   * This instance is stateless so it can be reused across tests.
   */
  protected final CodeMaker maker = new CodeMaker();

  public Compiler compiler() {
    return new Compiler(formatterTable(), predicateTable());
  }

  public FormatterTable formatterTable() {
    FormatterTable table = new FormatterTable();
    table.register(new CoreFormatters());
    table.register(new UnitTestFormatters());
    return table;
  }

  public PredicateTable predicateTable() {
    PredicateTable table = new PredicateTable();
    table.register(new CorePredicates());
    table.register(new UnitTestPredicates());
    return table;
  }

  public CodeBuilder builder() {
    return new CodeBuilder();
  }

  public Context context(String raw) {
    return new Context(json(raw));
  }

  public JsonNode json(String raw) {
    return JsonUtils.decode(raw);
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
    return new Tokenizer(data, sink, formatterTable(), predicateTable());
  }

  public Tokenizer tokenizer(String data, CodeSink sink, boolean preprocess) {
    return new Tokenizer(data, sink, preprocess, formatterTable(), predicateTable());
  }

  public String repr(Instruction inst) {
    return ReprEmitter.get(inst, true);
  }

  public String eval(Context ctx) {
    return ctx.buffer().toString();
  }

  public String commas(long num) {
    return NumberFormat.getInstance().format(num);
  }

  public String commas(double num) {
    return NumberFormat.getInstance().format(num);
  }

  /**
   * Run legacy tests on Java 8 only.
   */
  public static boolean isJava8() {
    String version = System.getProperty("java.version");
    return version.startsWith("1.8");
  }

  public List<ErrorType> errorTypes(List<ErrorInfo> errors) {
    List<ErrorType> actual = new ArrayList<>(errors.size());
    for (ErrorInfo error : errors) {
      actual.add(error.getType());
    }
    return actual;
  }

  /**
   * Execute the instruction on the given JSON node.  Return the execution context.
   */
  public Context execute(String jsonData, Instruction instruction) throws CodeExecuteException {
    return execute(JsonUtils.decode(jsonData), instruction);
  }

  /**
   * Execute the instruction on the given JSON data.  Return the execution context.
   */
  public Context execute(JsonNode node, Instruction... instructions) throws CodeExecuteException {
    Context ctx = new Context(node);
    for (Instruction inst : instructions) {
      ctx.execute(inst);
    }
    return ctx;
  }

  public Context execute(String template, String jsonData) throws CodeException {
    return execute(template, json(jsonData));
  }

  public Context execute(String template, JsonNode json) throws CodeException {
    Context ctx = new Context(json);
    CodeMachine sink = machine();
    tokenizer(template, sink).consume();
    ctx.execute(sink.getCode());
    return ctx;
  }

  /**
   * Grab the buffer from the context and assert that it equals the expected value.
   */
  public void assertContext(Context ctx, String expected) {
    String result = ctx.buffer().toString();
    assertEquals(result, expected);
  }


  public String format(Formatter impl, String json) throws CodeException {
    return format(impl, EMPTY_ARGUMENTS, json);
  }

  public String format(Formatter impl, Arguments args, String json) throws CodeException {
    Context ctx = new Context(JsonUtils.decode(json));
    impl.validateArgs(args);
    Variables variables = new Variables("var", ctx.node());
    impl.apply(ctx, args, variables);
    return variables.first().node().asText();
  }

  public void assertFormatter(Formatter impl, String json, String expected) throws CodeException {
    assertEquals(format(impl, EMPTY_ARGUMENTS, json), expected);
  }

  public void assertFormatter(Formatter impl, Arguments args, String json, String expected) throws CodeException {
    assertEquals(format(impl, args, json), expected);
  }

  public void assertInvalidArgs(Formatter impl, Arguments args) {
    try {
      impl.validateArgs(args);
      fail("Expected " + args + " to raise exception");
    } catch (ArgumentsException e) {
      // Expected
    }
  }

  public static String readFile(Path path) throws IOException {
    try (InputStream input = Files.newInputStream(path)) {
      return GeneralUtils.streamToString(input);
    }
  }

}

