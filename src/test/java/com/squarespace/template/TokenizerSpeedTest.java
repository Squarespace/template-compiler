package com.squarespace.template;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.squarespace.v6.utils.JSONUtils;


public class TokenizerSpeedTest extends UnitTestBase {

//  @Test
//  public void testBase() throws CodeSyntaxException {
////    String data = "{foo|pluralize/foo/bar}";
//    String data = "{.repeat \n section \t foo \t\n}";
//    InstructionCollector collector = collector();
//    Tokenizer tok = tokenizer(data, collector);
//    tok.consume();
//    for (Instruction inst : collector.getInstructions()) {
//      System.out.println(inst);
//    }
//  }

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
    String data = "START {foo.bar} {.section foo}{bar}{.end} {.repeated section list}A{@}{.end} END-";
    String jsonData = "{\"foo\": {\"bar\": 123}, \"list\": [1, 2, 3, 4, 5]}";
    for (int i = 2; i < (256 * 1024); i *= 2 ) {
      String testData = expand(data, i);
      run(testData, jsonData, false);
    }
  }
  
//  @Test
//  public void testScale() throws Exception {
//    System.out.println(System.currentTimeMillis() + " " + System.nanoTime());
//    Thread.sleep(100);
//    System.out.println(System.currentTimeMillis() + " " + System.nanoTime());
//  }
  
  private void run(String data, String jsonData, boolean verbose) throws CodeException {
    System.gc();
    System.gc();
//    System.out.printf("mem before    %s\n", Runtime.getRuntime().totalMemory());
    double nanos = 1_000_000.0;
    String result = null;
    int iters = 20;
    double[] times = new double[iters];
    for (int i = 0; i < iters; i++) {
      
      
      long start = System.nanoTime();
      JsonTemplateEngine compiler = compiler();
      CompiledTemplate script = compiler.compile(data);
      long compileTime = System.nanoTime() - start;
      
      start = System.nanoTime();
      Context ctx = compiler.execute(script.getCode(), JSONUtils.decode(jsonData));
      result = ctx.getBuffer().toString();
      long executeTime = System.nanoTime() - start;
      
      times[i] = (compileTime + executeTime) / nanos;
    }
    System.out.printf("input        %s chars\n", commas(data.length()));
    System.out.printf("output       %s chars\n",  commas(result.length()));
    
//    System.out.printf("compile       %.2f ms\n", compileTime / nanos);
//    System.out.printf("execute       %.2f ms\n", executeTime / nanos);
    
    Arrays.sort(times);
    System.out.printf("execute times (ms):\n\n");
    System.out.printf("   %10.2f  lo\n            ...\n", times[0]);
    for (int i = 8; i < 13; i++) {
      System.out.printf("   %10.2f  %d\n", times[i], i);
    }
    System.out.printf("            ...\n   %10.2f  hi\n", times[iters-1]);
    
//    int instructionCount = script.getMachine().getInstructionCount();
//    System.out.printf(" inst. count  %s\n\n", commas(instructionCount));

//    double compileRate = data.length() / (compileTime / nanos / 1000.0);
//    System.out.printf(" compiled     %s chars/s\n", commas((long)compileRate));
//    double executeRate = instructionCount / (executeTime / nanos / 1000.0);
//    System.out.printf(" executed     %s instructions/s\n\n", commas((long)executeRate));
//    System.out.printf("mem after     %s\n", Runtime.getRuntime().totalMemory());
    if (verbose) {
      System.out.println("input: " + data);
      System.out.println("result: " + result);
    }
    System.out.println("\n---------------------------------------");
    
  }
  
}
