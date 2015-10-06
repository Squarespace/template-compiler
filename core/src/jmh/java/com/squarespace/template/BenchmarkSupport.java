package com.squarespace.template;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.template.plugins.PluginUtils;


public class BenchmarkSupport implements FormatterRegistry {

  @Override
  public void registerFormatters(SymbolTable<StringView, Formatter> table) {
    table.add(new EmbeddedFormatter());
    table.add(new StaticFormatter());
  }

  public static class EmbeddedFormatter extends BaseFormatter {

    private Instruction template;

    public EmbeddedFormatter() {
      super("embedded", false);
    }

    @Override
    public void initialize(Compiler compiler) throws CodeException {
      String source = GeneralUtils.loadResource(BenchmarkSupport.class, "embedded.html");
      this.template = compiler.compile(source).code();
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      return GeneralUtils.executeTemplate(ctx, template, node, false);
    }

  }

  public static class StaticFormatter extends BaseFormatter {

    public StaticFormatter() {
      super("static", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      StringBuilder buf = new StringBuilder();
      String name = node.path("name").asText();
      int age = node.path("age").asInt();
      buf.append("<span class=\"foo\">\n");
      if (node.path("enterBlock").asBoolean()) {
        buf.append("<div id=\"in-block\" data-name=\"");
        PluginUtils.escapeHtmlAttribute(name, buf);
        buf.append("\">");
        buf.append(name).append(" is ").append(age).append(" years old.");
        buf.append("</div>\n");
      }
      if (node.path("iterate").asBoolean()) {
        buf.append("<div>\nValues: ");
        JsonNode values = node.path("values");
        int size = values.size();
        for (int i = 0; i < size; i++) {
          if (i > 0) {
            buf.append(", ");
          }
          buf.append(values.get(i).asText());
        }
        buf.append("</div>\n");
      }
      buf.append("</span>");
      return ctx.buildNode(buf.toString());
    }
  }

}
