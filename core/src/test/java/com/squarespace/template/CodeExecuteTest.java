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

import static com.squarespace.template.ExecuteErrorType.APPLY_PARTIAL_RECURSION_DEPTH;
import static com.squarespace.template.ExecuteErrorType.INCLUDE_PARTIAL_MISSING;
import static com.squarespace.template.ExecuteErrorType.INCLUDE_PARTIAL_SYNTAX;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.squarespace.template.Instructions.RootInst;
import com.squarespace.template.compat.CompatLevel;
import com.squarespace.template.plugins.CorePredicates;


/**
 * Executing pieces of code and verifying output.
 */
@Test(groups = { "unit" })
public class CodeExecuteTest extends UnitTestBase {

  private static final String ALPHAS = "abcdefghijklmnopqrstuvwxyz";

  @Test
  public void testBindVar() throws CodeException {
    RootInst root = builder().bindvar("@name", "foo").var("@name").eof().build();
    assertEquals(repr(root), "{.var @name foo}{@name}");
    assertContext(execute("{\"foo\": 123}", root), "123");

    // Bind variable to element inside array
    root = builder().bindvar("@name", "foo.1").var("@name").eof().build();
    assertEquals(repr(root), "{.var @name foo.1}{@name}");
    assertContext(execute("{\"foo\": [1,2,3]}", root), "2");

    // Resolve variable in outer scope
    root = builder().bindvar("@val", "foo").section("bar").var("@val").end().eof().build();
    assertEquals(repr(root), "{.var @val foo}{.section bar}{@val}{.end}");
    assertContext(execute("{\"foo\": 1, \"bar\": 2}", root), "1");

    // Full example with nesting
    String json = "{\"managers\":["
        + "{\"name\": \"Bill\", \"employees\": [{\"name\": \"Peter\"}, {\"name\": \"Michael\"}]},"
        + "{\"name\": \"Bob\", \"employees\": [{\"name\": \"Samir\"}]}"
        + "]}";
    CodeBuilder cb = builder();
    cb.repeated("managers").bindvar("@boss", "name").bindvar("@boss-idx", "@index");
    cb.repeated("employees").var("@boss-idx").text(".").var("@index").text(" ");
    cb.var("name").text(" is managed by ").var("@boss").text("\n").end();
    root = cb.alternatesWith().text("---\n").end().eof().build();
    assertContext(execute(json, root),
        "1.1 Peter is managed by Bill\n"
        + "1.2 Michael is managed by Bill\n"
        + "---\n"
        + "2.1 Samir is managed by Bob\n");

    // Example with dotted access on @var
    json = "{\"person\": {\"name\": \"Larry\", \"age\": \"21\"}}";
    root = builder().bindvar("@person", "person").var("@person.name").text(" is ").var("@person.age").eof().build();
    assertContext(execute(json, root), "Larry is 21");
  }

  @Test
  public void testCtxVar() throws CodeException {
    RootInst root = builder()
        .ctxvar("@foo", "src=image.src", "foc=style.focus", "unk=var.not.exist")
        .var("@foo.src").text(" (")
        .var("@foo.foc.x").text(",").var("@foo.foc.y")
        .text(") ")
        .var("@foo.unk").eof().build();

    assertEquals(repr(root),
        "{.ctx @foo src=image.src foc=style.focus unk=var.not.exist}"
        + "{@foo.src} ({@foo.foc.x},{@foo.foc.y}) {@foo.unk}");

    Context ctx = execute("{\"image\":{\"src\":\"./face.jpg\"},"
        + "\"style\":{\"focus\":{\"x\":0.3,\"y\":0.7}}}", root);
    assertContext(ctx, "./face.jpg (0.3,0.7) ");
  }

  @Test
  public void testLiterals() throws CodeException {
    RootInst root = builder().metaLeft().space().tab().newline().metaRight().eof().build();
    assertContext(execute("{}", root), "{ \t\n}");
  }

  @Test
  public void testPredicates() throws CodeException {
    CodeBuilder builder = builder();
    builder.predicate(CorePredicates.PLURAL).text("A");
    builder.or(CorePredicates.SINGULAR).text("B");
    builder.or().text("C").end();

    Instruction root = builder.eof().build();
    assertEquals(repr(root), "{.plural?}A{.or singular?}B{.or}C{.end}");
    assertContext(execute("5", root), "A");
    assertContext(execute("1", root), "B");
    assertContext(execute("0", root), "C");
    assertContext(execute("-3.1415926", root), "C");
  }

  @Test
  public void testSection() throws CodeException {
    RootInst root = builder().section("foo.bar").var("baz").end().eof().build();

    String json = "{\"foo\": {\"bar\": {\"baz\": 123}}}";
    assertEquals(repr(root), "{.section foo.bar}{baz}{.end}");
    assertContext(execute(json, root), "123");

    root = builder().section("foo.1").var("@").end().eof().build();
    json = "{\"foo\": [\"a\", \"b\"]}";
    assertEquals(repr(root), "{.section foo.1}{@}{.end}");
    assertContext(execute(json, root), "b");
  }

  @Test
  public void testSectionMissing() throws CodeException {
    CodeBuilder builder = builder();
    builder.section("foo").text("A").or().text("B").end();
    RootInst root = builder.eof().build();
    assertContext(execute("{\"foo\": 123}", root), "A");
    assertContext(execute("{}", root), "B");
    assertContext(execute("{\"foo\": null}", root), "B");
    assertContext(execute("{\"foo\": []}", root), "B");
  }

  @Test
  public void testText() throws CodeException {
    String expected = "defjkl";
    RootInst root = builder().text(ALPHAS, 3, 6).text(ALPHAS, 9, 12).eof().build();
    assertContext(execute("{}", root), expected);
  }

  @Test
  public void testRepeat() throws CodeException {
    String expected = "Hi, Joe! Hi, Bob! ";
    RootInst root = builder().repeated("@").text("Hi, ").var("foo").text("! ").end().eof().build();
    assertContext(execute("[{\"foo\": \"Joe\"},{\"foo\": \"Bob\"}]", root), expected);
  }

  @Test
  public void testRepeatOr() throws CodeException {
    RootInst root = builder().repeated("foo").text("A").var("@").or().text("B").end().eof().build();
    assertEquals(repr(root), "{.repeated section foo}A{@}{.or}B{.end}");
    assertContext(execute("{\"foo\": [1, 2, 3]}", root), "A1A2A3");
    assertContext(execute("{\"foo\": []}", root), "B");
    assertContext(execute("{}", root), "B");
  }

  @Test
  public void testRepeatIndex() throws CodeException {
    RootInst root = builder().repeated("foo")
          .var("@index").text("-").var("@index0").text(" ")
          .end().eof().build();
    assertEquals(repr(root), "{.repeated section foo}{@index}-{@index0} {.end}");
    assertContext(execute("{\"foo\": [8, 8, 8]}", root), "1-0 2-1 3-2 ");
    assertContext(execute("{\"foo\": []}", root), "");
    assertContext(execute("{}", root), "");
  }

  @Test
  public void testVariable() throws CodeException {
    RootInst root = builder().var("foo.bar").eof().build();
    assertContext(execute("{\"foo\": {\"bar\": 123}}", root), "123");

    root = builder().var("@").eof().build();
    assertContext(execute("3.14159", root), "3.14159");
    assertContext(execute("123.000", root), "123");
    assertContext(execute("null", root), "");


    root = builder().var("foo.2.bar").eof().build();
    assertContext(execute("{\"foo\": [0, 0, {\"bar\": \"hi\"}]}", root), "hi");

    root = builder().var("foo").eof().build();
    assertContext(execute("{\"foo\": [\"a\", \"b\", 123]}", root), "a,b,123");
  }

  @Test
  public void testVariableScope() throws CodeException {
    RootInst root = builder().repeated("names")
        .bindvar("@curr", "name").alternatesWith().var("@curr").end().eof().build();
    String json = "{\"names\": [{\"name\": \"bob\"}, {\"name\": \"larry\"}]}";
    assertContext(execute(json, root), "bob");
  }

  @Test
  public void testVariableTypes() throws CodeException {
    RootInst root = builder().var("@").eof().build();
    String value = "12345678900000000.1234567890000000";
    DecimalNode node = new DecimalNode(new BigDecimal(value));
    assertContext(execute(node, root), value);

    value = "123.0";
    node = new DecimalNode(new BigDecimal(value));
    assertContext(execute(node, root), value);
  }

  @Test
  public void testEvalSharedCompiledTemplate() throws Exception {
    // Compile once and share the code across many concurrent executions.
    // The expression is built in the constructor, so all threads that
    // share the compiled template evaluate the same complete expression.
    Compiler compiler = compiler();
    CompiledTemplate compiled = compiler.compile("{.eval a + 40}", false, false);
    JsonNode json = this.json("{\"a\": 4}");

    final int threads = 16;
    final int perThread = 20000;
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(threads);
    final AtomicLong dropped = new AtomicLong();
    for (int t = 0; t < threads; t++) {
      pool.submit(() -> {
        try {
          start.await();
          for (int i = 0; i < perThread; i++) {
            try {
              Context ctx = compiler.newExecutor()
                  .code(compiled.code())
                  .json(json)
                  .enableExpr(true)
                  .execute();
              if (!"44".equals(ctx.buffer().toString()) || !ctx.getErrors().isEmpty()) {
                dropped.incrementAndGet();
              }
            } catch (CodeException e) {
              dropped.incrementAndGet();
            }
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } finally {
          done.countDown();
        }
      });
    }
    start.countDown();
    assertTrue(done.await(300, TimeUnit.SECONDS));
    pool.shutdown();
    assertEquals(dropped.get(), 0L);
  }

  @Test
  public void testIncludePartialDepthBreach() throws CodeException {
    String partials = "{\"pA\": \"{.include pB}\", \"pB\": \"B\", \"pC\": \"C\"}";
    String template = "{.include pA}{.include pC output}";

    // Released behavior. The breach inside pA leaks the depth counter, so
    // the include of pC fails with a spurious depth error.
    Context ctx = partialContext("{}", partials, true, CompatLevel.defaultLevel());
    ctx.execute(compiler().compile(template, true, false, CompatLevel.defaultLevel()).code());
    assertContext(ctx, "");
    assertEquals(ctx.getErrors().size(), 2);
    assertEquals(ctx.getErrors().get(0).getType(), APPLY_PARTIAL_RECURSION_DEPTH);
    assertEquals(ctx.getErrors().get(1).getType(), APPLY_PARTIAL_RECURSION_DEPTH);

    // Fixed behavior. Only the real breach is reported and pC includes fine.
    ctx = partialContext("{}", partials, true, CompatLevel.at(1));
    ctx.execute(compiler().compile(template, true, false, CompatLevel.at(1)).code());
    assertContext(ctx, "C");
    assertEquals(ctx.getErrors().size(), 1);
    assertEquals(ctx.getErrors().get(0).getType(), APPLY_PARTIAL_RECURSION_DEPTH);
  }

  @Test
  public void testApplyPartialDepthBreach() throws CodeException {
    String partials = "{\"pA\": \"{.include pB}\", \"pB\": \"B\", \"pC\": \"C\"}";
    String json = "{\"out\": {}, \"out2\": {}}";
    String template = "{out|apply pA}{out2|apply pC}";

    // Released behavior. The breach inside pA leaks the depth counter, so
    // the apply of pC fails with a spurious depth error.
    Context ctx = partialContext(json, partials, true, CompatLevel.defaultLevel());
    ctx.execute(compiler().compile(template, true, false, CompatLevel.defaultLevel()).code());
    assertContext(ctx, "");
    assertEquals(ctx.getErrors().size(), 2);
    assertEquals(ctx.getErrors().get(0).getType(), APPLY_PARTIAL_RECURSION_DEPTH);
    assertEquals(ctx.getErrors().get(1).getType(), APPLY_PARTIAL_RECURSION_DEPTH);

    // Fixed behavior. Only the real breach is reported and pC applies fine.
    ctx = partialContext(json, partials, true, CompatLevel.at(1));
    ctx.execute(compiler().compile(template, true, false, CompatLevel.at(1)).code());
    assertContext(ctx, "C");
    assertEquals(ctx.getErrors().size(), 1);
    assertEquals(ctx.getErrors().get(0).getType(), APPLY_PARTIAL_RECURSION_DEPTH);
  }

  @Test
  public void testIncludePartialDepthAfterThrow() throws CodeException {
    String partials = "{\"pA\": \"{.include missing}\", \"pC\": \"C\"}";

    // The missing include inside pA throws in non-safe mode.
    for (CompatLevel compat : new CompatLevel[] { CompatLevel.defaultLevel(), CompatLevel.at(1) }) {
      Context ctx = partialContext("{}", partials, false, compat);
      try {
        ctx.execute(compiler().compile("{.include pA}", false, false, compat).code());
        fail("Expected the missing include to throw");
      } catch (CodeExecuteException e) {
        assertEquals(e.getErrorInfo().getType(), INCLUDE_PARTIAL_MISSING);
      }

      // The next include should see a clean depth counter.
      if (compat.level() == 0) {
        // Released behavior. The leaked counter raises a spurious depth error.
        try {
          ctx.execute(compiler().compile("{.include pC}", false, false, compat).code());
          fail("Expected the leaked depth counter to throw");
        } catch (CodeExecuteException e) {
          assertEquals(e.getErrorInfo().getType(), APPLY_PARTIAL_RECURSION_DEPTH);
        }
      } else {
        ctx.execute(compiler().compile("{.include pC output}", false, false, compat).code());
        assertContext(ctx, "C");
        assertEquals(ctx.getErrors().size(), 0);
      }
    }
  }

  @Test
  public void testApplyPartialDepthAfterThrow() throws CodeException {
    String partials = "{\"pA\": \"{.include missing}\", \"pC\": \"C\"}";
    String json = "{\"out\": {}}";

    // The missing include inside pA throws in non-safe mode.
    for (CompatLevel compat : new CompatLevel[] { CompatLevel.defaultLevel(), CompatLevel.at(1) }) {
      Context ctx = partialContext(json, partials, false, compat);
      try {
        ctx.execute(compiler().compile("{out|apply pA}", false, false, compat).code());
        fail("Expected the missing include to throw");
      } catch (CodeExecuteException e) {
        assertEquals(e.getErrorInfo().getType(), INCLUDE_PARTIAL_MISSING);
      }

      // The next include should see a clean depth counter.
      if (compat.level() == 0) {
        // Released behavior. The leaked counter raises a spurious depth error.
        try {
          ctx.execute(compiler().compile("{.include pC}", false, false, compat).code());
          fail("Expected the leaked depth counter to throw");
        } catch (CodeExecuteException e) {
          assertEquals(e.getErrorInfo().getType(), APPLY_PARTIAL_RECURSION_DEPTH);
        }
      } else {
        ctx.execute(compiler().compile("{.include pC output}", false, false, compat).code());
        assertContext(ctx, "C");
        assertEquals(ctx.getErrors().size(), 0);
      }
    }
  }

  @Test
  public void testIncludePartialBufferAfterThrow() throws CodeException {
    // pA compiles but fails at runtime on the missing include inside it.
    // pB emits output and then fails at runtime. pC fails to compile.
    String partials = "{\"pA\": \"{.include missing}\", "
        + "\"pB\": \"X {.include missing}\", "
        + "\"pC\": \"{.bogus}\"}";
    String template = "A {.include pA} B";

    // The missing include inside the partial throws in non-safe mode. The
    // caller buffer, seeded before the run, must survive the throw with its
    // identity and its content.
    for (CompatLevel compat : new CompatLevel[] { CompatLevel.defaultLevel(), CompatLevel.at(1) }) {
      StringBuilder callerBuffer = new StringBuilder("caller");
      Context ctx = partialContext("{}", partials, false, compat, callerBuffer);
      try {
        ctx.execute(compiler().compile(template, false, false, compat).code());
        fail("Expected the missing include to throw");
      } catch (CodeExecuteException e) {
        assertEquals(e.getErrorInfo().getType(), INCLUDE_PARTIAL_MISSING);
      }
      assertSame(callerBuffer, ctx.buffer());
      assertEquals(ctx.buffer().toString(), "callerA ");

      // The partial's own output, written before it throws, must not leak
      // into the caller buffer.
      callerBuffer = new StringBuilder("caller");
      ctx = partialContext("{}", partials, false, compat, callerBuffer);
      try {
        ctx.execute(compiler().compile("A {.include pB} B", false, false, compat).code());
        fail("Expected the missing include to throw");
      } catch (CodeExecuteException e) {
        assertEquals(e.getErrorInfo().getType(), INCLUDE_PARTIAL_MISSING);
      }
      assertSame(callerBuffer, ctx.buffer());
      assertEquals(ctx.buffer().toString(), "callerA ");

      // The bad partial fails to compile. The buffer swap never happens and
      // the caller buffer stays intact.
      callerBuffer = new StringBuilder("caller");
      ctx = partialContext("{}", partials, false, compat, callerBuffer);
      try {
        ctx.execute(compiler().compile("A {.include pC} B", false, false, compat).code());
        fail("Expected the bad partial to throw");
      } catch (CodeExecuteException e) {
        assertEquals(e.getErrorInfo().getType(), INCLUDE_PARTIAL_SYNTAX);
      }
      assertSame(callerBuffer, ctx.buffer());
      assertEquals(ctx.buffer().toString(), "callerA ");
    }
  }

  /**
   * Build a context with a compiler, the given partials, a depth limit of
   * one, and includes enabled.
   */
  private Context partialContext(String jsonText, String partialsText, boolean safe, CompatLevel compat) {
    return partialContext(jsonText, partialsText, safe, compat, null);
  }

  /**
   * Build a context with a compiler, the given partials, a depth limit of
   * one, and includes enabled, starting with the given output buffer.
   */
  private Context partialContext(String jsonText, String partialsText, boolean safe, CompatLevel compat,
      StringBuilder buf) {
    Context ctx = new Context(JsonUtils.decode(jsonText), buf, null);
    ctx.setCompiler(compiler());
    ctx.setPartials(JsonUtils.decode(partialsText));
    ctx.setMaxPartialDepth(1);
    ctx.setEnableInclude(true);
    if (safe) {
      ctx.setSafeExecution();
    }
    ctx.setCompat(compat);
    return ctx;
  }

  @Test
  public void testIncludeTrailingSpaceLiteral() throws CodeException {
    // Probe X2 (qwen-findings 2.11): in "{.include }" the space is consumed by
    // matcher.space() so matcher.arguments() fails and the instruction degrades
    // to literal text. Safe compile: 0 errors, literal text in output.
    CompiledTemplate compiled = compiler().compile("{.include }", true, false);
    assertEquals(compiled.errors().size(), 0);
    Context ctx = compiler().newExecutor()
        .code(compiled.code())
        .json("{}")
        .execute();
    assertContext(ctx, "{.include }");
    assertEquals(ctx.getErrors().size(), 0);

    // A real partial behind the space still parses as an include; with the
    // output flag it renders, since includes suppress output by default.
    Context ctx2 = compiler().newExecutor()
        .code(compiler().compile("{.include pC output}").code())
        .json("{}")
        .partialsMap("{\"pC\": \"C\"}")
        .enableInclude(true)
        .execute();
    assertContext(ctx2, "C");
    assertEquals(ctx2.getErrors().size(), 0);
  }

}
