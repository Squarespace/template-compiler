package com.squarespace.template;

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
  
  public static final Formatter IMAGE_META = new BaseFormatter("image-meta", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode node = ctx.node();
      if (node.isMissingNode()) {
        return;
      }
      ctx.buffer().append("IMAGE_META");
      
      
      String focalPoint = "0.5,0.5";
      JsonNode temp = node.path("mediaFocalPoint");
      if (!temp.isMissingNode()) {
//        focalPoint = temp.path("x").asText() + 
      }
      
      // IN PROGRESS......
    }
  };
  
  
  /*
  'image-meta': function(x) {
  if (!x) {
    return;
  }
  var focalPoint = '0.5,0.5';
  if (x.mediaFocalPoint) {
    focalPoint = x.mediaFocalPoint.x + ',' + x.mediaFocalPoint.y;
  }

  // NOTE: WHEN WE GET NODE WORKED OUT, WE SHOULD GIVE THIS A SHOT.
  // IT SOLVES A LOT OF BROWSER ISSUES
  // -naz
  // var canvas = document.createElement('canvas');
  // canvas.width = x.originalSize.split('x')[0];
  // canvas.width = x.originalSize.split('x')[1];
  // var src = canvas.toDataURL();
  //
  // return "src="' + src + '" .....

  // ALT text = title || description || filename (any of these could be undefined)
  var alt;
  if (x.title && x.title.length > 0) {
    alt = x.title;
  }
  if (!alt && x.body && x.body.length > 0) {
    alt = x.body.replace(/<(?:.|\n)*?>/gm, ''); // strip html
  }
  if (!alt && x.filename && x.filename.length > 0) {
    alt = x.filename;
  }
  if (alt) {
    alt = Y.Squarespace.Escaping.escapeForHtmlTag(alt);
  }
  return 'data-image="' + x.assetUrl + '" data-src="' + x.assetUrl + 
  '" data-image-dimensions="' + (x.originalSize || '') + 
  '" data-image-focal-point="' + focalPoint + '" alt="' +  alt +'" ';
},
  
*/
  
  static final Formatter VIDEO = new BaseFormatter("video", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      ctx.buffer().append("VIDEO");
    }
  };

}
