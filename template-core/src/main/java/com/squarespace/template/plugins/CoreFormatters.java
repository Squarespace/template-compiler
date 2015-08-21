/**
 * Copyright (c) 2014 SQUARESPACE, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.squarespace.template.plugins;

import static com.squarespace.template.ExecuteErrorType.APPLY_PARTIAL_MISSING;
import static com.squarespace.template.ExecuteErrorType.APPLY_PARTIAL_SYNTAX;
import static com.squarespace.template.ExecuteErrorType.GENERAL_ERROR;
import static com.squarespace.template.GeneralUtils.eatNull;
import static com.squarespace.template.GeneralUtils.isTruthy;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Pattern;

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
import com.squarespace.template.Patterns;
import com.squarespace.template.StringView;
import com.squarespace.template.SymbolTable;


public class CoreFormatters extends BaseRegistry<Formatter> {

  /**
   * Registers the active formatters in this registry.
   */
  @Override
  public void registerTo(SymbolTable<StringView, Formatter> table) {
    table.add(APPLY);
    table.add(COUNT);
    table.add(CYCLE);
    table.add(DATE);
    table.add(ENCODE_SPACE);
    table.add(HTML);
    table.add(HTMLATTR);
    table.add(HTMLTAG);
    table.add(ITER);
    table.add(JSON);
    table.add(JSON_PRETTY);
    table.add(OUTPUT);
    table.add(PLURALIZE);
    table.add(RAW);
    table.add(ROUND);
    table.add(SAFE);
    table.add(SLUGIFY);
    table.add(SMARTYPANTS);
    table.add(STR);
    table.add(TRUNCATE);
    table.add(URL_ENCODE);
  }

  /**
   * APPLY - This will compile and execute a "partial template", caching it in the
   * context for possible later use.
   */
  public static final Formatter APPLY = new BaseFormatter("apply", true) {

    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
      args.between(1, 2);
    }

    @Override
    public JsonNode apply(final Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      String name = args.first();
      boolean privateContext = false;
      if (args.count() == 2) {
        privateContext = args.get(1).equals("private");
      }
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
          return Constants.MISSING_NODE;
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
          return Constants.MISSING_NODE;
        } else {
          throw new CodeExecuteException(error);
        }
      }

      // Temporarily swap the buffers to capture all output of the partial.
      StringBuilder buf = new StringBuilder();
      StringBuilder origBuf = ctx.swapBuffer(buf);
      try {
        // If we want to hide the parent context during execution, create a new
        // temporary sub-context.
        if (privateContext) {
          Context.subContext(ctx, buf).execute(inst);
        } else {
          ctx.execute(inst);
        }

      } finally {
        ctx.swapBuffer(origBuf);
      }
      return ctx.buildNode(buf.toString());
    }

  };


  /**
   * COUNT - Returns a count of the number of members in an Array or Object.
   */
  public static final Formatter COUNT = new BaseFormatter("count", false) {

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      int res = 0;
      if (node.isArray() || node.isObject()) {
        res = node.size();
      }
      return ctx.buildNode(res);
    }

  };


  /**
   * CYCLE - Iterate over an array of arguments
   */
  public static final Formatter CYCLE = new BaseFormatter("cycle", true) {

    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
      args.atLeast(1);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      int value = node.asInt();
      int count = args.count();
      // Indices are 1-based and modulus of negative values is adjusted to properly wrap.
      int index = (value - 1) % count;
      if (index < 0) {
        index += count;
      }
      return ctx.buildNode(args.get(index));
    };

  };


  public static class DateFormatter extends BaseFormatter {

    private String[] timezoneKey = Constants.TIMEZONE_KEY;

    public DateFormatter() {
      super("date", true);
    }

    public void setTimezoneKey(String[] key) {
      timezoneKey = key;
    }

    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
      args.setOpaque(args.toString());
    };

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      JsonNode tzNode = ctx.resolve(timezoneKey);
      long instant = node.asLong();
      String tzName = "UTC";
      if (tzNode.isMissingNode()) {
        tzName = DateTimeZone.getDefault().getID();
      } else {
        tzName = tzNode.asText();
      }
      StringBuilder buf = new StringBuilder();
      PluginDateUtils.formatDate(ctx.locale(), (String)args.getOpaque(), instant, tzName, buf);
      return ctx.buildNode(buf.toString());
    }

  }

  /**
   * DATE - Format an epoch date using the site's timezone.
   */
  public static final DateFormatter DATE = new DateFormatter();


  /**
   * ENCODE_SPACE - Replace each space character with "&nbsp;".
   */
  public static final Formatter ENCODE_SPACE = new BaseFormatter("encode-space", false) {

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      String value = Patterns.ONESPACE.matcher(node.asText()).replaceAll("&nbsp;");
      return ctx.buildNode(value);
    }

  };


  /**
   * HTML - Escapes HTML characters & < > replacing them with the corresponding entity.
   */
  public static final Formatter HTML = new BaseFormatter("html", false) {

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      StringBuilder buf = new StringBuilder();
      PluginUtils.escapeHtml(eatNull(node), buf);
      return ctx.buildNode(buf.toString());
    }

  };


  /**
   * HTMLTAG - Escapes HTML characters & < > " replacing them with the corresponding entity.
   */
  public static final Formatter HTMLTAG = new BaseFormatter("htmltag", false) {

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      StringBuilder buf = new StringBuilder();
      PluginUtils.escapeHtmlTag(eatNull(node), buf);
      return ctx.buildNode(buf.toString());
    }

  };


  /**
   * HTMLATTR - Same as HTMLTAG.
   */
  public static final Formatter HTMLATTR = new BaseFormatter("htmlattr", false) {

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      StringBuilder buf = new StringBuilder();
      PluginUtils.escapeHtmlTag(eatNull(node), buf);
      return ctx.buildNode(buf.toString());
    }

  };


  /**
   * ITER - Outputs the index of the current array being iterated over.
   */
  public static final Formatter ITER = new BaseFormatter("iter", false) {

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      return ctx.buildNode(ctx.resolve("@index").asText());
    }

  };


  /**
   * JSON - Output a text representation of the node.
   */
  public static final Formatter JSON = new BaseFormatter("json", false) {

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      // NOTE: this is </script> replacement is copied verbatim from the JavaScript
      // version of JSONT, but it seems quite error-prone to me.
      return ctx.buildNode(node.toString().replace("</script>", "</scr\"+\"ipt>"));
    }

  };


  /**
   * JSON_PRETTY
   */
  public static final Formatter JSON_PRETTY = new BaseFormatter("json-pretty", false) {

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      try {
        String result = GeneralUtils.jsonPretty(node);
        // NOTE: this is </script> replacement is copied verbatim from the JavaScript
        // version of JSONT, but it seems quite error-prone to me.
        return ctx.buildNode(result.replace("</script>", "</scr\"+\"ipt>"));

      } catch (IOException e) {
        ErrorInfo error = ctx.error(GENERAL_ERROR).data(e.getMessage());
        if (ctx.safeExecutionEnabled()) {
          ctx.addError(error);
        } else {
          throw new CodeExecuteException(error);
        }
      }
      return Constants.MISSING_NODE;
    }

  };


  /**
   * OUTPUT
   */
  public static final Formatter OUTPUT = new BaseFormatter("output", false) {

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      List<String> values = args.getArgs();
      return ctx.buildNode(StringUtils.join(values.toArray(), ' '));
    }

  };


  static class PluralizeArgs {
    String singular = "";
    String plural = "s";
  }

  /**
   * PLURALIZE - Emit a string based on the plurality of the node.
   */
  public static final Formatter PLURALIZE = new BaseFormatter("pluralize", false) {

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
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      PluralizeArgs realArgs = (PluralizeArgs) args.getOpaque();
      CharSequence result = (node.asLong() == 1) ? realArgs.singular : realArgs.plural;
      return ctx.buildNode(result.toString());
    }
  };


  /**
   * RAW
   */
  public static final Formatter RAW = new BaseFormatter("raw", false) {

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      return ctx.buildNode(node.toString());
    }

  };


  /**
   * ROUND
   */
  public static final Formatter ROUND = new BaseFormatter("round", false) {

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      long value = Math.round(node.asDouble());
      return ctx.buildNode(value);
    }

  };


  /**
   * SAFE
   */
  public static final Formatter SAFE = new BaseFormatter("safe", false) {

    private final Pattern pattern = Pattern.compile("<[^>]*?>", Pattern.MULTILINE);

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      if (isTruthy(node)) {
        String value = pattern.matcher(node.asText()).replaceAll("");
        return ctx.buildNode(value);
      }
      return node;
    }

  };


  /**
   * SMARTYPANTS - Converts plain ASCII quote / apostrophe to corresponding Unicode curly characters.
   */
  public static final Formatter SMARTYPANTS = new BaseFormatter("smartypants", false) {

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      String str = eatNull(node);
      str = str.replaceAll("(^|[-\u2014\\s(\\[\"])'", "$1\u2018");
      str = str.replace("'", "\u2019");
      str = str.replaceAll("(^|[-\u2014/\\[(\u2018\\s])\"", "$1\u201c");
      str = str.replace("\"", "\u201d");
      str = str.replace("--", "\u2014");
      return ctx.buildNode(str);
    }

  };


  /**
   * SLUGIFY - Turn headline text into a slug.
   */
  public static final Formatter SLUGIFY = new BaseFormatter("slugify", false) {

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      String result = eatNull(node);
      return ctx.buildNode(PluginUtils.slugify(result));
    }

  };


  /**
   * STR - Output a string representation of the node.
   */
  public static final Formatter STR = new BaseFormatter("str", false) {

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      return ctx.buildNode(eatNull(node));
    }

  };


  static class TruncateArgs {
    int maxLen = 100;
    String ellipses = "...";
  }

  /**
   * TRUNCATE - Chop a string to a given length after the nearest space boundary.
   */
  public static final Formatter TRUNCATE = new BaseFormatter("truncate", false) {

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
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      TruncateArgs obj = (TruncateArgs)args.getOpaque();
      String value = PluginUtils.truncate(node.asText(), obj.maxLen, obj.ellipses);
      return ctx.buildNode(value);
    }
  };


  /**
   * URL_ENCODE - Encode characters which must be escaped in URLs. This
   * will output a hex escape sequence, '/' to %2F, or ' ' to '+'.
   */
  public static final Formatter URL_ENCODE = new BaseFormatter("url-encode", false) {

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      String value = node.asText();
      try {
        return ctx.buildNode(URLEncoder.encode(value, "UTF-8"));

      } catch (UnsupportedEncodingException e) {
        // Should never happen
        ErrorInfo error = ctx.error(GENERAL_ERROR).data(e.getMessage());
        if (ctx.safeExecutionEnabled()) {
          ctx.addError(error);
        } else {
          throw new CodeExecuteException(error);
        }
      }
      return Constants.MISSING_NODE;
    }

  };

}
