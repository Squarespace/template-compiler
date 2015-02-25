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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import org.apache.commons.io.IOUtils;

import com.squarespace.template.BuildProperties;
import com.squarespace.template.CodeException;
import com.squarespace.template.CompiledTemplate;
import com.squarespace.template.Context;
import com.squarespace.template.FormatterTable;
import com.squarespace.template.Instruction;
import com.squarespace.template.JsonTemplateEngine;
import com.squarespace.template.JsonUtils;
import com.squarespace.template.PredicateTable;
import com.squarespace.template.plugins.CoreFormatters;
import com.squarespace.template.plugins.CorePredicates;


/**
 * Command line template compiler.
 *
 * TODO: move to separate gradle subproject
 */
public class TemplateC {

  private static final String TEMPLATE_REPOSITORY = "http://github.com/squarespace/squarespace-template";

  private static final String PROGRAM_NAME = "templatec";

  private static final PredicateTable predicateTable = new PredicateTable();

  private static final FormatterTable formatterTable = new FormatterTable();

  static {
    predicateTable.register(new CorePredicates());
    formatterTable.register(new CoreFormatters());
  }

  public static void main(String[] args) {
    String version = buildVersion();
    ArgumentParser parser = ArgumentParsers.newArgumentParser(PROGRAM_NAME)
      .description("Complete template files")
      .version(version);

    parser.addArgument("--version", "-v")
      .action(Arguments.version())
      .help("Show the version and exit");

    parser.addArgument("template")
      .type(String.class)
      .help("Template source");

    parser.addArgument("json")
      .type(String.class)
      .help("JSON tree");

    parser.addArgument("--partials", "-p")
      .type(String.class)
      .help("JSON partials");

    try {
      Namespace res = parser.parseArgs(args);
      compile(res.getString("template"), res.getString("json"), res.getString("partials"));

    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.exit(1);
    }

  }

  /**
   * Compile a template against a given json tree and emit the result.
   */
  private static void compile(String templatePath, String jsonPath, String partialsPath) {
    int exitCode = 1;
    try {
      String template = readFile(templatePath);
      String json = readFile(jsonPath);

      String partials = null;
      if (partialsPath != null) {
        partials = readFile(partialsPath);
      }

      CompiledTemplate compiled = compiler().compile(template);
      Instruction code = compiled.code();

      Context context = null;
      if (partials == null) {
        context = compiler().execute(code, JsonUtils.decode(json));
      } else {
        context = compiler().execute(code, JsonUtils.decode(json), JsonUtils.decode(partials));
      }

      System.out.print(context.buffer().toString());
      exitCode = 0;

    } catch (CodeException e) {
      System.err.println(e.getMessage());

    } catch (Exception e) {
      e.printStackTrace();

    } finally {
      System.exit(exitCode);
    }
  }

  private static JsonTemplateEngine compiler() {
    return new JsonTemplateEngine(formatterTable, predicateTable);
  }

  private static String readFile(String rawPath) throws IOException {
    try (Reader reader = new InputStreamReader(new FileInputStream(rawPath), "UTF-8")) {
      return IOUtils.toString(reader);
    }
  }

  /**
   * Build the version string.
   */
  private static String buildVersion() {
    StringBuilder buf = new StringBuilder();
    buf.append("${prog} version ")
      .append(BuildProperties.version())
      .append('\n');
    buf.append("      repository: ").append(TEMPLATE_REPOSITORY).append('\n');
    buf.append("      build date: ").append(BuildProperties.date()).append('\n');
    buf.append("    build commit: ").append(BuildProperties.commit()).append('\n');
    return buf.toString();
  }

}
