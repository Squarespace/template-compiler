package com.squarespace.template;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;


public class SocialFormatters extends BaseRegistry<Formatter> {

  public static final Formatter COMMENTS = new BaseFormatter("comments", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode item = ctx.node();
      ctx.append("<div class=\"squarespace-comments\" id=\"comments-");
      ctx.append(item.path("id").asText());
      ctx.append("\"></div>");
    }
  };
  
  private static void addCommentCount(JsonNode item, StringBuilder buf) {
    int count = item.path("publicCommentCount").asInt();
    if (count == 0) {
      buf.append("No");
    } else {
      buf.append(count);
    }
    buf.append(" Comment");
    if (count != 1) {
      buf.append("s");
    }
  }
  
  public static final Formatter COMMENT_LINK = new BaseFormatter("comment-link", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      StringBuilder buf = ctx.buffer();
      JsonNode item = ctx.node();
      JsonNode settings = ctx.resolve(new String[] { "websiteSettings" });
      JsonNode disqusName = settings.path("disqusShortname");
      String itemId = item.path("id").asText();
      String fullUrl = item.path("fullUrl").asText();

      if (settings.isMissingNode() || disqusName.isMissingNode()) {
        buf.append("<a href=\"").append(fullUrl).append("#comments-");
        buf.append(itemId).append("\" class=\"sqs-comment-link\" data-id=\"");
        buf.append(itemId).append("\">");
        addCommentCount(item, buf);
        buf.append("</a>");
      } else {
        buf.append("<script>Y.use(\"squarespace-comment-links\");</script><a href=\"");
        buf.append(fullUrl).append("\" class=\"sqs-comment-link\" data-id=\");").append(itemId);
        buf.append("\"></a>");
      }
    }
  };

  public static final Formatter COMMENT_COUNT = new BaseFormatter("comment-count", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode item = ctx.node();
      addCommentCount(item, ctx.buffer());
    }
  };

  public static final Formatter LIKE_BUTTON = new BaseFormatter("like-button", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode item = ctx.node();
      JsonNode settings = ctx.resolve(new String[] { "websiteSettings" });
      StringBuilder buf = ctx.buffer();
      if (settings.path("simpleLikingEnabled").asBoolean()) {
        String itemId = item.path("id").asText();
        int likeCount = item.path("likeCount").asInt();
        buf.append("<span class=\"sqs-simple-like\" data-item-id=\"").append(itemId);
        buf.append("\" data-like-count=\"").append(likeCount);
        buf.append("\">'<span class=\"like-icon\"></span><span class=\"like-count\"></span></span>");
      }
    }
  };
  
  public static final Formatter SOCIAL_BUTTON = new BaseFormatter("social-button", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode website = ctx.resolve(new String[] { "website" });
      FormatterUtils.makeSocialButton(website, ctx.node(), "button-style", ctx.buffer());
    }
  };
  
  public static final Formatter SOCIAL_BUTTON_INLINE = new BaseFormatter("social-button-inline", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode website = ctx.resolve(new String[] { "website" });
      FormatterUtils.makeSocialButton(website, ctx.node(), "inline-style", ctx.buffer());
    }
  };

  public static final Formatter TWITTER_FOLLOW_BUTTON = new BaseFormatter("twitter-follow-button", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode account = ctx.node();
      String userName = account.path("userName").asText();
      if (userName.equals("")) {
        String profileUrl = account.path("profileUrl").asText();
        String[] parts = StringUtils.split(profileUrl, '/');
        userName = parts[parts.length - 1];
      }
      // IN PROGRESS...
    }
  };
  
  /*
  
      'twitter-follow-button': function(connectedAccount) {
      // If the connected account is old and doesn't have a userName set,
      // try to use the end of their profileUrl. This should be https://twitter.com/RyanCGee
      // for example.
      var userName = connectedAccount.userName || connectedAccount.profileUrl.split('/').pop();

      return "<script>Y.use('squarespace-follow-buttons', function(Y) { Y.on('domready', function() { Y.Squarespace.FollowButtonUtils.renderAll(); }); });</script><div class=\"squarespace-follow-button\" data-username=\"" + userName + "\"></div>";
    },
  
  */

}
