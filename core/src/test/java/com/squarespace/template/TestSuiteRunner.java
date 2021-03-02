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

import static com.squarespace.template.GeneralUtils.loadResource;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.squarespace.template.TestCaseParser.TestCase;


/**
 * Runs a test suite where the test cases are parsed from
 * resource files using a {@link TestCaseParser}.
 */
public class TestSuiteRunner {

  private final Set<String> filesSeen = new HashSet<>();

  private final TestCaseParser parser = new TestCaseParser();

  private final Compiler compiler;

  private final Class<?> resourceClass;

  public TestSuiteRunner(Compiler compiler, Class<?> resourceClass) {
    this.compiler = compiler;
    this.resourceClass = resourceClass;
  }

  public void exec(String pattern) {
    // Match numeric pattern in filename
    Predicate<String> predicate = Pattern.compile("^" + pattern.replace("%N", "\\d+") + "$").asPredicate();

    // List all files relative to the resource class that match the numeric pattern
    List<Path> paths = GeneralUtils.list(this.resourceClass, p -> predicate.test(p.getFileName().toString()));

    Map<String, AssertionError> errors = new HashMap<>();
    for (Path path : paths) {
      String file = path.toString();
      if (filesSeen.contains(file)) {
        throw new AssertionError("Already processed file " + path);
      }
      runOne(file, errors);
      filesSeen.add(file);
    }
    assertPass(errors);
  }

  public void run(String... paths) {
    Map<String, AssertionError> errors = new HashMap<>();
    for (String path : paths) {
      if (filesSeen.contains(path)) {
        throw new AssertionError("Already processed file " + path);
      }
      runOne(path, errors);
      filesSeen.add(path);
    }
    assertPass(errors);
  }

  private void runOne(String path, Map<String, AssertionError> errors) {
    try {
      System.out.println("Running " + path);
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
        entry.getValue().printStackTrace();
      }
      throw new AssertionError("Failed!");
    }
  }

}