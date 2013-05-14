package com.squarespace.template;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.template.Instructions.TextInst;
import com.squarespace.template.plugins.CoreFormatters;
import com.squarespace.template.plugins.CorePredicates;
import com.squarespace.template.plugins.MediaFormatters;
import com.squarespace.template.plugins.MediaPredicates;
import com.squarespace.v6.utils.JSONUtils;


public class ParoSpeedTest extends UnitTestBase {

  private static final PredicateTable predicateTable = new PredicateTable();
  
  private static final FormatterTable formatterTable = new FormatterTable();
  
  static {
    predicateTable.register(new CorePredicates());
    predicateTable.register(new MediaPredicates());
    
    formatterTable.register(new CoreFormatters());
    formatterTable.register(new MediaFormatters());
  }

  private static final JsonTemplateEngine PARO = new JsonTemplateEngine(formatterTable, predicateTable);

  @Test
  public void testSpeed() throws Exception {
    double NANOS_PER_MS = 1_000_000.0;
    Thread.sleep(1);
    Path root = Paths.get("/Users/phensley/jsont_testcases");
    DirectoryStream<Path> ds = Files.newDirectoryStream(root, "*.html");
    for (Path path : ds) {
      Path name = path.getFileName();
      String[] jsonPath = name.toString().split("\\.");
      String template = read(path);
      String json = read(root.resolve(jsonPath[0] + ".json"));
      String partials = read(root.resolve(jsonPath[0] + "-partials.json"));
  
      long now;
      long elapsed;
      int iters = 1000;

      System.out.println("FILE: " + name);
      System.out.print("decode json: " + json.length() + " chars");
      System.out.println(", decode partials: " + partials.length() + " chars");
      now = System.nanoTime();
      for (int i = 0; i < iters; i++) {
        JSONUtils.decode(json);
        JSONUtils.decode(partials);
      }
      elapsed = System.nanoTime() - now;
      System.out.printf("elapsed: %6.2f ms per iter\n\n", (elapsed / NANOS_PER_MS / (double)iters));
      
      JsonNode jsonNode = JSONUtils.decode(json);
      JsonNode partialsNode = JSONUtils.decode(partials);
      String error = null;
      System.out.println("tokenize/compile/execute: " + template.length() + " chars");
      try {
        CompiledTemplate script = PARO.compile(template);
        System.out.println("instruction count: " + script.getMachine().getInstructionCount());
        int textChars = countTextChars(template);
        int count = template.length() - textChars;
        System.out.println("template text: " + textChars + " chars.");
        System.out.printf("template overhead: %.1f %%\n", (count * 100.0 / (float)template.length()));
      } catch (CodeSyntaxException e) {
      }
      now = System.nanoTime();
      for (int i = 0; i < iters; i++) {
        try {
          runParo(template, jsonNode, partialsNode);
        } catch (Exception e) {
          error = e.getMessage();
        }
      }
      elapsed = System.nanoTime() - now;
      if (error != null) System.out.println("error: " + error);
      System.out.printf("elapsed: %6.2f ms per iter\n\n", (elapsed / NANOS_PER_MS / (double)iters));
      
    }
  }

  private String runParo(String template, JsonNode json, JsonNode partials) throws Exception {
    CompiledTemplate script = PARO.compile(template);
    Context ctx = PARO.executeWithPartials(script.getCode(), json, partials);
    return ctx.buffer().toString();
  }

  private int countTextChars(String template) throws Exception {
    int chars = 0;
    for (Instruction inst : PARO.tokenize(template).getInstructions()) {
      if (InstructionType.TEXT.equals(inst.getType())) {
        chars += ((TextInst)inst).getView().length();
      }
    }
    return chars;
  }
  
  private String read(Path path) throws IOException {
    try (InputStream input = Files.newInputStream(path)) {
      return IOUtils.toString(input);
    }
  }
}
