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
import static com.squarespace.template.GeneralUtils.executeTemplate;
import static com.squarespace.template.GeneralUtils.isTruthy;
import static com.squarespace.template.GeneralUtils.jsonPretty;
import static com.squarespace.template.GeneralUtils.splitVariable;
import static com.squarespace.template.plugins.PluginUtils.escapeScriptTags;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.compiler.text.EncodeUtils;
import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.BaseFormatter;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.CodeSyntaxException;
import com.squarespace.template.Constants;
import com.squarespace.template.Context;
import com.squarespace.template.ErrorInfo;
import com.squarespace.template.Formatter;
import com.squarespace.template.FormatterRegistry;
import com.squarespace.template.Instruction;
import com.squarespace.template.Patterns;
import com.squarespace.template.StringView;
import com.squarespace.template.SymbolTable;


public class CoreFormatters implements FormatterRegistry {

  /**
   * Registers the active formatters in this registry.
   */
  @Override
  public void registerFormatters(SymbolTable<StringView, Formatter> table) {
    table.add(new ApplyFormatter());
    table.add(new CountFormatter());
    table.add(new CycleFormatter());
    table.add(new DateFormatter());
    table.add(new EncodeSpaceFormatter());
    table.add(new EncodeUriFormatter());
    table.add(new EncodeUriComponentFormatter());
    table.add(new HtmlFormatter());
    table.add(new HtmlAttrFormatter());
    table.add(new HtmlTagFormatter());
    table.add(new IterFormatter());
    table.add(new JsonFormatter());
    table.add(new JsonPrettyFormatter());
    table.add(new OutputFormatter());
    table.add(new LookupFormatter());
    table.add(new PluralizeFormatter());
    table.add(new RawFormatter());
    table.add(new RoundFormatter());
    table.add(new SafeFormatter());
    table.add(new SlugifyFormatter());
    table.add(new SmartypantsFormatter());
    table.add(new StrFormatter());
    table.add(new TruncateFormatter());
    table.add(new UrlEncodeFormatter());
  }

  /**
   * APPLY - This will compile and execute a "partial template", caching it in the
   * context for possible later use.
   */
  public static class ApplyFormatter extends BaseFormatter {

    public ApplyFormatter() {
      super("apply", true);
    }

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
        // In safe execution mode this will never raise an exception.
        inst = ctx.getPartial(name);
      } catch (CodeSyntaxException e) {
        ErrorInfo parent = ctx.error(APPLY_PARTIAL_SYNTAX).name(name).data(e.getMessage());
        parent.child(e.getErrorInfo());
        throw new CodeExecuteException(parent);
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

      // A developer can attempt to recurse through a partial more than once. The following
      // barrier checks if we're currently executing a given partial.  If so, we refuse to
      // execute it a second time and return a missing node.  Otherwise we execute the partial
      // template and return the result.
      JsonNode result = Constants.MISSING_NODE;
      if (ctx.enterPartial(name)) {
        result = executeTemplate(ctx, inst, node, privateContext);
      }
      ctx.exitPartial(name);
      return result;
    }

  }


  /**
   * COUNT - Returns a count of the number of members in an Array or Object.
   */
  public static class CountFormatter extends BaseFormatter {

    public CountFormatter() {
      super("count", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      int res = 0;
      if (node.isArray() || node.isObject()) {
        res = node.size();
      }
      return ctx.buildNode(res);
    }

  }


  /**
   * CYCLE - Iterate over an array of arguments
   */
  public static class CycleFormatter extends BaseFormatter {

    public CycleFormatter() {
      super("cycle", true);
    }

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
    }

  }


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
    }

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
   * ENCODE_SPACE - Replace each space character with "&nbsp;".
   */
  public static class EncodeSpaceFormatter extends BaseFormatter {

    public EncodeSpaceFormatter() {
      super("encode-space", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      String value = Patterns.ONESPACE.matcher(node.asText()).replaceAll("&nbsp;");
      return ctx.buildNode(value);
    }

  }


  /**
   * ENCODE_URI - Encode characters equivalent to JavaScript encodeURI() function.
   *
   * See ECMA-262:
   *   https://www.ecma-international.org/ecma-262/7.0/index.html#sec-encodeuri-uri
   */
  public static class EncodeUriFormatter extends BaseFormatter {

    public EncodeUriFormatter() {
      super("encode-uri", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      String value = node.asText();
      return ctx.buildNode(EncodeUtils.encodeURI(value));
    }

  }


  /**
   * ENCODE_URI_COMPONENT - Encode characters equivalent to JavaScript encodeURIComponent() function.
   *
   * See ECMA-262:
   *   https://www.ecma-international.org/ecma-262/7.0/index.html#sec-encodeuricomponent-uricomponent
   */
  public static class EncodeUriComponentFormatter extends BaseFormatter {

    public EncodeUriComponentFormatter() {
      super("encode-uri-component", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      String value = node.asText();
      return ctx.buildNode(EncodeUtils.encodeURIComponent(value));
    }

  }


  /**
   * HTML - Escapes HTML characters & < > replacing them with the corresponding entity.
   */
  public static class HtmlFormatter extends BaseFormatter {

    public HtmlFormatter() {
      super("html", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      StringBuilder buf = new StringBuilder();
      PluginUtils.escapeHtml(eatNull(node), buf);
      return ctx.buildNode(buf.toString());
    }

  }


  /**
   * HTMLTAG - Escapes HTML characters & < > " replacing them with the corresponding entity.
   */
  public static class HtmlTagFormatter extends BaseFormatter {

    public HtmlTagFormatter() {
      super("htmltag", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      StringBuilder buf = new StringBuilder();
      PluginUtils.escapeHtmlAttribute(eatNull(node), buf);
      return ctx.buildNode(buf.toString());
    }

  }


  /**
   * HTMLATTR - Same as HTMLTAG.
   */
  public static class HtmlAttrFormatter extends BaseFormatter {

    public HtmlAttrFormatter() {
      super("htmlattr", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      StringBuilder buf = new StringBuilder();
      PluginUtils.escapeHtmlAttribute(eatNull(node), buf);
      return ctx.buildNode(buf.toString());
    }

  }


  /**
   * ITER - Outputs the index of the current array being iterated over.
   */
  public static class IterFormatter extends BaseFormatter {

    public IterFormatter() {
      super("iter", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      return ctx.buildNode(ctx.resolve("@index").asText());
    }

  }


  /**
   * JSON - Output a text representation of the node.
   */
  public static class JsonFormatter extends BaseFormatter {

    public JsonFormatter() {
      super("json", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      String escaped = escapeScriptTags((node.toString()));
      return ctx.buildNode(escaped);
    }

  }


  /**
   * JSON_PRETTY
   */
  public static class JsonPrettyFormatter extends BaseFormatter {


    public JsonPrettyFormatter() {
      super("json-pretty", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      try {
        String result = jsonPretty(node);
        return ctx.buildNode(escapeScriptTags(result));

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

  }


  /**
   * OUTPUT
   */
  public static class OutputFormatter extends BaseFormatter {

    public OutputFormatter() {
      super("output", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      List<String> values = args.getArgs();
      return ctx.buildNode(StringUtils.join(values.toArray(), ' '));
    }

  }

  /**
   * Lookup - given an object, returns the value for a given key
   */
  public static class LookupFormatter extends BaseFormatter {

    public LookupFormatter() {
      super("lookup", true);
    }

    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
      args.exactly(1);
    }

    @Override
    public JsonNode apply(final Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      String fieldName = args.first();
      JsonNode value = ctx.resolve(splitVariable(fieldName));
      JsonNode result = ctx.resolve(splitVariable(value.asText()));
      return result;
    }
  }

  static class PluralizeArgs {
    String singular = "";
    String plural = "s";
  }

  /**
   * PLURALIZE - Emit a string based on the plurality of the node.
   */
  public static class PluralizeFormatter extends BaseFormatter {

    public PluralizeFormatter() {
      super("pluralize", false);
    }

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
  }


  /**
   * RAW
   */
  public static class RawFormatter extends BaseFormatter {

    public RawFormatter() {
      super("raw", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      return ctx.buildNode(node.toString());
    }

  }


  /**
   * ROUND
   */
  public static class RoundFormatter extends BaseFormatter {

    public RoundFormatter() {
      super("round", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      long value = Math.round(node.asDouble());
      return ctx.buildNode(value);
    }

  }


  /**
   * SAFE
   */
  public static class SafeFormatter extends BaseFormatter {

    private final Pattern pattern = Pattern.compile("<[^>]*?>", Pattern.MULTILINE);

    public SafeFormatter() {
      super("safe", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      if (isTruthy(node)) {
        String value = pattern.matcher(node.asText()).replaceAll("");
        return ctx.buildNode(value);
      }
      return node;
    }

  }


  /**
   * SMARTYPANTS - Converts plain ASCII quote / apostrophe to corresponding Unicode curly characters.
   */
  public static class SmartypantsFormatter extends BaseFormatter {

    public SmartypantsFormatter() {
      super("smartypants", false);
    }

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

  }


  /**
   * SLUGIFY - Turn headline text into a slug.
   */
  public static class SlugifyFormatter extends BaseFormatter {

    public SlugifyFormatter() {
      super("slugify", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      String result = eatNull(node);
      return ctx.buildNode(PluginUtils.slugify(result));
    }

  }


  /**
   * STR - Output a string representation of the node.
   */
  public static class StrFormatter extends BaseFormatter {

    public StrFormatter() {
      super("str", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      return ctx.buildNode(eatNull(node));
    }

  }


  static class TruncateArgs {
    int maxLen = 100;
    String ellipses = "...";
  }

  /**
   * TRUNCATE - Chop a string to a given length after the nearest space boundary.
   */
  public static class TruncateFormatter extends BaseFormatter {

    public TruncateFormatter() {
      super("truncate", false);
    }

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
  }


  /**
   * URL_ENCODE - Encode characters which must be escaped in URLs. This
   * will output a hex escape sequence, '/' to %2F, or ' ' to '+'.
   */
  public static class UrlEncodeFormatter extends BaseFormatter {

    public UrlEncodeFormatter() {
      super("url-encode", false);
    }

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

  }

}
