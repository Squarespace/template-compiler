package com.squarespace.template.plugins.platform.enums;

import static com.squarespace.template.GeneralUtils.getOrDefault;
import static com.squarespace.template.plugins.platform.enums.EnumUtils.codeMap;

import java.util.Map;

/**
 * This mirrors the cms-legacy-data enum of the same name.
 */
public enum BlockType implements PlatformEnum {

  UNDEFINED(-1, "undefined"),
  @Deprecated
  NAVIGATION(1, "navigation"),
  HTML(2, "html"),
  LOGO(3, "logo"),
  MAP(4, "map"),
  IMAGE(5, "image"),
  TWITTER(6, "twitter"),
  @Deprecated
  JOURNAL(7, "journal"), // does not exist in production
  GALLERY(8, "gallery"),
  FORM(9, "form"),
  GEO(10, "geo"),
  @Deprecated
  INDEX(11, "index"), // does not exist in production
  COLLECTION_LINK(12, "collectionlink"),
  LINK(13, "link"),
  TAGCLOUD(14, "tagcloud"),
  COMMENTS(16, "comments"),
  FOLDER(17, "folder"),
  MENU(18, "menu"),
  @Deprecated
  SOCIAL_LINKS(19, "sociallinks"),
  SUMMARY(20, "summary"),
  SPACER(21, "spacer"),
  EMBED(22, "embed"),
  CODE(23, "code"),
  FOURSQUARE(24, "foursquare"),
  INSTAGRAM(25, "instagram"),
  CALENDAR(26, "calendar"),
  POSTS_BY_AUTHOR(27, "postsbyauthor"),
  POSTS_BY_TAG(28, "postsbytag"),
  POSTS_BY_CATEGORY(29, "postsbycategory"),
  POSTS_BY_MONTH(30, "postsbymonth"),
  QUOTE(31, "quote"),
  VIDEO(32, "video"),
  SEARCH(33, "search"),
  AUDIO(41, "audio"),
  FIVEHUNDREDPIX(42, "fivehundredpix"),
  PRODUCT(43, "product"),
  MARKDOWN(44, "markdown"),
  FLICKR(45, "flickr"),
  AMAZON(46, "amazon"),
  HORIZONTAL_RULE(47, "horizontalrule"),
  @Deprecated
  SOCIAL_ACCOUNT_LINKS(48, "socialaccountlinks"),
  RSS(49, "rss"),
  OPENTABLE(50, "opentable"),
  NEWSLETTER(51, "newsletter"),
  DONATION(52, "donation"),
  BUTTON(53, "button"),
  SOCIAL_ACCOUNT_LINKS_V2(54, "socialaccountlinks-v2"),
  SUMMARY_V2(55, "summary-v2"),
  SOUNDCLOUD(56, "soundcloud"),
  EMAIL_FOOTER(57, "emailfooter"),
  IN_BROWSER_MESSAGE_LINK(58, "in-browser-message-link"),
  TOURDATES(59, "tourdates"),
  @Deprecated
  ALBUM(60, "album"),
  ARCHIVE(61, "archive"),
  CHART(62, "chart"),
  ZOLA(63, "zola"),
  ACUITY(65, "acuity"),
  OPENTABLE_V2(66, "opentable-v2"),
  MEMBER_AREA(67, "member-area"),
  TOCK(68, "tock"),
  ACCORDION(69, "accordion");

  private final int code;

  private final String stringValue;

  private static final Map<Integer, BlockType> CODE_MAP = codeMap(BlockType.class);

  BlockType(int code, String stringValue) {
    this.code = code;
    this.stringValue = stringValue;
  }

  @Override
  public int code() {
    return code;
  }

  @Override
  public String stringValue() {
    return stringValue;
  }

  public static BlockType fromCode(int code) {
    return getOrDefault(CODE_MAP, code, UNDEFINED);
  }

}

