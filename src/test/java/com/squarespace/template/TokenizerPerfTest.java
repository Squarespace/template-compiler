package com.squarespace.template;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import com.squarespace.v6.utils.JSONUtils;


public class TokenizerPerfTest extends UnitTestBase {

  private static final double NANOSECONDS = 1_000_000.0;
  
  private String expand(String data, int times) {
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < times; i++) {
      buf.append(data);
    }
    return buf.toString();
  }
  
  @Test
  public void testSpeed() throws Exception {
    Thread.sleep(1);
    String data = "START {foo.bar} {.section foo}{bar} {d|date %c} {.repeated section list}A{@}{.end}{.end} END-";
    String jsonData = "{\"foo\": {\"bar\": 123}, \"d\": 1368160997000, \"list\": [1, 2, 3, 4, 5]}";
    for (int i = 2; i < (256 * 128); i *= 2 ) {
      String testData = expand(data, i);
      run(testData, jsonData);
    }
  }
  
  @Test
  public void testStatic() throws Exception {
    String template = read("/Users/phensley/jsont_testcases/adirondack-demo_35.html");
    String json = read("/Users/phensley/jsont_testcases/adirondack-demo_35.json");
    for (int i = 2; i < 10; i++) {
      template += template;
      run(template, json);
    }
  }
  
  private String read(String path) throws IOException {
    try (InputStream input = Files.newInputStream(Paths.get(path))) {
      return IOUtils.toString(input);
    }
  }
  
  private void run(String data, String jsonData) throws CodeException {
    JsonTemplateEngine compiler = compiler();
    String result = null;
    int iters = 20;
    double[] compTimes = new double[iters];
    double[] execTimes = new double[iters];
    int instructionCount = 0;
    for (int i = 0; i < iters; i++) {
      long start = System.nanoTime();
      CompiledTemplate script = compiler.compile(data);
      instructionCount = script.getMachine().getInstructionCount();
      long compileTime = System.nanoTime() - start;
      
      start = System.nanoTime();
      Context ctx = compiler.execute(script.getCode(), JSONUtils.decode(jsonData));
      result = ctx.buffer().toString();
      long executeTime = System.nanoTime() - start;
      
      compTimes[i] = compileTime / NANOSECONDS;
      execTimes[i] = executeTime / NANOSECONDS;
    }
    System.out.printf("input        %s chars\n", commas(data.length()));
    System.out.printf("output       %s chars\n",  commas(result.length()));
    System.out.printf("instructions %d\n", instructionCount);
    
    Arrays.sort(compTimes);
    Arrays.sort(execTimes);
    System.out.printf("compile + execute = total (ms):\n\n");
    System.out.printf("   %8.2f + %8.2f = %8.2f  lo\n            ...\n", compTimes[0], execTimes[0],
        (compTimes[0] + execTimes[0]));
    for (int i = 8; i < 13; i++) {
      double total = compTimes[i] + execTimes[i];
      System.out.printf("   %8.2f + %8.2f = %8.2f  %d\n", compTimes[i], execTimes[i], total, i);
    }
    System.out.printf("            ...\n   %8.2f + %8.2f = %8.2f  hi\n", compTimes[iters-1], execTimes[iters-1],
        (compTimes[iters-1] + execTimes[iters-1]));
    System.out.println("\n---------------------------------------");
    
  }
  
}
