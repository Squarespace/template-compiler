package com.squarespace.template.plugins;

import static com.squarespace.template.Constants.MISSING_NODE;
import static com.squarespace.template.ExecuteErrorType.APPLY_PARTIAL_MISSING;
import static com.squarespace.template.ExecuteErrorType.APPLY_PARTIAL_SYNTAX;
import static com.squarespace.template.ExecuteErrorType.GENERAL_ERROR;
import static com.squarespace.template.GeneralUtils.eatNull;
import static com.squarespace.template.GeneralUtils.isTruthy;
import static com.squarespace.template.plugins.PluginUtils.slugify;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.BaseFormatter;
import com.squarespace.template.BaseRegistry;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.CodeSyntaxException;
import com.squarespace.template.Constants;
import com.squarespace.template.Context;
import com.squarespace.template.ErrorInfo;
import com.squarespace.template.Formatter;
import com.squarespace.template.GeneralUtils;
import com.squarespace.template.Instruction;
import com.squarespace.template.JsonTemplateEngine;
import com.squarespace.template.Patterns;
import com.squarespace.v6.utils.enums.RecordType;


public class CoreFormatters extends BaseRegistry<Formatter> {

  
  /**
   * ABSURL - Create an absolute URL, using the "base-url" value.
   */
  public static Formatter ABSURL = new BaseFormatter("AbsUrl", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      String baseUrl = ctx.resolve(new String[] { "base-url" }).asText();
      String value = ctx.node().asText();
      ctx.setNode(baseUrl + "/" + value);
    }
  };
  
  
  /**
   * APPLY - This will compile and execute a "partial template", caching it in the
   * context for possible later use.
   */
  public static Formatter APPLY = new BaseFormatter("apply", true) {
    
    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
      args.exactly(1);
    }
    
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      String name = args.first();
      Instruction inst = null;
      try {
        inst = ctx.getPartial(name);
      } catch (CodeSyntaxException e) {
        ErrorInfo parent = ctx.error(APPLY_PARTIAL_SYNTAX).name(name).data(e.getMessage());
        parent.child(e.getErrorInfo());
        if (ctx.safeExecutionEnabled()) {
          ctx.addError(parent);
          // We're in safe mode, so return immediately since this 'apply' formatter
          // can't output anything meaningful.
          return;
        } else {
          throw new CodeExecuteException(parent);
        }
      }
      
      if (inst == null) {
        ErrorInfo error = ctx.error(APPLY_PARTIAL_MISSING).name(name);
        if (ctx.safeExecutionEnabled()) {
          ctx.addError(error);
          // We're in safe mode, so return immediately since this 'apply' formatter
          // can't output anything meaningful.
          return;
        } else {
          throw new CodeExecuteException(error);
        }
      }
      // Execute instruction starting with the current node, and appending to the parent
      // context's buffer.
      StringBuilder buf = new StringBuilder();
      JsonNode node = ctx.node();
      if (node == null) {
        node = MISSING_NODE;
      }
      JsonTemplateEngine compiler = ctx.getCompiler();
      if (ctx.safeExecutionEnabled()) {
        compiler.executeWithPartialsSafe(inst, node, MISSING_NODE, buf);
      } else {
        ctx.getCompiler().execute(inst, node, buf);
      }
      ctx.setNode(buf.toString());
    }
  };
  
  
  /**
   * COUNT - Returns a count of the number of members in an Array or Object.
   */
  public static Formatter COUNT = new BaseFormatter("count", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode node = ctx.node();
      int res = 0;
      if (node.isArray() || node.isObject()) {
        res = node.size();
      }
      ctx.setNode(res);
    }
  };
  
  
  /**
   * CYCLE - Iterate over an array of arguments
   */
  public static Formatter CYCLE = new BaseFormatter("cycle", true) {
    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
      args.atLeast(1);
    }
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      int value = ctx.node().asInt();
      int count = args.count();
      // Indices are 1-based and modulus of negative values is adjusted to properly wrap.
      int index = (value - 1) % count;
      if (index < 0) {
        index += count;
      }
      ctx.setNode(args.get(index));
    };
  };


  /**
   * DATE - Format an epoch date using the site's timezone.
   */
  public static Formatter DATE = new BaseFormatter("date", true) {
    
    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
      args.setOpaque(args.toString());
    };
    
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode tzNode = ctx.resolve(Constants.TIMEZONE_KEY);
      long instant = ctx.node().asLong();
      String tzName = "UTC";
      if (tzNode.isMissingNode()) {
        tzName = DateTimeZone.getDefault().getID();
      } else {
        tzName = tzNode.asText();
      }
      StringBuilder buf = new StringBuilder();
      PluginDateUtils.formatDate((String)args.getOpaque(), instant, tzName, buf);
      ctx.setNode(buf.toString());
    }
  };
  
  
  /**
   * ENCODE_SPACE - Replace each space character with "&nbsp;".
   */
  public static Formatter ENCODE_SPACE = new BaseFormatter("encode-space", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      String value = Patterns.ONESPACE.matcher(ctx.node().asText()).replaceAll("&nbsp;");
      ctx.setNode(value);
    }
  };
  
  
  /**
   * HTML - Escapes HTML characters & < > replacing them with the corresponding entity.
   */
  public static Formatter HTML = new BaseFormatter("html", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      StringBuilder buf = new StringBuilder();
      PluginUtils.escapeHtml(eatNull(ctx.node()), buf);
      ctx.setNode(buf.toString());
    }
  };
  
  
  /**
   * HTMLTAG - Escapes HTML characters & < > " replacing them with the corresponding entity.
   */
  public static Formatter HTMLTAG = new BaseFormatter("htmltag", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      StringBuilder buf = new StringBuilder();
      PluginUtils.escapeHtmlTag(eatNull(ctx.node()), buf);
      ctx.setNode(buf.toString());
    }
  };
  
  
  /**
   * HTMLATTR - Same as HTMLTAG.
   */
  public static Formatter HTMLATTR = new BaseFormatter("htmlattr", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      StringBuilder buf = new StringBuilder();
      PluginUtils.escapeHtmlTag(eatNull(ctx.node()), buf);
      ctx.setNode(buf.toString());
    }
  };
  
  
  /**
   * ITEM_CLASSES
   */
  public static Formatter ITEM_CLASSES = new BaseFormatter("item-classes", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode value = ctx.node();
      
      StringBuilder buf = new StringBuilder();
      buf.append("hentry");
      
      JsonNode node = ctx.resolve("promotedBlockType");
      if (isTruthy(node)) {
        buf.append(" promoted promoted-block-" + slugify(node.asText()));
      }
      
      node = ctx.resolve("categories");
      if (isTruthy(node)) {
        int size = node.size();
        for (int i = 0; i < size; i++) {
          buf.append(" category-" + slugify(node.path(i).asText()));
        }
      }
      
      node = ctx.resolve("tags");
      if (isTruthy(node)) {
        int size = node.size();
        for (int i = 0; i < size; i++) {
          buf.append(" tag-" + slugify(node.path(i).asText()));
        }
      }
      
      node = ctx.resolve("author");
      JsonNode displayName = node.path("displayName");
      if (isTruthy(node) && isTruthy(displayName)) {
        buf.append(" author-" + slugify(displayName.asText()));
      }
      node = ctx.resolve("recordTypeLabel");
      buf.append(" post-type-").append(node.asText());
      
      node = ctx.resolve("@index");
      if (!node.isMissingNode()) {
        buf.append(" article-index-" + node.asInt());
      }
      
      node = ctx.resolve("starred");
      if (isTruthy(node)) {
        buf.append( " featured");
      }

      node = value.path("recordType");
      if (RecordType.STORE_ITEM.value().equals(node.asInt())) {
        if (CommerceUtils.isOnSale(value)) {
          buf.append(" on-sale");
        }
        if (isTruthy(value.path("payWhatYouWant"))) {
          buf.append(" pay-what-you-want");
        }
      }
      
      ctx.setNode(buf.toString());
    }
    
  };
  
  
  /**
   * ITER - Outputs the index of the current array being iterated over.
   */
  public static Formatter ITER = new BaseFormatter("iter", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      ctx.setNode(ctx.resolve("@index").asText());
    }
  };
  
  
  /**
   * JSON - Output a text representation of the node.
   */
  public static Formatter JSON = new BaseFormatter("json", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      // NOTE: this is </script> replacement is copied verbatim from the JavaScript
      // version of JSONT, but it seems quite error-prone to me.
      ctx.setNode(ctx.node().toString().replace("</script>", "</scr\"+\"ipt>"));
    }
  };

  
  /**
   * JSON_PRETTY
   */
  public static Formatter JSON_PRETTY = new BaseFormatter("json-pretty", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      try {
        String result = GeneralUtils.jsonPretty(ctx.node());
        // NOTE: this is </script> replacement is copied verbatim from the JavaScript
        // version of JSONT, but it seems quite error-prone to me.
        ctx.setNode(result.replace("</script>", "</scr\"+\"ipt>"));
      } catch (IOException e) {
        ErrorInfo error = ctx.error(GENERAL_ERROR).data(e.getMessage());
        if (ctx.safeExecutionEnabled()) {
          ctx.addError(error);
        } else {
          throw new CodeExecuteException(error);
        }
      }
    }
  };

  
  /**
   * OUTPUT
   */
  public static Formatter OUTPUT = new BaseFormatter("output", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      List<String> values = args.getArgs();
      ctx.setNode(StringUtils.join(values.toArray(), ' '));
    }
  };
  
  
  static class PluralizeArgs {
    String singular = "";
    String plural = "s";
  }
  
  /**
   * PLURALIZE - Emit a string based on the plurality of the node.
   */
  public static Formatter PLURALIZE = new BaseFormatter("pluralize", false) {
    
    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
      args.between(0, 2);
      PluralizeArgs realArgs = new PluralizeArgs();
      args.setOpaque(realArgs);
      if (args.count() == 1) {
        realArgs.plural = args.get(0);
      } else if (args.count() == 2) {
        realArgs.singular = args.get(0);
        realArgs.plural = args.get(1);
      }
    }
    
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      PluralizeArgs realArgs = (PluralizeArgs) args.getOpaque();
      CharSequence result = (ctx.node().asLong() == 1) ? realArgs.singular : realArgs.plural;
      ctx.setNode(result.toString());
    }
  };
  
  
  /**
   * RAW
   */
  public static Formatter RAW = new BaseFormatter("raw", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      ctx.setNode(ctx.node().toString());
    }
  };

  
  /**
   * ROUND
   */
  public static Formatter ROUND = new BaseFormatter("round", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      long value = Math.round(ctx.node().asDouble());
      ctx.setNode(value);
    }
  };
  
  
  /**
   * SAFE
   */
  public static Formatter SAFE = new BaseFormatter("safe", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode node = ctx.node();
      if (isTruthy(node)) {
        String value = node.asText().replaceAll("<.*?>", "");
        ctx.setNode(value);
      }
    }
  };
  
  
  /**
   * SMARTYPANTS - Converts plain ASCII quote / apostrophe to corresponding Unicode curly characters.
   */
  public static Formatter SMARTYPANTS = new BaseFormatter("smartypants", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      String str = eatNull(ctx.node());
      str = str.replaceAll("(^|[-\u2014\\s(\\[\"])'", "$1\u2018");
      str = str.replace("'", "\u2019");
      str = str.replaceAll("(^|[-\u2014/\\[(\u2018\\s])\"", "$1\u201c");
      str = str.replace("\"", "\u201d");
      str = str.replace("--", "\u2014");
      ctx.setNode(str);
    }
  };

  
  /**
   * SLUGIFY - Turn headline text into a slug.
   */
  public static Formatter SLUGIFY = new BaseFormatter("slugify", false) {
    
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      String result = eatNull(ctx.node());
      ctx.setNode(PluginUtils.slugify(result));
    }
  };
  
  
  /**
   * STR - Output a string representation of the node.
   */
  public static Formatter STR = new BaseFormatter("str", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      ctx.setNode(eatNull(ctx.node()));
    }
  };
  
  
  /**
   * TIMESINCE - Outputs a human-readable representation of (now - timestamp).
   */
  public static Formatter TIMESINCE = new BaseFormatter("timesince", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      StringBuilder buf = new StringBuilder();
      JsonNode node = ctx.node();
      if (!node.isNumber()) {
        buf.append("Invalid date.");
      } else {
        long value = node.asLong();
        buf.append("<span class=\"timesince\" data-date=\"" + value + "\">");
        PluginDateUtils.humanizeDate(value, false, buf);
        buf.append("</span>");
      }
      ctx.setNode(buf.toString());
    }
  };

  
  static class TruncateArgs {
    int maxLen = 100;
    String ellipses = "...";
  }
  
  /**
   * TRUNCATE - Chop a string to a given length after the nearest space boundary.
   */
  public static Formatter TRUNCATE = new BaseFormatter("truncate", false) {
    
    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
      TruncateArgs obj = new TruncateArgs();
      args.setOpaque(obj);
      if (args.count() > 0) {
        try {
          obj.maxLen = Integer.parseInt(args.get(0));
        } catch (NumberFormatException e) {
          throw new ArgumentsException("bad value for length '" + args.get(0) + "'");
        }
      }
      if (args.count() > 1) {
        obj.ellipses = args.get(1);
      }
    }
    
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      TruncateArgs obj = (TruncateArgs)args.getOpaque();
      String value = PluginUtils.truncate(ctx.node().asText(), obj.maxLen, obj.ellipses);
      ctx.setNode(value);
    }
  };
  
  
  /**
   * URL_ENCODE - Encode characters which must be escaped in URLs. This
   * will output a hex escape sequence, '/' to %2F, or ' ' to '+'.
   */
  public static Formatter URL_ENCODE = new BaseFormatter("url-encode", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      String value = ctx.node().asText();
      try {
        ctx.setNode(URLEncoder.encode(value, "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        // Shouldn't happen
      }
    }
  };

}
