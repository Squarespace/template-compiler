package com.squarespace.template;

import static com.squarespace.template.UnitTestBase.readFile;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.template.Instructions.TextInst;
import com.squarespace.v6.utils.JSONUtils;


public class ParoSpeedTest {

  private static final JsonTemplateEngine PARO = UnitTestBase.compiler();
  
  @Test
  public void testSpeed() throws Exception {
    double NANOS_PER_MS = 1_000_000.0;
    Thread.sleep(1);
    CodeStats stats = new CodeStats();
    Path root = Paths.get("/Users/phensley/jsont_testcases");
    DirectoryStream<Path> ds = Files.newDirectoryStream(root, "*.html");
    for (Path path : ds) {
      Path name = path.getFileName();
      String[] jsonPath = name.toString().split("\\.");
      String template = readFile(path);
      String json = readFile(root.resolve(jsonPath[0] + ".json"));
      String partials = readFile(root.resolve(jsonPath[0] + "-partials.json"));
  
      long now;
      long elapsed;
      int iters = 1;

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
      Exception error = null;
      System.out.println("tokenize/compile/execute: " + template.length() + " chars");
      try {
        PARO.tokenize(template, stats);
        CompiledTemplate script = PARO.compile(template);
        System.out.println("instruction count: " + script.getMachine().getInstructionCount());
        int textChars = countTextChars(template);
        int count = template.length() - textChars;
        System.out.println("template text: " + textChars + " chars.");
        System.out.printf("template overhead: %.1f %%\n", (count * 100.0 / (float)template.length()));
      } catch (CodeSyntaxException e) {
      }
      String result = "";
      now = System.nanoTime();
      for (int i = 0; i < iters; i++) {
        try {
          result = runParo(template, jsonNode, partialsNode);
        } catch (Exception e) {
          error = e;
        }
      }
      elapsed = System.nanoTime() - now;
      if (error != null) {
        error.printStackTrace();
      }
      System.out.printf("elapsed: %6.2f ms per iter\n\n", (elapsed / NANOS_PER_MS / (double)iters));
      System.out.printf("result length: %d\n", result.length());
      result = result.substring(0, Math.min(256, result.length()));
      System.out.printf("result: %s\n", StringEscapeUtils.escapeJava(result));
    }
    showReport(stats);
  }

  private void showReport(CodeStats stats) {
    System.out.println("==========================================");
    System.out.println("INSTRUCTIONS:");
    for (Map.Entry<InstructionType, Integer> entry : stats.getInstructionCounts().entrySet()) {
      System.out.printf("%30s: %8d\n", entry.getKey().name(), entry.getValue());
    }
    System.out.println();
    System.out.println("FORMATTERS:");
    for (Map.Entry<String, Integer> entry : stats.getFormatterCounts().entrySet()) {
      System.out.printf("%30s: %8d\n", entry.getKey(), entry.getValue());
    }
    
    System.out.println();
    System.out.println("Not executed:");
    String[] formatters = UnitTestBase.formatterTable().getSymbols();
    Arrays.sort(formatters);
    for (String name : formatters) {
      if (!stats.getFormatterCounts().containsKey(name)) {
        System.out.printf("  %s\n", name);
      }
    }

    System.out.println();
    System.out.println("PREDICATES:");
    for (Map.Entry<String, Integer> entry : stats.getPredicateCounts().entrySet()) {
      if (entry == null || entry.getKey() == null) {
        System.out.println("NULL entry");
        continue;
      }
      System.out.printf("%30s: %8d\n", entry.getKey(), entry.getValue());
    }
    
    System.out.println();
    System.out.println("Not executed:");
    String[] predicates = UnitTestBase.predicateTable().getSymbols();
    Arrays.sort(predicates);
    for (String name : predicates) {
      if (!stats.getPredicateCounts().containsKey(name)) {
        System.out.printf("  %s\n", name);
      }
    }
    
  }
  
  private String runParo(String template, JsonNode json, JsonNode partials) throws Exception {
    CompiledTemplate script = PARO.compile(template);
    Context ctx = PARO.execute(script.getCode(), json, partials);
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
  
}
