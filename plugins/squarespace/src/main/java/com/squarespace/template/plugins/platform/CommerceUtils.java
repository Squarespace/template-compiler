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

package com.squarespace.template.plugins.platform;

import static com.squarespace.template.GeneralUtils.isTruthy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squarespace.cldr.CLDR;
import com.squarespace.template.JsonUtils;
import com.squarespace.template.plugins.PluginUtils;
import com.squarespace.template.plugins.platform.enums.ProductType;


/**
 * Extracted from Commons library at commit ab4ba7a6f2b872a31cb6449ae9e96f5f5b30f471
 */
public class CommerceUtils {

  private static final ArrayNode EMPTY_ARRAY = JsonUtils.createArrayNode();

  private static final String DEFAULT_CURRENCY = "USD";

  private static final BigDecimal DEFAULT_MONEY_AMOUNT = new BigDecimal(0);

  private static final JsonNode DEFAULT_MONEY_NODE = new ObjectMapper()
      .createObjectNode()
      .put("currency", "USD")
      .put("value", "0");

  private CommerceUtils() {
  }

  public static ProductType getProductType(JsonNode item) {
    JsonNode structuredContent = item.path("structuredContent");
    JsonNode node = structuredContent.path("productType");
    return ProductType.fromCode(node.asInt());
  }

  public static boolean isSubscribable(JsonNode item) {
    JsonNode structuredContent = item.path("structuredContent");
    JsonNode node = structuredContent.path("isSubscribable");
    return node.asBoolean();
  }

  public static JsonNode getSubscriptionPlanBillingPeriodNode(JsonNode item) {
    // BillingPeriod is represented as {value, unit} and is the period of time in between recurring billings
    // e.g. {2, MONTH} means a subscriber is billed once every 2 months
    JsonNode structuredContent = item.path("structuredContent");
    JsonNode node = structuredContent.path("subscriptionPlan").path("billingPeriod");
    return node;
  }

  public static String getUnitFromSubscriptionPlanBillingPeriod(JsonNode billingPeriodNode) {
    JsonNode node = billingPeriodNode.path("unit");
    return node.asText();
  }

  public static int getValueFromSubscriptionPlanBillingPeriod(JsonNode billingPeriodNode) {
    JsonNode node = billingPeriodNode.path("value");
    return node.asInt();
  }

  public static int getNumBillingCyclesFromSubscriptionPlanNode(JsonNode item) {
    JsonNode structuredContent = item.path("structuredContent");
    JsonNode node = structuredContent.path("subscriptionPlan").path("numBillingCycles");
    return node.asInt();
  }
  
  public static JsonNode getLowestPriceAmongVariants(JsonNode item) {
    ProductType type = getProductType(item);
    JsonNode structuredContent = item.path("structuredContent");
    switch (type) {

      case PHYSICAL:
      case SERVICE:
      case GIFT_CARD:
        JsonNode variants = structuredContent.path("variants");
        if (variants.size() == 0) {
          return DEFAULT_MONEY_NODE;
        }

        JsonNode first = variants.get(0);
        JsonNode moneyNode = isTruthy(first.path("onSale"))
            ? first.path("salePriceMoney")
            : first.path("priceMoney");
        BigDecimal price = getAmountFromMoneyNode(moneyNode);
        for (int i = 1; i < variants.size(); i++) {
          JsonNode var = variants.get(i);
          JsonNode currentMoneyNode = isTruthy(var.path("onSale"))
              ? var.path("salePriceMoney")
              : var.path("priceMoney");

          BigDecimal current = getAmountFromMoneyNode(currentMoneyNode);
          if (current.compareTo(price) < 0) {
            price = current;
            moneyNode = currentMoneyNode;
          }
        }
        return moneyNode;

      case DIGITAL:
        JsonNode digitalMoneyNode = structuredContent.path("priceMoney");
        return digitalMoneyNode.isMissingNode() ? DEFAULT_MONEY_NODE : digitalMoneyNode;

      default:
        return DEFAULT_MONEY_NODE;
    }
  }
  
  public static JsonNode getHighestPriceAmongVariants(JsonNode item) {
    ProductType type = getProductType(item);
    JsonNode structuredContent = item.path("structuredContent");

    switch (type) {
      case PHYSICAL:
      case SERVICE:
      case GIFT_CARD:
        JsonNode variants = structuredContent.path("variants");
        if (variants.size() == 0) {
          return DEFAULT_MONEY_NODE;
        }
        JsonNode moneyNode = variants.get(0).path("priceMoney");
        BigDecimal price = getAmountFromMoneyNode(moneyNode);
        for (int i = 1; i < variants.size(); i++) {
          JsonNode currMoneyNode = variants.get(i).path("priceMoney");
          BigDecimal curr = getAmountFromMoneyNode(currMoneyNode);
          if (curr.compareTo(price) > 0) {
            price = curr;
            moneyNode = currMoneyNode;
          }
        }
        return moneyNode;

      case DIGITAL:
        JsonNode digitalMoneyNode = structuredContent.path("priceMoney");
        return digitalMoneyNode.isMissingNode() ? DEFAULT_MONEY_NODE : digitalMoneyNode;

      default:
        return DEFAULT_MONEY_NODE;
    }
  }

  public static JsonNode getSalePriceMoneyNode(JsonNode item) {
    ProductType type = getProductType(item);
    JsonNode structuredContent = item.path("structuredContent");
    switch (type) {
      case PHYSICAL:
      case SERVICE:
        JsonNode variants = structuredContent.path("variants");
        if (variants.size() == 0) {
          return DEFAULT_MONEY_NODE;
        }
        BigDecimal salePrice = null;
        JsonNode salePriceNode = null;
        for (int i = 0; i < variants.size(); i++) {
          JsonNode variant = variants.path(i);
          JsonNode priceMoney = variant.path("salePriceMoney");
          BigDecimal price = getAmountFromMoneyNode(priceMoney);
          if (isTruthy(variant.path("onSale")) && (salePriceNode == null || price.compareTo(salePrice) < 0)) {
            salePrice = price;
            salePriceNode = priceMoney;
          }
        }
        return (salePriceNode == null) ? DEFAULT_MONEY_NODE : salePriceNode;

      case DIGITAL:
        JsonNode digitalMoneyNode = structuredContent.path("salePriceMoney");
        return digitalMoneyNode.isMissingNode() ? DEFAULT_MONEY_NODE : digitalMoneyNode;

      case GIFT_CARD:
        // this should never happen

      default:
        return DEFAULT_MONEY_NODE;
    }
  }

  public static BigDecimal getAmountFromMoneyNode(JsonNode moneyNode) {
    String value = StringUtils.trimToNull(moneyNode.path("value").asText());
    return value == null ? DEFAULT_MONEY_AMOUNT : new BigDecimal(value);
  }

  public static String getCurrencyFromMoneyNode(JsonNode moneyNode) {
    String currency = StringUtils.trimToNull(moneyNode.path("currency").asText());
    return currency == null ? DEFAULT_CURRENCY : currency;
  }

  public static double getLegacyPriceFromMoneyNode(JsonNode moneyNode) {
    BigDecimal price = getAmountFromMoneyNode(moneyNode);
    return price.movePointRight(2).doubleValue();
  }

  public static double getTotalStockRemaining(JsonNode item) {
    ProductType type = getProductType(item);
    JsonNode structuredContent = item.path("structuredContent");


    if (EnumSet.of(ProductType.DIGITAL, ProductType.GIFT_CARD).contains(type)) {
      return Double.POSITIVE_INFINITY;
    } else {
      int total = 0;
      JsonNode variants = structuredContent.path("variants");
      for (int i = 0; i < variants.size(); i++) {
        JsonNode variant = variants.get(i);
        if (isTruthy(variant.path("unlimited"))) {
          return Double.POSITIVE_INFINITY;
        } else {
          total += variant.path("qtyInStock").asLong();
        }
      }
      return total;
    }
  }

  public static boolean hasVariedPrices(JsonNode item) {
    ProductType type = getProductType(item);
    JsonNode structuredContent = item.path("structuredContent");

    switch (type) {
      case PHYSICAL:
      case SERVICE:
      case GIFT_CARD:
        JsonNode variants = structuredContent.path("variants");
        JsonNode first = variants.get(0);
        for (int i = 1; i < variants.size(); i++) {
          JsonNode var = variants.get(i);
          boolean flag1 = !var.path("onSale").equals(first.path("onSale"));
          boolean flag2 = isTruthy(first.path("onSale")) && !var.path("salePrice").equals(first.path("salePrice"));
          boolean flag3 = !var.path("price").equals(first.path("price"));
          if (flag1 || flag2 || flag3) {
            return true;
          }
        }
        return false;
      case DIGITAL:
      default:
        return false;
    }
  }

  public static boolean hasVariants(JsonNode item) {
    JsonNode structuredContent = item.path("structuredContent");
    JsonNode variants = structuredContent.path("variants");
    ProductType type = getProductType(item);
    return type.equals(ProductType.DIGITAL) ? false : variants.size() > 1;
  }

  public static boolean isOnSale(JsonNode item) {
    ProductType type = getProductType(item);
    JsonNode structuredContent = item.path("structuredContent");

    switch (type) {
      case PHYSICAL:
      case SERVICE:
        JsonNode variants = structuredContent.path("variants");
        int size = variants.size();
        for (int i = 0; i < size; i++) {
          JsonNode variant = variants.get(i);
          if (isTruthy(variant.path("onSale"))) {
            return true;
          }
        }
        break;

      case DIGITAL:
        return isTruthy(structuredContent.path("onSale"));

      case GIFT_CARD:
      default:
        break;
    }
    return false;
  }

  public static boolean isSoldOut(JsonNode item) {
    ProductType type = getProductType(item);
    JsonNode structuredContent = item.path("structuredContent");

    switch (type) {
      case PHYSICAL:
      case SERVICE:
        JsonNode variants = structuredContent.path("variants");
        int size = variants.size();
        for (int i = 0; i < size; i++) {
          JsonNode variant = variants.get(i);
          if (isTruthy(variant.path("unlimited")) || variant.path("qtyInStock").asInt() > 0) {
            return false;
          }
        }
        return true;

      case DIGITAL:
      case GIFT_CARD:
        return false;

      default:
        return true;
    }
  }

  /**
   * Format money using legacy currency formatter
   */
  public static void writeLegacyMoneyString(double value, StringBuilder buf) {
    String formatted = PluginUtils.formatMoney(value, Locale.US);
    buf.append("<span class=\"sqs-money-native\">").append(formatted).append("</span>");
  }

  /**
   * Format money using CLDR currency formatter
   */
  public static String getCLDRMoneyString(BigDecimal amount, String currencyCode, CLDR.Locale locale) {
    return PluginUtils.formatMoney(amount, currencyCode, locale);
  }

  public static void writeVariantFormat(JsonNode variant, StringBuilder buf) {
    ArrayNode optionValues = (ArrayNode) variant.get("optionValues");
    if (optionValues == null) {
      return;
    }

    Iterator<JsonNode> iterator = optionValues.elements();
    List<String> values = new ArrayList<>();

    while (iterator.hasNext()) {
      JsonNode option = iterator.next();
      values.add(option.get("value").asText());
    }

    int size = values.size();
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buf.append(" / ");
      }
      buf.append(values.get(i));
    }
  }

  public static ArrayNode getItemVariantOptions(JsonNode item) {
    JsonNode structuredContent = item.path("structuredContent");
    JsonNode variants = structuredContent.path("variants");
    if (variants.size() <= 1) {
      return EMPTY_ARRAY;
    }

    ArrayNode userDefinedOptions = JsonUtils.createArrayNode();
    JsonNode ordering = structuredContent.path("variantOptionOrdering");
    for (int i = 0; i < ordering.size(); i++) {
      String optionName = ordering.path(i).asText();
      ObjectNode option = JsonUtils.createObjectNode();
      option.put("name", optionName);
      option.put("values", JsonUtils.createArrayNode());
      userDefinedOptions.add(option);
    }

    for (int i = 0; i < variants.size(); i++) {
      JsonNode variant = variants.path(i);
      JsonNode attributes = variant.get("attributes");
      if (attributes == null) {
        continue;
      }
      Iterator<String> fields = attributes.fieldNames();

      while (fields.hasNext()) {
        String field = fields.next();

        String variantOptionValue = attributes.get(field).asText();
        ObjectNode userDefinedOption = null;

        for (int j = 0; j < userDefinedOptions.size(); j++) {
          ObjectNode current = (ObjectNode)userDefinedOptions.get(j);
          if (current.get("name").asText().equals(field)) {
            userDefinedOption = current;
          }
        }

        if (userDefinedOption != null) {
          boolean hasOptionValue = false;
          ArrayNode optionValues = (ArrayNode)userDefinedOption.get("values");
          for (int k = 0; k < optionValues.size(); k++) {
            String optionValue = optionValues.get(k).asText();
            if (optionValue.equals(variantOptionValue)) {
              hasOptionValue = true;
              break;
            }
          }

          if (!hasOptionValue) {
            optionValues.add(variantOptionValue);
          }
        }
      }
    }
    return userDefinedOptions;
  }

  public static boolean isMultipleQuantityAllowedForServices(JsonNode websiteSettings) {

    boolean defaultValue = true;
    String fieldName = "multipleQuantityAllowedForServices";

    JsonNode storeSettings = websiteSettings.get("storeSettings");

    if (storeSettings == null || !storeSettings.hasNonNull(fieldName)) {
      return defaultValue;
    }

    return storeSettings.get(fieldName).asBoolean(defaultValue);
  }

}
