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

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squarespace.template.BuildProperties;
import com.squarespace.template.CodeException;
import com.squarespace.template.CompiledTemplate;
import com.squarespace.template.Compiler;
import com.squarespace.template.Context;
import com.squarespace.template.FormatterTable;
import com.squarespace.template.GeneralUtils;
import com.squarespace.template.Instruction;
import com.squarespace.template.JsonUtils;
import com.squarespace.template.PredicateTable;
import com.squarespace.template.ReferenceScanner;
import com.squarespace.template.TreeEmitter;
import com.squarespace.template.plugins.CoreFormatters;
import com.squarespace.template.plugins.CorePredicates;
import com.squarespace.template.plugins.platform.CommerceFormatters;
import com.squarespace.template.plugins.platform.CommercePredicates;
import com.squarespace.template.plugins.platform.ContentFormatters;
import com.squarespace.template.plugins.platform.ContentPredicates;
import com.squarespace.template.plugins.platform.SlidePredicates;
import com.squarespace.template.plugins.platform.SocialFormatters;
import com.squarespace.template.plugins.platform.SocialPredicates;
import com.squarespace.template.plugins.platform.i18n.InternationalFormatters;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;


/**
 * Command line template compiler.
 */
public class TemplateC {

  private static final String TEMPLATE_REPOSITORY = "http://github.com/squarespace/squarespace-template";

  private static final String PROGRAM_NAME = "templatec";

  public static void main(String[] args) {
    TemplateC command = new TemplateC();
    command.execute(args);
  }

  protected void execute(String[] args) {
    String version = buildVersion();
    ArgumentParser parser = ArgumentParsers.newArgumentParser(PROGRAM_NAME)
      .description("Compile template files")
      .version(version);

    parser.addArgument("--version", "-v")
      .action(Arguments.version())
      .help("Show the version and exit");

    parser.addArgument("--stats", "-s")
      .action(Arguments.storeTrue())
      .help("Dump stats for the template");

    parser.addArgument("--tree", "-t")
      .action(Arguments.storeTrue())
      .help("Dump the syntax tree for the template");

    parser.addArgument("--json", "-j")
      .type(String.class)
      .help("JSON tree");

    parser.addArgument("--partials", "-p")
      .type(String.class)
      .help("JSON partials");

    parser.addArgument("template")
    .type(String.class)
    .help("Template source");

    int exitCode = 1;
    try {
      Namespace res = parser.parseArgs(args);
      if (res.getBoolean("stats")) {
        exitCode = stats(res.getString("template"));

      } else if (res.getBoolean("tree")) {
        exitCode = tree(res.getString("template"));

      } else {
        exitCode = compile(res.getString("template"), res.getString("json"), res.getString("partials"));
      }

    } catch (CodeException | IOException e) {
      System.err.println(e.getMessage());

    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.exit(1);

    } finally {
      System.exit(exitCode);
    }
  }

  /**
   * Compile a template against a given json tree and emit the result.
   */
  protected int compile(String templatePath, String jsonPath, String partialsPath)
      throws CodeException, IOException {

    String template = readFile(templatePath);
    String json = "{}";
    if (jsonPath != null) {
      json = readFile(jsonPath);
    }

    String partials = null;
    if (partialsPath != null) {
      partials = readFile(partialsPath);
    }

    CompiledTemplate compiled = compiler().compile(template, false);
    Instruction code = compiled.code();

    // Parse the JSON context
    JsonNode jsonTree = null;
    try {
      jsonTree = JsonUtils.decode(json);
    } catch (IllegalArgumentException e) {
      System.err.println("Caught error trying to parse JSON: " + e.getCause().getMessage());
      return 1;
    }

    // Parse the optional JSON partials dictionary.
    JsonNode partialsTree = null;
    if (partials != null) {
      try {
        partialsTree = JsonUtils.decode(partials);
        if (!(partialsTree instanceof ObjectNode)) {
          System.err.println("Partials map JSON must be an object. Found " + partialsTree.getNodeType());
          return 1;
        }
      } catch (IllegalArgumentException e) {
        System.err.println("Caught error trying to parse partials: " + e.getCause().getMessage());
        return 1;
      }
    }

    // Perform the compile.
    Context context = compiler().newExecutor()
        .code(code)
        .json(jsonTree)
        .partialsMap((ObjectNode)partialsTree)
        .execute();

    // If compile was successful, print the output.
    System.out.print(context.buffer().toString());
    return 0;
  }

  /**
   * Scan the compiled template and print statistics.
   */
  protected int stats(String templatePath) throws CodeException, IOException {
    String template = readFile(templatePath);

    CompiledTemplate compiled = compiler().compile(template, false);
    ReferenceScanner scanner = new ReferenceScanner();
    scanner.extract(compiled.code());
    ObjectNode report = scanner.references().report();
    String result = GeneralUtils.jsonPretty(report);
    System.out.println(result);
    return 0;
  }

  /**
   * Print the syntax tree for the given template.
   */
  protected int tree(String templatePath) throws CodeException, IOException {
    String template = readFile(templatePath);

    CompiledTemplate compiled = compiler().compile(template, false);
    StringBuilder buf = new StringBuilder();
    TreeEmitter.emit(compiled.code(), 0, buf);
    System.out.println(buf.toString());
    return 0;
  }

  protected Compiler compiler() {
    FormatterTable formatterTable = new FormatterTable();
    PredicateTable predicateTable = new PredicateTable();

    // core
    formatterTable.register(new CoreFormatters());
    predicateTable.register(new CorePredicates());

    // TODO: dynamic classpath scan and registration of plugin jars.

    // squarespace
    formatterTable.register(new CommerceFormatters());
    formatterTable.register(new ContentFormatters());
    formatterTable.register(new SocialFormatters());
    formatterTable.register(new InternationalFormatters());

    predicateTable.register(new CommercePredicates());
    predicateTable.register(new ContentPredicates());
    predicateTable.register(new SlidePredicates());
    predicateTable.register(new SocialPredicates());

    return new Compiler(formatterTable, predicateTable);
  }

  protected String readFile(String rawPath) throws IOException {
    try (Reader reader = new InputStreamReader(new FileInputStream(rawPath), "UTF-8")) {
      return IOUtils.toString(reader);
    }
  }

  /**
   * Build the version string.
   */
  protected String buildVersion() {
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
