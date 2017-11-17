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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squarespace.template.JsonUtils;
import com.squarespace.template.plugins.PluginUtils;
import com.squarespace.template.plugins.platform.enums.ProductType;


/**
 * Extracted from Commons library at commit ab4ba7a6f2b872a31cb6449ae9e96f5f5b30f471
 */
public class CommerceUtils {

  private static final ArrayNode EMPTY_ARRAY = JsonUtils.createArrayNode();

  private CommerceUtils() {
  }

  public static ProductType getProductType(JsonNode item) {
    JsonNode structuredContent = item.path("structuredContent");
    JsonNode node = structuredContent.path("productType");
    return ProductType.fromCode(node.asInt());
  }

  public static double getFromPrice(JsonNode item) {
    ProductType type = getProductType(item);
    JsonNode structuredContent = item.path("structuredContent");
    switch (type) {

      case PHYSICAL:
      case SERVICE:
      case GIFT_CARD:
        JsonNode variants = structuredContent.path("variants");
        if (variants.size() == 0) {
          return 0;
        }

        JsonNode first = variants.get(0);
        double price = isTruthy(first.path("onSale"))
            ? first.path("salePrice").asDouble()
            : first.path("price").asDouble();
        for (int i = 1; i < variants.size(); i++) {
          JsonNode var = variants.get(i);
          double current = isTruthy(var.path("onSale"))
              ? var.path("salePrice").asDouble()
              : var.path("price").asDouble();
          if (current < price) {
            price = current;
          }
        }
        return price;

      case DIGITAL:
        JsonNode cents = structuredContent.path("priceCents");
        return cents.isMissingNode() ? 0 : cents.asDouble();

      default:
        return 0.0;
    }
  }

  public static double getNormalPrice(JsonNode item) {
    ProductType type = getProductType(item);
    JsonNode structuredContent = item.path("structuredContent");

    switch (type) {
      case PHYSICAL:
      case SERVICE:
        JsonNode variants = structuredContent.path("variants");
        if (variants.size() == 0) {
          return 0;
        }
        double price = variants.get(0).path("price").asDouble();
        for (int i = 1; i < variants.size(); i++) {
          double curr = variants.get(i).path("price").asDouble();
          if (curr > price) {
            price = curr;
          }
        }
        return price;

      case DIGITAL:
        JsonNode cents = structuredContent.path("priceCents");
        return cents.isMissingNode() ? 0 : cents.asDouble();

      case GIFT_CARD:
        // this should never happen

      default:
        return 0.0;
    }
  }

  public static double getSalePrice(JsonNode item) {
    ProductType type = getProductType(item);
    JsonNode structuredContent = item.path("structuredContent");
    switch (type) {
      case PHYSICAL:
      case SERVICE:
        JsonNode variants = structuredContent.path("variants");
        if (variants.size() == 0) {
          return 0.0;
        }
        Double salePrice = null;
        for (int i = 0; i < variants.size(); i++) {
          JsonNode variant = variants.path(i);
          double price = variant.path("salePrice").asDouble();
          if (isTruthy(variant.path("onSale")) && (salePrice == null || price < salePrice)) {
            salePrice = price;
          }
        }
        return (salePrice == null) ? 0.0 : salePrice;

      case DIGITAL:
        JsonNode cents = structuredContent.path("salePriceCents");
        return cents.isMissingNode() ? 0.0 : cents.asDouble();

      case GIFT_CARD:
        // this should never happen

      default:
        return 0.0;
    }
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

  public static void writeMoneyString(double value, StringBuilder buf) {
    String formatted = PluginUtils.formatMoney(value, Locale.US);
    buf.append("<span class=\"sqs-money-native\">").append(formatted).append("</span>");
  }

  public static void writePriceString(JsonNode item, StringBuilder buf) {
    ProductType type = getProductType(item);
    double normalPrice = getNormalPrice(item);
    switch (type) {
      case PHYSICAL:
      case SERVICE:
        if (isOnSale(item)) {
          if (hasVariedPrices(item)) {
            buf.append("from ");
            writeMoneyString(getFromPrice(item), buf);
          } else {
            writeMoneyString(getSalePrice(item), buf);
            buf.append(" <span class=\"original-price\">");
            writeMoneyString(normalPrice, buf);
            buf.append("</span>");
          }
        } else if (hasVariedPrices(item)) {
          buf.append("from ");
          writeMoneyString(getFromPrice(item), buf);
        } else {
          writeMoneyString(normalPrice, buf);
        }
        break;

      case DIGITAL:
        if (isOnSale(item)) {
          writeMoneyString(getSalePrice(item), buf);
          buf.append(" <span class=\"original-price\">");
          writeMoneyString(normalPrice, buf);
          buf.append("</span>");
        } else {
          writeMoneyString(normalPrice, buf);
        }
        break;

      case GIFT_CARD:
        if (hasVariedPrices(item)) {
          buf.append("from ");
        }
        writeMoneyString(getFromPrice(item), buf);
        break;

      default:
        break;
    }
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
