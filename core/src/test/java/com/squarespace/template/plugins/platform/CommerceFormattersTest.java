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

import org.testng.annotations.Test;

import com.squarespace.template.CodeException;
import com.squarespace.template.Formatter;
import com.squarespace.template.TestSuiteRunner;
import com.squarespace.template.plugins.platform.CommerceFormatters.MoneyDashFormatter;


/**
 * Extracted from Commons library at commit ab4ba7a6f2b872a31cb6449ae9e96f5f5b30f471
 */
public class CommerceFormattersTest extends PlatformUnitTestBase {

  private static final Formatter MONEY_DASH_FORMAT = new MoneyDashFormatter();

  private final TestSuiteRunner runner = new TestSuiteRunner(compiler(), CommerceFormattersTest.class);

  @Test
  public void testAddToCartButton() {
    runner.exec("f-add-to-cart-btn-%N.html");
  }

  @Test
  public void testBookkeeperMoneyFormat() {
    runner.exec("f-bookkeeper-money-format-%N.html");
  }

  @Test
  public void testCartQuantity() {
    runner.exec("f-cart-quantity-%N.html");
  }

  @Test
  public void testCartSubtotal() {
    runner.exec("f-cart-subtotal-%N.html");
  }

  @Test
  public void testCartUrl() {
    runner.exec("f-cart-url-%N.html");
  }

  @Test
  public void testFromPrice() {
    runner.exec("f-from-price-%N.html");
  }

  @Test
  public void testMoneyFormatCamel() {
    runner.exec("f-money-format-camel-%N.html");
  }

  @Test
  public void testMoneyFormat() throws CodeException {
    assertFormatter(MONEY_DASH_FORMAT, "1", "0.01");
    assertFormatter(MONEY_DASH_FORMAT, "10", "0.10");
    assertFormatter(MONEY_DASH_FORMAT, "1201", "12.01");
    assertFormatter(MONEY_DASH_FORMAT, "350", "3.50");
    assertFormatter(MONEY_DASH_FORMAT, "100", "1.00");
    assertFormatter(MONEY_DASH_FORMAT, "100000", "1,000.00");
    assertFormatter(MONEY_DASH_FORMAT, "100003", "1,000.03");
    assertFormatter(MONEY_DASH_FORMAT, "1241313", "12,413.13");
    runner.exec("f-money-format-%N.html");
  }

  @Test
  public void testMoneyString() {
    runner.exec("f-money-string-%N.html");
  }

  @Test
  public void testPercentageFormat() {
    runner.exec("f-percentage-format-%N.html");
  }

  @Test
  public void testNormalPrice() {
    runner.exec("f-normal-price-%N.html");
  }

  @Test
  public void testProductCheckout() {
    runner.exec("f-product-checkout-%N.html");
  }

  @Test
  public void testProductPrice() {
    runner.exec("f-product-price-%N.html");
  }

  @Test
  public void testProductIndefiniteSubscriptionPrice() {
    runner.run("f-product-price-subscription-weekly.html",
        "f-product-price-subscription-bi-weekly.html",
        "f-product-price-subscription-monthly.html",
        "f-product-price-subscription-bi-monthly.html");

    runner.run("f-product-price-subscription-from-weekly.html",
        "f-product-price-subscription-from-bi-weekly.html",
        "f-product-price-subscription-from-monthly.html",
        "f-product-price-subscription-from-bi-monthly.html",
        "f-product-price-subscription-from-weekly-on-sale.html");

    runner.run("f-product-price-subscription-on-sale-bi-monthly.html",
        "f-product-price-subscription-on-sale-bi-weekly.html",
        "f-product-price-subscription-on-sale-monthly.html",
        "f-product-price-subscription-on-sale-weekly.html",
        "f-product-price-subscription-weekly-localized.html",
        "f-product-price-subscription-weekly-localized-multiple.html"
        );
  }

  @Test
  public void testProductFiniteSubscriptionPrice() {
    runner.run(
        "f-product-price-finite-subscription-weekly.html",
        "f-product-price-finite-subscription-bi-weekly.html",
        "f-product-price-finite-subscription-monthly.html",
        "f-product-price-finite-subscription-bi-monthly.html",
        "f-product-price-finite-subscription-bi-weekly-for-a-year.html",
        "f-product-price-finite-subscription-monthly-for-a-year.html");

    runner.run("f-product-price-finite-subscription-from-weekly.html",
        "f-product-price-finite-subscription-from-bi-weekly.html",
        "f-product-price-finite-subscription-from-monthly.html",
        "f-product-price-finite-subscription-from-bi-monthly.html",
        "f-product-price-finite-subscription-from-weekly-on-sale.html");

    runner.run("f-product-price-finite-subscription-on-sale-bi-monthly.html",
        "f-product-price-finite-subscription-on-sale-bi-weekly.html",
        "f-product-price-finite-subscription-on-sale-monthly.html",
        "f-product-price-finite-subscription-on-sale-weekly.html",
        "f-product-price-finite-subscription-weekly-localized.html",
        "f-product-price-finite-subscription-weekly-localized-multiple.html"
    );
  }

  @Test
  public void testProductSubscriptionPriceMissingPlan() {
    runner.run("f-product-price-subscription-weekly-plan-unavailable.html");
  }

  @Test
  public void testProductQuickView() {
    runner.exec("f-product-quick-view-%N.html");
  }

  @Test
  public void testProductStatus() {
    runner.exec("f-product-status-%N.html");
  }

  @Test
  public void testQuantityInput() {
    runner.exec("f-quantity-input-%N.html");
  }

  @Test
  public void testSalePrice() {
    runner.exec("f-sale-price-%N.html");
  }

  @Test
  public void testSummaryFormField() {
    runner.run(
        "f-summary-form-field-address-1.html",
        "f-summary-form-field-address-2.html"
        );
    runner.run(
        "f-summary-form-field-checkbox-1.html",
        "f-summary-form-field-checkbox-2.html",
        "f-summary-form-field-checkbox-3.html"
        );
    runner.run(
        "f-summary-form-field-date-1.html"
        );
    runner.run(
        "f-summary-form-field-likert-1.html"
        );
    runner.run(
        "f-summary-form-field-name-1.html"
        );
    runner.run(
        "f-summary-form-field-phone-1.html",
        "f-summary-form-field-phone-2.html"
        );
    runner.run(
        "f-summary-form-field-time-1.html",
        "f-summary-form-field-time-2.html",
        "f-summary-form-field-time-3.html"
        );
    runner.run(
        "f-summary-form-field-default-1.html"
        );
  }

  @Test
  public void testVariantDescriptor() {
    runner.exec("f-variant-descriptor-%N.html");
  }

  @Test
  public void testVariantsSelect() {
    runner.exec("f-variants-select-%N.html");
    runner.exec("f-variants-select-subscription.html");
  }

  @Test
  public void testProductScarcity() {
    runner.run(
        "f-scarcity-context-missing.html",
        "f-scarcity-not-enabled.html",
        "f-scarcity-default-shown.html",
        "f-scarcity-default-hidden.html",
        "f-scarcity-default-shown-escape-html.html",
        "f-scarcity-default-shown-and-variants.html"
    );
  }

  @Test
  public void testProductRestockNotification() {
    runner.exec("f-product-restock-notification-%N.html");
  }

  @Test
  public void testSubscriptionPrice() {
    runner.exec("f-subscription-price-%N.html");
  }
}
