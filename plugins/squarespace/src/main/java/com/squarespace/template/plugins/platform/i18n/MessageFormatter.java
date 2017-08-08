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

import java.time.ZoneId;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.cldr.MessageArgs;
import com.squarespace.cldr.MessageFormat;
import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.BaseFormatter;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Context;
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
    super(alias, true);
  }

  @Override
  public void validateArgs(Arguments args) throws ArgumentsException {
    args.setOpaque(messageArgs(args));
  }

  @Override
  public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
    Variable var = variables.first();
    JsonNode node = var.node();

    MessageArgs msgArgs = (MessageArgs) args.getOpaque();
    msgArgs.resetArgs();
    setContext(msgArgs, ctx);
    String message = node.asText();
    String tzName = PluginDateUtils.getTimeZoneNameFromContext(ctx);
    MessageFormat msgFormat = new MessageFormat(ctx.cldrLocale(), ZoneId.of(tzName), message);
    StringBuilder buf = new StringBuilder();
    msgFormat.format(msgArgs, buf);
    var.set(buf);
  }

  /**
   * Set the context instance used to resolve the argument values on demand.
   */
  private static void setContext(MessageArgs args, Context ctx) {
    int count = args.count();
    for (int i = 0; i < count; i++) {
      MsgArg arg = (MsgArg)args.get(i);
      arg.setContext(ctx);
    }
  }

  /**
   * Initialize the plural arguments array.
   */
  private static MessageArgs messageArgs(Arguments arguments) {
    MessageArgs result = new MessageArgs();
    int count = arguments.count();
    for (int i = 0; i < count; i++) {
      Object[] name = splitVariable(arguments.get(i));
      MsgArg arg = new MsgArg(name);
      result.add(arg);
    }
    return result;
  }

}