/**
 * Copyright (c) 2017 Squarespace, Inc.
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

import static com.squarespace.cldr.plurals.CLDRPluralRules.evalCardinal;
import static com.squarespace.cldr.plurals.CLDRPluralRules.evalOrdinal;

import com.squarespace.cldr.plurals.PluralCategory;
import com.squarespace.cldr.plurals.PluralOperands;


/**
 * A single argument to a plural formatter.
 */
public class PluralArg {

  // Name of the variable to resolve in the context
  public final Object[] name;

  // Plural operands used for calculations to assign a plural category.
  public final PluralOperands operands;

  // Value of the variable
  public String value;

  public PluralArg(Object[] name) {
    this.name = name;
    this.operands = new PluralOperands();
  }

  public PluralCategory evaluate(String language, boolean cardinal) {
    return cardinal ? evalCardinal(language, operands) : evalOrdinal(language, operands);
  }

}
