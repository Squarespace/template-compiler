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
import static org.testng.Assert.assertTrue;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squarespace.template.Instructions.EofInst;
import com.squarespace.template.Instructions.VariableInst;
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
