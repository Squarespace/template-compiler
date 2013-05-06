package com.squarespace.template;

import java.util.Arrays;

import javax.inject.Inject;

import org.testng.annotations.BeforeMethod;

import com.google.inject.Module;
import com.ibm.icu.text.NumberFormat;
import com.squarespace.test.CommonsTestModules;
import com.squarespace.test.TesterBase;
import com.squarespace.v6.utils.template.NodeJsContext;
import com.squarespace.v6.utils.template.NodeJsService;
import com.squarespace.v6.utils.template.NodeJsWorkspaceService;


public class NodeJsSpeedTest extends TesterBase {

  @Inject
  private NodeJsWorkspaceService nodeJsWorkspace;

  @Inject
  private NodeJsService nodeJsService;

  private NodeJsContext nodeContext = new NodeJsContext() {
    @Override
    public String getContext() {
      return "speed test";
    }
  };
  
  @Override
  public Module getModule() {
    return CommonsTestModules.getUnitTestModule();
  }
  
  @BeforeMethod
  public void startNodeServices() throws Exception {
    nodeJsWorkspace.start();
    nodeJsService.start();
  }
  
  private String expand(String data, int times) {
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < times; i++) {
      buf.append(data);
    }
    return buf.toString();
  }

// DISABLED: used for development only
//  @Test
  public void testSpeed() throws Exception {
    String data = "START {foo.bar} {.section foo}{bar}{.end} {.repeated section list}A{@}{.end} END-";
    String jsonData = "{\"foo\": {\"bar\": 123}, \"list\": [1, 2, 3, 4, 5]}";
    for (int i = 2; i < (256 * 1024); i *= 2 ) {
      String testData = expand(data, i);
      run(testData, jsonData, false);
    }
    
  }

  public String commas(long num) {
    return NumberFormat.getInstance().format(num);
  }
  
  public String commas(double num) {
    return NumberFormat.getInstance().format(num);
  }

  private void run(String data, String jsonData, boolean verbose) throws Exception {
    int iters = 20;
    double[] times = new double[iters];
    double nanos = 1_000_000.0;
    String result = null;
    for (int i = 0; i < iters; i++) {
      long start = System.nanoTime();
      result = nodeJsService.expandJsonTemplate(nodeContext, data, jsonData);
      long executeTime = System.nanoTime() - start;
      times[i] = executeTime / nanos;
    }
    System.out.printf("input      %s chars\n", commas(data.length()));
    System.out.printf("output     %s chars\n", commas(result.length()));
    Arrays.sort(times);
    System.out.print("execute times (ms):\n\n");
    System.out.printf("   %10.2f  lo\n            ...\n", times[0]);
    for (int i = 8; i < 13; i++) {
      System.out.printf("   %10.2f  %d\n", times[i], i);
    }
    System.out.printf("            ...\n   %10.2f  hi\n", times[iters-1]);
    if (verbose) {
      System.out.println(" input: " + data);
      System.out.println("result: " + result);
    }
    System.out.println("\n----------------------------------------------");
  }
  
}
