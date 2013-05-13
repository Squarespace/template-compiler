package com.squarespace.template;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;


public class SocialFormatters extends BaseRegistry<Formatter> {

  private static final Pattern TWITTER_LINKS_REGEX = Pattern.compile(
      "(\\b(https?|ftp|file):\\/\\/[-A-Z0-9+&@#\\/%?=~_|!:,.;]*[-A-Z0-9+&@#\\/%=~_|])", Pattern.CASE_INSENSITIVE
      );
  
  private static final String TWITTER_LINKS_REPLACE =
      "<a target=\"new\" href=\"$1\">$1</a>";
  
  private static final Pattern TWITTER_TWEETS_REGEX = Pattern.compile(
      "(^| )@([a-zA-Z0-9_]+)", Pattern.CASE_INSENSITIVE
      );
  
  private static final String TWITTER_TWEETS_REPLACE = 
      "$1<a target=\"new\" href=\"http://www.twitter.com/$2/\">@$2</a>";
  
  private static final Pattern TWITTER_HASHTAG_REGEX = Pattern.compile(
      "(^| )#([a-zA-Z0-9_]+)", Pattern.CASE_INSENSITIVE
      );
  
  
  public static final Formatter ACTIVATE_TWITTER_LINKS = new BaseFormatter("activate-twitter-links", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      String text = ctx.node().asText();
      text = TWITTER_LINKS_REGEX.matcher(text).replaceAll(TWITTER_LINKS_REPLACE);
      text = TWITTER_TWEETS_REGEX.matcher(text).replaceAll(TWITTER_TWEETS_REPLACE);
      Matcher matcher = TWITTER_HASHTAG_REGEX.matcher(text);

      // Hate using StringBuffer, but see Sun bug 5066679
      StringBuffer buf = new StringBuffer();
      while (matcher.find()) {
        matcher.appendReplacement(buf, "<a target=\"new\" href=\"http://www.twitter.com/search/" +
            GeneralUtils.urlEncode(matcher.group(2)) + "\">" + matcher.group(2) + "</a>");
      }
      matcher.appendTail(buf);
      ctx.append(buf.toString());
    }
  };
  
  
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
  
  private static final String CALENDAR_DATE_FORMAT = "%Y%m%dT%H%M%SZ";
  
  public static final Formatter GOOGLE_CALENDAR_URL = new BaseFormatter("google-calendar-url", false) {
    
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode node = ctx.node();
      long start = node.path("startDate").asLong();
      long end = node.path("endDate").asLong();
      ctx.append("http://www.google.com/calendar/event?action=TEMPLATE&ext=");
      ctx.append(node.path("title").asText());
      ctx.append("&dates=");
      FormatterDateUtils.formatDate(CALENDAR_DATE_FORMAT, start, "UTC", ctx.buffer());
      ctx.append("/");
      FormatterDateUtils.formatDate(CALENDAR_DATE_FORMAT, end, "UTC", ctx.buffer());
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
      ctx.append("<script>Y.use('squarespace-follow-buttons', function(Y) { ");
      ctx.append("Y.on('domready', function() { Y.Squarespace.FollowButtonUtils.renderAll(); }); });");
      ctx.append("</script><div class=\"squarespace-follow-button\"");
      ctx.append(userName);
      ctx.append("\"></div>");
    }
  };

}
