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

import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.template.Arguments;
import com.squarespace.template.BaseFormatter;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Constants;
import com.squarespace.template.Context;
import com.squarespace.template.Formatter;
import com.squarespace.template.FormatterRegistry;
import com.squarespace.template.StringView;
import com.squarespace.template.SymbolTable;
import com.squarespace.template.plugins.PluginUtils;


public class CommerceFormatters implements FormatterRegistry {

  @Override
  public void registerFormatters(SymbolTable<StringView, Formatter> table) {
    table.add(ADD_TO_CART_BTN);
    table.add(BOOKKEEPER_FORMAT);
    table.add(CART_QUANTITY);
    table.add(CART_SUBTOTAL);
    table.add(CART_URL);
    table.add(COUPON_DESCRIPTOR);
    table.add(FROM_PRICE);
    table.add(MONEY_FORMAT_CAMEL);
    table.add(MONEY_FORMAT_DASH);
    table.add(MONEY_STRING);
    table.add(NORMAL_PRICE);
    table.add(PERCENTAGE_FORMAT);
    table.add(PRODUCT_CHECKOUT);
    table.add(PRODUCT_PRICE);
    table.add(PRODUCT_QUICK_VIEW);
    table.add(PRODUCT_STATUS);
    table.add(QUANTITY_INPUT);
    table.add(SALE_PRICE);
    table.add(SUMMARY_FORM_FIELD);
    table.add(VARIANT_DESCRIPTOR);
    table.add(VARIANTS_SELECT);
  }

  protected static final Formatter ADD_TO_CART_BTN = new BaseFormatter("add-to-cart-btn", false) {
    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      StringBuilder buf = new StringBuilder();
      CommerceUtils.writeAddToCartBtnString(node, buf);
      return ctx.buildNode(buf.toString());
    }
  };

  protected static final Formatter CART_QUANTITY = new BaseFormatter("cart-quantity", false) {
    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      StringBuilder buf = new StringBuilder();
      buf.append("<span class=\"sqs-cart-quantity\">");

      int count = 0;
      JsonNode entriesNode = node.path("entries");
      for (int i = 0; i < entriesNode.size(); i++) {
        count += entriesNode.get(i).get("quantity").intValue();
      }

      buf.append(count + "");
      buf.append("</span>");
      return ctx.buildNode(buf.toString());
    }
  };

  protected static final Formatter CART_SUBTOTAL = new BaseFormatter("cart-subtotal", false) {
    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      StringBuilder buf = new StringBuilder();
      buf.append("<span class=\"sqs-cart-subtotal\">");

      double subtotalCents = node.path("subtotalCents").doubleValue();
      CommerceUtils.writeMoneyString(subtotalCents, buf);

      buf.append("</span>");
      return ctx.buildNode(buf.toString());
    }
  };

  protected static final Formatter CART_URL = new BaseFormatter("cart-url", false) {
    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      return ctx.buildNode("/commerce/show-cart");
    }
  };

  protected static final Formatter COUPON_DESCRIPTOR = new BaseFormatter("coupon-descriptor", false) {
    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      StringBuilder buf = new StringBuilder();
      CommerceUtils.writeCouponDescriptor(node, buf);
      return ctx.buildNode(buf.toString());
    }
  };


  protected static final Formatter FROM_PRICE = new BaseFormatter("from-price", false) {
    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode item) throws CodeExecuteException {
      double price = CommerceUtils.getFromPrice(item);
      return ctx.buildNode(price);
    }
  };

  private static class MoneyFormatter extends BaseFormatter {

    public MoneyFormatter(String identifier) {
      super(identifier, false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      double value = node.asDouble();
      return ctx.buildNode(PluginUtils.formatMoney(value, Locale.US));
    }
  }

  protected static final Formatter MONEY_FORMAT_DASH = new MoneyFormatter("money-format");

  protected static final Formatter MONEY_FORMAT_CAMEL = new MoneyFormatter("moneyFormat");

  protected static final Formatter BOOKKEEPER_FORMAT = new BaseFormatter("bookkeeper-money-format", false) {
    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      double value = ctx.node().asDouble();
      return ctx.buildNode(PlatformUtils.formatBookkeeperMoney(value, Locale.US));
    }
  };

  protected static final Formatter MONEY_STRING = new BaseFormatter("money-string", false) {
    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      double value = node.asDouble();
      StringBuilder buf = new StringBuilder();
      CommerceUtils.writeMoneyString(value, buf);
      return ctx.buildNode(buf.toString());
    }
  };

  protected static final Formatter PERCENTAGE_FORMAT = new BaseFormatter("percentage-format", false) {
    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      double value = node.asDouble();
      StringBuilder buf = new StringBuilder();
      String formatted = PlatformUtils.formatPercentage(value, Locale.US);
      buf.append(formatted);
      return ctx.buildNode(buf.toString());
    }
  };

  protected static final Formatter NORMAL_PRICE = new BaseFormatter("normal-price", false) {
    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode item) throws CodeExecuteException {
      return ctx.buildNode(CommerceUtils.getNormalPrice(item));
    }
  };


  protected static final Formatter PRODUCT_CHECKOUT = new BaseFormatter("product-checkout", false) {
    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode item) throws CodeExecuteException {
      StringBuilder buf = new StringBuilder();
      CommerceUtils.writeVariantSelectString(item, buf);
      CommerceUtils.writeQuantityInputString(item, buf);
      CommerceUtils.writeAddToCartBtnString(item, buf);
      return ctx.buildNode(buf.toString());
    }
  };


  protected static final Formatter PRODUCT_PRICE = new BaseFormatter("product-price", false) {
    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      StringBuilder buf = new StringBuilder();
      buf.append("<div class=\"product-price\">");
      CommerceUtils.writePriceString(node, buf);
      buf.append("</div>");
      return ctx.buildNode(buf.toString());
    }
  };


  public static final Formatter PRODUCT_QUICK_VIEW = new BaseFormatter("product-quick-view", false) {

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      String id = node.path("id").asText();
      String group = args.isEmpty() ? "" : args.first();

      // check to see if the group is a key that lives in the context or higher up
      if (!node.path(group).isMissingNode()) {
        group = node.path(group).asText();
      } else if (!ctx.resolve(group).isMissingNode()) {
        group = ctx.resolve(group).asText();
      }

      return ctx.buildNode(String.format("<span class=\"sqs-product-quick-view-button "
          + "sqs-editable-button\" data-id=\"%s\" data-group=\"%s\">Quick View</span>", id, group));
    }

  };


  protected static final Formatter PRODUCT_STATUS = new BaseFormatter("product-status", false) {
    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode item) throws CodeExecuteException {
      if (CommerceUtils.isSoldOut(item)) {
        return ctx.buildNode("<div class=\"product-mark sold-out\">sold out</div>");
      } else if (CommerceUtils.isOnSale(item)) {
        return ctx.buildNode("<div class=\"product-mark sale\">sale</div>");
      }
      return Constants.MISSING_NODE;
    }
  };

  protected static final Formatter QUANTITY_INPUT = new BaseFormatter("quantity-input", false) {
    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      StringBuilder buf = new StringBuilder();
      CommerceUtils.writeQuantityInputString(node, buf);
      return ctx.buildNode(buf.toString());
    }
  };

  protected static final Formatter SALE_PRICE = new BaseFormatter("sale-price", false) {
    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode item) throws CodeExecuteException {
      return ctx.buildNode(CommerceUtils.getSalePrice(item));
    }
  };

  protected static final Formatter VARIANT_DESCRIPTOR = new BaseFormatter("variant-descriptor", false) {
    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      StringBuilder buf = new StringBuilder();
      CommerceUtils.writeVariantFormat(node, buf);
      return ctx.buildNode(buf.toString());
    }
  };

  protected static final Formatter VARIANTS_SELECT = new BaseFormatter("variants-select", false) {
    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      StringBuilder buf = new StringBuilder();
      CommerceUtils.writeVariantSelectString(node, buf);
      return ctx.buildNode(buf.toString());
    }
  };

  protected static final Formatter SUMMARY_FORM_FIELD = new BaseFormatter("summary-form-field", false) {
    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      StringBuilder buf = new StringBuilder();
      CommerceUtils.writeSummaryFormFieldString(node, buf);
      return ctx.buildNode(buf.toString());
    }
  };
}
