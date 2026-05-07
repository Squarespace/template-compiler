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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squarespace.template.Instructions.EofInst;
import com.squarespace.template.compat.CompatLevel;
import com.squarespace.template.Instructions.VariableInst;
import com.squarespace.template.expr.ExprOptions;
import com.squarespace.template.plugins.CoreFormatters;
import com.squarespace.template.plugins.CorePredicates;


@Test(groups = { "unit" })
public class CompilerTest {

  private static final FormatterTable FORMATTERS = new FormatterTable();

  private static final PredicateTable PREDICATES = new PredicateTable();

//  private static final DateFormatter DATE = new DateFormatter();

  private static final Compiler COMPILER;

  static {
    FORMATTERS.register(new CoreFormatters());
    FORMATTERS.register(new UnitTestFormatters());
    PREDICATES.register(new CorePredicates());
    COMPILER = new Compiler(FORMATTERS, PREDICATES);
  }

  @Test
  public void testCompile() throws CodeException {
    COMPILER.compile("{.section foo}{@}{.end}", false, false);
    try {
      COMPILER.compile("{.foo?}", false, false);
      Assert.fail("Expected CodeException");
    } catch (CodeException e) {
    }
  }

  @Test
  public void testValidate() throws CodeException {
    CodeList code = new CodeList();
    code.accept(new VariableInst("@"));
    code.accept(new EofInst());
    ValidatedTemplate validated = COMPILER.validate("{@}");
    assertEquals(validated.code().getInstructions(), code.getInstructions());
  }

  @Test
  public void testExecuteNoTemplate() throws CodeException {
    Context ctx = COMPILER.newExecutor().execute();
    assertEquals(ctx.buffer().toString(), "");
  }

  @Test
  public void testAppendBuffer() throws CodeException {
    StringBuilder buf = new StringBuilder();
    COMPILER.newExecutor().template("{@}").json("1").buffer(buf).execute();
    COMPILER.newExecutor().template("{@}").json("2").buffer(buf).execute();
    COMPILER.newExecutor().template("{@}").json("3").buffer(buf).execute();
    assertEquals(buf.toString(), "123");
  }

  @Test
  public void testExecuteTemplate() throws CodeException {
    Context ctx = COMPILER.newExecutor()
        .template("{@}")
        .json(JsonUtils.decode("123"))
        .execute();
    assertEquals(ctx.buffer().toString(), "123");
  }

  @Test
  public void testMixedArray() throws CodeException {
    String json = "{\"a\": [1, null, 2, null, 3]}";
    CompiledTemplate compiled = COMPILER.compile("{a}");
    Context ctx = COMPILER.newExecutor()
        .code(compiled.code())
        .json(json)
        .execute();
    assertEquals(ctx.buffer().toString(), "1,null,2,null,3");

    compiled = COMPILER.compile("{.repeated section a}{@}{.end}");
    ctx = COMPILER.newExecutor()
        .code(compiled.code())
        .json(json)
        .execute();
    assertEquals(ctx.buffer().toString(), "123");
  }

  @Test
  public void testMixedObject() throws CodeException {
    String json = "{\"a\": {\"b\":1,\"c\":null,\"d\":false,\"e\":\"foo\"} }";
    CompiledTemplate compiled = COMPILER.compile("{a}");
    Context ctx = COMPILER.newExecutor()
        .code(compiled.code())
        .json(json)
        .execute();
    assertEquals(ctx.buffer().toString(), "");
  }

  @Test
  public void testExecuteCompiled() throws CodeException {
    CompiledTemplate compiled = COMPILER.compile("{@}");
    Context ctx = COMPILER.newExecutor()
        .code(compiled.code())
        .json("123")
        .execute();
    assertEquals(ctx.buffer().toString(), "123");
  }

  @Test
  public void testExecutePartials() throws CodeException {
    ObjectNode partialsMap = (ObjectNode) JsonUtils.decode("{\n\"foo\"\n:\n\"{@}\"\n}\n");
    Context ctx = COMPILER.newExecutor()
        .template("{@|apply foo}")
        .json("123")
        .partialsMap(partialsMap)
        .execute();
    assertEquals(ctx.buffer().toString(), "123");
  }

  @Test
  public void testPartialCacheAcrossContexts() throws CodeException {
    Compiler compiler = new Compiler(FORMATTERS, PREDICATES);
    ObjectNode partials = (ObjectNode) JsonUtils.decode("{\"greet\":\"{msg}!\"}");
    JsonNode json = JsonUtils.decode("{\"msg\":\"hi\"}");

    // Both executions render from the same partial source.
    Context ctx1 = compiler.newExecutor().template("{@|apply greet}").json(json).partialsMap(partials).execute();
    Context ctx2 = compiler.newExecutor().template("{@|apply greet}").json(json).partialsMap(partials).execute();
    assertEquals(ctx1.buffer().toString(), "hi!");
    assertEquals(ctx2.buffer().toString(), "hi!");

    // The second context reuses the instruction compiled by the first.
    assertSame(ctx2.getPartial("greet"), ctx1.getPartial("greet"));

    // Same partial name, different source: must recompile, never go stale.
    ObjectNode partials2 = (ObjectNode) JsonUtils.decode("{\"greet\":\"{msg}!!!\"}");
    Context ctx3 = compiler.newExecutor().template("{@|apply greet}").json(json).partialsMap(partials2).execute();
    assertEquals(ctx3.buffer().toString(), "hi!!!");
    assertNotSame(ctx3.getPartial("greet"), ctx1.getPartial("greet"));
  }

  @Test
  public void testCodeLimiter() throws CodeException {
    CodeLimiter limiter = new NoopCodeLimiter();
    Context ctx = COMPILER.newExecutor()
        .template("#{@}#")
        .json("123")
        .codeLimiter(limiter)
        .execute();
    // Instructions: root text var text
    assertEquals(limiter.instructionCount(), 4);
    assertEquals(ctx.buffer().toString(), "#123#");
  }

  @Test
  public void testLocale() throws CodeException {
    Context ctx = COMPILER.newExecutor()
        .template("{@|date %B}")
        .json("{\"website\":{\"timeZone\":\"UTC\"},\"now\": 1}")
        .locale(Locale.GERMANY)
        .execute();
    assertEquals(ctx.buffer().toString(), "Januar");
  }

  @Test
  public void testSafeMode() throws CodeException {
    Context ctx = COMPILER.newExecutor()
        .safeExecution(true)
        .template("{@|foobar}")
        .json("123")
        .execute();

    assertEquals(ctx.getErrors().size(), 1);
    assertEquals(ctx.getErrors().get(0).getType(), SyntaxErrorType.FORMATTER_UNKNOWN);
  }

  @Test
  public void testEval() throws CodeException {
    Context ctx = COMPILER.newExecutor()
        .template("{.eval 2 + 3}")
        .json("{}")
        .execute();
    assertEquals(ctx.getErrors().size(), 0);
    assertEquals(ctx.buffer().toString(), "5");
  }

  @Test
  public void testEvalIntegralResults() throws CodeException {
    // Legacy, the default level renders integral results through the
    // double path: exact below 2^53, rounded above it.
    assertEval("{.eval 3.0}", "3");
    assertEval("{.eval 2 ** 62}", "4611686018427388000");
    // fractional and out-of-range results render as double at every level
    assertEval("{.eval 1.5}", "1.5");
    assertEval("{.eval 1/3}", "0.3333333333333333");
    assertEval("{.eval 2 ** 64}", "18446744073709552000");
    // NOTE: literals past 2^53 are already rounded to a double at tokenize
    // time, so 2^53+1 renders as 2^53 (same as JS).
    assertEval("{.eval 9007199254740993}", "9007199254740992");
    assertEval("{.eval 0x20000000000001}", "9007199254740992");

    // Fixed, integral results within long range render as exact long values.
    Context fixed = COMPILER.newExecutor()
        .template("{.eval 2 ** 62}")
        .json("{}")
        .compat(CompatLevel.fixed())
        .execute();
    assertEquals(fixed.getErrors().size(), 0);
    assertEquals(fixed.buffer().toString(), "4611686018427387904");
  }

  @Test
  public void testEvalMaxTokens() throws CodeException {
    // Two executions share one compiled template; only one sets a token
    // limit. The limited execution must refuse to build the expression
    // and emit nothing, the other must evaluate it normally.
    CompiledTemplate compiled = COMPILER.compile("{.eval 1 + 2 + 3 + 4 + 5 + 6}");

    ExprOptions opts = new ExprOptions();
    opts.maxTokens(10);
    Context limited = COMPILER.newExecutor()
        .code(compiled.code())
        .json("{}")
        .exprOptions(opts)
        .execute();
    assertEquals(limited.getErrors().size(), 1);
    assertEquals(limited.getErrors().get(0).getType(), ExecuteErrorType.EXPRESSION_PARSE);
    assertTrue(limited.getErrors().get(0).getMessage().contains("maximum number of allowed tokens"));
    assertEquals(limited.buffer().toString(), "");

    Context unlimited = COMPILER.newExecutor()
        .code(compiled.code())
        .json("{}")
        .execute();
    assertEquals(unlimited.getErrors().size(), 0);
    assertEquals(unlimited.buffer().toString(), "21");
  }

  private void assertEval(String template, String expected) throws CodeException {
    Context ctx = COMPILER.newExecutor()
        .template(template)
        .json("{}")
        .execute();
    assertEquals(ctx.getErrors().size(), 0);
    assertEquals(ctx.buffer().toString(), expected);
  }

  @Test
  public void testLoggingHook() throws CodeException {
    final AtomicInteger count = new AtomicInteger();
    LoggingHook loggingHook = new LoggingHook() {
      @Override
      public void log(Exception e) {
        count.incrementAndGet();
        assertTrue(e instanceof NullPointerException);
      }
    };
    Context ctx = COMPILER.newExecutor()
        .template("{@|npe}")
        .json("123")
        .safeExecution(true)
        .loggingHook(loggingHook)
        .execute();
    assertEquals(count.get(), 1);
    assertEquals(ctx.getErrors().size(), 1);
    assertEquals(ctx.getErrors().get(0).getType(), ExecuteErrorType.UNEXPECTED_ERROR);
  }

}
