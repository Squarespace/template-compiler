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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.squarespace.cldr.CLDR;
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
import com.squarespace.template.Variable;
import com.squarespace.template.Variables;
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
    table.add(new ProductScarcityFormatter());
    table.add(new ProductRestockNotificationFormatter());
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
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      var.set(executeTemplate(ctx, template, var.node(), false));
    }
  }

  protected static class CartQuantityFormatter extends BaseFormatter {

    public CartQuantityFormatter() {
      super("cart-quantity", false);
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      int count = 0;
      JsonNode entriesNode = var.node().path("entries");
      for (int i = 0; i < entriesNode.size(); i++) {
        count += entriesNode.get(i).get("quantity").intValue();
      }

      StringBuilder buf = new StringBuilder();
      buf.append("<span class=\"sqs-cart-quantity\">").append(count).append("</span>");
      var.set(buf);
    }
  }

  /**
   * @deprecated this formatter is not used internally anymore. Remove it when all external usage is cleared.
   */
  @Deprecated
  protected static class CartSubtotalFormatter extends BaseFormatter {

    public CartSubtotalFormatter() {
      super("cart-subtotal", false);
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      double subtotalCents = var.node().path("subtotalCents").doubleValue();

      StringBuilder buf = new StringBuilder();
      buf.append("<span class=\"sqs-cart-subtotal\">");
      CommerceUtils.writeLegacyMoneyString(subtotalCents, buf);
      buf.append("</span>");
      var.set(buf);
    }
  }

  protected static class CartUrlFormatter extends BaseFormatter {

    public CartUrlFormatter() {
      super("cart-url", false);
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      variables.first().set("/cart");
    }
  }

  /**
   * @deprecated this formatter is not used internally anymore. Remove it when all external usage is cleared.
   */
  @Deprecated
  protected static class FromPriceFormatter extends BaseFormatter {

    public FromPriceFormatter() {
      super("from-price", false);
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable item = variables.first();
      JsonNode moneyNode = CommerceUtils.getLowestPriceAmongVariants(item.node());
      double legacyPrice = CommerceUtils.getLegacyPriceFromMoneyNode(moneyNode);
      item.set(legacyPrice);
    }
  };

  /**
   * @deprecated this formatter is not used internally anymore. Remove it when all external usage is cleared.
   */
  @Deprecated
  protected abstract static class MoneyBaseFormatter extends BaseFormatter {

    public MoneyBaseFormatter(String identifier) {
      super(identifier, false);
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      double value = var.node().asDouble();
      var.set(PluginUtils.formatMoney(value, Locale.US));
    }
  }

  /**
   * @deprecated this formatter is not used internally anymore. Remove it when all external usage is cleared.
   */
  @Deprecated
  protected static class MoneyCamelFormatter extends MoneyBaseFormatter {

    public MoneyCamelFormatter() {
      super("moneyFormat");
    }

  }

  /**
   * @deprecated this formatter is not used internally anymore. Remove it when all external usage is cleared.
   */
  @Deprecated
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
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      double value = var.node().asDouble();
      var.set(PlatformUtils.formatBookkeeperMoney(value, Locale.US));
    }
  }

  /**
   * @deprecated this formatter is not used internally anymore. Remove it when all external usage is cleared.
   */
  @Deprecated
  protected static class MoneyStringFormatter extends BaseFormatter {

    public MoneyStringFormatter() {
      super("money-string", false);
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      double value = var.node().asDouble();
      StringBuilder buf = new StringBuilder();
      CommerceUtils.writeLegacyMoneyString(value, buf);
      var.set(buf);
    }
  }

  protected static class PercentageFormatter extends BaseFormatter {

    public PercentageFormatter() {
      super("percentage-format", false);
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      double value = var.node().asDouble();
      StringBuilder buf = new StringBuilder();
      boolean trim = args.count() > 0 && args.first().equals("trim");
      String formatted = PlatformUtils.formatPercentage(value, trim, Locale.US);
      buf.append(formatted);
      var.set(buf);
    }
  }

  /**
   * @deprecated this formatter is not used internally anymore. Remove it when all external usage is cleared.
   */
  @Deprecated
  protected static class NormalPriceFormatter extends BaseFormatter {

    public NormalPriceFormatter() {
      super("normal-price", false);
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable item = variables.first();
      JsonNode moneyNode = CommerceUtils.getHighestPriceAmongVariants(item.node());
      double legacyPrice = CommerceUtils.getLegacyPriceFromMoneyNode(moneyNode);
      item.set(legacyPrice);
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
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      var.set(executeTemplate(ctx, template, var.node(), false));
    }
  }

  protected static class ProductPriceFormatter extends BaseFormatter {

    private Instruction template;
    private static final String BILLING_PERIOD_MONTHLY = "MONTH";
    private static final String BILLING_PERIOD_WEEKLY = "WEEK";
    private static final String BILLING_PERIOD_YEARLY = "YEAR";
    private static final Map<String, Integer> PER_YEAR = new HashMap<>();
    static {
      PER_YEAR.put(BILLING_PERIOD_WEEKLY, 52);
      PER_YEAR.put(BILLING_PERIOD_MONTHLY, 12);
    }

    public ProductPriceFormatter() {
      super("product-price", false);
    }

    @Override
    public void initialize(Compiler compiler) throws CodeException {
      String source = loadResource(CommerceFormatters.class, "product-price.html");
      this.template = compiler.compile(source.trim()).code();
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      JsonNode node = var.node();
      StringBuilder buf = new StringBuilder();

      ObjectNode obj = JsonUtils.createObjectNode();

      if (CommerceUtils.isSubscribable(node)) {
        resolveTemplateVariablesForSubscriptionProduct(ctx, node, obj);
      } else if (CommerceUtils.getProductType(node) != ProductType.UNDEFINED) {
        resolveTemplateVariablesForOTPProduct(ctx, node, obj);
      }

      JsonNode priceInfo = executeTemplate(ctx, template, obj, true);
      buf.append(priceInfo.asText());
      var.set(buf);
    }

    private static void resolveTemplateVariablesForOTPProduct(Context ctx, JsonNode productNode, ObjectNode args) {
      if (CommerceUtils.hasVariedPrices(productNode)) {
        args.put("fromText", StringUtils.defaultIfEmpty(
            ctx.resolve(Constants.PRODUCT_PRICE_FROM_TEXT_KEY).asText(), "from {fromPrice}"));
        args.put("formattedFromPrice", getMoneyString(CommerceUtils.getLowestPriceAmongVariants(productNode), ctx));
      }

      if (CommerceUtils.isOnSale(productNode)) {
        args.put("formattedSalePriceText", "{price}");
        args.put("formattedSalePrice", getMoneyString(CommerceUtils.getSalePriceMoneyNode(productNode), ctx));
      }

      args.put("formattedNormalPriceText", "{price}");
      args.put("formattedNormalPrice", getMoneyString(CommerceUtils.getHighestPriceAmongVariants(productNode), ctx));
    }

    private static void resolveTemplateVariablesForSubscriptionProduct(
        Context ctx, JsonNode productNode, ObjectNode args) {
      JsonNode billingPeriodNode = CommerceUtils.getSubscriptionPlanBillingPeriodNode(productNode);

      if (billingPeriodNode.isMissingNode()) {
        args.put("formattedFromPrice", true);
        args.put("fromText", StringUtils.defaultIfEmpty(
            ctx.resolve(new String[] {"localizedStrings", "productPriceUnavailable"}).asText(), "Unavailable"));
        return;
      }

      boolean hasMultiplePrices = CommerceUtils.hasVariedPrices(productNode);
      int billingPeriodValue = CommerceUtils.getValueFromSubscriptionPlanBillingPeriod(billingPeriodNode);
      String billingPeriodUnit = CommerceUtils.getUnitFromSubscriptionPlanBillingPeriod(billingPeriodNode);

      int durationValue = billingPeriodValue * CommerceUtils.getNumBillingCyclesFromSubscriptionPlanNode(productNode);
      String durationUnit = billingPeriodUnit;

      // If the duration is a multiple of 52 weeks or 12 months, convert to years.
      // Otherwise, use the billing period unit for the duration unit.
      if (durationValue > 0 && PER_YEAR.containsKey(durationUnit) && durationValue % PER_YEAR.get(durationUnit) == 0) {
        durationValue /= PER_YEAR.get(durationUnit);
        durationUnit = BILLING_PERIOD_YEARLY;
      }

      args.put("billingPeriodValue", billingPeriodValue);
      args.put("duration", durationValue);

      // This string needs to match the correct translation template in v6 products-2.0-en-US.json.
      StringBuilder i18nKeyBuilder = new StringBuilder("productPrice")
          .append("__")
          .append(hasMultiplePrices ? "multiplePrices" : "singlePrice")
          .append("__")
          .append(billingPeriodValue == 1 ? "1" : "n")
          .append(StringUtils.capitalize(billingPeriodUnit.toLowerCase()) + "ly").append("__");

      if (durationValue == 0) {
        i18nKeyBuilder.append("indefinite");
      } else {
        i18nKeyBuilder.append("limited__")
            .append(durationValue == 1 ? "1" : "n")
            .append(StringUtils.capitalize(durationUnit.toLowerCase()) + "s");
      }

      String templateForPrice = StringUtils.defaultIfEmpty(
          ctx.resolve(new String[] {"localizedStrings",
              i18nKeyBuilder.toString()}).asText(), defaultSubscriptionPriceString(productNode));

      if (hasMultiplePrices) {
        args.put("fromText", templateForPrice);
        args.put("formattedFromPrice", getMoneyString(CommerceUtils.getLowestPriceAmongVariants(productNode), ctx));
      }

      if (CommerceUtils.isOnSale(productNode)) {
        args.put("formattedSalePriceText", templateForPrice);
        args.put("formattedSalePrice", getMoneyString(CommerceUtils.getSalePriceMoneyNode(productNode), ctx));
      }

      args.put("formattedNormalPriceText", templateForPrice);
      args.put("formattedNormalPrice", getMoneyString(CommerceUtils.getHighestPriceAmongVariants(productNode), ctx));
    }

    // TODO: This is shitty. The formatter should, if necessary, look up the English string and use it.
    private static String defaultSubscriptionPriceString(JsonNode productNode) {
      JsonNode billingPeriodNode = CommerceUtils.getSubscriptionPlanBillingPeriodNode(productNode);

      boolean hasMultiplePrices = CommerceUtils.hasVariedPrices(productNode);
      int billingPeriodValue = CommerceUtils.getValueFromSubscriptionPlanBillingPeriod(billingPeriodNode);
      boolean billingPeriodPlural = billingPeriodValue > 1;
      String billingPeriodUnit = CommerceUtils.getUnitFromSubscriptionPlanBillingPeriod(billingPeriodNode);
      int numBillingCycles = CommerceUtils.getNumBillingCyclesFromSubscriptionPlanNode(productNode);
      int durationValue = billingPeriodValue * numBillingCycles;
      String durationUnit = billingPeriodUnit;

      // If the duration is a multiple of 52 weeks or 12 months, convert to years.
      // Otherwise, use the billing period unit for the duration unit.
      if (durationValue > 0 && PER_YEAR.containsKey(durationUnit) && durationValue % PER_YEAR.get(durationUnit) == 0) {
        durationValue /= PER_YEAR.get(durationUnit);
        durationUnit = BILLING_PERIOD_YEARLY;
      }

      StringBuilder sb = new StringBuilder()
          .append(hasMultiplePrices ? "from " : "")
          .append("{price} every ")
          .append(billingPeriodPlural ? "{billingPeriodValue} " : "")
          .append(billingPeriodUnit.toLowerCase())
          .append(billingPeriodPlural ? "s" : "");

      if (numBillingCycles > 0) {
        sb.append(" for {duration} ")
            .append(durationUnit.toLowerCase())
            .append(durationValue == 1 ? "" : "s");
      }

      return sb.toString();
    }

    private static String getMoneyString(JsonNode moneyNode, Context ctx) {
      if (CommerceUtils.useCLDRMode(ctx)) {
        BigDecimal amount = CommerceUtils.getAmountFromMoneyNode(moneyNode);
        String currencyCode = CommerceUtils.getCurrencyFromMoneyNode(moneyNode);
        CLDR.Locale locale = ctx.cldrLocale();
        return CommerceUtils.getCLDRMoneyString(amount, currencyCode, locale);
      } else {
        double legacyAmount = CommerceUtils.getLegacyPriceFromMoneyNode(moneyNode);
        StringBuilder buf = new StringBuilder();
        CommerceUtils.writeLegacyMoneyString(legacyAmount, buf);
        return buf.toString();
      }
    }
  }

  protected static class ProductQuickViewFormatter extends BaseFormatter {

    public ProductQuickViewFormatter() {
      super("product-quick-view", false);
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      JsonNode node = var.node();
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
      buf.append("<span class=\"sqs-product-quick-view-button\" role=\"button\" tabindex=\"0\" data-id=\"").append(id);
      buf.append("\" data-group=\"").append(group).append("\">");
      String text = ctx.resolve(Constants.PRODUCT_QUICK_VIEW_TEXT_KEY).asText();
      buf.append(StringUtils.defaultIfEmpty(text, "Quick View"));
      buf.append("</span>");
      var.set(buf);
    }
  }

  protected static class ProductStatusFormatter extends BaseFormatter {

    public ProductStatusFormatter() {
      super("product-status", false);
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      JsonNode node = var.node();

      String productId = node.path("id").asText();
      JsonNode productCtx = ctx.resolve("productMerchandisingContext");
      String customSoldOutMessage = null;
      if (productId != null && productCtx != null) {
        customSoldOutMessage = productCtx.path(productId).path("customSoldOutText").asText();
      }

      StringBuilder buf = new StringBuilder();
      if (CommerceUtils.isSoldOut(node)) {
        String defaultSoldOutText = ctx.resolve(Constants.PRODUCT_SOLD_OUT_TEXT_KEY).asText();
        String defaultSoldOutMessage = StringUtils.defaultIfEmpty(defaultSoldOutText, "sold out");
        String soldOutMessage = StringUtils.defaultIfEmpty(customSoldOutMessage, defaultSoldOutMessage);
        buf.append("<div class=\"product-mark sold-out\">");
        buf.append(soldOutMessage);
        buf.append("</div>");
        var.set(buf);
      } else if (CommerceUtils.isOnSale(node)) {
        String text = ctx.resolve(Constants.PRODUCT_SALE_TEXT_KEY).asText();
        buf.append("<div class=\"product-mark sale\">");
        buf.append(StringUtils.defaultIfEmpty(text, "sale"));
        buf.append("</div>");
        var.set(buf);
      } else {
        var.setMissing();
      }
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
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      JsonNode node = var.node();
      ProductType type = CommerceUtils.getProductType(node);

      boolean multipleQuantityAllowed = (ProductType.PHYSICAL.equals(type)
          || (ProductType.SERVICE.equals(type)
             && CommerceUtils.isMultipleQuantityAllowedForServices(ctx.resolve("websiteSettings"))))
          && !CommerceUtils.isSubscribable(node);
      boolean hideQuantityInput = !multipleQuantityAllowed || CommerceUtils.getTotalStockRemaining(node) <= 1;

      if (hideQuantityInput) {
        var.setMissing();
        return;
      }
      var.set(executeTemplate(ctx, template, var.node(), false));
    }
  }

  protected static class SalePriceFormatter extends BaseFormatter {

    public SalePriceFormatter() {
      super("sale-price", false);
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      JsonNode moneyNode = CommerceUtils.getSalePriceMoneyNode(var.node());
      double legacyPrice = CommerceUtils.getLegacyPriceFromMoneyNode(moneyNode);
      var.set(legacyPrice);
    }
  }

  protected static class VariantDescriptorFormatter extends BaseFormatter {

    public VariantDescriptorFormatter() {
      super("variant-descriptor", false);
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      StringBuilder buf = new StringBuilder();
      CommerceUtils.writeVariantFormat(var.node(), buf);
      var.set(buf);
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
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      JsonNode node = var.node();

      ArrayNode options = CommerceUtils.getItemVariantOptions(node);
      if (options.size() == 0) {
        // Don't bother executing the template of nothing would be emitted.
        var.setMissing();
        return;
      }

      ObjectNode obj = JsonUtils.createObjectNode();
      obj.set("item", node);
      obj.set("options", options);
      obj.set("selectText", getSelectText(ctx, node));
      var.set(executeTemplate(ctx, template, obj, false));
    }

    private static TextNode getSelectText(Context ctx, JsonNode node) {
      ProductType productType = CommerceUtils.getProductType(node);
      // Gift Cards have variants forcibly named "Value" by default (as opposed to a merchant-defined variant name) and
      // thus must be translated differently than other products. See COM-4912 for more details
      if (productType == ProductType.GIFT_CARD) {
        String localizedSelectText = ctx.resolve(Constants.GIFT_CARD_VARIANT_SELECT_TEXT).asText();
        return new TextNode(StringUtils.defaultIfEmpty(localizedSelectText, "Select Value"));
      } else {
        String localizedSelectText = ctx.resolve(Constants.PRODUCT_VARIANT_SELECT_TEXT).asText();
        return new TextNode(StringUtils.defaultIfEmpty(localizedSelectText, "Select {variantName}"));
      }
    }
  }

  protected static class SummaryFormFieldFormatter extends BaseFormatter {

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
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      JsonNode field = var.node();
      String type = field.path("type").asText();
      Instruction code = templateMap.get(type);
      JsonNode value = null;
      if (code == null) {
        value = field.path("value");
      } else {
        JsonNode node = field;
        if (type.equals("likert")) {
          Map<String, String> answerMap = buildAnswerMap(ctx.resolve("localizedStrings"));
          node = convertLikert(field.path("values"), answerMap);
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
        String text = ctx.resolve(Constants.PRODUCT_SUMMARY_FORM_NO_ANSWER_TEXT_KEY).asText();
        buf.append(StringUtils.defaultIfEmpty(text, "N/A"));
      }
      buf.append("\n</div>");

      var.set(buf);
    }

    private static JsonNode convertLikert(JsonNode values, Map<String, String> answerMap) {
      ArrayNode result = JsonUtils.createArrayNode();
      Iterator<Entry<String, JsonNode>> likertFields = values.fields();
      while (likertFields.hasNext()) {
        Entry<String, JsonNode> likertField = likertFields.next();
        String answer = likertField.getValue().asText();
        ObjectNode node = JsonUtils.createObjectNode();
        node.put("question", likertField.getKey());
        node.put("answer", getOrDefault(answerMap, answer, answerMap.get("0")));
        result.add(node);
      }
      return result;
    }

    private static final String KEY_PREFIX = "productAnswerMap";
    private static final String KEY_STRONGLY_DISAGREE = KEY_PREFIX + "StronglyDisagree";
    private static final String KEY_DISAGREE = KEY_PREFIX + "Disagree";
    private static final String KEY_NEUTRAL = KEY_PREFIX + "Neutral";
    private static final String KEY_AGREE = KEY_PREFIX + "Agree";
    private static final String KEY_STRONGLY_AGREE = KEY_PREFIX + "StronglyAgree";

    private Map<String, String> buildAnswerMap(JsonNode strings) {
      Map<String, String> map = new HashMap<>();
      map.put("-2", GeneralUtils.localizeOrDefault(strings, KEY_STRONGLY_DISAGREE, "Strongly Disagree"));
      map.put("-1", GeneralUtils.localizeOrDefault(strings, KEY_DISAGREE, "Disagree"));
      map.put("0", GeneralUtils.localizeOrDefault(strings, KEY_NEUTRAL, "Neutral"));
      map.put("1", GeneralUtils.localizeOrDefault(strings, KEY_AGREE, "Agree"));
      map.put("2", GeneralUtils.localizeOrDefault(strings, KEY_STRONGLY_AGREE, "Strongly Agree"));
      return map;
    }
  }

  protected static class ProductScarcityFormatter extends BaseFormatter {

    private Instruction template;

    ProductScarcityFormatter() {
      super("product-scarcity", false);
    }

    @Override
    public void initialize(Compiler compiler) throws CodeException {
      String source = loadResource(CommerceFormatters.class, "product-scarcity.html");
      this.template = compiler.compile(source.trim()).code();
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      JsonNode productMerchandisingContext = ctx.resolve("productMerchandisingContext");
      Variable var = variables.first();
      JsonNode product = var.node();

      if (productMerchandisingContext.isMissingNode()) {
        return;
      }

      // Find the merchandising context for this product
      String productId = product.get("id").asText();
      JsonNode contextForProduct = productMerchandisingContext.path(productId);

      if (contextForProduct.isMissingNode()) {
        return;
      }

      if (contextForProduct.get("scarcityEnabled").asBoolean()) {
        ObjectNode templateVariables = JsonUtils.createObjectNode();
        templateVariables.put("scarcityTemplateViews", contextForProduct.get("scarcityTemplateViews"));
        templateVariables.put("scarcityText", contextForProduct.get("scarcityText"));
        templateVariables.put("scarcityShownByDefault", contextForProduct.get("scarcityShownByDefault"));
        var.set(executeTemplate(ctx, template, templateVariables, false));
      }
    }
  }

  protected static class ProductRestockNotificationFormatter extends BaseFormatter {

    private Instruction template;

    ProductRestockNotificationFormatter() {
      super("product-restock-notification", false);
    }

    @Override
    public void initialize(Compiler compiler) throws CodeException {
      String source = loadResource(CommerceFormatters.class, "product-restock-notification.html");
      this.template = compiler.compile(source.trim()).code();
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      JsonNode product = var.node();

      JsonNode websiteCtx = ctx.resolve("website");
      JsonNode productCtx = ctx.resolve("productMerchandisingContext");
      if (websiteCtx == null || productCtx == null) {
        return;
      }

      String productId = product.get("id").asText();
      ObjectNode templateVariables = JsonUtils.createObjectNode();
      templateVariables.set("product", product);
      templateVariables.set("views", productCtx.path(productId).path("restockNotificationViews"));
      templateVariables.set("messages", productCtx.path(productId).path("restockNotificationMessages"));
      templateVariables.set("mailingListSignUpEnabled", productCtx.path(productId).path("mailingListSignUpEnabled"));
      templateVariables.set("mailingListOptInByDefault", productCtx.path(productId).path("mailingListOptInByDefault"));
      templateVariables.set("captchaSiteKey", websiteCtx.path("captchaSettings").path("siteKey"));
      var.set(executeTemplate(ctx, template, templateVariables, true));
    }
  }

}
