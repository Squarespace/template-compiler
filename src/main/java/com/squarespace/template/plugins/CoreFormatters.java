package com.squarespace.template.plugins;

import static com.squarespace.template.ExecuteErrorType.APPLY_PARTIAL_MISSING;
import static com.squarespace.template.ExecuteErrorType.APPLY_PARTIAL_SYNTAX;
import static com.squarespace.template.ExecuteErrorType.GENERAL_ERROR;
import static com.squarespace.template.GeneralUtils.isTruthy;
import static com.squarespace.template.plugins.PluginUtils.slugify;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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
import com.squarespace.template.Formatter;
import com.squarespace.template.GeneralUtils;
import com.squarespace.template.Instruction;
import com.squarespace.template.Patterns;
import com.squarespace.v6.utils.enums.ProductType;
import com.squarespace.v6.utils.enums.RecordType;


/**
 * A formatter is a function that examines a value and emits a string. Formatters
 * can have zero or more arguments.
 */
public class CoreFormatters extends BaseRegistry<Formatter> {

  
  public static Formatter ABSURL = new BaseFormatter("AbsUrl", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      String baseUrl = ctx.resolve(new String[] { "base-url" }).asText();
      String value = ctx.node().asText();
      ctx.buffer().append(baseUrl).append('/').append(value);
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
        // The partial template failed to parse. We need to raise an error.
        fail(ctx.error(APPLY_PARTIAL_SYNTAX).name(name).data(e.getMessage()));
      }
      
      if (inst == null) {
        fail(ctx.error(APPLY_PARTIAL_MISSING).name(name));
      }
      // Execute instruction starting with the current node, and appending to the parent
      // context's buffer.
      ctx.getCompiler().execute(inst, ctx.node(), ctx.buffer());
    }
  };
  
  
  public static Formatter COUNT = new BaseFormatter("count", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode node = ctx.node();
      int res = 0;
      if (node.isArray() || node.isObject()) {
        res = node.size();
      }
      ctx.append(Integer.toString(res));
    }
  };
  
  
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
      ctx.append(args.get(index));
    };
  };


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
      PluginDateUtils.formatDate((String)args.getOpaque(), instant, tzName, ctx.buffer());
    }
  };
  
  
  public static Formatter ENCODE_SPACE = new BaseFormatter("encode-space", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      ctx.append(Patterns.ONESPACE.matcher(ctx.node().asText()).replaceAll("&nbsp;"));
    }
  };
  
  
  public static Formatter HTML = new BaseFormatter("html", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      PluginUtils.escapeHtml(ctx.node().asText(), ctx.buffer());
    }
  };
  
  
  public static Formatter HTMLTAG = new BaseFormatter("htmltag", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      PluginUtils.escapeHtmlTag(ctx.node().asText(), ctx.buffer());
    }
  };
  
  
  public static Formatter HTMLATTR = new BaseFormatter("htmlattr", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      PluginUtils.escapeHtmlTag(ctx.node().asText(), ctx.buffer());
    }
  };
  
  public static Formatter ITEM_CLASSES = new BaseFormatter("item-classes", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode value = ctx.node();
      
      StringBuilder buf = ctx.buffer();
      buf.append("hentry");
      
      JsonNode node = ctx.resolve("promotedBlockType");
      if (isTruthy(node)) {
        buf.append(" promoted promoted-block-" + slugify(node.asText()));
      }
      
      node = ctx.resolve("tags");
      if (isTruthy(node)) {
        int size = node.size();
        for (int i = 0; i < size; i++) {
          buf.append(" category-" + slugify(node.get(i).asText()));
        }
      }
      
      node = ctx.resolve("author");
      JsonNode displayName = node.path("displayName");
      if (isTruthy(node) && isTruthy(displayName)) {
        ctx.append(" author-" + slugify(displayName.asText()));
      }
      node = ctx.resolve("recordTypeLabel");
      buf.append(' ').append(node.asText());
      
      node = ctx.resolve("@index");
      buf.append(" article-index-" + node.asInt());
      
      node = ctx.resolve("starred");
      if (isTruthy(node)) {
        buf.append( "featured");
      }

      node = value.path("recordType");
      if (RecordType.STORE_ITEM.value().equals(node.asInt())) {
        if (onSale(value)) {
          buf.append(" on-sale");
        }
        if (isTruthy(value.path("payWhatYouWant"))) {
          buf.append(" pay-what-you-want");
        }
      }
    }
    
    private boolean onSale(JsonNode item) {
      boolean onSale = false;
      ProductType type = ProductType.valueOf(item.path("productType").asInt());
      switch (type) {
        case PHYSICAL:
          JsonNode variants = item.path("variants");
          for (int i = 0; i < variants.size(); i++) {
            JsonNode variant = variants.get(i);
            if (isTruthy(variant.path("onSale"))) {
              onSale = true;
              break;
            }
          }
          break;
          
        case DIGITAL:
          onSale = isTruthy(item.path("payWhatYouWant")) ? false : isTruthy(item.path("onSale"));
          break;
          
        default:
          break;
      }
      return onSale;
    }
    
  };
  
  
  public static Formatter ITER = new BaseFormatter("iter", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      ctx.append(ctx.resolve("@index").asText());
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
      ctx.append(ctx.node().toString().replace("</script>", "</scr\"+\"ipt>"));
    }
  };

  
  public static Formatter JSON_PRETTY = new BaseFormatter("json-pretty", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      try {
        String result = GeneralUtils.jsonPretty(ctx.node());
        // NOTE: this is </script> replacement is copied verbatim from the JavaScript
        // version of JSONT, but it seems quite error-prone to me.
        ctx.append(result.replace("</script>", "</scr\"+\"ipt>"));
      } catch (IOException e) {
        fail(ctx.error(GENERAL_ERROR).data(e.getMessage()));
      }
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
      CharSequence result = (ctx.node().asLong() > 1) ? realArgs.plural : realArgs.singular;
      ctx.append(result);
    }
  };
  
  
  public static Formatter RAW = new BaseFormatter("raw", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      ctx.append(ctx.node().toString());
    }
  };

  
  public static Formatter ROUND = new BaseFormatter("round", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      long value = Math.round(ctx.node().asDouble());
      ctx.buffer().append(value);
    }
  };
  
  
  public static Formatter SAFE = new BaseFormatter("safe", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode node = ctx.node();
      if (GeneralUtils.isTruthy(node)) {
        ctx.append(node.asText().replaceAll("<.*?>", ""));
      }
    }
  };
  
  
  public static Formatter SMARTYPANTS = new BaseFormatter("smartypants", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      String str = ctx.node().asText();
      str = str.replaceAll("(^|[-\u2014\\s(\\[\"])'", "$1\u2018");
      str = str.replace("'", "\u2019");
      str = str.replaceAll("(^|[-\u2014/\\[(\u2018\\s])\"", "$1\u201c");
      str = str.replace("\"", "\u201d");
      str = str.replace("--", "\u2014");
      ctx.append(str);
    }
  };

  
  /**
   * SLUGIFY - Turn headline text into a slug.
   */
  public static Formatter SLUGIFY = new BaseFormatter("slugify", false) {
    
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      String result = ctx.node().asText();
      ctx.append(PluginUtils.slugify(result));
    }
  };
  
  
  public static Formatter STR = new BaseFormatter("str", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode node = ctx.node();
      if (!(node.isMissingNode() || node.isNull())) {
        ctx.append(node.asText());
      }
    }
  };
  
  
  public static Formatter TIMESINCE = new BaseFormatter("timesince", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode node = ctx.node();
      if (!node.isNumber()) {
        ctx.append("Invalid date.");
      } else {
        long value = node.asLong();
        ctx.append("<span class=\"timesince\" data-date=\"" + value + "\">");
        PluginDateUtils.humanizeDate(value, false, ctx.buffer());
        ctx.append("</span>");
      }
    }
  };

  static class TruncateArgs {
    int maxLen = 100;
    String ellipses = "...";
  }
  
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
      String value = ctx.node().asText();
      PluginUtils.truncate(value, obj.maxLen, obj.ellipses, ctx.buffer());
    }
  };
  
  
  public static Formatter URL_ENCODE = new BaseFormatter("url-encode", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      String value = ctx.node().asText();
      try {
        ctx.append(URLEncoder.encode(value, "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        // Shouldn't happen
      }
    }
  };

}
