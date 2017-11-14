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
package com.squarespace.template.plugins.platform.i18n;

import org.testng.annotations.Test;

import com.squarespace.template.TestSuiteRunner;
import com.squarespace.template.plugins.platform.PlatformUnitTestBase;


public class MessageFormatterTest extends PlatformUnitTestBase {

  private final TestSuiteRunner runner = new TestSuiteRunner(compiler(), MessageFormatterTest.class);

  @Test
  public void testMessageFormatter() throws Exception {
    runner.run(
        "f-message-named-args.html",
        "f-message-units-en-US.html",
        "f-message-units-fr-FR.html",
        "f-message-datetime-interval-en-US.html",
        "f-message-datetime-interval-fr-FR.html",
        "f-message-plural-en-US.html",
        "f-message-plural-fr-FR.html",
        "f-message-plural-pl-PL.html"
    );
  }

}
