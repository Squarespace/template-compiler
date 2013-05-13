package com.squarespace.template;

import static com.squarespace.template.GeneralUtils.isTruthy;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;


public class MediaFormatters extends BaseRegistry<Formatter> {

  public static final Formatter AUDIO_PLAYER = new BaseFormatter("audio-player", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode node = ctx.node();
      String assetUrl = node.path("audioAssetUrl").asText();
      String id = node.path("id").asText();
      ctx.append("<script>Y.use('squarespace-audio-player-frontend');</script>");
      ctx.append("<div class=\"squarespace-audio-player\" data-audio-asset-url=\"");
      ctx.append(assetUrl);
      ctx.append("\" data-item-id=\"");
      ctx.append(id);
      ctx.append("\" id=\"audio-player-");
      ctx.append(id);
      ctx.append("\"></div>");
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
  
  public static final Formatter HEIGHT = new BaseFormatter("height", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      String[] parts = splitDimensions(ctx.node());
      if (parts == null || parts.length != 2) {
        ctx.append("Invalid source paramter. Pass in 'originalSize'.");
      }
      int height = Integer.parseInt(parts[1]);
      ctx.buffer().append(height);
    }
  };
  
  /*
   
       'height': function(x) {
      var wh = x.split("x");
      if (wh.length != 2) { return "Invalid source parameter.  Pass in 'originalSize'."; }

      var width = parseInt(wh[0], 10);
      var height = parseInt(wh[1], 10);

      return height;
    },
   
   */
  
  
  public static final Formatter IMAGE_META = new BaseFormatter("image-meta", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode node = ctx.node();
      if (node.isMissingNode()) {
        return;
      }
      
      String focalPoint = "0.5,0.5";
      JsonNode fpNode = node.path("mediaFocalPoint");
      if (!fpNode.isMissingNode()) {
        focalPoint = fpNode.path("x").asDouble() + "," + fpNode.path("y").asDouble();
      }
      String origSize = node.path("originalSize").asText();
      String assetUrl = node.path("assetUrl").asText();
      
      JsonNode title = node.path("title");
      JsonNode body = node.path("body");
      JsonNode filename = node.path("filename");
      String altText = "";
      
      if (isTruthy(title)) {
        String text = title.asText();
        if (text.length() > 0) {
          altText = text;
        }
      }
      if (!(altText.length() > 0) && isTruthy(body)) {
        String text = FormatterUtils.removeTags(body.asText());
        if (text.length() > 0) {
          altText = text;
        }
      }
      if (!(altText.length() > 0) && isTruthy(filename)) {
        String text = filename.asText();
        if (text.length() > 0) {
          altText = text;
        }
      }
      
      ctx.append("data-image=\"");
      ctx.append(assetUrl);
      ctx.append("\" data-src=\"");
      ctx.append(assetUrl);
      ctx.append("\" data-image-dimensions=\"");
      ctx.append(origSize);
      ctx.append("\" data-image-focal-point=\"");
      ctx.append(focalPoint);
      ctx.append("\" alt=\"");
      FormatterUtils.escapeHtmlTag(altText, ctx.buffer());
      ctx.append("\" ");
    }
  };
  
  
  public static final Formatter WIDTH = new BaseFormatter("width", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      String[] parts = splitDimensions(ctx.node());
      if (parts == null || parts.length != 2) {
        ctx.append("Invalid source paramter. Pass in 'originalSize'.");
      }
      int width = Integer.parseInt(parts[0]);
      ctx.buffer().append(width);
    }
  };

  
  public static final Formatter VIDEO = new BaseFormatter("video", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      ctx.buffer().append("[video]");
    }
  };

}
