/**
 *  Copyright, 2015, Squarespace, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.squarespace.template.cli;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.testng.Assert;

import com.squarespace.template.compat.CompatLevel;
import org.testng.annotations.Test;


/**
 * Compile-mode tests for the templatec CLI: a non-zero exit code must be
 * returned whenever the template has compile-time or render-time errors.
 */
public class TemplateCTest {

  private int compile(String template) throws Exception {
    Path tmp = Files.createTempFile("templatec", ".html");
    try {
      Files.write(tmp, template.getBytes(StandardCharsets.UTF_8));
      return new TemplateC().compile(tmp.toString(), null, null, "en-US", false,
        CompatLevel.defaultLevel());
    } finally {
      Files.deleteIfExists(tmp);
    }
  }

  @Test
  public void testCleanTemplateExitZero() throws Exception {
    Assert.assertEquals(compile("{.section items}{@name}{.end}"), 0);
  }

  @Test
  public void testSyntaxErrorExitNonzero() throws Exception {
    // Broken template: safe-mode compile collects the error and the CLI
    // must exit nonzero instead of pretending success.
    Assert.assertEquals(compile("{.section items}"), 1);
  }

  @Test
  public void testUnknownFormatterExitNonzero() throws Exception {
    Assert.assertEquals(compile("{@value|nonsuch}"), 1);
  }

  @Test
  public void testRenderErrorExitNonzero() throws Exception {
    // Missing partial is a render-time error collected on the context.
    Assert.assertEquals(compile("{.include noSuchPartial}"), 1);
  }

  @Test
  public void testSingleCompilerPerRun() {
    // One Compiler per run so the partial compile cache is shared
    // between the compile and execute phases.
    TemplateC command = new TemplateC();
    Assert.assertSame(command.compiler(), command.compiler());
  }

}
