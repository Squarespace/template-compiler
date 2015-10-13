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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.template.GeneralUtils;
import com.squarespace.template.plugins.PluginUtils;


/**
 * Extracted from Commons library at commit ab4ba7a6f2b872a31cb6449ae9e96f5f5b30f471
 */
public class PlatformUtils {

  private PlatformUtils() {
  }

  /**
   * Like money-format (e.g. 0.00) but with a currency (dollar) sign and parentheses on negative values.
   * Fully internationalized. Uses NumberFormat under the hood.
   */
  public static String formatBookkeeperMoney(double cents, Locale locale) {
    NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
    return formatter.format(cents / 100);
  }

  public static String formatPercentage(double percentage, boolean trim, Locale locale) {
    DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
    String pattern = trim ? "#,##0.###" : "#,##0.00#";
    DecimalFormat format = new DecimalFormat(pattern, symbols);
    return format.format(percentage);
  }

  public static String formatPercentageTrimmed(double percentage, Locale locale) {
    DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
    DecimalFormat format = new DecimalFormat("#,##0.###", symbols);
    return format.format(percentage);
  }

  public static void makeSocialButton(JsonNode website, JsonNode item, boolean inline, StringBuilder buf) {
    JsonNode options = website.path("shareButtonOptions");
    if (website.isMissingNode() || options.isMissingNode() || options.size() == 0) {
      return;
    }

    JsonNode node = GeneralUtils.getFirstMatchingNode(item, "systemDataId", "mainImageId");
    String imageId = node.asText();
    node = item.path("assetUrl");
    String assetUrl = node.asText();
    if (node.isMissingNode()) {
      node = item.path("mainImage").path("assetUrl");
      assetUrl = node.asText();
    }
    String style = (inline) ? "inline-style" : "button-style";
    if (inline) {
      buf.append("<span ");
    } else {
      buf.append("<div ");
    }
    buf.append("class=\"squarespace-social-buttons ");
    buf.append(style);
    buf.append("\" data-system-data-id=\"");
    buf.append(imageId);
    buf.append("\" data-asset-url=\"");
    buf.append(assetUrl);
    buf.append("\" data-record-type=\"");
    buf.append(item.path("recordType").asText());
    buf.append("\" data-full-url=\"");
    buf.append(item.path("fullUrl").asText());
    buf.append("\" data-title=\"");
    PluginUtils.escapeHtmlAttribute(item.path("title").asText(), buf);
    buf.append("\">");
    if (inline) {
      buf.append("</span>");
    } else {
      buf.append("</div>");
    }
  }
}
