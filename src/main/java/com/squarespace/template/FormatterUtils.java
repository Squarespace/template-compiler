package com.squarespace.template;

import com.fasterxml.jackson.databind.JsonNode;


public class FormatterUtils {

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
  
  public static void truncate(String value, int maxLen, String ellipses, StringBuilder buf) {
    if (value.length() <= maxLen) {
      buf.append(value);
      return;
    }
    
    int end = maxLen;
    for (int i = maxLen-1; i >= 0; i--) {
      if (Character.isWhitespace(value.charAt(i))) {
        end = i + 1;
        break;
      }
    }
    buf.append(value, 0, end);
    buf.append(ellipses);
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
  
  public static void makeSocialButton(JsonNode website, JsonNode item, String style, StringBuilder buf) {
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
    
    buf.append("<script>Y.use('squarespace-social-buttons');");
    buf.append("</script><div class=\"squarespace-social-buttons ");
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
    buf.append("\"></div>");
  }

}
