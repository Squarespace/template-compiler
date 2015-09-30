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

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.template.Arguments;
import com.squarespace.template.BaseFormatter;
import com.squarespace.template.CodeException;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Compiler;
import com.squarespace.template.Context;
import com.squarespace.template.Formatter;
import com.squarespace.template.FormatterRegistry;
import com.squarespace.template.GeneralUtils;
import com.squarespace.template.Instruction;
import com.squarespace.template.StringView;
import com.squarespace.template.SymbolTable;
import com.squarespace.template.plugins.PluginDateUtils;


/**
 * Extracted from Commons library at commit ab4ba7a6f2b872a31cb6449ae9e96f5f5b30f471
 */
public class SocialFormatters implements FormatterRegistry {

  @Override
  public void registerFormatters(SymbolTable<StringView, Formatter> table) {
    table.add(new ActivateTwitterLinksFormatter());
    table.add(new CommentCountFormatter());
    table.add(new CommentLinkFormatter());
    table.add(new CommentsFormatter());
    table.add(new GoogleCalendarUrlFormatter());
    table.add(new LikeButtonFormatter());
    table.add(new SocialButtonFormatter());
    table.add(new SocialButtonInlineFormatter());
    table.add(new TwitterFollowButtonFormatter());
  }

  private static final String CALENDAR_DATE_FORMAT = "%Y%m%dT%H%M%SZ";

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


  public static class ActivateTwitterLinksFormatter extends BaseFormatter {

    public ActivateTwitterLinksFormatter() {
      super("activate-twitter-links", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      String text = node.asText();
      text = TWITTER_LINKS_REGEX.matcher(text).replaceAll(TWITTER_LINKS_REPLACE);
      text = TWITTER_TWEETS_REGEX.matcher(text).replaceAll(TWITTER_TWEETS_REPLACE);
      Matcher matcher = TWITTER_HASHTAG_REGEX.matcher(text);

      // Hate using StringBuffer, but see Sun bug 5066679
      StringBuffer buf = new StringBuffer();
      while (matcher.find()) {
        matcher.appendReplacement(buf, "<a target=\"new\" href=\"http://www.twitter.com/search/"
            + GeneralUtils.urlEncode(matcher.group(2)) + "\">" + matcher.group(2) + "</a>");
      }
      matcher.appendTail(buf);
      return ctx.buildNode(buf.toString());
    }
  }

  public static class CommentsFormatter extends BaseFormatter {

    private Instruction template;

    public CommentsFormatter() {
      super("comments", false);
    }

    @Override
    public void initialize(Compiler compiler) throws CodeException {
      String source = loadResource(SocialFormatters.class, "comments.html");
      this.template = compiler.compile(source).code();
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode item) throws CodeExecuteException {
      return execute(ctx, template, item, false);
    }
  }


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

  public static class CommentLinkFormatter extends BaseFormatter {

    private Instruction template;

    public CommentLinkFormatter() {
      super("comment-link", false);
    }

    @Override
    public void initialize(Compiler compiler) throws CodeException {
      String source = loadResource(SocialFormatters.class, "comment-link.html");
      this.template = compiler.compile(source).code();
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode item) throws CodeExecuteException {
      return execute(ctx, template, item, false);
    }
  }

  public static class CommentCountFormatter extends BaseFormatter {

    public CommentCountFormatter() {
      super("comment-count", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode item) throws CodeExecuteException {
      StringBuilder buf = new StringBuilder();
      addCommentCount(item, buf);
      return ctx.buildNode(buf.toString());
    }
  }

  public static class GoogleCalendarUrlFormatter extends BaseFormatter {

    public GoogleCalendarUrlFormatter() {
      super("google-calendar-url", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      StringBuilder buf = new StringBuilder();
      long start = node.path("startDate").asLong();
      long end = node.path("endDate").asLong();
      buf.append("http://www.google.com/calendar/event?action=TEMPLATE&text=");
      buf.append(GeneralUtils.urlEncode(node.path("title").asText()));
      buf.append("&dates=");
      PluginDateUtils.formatDate(Locale.US, CALENDAR_DATE_FORMAT, start, "UTC", buf);
      buf.append("/");
      PluginDateUtils.formatDate(Locale.US, CALENDAR_DATE_FORMAT, end, "UTC", buf);
      if (node.has("location")) {
        String location = getLocationString(node.get("location"));
        if (StringUtils.trimToNull(location) != null) {
          buf.append("&location=").append(GeneralUtils.urlEncode(location));
        }
      }
      return ctx.buildNode(buf.toString());
    }
  }

  private static String getLocationString(JsonNode location) {
    StringBuilder sb = new StringBuilder();
    String addressLine1 = location.path("addressLine1").asText();
    String addressLine2 = location.path("addressLine2").asText();
    String addressCountry = location.path("addressCountry").asText();
    if (StringUtils.trimToNull(addressLine1) != null) {
      sb.append(addressLine1);
    }
    if (StringUtils.trimToNull(addressLine2) != null) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(addressLine2);
    }
    if (StringUtils.trimToNull(addressCountry) != null) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(addressCountry);
    }
    return sb.toString();
  }


  public static class LikeButtonFormatter extends BaseFormatter {

    private Instruction template;

    public LikeButtonFormatter() {
      super("like-button", false);
    }

    @Override
    public void initialize(Compiler compiler) throws CodeException {
      String source = loadResource(SocialFormatters.class, "like-button.html");
      this.template = compiler.compile(source).code();
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode item) throws CodeExecuteException {
      return this.execute(ctx, template, item, false);
    }
  }

  public static class SocialButtonFormatter extends BaseFormatter {

    public SocialButtonFormatter() {
      super("social-button", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      JsonNode website = ctx.resolve("website");
      StringBuilder buf = new StringBuilder();
      PlatformUtils.makeSocialButton(website, node, false, buf);
      return ctx.buildNode(buf.toString());
    }
  }

  public static class SocialButtonInlineFormatter extends BaseFormatter {

    public SocialButtonInlineFormatter() {
      super("social-button-inline", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode node) throws CodeExecuteException {
      JsonNode website = ctx.resolve("website");
      StringBuilder buf = new StringBuilder();
      PlatformUtils.makeSocialButton(website, node, true, buf);
      return ctx.buildNode(buf.toString());
    }
  }


  public static class TwitterFollowButtonFormatter extends BaseFormatter {

    public TwitterFollowButtonFormatter() {
      super("twitter-follow-button", false);
    }

    @Override
    public JsonNode apply(Context ctx, Arguments args, JsonNode account) throws CodeExecuteException {
      StringBuilder buf = new StringBuilder();
      String userName = account.path("userName").asText();
      if (userName.equals("")) {
        String profileUrl = account.path("profileUrl").asText();
        String[] parts = StringUtils.split(profileUrl, '/');
        userName = parts[parts.length - 1];
      }
      buf.append("<script>Y.use('squarespace-follow-buttons', function(Y) { ");
      buf.append("Y.on('domready', function() { Y.Squarespace.FollowButtonUtils.renderAll(); }); });");
      buf.append("</script><div class=\"squarespace-follow-button\" data-username=\"");
      buf.append(userName);
      buf.append("\"></div>");
      return ctx.buildNode(buf.toString());
    }
  }

}
