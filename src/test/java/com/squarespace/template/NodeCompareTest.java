package com.squarespace.template;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Module;
import com.squarespace.test.CommonsTestModules;
import com.squarespace.test.TesterBase;
import com.squarespace.v6.utils.JSONUtils;
import com.squarespace.v6.utils.template.NodeJsContext;
import com.squarespace.v6.utils.template.NodeJsService;
import com.squarespace.v6.utils.template.NodeJsWorkspaceService;

import difflib.Chunk;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;


public class NodeCompareTest extends TesterBase {
  
  private static final Pattern SPACES = Pattern.compile("[ \r\t\f\b\u00a0\u200b]+");
  
  private static final Pattern SPACELINE = Pattern.compile("([ \t\r\f\b]+\n+)+");
  
  private static final Pattern LINESPACE = Pattern.compile("(\n+[ \t\r\f\b]+)+");

  private static final Pattern NEWLINES = Pattern.compile("\n+");

  private static final Pattern HTMLCOMMENTS = Pattern.compile("(?s)<!--.*-->");
  
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
    
    System.out.println(runNode(tmpl, json, "{}"));
    System.out.println(runParo(tmpl, json, "{}"));
  }
  
  // DISABLED: development test to compare output. will expand later.
  @Test
  public void testEquals() throws Exception {
    String template = "{.repeated section key}{@}{.alternates with}-{.end}";
    String json = "{\"key\": [\"a\", \"b\", \"c\", \"d\", \"e\"]}";
    compare(template, json, "{}");

    template = "START {foo.bar}\n{.section foo}{bar}\n{.end}\n {.repeated section list}A{@}{.end} END-";
    json = "{\"foo\": {\"bar\": 123}, \"list\": [1, 2, \"a\", 4, 5]}";
    compare(template, json, "{}");
}
  
  @Test
  public void testRealTemplates() throws Exception {
    Path root = Paths.get("/Users/phensley/jsont_testcases");
    DirectoryStream<Path> ds = Files.newDirectoryStream(root, "*.html");
    for (Path path : ds) {
      Path name = path.getFileName();
      if (!name.toString().equals("jirick-demo_215.html")) {
        continue;
      }
      String[] jsonPath = name.toString().split("\\.");
      String template = UnitTestBase.readFile(path);
      String json = UnitTestBase.readFile(root.resolve(jsonPath[0] + ".json"));
      String partials = UnitTestBase.readFile(root.resolve(jsonPath[0] + "-partials.json"));
      System.out.println(name);
      compare(template, json, partials);
    }
  }
  
  private void compare(String template, String json, String partials) {
    String nodeRes;
    String paroRes;
    try {
      nodeRes = runNode(template, json, partials);
      paroRes = runParo(template, json, partials);
    } catch (Exception e) {
      System.out.println("  ERROR: " + e.getMessage());
      return;
    }
    if (!differs(nodeRes, paroRes)) {
      System.out.println("IDENTICAL!");
    }
    System.out.println("\n");
  }
  
  private boolean differs(String nodeRes, String paroRes) {
    nodeRes = normalize(nodeRes);
    paroRes = normalize(paroRes);
    if (compress(nodeRes).equals(compress(paroRes))) {
      return false;
    }
    List<String> nodeLines = Arrays.asList(StringUtils.split(nodeRes, '\n'));
    List<String> paroLines = Arrays.asList(StringUtils.split(paroRes, '\n'));
    Patch patch = DiffUtils.diff(nodeLines, paroLines);
    List<Delta> deltas = patch.getDeltas();
    boolean emitted = false;
    if (deltas.size() > 0) {
      for (int i = 0; i < deltas.size(); i++) {
        Delta delta = deltas.get(i);
        if (!chunksDiffer(delta.getOriginal(), delta.getRevised())) {
          System.out.println("LINE " + (i + 1) + " identical (ignoring whitespace)");
          System.out.println();
          continue;
        }
        emitted = true;
        System.out.println("LINE " + (i + 1) + " " + delta.getType());
        System.out.println("  " + delta.getOriginal());
        System.out.println("  " + delta.getRevised());
        System.out.println();
      }
      if (emitted) {
        System.out.println();
        System.out.println("  NODE: |" + StringEscapeUtils.escapeJava(compress(nodeRes)) + "|");
        System.out.println("  PARO: |" + StringEscapeUtils.escapeJava(compress(paroRes)) + "|");
        return true;
      } else {
        return false;
      }
    }
    return false;
  }
  
  private String compress(String str) {
    return Patterns.WHITESPACE_NBSP.matcher(str).replaceAll(" ");
  }
  
  private String concat(Chunk chunk) {
    StringBuilder buf = new StringBuilder();
    for (Object obj : chunk.getLines()) {
      buf.append(obj.toString());
    }
    return buf.toString();
  }
  
  private boolean chunksDiffer(Chunk chunk1, Chunk chunk2) {
    String str1 = concat(chunk1);
    String str2 = concat(chunk2);
    return !StringUtils.difference(compress(str1), compress(str2)).equals("");
  }
  
  private String normalize(String str) {
    str = SPACES.matcher(str).replaceAll("  ");
    str = SPACELINE.matcher(str).replaceAll(" \n");
    str = LINESPACE.matcher(str).replaceAll("\n ");
    str = NEWLINES.matcher(str).replaceAll("\n");
    str = SPACES.matcher(str).replaceAll("  ");
//    str = HTMLCOMMENTS.matcher(str).replaceAll("");
    return str;
  }
  
  private String escape(String str) {
    return StringEscapeUtils.escapeJava(str);
  }
  
  @Test
  public void testFoo() {
    System.out.println(escape(normalize("foo bar \n \n\n baz")));
    System.out.println(escape(normalize("foo \nbar \n \n\n baz")));
    System.out.println(escape(normalize("foo\n bar \n \n\n baz")));
  }
  
  private String runNode(String template, String json, String partials) throws Exception {
    return nodeJsService.expandJsonTemplateWithPartials(nodeContext, template, json, partials);
  }
  
  private String runParo(String template, String json, String partials) throws CodeException {
    JsonTemplateEngine compiler = UnitTestBase.compiler();
    CompiledTemplate script = compiler.compile(template);
    JsonNode jsonNode = JSONUtils.decode(json);
    JsonNode partialsNode = JSONUtils.decode(partials);
    Context ctx = compiler.executeWithPartials(script.getCode(), jsonNode, partialsNode);
    return ctx.buffer().toString();
  }
 
}
