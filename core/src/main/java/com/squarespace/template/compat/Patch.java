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

package com.squarespace.template.compat;

/**
 * Legacy behaviors gated by a {@link CompatLevel}. A patch's legacy behavior
 * is active while the level is below its threshold, or when a per-site
 * override forces it on.
 *
 * A fix that changes output ships with a legacy path gated by a new patch at
 * a higher threshold. Higher levels apply more fixes without changing
 * the released surface at level 0. The default level is 0.
 *
 * The threshold freezes once a release ships. Changing it would change live
 * sites at intermediate levels.
 *
 * Retire a patch when no site needs it. Remove the legacy path, its tests,
 * and the entry together.
 */
public enum Patch {

  /**
   * The partial depth counter is not balanced on a depth breach or a partial
   * that throws. A later include then fails with a spurious depth error.
   */
  PARTIAL_DEPTH_LEAK(1),

  /**
   * The compare order for mixed types is not a total order. Predicate
   * results differ from the JS engine.
   */
  COMPARE_TOTAL_ORDER(1),

  /**
   * twitter-follow-button throws on an empty derived username and leaves
   * data-username unescaped.
   */
  TWITTER_BUTTON_USERNAME(1),

  /**
   * money and decimal throw when the value is null or not a number.
   */
  MONEY_BAD_DECIMAL(1),

  /**
   * i18n-money-format throws Malformed pattern on a JVM default locale
   * without a dot decimal separator.
   */
  MONEY_LOCALE_SYMBOLS(1),

  /**
   * The legacy money formatters (i18n-money-format, moneyFormat,
   * money-format, money-string, cart-subtotal) format through a double and
   * lose precision on large values.
   */
  MONEY_DOUBLE_ROUNDING(1),

  /**
   * mod divides by zero when the divisor is 0.
   */
  MOD_ZERO(1),

  /**
   * nth? divides by zero when the modulus is 0.
   */
  NTH_MODULO_ZERO(1),

  /**
   * truncate throws a string index error on a negative length.
   */
  TRUNCATE_NEGATIVE(1),

  /**
   * timesince adds the zone offset to an epoch millis delta and reports the
   * wrong age.
   */
  HUMANIZE_DATE_TZ(1),

  /**
   * The %W date field is Sunday anchored and duplicates %U instead of
   * Monday anchored.
   */
  WEEK_MONDAY_ANCHOR(1),

  /**
   * @subpath in a message argument cannot address the parent scope.
   */
  SUBPATH_PARENT_SCOPE(2),

  /**
   * htmlattr does not escape the single quote.
   */
  HTMLATTR_QUOTE(2),

  /**
   * social-button leaves assetUrl and other attributes unescaped and does
   * not fall back on a present but empty assetUrl.
   */
  SOCIAL_BUTTON_ATTRIBUTES(2),

  /**
   * activate-twitter-links linkifies before escaping and passes raw html
   * tags through.
   */
  TWITTER_LINKS_RAW_HTML(2),

  /**
   * json and json-pretty emit raw U+2028 and U+2029 line separators.
   */
  JSON_LINE_SEPARATORS(2),

  /**
   * encode-space replaces tabs and newlines, not just spaces.
   */
  ENCODE_SPACE_WHITESPACE(2),

  /**
   * cart-subtotal and the legacy money formatters throw on blank or null
   * input.
   */
  MONEY_BLANK_PARSE(2),

  /**
   * money renders a bare number with no symbol for an unknown currency
   * code.
   */
  MONEY_UNKNOWN_CURRENCY(2),

  /**
   * i18n-money-format throws for a present but invalid currency code.
   */
  LEGACY_MONEY_BAD_CURRENCY(2),

  /**
   * width, height, and resize throw on non numeric dimensions.
   */
  SPLIT_DIMENSIONS_NONNUMERIC(2),

  /**
   * datetime and relative-time render the epoch date for a missing value.
   */
  DATETIME_MISSING_EPOCH(2),

  /**
   * message splits a positional url argument on the colon.
   */
  MESSAGE_ARG_URL_SPLIT(2),

  /**
   * current-type? throws when called with no arguments.
   */
  CURRENT_TYPE_ARITY(2),

  /**
   * A boolean keyword argument with leading whitespace is not parsed as
   * json.
   */
  JSON_START_KEYWORD(2),

  /**
   * color-weight accepts 4 and 5 character hex and reports a wrong weight.
   */
  COLOR_WEIGHT_LENGTH(2),

  /**
   * format leaks a slot value after a bad tag and has no brace escape.
   */
  FORMAT_STATE_DIGITS(2),

  /**
   * encode-uri renders the text null for a lone surrogate.
   */
  ENCODE_URI_SURROGATE(2),

  /**
   * encode-uri percent encodes the single quote.
   */
  ENCODE_URI_QUOTE(2),

  /**
   * cart-quantity throws when an entry has no quantity.
   */
  CART_QUANTITY_MISSING(2),

  /**
   * varied-prices? throws when variants is an object with two or more
   * fields instead of an array.
   */
  VARIED_PRICES_NON_ARRAY(2),

  /**
   * A null timeZone renders as the text null and fails the zone lookup.
   */
  TIMEZONE_NULL_LITERAL(3),

  /**
   * summary-form-field leaves rawTitle and the fallback text unescaped.
   */
  SUMMARY_FIELD_TITLE_ESCAPES(3),

  /**
   * product-price puts a boolean true in the formattedFromPrice slot.
   */
  PRODUCT_PRICE_TRUE_SLOT(3),

  /**
   * datetime-interval returns the raw input for a missing operand.
   */
  DATETIME_INTERVAL_RAW(3),

  /**
   * An integral eval result emits DoubleNode, so exact-value consumers
   * such as the level 1 compare ordering treat it as a double.
   */
  EVAL_INTEGRAL_LONG(3),

  /**
   * product-scarcity and restock throw when the product has no id or
   * the merchandising context entry has no scarcityEnabled field.
   */
  SCARCITY_MISSING_FIELD(3),

  /**
   * quantity-input wraps the variant stock total negative past 2^31
   * units, hiding the quantity input for in-stock products.
   */
  STOCK_OVERFLOW(3);

  /**
   * Lowest level where this patch is fixed. Frozen once a release ships.
   */
  private final int threshold;

  Patch(int threshold) {
    this.threshold = threshold;
  }

  public int threshold() {
    return threshold;
  }

  /**
   * Highest threshold. The fully fixed compiler sits here.
   */
  public static int maxThreshold() {
    int max = 0;
    for (Patch patch : values()) {
      max = Math.max(max, patch.threshold);
    }
    return max;
  }
}
