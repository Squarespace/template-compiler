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

import com.squarespace.cldr.units.Unit;
import com.squarespace.cldr.units.UnitCategory;
import com.squarespace.cldr.units.UnitFactorSet;
import com.squarespace.cldr.units.UnitFormat;
import com.squarespace.cldr.units.UnitFormatOptions;
import com.squarespace.template.Arguments;


public class UnitUtils {

  /**
   * Generic unit formatter options.
   */
  public static class UnitFormatterOptions implements ArgumentTarget {

    final UnitFormatOptions options = new UnitFormatOptions();

    // Units of the input value.
    Unit inputUnit;

    // Exact unit to convert to.
    Unit exactUnit;

    // Format compact using only one of the given units.
    Unit[] exactUnits;

    // TODO: per unit formatting
    // Unit perUnit;

    // String indicating the output units. This could indicate the type of unit (bits vs bytes)
    // or the style of units (english vs metric), etc.
    String compact;

    // Format as a sequence of units
    Unit[] sequence;

    // Cached reference to the selected / constructed factor set, if any.
    UnitFactorSet factorSet;

    @Override
    public boolean parse(String arg, String value) {
      switch (arg) {
        case "compact":
          // Use this type to format by automatically selecting the best unit.
          compact = value;
          return true;

        case "in":
          // Units of the input amount
          inputUnit = Unit.fromIdentifier(value);
          return true;

        case "out":
          // Format by converting to the exact units.
          if (value.indexOf(',') == -1) {
            exactUnit = Unit.fromIdentifier(value);
          } else {
            exactUnits = parseUnitList(value);
          }
          return true;

// TODO: per unit formatting
//        case "per":
//          perUnit = Unit.fromIdentifier(value);
//          return true;

        case "sequence":
          if (sequence == null && compact == null) {
            sequence = parseUnitList(value);
          }
          return true;

        default:
          break;
      }
      return false;
    }
  }

  private static Unit[] parseUnitList(String value) {
    String[] parts = value.split(",");
    UnitCategory category = null;
    Unit[] units = new Unit[parts.length];
    for (int i = 0; i < parts.length; i++) {
      Unit unit = Unit.fromIdentifier(parts[i]);

      // Bail out on invalid or incompatible units
      if (unit == null) {
        return null;
      }

      if (category == null) {
        category = unit.category();
      } else if (unit.category() != category) {
        return null;
      }
      units[i] = unit;
    }
    return units;
  }

  public interface ArgumentTarget {
    boolean parse(String arg, String value);
  }

  public static void parseUnitArguments(Arguments args, ArgumentTarget target, UnitFormatOptions opts) {
    int count = args.count();
    String value = "";
    for (int i = 0; i < count; i++) {
      String arg = args.get(i);
      int index = arg.indexOf(':');
      if (index != -1) {
        value = arg.substring(index + 1);
        arg = arg.substring(0, index);
      }

      if (!target.parse(arg, value)) {
        parseUnitOption(arg, value, opts);
      }
    }

    // If no format value is set, default to short.
    if (opts.format() == null) {
      opts.setFormat(UnitFormat.NARROW);
    }
  }

  private static void parseUnitOption(String arg, String value, UnitFormatOptions opts) {
    if (arg.equals("format")) {
      switch (value) {
        case "long":
          opts.setFormat(UnitFormat.LONG);
          break;
        case "narrow":
          opts.setFormat(UnitFormat.NARROW);
          break;
        case "short":
          opts.setFormat(UnitFormat.SHORT);
          break;
        default:
          break;
      }
      return;
    }

    // TODO: fixme
//    DecimalFormatter.setNumberOption(arg, value, opts);
  }

}
