package com.squarespace.template;

import javax.inject.Inject;

import org.apache.commons.lang3.StringEscapeUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Module;
import com.squarespace.test.CommonsTestModules;
import com.squarespace.test.TesterBase;
import com.squarespace.v6.utils.JSONUtils;
import com.squarespace.v6.utils.template.NodeJsContext;
import com.squarespace.v6.utils.template.NodeJsService;
import com.squarespace.v6.utils.template.NodeJsWorkspaceService;


public class NodeCompareTest extends TesterBase {

  private static final PredicateTable predicateTable = new PredicateTable();
  
  private static final FormatterTable formatterTable = new FormatterTable();
  
  static {
    predicateTable.register(new CorePredicates());
    formatterTable.register(new CoreFormatters());
  }
  
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

  @Test
  public void testCompare() throws Exception {
    String tmpl = "{.repeated section a}{b}{@index}{.end}";
    String json = "{\"a\": [1,2,3], \"b\": \"A\"}";
    
    System.out.println(runNode(tmpl, json));
    System.out.println(runParo(tmpl, json));
  }
  
  // DISABLED: development test to compare output
//  @Test
  public void testEquals() throws Exception {
    String template = "START {foo.bar}\n{.section foo}{bar}\n{.end}\n {.repeated section list}A{@}{.end} END-";
    String jsonData = "{\"foo\": {\"bar\": 123}, \"list\": [1, 2, \"a\", 4, 5]}";
//    String template = "{.repeated section key}{@}{.alternates with}-{.end}";
//    String jsonData = "{\"key\": [\"a\", \"b\", \"c\", \"d\", \"e\"]}";
    String res1 = runNode(template, jsonData);
    String res2 = runParo(template, jsonData);
    System.out.println("NODE: |" + StringEscapeUtils.escapeJava(res1) + "|");
    System.out.println("PARO: |" + StringEscapeUtils.escapeJava(res2) + "|");
  }
  
  private String runNode(String template, String jsonData) throws Exception {
    return nodeJsService.expandJsonTemplate(nodeContext, template, jsonData);
  }
  
  private String runParo(String template, String jsonData) throws CodeException {
    JsonTemplateEngine compiler = compiler();
    CompiledTemplate script = compiler.compile(template);
    Context ctx = compiler.execute(script.getCode(), JSONUtils.decode(jsonData));
    return ctx.buffer().toString();
  }
 
  public JsonTemplateEngine compiler() {
    return new JsonTemplateEngine(formatterTable, predicateTable);
  }

}
