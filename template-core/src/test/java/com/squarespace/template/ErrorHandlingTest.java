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
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

@Test(groups = { "unit" })
public class ErrorHandlingTest extends UnitTestBase {

  @Test
  public void testDummy() throws CodeException {
    // Here for completeness, code coverage
    assertContext(execute("{@|dummy}", "1"), "1");
  }

  @Test
  public void testUnexpected() throws CodeException {
    assertErrors("{@|npe}", "1", ExecuteErrorType.UNEXPECTED_ERROR);
    assertErrors("{@|unstable}", "1", ExecuteErrorType.UNEXPECTED_ERROR);
  }

  private void assertErrors(String template, String json, ErrorType expected) throws CodeSyntaxException {
    try {
      Context ctx = new Context(JsonUtils.decode(json));
      CodeMachine sink = machine();
      tokenizer(template, sink).consume();
      ctx.execute(sink.getCode());
      fail("expected CodeExecuteException!");

    } catch (CodeExecuteException e) {
      assertEquals(e.getErrorInfo().getType(), expected);
    }
  }
}
