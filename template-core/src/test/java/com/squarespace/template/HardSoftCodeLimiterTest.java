/**
 * Copyright (c) 2015 SQUARESPACE, Inc.
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

import static com.squarespace.template.ExecuteErrorType.CODE_LIMIT_REACHED;
import static com.squarespace.template.ExecuteErrorType.UNEXPECTED_ERROR;
import static com.squarespace.template.plugins.CoreFormatters.APPLY;
import static com.squarespace.template.plugins.CoreFormatters.TRUNCATE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.template.HardSoftCodeLimiter.Limit;
import com.squarespace.template.Instructions.RootInst;


public class HardSoftCodeLimiterTest extends UnitTestBase {

  @Test
  public void testNoLimit() throws CodeException {
    JsonNode node = JsonUtils.decode("[0,1,2,3,4,5,6,7,8,9]");
    Context ctx = new Context(node);
    RootInst root = builder().repeated("@").var("@").end().eof().build();
    ctx.execute(root);
    assertEquals(ctx.buffer().toString(), "0123456789");
  }

  @Test
  public void testSoftLimit() throws CodeException {
    TestHandler handler = new TestHandler(Limit.SOFT, 6);
    CodeLimiter limiter = HardSoftCodeLimiter.builder()
        .setSoftLimit(5)
        .setResolution(1)
        .setHandler(handler)
        .build();

    JsonNode node = JsonUtils.decode("[0,1,2,3,4,5,6,7,8,9]");
    Context ctx = new Context(node);
    ctx.setCodeLimiter(limiter);
    RootInst root = builder().repeated("@").var("@").end().eof().build();
    ctx.execute(root);
    assertTrue(handler.wasCalled());
  }

  @Test
  public void testFormatter() throws CodeException {
    TestHandler handler = new TestHandler(Limit.SOFT, 6);
    CodeLimiter limiter = HardSoftCodeLimiter.builder()
        .setSoftLimit(5)
        .setResolution(1)
        .setHandler(handler)
        .build();

    JsonNode node = JsonUtils.decode("[\"abc\",\"def\",\"ghi\"]");
    Context ctx = new Context(node);
    ctx.setCodeLimiter(limiter);
    CodeMaker mk = maker();
    // Instruction count: root repeated[N] { var truncate }
    RootInst root = builder()
        .repeated("@").var("@", mk.fmt(TRUNCATE, mk.args(" 2")))
        .end().eof().build();
    ctx.execute(root);

    assertTrue(handler.wasCalled());
    assertEquals(ctx.buffer().toString(), "ab...de...gh...");
    assertEquals(limiter.instructionCount(), 8);
  }

  @Test
  public void testApplyPartialSubcontext() throws CodeException {
    TestHandler handler = new TestHandler(Limit.SOFT, 6);
    CodeLimiter limiter = HardSoftCodeLimiter.builder()
        .setSoftLimit(5)
        .setResolution(1)
        .setHandler(handler)
        .build();

    JsonNode node = JsonUtils.decode("[0,1,2]");
    JsonNode partials = JsonUtils.decode("{\"foo\":\"{@}\"}");
    Context ctx = new Context(node);
    ctx.setPartials(partials);
    ctx.setCompiler(compiler());
    ctx.setCodeLimiter(limiter);

    CodeMaker mk = maker();
    // Instruction count: root repeated[N] { var apply { root var } }
    RootInst root = builder()
        .repeated("@").var("@", mk.fmt(APPLY, mk.args(" foo")))
        .end().eof().build();
    ctx.execute(root);

    assertTrue(handler.wasCalled());
    assertEquals(ctx.buffer().toString(), "012");
    assertEquals(limiter.instructionCount(), 14);
  }

  @Test
  public void testHardLimit() throws CodeException {
    TestHandler handler = new TestHandler(Limit.HARD, 6);
    CodeLimiter limiter = HardSoftCodeLimiter.builder()
        .setHardLimit(5)
        .setResolution(1)
        .setHandler(handler)
        .build();

    JsonNode node = JsonUtils.decode("[0,1,2,3,4,5,6,7,8,9]");
    Context ctx = new Context(node);
    ctx.setCodeLimiter(limiter);
    RootInst root = builder().repeated("@").var("@").end().eof().build();
    try {
      ctx.execute(root);
      fail("Expected CODE_LIMIT_REACHED exception");

    } catch (CodeExecuteException e) {
      assertTrue(handler.wasCalled());
      assertEquals(handler.limitType(), Limit.HARD);
      assertEquals(e.getErrorInfo().getType(), CODE_LIMIT_REACHED);
      assertEquals(limiter.instructionCount(), 6);
    }
  }

  @Test
  public void testResolution() throws CodeException {
    TestHandler handler = new TestHandler(Limit.HARD, 6);
    CodeLimiter limiter = HardSoftCodeLimiter.builder()
        .setHardLimit(4)
        .setResolution(3)
        .setHandler(handler)
        .build();
    JsonNode node = JsonUtils.decode("[0,1,2,3,4,5,6,7,8,9]");
    Context ctx = new Context(node);
    ctx.setCodeLimiter(limiter);
    RootInst root = builder().repeated("@").var("@").end().eof().build();
    try {
      ctx.execute(root);
      fail("Expected CODE_LIMIT_REACHED exception");

    } catch (CodeExecuteException e) {
      assertTrue(handler.wasCalled());
      assertEquals(handler.limitType(), Limit.HARD);
      assertEquals(e.getErrorInfo().getType(), CODE_LIMIT_REACHED);
      assertEquals(limiter.instructionCount(), 6);
    }
  }

  private static class TestHandler implements HardSoftCodeLimiter.Handler {

    private final Limit limitType;

    private final int assertLimit;

    private boolean wasCalled;

    public TestHandler(Limit limitType, int assertLimit) {
      this.limitType = limitType;
      this.assertLimit = assertLimit;
    }

    @Override
    public void onLimit(Limit limit, HardSoftCodeLimiter limiter) throws CodeExecuteException {
      if (wasCalled) {
        throw new CodeExecuteException(new ErrorInfo(UNEXPECTED_ERROR));
      }
      assertEquals(limiter.instructionCount(), assertLimit);
      this.wasCalled = true;
      if (limit.equals(Limit.HARD)) {
        throw new CodeExecuteException(new ErrorInfo(CODE_LIMIT_REACHED));
      }
    }

    public Limit limitType() {
      return limitType;
    }

    public boolean wasCalled() {
      return wasCalled;
    }

  }

}
