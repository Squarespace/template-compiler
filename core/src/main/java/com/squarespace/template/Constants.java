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

package com.squarespace.template;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;


public class Constants {

  public static final int DEFAULT_MAX_PARTIAL_DEPTH = 16;

  public static final StringView EMPTY_STRING_VIEW = new StringView("");

  public static final String[] EMPTY_ARRAY_OF_STRING = new String[] { };

  public static final Arguments EMPTY_ARGUMENTS = new Arguments();

  public static final String NULL_PLACEHOLDER = "???";

  public static final String[] BASE_URL_KEY = new String[] { "base-url" };

  public static final String[] TIMEZONE_KEY = new String[] { "website", "timeZone" };

  public static final String[] PRODUCT_PRICE_FROM_TEXT_KEY = new String[] {
      "localizedStrings", "productPriceFromText"};

  public static final String[] PRODUCT_PRICE_FROM_ONE_MONTH_TEXT_KEY = new String[] {
      "localizedStrings", "productPriceFromMonthlyText", "one"};

  public static final String[] PRODUCT_PRICE_FROM_MULTIPLE_MONTH_TEXT_KEY = new String[] {
      "localizedStrings", "productPriceFromMonthlyText", "other"};

  public static final String[] PRODUCT_PRICE_FROM_ONE_WEEK_TEXT_KEY = new String[] {
      "localizedStrings", "productPriceFromWeeklyText", "one"};

  public static final String[] PRODUCT_PRICE_FROM_MULTIPLE_WEEK_TEXT_KEY = new String[] {
      "localizedStrings", "productPriceFromWeeklyText", "other"};

  public static final String[] PRODUCT_PRICE_ONE_MONTH_TEXT_KEY = new String[] {
      "localizedStrings", "productPricePerMonth", "one"};

  public static final String[] PRODUCT_PRICE_MULTIPLE_MONTH_TEXT_KEY = new String[] {
      "localizedStrings", "productPricePerMonth", "other"};

  public static final String[] PRODUCT_PRICE_ONE_WEEK_TEXT_KEY = new String[] {
      "localizedStrings", "productPricePerWeek", "one"};

  public static final String[] PRODUCT_PRICE_MULTIPLE_WEEK_TEXT_KEY = new String[] {
      "localizedStrings", "productPricePerWeek", "other"};

  public static final String[] PRODUCT_PRICE_UNAVAILABLE_TEXT_KEY = new String[] {
      "localizedStrings", "productPriceUnavailable"};

  public static final String[] PRODUCT_QUICK_VIEW_TEXT_KEY = new String[] {
      "localizedStrings", "productQuickViewText" };

  public static final String[] PRODUCT_SOLD_OUT_TEXT_KEY = new String[] {
      "localizedStrings", "productSoldOutText" };

  public static final String[] PRODUCT_SALE_TEXT_KEY = new String[] {
      "localizedStrings", "productSaleText" };

  public static final String[] PRODUCT_SUMMARY_FORM_NO_ANSWER_TEXT_KEY = new String[] {
      "localizedStrings", "productSummaryFormNoAnswerText"};

  public static final String[] PRODUCT_VARIANT_SELECT_TEXT = new String[] {
      "localizedStrings", "productVariantSelectText"
  };

  public static final String[] PRODUCT_SCARCITY_DEFAULT_TEXT = new String[] {
      "localizedStrings", "productDefaultScarcityText"
  };

  public static final String[] PRODUCT_SCARCITY_MESSAGE_1 = new String[] {
      "localizedStrings", "productScarcityMessage1"
  };

  public static final String[] PRODUCT_SCARCITY_MESSAGE_2 = new String[] {
      "localizedStrings", "productScarcityMessage2"
  };

  public static final String[] PRODUCT_SCARCITY_MESSAGE_3 = new String[] {
      "localizedStrings", "productScarcityMessage3"
  };

  public static final String[] GIFT_CARD_VARIANT_SELECT_TEXT = new String[] {
      "localizedStrings", "giftCardVariantSelectText"
  };

  public static final String[] CLDR_MONEYFORMAT_KEY = new String[] {
      "website", "useCLDRMoneyFormat" };

  public static final JsonNode MISSING_NODE = MissingNode.getInstance();

}
