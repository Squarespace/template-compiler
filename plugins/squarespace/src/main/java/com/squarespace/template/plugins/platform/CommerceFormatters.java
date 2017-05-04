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

import static com.squarespace.template.GeneralUtils.executeTemplate;
import static com.squarespace.template.GeneralUtils.getOrDefault;
import static com.squarespace.template.GeneralUtils.loadResource;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squarespace.template.Arguments;
import com.squarespace.template.BaseFormatter;
import com.squarespace.template.CodeException;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Compiler;
import com.squarespace.template.Constants;
import com.squarespace.template.Context;
import com.squarespace.template.Formatter;
import com.squarespace.template.FormatterRegistry;
import com.squarespace.template.GeneralUtils;
import com.squarespace.template.Instruction;
import com.squarespace.template.JsonUtils;
import com.squarespace.template.StringView;
import com.squarespace.template.SymbolTable;
import com.squarespace.template.plugins.PluginUtils;
import com.squarespace.template.plugins.platform.enums.ProductType;


/**
 * Extracted from Commons library at commit ab4ba7a6f2b872a31cb6449ae9e96f5f5b30f471
 */
public class CommerceFormatters implements FormatterRegistry {

  @Override
  public void registerFormatters(SymbolTable<StringView, Formatter> table) {
    table.add(new AddToCartButtonFormatter());
    table.add(new BookkeeperMoneyFormatter());
    table.add(new CartQuantityFormatter());
    table.add(new CartSubtotalFormatter());
    table.add(new CartUrlFormatter());
    table.add(new CouponDescriptorFormatter());
    table.add(new FromPriceFormatter());
    table.add(new MoneyCamelFormatter());
    table.add(new MoneyDashFormatter());
    table.add(new MoneyStringFormatter());
    table.add(new NormalPriceFormatter());
    table.add(new PercentageFormatter());
    table.add(new ProductCheckoutFormatter());
    table.add(new ProductPriceFormatter());
    table.add(new ProductQuickViewFormatter());
    table.add(new ProductStatusFormatter());
    table.add(new QuantityInputFormatter());
    table.add(new SalePriceFormatter());
    table.add(new SummaryFormFieldFormatter());
    table.add(new VariantDescriptorFormatter());
    table.add(new VariantsSelectFormatter());
  }

  protected static class AddToCartButtonFormatter extends BaseFormatter {

    private Instruction template;

    public AddToCartButtonFormatter() {
      super("add-to-cart-btn", false);
    }

    @Override
    public void initialize(Compiler compiler) throws CodeException {
      String source = loadResource(CommerceFormatters.class, "add-to-cart-btn.html");
      this.template = compiler.compile(source).code();
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      return executeTemplate(ctx, template, node, true);
    }
  }

  protected static class CartQuantityFormatter extends BaseFormatter {

    public CartQuantityFormatter() {
      super("cart-quantity", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      int count = 0;
      JsonNode entriesNode = node.path("entries");
      for (int i = 0; i < entriesNode.size(); i++) {
        count += entriesNode.get(i).get("quantity").intValue();
      }

      StringBuilder buf = new StringBuilder();
      buf.append("<span class=\"sqs-cart-quantity\">").append(count).append("</span>");
      return ctx.buildNode(buf.toString());
    }
  }

  protected static class CartSubtotalFormatter extends BaseFormatter {

    public CartSubtotalFormatter() {
      super("cart-subtotal", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      double subtotalCents = node.path("subtotalCents").doubleValue();

      StringBuilder buf = new StringBuilder();
      buf.append("<span class=\"sqs-cart-subtotal\">");
      CommerceUtils.writeMoneyString(subtotalCents, buf);
      buf.append("</span>");
      return ctx.buildNode(buf.toString());
    }
  }

  protected static class CartUrlFormatter extends BaseFormatter {

    public CartUrlFormatter() {
      super("cart-url", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      return ctx.buildNode("/cart");
    }
  }

  protected static class CouponDescriptorFormatter extends BaseFormatter {

    public CouponDescriptorFormatter() {
      super("coupon-descriptor", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      StringBuilder buf = new StringBuilder();
      CommerceUtils.writeCouponDescriptor(node, buf);
      return ctx.buildNode(buf.toString());
    }
  };

  protected static class FromPriceFormatter extends BaseFormatter {

    public FromPriceFormatter() {
      super("from-price", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode item) throws CodeExecuteException {
      double price = CommerceUtils.getFromPrice(item);
      return ctx.buildNode(price);
    }
  };

  protected abstract static class MoneyBaseFormatter extends BaseFormatter {

    public MoneyBaseFormatter(String identifier) {
      super(identifier, false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      double value = node.asDouble();
      return ctx.buildNode(PluginUtils.formatMoney(value, Locale.US));
    }
  }

  protected static class MoneyCamelFormatter extends MoneyBaseFormatter {

    public MoneyCamelFormatter() {
      super("moneyFormat");
    }

  }

  protected static class MoneyDashFormatter extends MoneyBaseFormatter {

    public MoneyDashFormatter() {
      super("money-format");
    }
  }

  protected static class BookkeeperMoneyFormatter extends BaseFormatter {

    public BookkeeperMoneyFormatter() {
      super("bookkeeper-money-format", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      double value = ctx.node().asDouble();
      return ctx.buildNode(PlatformUtils.formatBookkeeperMoney(value, Locale.US));
    }
  }

  protected static class MoneyStringFormatter extends BaseFormatter {

    public MoneyStringFormatter() {
      super("money-string", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      double value = node.asDouble();
      StringBuilder buf = new StringBuilder();
      CommerceUtils.writeMoneyString(value, buf);
      return ctx.buildNode(buf.toString());
    }
  }

  protected static class PercentageFormatter extends BaseFormatter {

    public PercentageFormatter() {
      super("percentage-format", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      double value = node.asDouble();
      StringBuilder buf = new StringBuilder();
      boolean trim = args.count() > 0 && args.first().equals("trim");
      String formatted = PlatformUtils.formatPercentage(value, trim, Locale.US);
      buf.append(formatted);
      return ctx.buildNode(buf.toString());
    }
  }

  protected static class NormalPriceFormatter extends BaseFormatter {

    public NormalPriceFormatter() {
      super("normal-price", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode item) throws CodeExecuteException {
      return ctx.buildNode(CommerceUtils.getNormalPrice(item));
    }
  }

  protected static class ProductCheckoutFormatter extends BaseFormatter {

    private static final String SOURCE = "{@|variants-select}{@|quantity-input}{@|add-to-cart-btn}";

    private Instruction template;

    public ProductCheckoutFormatter() {
      super("product-checkout", false);
    }

    @Override
    public void initialize(Compiler compiler) throws CodeException {
      this.template = compiler.compile(SOURCE).code();
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode item) throws CodeExecuteException {
      return executeTemplate(ctx, template, item, false);
    }
  }

  protected static class ProductPriceFormatter extends BaseFormatter {

    public ProductPriceFormatter() {
      super("product-price", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      StringBuilder buf = new StringBuilder();
      buf.append("<div class=\"product-price\">");
      CommerceUtils.writePriceString(node, buf);
      buf.append("</div>");
      return ctx.buildNode(buf.toString());
    }
  }

  protected static class ProductQuickViewFormatter extends BaseFormatter {

    public ProductQuickViewFormatter() {
      super("product-quick-view", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      String id = node.path("id").asText();
      String group = args.isEmpty() ? "" : args.first();

      // check to see if the group is a key that lives in the context or higher up
      JsonNode groupNode = node.path(group);
      if (!groupNode.isMissingNode()) {
        group = groupNode.asText();
      } else {
        groupNode = ctx.resolve(group);
        if (!groupNode.isMissingNode()) {
          group = groupNode.asText();
        }
      }

      StringBuilder buf = new StringBuilder();
      buf.append("<span class=\"sqs-product-quick-view-button\" data-id=\"").append(id);
      buf.append("\" data-group=\"").append(group);
      buf.append("\">Quick View</span>");
      return ctx.buildNode(buf.toString());
    }
  }

  protected static class ProductStatusFormatter extends BaseFormatter {

    public ProductStatusFormatter() {
      super("product-status", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode item) throws CodeExecuteException {
      if (CommerceUtils.isSoldOut(item)) {
        return ctx.buildNode("<div class=\"product-mark sold-out\">sold out</div>");
      } else if (CommerceUtils.isOnSale(item)) {
        return ctx.buildNode("<div class=\"product-mark sale\">sale</div>");
      }
      return Constants.MISSING_NODE;
    }
  }

  protected static class QuantityInputFormatter extends BaseFormatter {

    private Instruction template;

    public QuantityInputFormatter() {
      super("quantity-input", false);
    }

    @Override
    public void initialize(Compiler compiler) throws CodeException {
      String source = loadResource(CommerceFormatters.class, "quantity-input.html");
      this.template = compiler.compile(source).code();
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode item) throws CodeExecuteException {

      ProductType type = CommerceUtils.getProductType(item);

      boolean multipleQuantityAllowed = ProductType.PHYSICAL.equals(type)
          || (ProductType.SERVICE.equals(type)
             && CommerceUtils.isMultipleQuantityAllowedForServices(ctx.resolve("websiteSettings")));
      boolean hideQuantityInput = !multipleQuantityAllowed || CommerceUtils.getTotalStockRemaining(item) <= 1;

      if (hideQuantityInput) {
        return MissingNode.getInstance();
      }
      return executeTemplate(ctx, template, item, true);
    }
  }

  protected static class SalePriceFormatter extends BaseFormatter {

    public SalePriceFormatter() {
      super("sale-price", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode item) throws CodeExecuteException {
      return ctx.buildNode(CommerceUtils.getSalePrice(item));
    }
  }

  protected static class VariantDescriptorFormatter extends BaseFormatter {

    public VariantDescriptorFormatter() {
      super("variant-descriptor", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      StringBuilder buf = new StringBuilder();
      CommerceUtils.writeVariantFormat(node, buf);
      return ctx.buildNode(buf.toString());
    }
  }

  protected static class VariantsSelectFormatter extends BaseFormatter {

    private Instruction template;

    public VariantsSelectFormatter() {
      super("variants-select", false);
    }

    @Override
    public void initialize(Compiler compiler) throws CodeException {
      String source = loadResource(CommerceFormatters.class, "variants-select.html");
      this.template = compiler.compile(source).code();
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode item) throws CodeExecuteException {
      ArrayNode options = CommerceUtils.getItemVariantOptions(item);
      if (options.size() == 0) {
        // Don't bother executing the template of nothing would be emitted.
        return MissingNode.getInstance();
      }

      ObjectNode node = JsonUtils.createObjectNode();
      node.put("item", item);
      node.put("options", options);
      return executeTemplate(ctx, template, node, false);
    }
  }

  protected static class SummaryFormFieldFormatter extends BaseFormatter {

    private static final Map<String, String> ANSWER_MAP = buildAnswerMap();

    private static final String[] TEMPLATES = new String[] {
      "address", "checkbox", "date", "likert", "name", "phone", "time"
    };

    private final Map<String, Instruction> templateMap = new HashMap<>();

    public SummaryFormFieldFormatter() {
      super("summary-form-field", false);
    }

    @Override
    public void initialize(Compiler compiler) throws CodeException {
      for (String type : TEMPLATES) {
        String source = loadResource(CommerceFormatters.class, "summary-form-field-" + type + ".html");
        Instruction code = compiler.compile(source.trim()).code();
        templateMap.put(type, code);
      }
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode field) throws CodeExecuteException {
      String type = field.path("type").asText();
      Instruction code = templateMap.get(type);
      JsonNode value = null;
      if (code == null) {
        value = field.path("value");
      } else {
        JsonNode node = field;
        if (type.equals("likert")) {
          node = convertLikert(field.path("values"));
        }
        value = executeTemplate(ctx, code, node, true);
      }

      // Assemble the HTML form wrapper containing the rendered value.
      StringBuilder buf = new StringBuilder();
      buf.append("<div style=\"font-size:11px; margin-top:3px\">\n");
      buf.append("  <span style=\"font-weight:bold;\">");
      buf.append(field.path("rawTitle").asText());
      buf.append(":</span> ");
      if (GeneralUtils.isTruthy(value)) {
        buf.append(value.asText());
      } else {
        buf.append("N/A");
      }
      buf.append("\n</div>");
      return ctx.buildNode(buf.toString());
    }

    private static JsonNode convertLikert(JsonNode values) {
      ArrayNode result = JsonUtils.createArrayNode();
      Iterator<Entry<String, JsonNode>> likertFields = values.fields();
      while (likertFields.hasNext()) {
        Entry<String, JsonNode> likertField = likertFields.next();
        String answer = likertField.getValue().asText();
        ObjectNode node = JsonUtils.createObjectNode();
        node.put("question", likertField.getKey());
        node.put("answer", getOrDefault(ANSWER_MAP, answer, "Neutral"));
        result.add(node);
      }
      return result;
    }

    private static Map<String, String> buildAnswerMap() {
      Map<String, String> map = new HashMap<>();
      map.put("-2", "Strongly Disagree");
      map.put("-1", "Disagree");
      map.put("0", "Neutral");
      map.put("1", "Agree");
      map.put("2", "Strongly Agree");
      return map;
    }
  }

}
