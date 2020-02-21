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
import com.squarespace.cldrengine.api.MessageArgs;
import com.squarespace.template.Arguments;
import com.squarespace.template.BaseFormatter;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Context;
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

    String zoneId = PluginDateUtils.getTimeZoneNameFromContext(ctx);
    MessageArgs msgargs = messageArgs(args, ctx);
    MessageFormats formats = ctx.messageFormatter();
    formats.setTimeZone(zoneId);

    String message = node.asText();
    String result = formats.formatter().format(message, msgargs);
    var.set(result);
  }

  private static MessageArgs messageArgs(Arguments args, Context ctx) {
    MessageArgs res = new MessageArgs();
    int count = args.count();
    for (int i = 0; i < count; i++) {
      String raw = args.get(i);
      String name = null;
      int index = raw.indexOf(':');
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

      if (name != null) {
        res.add(name, value);
      }
      res.add(value);
    }
    return res;
  }
}