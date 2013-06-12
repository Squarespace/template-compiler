package com.squarespace.template.plugins;

import static com.squarespace.template.Patterns.WHITESPACE;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.template.GeneralUtils;


public class PluginUtils {
  
  private static final Pattern REMOVE_TAGS = Pattern.compile("<(?:.|\n)*?>");

  private static final Pattern SLUG_KILLCHARS = Pattern.compile("[^a-zA-Z0-9\\s-]+");

  private PluginUtils() {
  }
  
  public static void escapeHtml(String str, StringBuilder buf) {
    for (int i = 0; i < str.length(); i++) {
      char ch = str.charAt(i);
      switch (ch) {
        case '&':
          buf.append("&amp;");
          break;
        case '<':
          buf.append("&lt;");
          break;
        case '>':
          buf.append("&gt;");
          break;
        default:
          buf.append(ch);
      }
    }
  }
  
  public static void escapeHtmlTag(String str, StringBuilder buf) {
    for (int i = 0; i < str.length(); i++) {
      char ch = str.charAt(i);
      switch (ch) {
        case '&':
          buf.append("&amp;");
          break;
        case '<':
          buf.append("&lt;");
          break;
        case '>':
          buf.append("&gt;");
          break;
        case '"':
          buf.append("&quot;");
          break;
        default:
          buf.append(ch);
      }
    }
  }
  
  public static String formatMoney(double cents, Locale locale) {
    cents /= 100;
    boolean isWhole = (cents == Math.round(cents));
    DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
    String pattern = isWhole ? "#,##0.00" : "#,##0.##";
    DecimalFormat format = new DecimalFormat(pattern, symbols);
    return format.format(cents);
  }

  public static String removeTags(String str) {
    return REMOVE_TAGS.matcher(str).replaceAll("");
  }
  
  public static String slugify(String value) {
    value = SLUG_KILLCHARS.matcher(value).replaceAll("");
    value = WHITESPACE.matcher(value).replaceAll("-");
    return value.toLowerCase();
  }
  
  public static String truncate(String value, int maxLen, String ellipses) {
    if (value.length() <= maxLen) {
      return value;
    }
    
    int end = maxLen;
    for (int i = maxLen-1; i >= 0; i--) {
      if (Character.isWhitespace(value.charAt(i))) {
        end = i + 1;
        break;
      }
    }
    return value.substring(0, end) + ellipses;
  }
  
  /**
   * Left-pads values where 0 <= n.
   */
  public static void leftPad(long value, char padChar, int maxDigits, StringBuilder buf) {
    int digits = (value == 0) ? 1 : (int) Math.log10(value) + 1;
    for (int i = 0; i < maxDigits - digits; i++) {
      buf.append(padChar);
    }
    buf.append(value);
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
    buf.append("<script>Y.use('squarespace-social-buttons');");
    buf.append("</script>");
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
    escapeHtmlTag(item.path("title").asText(), buf);
    buf.append("\">");
    if (inline) {
      buf.append("</span>");
    } else {
      buf.append("</div>");
    }
  }

}
