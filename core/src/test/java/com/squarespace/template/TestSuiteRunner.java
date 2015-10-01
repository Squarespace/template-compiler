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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.squarespace.template.TestCaseParser.TestCase;


/**
 * Runs a test suite where the test cases are parsed from
 * resource files using a {@link TestCaseParser}.
 */
public class TestSuiteRunner {

  private final TestCaseParser parser = new TestCaseParser();

  private final Compiler compiler;

  private final Class<?> resourceClass;

  public TestSuiteRunner(Compiler compiler, Class<?> resourceClass) {
    this.compiler = compiler;
    this.resourceClass = resourceClass;
  }

  public void run(String ... paths) {
    Map<String, AssertionError> errors = new HashMap<>();
    for (String path : paths) {
      runOne(path, errors);
    }
    assertPass(errors);
  }

  private void runOne(String path, Map<String, AssertionError> errors) {
    try {
      String source = loadResource(resourceClass, path);
      TestCase testCase = parser.parseTest(source);
      testCase.run(compiler);

    } catch (AssertionError e) {
      errors.put(path, e);
    } catch (CodeException e) {
      errors.put(path, new AssertionError(e.toString()));
    }
  }

  private void assertPass(Map<String, AssertionError> errors) {
    if (!errors.isEmpty()) {
      for (Map.Entry<String, AssertionError> entry : errors.entrySet()) {
        System.err.println("Case: " + entry.getKey());
        System.err.println(entry.getValue());
      }
      throw new AssertionError("Failed!");
    }
  }

  protected static String loadResource(Class<?> cls, String path) throws CodeException {
    try (InputStream stream = cls.getResourceAsStream(path)) {
      if (stream == null) {
        throw new CodeExecuteException(resourceLoadError(path, "not found"));
      }
      return IOUtils.toString(stream, "UTF-8");
    } catch (IOException e) {
      throw new CodeExecuteException(resourceLoadError(path, e.toString()));
    }
  }

  private static ErrorInfo resourceLoadError(String path, String message) {
    ErrorInfo info = new ErrorInfo(ExecuteErrorType.RESOURCE_LOAD);
    info.name(path);
    info.data(message);
    return info;
  }

}