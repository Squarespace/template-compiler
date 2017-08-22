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

import static com.squarespace.cldr.units.MeasurementSystem.METRIC;
import static com.squarespace.cldr.units.MeasurementSystem.METRIC_WITH_US_TEMPERATURE;
import static com.squarespace.cldr.units.MeasurementSystem.US_WITH_METRIC_TEMPERATURE;

import com.squarespace.cldr.CLDR;
import com.squarespace.cldr.units.MeasurementSystem;
import com.squarespace.cldr.units.UnitConverter;
import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.BasePredicate;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Context;


/**
 * Predicate that returns true if the given category uses the metric system
 * for the current locale. Defaults to true when locale is not determined.
 */
public class UnitsMetricPredicate extends BasePredicate {

  private static final CLDR CLDR_INSTANCE = CLDR.get();

  public UnitsMetricPredicate() {
    super("units-metric?", true);
  }

  @Override
  public void validateArgs(Arguments args) throws ArgumentsException {
    int count = args.count();
    if (count >= 1) {
      args.setOpaque(args.get(0));
    }
  }

  @Override
  public boolean apply(Context ctx, Arguments arguments) throws CodeExecuteException {
    CLDR.Locale locale = ctx.cldrLocale();
    if (locale == null) {
      return true;
    }

    String category = (String) arguments.getOpaque();
    if (category == null) {
      return true;
    }

    UnitConverter converter = CLDR_INSTANCE.getUnitConverter(locale);
    MeasurementSystem system = converter.measurementSystem();

    switch (category) {
      case "consumption":
      case "length":
      case "liquid":
      case "mass":
      case "speed":
      case "volume":
        return system == METRIC || system == METRIC_WITH_US_TEMPERATURE;

      case "temperature":
        return system == METRIC || system == US_WITH_METRIC_TEMPERATURE;

      default:
        break;
    }

    return true;
  }

}
