/**
 * Copyright (c) 2017 SQUARESPACE, Inc.
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

import org.testng.annotations.Test;


/**
 * Parse and render the template and ensure the output matches
 * the original source.
 */
public class RoundTripTest extends UnitTestBase {

  @Test
  public void testRoundTrip() throws CodeException {
    run("roundtrip-1.html");
    run("wright.html");
  }

  private void run(String path) throws CodeException {
    String expected = GeneralUtils.loadResource(RoundTripTest.class, path);
    CodeMachine sink = new CodeMachine();
    Tokenizer tokenizer = new Tokenizer(expected, sink, formatterTable(), predicateTable());
    tokenizer.setValidate();
    tokenizer.consume();

    String actual = ReprEmitter.get(sink.getCode(), true);
    if (!actual.equals(expected)) {
      throw new AssertionError("Output does not match:\n" + TestCaseParser.diff(expected, actual));
    }
  }

}
