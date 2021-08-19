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
import static com.squarespace.template.GeneralUtils.isTruthy;
import static com.squarespace.template.GeneralUtils.loadResource;
import static com.squarespace.template.plugins.PluginUtils.slugify;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.BaseFormatter;
import com.squarespace.template.CodeException;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Compiler;
import com.squarespace.template.Constants;
import com.squarespace.template.Context;
import com.squarespace.template.Formatter;
import com.squarespace.template.FormatterRegistry;
import com.squarespace.template.Instruction;
import com.squarespace.template.StringView;
import com.squarespace.template.SymbolTable;
import com.squarespace.template.Variable;
import com.squarespace.template.Variables;
import com.squarespace.template.plugins.PluginDateUtils;
import com.squarespace.template.plugins.PluginUtils;
import com.squarespace.template.plugins.platform.enums.BlockType;
import com.squarespace.template.plugins.platform.enums.RecordType;


/**
 * Extracted from Commons library at commit ab4ba7a6f2b872a31cb6449ae9e96f5f5b30f471
 */
public class ContentFormatters implements FormatterRegistry {

  public static final int MAX_ALT_TEXT_LENGTH = 1000;

  @Override
  public void registerFormatters(SymbolTable<StringView, Formatter> table) {
    table.add(new AbsUrlFormatter(Constants.BASE_URL_KEY));
    table.add(new AudioPlayerFormatter());
    table.add(new CapitalizeFormatter());
    table.add(new ChildImageMetaFormatter());
    table.add(new ColorWeightFormatter());
    table.add(new CoverImageMetaFormatter());
    table.add(new HeightFormatter());
    table.add(new HumanizeDurationFormatter());
    table.add(new ImageFormatter());
    table.add(new ImageColorFormatter());
    table.add(new ImageMetaFormatter());
    table.add(new ImageMetaSrcSetFormatter());
    table.add(new ItemClassesFormatter());
    table.add(new ResizedHeightForWidthFormatter());
    table.add(new ResizedWidthForHeightFormatter());
    table.add(new SqspThumbForHeightFormatter());
    table.add(new SqspThumbForWidthFormatter());
    table.add(new TimesinceFormatter());
    table.add(new VideoFormatter());
    table.add(new WidthFormatter());
  }

  public static class AbsUrlFormatter extends BaseFormatter {

    private final String[] baseUrlKey;

    public AbsUrlFormatter(String[] baseUrlKey) {
      super("AbsUrl", false);
      this.baseUrlKey = baseUrlKey;
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      String baseUrl = ctx.resolve(baseUrlKey).asText();
      String value = var.node().asText();
      var.set(baseUrl + "/" + value);
    }

  }

  public static class AudioPlayerFormatter extends BaseFormatter {

    private Instruction template;

    public AudioPlayerFormatter() {
      super("audio-player", false);
    }

    @Override
    public void initialize(Compiler compiler) throws CodeException {
      String source = loadResource(ContentFormatters.class, "audio-player.html");
      template = compiler.compile(source.trim()).code();
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      var.set(executeTemplate(ctx, template, var.node(), true));
    }
  }

  public static class CapitalizeFormatter extends BaseFormatter {

    public CapitalizeFormatter() {
      super("capitalize", false);
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      String text = var.node().asText();
      var.set(text.toUpperCase());
    }
  }


  private static void outputImageMeta(JsonNode image, StringBuilder buf) {
      outputImageMeta(image, buf, null);
  }

  private static void outputImageMeta(JsonNode image, StringBuilder buf, String preferredAltText) {
    if (image.isMissingNode()) {
      return;
    }

    JsonNode componentKey = image.path("componentKey");
    String focalPoint = getFocalPoint(image);
    String origSize = image.path("originalSize").asText();
    String assetUrl = image.path("assetUrl").asText();

    String altText = preferredAltText != null ? preferredAltText : computeAltTextFromContentItemFields(image);

    if (isLicensedAssetPreview(image)) {
      buf.append("data-licensed-asset-preview=\"true\" ");
    }

    if (!componentKey.isMissingNode()) {
      buf.append("data-component-key=\"").append(componentKey.asText()).append("\" ");
    }

    buf.append("data-src=\"").append(assetUrl).append("\" ");
    buf.append("data-image=\"").append(assetUrl).append("\" ");
    buf.append("data-image-dimensions=\"").append(origSize).append("\" ");
    buf.append("data-image-focal-point=\"").append(focalPoint).append("\" ");
    buf.append("alt=\"");
    PluginUtils.escapeHtmlAttribute(altText, buf);
    buf.append("\" ");
  }

  public static class ChildImageMetaFormatter extends BaseFormatter {

    public ChildImageMetaFormatter() {
      super("child-image-meta", false);
    }

    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
      args.atMost(1);
      int index = 0;
      args.setOpaque(index);
      if (args.count() == 1) {
        try {
          index = Integer.parseInt(args.first());
          args.setOpaque(index);
        } catch (NumberFormatException e) {
          throw new ArgumentsException("expected an integer index, found '" + args.first() + "'");
        }
      }
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      int index = (Integer)args.getOpaque();
      JsonNode child = var.node().path("items").path(index);
      StringBuilder buf = new StringBuilder();
      outputImageMeta(child, buf);
      var.set(buf);
    }
  }


  private static final Pattern VALID_COLOR = Pattern.compile("[abcdef0-9]{3,6}", Pattern.CASE_INSENSITIVE);

  private static final int HALFBRIGHT = 0xFFFFFF / 2;

  /**
   * COLOR_WEIGHT
   */
  public static class ColorWeightFormatter extends BaseFormatter {

    public ColorWeightFormatter() {
      super("color-weight", false);
    }

    /**
     * Properly handle hex colors of length 3. Width of each channel needs to be expanded.
     */
    private int color3(char c1, char c2, char c3) {
      int n1 = PluginUtils.hexDigitToInt(c1);
      int n2 = PluginUtils.hexDigitToInt(c2);
      int n3 = PluginUtils.hexDigitToInt(c3);
      return (n1 << 20) | (n1 << 16) | (n2 << 12) | (n2 << 8) | (n3 << 4) | n3;
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      String hex = var.node().asText();
      hex = hex.replace("#", "");
      if (!VALID_COLOR.matcher(hex).matches()) {
        var.setMissing();
        return;
      }
      int value = 0;
      if (hex.length() == 3) {
        value = color3(hex.charAt(0), hex.charAt(1), hex.charAt(2));
      } else if (hex.length() == 6) {
        value = Integer.parseInt(hex, 16);
      }
      String weight = (value > HALFBRIGHT) ? "light" : "dark";
      var.set(weight);
    }
  };

  private static String[] splitDimensions(JsonNode node) {
    String val = node.asText();
    String[] parts = StringUtils.split(val, 'x');
    if (parts.length != 2) {
      return null;
    }
    return parts;
  }

  private static String getFocalPoint(JsonNode media) {
    String focalPoint = "0.5,0.5";
    JsonNode node = media.path("mediaFocalPoint");
    if (!node.isMissingNode()) {
      focalPoint = node.path("x").asDouble() + "," + node.path("y").asDouble();
    }
    return focalPoint;
  }

  private static String computeAltTextFromContentItemFields(JsonNode contentItemNode) {
    JsonNode title = contentItemNode.path("title");
    if (isTruthy(title)) {
      return title.asText();
    }

    JsonNode body = contentItemNode.path("body");
    if (isTruthy(body)) {
      String text = PluginUtils.removeTags(body.asText());
      if (text.length() > 0) {
        return text.substring(0, Math.min(text.length(), MAX_ALT_TEXT_LENGTH));
      }
    }

    JsonNode filename = contentItemNode.path("filename");
    if (isTruthy(filename)) {
      return filename.asText();
    }

    return "";
  }

  public static class HeightFormatter extends BaseFormatter {

    public HeightFormatter() {
      super("height", false);
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      String[] parts = splitDimensions(var.node());
      if (parts == null || parts.length != 2) {
        var.set("Invalid source parameter. Pass in 'originalSize'.");
      } else {
        int height = Integer.parseInt(parts[1]);
        var.set(height);
      }
    }
  };


  /**
   * HUMANIZE_DURATION
   */
  public static class HumanizeDurationFormatter extends BaseFormatter {

    public HumanizeDurationFormatter() {
      super("humanizeDuration", false);
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      long duration = var.node().asLong();
      var.set(DurationFormatUtils.formatDuration(duration, "m:ss"));
    }

  };

  private static boolean isLicensedAssetPreview(JsonNode image) {
    if (image.path("licensedAssetPreview").isObject()) {
      return true;
    }

    return false;
  }

  public static class ImageFormatter extends BaseFormatter {

    public ImageFormatter() {
      super("image", false);
    }

    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
      args.atMost(1);
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      JsonNode node = var.node();

      String cls = (args.count() == 1) ? args.first() : "thumb-image";

      String id = node.path("id").asText();
      String altText = getAltText(ctx);
      String assetUrl = node.path("assetUrl").asText();

      StringBuilder buf = new StringBuilder();

      buf.append("<noscript>");
      buf.append("<img ");
      buf.append("src=\"").append(assetUrl).append("\" ");
      buf.append("alt=\"");
      PluginUtils.escapeHtmlAttribute(altText, buf);
      buf.append("\" ");
      buf.append("/>");
      buf.append("</noscript>");

      buf.append("<img class=\"").append(cls).append("\" ");
      outputImageMeta(node, buf, altText);

      buf.append("data-load=\"false\"").append(" ");
      buf.append("data-image-id=\"").append(id).append("\" ");
      buf.append("data-type=\"image\" ");
      buf.append("/>");
      var.set(buf);
    }

    private String getAltText(Context ctx) {
      // Content Items for image blocks were populated with an altText value with a migration. See CMS-33805.
      // For those, the Content Item value should always be used even if it is empty.
      JsonNode blockType = ctx.resolve("blockType");

      if (!blockType.isMissingNode() && blockType.asInt() == BlockType.IMAGE.code()) {
        JsonNode altText = ctx.node().path("altText");
        return StringUtils.trim(altText.asText());
      }

      JsonNode image = ctx.node();
      return computeAltTextFromContentItemFields(image);
    }
  }

  public static class ImageColorFormatter extends BaseFormatter {

    public ImageColorFormatter() {
      super("image-color", false);
    }

    private final List<String> positions = Arrays.asList(
        "topLeft", "topRight", "bottomLeft", "bottomRight", "center"
        );

    private final Set<String> positionSet = new HashSet<>(positions);

    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
      args.atMost(2);
      if (args.count() >= 1) {
        String pos = args.first();
        if (!positionSet.contains(pos)) {
          throw new ArgumentsException("illegal value '" + pos + "' found");
        }
      }
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      JsonNode colorData = var.node().path("colorData");

      if (colorData.isMissingNode()) {
        var.setMissing();
        return;
      }

      StringBuilder buf = new StringBuilder();
      if (args.count() > 0) {
        String key = args.first();
        String color = colorData.path(key + "Average").asText();
        if (color.length() > 0) {
          if (args.count() == 2) {
            buf.append(args.get(1)).append(": ");
          }
          buf.append('#').append(color);
        } else {
          buf.append("\"").append(key).append("\" not found.");
        }

      } else {
        for (String key : positions) {
          buf.append("data-color-").append(key).append("=\"#");
          buf.append(colorData.path(key + "Average").asText());
          buf.append("\" ");
        }
      }

      var.set(buf);
    }
  }

  public static class ImageMetaFormatter extends BaseFormatter {

    public ImageMetaFormatter() {
      super("image-meta", false);
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      StringBuilder buf = new StringBuilder();
      outputImageMeta(var.node(), buf);
      var.set(buf);
    }
  }

  public static class ImageMetaSrcSetFormatter extends BaseFormatter {
    private String[] SQUARESPACE_SIZES = {"100w", "300w", "500w", "750w", "1000w", "1500w", "2500w"};

    public ImageMetaSrcSetFormatter() {
      super("image-srcset", false);
    }

    protected static int filterVariants(String[] variants) {
      int c = 0;
      for (int i = 0; i < variants.length; i++) {
        if (variants[i].endsWith("w")) {
          variants[c] = variants[i];
          c++;
        }
      }
      return c;
    }

    protected void outputImageSrcSet(JsonNode image, StringBuilder buf) {
      if (image.isMissingNode()) {
        return;
      }

      String assetUrl = image.path("assetUrl").asText();
      String[] variants = image.path("systemDataVariants").asText().split(",");
      int limit = filterVariants(variants);
      if (limit == 0) {
        return;
      }

      buf.append(" srcset=\"");
      for (int i = 0; i < limit; i++) {
        if (i > 0) {
          buf.append(',');
        }
        buf.append(assetUrl).append("?format=").append(variants[i]).append(" ").append(variants[i]);
      }
      addOriginalImageFormat(buf, variants, limit, assetUrl);

      buf.append("\"");
    }

    /**
     * Not all images will have complete list of Squarespace sizes due to original uploaded image is already too small. So no compression
     * or smaller image size is produced.
     * For these cases, we want to use the original image for missing sizes by setting the next size to use original image.
     */
    public void addOriginalImageFormat(StringBuilder buf, String[] variants, int limit, String assetUrl) {
      String lastVariant = variants[limit - 1];
      if (lastVariant.equals(SQUARESPACE_SIZES[SQUARESPACE_SIZES.length - 1])) {
        return;
      }
      for (int i = 0; i < SQUARESPACE_SIZES.length - 1; i++) {
        if (SQUARESPACE_SIZES[i].equals(lastVariant)) {
          buf.append(',');
          buf.append(assetUrl).append("?format=original").append(" ").append(SQUARESPACE_SIZES[i + 1]);
          return;
        }
      }
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      StringBuilder buf = new StringBuilder();
      Variable var = variables.first();
      outputImageSrcSet(var.node(), buf);
      var.set(buf);
    }

  }

  public static class CoverImageMetaFormatter extends BaseFormatter {

    public CoverImageMetaFormatter() {
      super("cover-image-meta", false);
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      StringBuilder buf = new StringBuilder();
      outputImageMeta(var.node().path("coverImage"), buf);
      var.set(buf);
    }
  };


  /**
   * ITEM_CLASSES
   */
  public static class ItemClassesFormatter extends BaseFormatter {

    public ItemClassesFormatter() {
      super("item-classes", false);
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      JsonNode value = var.node();

      StringBuilder buf = new StringBuilder();
      buf.append("hentry");

      JsonNode node = ctx.resolve("promotedBlockType");
      if (isTruthy(node)) {
        buf.append(" promoted promoted-block-" + slugify(node.asText()));
      }

      node = ctx.resolve("categories");
      if (isTruthy(node)) {
        int size = node.size();
        for (int i = 0; i < size; i++) {
          buf.append(" category-" + slugify(node.path(i).asText()));
        }
      }

      node = ctx.resolve("tags");
      if (isTruthy(node)) {
        int size = node.size();
        for (int i = 0; i < size; i++) {
          buf.append(" tag-" + slugify(node.path(i).asText()));
        }
      }

      node = ctx.resolve("author");
      JsonNode displayName = node.path("displayName");
      if (isTruthy(node) && isTruthy(displayName)) {
        buf.append(" author-" + slugify(displayName.asText()));
      }
      node = ctx.resolve("recordTypeLabel");
      buf.append(" post-type-").append(node.asText());

      node = ctx.resolve("@index");
      if (!node.isMissingNode()) {
        buf.append(" article-index-" + node.asInt());
      }

      node = ctx.resolve("starred");
      if (isTruthy(node)) {
        buf.append(" featured");
      }

      node = value.path("recordType");
      if (RecordType.STORE_ITEM.code() == node.asInt()) {
        if (CommerceUtils.isOnSale(value)) {
          buf.append(" on-sale");
        }
        if (CommerceUtils.isSoldOut(value)) {
          buf.append(" sold-out");
        }
      }

      var.set(buf);
    }

  }

  private static abstract class ResizeBaseFormatter extends BaseFormatter {

    ResizeBaseFormatter(String identifier) {
      super(identifier, true);
    }

    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
      args.atLeast(1);
      Integer requestedWidth = Integer.parseInt(args.first());
      args.setOpaque(requestedWidth);
    }

    protected JsonNode resize(Context ctx, JsonNode node, boolean resizeWidth, int requested) {
      String[] parts = splitDimensions(node);
      if (parts == null || parts.length != 2) {
        return new TextNode("Invalid source parameter. Pass in 'originalSize'.");
      }
      int width = Integer.parseInt(parts[0]);
      int height = Integer.parseInt(parts[1]);
      int value = 0;
      if (resizeWidth) {
        value = (int)(width * (requested / (float)height));
      } else {
        value = (int)(height * (requested / (float)width));
      }
      return new IntNode(value);
    }

    protected String getSquarespaceSizeForWidth(int width) {
      if (width > 1000) {
        return "1500w";
      } else if (width > 750) {
        return "1000w";
      } else if (width > 500) {
        return "750w";
      } else if (width > 300) {
        return "500w";
      } else if (width > 100) {
        return "300w";
      } else {
        return "100w";
      }
    }
  }


  public static class ResizedHeightForWidthFormatter extends ResizeBaseFormatter {

    public ResizedHeightForWidthFormatter() {
      super("resizedHeightForWidth");
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      var.set(resize(ctx, var.node(), false, (Integer)args.getOpaque()));
    }
  }

  public static class ResizedWidthForHeightFormatter extends ResizeBaseFormatter {

    public ResizedWidthForHeightFormatter() {
      super("resizedWidthForHeight");
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      var.set(resize(ctx, var.node(), true, (Integer)args.getOpaque()));
    }
  }

  public static class SqspThumbForWidthFormatter extends ResizeBaseFormatter {

    public SqspThumbForWidthFormatter() {
      super("squarespaceThumbnailForWidth");
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      var.set(getSquarespaceSizeForWidth((Integer)args.getOpaque()));
    }
  }

  public static class SqspThumbForHeightFormatter extends ResizeBaseFormatter {

    public SqspThumbForHeightFormatter() {
      super("squarespaceThumbnailForHeight");
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      JsonNode resized = resize(ctx, var.node(), true, (Integer)args.getOpaque());
      if (resized.isInt()) {
        var.set(getSquarespaceSizeForWidth(resized.asInt()));
      } else {
        var.set(resized);
      }
    }
  }


  /**
   * TIMESINCE - Outputs a human-readable representation of (now - timestamp).
   */
  public static class TimesinceFormatter extends BaseFormatter {

    public TimesinceFormatter() {
      super("timesince", false);
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      JsonNode node = var.node();
      StringBuilder buf = new StringBuilder();
      if (!node.isNumber()) {
        buf.append("Invalid date.");
      } else {
        Long _now = ctx.now();
        long now = _now == null ? System.currentTimeMillis() : _now.longValue();
        long value = node.asLong();
        buf.append("<span class=\"timesince\" data-date=\"" + value + "\">");
        PluginDateUtils.humanizeDate(value, now, false, buf);
        buf.append("</span>");
      }
      var.set(buf);
    }

  }

  public static class WidthFormatter extends BaseFormatter {

    public WidthFormatter() {
      super("width", false);
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      String[] parts = splitDimensions(var.node());
      if (parts == null || parts.length != 2) {
        var.set("Invalid source parameter. Pass in 'originalSize'.");
      } else {
        int width = Integer.parseInt(parts[0]);
        var.set(width);
      }
    }
  }

  public static class VideoFormatter extends BaseFormatter {

    public VideoFormatter() {
      super("video", false);
    }

    private final Set<String> validArgs = new HashSet<>(Arrays.asList("load-false", "color-data"));

    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
      for (String arg : args.getArgs()) {
        if (!validArgs.contains(arg)) {
          throw new ArgumentsException("'" + arg + "' is not an expected value");
        }
      }
    }

    @Override
    public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
      Variable var = variables.first();
      JsonNode node = var.node();

      JsonNode oEmbed = node.path("oembed");
      JsonNode colorData = node.path("colorData");
      String assetUrl = node.path("assetUrl").asText();
      String focalPoint = getFocalPoint(node);
      String originalSize = node.path("originalSize").asText();

      boolean loadFalse = false;
      boolean useColorData = false;

      for (String arg : args.getArgs()) {
        if (arg.equals("load-false")) {
          loadFalse = true;
        } else if (arg.equals("color-data")) {
          useColorData = true;
        }
      }

      StringBuilder buf = new StringBuilder();
      buf.append("<div class=\"sqs-video-wrapper\" ");
      if (loadFalse) {
        buf.append(" data-load=\"false\" ");
      }
      buf.append("data-html=\"");
      PluginUtils.escapeHtmlAttribute(oEmbed.path("html").asText(), buf);
      buf.append("\" data-provider-name=\"").append(oEmbed.path("providerName").asText()).append("\">");

      if (isTruthy(node.path("overlay"))) {
        buf.append("<div class=\"sqs-video-overlay");

        if (isTruthy(node.path("mainImageId")) || isTruthy(node.path("systemDataId"))) {
          buf.append("\" style=\"opacity: 0;\">");
          buf.append("<img data-load=\"false\" data-src=\"").append(assetUrl).append("\" ");
          buf.append("data-image-dimensions=\"").append(originalSize).append("\" ");
          buf.append("data-image-focal-point=\"").append(focalPoint).append("\" ");

          if (useColorData && isTruthy(colorData)) {
            buf.append("data-color-topleft=\"#")
               .append(colorData.path("topLeftAverage").asText())
               .append("\" ");
            buf.append("data-color-topright=\"#")
               .append(colorData.path("topRightAverage").asText())
               .append("\" ");
            buf.append("data-color-bottomleft=\"#")
               .append(colorData.path("bottomLeftAverage").asText())
               .append("\" ");
            buf.append("data-color-bottomright=\"#")
               .append(colorData.path("bottomRightAverage").asText())
               .append("\" ");
            buf.append("data-color-center=\"#")
               .append(colorData.path("centerAverage").asText())
               .append("\" ");
          }

          buf.append("/>"); // close <img>
        } else {
          buf.append(" no-thumb\" style=\"opacity: 0;\">"); // close <img>
        }

        buf.append("<div class=\"sqs-video-opaque\"> </div><div class=\"sqs-video-icon\"></div>");
        buf.append("</div>");
      }

      buf.append("</div>");
      var.set(buf);
    }

  }

}
