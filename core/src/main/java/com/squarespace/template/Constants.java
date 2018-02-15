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

  public static final String[] PRODUCT_QUICK_VIEW_TEXT_KEY = new String[] {
      "localizedStrings", "productQuickViewText" };

  public static final String[] PRODUCT_SOLD_OUT_TEXT_KEY = new String[] {
      "localizedStrings", "productSoldOutText" };

  public static final String[] PRODUCT_SALE_TEXT_KEY = new String[] {
      "localizedStrings", "productSaleText" };

  public static final String[] PRODUCT_SUMMARY_FORM_NO_ANSWER_TEXT_KEY = new String[] {
      "localizedStrings", "productSummaryFormNoAnswerText"};

  public static final String[] CLDR_MONEYFORMAT_KEY = new String[] {
      "website", "useCLDRMoneyFormat" };

  public static final JsonNode MISSING_NODE = MissingNode.getInstance();

}
