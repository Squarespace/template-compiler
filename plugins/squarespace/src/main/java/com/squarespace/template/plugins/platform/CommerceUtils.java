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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.squarespace.template.GeneralUtils;
import com.squarespace.template.MapBuilder;
import com.squarespace.template.MapFormat;
import com.squarespace.template.plugins.PluginUtils;
import com.squarespace.template.plugins.platform.enums.CommerceCouponType;
import com.squarespace.template.plugins.platform.enums.CommerceDiscountType;
import com.squarespace.template.plugins.platform.enums.ProductType;


/**
 * Extracted from Commons library at commit ed6b7ee3b23839afe998a23544dd6b2188b60fca
 */
public class CommerceUtils {

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
    if (type == null) {
      return 0.0;
    }
    switch (type) {

      case PHYSICAL:
      case SERVICE:
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

    if (type == null) {
      return 0.0;
    }
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

      default:
        return 0.0;
    }
  }

  public static double getSalePrice(JsonNode item) {
    ProductType type = getProductType(item);
    JsonNode structuredContent = item.path("structuredContent");
    if (type == null) {
      return 0.0;
    }
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

      default:
        return 0.0;
    }
  }

  public static double getTotalStockRemaining(JsonNode item) {
    ProductType type = getProductType(item);
    JsonNode structuredContent = item.path("structuredContent");

    if (ProductType.DIGITAL.equals(type)) {
      return Double.POSITIVE_INFINITY;
    } else if (ProductType.PHYSICAL.equals(type) || ProductType.SERVICE.equals(type)) {
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

    return 0;
  }

  public static boolean hasVariedPrices(JsonNode item) {
    ProductType type = getProductType(item);
    JsonNode structuredContent = item.path("structuredContent");

    if (type == null) {
      return false;
    }
    switch (type) {
      case PHYSICAL:
      case SERVICE:
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

    if (type == null) {
      return false;
    }
    return type.equals(ProductType.DIGITAL) ? false : variants.size() > 1;
  }

  public static boolean isOnSale(JsonNode item) {
    boolean onSale = false;

    ProductType type = getProductType(item);
    JsonNode structuredContent = item.path("structuredContent");

    if (type == null) {
      return onSale;
    }
    switch (type) {
      case PHYSICAL:
      case SERVICE:
        JsonNode variants = structuredContent.path("variants");
        for (int i = 0; i < variants.size(); i++) {
          JsonNode variant = variants.get(i);
          if (isTruthy(variant.path("onSale"))) {
            onSale = true;
            break;
          }
        }
        break;

      case DIGITAL:
        onSale = isTruthy(structuredContent.path("onSale"));
        break;

      default:
        break;
    }
    return onSale;
  }

  public static boolean isSoldOut(JsonNode item) {
    ProductType type = getProductType(item);
    JsonNode structuredContent = item.path("structuredContent");

    if (type == null) {
      return true;
    }
    switch (type) {
      case PHYSICAL:
      case SERVICE:
        JsonNode variants = structuredContent.path("variants");
        for (int i = 0; i < variants.size(); i++) {
          JsonNode variant = variants.get(i);
          if (isTruthy(variant.path("unlimited")) || variant.path("qtyInStock").asInt() > 0) {
            return false;
          }
        }
        return true;

      case DIGITAL:
        return false;

      default:
        return true;
    }
  }

  public static void writeAddToCartBtnString(JsonNode item, StringBuilder buf) {
    JsonNode structuredContent = item.path("structuredContent");

    String formId = structuredContent.path("additionalFieldsFormId").asText();
    boolean useForm = !StringUtils.isEmpty(formId) && structuredContent.has("additionalFieldsForm");

    HtmlElementBuilder buttonNode = new HtmlElementBuilder("div");

    buttonNode.addClass("sqs-add-to-cart-button sqs-suppress-edit-mode sqs-editable-button");

    if (useForm) {
      buttonNode.addClass("use-form");
    }

    boolean useCustom = structuredContent.path("useCustomAddButtonText").asBoolean();
    String buttonText = useCustom ? structuredContent.path("customAddButtonText").asText() : "Add To Cart";

    buttonNode.set("data-item-id", item.path("id").asText());
    buttonNode.set("data-original-label", buttonText);
    buttonNode.set("data-collection-id", item.path("collectionId").asText());

    HtmlElementBuilder buttonNodeInner = new HtmlElementBuilder("div");
    buttonNodeInner.addClass("sqs-add-to-cart-button-inner");
    buttonNodeInner.setContent(buttonText);

    buttonNode.appendChild(buttonNodeInner);

    if (useForm) {
      buttonNode.set("data-form", structuredContent.path("additionalFieldsForm").toString());
    }

    HtmlElementBuilder builder = new HtmlElementBuilder("div");
    builder.addClass("sqs-add-to-cart-button-wrapper");
    builder.appendChild(buttonNode);

    builder.render(buf);
  }

  private static final MapFormat ALL_ORDERS_AMT = new MapFormat("Save %(discountAmt)s on any order.");

  private static final MapFormat ALL_ORDERS_SHP = new MapFormat("Free shipping on any order.");

  private static final MapFormat ORDERS_OVER_AMT = new MapFormat(
      "Save %(discountAmt)s on any order over %(minPrice)s.");

  private static final MapFormat ORDERS_OVER_SHP = new MapFormat("Free shipping on any order over %(minPrice)s.");

  private static final MapFormat CATEGORIES_AMT = new MapFormat("Save %(discountAmt)s on select products.");

  private static final MapFormat SINGLE_PRODUCT_AMT = new MapFormat("Save %(discountAmt)s on %(productTitle)s .");

  public static void writeCouponDescriptor(JsonNode coupon, StringBuilder buf) {
    String minPrice = PluginUtils.formatMoney(coupon.path("minPrice").asDouble(), Locale.US);
    String productTitle = GeneralUtils.ifString(coupon.path("productTitle"), "?");
    double discountAmt = GeneralUtils.ifDouble(coupon.path("discountAmt"), 0.0);
    String discountAmtStr = (discountAmt > 0) ? Double.toString(discountAmt) : "?";

    MapBuilder<String, Object> tplData = new MapBuilder<>();
    tplData.put("minPrice", minPrice).put("productTitle", productTitle).put("discountAmt", discountAmtStr);

    CommerceCouponType type = CommerceCouponType.fromCode(coupon.path("type").asInt());
    if (type == null) {
      return;
    }
    MapFormat amountTpl = null;
    MapFormat freeShipTpl = null;

    switch (type) {
      case ALL_ORDERS:
        amountTpl = ALL_ORDERS_AMT;
        freeShipTpl = ALL_ORDERS_SHP;
        break;
      case ORDERS_OVER:
        amountTpl = ORDERS_OVER_AMT;
        freeShipTpl = ORDERS_OVER_SHP;
        break;

      case CATEGORIES:
        amountTpl = CATEGORIES_AMT;
        break;

      case SINGLE_PRODUCT:
        amountTpl = SINGLE_PRODUCT_AMT;
        break;

      default:
        return;
    }

    CommerceDiscountType discountType = CommerceDiscountType.fromCode(coupon.path("discountType").asInt());
    if (discountType == null) {
      return;
    }

    switch (discountType) {
      case FLAT:
        tplData.put("discountAmt", PluginUtils.formatMoney(discountAmt, Locale.US));
        break;

      case PERCENTAGE:
        tplData.put("discountAmt", discountAmt + "%");
        break;

      case FREE_SHIPPING:
        if (freeShipTpl != null) {
          buf.append(freeShipTpl.apply(tplData.get()));
        }
        return;

      default:
        return;
    }
    buf.append(amountTpl.apply(tplData.get()));
  }

  public static void writeMoneyString(double value, StringBuilder buf) {
    String formatted = PluginUtils.formatMoney(value, Locale.US);
    buf.append("<span class=\"sqs-money-native\">").append(formatted).append("</span>");
  }

  public static void writePriceString(JsonNode item, StringBuilder buf) {
    ProductType type = getProductType(item);
    if (type == null) {
      return;
    }
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

      default:
        break;
    }
  }

  public static void writeQuantityInputString(JsonNode item, StringBuilder buf) {
    ProductType type = getProductType(item);
    if (!ProductType.PHYSICAL.equals(type)) {
      return;
    }

    if (getTotalStockRemaining(item) <= 1) {
      return;
    }

    String itemId = item.path("id").asText();
    buf.append("<div class=\"product-quantity-input\" data-item-id=\"");
    buf.append(itemId).append("\">");
    buf.append("<div class=\"quantity-label\">Quantity:</div>");
    buf.append("<input size=\"4\" max=\"9999\" min=\"1\" value=\"1\" type=\"number\" step=\"1\"></input>");
    buf.append("</div>");
  }

  public static void writeVariantFormat(JsonNode variant, StringBuilder buf) {
    if (variant == null) {
      return;
    }

    ArrayNode optionValuesJson = (ArrayNode) variant.get("optionValues");

    if (optionValuesJson == null) {
      return;
    }

    Iterator<JsonNode> optionValuesIterator = optionValuesJson.elements();
    List<String> variantValues = new ArrayList<>();

    while (optionValuesIterator.hasNext()) {
      JsonNode optionValueJson = optionValuesIterator.next();
      variantValues.add(optionValueJson.get("value").asText());
    }

    for (int i = 0; i < variantValues.size(); i++) {

      if (i > 0) {
        buf.append(" / ");
      }

      buf.append(variantValues.get(i));
    }
  }

  public static void writeVariantSelectString(JsonNode item, StringBuilder buf) {
    JsonNode structuredContent = item.path("structuredContent");
    JsonNode variants = structuredContent.path("variants");
    if (variants.size() <= 1) {
      return;
    }
    List<VariantOption> userDefinedOptions = new ArrayList<>();
    JsonNode ordering = structuredContent.path("variantOptionOrdering");
    for (int i = 0; i < ordering.size(); i++) {
      String optionName = ordering.path(i).asText();
      userDefinedOptions.add(new VariantOption(optionName));
    }

    for (int i = 0; i < variants.size(); i++) {
      JsonNode variant = variants.path(i);
      if (variant == null) {
        continue;
      }
      JsonNode attributes = variant.get("attributes");
      if (attributes == null) {
        continue;
      }
      Iterator<String> fields = attributes.fieldNames();

      while (fields.hasNext()) {
        String field = fields.next();

        String variantOptionValue = variant.get("attributes").get(field).asText();
        VariantOption userDefinedOption = null;

        for (int j = 0; j < userDefinedOptions.size(); j++) {
          VariantOption current = userDefinedOptions.get(j);
          if (current.name.equals(field)) {
            userDefinedOption = current;
          }
        }

        if (userDefinedOption != null) {
          boolean hasOptionValue = false;
          List<String> optionValues = userDefinedOption.values;
          for (int k = 0; k < optionValues.size(); k++) {
            String optionValue = optionValues.get(k);
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

    String itemId = item.path("id").asText();
    buf.append("<div class=\"product-variants\" data-item-id=\"").append(itemId);
    buf.append("\" data-variants=\"");
    PluginUtils.escapeHtmlTag(variants.toString(), buf);
    buf.append("\">");

    // Assemble the select block.
    for (int i = 0; i < userDefinedOptions.size(); i++) {
      VariantOption userDefinedOption = userDefinedOptions.get(i);
      String name = userDefinedOption.name;
      List<String> values = userDefinedOption.values;

      buf.append("<div class=\"variant-option\">");
      buf.append("<div class=\"variant-option-title\">").append(name).append(": </div>");
      buf.append("<div class=\"variant-select-wrapper\">");
      buf.append("<select data-variant-option-name=\"");
      PluginUtils.escapeHtmlTag(name, buf);
      buf.append("\"><option value=\"\">Select ").append(name).append("</option>");

      for (int j = 0; j < values.size(); j++) {
        String value = values.get(j);
        buf.append("<option value=\"");
        PluginUtils.escapeHtmlTag(value, buf);
        buf.append("\">").append(value).append("</option>");
      }

      buf.append("</select></div></div>");
    }

    buf.append("</div>");
  }

  public static void writeSummaryFormFieldString(JsonNode field, StringBuilder buf) {
    String fieldType = field.path("type").asText();
    JsonNode values = field.path("values");
    String valueString = null;

    switch (fieldType) {
      case "name":
      {
        valueString = values.path("First").asText() + " " + values.path("Last").asText();
        break;
      }

      case "phone":
      {
        String countryCode = StringUtils.trimToNull(values.path("Country").asText());

        valueString = "";

        if (countryCode != null) {
          valueString += "+" + countryCode + " ";
        }

        String areaCode = values.path("Areacode").asText();
        String prefix = values.path("Prefix").asText();
        String line = values.path("Line").asText();
        valueString += String.format("%s-%s-%s", areaCode, prefix, line);
        break;
      }

      case "likert":
      {
        valueString = "<div style=\"padding-left:5px;\">";

        Iterator<Entry<String, JsonNode>> likertFields = values.fields();
        while (likertFields.hasNext()) {
          Entry<String, JsonNode> likertField = likertFields.next();

          String question = likertField.getKey();
          String answer = likertField.getValue().asText();
          String likeStr = getStringForLikertValue(answer);
          valueString += String.format("<div><span style=\"font-weight:bold;\">%s:</span> %s</div>", question, likeStr);
        }

        valueString += "</div>";
        break;
      }

      case "address":
      {
        valueString = "<div style=\"padding-left:5px;\"><div>" + values.path("Line1").asText() + "</div>";

        String line2 = StringUtils.trimToNull(values.path("Line2").asText());

        if (line2 != null) {
          valueString += "<div>" + line2 + "</div>";
        }

        String city = values.path("City").asText();
        String state = values.path("State").asText();
        String zip = values.path("Zip").asText();
        String country = values.path("Country").asText();
        valueString += String.format("<div>%s, %s %s %s</div>", city, state, zip, country);
        valueString += "</div>";
        break;
      }

      case "date":
      {
        String month = values.path("Month").asText();
        String day = values.path("Day").asText();
        String year = values.path("Year").asText();
        valueString = String.format("%s/%s/%s", month, day, year);
        break;
      }

      case "time":
      {
        String hour = StringUtils.trimToNull(values.path("Hour").asText());
        String minute = StringUtils.trimToNull(values.path("Minute").asText());
        String second = StringUtils.trimToNull(values.path("Second").asText());

        if (hour == null) {
          hour = "00";
        }

        if (minute == null) {
          minute = "00";
        }

        if (second == null) {
          second = "00";
        }

        valueString = String.format("%s:%s:%s %s", hour, minute, second, values.path("Ampm"));
        break;
      }

      case "checkbox":
      {
        /**
         * Because form submissions are not modeled, we have no easy way to migrate bad checkbox submissions
         * (value -> values). We need this fix.
         */
        if (values.isMissingNode() && field.has("value")) {
          valueString = field.path("value").asText();
          break;
        }

        List<String> valuesList = new ArrayList<>();

        for (JsonNode value : values) {
          valuesList.add(value.asText());
        }

        valueString = StringUtils.join(valuesList, ", ");
        break;
      }

      default:
        valueString = field.path("value").asText();
        break;
    }

    if (valueString == null || valueString.trim().isEmpty()) {
      valueString = "N/A";
    }

    String rawTitle = field.path("rawTitle").asText();
    buf.append("<div class=\"foobar\" style=\"font-size:11px; margin-top:3px\">");
    buf.append(String.format("<span style=\"font-weight:bold;\">%s:</span> %s", rawTitle, valueString));
    buf.append("</div>");
  }

  private static String getStringForLikertValue(String value) {
    String string = "Neutral";

    switch (value) {
      case "-2":
        string = "Strongly Disagree";
        break;

      case "-1":
        string = "Disagree";
        break;

      case "0":
        string = "Neutral";
        break;

      case "1":
        string = "Agree";
        break;

      case "2":
        string = "Strongly Agree";
        break;

      default:
        break;
    }

    return string;
  }

  private static class VariantOption {

    public String name;

    public List<String> values = new ArrayList<>();

    public VariantOption(String name) {
      this.name = name;
    }
  }

}
