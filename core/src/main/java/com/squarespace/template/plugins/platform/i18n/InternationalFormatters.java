/**
 * Copyright (c) 2015 SQUARESPACE, Inc.
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

import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.BaseFormatter;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Context;
import com.squarespace.template.Formatter;
import com.squarespace.template.FormatterRegistry;
import com.squarespace.template.StringView;
import com.squarespace.template.SymbolTable;
import com.squarespace.template.Variables;


public class InternationalFormatters implements FormatterRegistry {

  @Override
  public void registerFormatters(SymbolTable<StringView, Formatter> table) {
    table.add(new DateTimeFormatter());
    table.add(new DateTimeFieldFormatter());
    table.add(new DateTimeIntervalFormatter());
    table.add(new DecimalFormatter());
    table.add(new MessageFormatter());
    table.add(new MoneyFormatter());
    table.add(new LegacyMoneyFormatter());
    table.add(new PluralFormatter());
    table.add(new RelativeTimeFormatter());
    table.add(new UnitFormatter());
  }

  /**
   * DEPRECATE in favor of using MessageFormatter.
   */
  private static class PluralFormatter extends MessageFormatter {

    PluralFormatter() {
      super("plural");
    }
  }

  /**
   * DEPRECATE
   */
  public static class DateTimeFieldFormatter extends BaseFormatter {

    public DateTimeFieldFormatter() {
      super("datetimefield", true);
    }

    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      variables.first().setMissing();
    }
  }

}
