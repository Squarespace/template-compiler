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
    runner.run(
        "f-add-to-cart-btn-1.html",
        "f-add-to-cart-btn-2.html",
        "f-add-to-cart-btn-3.html",
        "f-add-to-cart-btn-4.html",
        "f-add-to-cart-btn-subscribable-attribute-false.html",
        "f-add-to-cart-btn-subscribable-attribute-true.html"
        );
  }

  @Test
  public void testBookkeeperMoneyFormat() {
    runner.run(
        "f-bookkeeper-money-format-1.html"
        );
  }

  @Test
  public void testCartQuantity() {
    runner.run(
        "f-cart-quantity-1.html",
        "f-cart-quantity-2.html"
        );
  }

  @Test
  public void testCartSubtotal() {
    runner.run(
        "f-cart-subtotal-1.html"
        );
  }

  @Test
  public void testCartUrl() {
    runner.run(
        "f-cart-url-1.html"
        );
  }

  @Test
  public void testFromPrice() {
    runner.run(
        "f-from-price-1.html",
        "f-from-price-2.html",
        "f-from-price-3.html",
        "f-from-price-4.html",
        "f-from-price-5.html",
        "f-from-price-6.html",
        "f-from-price-7.html"
        );
  }

  @Test
  public void testMoneyFormatCamel() {
    runner.run("f-money-format-camel-1.html");
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
    runner.run(
        "f-money-format-1.html",
        "f-money-format-2.html"
        );
  }

  @Test
  public void testMoneyString() {
    runner.run(
        "f-money-string-1.html"
        );
  }

  @Test
  public void testPercentageFormat() {
    runner.run(
        "f-percentage-format-1.html",
        "f-percentage-format-2.html"
        );
  }

  @Test
  public void testNormalPrice() {
    runner.run(
        "f-normal-price-1.html",
        "f-normal-price-2.html",
        "f-normal-price-3.html",
        "f-normal-price-4.html",
        "f-normal-price-5.html",
        "f-normal-price-6.html",
        "f-normal-price-7.html"
        );
  }

  @Test
  public void testProductPrice() {
    runner.run(
      "f-product-price-1.html",
      "f-product-price-2.html",
      "f-product-price-3.html",
      "f-product-price-4.html",
      "f-product-price-5.html",
      "f-product-price-6.html",
      "f-product-price-7.html",
      "f-product-price-8.html",
      "f-product-price-9.html",
      "f-product-price-10.html"
      );
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
    runner.run(
        "f-product-quick-view-1.html",
        "f-product-quick-view-2.html",
        "f-product-quick-view-3.html",
        "f-product-quick-view-4.html"
        );
  }

  @Test
  public void testProductStatus() {
    runner.run(
        "f-product-status-1.html",
        "f-product-status-2.html",
        "f-product-status-3.html",
        "f-product-status-4.html",
        "f-product-status-5.html",
        "f-product-status-6.html",
        "f-product-status-7.html",
        "f-product-status-8.html"
        );
  }

  @Test
  public void testQuantityInput() {
    runner.run(
        "f-quantity-input-1.html",
        "f-quantity-input-2.html",
        "f-quantity-input-3.html",
        "f-quantity-input-4.html",
        "f-quantity-input-5.html"
        );
  }

  @Test
  public void testSalePrice() {
    runner.run(
        "f-sale-price-1.html",
        "f-sale-price-2.html",
        "f-sale-price-3.html",
        "f-sale-price-4.html",
        "f-sale-price-5.html",
        "f-sale-price-6.html",
        "f-sale-price-7.html"
        );
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
    runner.run(
        "f-variant-descriptor-1.html",
        "f-variant-descriptor-2.html"
        );
  }

  @Test
  public void testVariantsSelect() {
    runner.run(
        "f-variants-select-1.html",
        "f-variants-select-2.html",
        "f-variants-select-3.html",
        "f-variants-select-subscription.html"
        );
  }

}
