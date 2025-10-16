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
import static com.squarespace.template.GeneralUtils.getNodeAtPath;
import static com.squarespace.template.plugins.PluginDateUtils.formatDate;
import static com.squarespace.template.plugins.PluginUtils.escapeScriptTags;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import com.squarespace.template.GeneralUtils;
import com.squarespace.template.Instruction;
import com.squarespace.template.JsonUtils;
import com.squarespace.template.Patterns;
import com.squarespace.template.StringView;
import com.squarespace.template.SymbolTable;
import com.squarespace.template.Variable;
import com.squarespace.template.Variables;
import com.squarespace.template.plugins.FormatUtils.FormatArg;


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
    table.add(new FormatFormatter());
    table.add(new GetFormatter());
    table.add(new HtmlFormatter());
    table.add(new HtmlAttrFormatter());
    table.add(new HtmlTagFormatter());
    table.add(new IterFormatter());
    table.add(new JsonFormatter());
    table.add(new JsonPrettyFormatter());
    table.add(new KeyByFormatter());
    table.add(new OutputFormatter());
    table.add(new LookupFormatter());
    table.add(new ModFormatter());
    table.add(new PluralizeFormatter());
    table.add(new PropFormatter());
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
      args.atLeast(1);
    }

    @Override
    public void apply(final Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      String name = args.first();
      boolean privateContext = false;
      ObjectNode argvar = null;
      int count = args.count();
      if (count > 1) {
        argvar = JsonUtils.createObjectNode();
        for (int i = 1; i < count; i++) {
          String arg = args.get(i);

          // Mark the context as private
          if (arg.equals("private")) {
            privateContext = true;
            continue;
          }

          // Parse the colon-delimited arguments into key-values
          int j = arg.indexOf('=');
          if (j != -1) {
            String k = arg.substring(0, j);
            String v = arg.substring(j + 1);
            argvar.put(k, v);
          }
        }
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
          var.setMissing();
          return;
        } else {
          throw new CodeExecuteException(error);
        }
      }

      // A developer can attempt to recurse through a partial / macro more than once. The following
      // barrier checks if we're currently executing a given partial.  If so, we refuse to
      // execute it a second time and return a missing node.  Otherwise we execute the partial
      // template and return the result.
      if (ctx.enterPartial(name)) {
        var.set(executeTemplate(ctx, inst, var.node(), privateContext, argvar));
      } else {
        var.setMissing();
      }
      ctx.exitPartial(name);
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
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      JsonNode node = var.node();
      if (node.isArray() || node.isObject()) {
        var.set(node.size());
      } else if (node.isTextual()) {
        var.set(node.asText().length());
      } else {
        var.set(0);
      }
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
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      int value = var.node().asInt();
      int count = args.count();
      // Indices are 1-based and modulus of negative values is adjusted to properly wrap.
      int index = (value - 1) % count;
      if (index < 0) {
        index += count;
      }
      var.set(args.get(index));
    }

  }


  public static class DateFormatter extends BaseFormatter {

    public DateFormatter() {
      super("date", true);
    }

    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
      args.setOpaque(args.toString());
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      String tzName = PluginDateUtils.getTimeZoneNameFromContext(ctx);
      long instant = var.node().asLong();
      StringBuilder buf = new StringBuilder();
      formatDate(ctx.cldr(), (String)args.getOpaque(), instant, tzName, buf);
      var.set(buf);
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
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      JsonNode node = var.node();
      String value = Patterns.ONESPACE.matcher(node.asText()).replaceAll("&nbsp;");
      var.set(value);
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
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      String value = var.node().asText();
      var.set(EncodeUtils.encodeURI(value));
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
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      String value = var.node().asText();
      var.set(EncodeUtils.encodeURIComponent(value));
    }

  }

  /**
   * FORMAT - Substitutes positional arguments in a format string.
   */
  public static class FormatFormatter extends BaseFormatter {

    public FormatFormatter() {
      super("format", false);
    }

    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
      // Build a reusable array holding the argument variable and a value placeholder
      FormatArg[] arguments = new FormatArg[args.count()];
      for (int i = 0; i < arguments.length; i++) {
        arguments[i] = new FormatArg(splitVariable(args.get(i)));
      }
      args.setOpaque(arguments);
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      FormatArg[] arguments = (FormatArg[]) args.getOpaque();
      resolve(ctx, arguments);
      StringBuilder buf = new StringBuilder();
      String pattern = var.node().asText();
      FormatUtils.format(pattern, arguments, buf);
      var.set(buf);
    }

    private void resolve(Context ctx, FormatArg[] arguments) {
      for (int i = 0; i < arguments.length; i++) {
        FormatArg arg = arguments[i];
        arg.value = ctx.resolve(arg.name).asText();
      }
    }

  }

  /**
   * GET - Indirect lookup of properties on objects.
   *
   */
  public static class GetFormatter extends BaseFormatter {

    public GetFormatter() {
      super("get", false);
    }

    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {

    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable first = variables.first();
      JsonNode tmp = first.node();
      for (String arg : args.getArgs()) {
        Object[] path = GeneralUtils.splitVariable(arg);
        JsonNode node = ctx.resolve(path);
        if (node.isMissingNode()) {
          tmp = Constants.MISSING_NODE;
          break;
        } else {

          // Resolved node can be an array path
          if (node.isArray()) {
            ArrayNode arr = (ArrayNode)node;
            for (int i = 0; i < arr.size(); i++) {
              JsonNode elem = arr.get(i);
              if (elem.isNumber()) {
                tmp = tmp.path(elem.asInt());
              } else if (elem.isTextual()) {
                tmp = tmp.path(elem.asText());
              }
            }
          } else if (node.isNumber()) {
            tmp = tmp.path(node.asInt());
          } else if (node.isTextual()) {
            tmp = tmp.path(node.asText());
          }
        }

        // Once we hit a missing node, no point continuing
        if (tmp.isMissingNode()) {
          break;
        }
      }
      first.set(tmp);
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
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      StringBuilder buf = new StringBuilder();
      PluginUtils.escapeHtml(eatNull(var.node()), buf);
      var.set(buf);
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
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      StringBuilder buf = new StringBuilder();
      PluginUtils.escapeHtmlAttribute(eatNull(var.node()), buf);
      var.set(buf);
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
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      StringBuilder buf = new StringBuilder();
      PluginUtils.escapeHtmlAttribute(eatNull(var.node()), buf);
      var.set(buf);
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
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      var.set(ctx.resolve("@index").asText());
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
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      String escaped = escapeScriptTags((var.node().toString()));
      var.set(escaped);
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
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      try {
        String result = jsonPretty(var.node());
        var.set(escapeScriptTags(result));
        return;

      } catch (IOException e) {
        ErrorInfo error = ctx.error(GENERAL_ERROR).data(e.getMessage());
        if (ctx.safeExecutionEnabled()) {
          ctx.addError(error);
        } else {
          throw new CodeExecuteException(error);
        }
      }
      var.setMissing();
    }

  }

  /**
   * KEY_BY - maps an array of objects by the value for each at the given path
   */
  public static class KeyByFormatter extends BaseFormatter {

    public KeyByFormatter() {
      super("key-by", true);
    }

    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
      args.exactly(1);
    }

    @Override
    public void apply(final Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable first = variables.first();
      JsonNode firstNode = first.node();
      String path = args.first();
      ObjectNode keyByMap = JsonUtils.createObjectNode();

      if (firstNode.isArray() && path.length() > 0) {
        ArrayNode nodes = (ArrayNode)firstNode;
        Object[] splitPath = splitVariable(path);

        for (JsonNode node : nodes) {
          JsonNode nodeAtPath = getNodeAtPath(node, splitPath);

          if (!nodeAtPath.isMissingNode()) {
            keyByMap.put(nodeAtPath.asText(), node);
          }
        }
      }

      first.set(keyByMap);
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
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      List<String> values = args.getArgs();
      var.set(StringUtils.join(values.toArray(), ' '));
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
    public void apply(final Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      String fieldName = args.first();
      JsonNode value = ctx.resolve(splitVariable(fieldName));
      JsonNode result = ctx.resolve(splitVariable(value.asText()));

      Variable var = variables.first();
      var.set(result);
    }
  }

  /**
   * Mod - compute modulus of a value.
   */
  public static class ModFormatter extends BaseFormatter {

    public ModFormatter() {
      super("mod", false);
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      long divisor = 2;
      if (args.count() > 0) {
        try {
          String arg = args.get(0);
          divisor = Long.parseLong(arg, 10);
        } catch (NumberFormatException e) {
          // NOOP, default to divisor = 2
        }
      }
      Variable first = variables.first();
      long value = first.node().asLong();
      long result = value % divisor;
      first.set(result);
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
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      PluralizeArgs realArgs = (PluralizeArgs) args.getOpaque();
      CharSequence result = (var.node().asLong() == 1) ? realArgs.singular : realArgs.plural;
      var.set(result);
    }
  }

  /**
   * PROP - Access properties on an object or array.
   */
  public static class PropFormatter extends BaseFormatter {

    public PropFormatter() {
      super("prop", false);
    }

    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {

    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable first = variables.first();
      JsonNode tmp = first.node();
      int count = args.count();
      for (int i = 0; i < count; i++) {
        Object[] path = splitVariable(args.get(i));
        tmp = getNodeAtPath(tmp, path);
        if (tmp.isMissingNode()) {
          break;
        }
      }
      first.set(tmp);
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
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      var.set(var.node().toString());
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
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      long value = Math.round(var.node().asDouble());
      var.set(value);
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
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      JsonNode node = var.node();
      if (isTruthy(node)) {
        String value = pattern.matcher(node.asText()).replaceAll("");
        var.set(value);
      }
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
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      String str = eatNull(var.node());
      str = str.replaceAll("(^|[-\u2014\\s(\\[\"])'", "$1\u2018");
      str = str.replace("'", "\u2019");
      str = str.replaceAll("(^|[-\u2014/\\[(\u2018\\s])\"", "$1\u201c");
      str = str.replace("\"", "\u201d");
      str = str.replace("--", "\u2014");
      var.set(str);
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
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      String result = eatNull(var.node());
      var.set(PluginUtils.slugify(result));
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
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      var.set(eatNull(var.node()));
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
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      TruncateArgs obj = (TruncateArgs)args.getOpaque();
      String value = PluginUtils.truncate(var.node().asText(), obj.maxLen, obj.ellipses);
      var.set(value);
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
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      String value = var.node().asText();
      try {
        var.set(URLEncoder.encode(value, "UTF-8"));
        return;

      } catch (UnsupportedEncodingException e) {
        // Should never happen
        ErrorInfo error = ctx.error(GENERAL_ERROR).data(e.getMessage());
        if (ctx.safeExecutionEnabled()) {
          ctx.addError(error);
        } else {
          throw new CodeExecuteException(error);
        }
      }
      var.setMissing();
    }

  }

}
