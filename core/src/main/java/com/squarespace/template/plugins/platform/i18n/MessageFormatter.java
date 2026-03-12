/**
 * Copyright (c) 2017 SQUARESPACE, Inc.
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
package com.squarespace.template.plugins.platform.i18n;

import static com.squarespace.template.GeneralUtils.splitVariable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.squarespace.cldrengine.api.MessageArgs;
import com.squarespace.template.Arguments;
import com.squarespace.template.BaseFormatter;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Context;
import com.squarespace.template.compat.Patch;
import com.squarespace.template.Frame;
import com.squarespace.template.MessageFormats;
import com.squarespace.template.Variable;
import com.squarespace.template.Variables;
import com.squarespace.template.plugins.PluginDateUtils;


/**
 * MESSAGE - Evaluates a MessageFormat against one or more arguments.
 */
public class MessageFormatter extends BaseFormatter {

  public MessageFormatter() {
    this("message");
  }

  public MessageFormatter(String alias) {
    super(alias, false);
  }

  @Override
  public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
    Variable var = variables.first();
    JsonNode node = var.node();

    String zoneId = PluginDateUtils.getTimeZoneNameFromContext(ctx,
        ctx.compatEnabled(Patch.TIMEZONE_NULL_LITERAL));
    MessageArgs msgargs = messageArgs(args, ctx);
    MessageFormats formats = ctx.messageFormatter(zoneId);

    String message = node.asText();
    String result = formats.formatter().format(message, msgargs);
    var.set(result);
  }

  /**
   * Find the name/value delimiter in an argument. At the fixed level an
   * argument is a named argument only when the name part is a plain
   * identifier and the delimiter is not part of a URL scheme (e.g.
   * http://example.com). Otherwise the argument is positional.
   */
  private static int delimiter(String arg, boolean legacyUrlSplit) {
    int len = arg.length();
    for (int i = 0; i < len; i++) {
      char c = arg.charAt(i);
      if (c != ':' && c != '=') {
        continue;
      }
      // Legacy, the first colon or equals is the delimiter.
      if (legacyUrlSplit) {
        return i;
      }
      // A colon followed by "//" is a URL scheme, not a delimiter.
      if (c == ':' && i + 2 < len && arg.charAt(i + 1) == '/' && arg.charAt(i + 2) == '/') {
        continue;
      }
      // Any later delimiter would extend the same name, which already
      // failed, so no later position can be a delimiter either.
      return isName(arg.substring(0, i)) ? i : -1;
    }
    return -1;
  }

  private static boolean isName(String s) {
    if (s.isEmpty()) {
      return false;
    }
    char c0 = s.charAt(0);
    if (!(Character.isLetter(c0) || c0 == '_' || c0 == '$')) {
      return false;
    }
    for (int i = 1; i < s.length(); i++) {
      char c = s.charAt(i);
      if (!(Character.isLetterOrDigit(c) || c == '_' || c == '$')) {
        return false;
      }
    }
    return true;
  }

  private static MessageArgs messageArgs(Arguments args, Context ctx) {
    MessageArgs res = new MessageArgs();
    int count = args.count();
    for (int i = 0; i < count; i++) {
      String raw = args.get(i);
      String name = null;

      // Either ':' or '=' can delimit key/value arguments
      int index = delimiter(raw, ctx.compatEnabled(Patch.MESSAGE_ARG_URL_SPLIT));
      if (index != -1) {
        // Map named argument
        name = raw.substring(0, index);
        raw = raw.substring(index + 1);
      }

      Object[] ref = splitVariable(raw);

      // Since the message string is the node on the current stack frame, the
      // variable reference '@' will point to it, instead of the parent scope.
      // Skip the current frame so we avoid trying to resolve variables against
      // the message string.
      Frame parent = ctx.frame().parent();
      JsonNode value = ctx.resolve(ref, parent == null ? ctx.frame() : parent);
      // Fixed, an argument that did not resolve to a variable passes
      // through as literal text. Legacy, it is dropped.
      if (value.isMissingNode() && !ctx.compatEnabled(Patch.MESSAGE_ARG_URL_SPLIT)) {
        value = TextNode.valueOf(raw);
      }

      if (name != null) {
        res.add(name, value);
      }
      // Keyword arguments are also positional
      res.add(value);
    }
    return res;
  }
}