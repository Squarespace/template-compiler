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

import java.math.BigDecimal;
import java.util.List;

import com.squarespace.cldr.CLDR;
import com.squarespace.cldr.numbers.NumberFormatter;
import com.squarespace.cldr.units.Unit;
import com.squarespace.cldr.units.UnitCategory;
import com.squarespace.cldr.units.UnitConverter;
import com.squarespace.cldr.units.UnitFactorSet;
import com.squarespace.cldr.units.UnitFactorSets;
import com.squarespace.cldr.units.UnitValue;
import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.BaseFormatter;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Context;
import com.squarespace.template.GeneralUtils;
import com.squarespace.template.Variable;
import com.squarespace.template.Variables;
import com.squarespace.template.plugins.platform.i18n.UnitUtils.UnitFormatterOptions;


/**
 * UNIT - Conversion and formatting of units and unit sequences. This formatter
 * attempts to cover all possible cases for all unit categories using a compact
 * interface. This was done to avoid creating a formatter for each category
 * with a high degree of code duplication.
 *
 * Since there are a lot of possible permutations of input arguments, the apply()
 * method is split into sections each with a header comment.
 */
public class UnitFormatter extends BaseFormatter {

  private static final CLDR CLDR_INSTANCE = CLDR.get();

  public UnitFormatter() {
    super("unit", false);
  }

  @Override
  public void validateArgs(Arguments args) throws ArgumentsException {
    UnitFormatterOptions opts = new UnitFormatterOptions();
    UnitUtils.parseUnitArguments(args, opts, opts.options);
    args.setOpaque(opts);
  }

  @Override
  public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
    Variable var = variables.first();
    BigDecimal amount = GeneralUtils.nodeToBigDecimal(var.node());
    if (amount == null) {
      var.setMissing();
      return;
    }

    UnitFormatterOptions opts = (UnitFormatterOptions) args.getOpaque();
    CLDR.Locale locale = ctx.cldrLocale();

    Unit inputUnit = opts.inputUnit;
    Unit exactUnit = opts.exactUnit;
    Unit[] exactUnits = opts.exactUnits;
    String compact = opts.compact;
    Unit[] sequence = opts.sequence;

    // == FALLBACK ==

    // If no arguments set, format a plain number with no unit.
    if (inputUnit == null && exactUnit == null && exactUnits == null && compact == null && sequence == null) {
      UnitValue value = new UnitValue(amount, null);
      StringBuilder buf = new StringBuilder();
      NumberFormatter formatter = CLDR_INSTANCE.getNumberFormatter(locale);
      formatter.formatUnit(value, buf, opts.options);
      var.set(buf);
      return;
    }

    // At least one argument was provided. We will try to infer the others where possible.
    UnitConverter converter = CLDR_INSTANCE.getUnitConverter(locale);
    UnitFactorSet factorSet = null;

    // == INTERPRET ARGUMENTS ==

    if (compact != null) {
      // First see if compact format matches a direct unit conversion (e.g. temperature, speed)
      Unit unit = selectExactUnit(compact, converter);
      if (unit != null) {
        exactUnit = unit;
      } else if (opts.factorSet != null) {
        // Compact format might correspond to a factor set (e.g. digital bits, bytes).
        factorSet = opts.factorSet;
      } else {
        factorSet = selectFactorSet(compact, converter);
        opts.factorSet = factorSet;
      }

    } else if (exactUnits != null && exactUnits.length > 0) {
      if (opts.factorSet != null) {
        factorSet = opts.factorSet;
      } else {
        UnitCategory category = exactUnits[0].category();
        factorSet = converter.getFactorSet(category, exactUnits);
        opts.factorSet = factorSet;
      }

    } else if (sequence != null && sequence.length > 0) {
      if (opts.factorSet != null) {
        factorSet = opts.factorSet;
      } else {
        UnitCategory category = sequence[0].category();
        factorSet = converter.getFactorSet(category, sequence);
        opts.factorSet = factorSet;
      }
    }

    // Make sure we know what the input units are.
    if (inputUnit == null) {
      if (exactUnit != null) {
        inputUnit = inputFromExactUnit(exactUnit, converter);
      } else if (exactUnits != null) {
        inputUnit = inputFromExactUnit(exactUnits[0], converter);
      } else if (factorSet != null) {
        inputUnit = factorSet.base();
      }
    }

    // == CONVERSION ==

    UnitValue value = new UnitValue(amount, inputUnit);

    // In sequence mode this will get set below.
    List<UnitValue> values = null;

    if (exactUnit != null) {
      // Convert to exact units using the requested unit.
      value = converter.convert(value, exactUnit);

    } else if (factorSet == null) {
        // Convert directly to "best" unit using the default built-in factor sets.
        value = converter.convert(value);

    } else if (compact != null || exactUnits != null) {
        // Use the factor set to build a compact form.
        value = converter.convert(value, factorSet);

    } else if (sequence != null) {
      // Use the factor set to produce a sequence.
      values = converter.sequence(value, factorSet);
    }

    // == FORMATTING ==

    StringBuilder buf = new StringBuilder();
    NumberFormatter formatter = CLDR_INSTANCE.getNumberFormatter(locale);
    if (values == null) {
      formatter.formatUnit(value, buf, opts.options);
    } else {
      formatter.formatUnits(values, buf, opts.options);
    }
    var.set(buf);
  }

  /**
   * Some categories only have a single possible unit depending the locale.
   */
  protected Unit selectExactUnit(String compact, UnitConverter converter) {
    if (compact != null) {
      switch (compact) {
        case "consumption":
          return converter.consumptionUnit();
        case "light":
          return Unit.LUX;
        case "speed":
          return converter.speedUnit();
        case "temp":
        case "temperature":
          return converter.temperatureUnit();
        default:
          break;
      }
    }
    return null;
  }

  /**
   * Select a factor set based on a name, used to format compact units. For example,
   * if compact="bytes" we return a factor set DIGITAL_BYTES. This set is then used
   * to produce the most compact form for a given value, e.g. "1.2MB", "37TB", etc.
   */
  protected UnitFactorSet selectFactorSet(String compact, UnitConverter converter) {
    if (compact != null) {
      switch (compact) {
        case "angle":
        case "angles":
          return UnitFactorSets.ANGLE;
        case "area":
          return converter.areaFactors();
        case "bit":
        case "bits":
          return UnitFactorSets.DIGITAL_BITS;
        case "byte":
        case "bytes":
          return UnitFactorSets.DIGITAL_BYTES;
        case "duration":
          return UnitFactorSets.DURATION;
        case "duration-large":
          return UnitFactorSets.DURATION_LARGE;
        case "duration-small":
          return UnitFactorSets.DURATION_SMALL;
        case "energy":
          return UnitFactorSets.ENERGY;
        case "frequency":
          return UnitFactorSets.FREQUENCY;
        case "length":
          return converter.lengthFactors();
        case "mass":
          return converter.massFactors();
        case "power":
          return UnitFactorSets.POWER;
        case "volume":
          return converter.volumeFactors();
        case "liquid":
          return converter.volumeLiquidFactors();
        default:
          break;
      }
    }
    return null;
  }

  /**
   * Based on the unit we're converting to, guess the input unit. For example, if we're
   * converting to MEGABIT and no input unit was specified, assume BIT.
   */
  protected Unit inputFromExactUnit(Unit exact, UnitConverter converter) {
    switch (exact) {
      case TERABIT:
      case GIGABIT:
      case MEGABIT:
      case KILOBIT:
      case BIT:
        return Unit.BIT;

      case TERABYTE:
      case GIGABYTE:
      case MEGABYTE:
      case KILOBYTE:
      case BYTE:
        return Unit.BYTE;

      default:
        break;
    }

    UnitCategory category = exact.category();
    switch (category) {
      case CONSUMPTION:
        return converter.consumptionUnit();
      case FREQUENCY:
        return Unit.HERTZ;
      case LIGHT:
        return Unit.LUX;
      case PRESSURE:
        return Unit.MILLIBAR;
      case SPEED:
        return converter.speedUnit();
      case TEMPERATURE:
        return converter.temperatureUnit();

      default:
        UnitFactorSet factorSet = getDefaultFactorSet(category, converter);
        if (factorSet != null) {
          return factorSet.base();
        }
        break;
    }
    return null;
  }

  /**
   * Default conversion factors for each category. Some of these differ based on the locale
   * of the converter.
   */
  protected UnitFactorSet getDefaultFactorSet(UnitCategory category, UnitConverter converter) {
    switch (category) {
      case ANGLE:
        return UnitFactorSets.ANGLE;
      case AREA:
        return converter.areaFactors();
      case DURATION:
        return UnitFactorSets.DURATION;
      case ENERGY:
        return converter.energyFactors();
      case FREQUENCY:
        return UnitFactorSets.FREQUENCY;
      case LENGTH:
        return converter.lengthFactors();
      case MASS:
        return converter.massFactors();
      case POWER:
        return UnitFactorSets.POWER;
      case VOLUME:
        return converter.volumeFactors();
      default:
        break;
    }
    return null;
  }

}