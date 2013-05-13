package com.squarespace.template;

import static com.squarespace.template.ExecuteErrorType.APPLY_PARTIAL_MISSING;
import static com.squarespace.template.ExecuteErrorType.APPLY_PARTIAL_SYNTAX;
import static com.squarespace.template.ExecuteErrorType.GENERAL_ERROR;
import static com.squarespace.template.FormatterUtils.slugify;
import static com.squarespace.template.GeneralUtils.isTruthy;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTimeZone;

import com.fasterxml.jackson.databind.JsonNode;


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
      FormatterDateUtils.formatDate((String)args.getOpaque(), instant, tzName, ctx.buffer());
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
      FormatterUtils.escapeHtml(ctx.node().asText(), ctx.buffer());
    }
  };
  
  
  public static Formatter HTMLTAG = new BaseFormatter("htmltag", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      FormatterUtils.escapeHtmlTag(ctx.node().asText(), ctx.buffer());
    }
  };
  
  
  public static Formatter HTMLATTR = new BaseFormatter("htmlattr", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      FormatterUtils.escapeHtmlTag(ctx.node().asText(), ctx.buffer());
    }
  };
  
  /*
   
      'item-classes': function(value, context) {

      if (context.hash) { // sign its handlebars
        context = this;
      }

      var classes = ['hentry'];

      var promotedBlockType = TemplateContextUtils.get(context, 'promotedBlockType');
      if (!!promotedBlockType) {
        classes.push('promoted');
        classes.push(FORMATTERS['slugify']('promoted-block-' + promotedBlockType));
      }

      var categories = TemplateContextUtils.get(context, 'categories');
      if (!!categories) {
        for (var i = 0; i < categories.length; i++) {
          classes.push(FORMATTERS['slugify']('category-' + categories[i]));
        }
      }

      var tags = TemplateContextUtils.get(context, 'tags');
      if (!!tags) {
        for (var j = 0; j < tags.length; j++) {
          classes.push(FORMATTERS['slugify']('tag-' + tags[j]));
        }
      }

      var author = TemplateContextUtils.get(context, 'author');
      if (!!author && !!author.displayName) {
        classes.push(FORMATTERS['slugify']('author-' + author.displayName));
      }

      classes.push('post-type-' + TemplateContextUtils.get(context, 'recordTypeLabel'));

      classes.push('article-index-' + TemplateContextUtils.get(context, '@index'));

      if (TemplateContextUtils.get(context, 'starred')) {
        classes.push('featured');
      }

      // product classes
      if (value.recordType === Y.Squarespace.ContentConstants.STORE_ITEM) {

        if (Y.Squarespace.Commerce.onSale(value)) {
          classes.push('on-sale');
        }

        if (value.payWhatYouWant) {
          classes.push('pay-what-you-want');
        }
      }

      return classes.join(' ');
    },
   
   */
  
  public static Formatter ITEM_CLASSES = new BaseFormatter("item-classes", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      List<String> classes = Arrays.asList("hentry");
      JsonNode node = ctx.resolve("promotedBlockType");
      if (isTruthy(node)) {
        classes.add("promoted");
        classes.add("promoted-block-" + slugify(node.asText()));
      }
      node = ctx.resolve("tags");
      if (isTruthy(node)) {
        int size = node.size();
        for (int i = 0; i < size; i++) {
          classes.add("category-" + slugify(node.get(i).asText()));
        }
      }
      node = ctx.resolve("author");
      JsonNode displayName = node.path("displayName");
      if (isTruthy(node) && isTruthy(displayName)) {
        classes.add("author-" + slugify(displayName.asText()));
      }
      node = ctx.resolve("recordTypeLabel");
      classes.add(node.asText());
      // TODO:  fixme
      node = ctx.resolve("@index");
      classes.add("article-index-" + node.asInt());
      
      // TODO: implement
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
      ctx.append(FormatterUtils.slugify(result));
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
        FormatterDateUtils.humanizeDate(value, false, ctx.buffer());
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
      FormatterUtils.truncate(value, obj.maxLen, obj.ellipses, ctx.buffer());
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
